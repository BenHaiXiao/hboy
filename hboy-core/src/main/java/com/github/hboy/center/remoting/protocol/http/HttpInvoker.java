package com.github.hboy.center.remoting.protocol.http;

import com.github.hboy.center.remoting.exchange.ExchangeClient;
import com.github.hboy.center.remoting.protocol.AbstractInvoker;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 14:35
 */
public class HttpInvoker<T> extends AbstractInvoker<T> {

    public HttpInvoker(Class<T> serviceType, InvokerConfig invokerConfig, ExchangeClient[] exchangeClients) {
        super(serviceType, invokerConfig, exchangeClients);
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
