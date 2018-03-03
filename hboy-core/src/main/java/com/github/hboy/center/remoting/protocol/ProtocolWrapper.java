package com.github.hboy.center.remoting.protocol;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.Protocol;
import com.github.hboy.center.remoting.protocol.http.HttpProtocol;
import com.github.hboy.center.remoting.protocol.thrift.ThriftProtocol;
import com.github.hboy.common.util.Constants;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 19:45
 */
public class ProtocolWrapper implements Protocol {

    
    private static ConcurrentHashMap<Constants.ServiceProtocolType,Protocol> protocols = new ConcurrentHashMap<Constants.ServiceProtocolType,Protocol>();
    
    private static ProtocolWrapper protocolWrapper = new ProtocolWrapper();
    
    private ProtocolWrapper(){}
    
    public static Protocol  getProtocol(){
        return protocolWrapper;
    }
    
    public <T> Invoker<T> refer(Class<T> type, InvokerConfig url, List<ChannelEventHandler> handlers) throws RemotingException {
        return getProtocol(url).refer(type, url,handlers);
    }

    public Protocol getProtocol(InvokerConfig url) {
        
        Constants.ServiceProtocolType protocolType = url.getServiceProtocol();
        Protocol protocol = protocols.get(protocolType);
        if (protocol == null) {
            synchronized (ProtocolWrapper.class) {
                protocol = protocols.get(protocolType);
                if (protocol == null) {
                    if (url.getServiceProtocol() == Constants.ServiceProtocolType.thrift) {
                        protocols.putIfAbsent(url.getServiceProtocol(), new ThriftProtocol());
                    } else if(url.getServiceProtocol() == Constants.ServiceProtocolType.HTTP){
                        protocols.putIfAbsent(url.getServiceProtocol(), new HttpProtocol());
                    } else {
                        protocols.putIfAbsent(url.getServiceProtocol(), new ThriftProtocol());
                    }
                    protocol = protocols.get(protocolType);
                }
            }
        }
        return protocol;
    }

    @Override
    public <T> void export(T service, Class<T> type, InvokerConfig url,List<ChannelEventHandler> handlers) throws RemotingException {
        getProtocol(url).export(service,type, url,handlers);
    }

    @Override
    public void destroy() {
        for(Protocol protocol : protocols.values()){
            protocol.destroy();
        }
    }

  
}
