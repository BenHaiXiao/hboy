/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.hboy.center.remoting.codec.thrift;

 
import static org.apache.thrift.TApplicationException.BAD_SEQUENCE_ID;
import static org.apache.thrift.TApplicationException.INTERNAL_ERROR;
import static org.apache.thrift.TApplicationException.INVALID_MESSAGE_TYPE;
import static org.apache.thrift.TApplicationException.WRONG_METHOD_NAME;
import static org.apache.thrift.protocol.TMessageType.CALL;
import static org.apache.thrift.protocol.TMessageType.EXCEPTION;
import static org.apache.thrift.protocol.TMessageType.REPLY;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.github.hboy.center.proxy.Invocation;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.facebook.swift.codec.ThriftCodec;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.internal.TProtocolReader;
import com.facebook.swift.codec.internal.TProtocolWriter;
import com.facebook.swift.codec.metadata.ThriftFieldMetadata;
import com.facebook.swift.codec.metadata.ThriftParameterInjection;
import com.facebook.swift.codec.metadata.ThriftType;
import com.google.common.base.Defaults;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 19:58
 */
public class ThriftMethodHandler
{
    private final String name;
    private final String qualifiedName;
    private final List<ParameterHandler> writeParameterCodecs;
    private final Map<Short, ThriftCodec<?>> readParameterCodecs;
    private final ThriftCodec<Object> successCodec;
    private final Map<Short, ThriftCodec<Object>> exceptionCodecsShort;
    private final Map<Class<?>, ExceptionHandler> exceptionCodecsClass;
   
    private final Map<Short, Short> thriftParameterIdToJavaArgumentListPositionMap;
    private final ImmutableList<ThriftFieldMetadata> parameters;
    private final Method method;
    
    private static final Logger logger = LoggerFactory.getLogger(ThriftMethodHandler.class);
    
    @SuppressWarnings("unchecked")
	public ThriftMethodHandler(ThriftMethodMetadata methodMetadata, ThriftCodecManager codecManager)
    {
        name = methodMetadata.getName();
        qualifiedName = methodMetadata.getQualifiedName();
        
        parameters = ImmutableList.copyOf(methodMetadata.getParameters());
        
        method = methodMetadata.getMethod();
        short javaArgumentPosition = 0;
        ImmutableMap.Builder<Short, Short> parameterOrderingBuilder = ImmutableMap.builder();
       
        // get the thrift codecs for the parameters
        ImmutableMap.Builder<Short, ThriftCodec<?>> builder = ImmutableMap.builder();
        ParameterHandler[] parameters = new ParameterHandler[methodMetadata.getParameters().size()];
        for (ThriftFieldMetadata fieldMetadata : methodMetadata.getParameters()) {
            ThriftParameterInjection parameter = (ThriftParameterInjection) fieldMetadata.getInjections().get(0);
            ParameterHandler handler = new ParameterHandler(
                    fieldMetadata.getId(),
                    fieldMetadata.getName(),
                    (ThriftCodec<Object>) codecManager.getCodec(fieldMetadata.getThriftType()));

            parameters[parameter.getParameterIndex()] = handler;
            builder.put(fieldMetadata.getId(), codecManager.getCodec(fieldMetadata.getThriftType()));
            
            parameterOrderingBuilder.put(fieldMetadata.getId(), javaArgumentPosition++);
            
        }
        writeParameterCodecs = ImmutableList.copyOf(parameters);
        readParameterCodecs = builder.build();
        thriftParameterIdToJavaArgumentListPositionMap = parameterOrderingBuilder.build();
        
        // get the thrift codecs for the exceptions
        ImmutableMap.Builder<Short, ThriftCodec<Object>> exceptionsShort = ImmutableMap.builder();
        ImmutableMap.Builder<Class<?>, ExceptionHandler> exceptionsType = ImmutableMap.builder();
        for (Map.Entry<Short, ThriftType> entry : methodMetadata.getExceptions().entrySet()) {
        	exceptionsShort.put(entry.getKey(), (ThriftCodec<Object>) codecManager.getCodec(entry.getValue()));
        	
        	Class<?> type = TypeToken.of(entry.getValue().getJavaType()).getRawType();
        	ExceptionHandler processor = new ExceptionHandler(entry.getKey(), codecManager.getCodec(entry.getValue()));
        	exceptionsType.put(type, processor);
        }
        exceptionCodecsShort = exceptionsShort.build();
        exceptionCodecsClass = exceptionsType.build();
        // get the thrift codec for the return value
        successCodec = (ThriftCodec<Object>) codecManager.getCodec(methodMetadata.getReturnType());
    }


    public Method getMethod()
    {
        return method;
    }
    
