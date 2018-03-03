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

import static com.facebook.swift.codec.metadata.ReflectionHelper.findAnnotatedMethods;
import static com.facebook.swift.codec.metadata.ReflectionHelper.getEffectiveClassAnnotations;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 19:25
 */
@Immutable
public class ThriftServiceMetadata
{
    private final String name;
    private final ImmutableList<ThriftServiceMetadata> parentServices;
    private final Map<String, ThriftMethodHandler> methodKeyHandlers;
    private final Map<String, ThriftMethodHandler> methodHandlers;
    private final Class<?> serviceClass;
   
    public ThriftServiceMetadata(Class<?> serviceClass, ThriftCodecManager codecManager)
    {
        Preconditions.checkNotNull(serviceClass, "serviceClass is null");
        ThriftService thriftService = getThriftServiceAnnotation(serviceClass);

        if (thriftService.value().length() == 0) {
            name = serviceClass.getSimpleName();
        }
        else {
            name = thriftService.value();
        }
        this.serviceClass = serviceClass;
        ImmutableMap.Builder<String, ThriftMethodMetadata> builder = ImmutableMap.builder();

        for (Method method : findAnnotatedMethods(serviceClass, ThriftMethod.class)) {
            if (method.isAnnotationPresent(ThriftMethod.class)) {
                ThriftMethodMetadata methodMetadata = new ThriftMethodMetadata(name, method, codecManager.getCatalog());
                builder.put(methodMetadata.getName(), methodMetadata);
            }
        }
        
        Map<String, ThriftMethodMetadata> methods = builder.build();
     
        ImmutableList.Builder<ThriftServiceMetadata> parentServiceBuilder = ImmutableList.builder();
        for (Class<?> parent : serviceClass.getInterfaces()) {
            if (!getEffectiveClassAnnotations(parent, ThriftService.class).isEmpty()) {
                parentServiceBuilder.add(new ThriftServiceMetadata(parent, codecManager));
            }
        }
        this.parentServices = parentServiceBuilder.build();
        
        ImmutableMap.Builder<String, ThriftMethodHandler> methodKeyHandlers = ImmutableMap.builder();
        ImmutableMap.Builder<String, ThriftMethodHandler> methodHandlers = ImmutableMap.builder();
        for (ThriftMethodMetadata methodMetadata : methods.values()) {
            ThriftMethodHandler methodHandler = new ThriftMethodHandler(methodMetadata, codecManager);
            String name = methodMetadata.getName();
            methodKeyHandlers.put(name, methodHandler);
            if(!name.equals(methodMetadata.getMethod().getName())){
            	 methodHandlers.put(methodMetadata.getMethod().getName(), methodHandler); 	
            }
           
        }
        this.methodKeyHandlers = methodKeyHandlers.build();
        this.methodHandlers = methodHandlers.build();
    }
    
    
    public Class<?> getServiceClass()
    {
        return serviceClass;
    }

    public String getName()
    {
        return name;
    }
    
    
    public ThriftMethodHandler getMethodHandler(String methodName)
    {
    	ThriftMethodHandler methodHandler = methodKeyHandlers.get(methodName);
    	if(methodHandler !=  null){
    		return methodHandler;
    	}
        return methodHandlers.get(methodName);
    }
    
    
    public Map<String, ThriftMethodHandler> getMethodHandlers()
    {
        return methodKeyHandlers;
    }
    

    public static ThriftService getThriftServiceAnnotation(Class<?> serviceClass)
    {
        Set<ThriftService> serviceAnnotations = getEffectiveClassAnnotations(serviceClass, ThriftService.class);
        Preconditions.checkArgument(!serviceAnnotations.isEmpty(), "Service class %s is not annotated with @ThriftService", serviceClass.getName());
        Preconditions.checkArgument(serviceAnnotations.size() == 1,
                "Service class %s has multiple conflicting @ThriftService annotations: %s",
                serviceClass.getName(),
                serviceAnnotations
        );

        return Iterables.getOnlyElement(serviceAnnotations);
    }

    public ImmutableList<ThriftServiceMetadata> getParentServices()
    {
        return parentServices;
    }

    public ThriftServiceMetadata getParentService()
    {
        // Assert that we have 0 or 1 parent.
        // Having multiple @ThriftService parents is generally supported by swift,
        // but this is a restriction that applies to swift2thrift generator (because the Thrift IDL doesn't)
        Preconditions.checkState(parentServices.size() <= 1);

        if (parentServices.isEmpty()) {
            return null;
        } else {
            return parentServices.get(0);
        }
    }
    
}
