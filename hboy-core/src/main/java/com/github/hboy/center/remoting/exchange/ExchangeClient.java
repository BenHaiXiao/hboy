package com.github.hboy.center.remoting.exchange;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.MethodCallback;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 15:25
 */
public interface ExchangeClient {
	
	 /**
     * 发送请求默认的超时时间
     * 
     * @param request
     * @return response future
     * @throws RemotingException
     */
    ResponseFuture request(Invocation inv) throws RemotingException;

    /**
     * 发送请求
     * 
     * @param request
     * @param timeout
     * @return response future
     * @throws RemotingException
     */
    ResponseFuture request(Invocation inv, int timeout) throws RemotingException;

    
    /**
     * 发送异步消息
     * @param message
     * @throws RemotingException
     */
    void send(Invocation inv) throws RemotingException;
    
    /**
     * 发送异步消息,有回调
     * @param message
     * @throws RemotingException
     */
    void send(Invocation inv,MethodCallback<Object> callback) throws RemotingException;
    
    /**
     * 
     */
    void close();
    
    /**
     * 
     * @return
     */
    boolean isClosed();
    
    /**
     * 
     * @return
     */
    InvokerConfig getUrl(); 
    
    /**
     * 
     * @return
     */
    boolean isConnected();
    
}