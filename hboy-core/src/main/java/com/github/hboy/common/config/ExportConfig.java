package com.github.hboy.common.config;
/**
 * @author xiaobenhai
 * Date: 2016/3/26
 * Time: 15:36
 */
public class ExportConfig extends AbstractInterfaceConfig{
	
	
	private String              host;	 // 服务IP地址
	
	private Integer             port;   // 服务端口

	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	
	public Integer getPort() {
		return port;
	}
	
	
	public void setPort(Integer port) {
		this.port = port;
	}
	  
}
