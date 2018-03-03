package com.github.hboy.center.remoting;

import java.util.List;

import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 10:39
 */
public interface Protocol {
	
    
    /**
     * 引用远程服务
     * @param <T> 服务的类型
     * @param type 服务的类型
     * @param url 远程服务的URL地址
     * @param 监听通道的ChannelHandler
     * @return invoker 服务的本地代理
     * @throws RemotingException 当连接服务提供方失败时抛出
     */
    <T> Invoker<T> refer(Class<T> type, InvokerConfig url,List<ChannelEventHandler> handlers) throws RemotingException;
    
    
    /**
     * 发布服务
     * @param service 具体服务的实现
     * @param type  服务的类型
     * @param url  发布的URL
     * @param 监听通道的ChannelHandler
     * @return  Exporter
     * @throws RemotingException
     */
    <T> void export(T service, Class<T> type, InvokerConfig url,List<ChannelEventHandler> handlers) throws RemotingException;
    
    
    /**
     * 释放资源
     * 1. 取消该协议所有已经暴露和引用的服务。
     * 2. 释放协议所占用的所有资源，比如连接和端口。
     */
	void destroy();
}
