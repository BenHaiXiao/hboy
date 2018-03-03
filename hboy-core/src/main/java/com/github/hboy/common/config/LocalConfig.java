package com.github.hboy.common.config;
/**
 * @author xiaobenhai
 * Date: 2016/3/26
 * Time: 12:47
 */
public class LocalConfig extends AbstractInterfaceConfig{
    
	private String host;	//服务端host
    
    private int port;		//端口
    
    private String monitorAddress;    //监控地址
    
	public LocalConfig(){
    }

    public LocalConfig(String host, int port) {
		this.host = host;
		this.port = port;
    }
    
    public LocalConfig(String host, int port,String monitorAddress) {
        this.host = host;
        this.port = port;
        this.monitorAddress = monitorAddress;
    }
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMonitorAddress() {
        return monitorAddress;
    }

    public void setMonitorAddress(String monitorAddress) {
        this.monitorAddress = monitorAddress;
    }
    
}
