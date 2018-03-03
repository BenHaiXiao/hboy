package com.github.hboy.center;

import com.github.hboy.center.monitor.MonitorFactory;
import com.github.hboy.center.subscribe.InvokerFactory;
import com.github.hboy.common.config.ExportConfig;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 15:22
 */
public class ServiceExporter<T> extends AbstractReference<T> {

	
    private volatile boolean exported = false;
	
	private InvokerConfig url;
    
	private boolean isLocal = true;
	
	private T service;
	
	private ExportConfig exportConfig;
	
	private String monitorAddress;
	
	public synchronized void export() {
		if (exported) {
            return;
        }
        exported = true;
		init();
		if(log.isInfoEnabled()){
			log.info("ServiceExporter start , url:" + url);
		}
		//初始化监控中心
        if(url.getMonitorAddress() != null && !"".equals(url.getMonitorAddress())){
            MonitorFactory.getMonitor(url);
        }
		InvokerFactory invokerFactory = InvokerFactory.getInvokerFacoty();
		if(isLocal){
			invokerFactory.localExport(service, getInterface(),url,channelEventHandlers);
		}else{
			invokerFactory.export(service, getInterface(),url,channelEventHandlers);
		}
		if(log.isInfoEnabled()){
			log.info("ServiceExporter end , url:" + url);
		}
		
	}
	
	
    private void init(){
		checkConfig();
		checkInterfaces();
		toURL();
	}
	
	private InvokerConfig toURL(){
		url = new InvokerConfig();
		url.setHost(exportConfig.getHost());
		url.setPort(exportConfig.getPort());
		url.setTimeout(exportConfig.getTimeout());
		url.setWeight(exportConfig.getWeight());
		url.setPoolSize(exportConfig.getMaxActive());

		url.setGroup(group);
		url.setInterfaceName(interfaceName);
		url.setFault(fault);
		url.setLoadBalance(loadBalance);
		url.setThriftProtocol(thriftProtocol);
		url.setThriftTransport(transport);
		url.setInterval(interval);
		url.setRetries(retries);
		url.setServiceProtocol(protocol);
		url.setFilters(filters);
		url.setMonitorAddress(monitorAddress);
		if(!isLocal){
			url.setApplication(centerConfig.getApplication());
			url.setSubscribeAddress(centerConfig.getAddress());
			url.setSubscribeConnectionTimeOut(centerConfig.getConnectionTimeOut());
			url.setSubscribeSessionTimeOut(centerConfig.getSessionTimeOut());
			url.setMonitorAddress(centerConfig.getMonitorAddress());
		}
		url.setClient(false);
		return url;
	}
	
	
	private void checkConfig(){
		if(service == null){
			log.error(" service is null !");
			throw new IllegalStateException(" service is null !");
		}
		if(exportConfig == null){
			log.error(" exportConfig is null !");
			throw new IllegalStateException(" exportConfig is null !");
		}
		if(centerConfig != null){
			isLocal = false;
			checkCenterConfig();
		}
	} 
		
	
	public T getService() {
		return service;
	}

	public void setService(T service) {
		this.service = service;
	}

	public ExportConfig getExportConfig() {
		return exportConfig;
	}

	public void setExportConfig(ExportConfig exportConfig) {
		this.exportConfig = exportConfig;
	}

    public String getMonitorAddress() {
        return monitorAddress;
    }

    public void setMonitorAddress(String monitorAddress) {
        this.monitorAddress = monitorAddress;
    }
}
