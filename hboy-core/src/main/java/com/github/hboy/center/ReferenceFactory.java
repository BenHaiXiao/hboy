package com.github.hboy.center;

import java.util.ArrayList;
import java.util.List;

import com.github.hboy.center.monitor.MonitorFactory;
import com.github.hboy.center.proxy.JDKProxy;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.subscribe.InvokerFactory;
import com.github.hboy.common.config.InvokerConfig;
import com.github.hboy.common.config.LocalConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 15:8
 */
public class ReferenceFactory<T> extends AbstractReference<T> {
	
	protected InvokerConfig clientUrl;
	
	protected T t;
	
	protected boolean isLocal = false;
	
	protected List<InvokerConfig> locals;
	
	protected List<LocalConfig> localConfigs;
	
	private Object action;  //

	public synchronized T getClient() {
		if (t == null) {
			init();
			if(log.isInfoEnabled()){
				log.info("ReferenceFactory start  init client Url:" + clientUrl);
			}
			
			//初始化监控中心
			if(clientUrl.getMonitorAddress() != null && !"".equals(clientUrl.getMonitorAddress())){
			    MonitorFactory.getMonitor(clientUrl);
			}
			
			Invoker<T> invoker;
			if(isLocal){
				invoker = InvokerFactory.getInvokerFacoty().buildInvoker(clientUrl,locals,channelEventHandlers);
			}else{
				invoker = InvokerFactory.getInvokerFacoty().buildInvoker(clientUrl,channelEventHandlers);
			}
			t = JDKProxy.getInvokerProxy(invoker, interfaceClass);
			if(log.isInfoEnabled()){
				log.info("ReferenceFactory init end , is init " + (isLocal ? localConfigs : centerConfig));	
			}
		}
		return t;
	}
	
	protected void init(){
		checkConfig();
		checkInterfaces();
		clientUrl = toClientUrl();
		if(isLocal){
			locals = localToUrls();
		}
	}
	
    public List<LocalConfig> getLocalConfigs() {
		return localConfigs;
	}

	public void setLocalConfigs(List<LocalConfig> localConfigs) {
		this.localConfigs = localConfigs;
	}
	
	private InvokerConfig toClientUrl(){
		InvokerConfig url = createURL();
		if(centerConfig != null){
//			url.setPoolSize(centerConfig.getMaxActive());
			url.setSubscribeAddress(centerConfig.getAddress());
//			url.setTimeout(centerConfig.getTimeout());
//			url.setWeight(centerConfig.getWeight());
			url.setSubscribeConnectionTimeOut(centerConfig.getConnectionTimeOut());
			url.setSubscribeSessionTimeOut(centerConfig.getSessionTimeOut());
			url.setApplication(centerConfig.getApplication());
			url.setMonitorAddress(centerConfig.getMonitorAddress());
		}
		return url;
	}
	
	@SuppressWarnings("deprecation")
    private InvokerConfig createURL(){
		InvokerConfig url = new InvokerConfig();
		url.setFault(fault);
		url.setInterfaceName(interfaceName);
		url.setLoadBalance(loadBalance);
		url.setThriftProtocol(thriftProtocol);
		url.setThriftTransport(transport);
		url.setRetries(retries);
		url.setGroup(group);
		url.setServiceProtocol(protocol);
		url.setAnnotation(isAnnotation());
		url.setAction(action);
		url.setFilters(filters);
		return url;
	}
	
	private List<InvokerConfig> localToUrls(){
		List<InvokerConfig> urls = new ArrayList<InvokerConfig>();
		for(LocalConfig l : localConfigs){
			InvokerConfig url = createURL();
			url.setTimeout(l.getTimeout());
			url.setWeight(l.getWeight());
			url.setPoolSize(l.getMaxActive());
			url.setHost(l.getHost());
			url.setPort(l.getPort());
//			url.setApplication(l.getApplication());
			urls.add(url);
			url.setMonitorAddress(l.getMonitorAddress());
			clientUrl.setMonitorAddress(l.getMonitorAddress());
		}
		return urls;
	}
	
	/**
	 * 校验必填项配置
	 */
	private void checkConfig(){
		if(centerConfig == null && (localConfigs == null || localConfigs.size() == 0)){
			log.error("config == null !");
			throw new IllegalStateException("config == null !");
		}
		if(centerConfig == null){
			isLocal = true;
			for(LocalConfig l : localConfigs){
				if(l.getHost() == null || "".equals(l.getHost()) || l.getPort() <=0 ){
					log.error("loacl config  host is error !");
					throw new IllegalStateException("loacl config  host is error !");
				}
			}
		}else{
			checkCenterConfig();
		}
	}
	
	public void destroy(){
		InvokerFactory.getInvokerFacoty().destroy(clientUrl);
    }

    public Object getAction() {
        return action;
    }

    public void setAction(Object action) {
        this.action = action;
    }

}
