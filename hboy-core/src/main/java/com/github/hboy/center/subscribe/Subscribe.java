package com.github.hboy.center.subscribe;

import java.util.List;

import com.github.hboy.center.subscribe.directory.Directory;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 21:58
 */
public interface Subscribe{

	
    /**
     * 订阅服务
     * 
     */
    List<InvokerConfig> subscribe(InvokerConfig url, Directory<?> listener);
    
    /**
     * 取消订阅服务
     * 
     */
    void unsubscribe(InvokerConfig url, Directory<?> listener);
    
    
    /**
     * 注册服务
     * 
     */
    void register(InvokerConfig url);
    
    /**
     * 取消注册服务
     * 
     */
    void unregister(InvokerConfig url);
    
    /**
     * 是否可用
     * @return
     */
    boolean isAvailable();
    
    
    void destroy();
}
