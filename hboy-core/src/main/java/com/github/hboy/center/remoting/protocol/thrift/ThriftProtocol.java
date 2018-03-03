package com.github.hboy.center.remoting.protocol.thrift;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.internal.coercion.DefaultJavaCoercions;
import com.facebook.swift.codec.metadata.ThriftCatalog;
import com.facebook.swift.service.ThriftService;
import com.github.hboy.center.proxy.Exporter;
import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.EventHandlerWrapper;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.codec.thrift.ThriftMethodHandler;
import com.github.hboy.common.util.Constants;
import com.github.hboy.center.proxy.ExporterProxy;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.codec.thrift.DateCoercionUtil;
import com.github.hboy.center.remoting.codec.thrift.ThriftServiceMetadata;
import com.github.hboy.center.remoting.dispatcher.DispatchHandler;
import com.github.hboy.center.remoting.exchange.ExchangeServer;
import com.github.hboy.center.remoting.protocol.AbstractProtocol;
import com.github.hboy.center.remoting.transport.Transporters;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 9:46
 */
public class ThriftProtocol extends AbstractProtocol {
	
    public static final String NAME = "thrift";
    
    private final ConcurrentMap<String, ExchangeServer> serverMap = new ConcurrentHashMap<String, ExchangeServer>();
    
    private final ThriftHandler  thriftHandler = new ThriftHandler();
    
    private static ThriftCodecManager codecManager = new ThriftCodecManager();
    
    private static final ConcurrentMap<String, ThriftServiceMetadata> serviceCached =
            new ConcurrentHashMap<String, ThriftServiceMetadata>();
    
    private static final ConcurrentMap<String, ThriftServiceMetadata> methodCached =
            new ConcurrentHashMap<String, ThriftServiceMetadata>();
    
    public ThriftProtocol(){
        ThriftCatalog catalog = codecManager.getCatalog();
        catalog.addDefaultCoercions(DateCoercionUtil.class);
        catalog.addDefaultCoercions(DefaultJavaCoercions.class);
    }
    
    
    public <T> Invoker<T> doRefer(Class<T> type, InvokerConfig url, List<ChannelEventHandler> handlers) throws RemotingException {
//      因为tsocket的方式没有定义包长,所以只能初始化原生client的形式
        if(url.getThriftTransport() == Constants.TransportType.TSOCKET ||  !isAnnotation(type)){
            try {
                return new com.github.hboy.center.thrift.ThriftInvoker<T>(url);
            } catch (Exception e) {
                throw new RemotingException(RemotingException.NETWORK_EXCEPTION,e);
            } 
        }
        generServiceMetadata(type);
        ChannelEventHandler handler = EventHandlerWrapper.buildEventHandlerChain(thriftHandler, handlers);
    	ThriftInvoker<T> invoker = new ThriftInvoker<T>(type,url,getClients(url,handler));
    	return invoker;
    }

    private <T> boolean isAnnotation(Class<T> type){
        if (type.getAnnotation(ThriftService.class) != null) {
            return true;    
        }
        return false;
    }
    
	public <T> void doExport(T service, Class<T> type, InvokerConfig url,List<ChannelEventHandler> handlers)
			throws RemotingException {
		
        ThriftServiceMetadata serviceMetadata = generServiceMetadata(type);
        Map<String, ThriftMethodHandler> methodHandlers =  serviceMetadata.getMethodHandlers();
		for (ThriftMethodHandler methodHandler : methodHandlers.values()) {
			
			String methodkey = methodHandler.getName();
			if (thriftHandler.exporterMap.containsKey(methodkey)) {
				throw new IllegalArgumentException(
						"Multiple @ThriftMethod-annotated methods named '"
								+ methodkey + "' found in the given services");
			}
			Exporter<T> export = new ExporterProxy<T>(methodHandler.getMethod(),
					service,url);
			thriftHandler.exporterMap.putIfAbsent(methodkey, export);
			if (log.isInfoEnabled()) {
	        	log.info("export method name: " + export.toString());
	        }
		}
		ChannelEventHandler handler = EventHandlerWrapper.buildEventHandlerChain(thriftHandler, handlers);
        String key = url.getAddress();
        if (!serverMap.containsKey(key)) {
            serverMap.put(key, getServer(url, handler));
        }
	}

	
	@Override
	public void destroy() {
	    for (String key : new ArrayList<String>(thriftHandler.exporterMap.keySet())) {
            Exporter<?> exporter = thriftHandler.exporterMap.remove(key);
            if (exporter != null) {
                try {
                    if (log.isInfoEnabled()) {
                    	log.info("Unexport service: " + exporter.getInvoker().getUrl());
                    }
                    exporter.unexport();
                } catch (Throwable t) {
                	log.warn(t.getMessage(), t);
                }
            }
        }
	    for (String key : new ArrayList<String>(serverMap.keySet())) {
	    	ExchangeServer server = serverMap.remove(key);
            if (server != null) {
                try {
                    if (log.isInfoEnabled()) {
                    	log.info("Close  server: " + server.getLocalAddress());
                    }
                    server.close();
                } catch (Throwable t) {
                	log.warn(t.getMessage(), t);
                }
            }
        }
	    for (Class<?> clazz : new ArrayList<Class<?>>(Transporters.getDispatchHandlers().keySet())) {
	    	DispatchHandler handler = Transporters.getDispatchHandlers().remove(clazz);
            if (handler != null) {
                try {
                    if (log.isInfoEnabled()) {
                    	log.info("Close  DispatchHandler: " + handler.toString());
                    }
                    handler.close();
                } catch (Throwable t) {
                	log.warn(t.getMessage(), t);
                }
            }
        }
	    
	}
	
	
	public ThriftServiceMetadata generServiceMetadata(Class<?> type) throws RemotingException{
		String name = type.getName();
		ThriftServiceMetadata thriftServiceMetadata = serviceCached.get(name);
		if (thriftServiceMetadata == null) {
			serviceCached.putIfAbsent(name,
					new ThriftServiceMetadata(type,
							codecManager));
			thriftServiceMetadata = serviceCached.get(name);
			for(ThriftMethodHandler methodHandler : thriftServiceMetadata.getMethodHandlers().values()){
				methodCached.putIfAbsent(methodHandler.getName(), thriftServiceMetadata);
				methodCached.putIfAbsent(methodHandler.getMethod().getName(), thriftServiceMetadata);
			}
		}
		return thriftServiceMetadata;
    }
	
	
	public ThriftServiceMetadata getMethodMetadata(String methodName) throws RemotingException{
		return methodCached.get(methodName);
    }
}
