package com.github.hboy.center.remoting.codec.thrift;



import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.exchange.Request;
import com.github.hboy.center.remoting.protocol.thrift.ThriftProtocol;
import com.github.hboy.center.thrift.protocol.ThriftProtocolFactory;
import com.github.hboy.common.util.Constants;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.codec.Codec;
import com.github.hboy.center.remoting.exchange.Response;
import com.github.hboy.center.remoting.protocol.ProtocolWrapper;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 20:25
 */
public class ThriftCodec implements Codec {


    private final InvokerConfig url;
    
    private final ThriftProtocol protocol;
    
    public ThriftCodec(InvokerConfig url){
    	this.url = url;
    	//TODO 
    	ProtocolWrapper p = (ProtocolWrapper)ProtocolWrapper.getProtocol();
    	protocol = (ThriftProtocol)p.getProtocol(url);
    }
    
    public static final String NAME = "thrift";

    public ChannelBuffer encode(Object message)
            throws RemotingException {
        if ( message instanceof Request) {
            return encodeRequest((Request)message);
        } else if ( message instanceof Response ) {
        	 return encodeResponse((Response)message);
        } else {
            throw new RemotingException("Thrift codec only support encode" + Request.class.getName() + " or " + Response.class.getName());
        }
    }
    
    /**
     *  ChannelBuffer 转换为 Response 对象
     */
	public Object decode(ChannelBuffer buffer) throws RemotingException {
        int readable = buffer.readableBytes();
        if (readable < 4) {
            return null;
        }
	    buffer.markReaderIndex();
        int size = buffer.readInt();
        if (buffer.readableBytes() < size) {
            buffer.resetReaderIndex();
            return null;
        }
        if (size >= Constants.DEFAULT_MAX_MESSAGE_SIZE) {
            throw new RemotingException("The  Buffer:" + buffer +" size:" + size +" is too big, max payload:" + Constants.DEFAULT_MAX_MESSAGE_SIZE);
        }
		// 获取 protocol 类型
		TIOStreamTransport transport = new TIOStreamTransport(
				new ChannelBufferInputStream(buffer));
		TProtocol protocol = ThriftProtocolFactory.getTProtocol(transport,
				url.getThriftProtocol());

		// 原生thrift协议头
		TMessage message = null;
		try {
			message = protocol.readMessageBegin();
		} catch (TException e) {
			throw new RemotingException(
					RemotingException.SERIALIZATION_EXCEPTION, e.getMessage(),
					e);
		}
		String name = message.name;
		ThriftMethodHandler methodHandler = getMethodHandler(name);
        if(methodHandler == null){
        	throw new RemotingException(
    				RemotingException.SERIALIZATION_EXCEPTION,
    				"Could not infer service result class name from method name "
    						+ name);
        }
		
		if (message.type == TMessageType.EXCEPTION) {
			Response response = new Response();
			response.setId(message.seqid);
			response.setMethodName(message.name);
            TApplicationException exception = null;
			try {
				exception = TApplicationException.read(protocol);
				protocol.readMessageEnd();
			} catch (TException e) {
				throw new RemotingException(
						RemotingException.SERIALIZATION_EXCEPTION,
						e.getMessage(), e);
			}
            response.setError(exception);
			response.setErrorMessage(exception.getMessage());
			response.setStatus(Response.BAD_ERROR);
			return response;
        }
		if (message.type == TMessageType.REPLY) {
			Response response = new Response();
			response.setId(message.seqid);
			response.setMethodName(message.name);
			try {
				response.setResult(methodHandler.readResponse(protocol));
			} catch (Exception e) {
				response.setError(e);
				response.setErrorMessage(e.getMessage());
				response.setStatus(Response.BAD_ERROR);
			}
			return response;
		}
		if (message.type == TMessageType.CALL) {
			Request request = new Request(message.seqid);
			try {
				ThriftServiceMetadata serviceMetadata = getThriftServiceMetadata(name);
				Invocation inv = methodHandler.readArguments(protocol,serviceMetadata.getServiceClass());
				request.setData(inv);
			} catch (Exception e) {
				throw new RemotingException(
						RemotingException.SERIALIZATION_EXCEPTION,
						e.getMessage(), e);
			}
			return request;
		}
		// 如果类型没有匹配,抛异常.
		throw new RemotingException(RemotingException.SERIALIZATION_EXCEPTION);
	}

