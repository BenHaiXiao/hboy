package com.github.hboy.center.remoting;

import java.net.SocketAddress;

import com.github.hboy.common.config.InvokerConfig;


/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 18:31
 */
public interface Channel {
	
	
    /**
     * 
     * @return
     */
    public boolean isConnected();
    
    /**
     * 
     * @return url
     */
    InvokerConfig getUrl();
    
    /**
     * 
     * @return
     */
    public SocketAddress getLocalAddress();
    
    /**
     * 
     * @throws RemotingException
     */
    public void send(Object message) throws RemotingException;

    /**
     * 
     * @return
     */
    public SocketAddress getRemoteAddress();

    /**
     * 关闭连接
     */
    public void close();

}
