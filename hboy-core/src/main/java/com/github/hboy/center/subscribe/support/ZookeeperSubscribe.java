package com.github.hboy.center.subscribe.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.github.hboy.center.subscribe.directory.Directory;
import com.github.hboy.common.config.Configuration;
import com.github.hboy.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.hboy.center.subscribe.SubscribeException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 15:19
 */
public class ZookeeperSubscribe extends AbstractSubscribe {
	
	protected static final Logger logger = LoggerFactory.getLogger(ZookeeperSubscribe.class);
	
	private final ZookeeperClient zkClient;
	 // 定时任务执行器
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1);

    // 失败重试定时器，定时检查是否有请求失败，如有，无限次重试
    private final ScheduledFuture<?> retryFuture;
    
	private final ConcurrentMap<InvokerConfig, Directory<?>> failedSubscribed = new ConcurrentHashMap<InvokerConfig, Directory<?>>();
	
	private final ConcurrentMap<InvokerConfig, Directory<?>> subscribed = new ConcurrentHashMap<InvokerConfig, Directory<?>>();
	
	private final Set<InvokerConfig> registered = new java.util.concurrent.CopyOnWriteArraySet<InvokerConfig>();
	
	private final Set<InvokerConfig> failedRegistered = new java.util.concurrent.CopyOnWriteArraySet<InvokerConfig>();

	 // zk的path----ChildListener
	private final ConcurrentMap<String, ChildListener> zkChildListeners = new ConcurrentHashMap<String, ChildListener>();
	 
    //  zk的path----DataListener
    private final ConcurrentMap<String, DataListener> zkDataListeners = new ConcurrentHashMap<String, DataListener>();

    //保存clientConfig---<path----urls>
    private final ConcurrentMap<InvokerConfig, Map<String, List<String>>> notified = new ConcurrentHashMap<InvokerConfig, Map<String, List<String>>>();
    
	public ZookeeperSubscribe(InvokerConfig url){
		super();
		zkClient = new ZookeeperClient(url);
		//如果zk重新连接，则重新注册、订阅。
		zkClient.addStateListener(new StateListener() {
			public void stateChanged(int state) {
				if (state == RECONNECTED) {
					try {
						recover();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		});
		//定时任务，重试机制。
		this.retryFuture = retryExecutor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				// 检测并连接
				try {
					retry();
				} catch (Throwable t) { // 防御性容错
					logger.error(
							"Unexpected error occur at failed retry, cause: "
									+ t.getMessage(), t);
				}
			}
		}, 5000, 10000, TimeUnit.MILLISECONDS);
	}
	
	 // 重试失败的动作
    protected void retry() {
    	if (! failedRegistered.isEmpty()) {
     		Set<InvokerConfig> failed = new HashSet<InvokerConfig>(failedRegistered);
 			logger.info("Retry registered: {}", failed);
			for (InvokerConfig url : failed) {
				try {
					register(url);
				} catch (Throwable t) { // 忽略所有异常，等待下次重试
					logger.warn("Failed to retry register " + failed
							+ ", waiting for again, cause: " + t.getMessage(),
							t);
				}
			}
    	}
    	
        if (! failedSubscribed.isEmpty()) {
            Map<InvokerConfig, Directory<?>> failed = new HashMap<InvokerConfig, Directory<?>>(failedSubscribed);
            if (failed.size() <= 0) {
            	return;
            }
			logger.info("Retry subscribe: {}", failed);
			for (Map.Entry<InvokerConfig, Directory<?>> entry : failed.entrySet()) {
				try {
					InvokerConfig url = entry.getKey();
					Directory<?> listener = entry.getValue();
					subscribe(url, listener);
				} catch (Throwable t) { // 忽略所有异常，等待下次重试
					logger.warn("Failed to retry subscribe " + failed
							+ ", waiting for again, cause: " + t.getMessage(),
							t);
				}
			}
        }
    }
	
	
	/**
	 * 取值，监听
	 */
	@Override
	public List<InvokerConfig> subscribe(final InvokerConfig url, final Directory<?> directory) {
		if (url == null) {
            throw new IllegalArgumentException("subscribe url == null");
        }
        if (directory == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
       
		List<String> urls = new ArrayList<String>();
		try{
			logger.info("Subscribe: {}", url);
			failedSubscribed.remove(url);
			this.connect();
			if(!zkClient.exists(toAppPath(url))){
				throw new SubscribeException("Application not exists, application:" + url.getApplication() + ",url:" + url);
			}
			
			String providerPath = toServiceProviderPath(url);
			ChildListener zkChildListener = zkChildListeners.get(providerPath);
			if (zkChildListener == null) {
			    zkChildListeners.putIfAbsent(providerPath, new ChildListener() {
			      //需要增加和删除data上的监听
					public void childChanged(String parentPath, List<String> currentChilds) {
						ZookeeperSubscribe.this.notify(url, directory, zkClient.queryProviderData(parentPath,currentChilds));
						subscribeDataChange(url, directory, currentChilds);
					}
				});
				zkChildListener = zkChildListeners.get(providerPath);
			}
			subscribed.putIfAbsent(url, directory);
			List<String> providersNodes = zkClient.addChildListener(providerPath, zkChildListener);
			 
//			List<String> providerNodePaths = subscribeDataChange(url,directory,providersNodes);
			
			List<String> providerNodeDatas = zkClient.queryProviderData(providerPath,providersNodes);
			if (providerNodeDatas != null) {
                urls.addAll(providerNodeDatas);
            }
			/**
             * TODO Configuration
             */
			String configurationPath = toServiceConfigurationPath(url);
			ChildListener zkConfigurationListener = zkChildListeners.get(url);
            if (zkConfigurationListener == null) {
                zkChildListeners.putIfAbsent(configurationPath, new ChildListener() {
                    public void childChanged(String parentPath, List<String> currentChilds) {
                        ZookeeperSubscribe.this.notify(url, directory,currentChilds);
                    }
                });
                zkChildListener = zkChildListeners.get(configurationPath);
            }
            List<String> configurations = zkClient.addChildListener(configurationPath, zkChildListener);
			if (configurations != null) {
				urls.addAll(configurations);
			}
		}catch(Exception e){
			logger.warn("Failed subscribe url：" + url + ", waiting for again, cause: " + e.getMessage(), e);
			addFailedSubscribed(url, directory);
			urls = getCacheUrls(url);
		}
		return this.notify(url, directory, urls);
	}
	
	
	
	private List<String> subscribeDataChange(final InvokerConfig invokerConfig,final Directory<?> directory,List<String> providersNodes){
	    String providerPath = toServiceProviderPath(invokerConfig);
	    List<String> providerNodePaths = new ArrayList<String>();
	    for(String provider : providersNodes){
            String providerNodePath = providerPath + Constants.PATH_SPLIT + provider;
            DataListener zkDataListener = zkDataListeners.get(invokerConfig);
            if (zkDataListener == null) {
                zkDataListeners.putIfAbsent(providerNodePath, new DataListener() {
                    @Override
                    public void dataChange(String dataPath, String data) throws Exception {
                        String providerPath = toServiceProviderPath(invokerConfig);
                        List<String> providerNodes = zkClient.getChildren(providerPath);
                        ZookeeperSubscribe.this.notify(invokerConfig, directory, zkClient.queryProviderData(providerPath,providerNodes));
                    }
                    @Override
                    public void dataDeleted(String dataPath) throws Exception {
                        zkClient.removeDataListener(dataPath, zkDataListeners.remove(dataPath));
                    }
                });
                zkDataListener = zkDataListeners.get(providerNodePath);
            }
            providerNodePaths.add(providerNodePath);
            zkClient.addDataListener(providerNodePath, zkDataListener);
        }
	    return providerNodePaths;
	}
	
	@Deprecated
	@SuppressWarnings("unused")
	private List<InvokerConfig> toUrls(List<String> uris) {
	    List<InvokerConfig> urls =  new ArrayList<InvokerConfig>();
		if(uris == null || uris.size() == 0){
			return urls;
		}
		for(String u : uris){
			InvokerConfig url;
			try {
				url = mapper.readValue(u, InvokerConfig.class);//Json.strToObj(u, URL.class);
			} catch (IOException e) {
				logger.error("string to  url error, url:" + u);
				continue;
			}
			urls.add(url);
		}
		return urls;
	}
	
	
	private List<InvokerConfig> notify(InvokerConfig url, Directory<?> listener, List<String> configs) {
		if (url == null) {
			throw new IllegalArgumentException("notify clientUrl == null");
		}
		if (listener == null) {
			throw new IllegalArgumentException("notify listener == null");
		}
		if ((configs == null || configs.size() == 0)) {
			logger.warn("notify urls is null , url" + url);
			return null;
		}
		if (logger.isInfoEnabled()) {
			logger.info("Notify urls for subscribe clientUrl: " + url + ", urls: "
					+ configs);
		}
		//<path-----provider and configurators>   新的provider和configurators
		Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (String u : configs) {
            String category = Constants.PATH_PROVIDER;
            if(u.contains(Constants.PATH_CONFIGURATION)){
                category = Constants.PATH_CONFIGURATION;
            }
            List<String> categoryList = result.get(category);
            if (categoryList == null) {
                categoryList = new ArrayList<String>();
                result.put(category, categoryList);
            }
            categoryList.add(u);
        }
        if (result.size() == 0) {
            return null;
        }
        Map<String, List<String>> categoryNotified = notified.get(url);
        if (categoryNotified == null) {
            notified.putIfAbsent(url, new ConcurrentHashMap<String, List<String>>());
            categoryNotified = notified.get(url);
        }
       
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            String category = entry.getKey();
            List<String> categoryList = entry.getValue();
            categoryNotified.put(category, categoryList);
        }
        //合并provider 和configcategory
        List<String> providers = categoryNotified.get(Constants.PATH_PROVIDER);
        List<String> configcategory = categoryNotified.get(Constants.PATH_CONFIGURATION);
        
        List<InvokerConfig> invokerConfigList = mergeConfig(providers,configcategory);
        saveProperties(url.getInterfaceName(),invokerConfigList);
		listener.notify(invokerConfigList);
		return invokerConfigList;
	}
	
	private List<InvokerConfig> mergeConfig(List<String> providers,List<String> configcategory){
	    Configuration configuration = null;
	    if(configcategory != null){
	        for(String c : configcategory){
	            try {
	                configuration = mapper.readValue(c, Configuration.class);
	            } catch (IOException e) {
	                logger.error("string to  url error, configuration:" + c);
	                continue;
	            }
	            
	        }
	    }
	    List<InvokerConfig> invokers = new ArrayList<InvokerConfig>();
        if(providers == null || providers.size() == 0){
            return invokers;
        }
	    for(String u : providers){
	        InvokerConfig url;
            try {
                url = mapper.readValue(u, InvokerConfig.class);//Json.strToObj(u, URL.class);
            } catch (IOException e) {
                logger.error("string to  url error, url:" + u);
                continue;
            }
            if(configuration != null){
                url.setAccessable(configuration.getAccessable());
                url.setLoadBalance(configuration.getLoadBalance());
                url.setFault(configuration.getFault());
                url.setInterval(configuration.getInterval());
                url.setRetries(configuration.getRetries());
                url.setServiceProtocol(configuration.getServiceProtocol());
            }
            invokers.add(url);
        }
	    return invokers;
	}
	
	
	//    /application/service/provider/....
	private String toServiceProviderPath(InvokerConfig url) {
		return  toAppPath(url) + Constants.PATH_SPLIT
				+ url.getInterfaceName() + Constants.PATH_SPLIT
				+ Constants.PATH_PROVIDER;
	}
	// /application/service/configuration/.....
	private String toServiceConfigurationPath(InvokerConfig url) {
        return  toAppPath(url) + Constants.PATH_SPLIT
                + url.getInterfaceName() + Constants.PATH_SPLIT
                + Constants.PATH_CONFIGURATION;
    }
	
	private String toAppPath(InvokerConfig url) {
		return  Constants.PATH_SPLIT+ url.getApplication();
	}
	
	private String toProviderData(InvokerConfig url) {
		try {
			return  mapper.writeValueAsString(url.toServerInfo());
		} catch (JsonProcessingException e) {
			throw new SubscribeException("server info to path is Error!, url:" + url);
		}
	}
	
	private String toProviderNode(InvokerConfig url) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("host", url.getHost());
            map.put("port", url.getPort());
            return  toServiceProviderPath(url) + Constants.PATH_SPLIT + mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new SubscribeException("server info to path is Error!, url:" + url);
        }
    }
	
	/**
	 * TODO 删除监听
	 */
	@Override
	public void unsubscribe(InvokerConfig url,Directory<?> listener) {
		if (url == null) {
            throw new IllegalArgumentException("unsubscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
		String path = toServiceProviderPath(url);
		ChildListener zkListener = zkChildListeners.get(url);
		if (zkListener != null) {
		    zkChildListeners.remove(url);
			subscribed.remove(url);
			zkClient.removeChildListener(path, zkListener);
		}
	}
	
    public Map<InvokerConfig, Directory<?>> getSubscribed() {
	     return subscribed;
	}
    
    public Set<InvokerConfig> getRegistered() {
	     return registered;
	}
    
    /**
     * 放入失败集合,重新注册、订阅所有的服务，
     */
	public void recover(){
		// register
        Set<InvokerConfig> recoverRegistered = new HashSet<InvokerConfig>(getRegistered());
        if (! recoverRegistered.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover register url " + recoverRegistered);
            }
            for (InvokerConfig url : recoverRegistered) {
            	addFailedRegistered(url);
            }
        }
		//subscribed
        Map<InvokerConfig, Directory<?>> recoverSubscribed = new HashMap<InvokerConfig, Directory<?>>(getSubscribed());
        if (! recoverSubscribed.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
			for (Map.Entry<InvokerConfig, Directory<?>> entry : recoverSubscribed
					.entrySet()) {
				InvokerConfig url = entry.getKey();
				addFailedSubscribed(url, entry.getValue());
			}
        }
	}
	
	private void addFailedSubscribed(InvokerConfig url, Directory<?> listener) {
		failedSubscribed.putIfAbsent(url, listener);
	}
	
	private void addFailedRegistered(InvokerConfig url) {
		failedRegistered.add(url);
	}
	//TODO
	public void destroy() {
		
		Set<InvokerConfig> destroyRegistered = new HashSet<InvokerConfig>(getRegistered());
		if (!destroyRegistered.isEmpty()) {
			for (InvokerConfig url : destroyRegistered) {
				try {
					if (logger.isInfoEnabled()) {
						logger.info("Destroy unregister url " + url);
					}
					unregister(url);
				} catch (Throwable t) {
					logger.warn("Destroy Failed to unregister url " + url
							+ ", cause: " + t.getMessage(), t);
				}
			}
		}
		
		Map<InvokerConfig, Directory<?>> destroySubscribed = new HashMap<InvokerConfig, Directory<?>>(
				getSubscribed());
		if (!destroySubscribed.isEmpty()) {
			for (Map.Entry<InvokerConfig, Directory<?>> entry : destroySubscribed
					.entrySet()) {
				InvokerConfig url = entry.getKey();
				try {
					unsubscribe(url, entry.getValue());
					if (logger.isInfoEnabled()) {
						logger.info("Destroy unsubscribe url " + url);
					}
				} catch (Throwable t) {
					logger.warn("Destroy Failed to unsubscribe url " + url
							+ ", cause: " + t.getMessage(), t);
				}
			}
		}
		try {
			retryFuture.cancel(true);
			zkClient.close();
		} catch (Throwable t) {
			logger.warn(t.getMessage(), t);
		}
	}

	@Override
	public boolean isAvailable() {
		return zkClient.isClose();
	}

	private void connect(){
		if(!zkClient.isConnected()){
			zkClient.connect();
		}
	} 
	
	
	@Override
	public void register(InvokerConfig url) {
		try{
			if (url == null) {
	            throw new IllegalArgumentException("register url == null");
	        }
			if (logger.isInfoEnabled()) {
				logger.info("Register: " + url);
			}
			failedRegistered.remove(url);
			this.connect();
			if(!zkClient.exists(toAppPath(url))){
				throw new SubscribeException("Application not exists, application:" + url.getApplication() + ",url:" + url);
			}
			 
			String servicePath = toServiceProviderPath(url);
			if(!zkClient.exists(servicePath)){
				zkClient.createPersistent(servicePath);
			}
			 
	        String providerNode = toProviderNode(url);
	        byte[] providerData = toProviderData(url).getBytes("UTF-8");
	        zkClient.createEphemeral(providerNode,providerData);
	        registered.add(url);
		}catch(Exception e){
			logger.warn("Failed register url：" + url + ", waiting for again, cause: " + e.getMessage(), e);
			addFailedRegistered(url);
		}
	}

	@Override
	public void unregister(InvokerConfig url) {
		if (url == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (logger.isInfoEnabled()){
            logger.info("Unregister: " + url);
        }
        this.connect();
		registered.remove(url);
		zkClient.delete(toProviderNode(url));
	}
	
}
