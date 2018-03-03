package com.github.hboy.center.remoting.netty;



import java.net.InetSocketAddress;

import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.codec.Codec;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/11
 * Time: 9:31
 */
public abstract class AbstractServer  extends AbstractChannel  {


    protected final InetSocketAddress address;
    
    public AbstractServer(InvokerConfig url, ChannelEventHandler handler, Codec codec) throws RemotingException {
        super(url, handler, codec);
        this.address = new InetSocketAddress(url.getHost(), url.getPort());
        if (logger.isInfoEnabled()) {
            logger.info("Start " + getClass().getSimpleName() + " bind " + getAddress());
        }
    }
    
    protected abstract void doClose() throws Throwable;

    public InetSocketAddress getAddress(){
        return address;
    }
    
	public InvokerConfig getUrl() {
		return url;
	}

    public void close() {
        try {
            doClose();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    

}