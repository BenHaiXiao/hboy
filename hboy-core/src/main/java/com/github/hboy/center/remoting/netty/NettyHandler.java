package com.github.hboy.center.remoting.netty;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.hboy.center.remoting.ChannelEventHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/11
 * Time: 14:47
 */
public class NettyHandler extends SimpleChannelHandler {

    private ChannelEventHandler handler;
    
    private InvokerConfig url;
    
    private final Map<String, NettyChannel> channels = new ConcurrentHashMap<String, NettyChannel>(); // <ip:port, channel>
    
    public NettyHandler(InvokerConfig url,ChannelEventHandler handler) {
    	this.handler = handler;
    	this.url = url;
    }
    
    public Map<String, NettyChannel> getChannels() {
        return new HashMap<String, NettyChannel>(channels);
    }
    
    /**
     * 通道建立连接之后
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    	NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
        try {
            if (channel != null) {
                channels.put(toAddressString((InetSocketAddress) ctx.getChannel().getRemoteAddress()), channel);
            }
            handler.connected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }
    
    /**
     * 通道关闭连接之后
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    	NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
        try {
            channels.remove(toAddressString((InetSocketAddress) ctx.getChannel().getRemoteAddress()));
            handler.disconnected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }
    
    /**
     * 通道收到消息
     */
    @Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
    	 NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
         try {
             handler.received(channel, e.getMessage());
         } finally {
             NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
         }
	}
    
    /**
     * 发送请求
     */
    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	super.writeRequested(ctx, e);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
        try {
            handler.sent(channel, e.getMessage());
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }
    
    /**
     * 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    	 NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
         try {
             handler.caught(channel, e.getCause());
         } finally {
             NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
         }
    }
    
    private  String toAddressString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
}
