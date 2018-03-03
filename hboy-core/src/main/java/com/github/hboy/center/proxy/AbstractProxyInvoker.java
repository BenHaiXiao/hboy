package com.github.hboy.center.proxy;


import java.lang.reflect.Method;

import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 9:10
 */
public abstract class AbstractProxyInvoker<T> implements Invoker<T> {
    
    private final Method method;
    
    private final Object service;
    
    private final InvokerConfig url;

    public AbstractProxyInvoker(Method method, Object service, InvokerConfig url){
        if (method == null) {
            throw new IllegalArgumentException("method == null");
        }
        if (service == null) {
            throw new IllegalArgumentException("service == null");
        }
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.method = method;
        this.service = service;
        this.url = url;
    }

    public Class<T> getInterface() {
        return url.getInterface();
    }

    public InvokerConfig getUrl() {
        return url;
    }

    public boolean isAvailable() {
        return true;
    }

    public void destroy() {
    }

    public Object invoke(Invocation invocation) throws Throwable {
        return doInvoke(method, service, invocation.getParameterTypes(), invocation.getParameters());
    }
    
    protected abstract Object doInvoke(Method method, Object service, Class<?>[] parameterTypes, Object[] arguments) throws Throwable;

    @Override
    public String toString() {
        return url.getInterfaceName() + "." + method.getName() +" -> " + url.toString();
    }

    
}