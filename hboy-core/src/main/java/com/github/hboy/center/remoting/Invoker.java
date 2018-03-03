package com.github.hboy.center.remoting;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/14
 * Time: 16:31
 */
public interface Invoker<T> {
	
	Class<T> getInterface();

    
    InvokerConfig getUrl();
    
    /**
     */
    Object invoke(Invocation invocation) throws Throwable;
    
    
    boolean isAvailable();
    /**
     */
    void destroy();
}
