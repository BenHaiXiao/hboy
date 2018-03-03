package com.github.hboy.center.remoting.netty;

import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.hboy.common.util.Constants;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.codec.Codec;
import com.github.hboy.center.remoting.exchange.TimeoutException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/2/12
 * Time: 14:36
 */
public class NettyClient extends AbstractClient {

    private ClientBootstrap bootstrap;

    private Channel channel;
    
    private static final NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), Constants.DEFAULT_IO_THREADS);

    public NettyClient(InvokerConfig url,ChannelEventHandler handler,Codec codec) throws RemotingException {
    	super(url,handler,codec);
    }

	public void start() throws RemotingException {
		try {
			bootstrap = new ClientBootstrap(channelFactory);
			bootstrap.setOption("keepAlive", true);
			bootstrap.setOption("tcpNoDelay", true);
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() {
					NettyCodec nettyCodec = new NettyCodec(codec,NettyClient.this);
					ChannelPipeline pipeline = Channels.pipeline();
					pipeline.addLast("decoder", nettyCodec.getDecoder());
					pipeline.addLast("encoder", nettyCodec.getEncoder());
					pipeline.addLast("handler", NettyClient.this.handler);
					return pipeline;
				}
			});
		} catch (Exception e) {
			throw new RemotingException(String.format(
					"Netty client connect to %s failed", NettyClient.this.getAddress()), e);
		}
	}

    
    public boolean isConnected() {
        return channel != null && channel.isConnected();
    }

    public SocketAddress getRemoteAddress() {
        if(channel != null){
            return channel.getRemoteAddress();    
        }
        return null;
        
    }

    public String toString() {
        return "NettyClient [address=" + getAddress() + "]";
    }

	public SocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	public InvokerConfig getUrl() {
		if (url == null) {
	        throw new IllegalArgumentException("url == null");
	    }
		return this.url;
	}


	@Override
	protected void doClose() throws Throwable {
		if (channel != null) {
            channel.close();
        }
	}

	/**
	 * 重新连接需要回收旧通道
	 */
	@Override
	protected void doConnect() throws Throwable {
		long start = System.currentTimeMillis();
		ChannelFuture future = bootstrap.connect(newSocketAddress());
		try {
			//建立连接等待，超时时间
			boolean ret = future.awaitUninterruptibly(this.url.getTimeout(),
					TimeUnit.MILLISECONDS);
			if (ret && future.isSuccess()) {
				Channel newChannel = future.getChannel();
				newChannel.setInterestOps(Channel.OP_READ_WRITE);
				try {
					// 关闭旧的连接
					Channel oldChannel = NettyClient.this.channel; //  
					if (oldChannel != null) {
						if (logger.isInfoEnabled()) {
							logger.info("Close old netty channel " + oldChannel
									+ " on create new netty channel "
									+ newChannel);
						}
						oldChannel.close();
					}
				} finally {
					//检查是否被close，如果被调用close，新建立的连接关闭，不存留通道
					if (NettyClient.this.isClosed()) {
						try {
							if (logger.isInfoEnabled()) {
								logger.info("Close new netty channel "
										+ newChannel
										+ ", because the client closed.");
							}
							newChannel.close();
						} finally {
							NettyClient.this.channel = null;
						}
					} else {
						NettyClient.this.channel = newChannel;
					}
				}
			} else if (future.getCause() != null) {
				throw new RemotingException("client(url: " + getUrl()
						+ ") failed to connect to server " + getRemoteAddress()
						+ ", error message is:"
						+ future.getCause().getMessage(), future.getCause());
			} else {
				//建立连接超时
				throw new TimeoutException("client(url: " + getUrl()
						+ ") failed to connect to server " + getRemoteAddress()
						+ " client  timeout " + getUrl().getTimeout()
						+ "ms (elapsed: "
						+ (System.currentTimeMillis() - start)
						+ "ms) from client " + this.getLocalAddress()
						);
			}
		} finally {
			//如果没建立起连接，cancel掉future
			if (!isConnected()) {
				future.cancel();
			}
		}
	}
	
	@Override
	public Channel getChannel() {
		return channel;
	}
 
}
