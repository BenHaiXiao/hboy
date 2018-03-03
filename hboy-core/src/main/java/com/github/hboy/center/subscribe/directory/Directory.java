 
package com.github.hboy.center.subscribe.directory;

import java.util.List;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/25
 * Time: 19:28
 */
public interface Directory<T> {
    
    /**
     * 
     */
    List<Invoker<T>> list(Invocation invocation) throws RemotingException;
    
    
    void notify(List<InvokerConfig> urls);
    
    
    void destroy();
    
    
    boolean isAvailable();
    
    
    boolean isDestroyed();
    
    
    InvokerConfig getUrl();
    
}