    /**
     * request 转为二进制
     * @param request
     * @return
     * @throws RemotingException
     */
	private ChannelBuffer encodeRequest(Request request)
            throws RemotingException {

    	Invocation inv = ( Invocation ) request.getData();
        int seqId = request.getId();
        
        DynamicChannelBuffer buffer = new DynamicChannelBuffer(1024);
        TIOStreamTransport transport = new TIOStreamTransport(new ChannelBufferOutputStream(buffer));
        TProtocol protocol = ThriftProtocolFactory.getTProtocol(transport, url.getThriftProtocol());
        
        ThriftMethodHandler methodHandler = getMethodHandler(inv.getPath());
		try {
			methodHandler.writeArguments(protocol, seqId, inv.getParameters());
		} catch (Exception e) {
			throw new RemotingException(
					RemotingException.SERIALIZATION_EXCEPTION,
					e.getMessage(), e);
		}
        // set buffer
        int messageLength = buffer.readableBytes();
        DynamicChannelBuffer headBuffer = new DynamicChannelBuffer(4);
        headBuffer.writeInt(messageLength);
        int dataSize = 4 + messageLength;
        if (dataSize >= Constants.DEFAULT_MAX_MESSAGE_SIZE) {
            throw new RemotingException("The request:" + request +" size:" + dataSize +" is too big, max payload:" + Constants.DEFAULT_MAX_MESSAGE_SIZE);
        }
        return ChannelBuffers.wrappedBuffer(headBuffer,buffer);
    }
	
	/**
     * response 转为二进制
     * @param request
     * @return
     * @throws RemotingException
     */
	private ChannelBuffer encodeResponse(Response response)
            throws RemotingException {

        int seqId = response.getId();
        String methodName = response.getMethodName();
        
        //原生thrift消息头
        TMessage message = new TMessage(methodName,TMessageType.REPLY,seqId );
        
		ThriftMethodHandler  methodHandler = this.getMethodHandler(methodName);
        // protocol
        DynamicChannelBuffer buffer = new DynamicChannelBuffer(1024);
        TIOStreamTransport transport = new TIOStreamTransport(new ChannelBufferOutputStream(buffer));
        TProtocol protocol = ThriftProtocolFactory.getTProtocol(transport, url.getThriftProtocol());
		try {
			protocol.writeMessageBegin(message);
			if(response.getStatus() == Response.OK){
				methodHandler.writeSuccessResponse(protocol, response.getResult());
			}else{
				methodHandler.writeFailureResponse(protocol, response.getError());
			}
			protocol.writeMessageEnd();
		} catch (Exception e) {
			throw new RemotingException(
					RemotingException.SERIALIZATION_EXCEPTION,
					e.getMessage(), e);
		}
        // set buffer
        int messageLength = buffer.readableBytes();
        DynamicChannelBuffer lengthBuffer = new DynamicChannelBuffer(4);
        lengthBuffer.writeInt(messageLength);
        int dataSize = 4 + messageLength;
        if (dataSize >= Constants.DEFAULT_MAX_MESSAGE_SIZE) {
            throw new RemotingException("The response:" + response +" size:" + dataSize +" is too big, max payload:" + Constants.DEFAULT_MAX_MESSAGE_SIZE);
        }
        return ChannelBuffers.wrappedBuffer(lengthBuffer,buffer);
    }
	
	
	private ThriftServiceMetadata getThriftServiceMetadata(String methodName) throws RemotingException{
		ThriftServiceMetadata thriftServiceMetadata = protocol.getMethodMetadata(methodName);
		if(thriftServiceMetadata == null){
			throw new RemotingException(
					RemotingException.SERIALIZATION_EXCEPTION,
					"Could not infer service result class name from method name "
							+ methodName);
		}
		return thriftServiceMetadata;
    }
	
	
	private ThriftMethodHandler getMethodHandler(String methodName) throws RemotingException{
		ThriftServiceMetadata thriftServiceMetadata = getThriftServiceMetadata(methodName);
		return thriftServiceMetadata.getMethodHandler(methodName);
    }
}
