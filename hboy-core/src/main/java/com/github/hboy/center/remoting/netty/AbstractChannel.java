package com.github.hboy.center.remoting.netty;

import com.github.hboy.center.remoting.Channel;
import com.github.hboy.center.remoting.ChannelEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.codec.Codec;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/2/11
 * Time: 9:36
 */
public abstract class AbstractChannel  implements Channel {
    
    
    protected  final Logger logger = LoggerFactory.getLogger(getClass());
    
    private volatile boolean     closed = false;
    
    protected final InvokerConfig url;
    
    protected final NettyHandler handler;
    
    protected final Codec codec;
    
    public AbstractChannel(InvokerConfig url, ChannelEventHandler handler, Codec codec) throws RemotingException {
    	if (url == null) {
    		throw new IllegalArgumentException("url == null");
    	}
    	if (handler == null) {
    		throw new IllegalArgumentException("handler == null");
    	}
    	if (codec == null) {
            throw new IllegalArgumentException("codec == null");
        }
    	this.codec = codec;
    	this.url = url;
        this.handler = new NettyHandler(url,handler);
        try {
            start();
        } catch (Throwable t) {
            close();
            throw new RemotingException("Failed to start " + getClass().getSimpleName() +", cause: " + t.getMessage(), t);
        }
    }
    
    public boolean isClosed(){
		return closed;
	}
   
    /**
     * 关闭
     */
    public void close() {
    	closed = true;
    }
    
    @Override
    public String toString() {
        return getClass().getName() + " [" + getLocalAddress() + " -> " + getRemoteAddress() + "]";
    }

    
    public abstract void start() throws RemotingException;
}