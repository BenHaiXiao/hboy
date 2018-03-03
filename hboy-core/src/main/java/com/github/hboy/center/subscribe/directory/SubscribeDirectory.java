package com.github.hboy.center.subscribe.directory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.protocol.ProtocolWrapper;
import com.github.hboy.center.subscribe.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 *
 * 每一个服务一个SubscribeDirectory
 * @author xiaobenhai
 * Date: 2016/3/25
 * Time: 16:21
 */
public class SubscribeDirectory<T> implements Directory<T> {

	
    private static final Logger logger = LoggerFactory.getLogger(SubscribeDirectory.class);
    
    private InvokerConfig clientUrl; // 构造时初始化 
    
    private volatile Map<String, Invoker<T>> urlInvokerMap;
    
    private volatile boolean isDestroyed = false;
    
    private final Subscribe subscribe;
    
    private final List<ChannelEventHandler> handlers;
    
    public SubscribeDirectory(InvokerConfig url, Subscribe subscribe,List<ChannelEventHandler> handlers) {
        if(url == null){
            throw new IllegalArgumentException("serviceName  is null.");
        }
        this.subscribe = subscribe;
        clientUrl = url;
        this.handlers = handlers;
    }

    public SubscribeDirectory(InvokerConfig url,List<ChannelEventHandler> handlers) {
        this(url,null,handlers);
    }
    
    public SubscribeDirectory(InvokerConfig url) {
        this(url,null,null);
    }
    
    public SubscribeDirectory(InvokerConfig url, Subscribe subscribe){
        this(url,subscribe,null);
    }
    
    
    public void destroy() {
        if(isDestroyed()) {
            return;
        }
        try {
            if(clientUrl != null && subscribe != null) {
            	subscribe.unsubscribe(clientUrl, this);
            }
        } catch (Throwable t) {
            logger.warn("unexpeced error when unsubscribe service " + clientUrl.getInterfaceName());
        }
        try {
            destroyAllInvokers();
        } catch (Throwable t) {
            logger.warn("Failed to destroy service " + clientUrl.getInterfaceName(), t);
        }
    }

	public synchronized void notify(List<InvokerConfig> invokerUrls) {
		if (invokerUrls == null || invokerUrls.size() == 0) {
			return;
		}
		Map<String, Invoker<T>> oldUrlInvokerMap = this.urlInvokerMap; // local
		
		Map<String, Invoker<T>> newUrlInvokerMap = toInvokers(invokerUrls);// 将URL列表转成Invoker列表
		// 如果计算错误，则不进行处理.
		if (newUrlInvokerMap == null || newUrlInvokerMap.size() == 0) {
			logger.error("urls to invokers error .invokerUrls.size :"
					+ invokerUrls.size() + ", invoker.size :0. urls :"
					+ invokerUrls.toString());
			return;
		}
		this.urlInvokerMap = newUrlInvokerMap;
		try {
			destroyUnusedInvokers(oldUrlInvokerMap, newUrlInvokerMap); // 关闭未使用的Invoker
		} catch (Exception e) {
			logger.warn("destroyUnusedInvokers error. ", e);
		}
	}
    
    /**
     * 将urls转成invokers,如果url已经被初始化了，不再重新初始化。
     */
    @SuppressWarnings("unchecked")
	private Map<String, Invoker<T>> toInvokers(List<InvokerConfig> urls) {
        Map<String, Invoker<T>> newUrlInvokerMap = new HashMap<String, Invoker<T>>();
        if(urls == null || urls.size() == 0){
            return newUrlInvokerMap;
        }
        Set<String> keys = new HashSet<String>();
        for (InvokerConfig providerUrl : urls) {
            InvokerConfig url = mergeUrl(providerUrl);
            String key = url.toString();  
            if (keys.contains(key)) { // 重复URL
                continue;
            }
            keys.add(key);
            // 缓存key合并了消费端参数的URL， 如果合并之后发生了变化，则重新refer
            Map<String, Invoker<T>> localUrlInvokerMap = this.urlInvokerMap; // local reference
            Invoker<T> invoker = localUrlInvokerMap == null ? null : localUrlInvokerMap.get(key);
            // 缓存中没有，重新refer
            if (invoker == null) { 
                try {
                    invoker = (Invoker<T>) ProtocolWrapper.getProtocol().refer(url.getInterface(), url,handlers);
                } catch (Throwable t) {
                    logger.error("Failed create invoker for interface:"+clientUrl.getInterfaceName()+",url:("+url+")" + t.getMessage(), t);
                }
                if (invoker != null) { // 将新的引用放入缓存
                    newUrlInvokerMap.put(key, invoker);
                }
            }else {
            	//已经存在直接放入缓存
                newUrlInvokerMap.put(key, invoker);
            }
        }
        keys.clear();
        return newUrlInvokerMap;
    }
    
