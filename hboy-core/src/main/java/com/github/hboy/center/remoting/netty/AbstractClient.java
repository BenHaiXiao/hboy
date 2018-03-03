package com.github.hboy.center.remoting.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hboy.common.util.Constants;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.github.hboy.center.pooling.thread.NamedThreadFactory;
import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.codec.Codec;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/5/11
 * Time: 9:36
 */
public abstract class AbstractClient extends AbstractChannel {
    
 
	private final Lock connectLock = new ReentrantLock();

	protected volatile InetSocketAddress address;
	
	private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(
			2, new NamedThreadFactory("client-connect-check-timer", true));

    private volatile  ScheduledFuture<?> reconnectExecutorFuture = null;
    
    public AbstractClient(InvokerConfig url,ChannelEventHandler handler,Codec codec) throws RemotingException {
    	super(url,handler,codec);
    	this.address = new InetSocketAddress(url.getHost(), url.getPort());
        try {
            connect();
            if (logger.isInfoEnabled()) {
                logger.info("Start " + getClass().getSimpleName() + " " + this.getLocalAddress() + " connect to the server " + getRemoteAddress());
            }
		} catch (RemotingException t) {
			close();
			throw t;
		} catch (Throwable t){
            close();
            throw new RemotingException("Failed to start " + getClass().getSimpleName() 
                    + " connect to the server " + getAddress() + ", cause: " + t.getMessage(), t);
        }
    }
    
    public InetSocketAddress getAddress(){
        return address;
    }
    
    
    public InetSocketAddress newSocketAddress(){
        address = new InetSocketAddress(url.getHost(), url.getPort());
        return address;
    }
    
    
    /**
     * 定时重新连接线程
     */
    private synchronized void initConnectStatusCheckCommand(){
        int reconnect = Constants.DEFAULT_RECONNECT_PERIOD;
		if (reconnect > 0 && reconnectExecutorFuture == null) {
			Runnable connectStatusCheckCommand = new Runnable() {
				String errorMsg = "Unexpected error occur at client reconnect";

				public void run() {
					try {
						if (!isConnected()) {
							doConnect();
						}
					} catch (Throwable t) {
						logger.error(errorMsg, t);
					}
				}
			};
			reconnectExecutorFuture = reconnectExecutorService
					.scheduleWithFixedDelay(connectStatusCheckCommand,
							reconnect, reconnect, TimeUnit.MILLISECONDS);
		}
    }
    
    /**
     * 取消定时重新连接线程
     */
    private synchronized void destroyConnectStatusCheckCommand(){
        try {
            if (reconnectExecutorFuture != null && ! reconnectExecutorFuture.isDone()){
                reconnectExecutorFuture.cancel(true);
                reconnectExecutorService.purge();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    public boolean isConnected() {
        Channel channel = getChannel();
        if (channel == null)
            return false;
        return channel.isConnected();
    }

    
    
    public void send(Object message) throws RemotingException {
//        if (!isConnected()){
//            connect();
//        }
		Channel channel = getChannel();
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
    
    /**
     * 建立连接
     * @throws RemotingException
     */
    private void connect() throws RemotingException {
        connectLock.lock();
        try {
            if (isConnected()) {
                return;
            }
            initConnectStatusCheckCommand();
            doConnect();
            if (! isConnected()) {
                throw new RemotingException("Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName()
                		 + this.getLocalAddress() + ", cause: Connect wait timeout: " + getUrl().getTimeout() + "ms.");
            }
        } catch (RemotingException e) {
            throw e;
        } catch (Throwable e) {
            throw new RemotingException("Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName()
                                        + this.getLocalAddress() + ", cause: " + e.getMessage(), e);
        } finally {
            connectLock.unlock();
        }
    }
    /**
     * 取消连接
     */
    public void disconnect() {
        connectLock.lock();
        try {
            destroyConnectStatusCheckCommand();
            try {
                NettyChannel.removeChannelIfDisconnected(getChannel());
            } catch (Throwable t) {
                logger.warn(t.getMessage());
            }
            try {
                Channel channel = getChannel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
        } finally {
            connectLock.unlock();
        }
    }
    
       

   /**
    * 重新连接
    * @throws RemotingException
    */
    public void reconnect() throws RemotingException {
        disconnect();
        connect();
    }
    
    /**
     * 关闭
     */
    public void close() {
    	super.close();
        disconnect();
        try {
            doClose();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    @Override
    public String toString() {
        return getClass().getName() + " [" + getLocalAddress() + " -> " + getRemoteAddress() + "]";
    }

    /**
     * 
     */
    protected abstract void doClose() throws Throwable;

    /**
     * 
     */
    protected abstract void doConnect() throws Throwable;
    

    /**
     * 
     */
    public abstract Channel getChannel();

}