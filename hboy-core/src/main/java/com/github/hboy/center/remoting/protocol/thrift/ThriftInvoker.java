package com.github.hboy.center.remoting.protocol.thrift;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.exchange.ExchangeClient;
import com.github.hboy.center.remoting.protocol.AbstractInvoker;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;


/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 21:30
 */
public class ThriftInvoker<T> extends AbstractInvoker<T> {
	
 
	public ThriftInvoker(Class<T> serviceType, InvokerConfig url, ExchangeClient[] clients){
		 super(serviceType, url, clients);
	}

    @Override
    public Object doInvoke(ExchangeClient currentClient, Invocation invocation) throws Throwable {
        try {
            int timeout = getUrl().getTimeout();
            return currentClient.request(invocation, timeout).get();
        } catch (RemotingException e) {
            throw e;
        }
    }
 
}
