package com.github.hboy.common.config;

import java.util.List;

import com.github.hboy.center.filter.Filter;
import com.github.hboy.common.util.Constants;

/**
 * @author xiaobenhai
 * Date: 2016/3/26
 * Time: 14:28
 */
@SuppressWarnings("rawtypes")
public class InvokerConfig extends Configuration{
	
	protected  String subscribeAddress;		//订阅地址
    
	protected Constants.ThriftProtocolType thriftProtocol;			//协议
	
	protected Constants.TransportType thriftTransport;		//传输方式
    
	protected Class interfaceClass;		//接口
	
	protected int subscribeConnectionTimeOut = 10000;	//zk连接超时时间

	protected int subscribeSessionTimeOut = 30000;	//zkSession超时时间
	
	protected String application;			//应用名称
	
	@Deprecated
	protected boolean isAnnotation = false;			//是否采用注解方式，标识服务
	
	protected Object action;
	
	protected List<Filter> filters;
	
	protected String monitorAddress;
	
	protected boolean isClient = true;
	
	public InvokerConfig() {
		this(null,0,null);
	}

	public InvokerConfig(String host, int port, String interfaceName) {
		super();
		this.host = host;
		this.port = port;
		this.interfaceName = interfaceName;
	}
	
	public String getAddress(){
		return host + ":"+ port;
	}
	
	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	public int getSubscribeConnectionTimeOut() {
		return subscribeConnectionTimeOut;
	}

	public void setSubscribeConnectionTimeOut(int subscribeConnectionTimeOut) {
		this.subscribeConnectionTimeOut = subscribeConnectionTimeOut;
	}

	public int getSubscribeSessionTimeOut() {
		return subscribeSessionTimeOut;
	}

	public void setSubscribeSessionTimeOut(int subscribeSessionTimeOut) {
		this.subscribeSessionTimeOut = subscribeSessionTimeOut;
	}

    public Constants.ThriftProtocolType getThriftProtocol() {
        return thriftProtocol;
    }

    public void setThriftProtocol(Constants.ThriftProtocolType thriftProtocol) {
        this.thriftProtocol = thriftProtocol;
    }

    public Constants.TransportType getThriftTransport() {
        return thriftTransport;
    }

    public void setThriftTransport(Constants.TransportType thriftTransport) {
        this.thriftTransport = thriftTransport;
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> getInterface() {
		if (interfaceClass != null) {
	        return interfaceClass;
	    }
	    try {
	        if (interfaceName != null && interfaceName.length() > 0) {
	            this.interfaceClass = Class.forName(interfaceName);
	        }
	    } catch (ClassNotFoundException t) {
	        throw new IllegalStateException(t.getMessage(), t);
	    }
	    return interfaceClass;
	}
	
	public String getSubscribeAddress() {
		return subscribeAddress;
	}

	public void setSubscribeAddress(String subscribeAddress) {
		this.subscribeAddress = subscribeAddress;
	}
	@Deprecated
    public boolean isAnnotation() {
        return isAnnotation;
    }
    
    @Deprecated
    public void setAnnotation(boolean isAnnotation) {
        this.isAnnotation = isAnnotation;
    }
    
    
    public Object getAction() {
        return action;
    }

    public void setAction(Object action) {
        this.action = action;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public ServerInfo toServerInfo(){
		return new ServerInfo(host, port, interfaceName, timeout,
				weight, poolSize, group);
	}

    public String getMonitorAddress() {
        return monitorAddress;
    }

    public void setMonitorAddress(String monitorAddress) {
        this.monitorAddress = monitorAddress;
    }

    public boolean isClient() {
        return isClient;
    }

    public void setClient(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    public String toString() {
        return "InvokerConfig [subscribeAddress=" + subscribeAddress + ", thriftProtocol=" + thriftProtocol
                + ", thriftTransport=" + thriftTransport + ", interfaceClass=" + interfaceClass
                + ", subscribeConnectionTimeOut=" + subscribeConnectionTimeOut + ", subscribeSessionTimeOut="
                + subscribeSessionTimeOut + ", application=" + application + ", isAnnotation=" + isAnnotation
                + ", action=" + action + ", filters=" + filters + ", monitorAddress=" + monitorAddress + ", isClient="
                + isClient + ", loadBalance=" + loadBalance + ", fault=" + fault + ", retries=" + retries
                + ", interval=" + interval + ", serviceProtocol=" + serviceProtocol + ", accessable=" + accessable
                + ", host=" + host + ", port=" + port + ", interfaceName=" + interfaceName + ", timeout=" + timeout
                + ", weight=" + weight + ", poolSize=" + poolSize + ", group=" + group + "]";
    }

}
