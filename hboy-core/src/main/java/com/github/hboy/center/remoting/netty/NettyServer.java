package com.github.hboy.center.remoting.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.hboy.center.pooling.thread.NamedThreadFactory;
import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.common.util.Constants;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.codec.Codec;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/11
 * Time: 14:35
 */
public class NettyServer extends AbstractServer {

    private ServerBootstrap                 bootstrap;

    private org.jboss.netty.channel.Channel channel;
    
    public NettyServer(InvokerConfig url, ChannelEventHandler handler, Codec codec) throws RemotingException{
        super(url,handler,codec);
    }

    public void  start() {
        ExecutorService boss = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerBoss", true));
        ExecutorService worker = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerWorker", true));
        ChannelFactory channelFactory = new NioServerSocketChannelFactory(boss, worker, Constants.DEFAULT_IO_THREADS);
        bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                NettyCodec nettyCodec = new NettyCodec(codec,NettyServer.this);
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", nettyCodec.getDecoder());
				pipeline.addLast("encoder", nettyCodec.getEncoder());
				pipeline.addLast("handler", NettyServer.this.handler);
				return pipeline;
            }
        });
        channel = bootstrap.bind(new InetSocketAddress(url.getPort()));
    }

    
    @Override
    protected void doClose() throws Throwable {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
        	Map<String, NettyChannel> channels = NettyServer.this.handler.getChannels();
            for (NettyChannel channel : channels.values()) {
                if (channel.isConnected()) {
                	try {
                        channel.close();
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }  
            }
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (bootstrap != null) { 
                bootstrap.releaseExternalResources();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
	@Override
	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}

	public void send(Object message) throws RemotingException {
		if (channel == null || !channel.isConnected()) {
			throw new RemotingException(channel == null ? "channel is null "
					: (" channel is closed ") + ". url:" + getUrl());
		}
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

	public SocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	@Override
	public SocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

}