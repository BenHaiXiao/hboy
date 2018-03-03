package com.github.hboy.center.remoting.protocol;

import java.util.concurrent.locks.ReentrantLock;

import com.github.hboy.center.remoting.exchange.ExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 19:11
 */
public abstract class AbstractInvoker<T> implements Invoker<T> {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private final InvokerConfig url;
	
	private java.util.concurrent.atomic.AtomicInteger sequence = new java.util.concurrent.atomic.AtomicInteger();
	
	private final ExchangeClient[]      clients;

    private final ReentrantLock     destroyLock = new ReentrantLock();
	
    private final Class<T>   type;

    private volatile boolean available = true;

    private volatile boolean destroyed = false;
    
	public AbstractInvoker(Class<T> serviceType, InvokerConfig url, ExchangeClient[] clients){
		if (serviceType == null){
			throw new IllegalArgumentException("service type == null");
		}
	    if (url == null){
	    	throw new IllegalArgumentException("service url == null");
	    }
	    if (clients == null){
            throw new IllegalArgumentException("clients url == null");
        }
		this.url = url;
		this.type = serviceType;
		this.clients = clients;
	}
 

	public Object invoke(final Invocation invocation) throws Throwable {
		if(destroyed) {
	         throw new RemotingException("invoker for service " + type + " is destroyed!");
	    }
		ExchangeClient currentClient;
		if (clients.length == 1) {
			currentClient = clients[0];
		} else {
			//保证为正数
			int index = sequence.getAndIncrement();
			if (index >= Integer.MAX_VALUE || index < 0) {
				sequence.set(0);
				index = sequence.getAndIncrement();
			}
			currentClient = clients[index % clients.length];
		}
		return doInvoke(currentClient,invocation);
	}
	
	public abstract Object doInvoke(final ExchangeClient currentClient,final Invocation invocation) throws Throwable;

	@Override
	public Class<T> getInterface() {
		return url.getInterface();
	}


	@Override
	public InvokerConfig getUrl() {
		return url;
	}


	@Override
	public void destroy() {
        //,避免多次关闭
        destroyLock.lock();
        try{
            if (destroyed){
                return ;
            }
            destroyed = true;
            available = false;
            for (ExchangeClient client : clients) {
                try {
                    client.close();
                } catch (Throwable t) {
                	log.warn(t.getMessage(), t);
                }
            }
        }finally {
            destroyLock.unlock();
        }
	}
 
	@Override
	public boolean isAvailable() {
		return available;
	}
	 
}
