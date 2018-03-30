package com.github.hboy.web.bean;

public class ApplicationBean {
	
	private String  appName_EN ; 
	
	private String appName_CN ;
	
	private String publisher ; 
	
	private String version ;
	

	public String getAppName_EN() {
		return appName_EN;
	}

	public void setAppName_EN(String appName_EN) {
		this.appName_EN = appName_EN;
	}

	public String getAppName_CN() {
		return appName_CN;
	}

	public void setAppName_CN(String appName_CN) {
		this.appName_CN = appName_CN;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	} 
	
	public ApplicationBean(String ENappName ,String CNappName){
		this.appName_CN =CNappName ; 
		this.appName_EN = ENappName ; 
	}
	
	public ApplicationBean(){}

}
