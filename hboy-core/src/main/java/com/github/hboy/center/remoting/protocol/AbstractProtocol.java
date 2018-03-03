package com.github.hboy.center.remoting.protocol;

import java.util.ArrayList;
import java.util.List;

import com.github.hboy.center.filter.AccessFilter;
import com.github.hboy.center.filter.Filter;
import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.Protocol;
import com.github.hboy.center.remoting.exchange.ExchangeClient;
import com.github.hboy.center.remoting.transport.Transporters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.filter.FilterWrapper;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.exchange.ExchangeServer;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 19:55
 */
public abstract class AbstractProtocol implements Protocol {

    public Logger log = LoggerFactory.getLogger(getClass());
    
    public <T> Invoker<T> refer(Class<T> type, InvokerConfig url, List<ChannelEventHandler> handlers) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type == null");
        }
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new AccessFilter());
        if( url.getFilters() != null && url.getFilters().size() > 0 ){
            filters.addAll(url.getFilters());
        }
        return FilterWrapper.buildFilterChain(doRefer(type,url,handlers), filters);
        //return doRefer(type,url,handlers);
    }

    @Override
    public <T> void export(T service, Class<T> type, InvokerConfig url,List<ChannelEventHandler> handlers) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (service == null) {
            throw new IllegalArgumentException("service == null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type == null");
        }
        doExport(service, type, url,handlers);
    }

    public abstract <T> void doExport(T service, Class<T> type, InvokerConfig url,List<ChannelEventHandler> handlers) throws RemotingException;
     
    public abstract <T> Invoker<T> doRefer(Class<T> type, InvokerConfig url,List<ChannelEventHandler> handlers) throws RemotingException;
    
    
    protected ExchangeServer getServer(InvokerConfig url,ChannelEventHandler handler) throws RemotingException {
        return Transporters.bind(url,handler);
    }
    
    protected ExchangeClient[] getClients(InvokerConfig url, ChannelEventHandler handler) throws RemotingException{
        int connections = url.getPoolSize();
        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = Transporters.connect(url,handler);
        }
        return clients;
    }
    
    
    /**
     * 获取docec的解析对象
     * @param methodkey
     * @return
     */
    
    public abstract Object getMethodMetadata(String methodkey) throws RemotingException;
    
    
}
