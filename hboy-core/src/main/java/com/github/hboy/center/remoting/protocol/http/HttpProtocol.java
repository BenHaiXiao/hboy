package com.github.hboy.center.remoting.protocol.http;

import java.util.List;

import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.exchange.ExchangeClient;
import com.github.hboy.center.remoting.protocol.AbstractProtocol;
import com.github.hboy.center.remoting.transport.Transporters;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * 直接实现Protocol接口，因为AbstractProtocol与Netty体系相关，目前不适用于Http协议
 *
 * Bad smell：
 * 1.ChannelEventHandler不适用
 * 2.部分代码与AbstractProtocol中的重复
 * TODO:以后会重构，统一不同协议的顶层接口。使其优雅起来
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 14:77
 */
public class HttpProtocol extends AbstractProtocol {
    
    public <T> Invoker<T> doRefer(Class<T> serviceType, InvokerConfig invokerConfig, List<ChannelEventHandler> handlers) throws RemotingException {
        // TODO: handlers暂时无视
        return new HttpInvoker<T>(serviceType, invokerConfig, getHttpClients(invokerConfig));
    }

    private ExchangeClient[] getHttpClients(InvokerConfig invokerConfig){
        int connections = invokerConfig.getPoolSize();
        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = Transporters.httpWrapperClient(invokerConfig);
        }
        return clients; 
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T> void doExport(T service, Class<T> type, InvokerConfig url, List<ChannelEventHandler> handlers)
            throws RemotingException {
        // TODO Auto-generated method stub
        
    }
//
//    @Override
//    public <T> Invoker<T> doRefer(Class<T> type, InvokerConfig url, List<ChannelEventHandler> handlers)
//            throws RemotingException {
//        return null;
//    }

    @Override
    public Object getMethodMetadata(String methodkey) throws RemotingException {
        // TODO Auto-generated method stub
        return null;
    }

}