    public String getName()
    {
        return name;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public Invocation readArguments(TProtocol in, Class<?> serviceClass)
            throws Exception
    {
        try {
            Object[] args = new Object[readParameterCodecs.size()];
            TProtocolReader reader = new TProtocolReader(in);

            // Map incoming arguments from the ID passed in on the wire to the position in the
            // java argument list we expect to see a parameter with that ID.
            reader.readStructBegin();
            while (reader.nextField()) {
                short fieldId = reader.getFieldId();
                ThriftCodec<?> codec = readParameterCodecs.get(fieldId);
                if (codec == null) {
                    // unknown field
                    reader.skipFieldData();
                }
                else {
                    // Map the incoming arguments to an array of arguments ordered as the java
                    // code for the handler method expects to see them
                    args[thriftParameterIdToJavaArgumentListPositionMap.get(fieldId)] = reader.readField(codec);
                }
            }
            reader.readStructEnd();

            // Walk through our list of expected parameters and if no incoming parameters were
            // mapped to a particular expected parameter, fill the expected parameter slow with
            // the default for the parameter type.
            int argumentPosition = 0;
            for (ThriftFieldMetadata argument : parameters) {
                if (args[argumentPosition] == null) {
                    Type argumentType = argument.getThriftType().getJavaType();

                    if (argumentType instanceof Class) {
                        Class<?> argumentClass = (Class<?>) argumentType;
                        argumentClass = Primitives.unwrap(argumentClass);
                        args[argumentPosition] = Defaults.defaultValue(argumentClass);
                    }
                }
                argumentPosition++;
            }
            
            return new Invocation(method,args,serviceClass,name);
        }
        catch (TProtocolException e) {
            throw new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
        }
    }
    
    public void writeArguments(TProtocol out, int sequenceId, Object[] args)
            throws Exception
    {
      
        out.writeMessageBegin(new TMessage(name,CALL, sequenceId));

        // write the parameters
        TProtocolWriter writer = new TProtocolWriter(out);
        writer.writeStructBegin(name + "_args");
        if(args != null){
        	for (int i = 0; i < args.length; i++) {
                Object value = args[i];
                ParameterHandler parameter = writeParameterCodecs.get(i);
                writer.writeField(parameter.getName(), parameter.getId(), parameter.getCodec(), value);
            }
        }
        writer.writeStructEnd();

        out.writeMessageEnd();
        out.getTransport().flush();
    }

    public Object readResponse(TProtocol in)
            throws Exception
    {
        TProtocolReader reader = new TProtocolReader(in);
        reader.readStructBegin();
        Object results = null;
        Exception exception = null;
        while (reader.nextField()) {
            if (reader.getFieldId() == 0) {
                results = reader.readField(successCodec);
            }
            else {
                ThriftCodec<Object> exceptionCodec = exceptionCodecsShort.get(reader.getFieldId());
                if (exceptionCodec != null) {
                    exception = (Exception) reader.readField(exceptionCodec);
                }
                else {
                    reader.skipFieldData();
                }
            }
        }
        reader.readStructEnd();
        in.readMessageEnd();

        if (exception != null) {
            throw exception;
        }

        if (successCodec.getType() == ThriftType.VOID) {
            // TODO: check for non-null return from a void function?
            return null;
        }

        if (results == null) {
            throw new TApplicationException(TApplicationException.MISSING_RESULT, name + " failed: unknown result");
        }
        return results;
    }
    
    
    public <T> void writeSuccessResponse(TProtocol out, T result) throws Exception {

		TProtocolWriter writer = new TProtocolWriter(out);
		writer.writeStructBegin(name + "_result");
		writer.writeField("success", (short) 0,
				successCodec, result);
		writer.writeStructEnd();

	}
    
    
    public <T> void writeFailureResponse(TProtocol out, Throwable result) throws Exception {
    	 
    	ExceptionHandler exceptionCodec = exceptionCodecsClass.get(result.getClass());
    	if(exceptionCodec != null){
    		TProtocolWriter writer = new TProtocolWriter(out);
        	writer.writeStructBegin(name + "_result");
        	writer.writeField("exception", exceptionCodec.getId(),
        			exceptionCodec.getCodec(), result);
        	writer.writeStructEnd();
    	}else{
    		String errorMessage = "Internal error processing " + method.getName();
            TApplicationException applicationException = new TApplicationException(INTERNAL_ERROR, errorMessage);
            if (result != null) {
                applicationException.initCause(result);
            }
            logger.error(errorMessage, applicationException);
            applicationException.write(out);

    	}
	}
    
    
    

   

    public void waitForResponse(TProtocol in, int sequenceId)
            throws TException
    {
        TMessage message = in.readMessageBegin();
        if (message.type == EXCEPTION) {
            TApplicationException exception = TApplicationException.read(in);
            in.readMessageEnd();
            throw exception;
        }
        if (message.type != REPLY) {
            throw new TApplicationException(INVALID_MESSAGE_TYPE,
                                            "Received invalid message type " + message.type + " from server");
        }
        if (!message.name.equals(this.name)) {
            throw new TApplicationException(WRONG_METHOD_NAME,
                                            "Wrong method name in reply: expected " + this.name + " but received " + message.name);
        }
        if (message.seqid != sequenceId) {
            throw new TApplicationException(BAD_SEQUENCE_ID, name + " failed: out of sequence response");
        }
    }

    private static final class ParameterHandler
    {
        private final short id;
        private final String name;
        private final ThriftCodec<Object> codec;

        private ParameterHandler(short id, String name, ThriftCodec<Object> codec)
        {
            this.id = id;
            this.name = name;
            this.codec = codec;
        }

        public short getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public ThriftCodec<Object> getCodec()
        {
            return codec;
        }
    }
    
    private static final class ExceptionHandler
    {
        private final short id;
        private final ThriftCodec<Object> codec;

        @SuppressWarnings("unchecked")
		private ExceptionHandler(short id, ThriftCodec<?> coded)
        {
            this.id = id;
            this.codec = (ThriftCodec<Object>) coded;
        }

		public short getId()
        {
            return id;
        }

		public ThriftCodec<Object> getCodec()
        {
            return codec;
        }
    }
}