    /**
     * 和客户端参数合并，如果服务端没有以客户端为准
     * @param providerUrl
     * @return
     */
    @SuppressWarnings("deprecation")
    private InvokerConfig mergeUrl(InvokerConfig providerUrl){
        
    	if(providerUrl.getFault() == null){
    		providerUrl.setFault(clientUrl.getFault());
    	}
    	if(providerUrl.getLoadBalance() == null){
    		providerUrl.setLoadBalance(clientUrl.getLoadBalance());
    	}
    	if(providerUrl.getPoolSize() <= 0 ){
    		providerUrl.setPoolSize(clientUrl.getPoolSize());
    	}
    	if(providerUrl.getThriftProtocol() == null){
    		providerUrl.setThriftProtocol(clientUrl.getThriftProtocol());
    	}
    	if(providerUrl.getThriftTransport() == null){
    		providerUrl.setThriftTransport(clientUrl.getThriftTransport());
    	}
    	if(providerUrl.getWeight()  <0 ){
    		providerUrl.setWeight(clientUrl.getWeight());
    	}
    	if(providerUrl.getRetries()  <0 ){
    		providerUrl.setRetries(clientUrl.getRetries());
    	}
    	if(providerUrl.getGroup() == null || "".equals(providerUrl.getGroup())){
    		providerUrl.setGroup(clientUrl.getGroup());
    	}
    	if(providerUrl.getInterfaceName()  == null || "".equals(providerUrl.getInterfaceName())){
    		providerUrl.setInterfaceName(clientUrl.getInterfaceName());
    	}
    	providerUrl.setAnnotation(clientUrl.isAnnotation());
    	providerUrl.setFilters(clientUrl.getFilters());
    	providerUrl.setAction(clientUrl.getAction());
    	providerUrl.setMonitorAddress(clientUrl.getMonitorAddress());
    	providerUrl.setApplication(clientUrl.getApplication());
    	providerUrl.setServiceProtocol(clientUrl.getServiceProtocol());
        return providerUrl;
    }

    /**
     * 关闭所有Invoker
     */
    private void destroyAllInvokers() {
        Map<String, Invoker<T>> localUrlInvokerMap = this.urlInvokerMap; // local 
        if(localUrlInvokerMap != null) {
            for (Invoker<T> invoker : new ArrayList<Invoker<T>>(localUrlInvokerMap.values())) {
                try {
                    invoker.destroy();
                    if(logger.isDebugEnabled()){
                        logger.debug("destory invoker["+invoker.getUrl()+"] success. ");
                    }
                } catch (Throwable t) {
                    logger.warn("Failed to destroy service " + clientUrl.getInterfaceName() + " to provider " + invoker.getUrl(), t);
                }
            }
            localUrlInvokerMap.clear();
        }
    }
    
    /**
     * destroy掉没有使用的invoke
     * @param invokers
     */
    private void destroyUnusedInvokers(Map<String, Invoker<T>> oldUrlInvokerMap, Map<String, Invoker<T>> newUrlInvokerMap) {
        if (newUrlInvokerMap == null || newUrlInvokerMap.size() == 0) {
            destroyAllInvokers();
            return;
        }
        List<String> deleted = null;
        if (oldUrlInvokerMap != null) {
            Collection<Invoker<T>> newInvokers = newUrlInvokerMap.values();
            for (Map.Entry<String, Invoker<T>> entry : oldUrlInvokerMap.entrySet()){
                if (! newInvokers.contains(entry.getValue())) {
                    if (deleted == null) {
                        deleted = new ArrayList<String>();
                    }
                    deleted.add(entry.getKey());
                }
            }
        }
        
        if (deleted != null) {
            for (String url : deleted){
                if (url != null ) {
                    Invoker<T> invoker = oldUrlInvokerMap.remove(url);
                    if (invoker != null) {
                        try {
                            invoker.destroy();
                            if(logger.isDebugEnabled()){
                                logger.debug("destory invoker["+invoker.getUrl()+"] success. ");
                            }
                        } catch (Exception e) {
                            logger.warn("destory invoker["+invoker.getUrl()+"] faild. " + e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    public String getInterface() {
        return clientUrl.getInterfaceName();
    }

    public InvokerConfig getUrl() {
    	return this.clientUrl;
    }
    
    public boolean isDestroyed(){
    	return isDestroyed;
    }
    
    public boolean isAvailable() {
        if (isDestroyed()) {
            return false;
        }
        Map<String, Invoker<T>> localUrlInvokerMap = urlInvokerMap;
        if (localUrlInvokerMap != null && localUrlInvokerMap.size() > 0) {
            for (Invoker<T> invoker : new ArrayList<Invoker<T>>(localUrlInvokerMap.values())) {
                if (invoker.isAvailable()) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 
     */
    public Map<String, Invoker<T>> getUrlInvokerMap(){
        return urlInvokerMap;
    }
    
	@Override
	public List<Invoker<T>> list(Invocation invocation) throws RemotingException {
		List<Invoker<T>> invokers = new ArrayList<Invoker<T>>();
		
		Map<String, Invoker<T>> localInvokerMap = this.urlInvokerMap; // local reference
		if (urlInvokerMap != null && urlInvokerMap.size() > 0) {
			for (Entry<String, Invoker<T>> e : localInvokerMap.entrySet()) {
				invokers.add(e.getValue());
			}
		}
		return invokers;
	}

 
}
