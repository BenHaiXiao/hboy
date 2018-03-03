package com.github.hboy.center.remoting.dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.hboy.center.pooling.thread.NamedThreadFactory;
import com.github.hboy.center.remoting.Channel;
import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 23:25
 */
public class DispatchHandler implements ChannelEventHandler {
    
    protected static final Logger logger = LoggerFactory.getLogger(DispatchHandler.class);

    protected static final ExecutorService SHARED_EXECUTOR = Executors.newCachedThreadPool(new NamedThreadFactory("ServiceSharedHandler", true));
    
    protected final ExecutorService executor;
    
    protected final ChannelEventHandler handler;

    public DispatchHandler(ChannelEventHandler handler) {
        this.handler = handler;
		executor = new ThreadPoolExecutor(0, 1000, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new NamedThreadFactory(
						"Dispatch-Service", true));
    }
    
    public void close() {
        try {
            if (executor instanceof ExecutorService) {
                ((ExecutorService)executor).shutdown();
            }
        } catch (Throwable t) {
            logger.warn("fail to destroy thread pool of server: " + t.getMessage(), t);
        }
    }

    public void connected(Channel channel) throws RemotingException {
        ExecutorService cexecutor = getExecutorService(); 
        try{
            cexecutor.execute(new ChannelEventRunnable(channel, handler , ChannelEventRunnable.ChannelState.CONNECTED));
        }catch (Throwable t) {
            throw new RemotingException("connect event" + channel + getClass()+" error when process connected event ." , t);
        }
    }
    
    public void disconnected(Channel channel) throws RemotingException {
        ExecutorService cexecutor = getExecutorService(); 
        try{
            cexecutor.execute(new ChannelEventRunnable(channel, handler , ChannelEventRunnable.ChannelState.DISCONNECTED));
        }catch (Throwable t) {
            throw new RemotingException("disconnect event " + channel +  getClass()+" error when process disconnected event ." , t);
        }
    }

    public void received(Channel channel, Object message) throws RemotingException {
        ExecutorService cexecutor = getExecutorService();
        try {
            cexecutor.execute(new ChannelEventRunnable(channel, handler, ChannelEventRunnable.ChannelState.RECEIVED, message));
        } catch (Throwable t) {
            throw new RemotingException("received event " + message  + channel +  getClass() + " error when process received event .", t);
        }
    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
        ExecutorService cexecutor = getExecutorService(); 
        try{
            cexecutor.execute(new ChannelEventRunnable(channel, handler , ChannelEventRunnable.ChannelState.CAUGHT, exception));
        }catch (Throwable t) {
            throw new RemotingException("caught event " + channel + getClass()+" error when process caught event ." , t);
        }
    }
    
    public void sent(Channel channel, Object message) throws RemotingException {
        handler.sent(channel, message);
    }
    
    private ExecutorService getExecutorService() {
        ExecutorService cexecutor = executor;
        if (cexecutor == null || cexecutor.isShutdown()) { 
            cexecutor = SHARED_EXECUTOR;
        }
        return cexecutor;
    }
    
}