package com.github.hboy.center;

import java.util.List;

import com.github.hboy.center.filter.Filter;
import com.github.hboy.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.common.config.CenterConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 23:22
 */
public abstract class AbstractReference<T> {
		
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	protected Constants.ThriftProtocolType thriftProtocol = Constants.ThriftProtocolType.BINARY;
	
	protected Constants.TransportType transport = Constants.TransportType.TFRAMED;
	
	protected Class<T> interfaceClass;
	
	protected String interfaceName;
	
	protected String group = Constants.DEFAULT_GROUP;
	
	protected int retries = 3;
	
	protected long interval = 30 * 1000;    
	
	protected Constants.LoadBalanceType loadBalance = Constants.LoadBalanceType.RANDOM;
	
	protected Constants.FaultType fault =  Constants.FaultType.FAILFASE;
	
	@Deprecated
	protected boolean isAnnotation = false;
	
	protected CenterConfig centerConfig;
	
	protected Constants.ServiceProtocolType protocol = Constants.ServiceProtocolType.thrift;
	
	protected List<ChannelEventHandler> channelEventHandlers;
	
	protected List<Filter> filters;
	
	
    public List<ChannelEventHandler> getChannelEventHandlers() {
        return channelEventHandlers;
    }

    public void setChannelEventHandlers(List<ChannelEventHandler> channelEventHandlers) {
        this.channelEventHandlers = channelEventHandlers;
    }

    public CenterConfig getCenterConfig() {
		return centerConfig;
	}

	public void setCenterConfig(CenterConfig centerConfig) {
		this.centerConfig = centerConfig;
	}

	public Constants.LoadBalanceType getLoadBalance() {
		return loadBalance;
	}

	public void setLoadBalance(Constants.LoadBalanceType loadBalance) {
		this.loadBalance = loadBalance;
	}

	public Constants.FaultType getFault() {
		return fault;
	}

	public void setFault(Constants.FaultType fault) {
		this.fault = fault;
	}
	
	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}
	
	public Constants.ThriftProtocolType getThriftProtocol() {
        return thriftProtocol;
    }

    public void setThriftProtocol(Constants.ThriftProtocolType thriftProtocol) {
        this.thriftProtocol = thriftProtocol;
    }
    
    public Constants.TransportType getTransport() {
        return transport;
    }

    public void setTransport(Constants.TransportType transport) {
        this.transport = transport;
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
	}

	public void setInterface(Class<T> interfaceClass) {
		if (interfaceClass != null && !interfaceClass.isInterface()) {
			throw new IllegalStateException("The interface class "
					+ interfaceClass + " is not a interface");
		}
		this.interfaceClass = interfaceClass;
		this.interfaceName = interfaceClass == null ? null : interfaceClass
				.getName();
	}
	
	public Constants.ServiceProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(Constants.ServiceProtocolType protocol) {
        this.protocol = protocol;
    }

    @SuppressWarnings("unchecked")
    public  Class<T> getInterface() {
		if (interfaceClass != null) {
	        return interfaceClass;
	    }
	    try {
	        if (interfaceName != null && interfaceName.length() > 0) {
	            this.interfaceClass = (Class<T>) Class.forName(interfaceName);
	        }
	    } catch (ClassNotFoundException t) {
	        throw new IllegalStateException(t.getMessage(), t);
	    }
	    return interfaceClass;
	}
	
    @Deprecated
	public boolean isAnnotation() {
		return isAnnotation;
	}
	
	@Deprecated
	public void setAnnotation(boolean isAnnotation) {
		this.isAnnotation = isAnnotation;
	}

	public String getInterfaceName() {
		return interfaceName;
	}
	
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	protected void checkInterfaces(){
		if(getInterface() == null){
			log.error("interfaces == null");
			throw new IllegalStateException("interfaces == null");
		}
	}
	
	
	public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    protected void checkCenterConfig(){
		
		if(centerConfig.getAddress() == null || "".equals(centerConfig.getAddress())){
			log.error("center config  host is null !");
			throw new IllegalStateException("center config  host is null !");
		} 
		if(centerConfig.getApplication() == null || "".equals(centerConfig.getApplication())){
			log.error("center config  application is null !");
			throw new IllegalStateException("center config  application is null !");
		}
	} 
}
