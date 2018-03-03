package com.github.hboy.center.remoting.transport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.codec.CodecFactory;
import com.github.hboy.center.remoting.exchange.ExchangeClient;
import com.github.hboy.center.remoting.exchange.HeaderExchangeClient;
import com.github.hboy.center.remoting.exchange.HeaderExchangeServer;
import com.github.hboy.center.remoting.http.HttpClient;
import com.github.hboy.center.remoting.http.HttpWrappedClient;
import com.github.hboy.center.remoting.netty.NettyClient;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.dispatcher.DispatchHandler;
import com.github.hboy.center.remoting.exchange.ExchangeServer;
import com.github.hboy.center.remoting.netty.NettyServer;
import com.github.hboy.common.config.InvokerConfig;

/**
 * TODO:这个只有静态方法的类应该需要改造。功能上看这更像是一个工厂
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 19:31
 */
public class Transporters {

	
	private static ConcurrentMap<Class<?>,DispatchHandler> handlerMap = new ConcurrentHashMap<Class<?>,DispatchHandler>();

	public static ExchangeClient httpWrapperClient(InvokerConfig invokerConfig){
	    if(invokerConfig == null){
            throw new IllegalArgumentException("invokerConfig == null");
	    }
	    HttpClient client = new HttpClient(invokerConfig);
	    return new HttpWrappedClient(client);
	}
	
    public static ExchangeClient connect(InvokerConfig url,ChannelEventHandler handler) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        NettyClient client = new NettyClient(url,handler, CodecFactory.getCodec(url));
        return new HeaderExchangeClient(client);
    }

    private Transporters(){
    }
    
	public static ExchangeServer bind(InvokerConfig url,ChannelEventHandler handler) throws RemotingException {
		if (url == null) {
			throw new IllegalArgumentException("url == null");
		}
		if (handler == null) {
			throw new IllegalArgumentException("handler == null");
		}
		NettyServer server = new NettyServer(url,getDispatchHandler(handler),CodecFactory.getCodec(url));
		return new HeaderExchangeServer(server);
	}
	
	private static DispatchHandler getDispatchHandler(ChannelEventHandler handler){
		Class<?> clazz = handler.getClass();
		DispatchHandler dispatchHandler = handlerMap.get(clazz);
		if(dispatchHandler == null){
			handlerMap.putIfAbsent(clazz, new DispatchHandler(handler));
			dispatchHandler = handlerMap.get(clazz);
		}
		return dispatchHandler;
	}
	
	public static Map<Class<?>,DispatchHandler> getDispatchHandlers(){
		return handlerMap;
	}
	
}