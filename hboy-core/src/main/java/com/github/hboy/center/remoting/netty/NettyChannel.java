package com.github.hboy.center.remoting.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.Channel;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/2/17
 * Time: 9:39
 */
public final class NettyChannel implements Channel {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    private static final ConcurrentMap<org.jboss.netty.channel.Channel, NettyChannel> channelMap = new ConcurrentHashMap<org.jboss.netty.channel.Channel, NettyChannel>();

    private final org.jboss.netty.channel.Channel channel;

    protected final InvokerConfig url;
    
    protected final ChannelEventHandler handler;
    
    private NettyChannel(org.jboss.netty.channel.Channel channel, InvokerConfig url, ChannelEventHandler handler) {
    	if (url == null) {
    		throw new IllegalArgumentException("url == null");
    	}
    	if (handler == null) {
    		throw new IllegalArgumentException("handler == null");
		}
		if (channel == null) {
			throw new IllegalArgumentException("netty channel == null;");
		}
    	this.url = url;
        this.handler = handler;
        this.channel = channel;
    }

    static NettyChannel getOrAddChannel(org.jboss.netty.channel.Channel ch, InvokerConfig url, ChannelEventHandler handler) {
        if (ch == null) {
            return null;
        }
        NettyChannel ret = channelMap.get(ch);
        if (ret == null) {
            NettyChannel nc = new NettyChannel(ch, url, handler);
            if (ch.isConnected()) {
                ret = channelMap.putIfAbsent(ch, nc);
            }
            if (ret == null) {
                ret = nc;
            }
        }
        return ret;
    }

    public static NettyChannel getChannel(org.jboss.netty.channel.Channel ch) {
        if (ch == null) {
            return null;
        }
        NettyChannel ret = channelMap.get(ch);
        return ret;
    }
    
    static void removeChannelIfDisconnected(org.jboss.netty.channel.Channel ch) {
        if (ch != null && ! ch.isConnected()) {
            channelMap.remove(ch);
        }
    }

    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) channel.getLocalAddress();
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.getRemoteAddress();
    }

    public boolean isConnected() {
        return channel.isConnected();
    }

    public void send(Object message) throws RemotingException {
        try {
            ChannelFuture future = channel.write(message);
            Throwable cause = future.getCause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            throw new RemotingException("Failed to send message " + message + " to " + getRemoteAddress() + ", cause: " + e.getMessage(), e);
        }
    }

    public void close() {
        try {
            removeChannelIfDisconnected(channel);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Close netty channel " + channel);
            }
            channel.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NettyChannel other = (NettyChannel) obj;
        if (channel == null) {
            if (other.channel != null) return false;
        } else if (!channel.equals(other.channel)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "NettyChannel [channel=" + channel + "]";
    }

	@Override
	public InvokerConfig getUrl() {
		return url;
	}

}