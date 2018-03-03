package com.github.hboy.common.config;

/**
 * @author xiaobenhai
 * Date: 2016/3/26
 * Time: 17:19
 */
public class CenterConfig{
    
	private String address;
	
	private int connectionTimeOut = 10000;

	private int sessionTimeOut = 30000;
	
	private String application;	//应用名称
	
	private String monitorAddress;    //监控地址

	public CenterConfig(String address, int connectionTimeOut,
			int sessionTimeOut) {
		super();
		this.address = address;
		this.connectionTimeOut = connectionTimeOut;
		this.sessionTimeOut = sessionTimeOut;
	}

	public CenterConfig(String address, int connectionTimeOut,
            int sessionTimeOut,String monitorAddress) {
        super();
        this.address = address;
        this.connectionTimeOut = connectionTimeOut;
        this.sessionTimeOut = sessionTimeOut;
        this.monitorAddress = monitorAddress;
    }
	
	public CenterConfig() {
		super();
	}
	
	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}
	
	public CenterConfig(String address) {
		super();
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}

	public int getSessionTimeOut() {
		return sessionTimeOut;
	}

	public void setSessionTimeOut(int sessionTimeOut) {
		this.sessionTimeOut = sessionTimeOut;
	}

    public String getMonitorAddress() {
        return monitorAddress;
    }

    public void setMonitorAddress(String monitorAddress) {
        this.monitorAddress = monitorAddress;
    }
	
}
