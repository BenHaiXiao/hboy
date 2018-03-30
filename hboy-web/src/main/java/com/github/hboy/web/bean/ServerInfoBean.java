package com.github.hboy.web.bean;


import com.github.hboy.common.config.Configuration;
import com.github.hboy.common.config.ServerInfo;

public class ServerInfoBean extends Configuration {
	
    public ServerInfoBean(){
        
    }

    public ServerInfoBean(String appName, String path) {
        super();
        this.appName = appName;
        this.path = path;
    }
    
    public ServerInfoBean(String appName, String path,Configuration configuration) {
        super(configuration.getHost(), configuration.getPort(), configuration.getInterfaceName(), configuration
                .getTimeout(), configuration.getWeight(), configuration.getPoolSize(), configuration.getGroup(),
                configuration.getLoadBalance(), configuration.getFault(), configuration.getRetries(), configuration
                        .getInterval(), configuration.getServiceProtocol(), configuration.getAccessable(),
                configuration.getCategory());
        this.appName = appName;
        this.path = path;
    }

    public ServerInfoBean(String appName, String path,ServerInfo configuration) {
        super(configuration.getHost(), configuration.getPort(), configuration.getInterfaceName(), configuration
                .getTimeout(), configuration.getWeight(), configuration.getPoolSize(), configuration.getGroup());
        this.appName = appName;
        this.path = path;
    }
    

    private String appName ; 
    
    private String path ; 
    
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

   

//	private int subscribeConnectionTimeOut = 10000;	//zk连接超时时间

//	private int subscribeSessionTimeOut = 30000;	//zkSession超时时间
	
	
}
