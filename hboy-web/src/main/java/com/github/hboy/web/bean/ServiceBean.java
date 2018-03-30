package com.github.hboy.web.bean;

import java.util.Date;

public class ServiceBean {
	
	private String serviceName ; 
	
	private String serviceName_CN ; 
	
	private String Creator ; 
	
	private String version ;
	
	private Date createTime ; 
	
	private String parentNodeName ; 
	
	private boolean accessable = true;
	
	public ServiceBean(String serviceName,String creator) {
		super();
		this.serviceName = serviceName;
		this.Creator = creator ; 
	}
	public ServiceBean(String serviceName) {
		super();
		this.serviceName = serviceName;
	}
	
	public ServiceBean() {
		super();
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName_CN() {
		return serviceName_CN;
	}

	public void setServiceName_CN(String serviceName_CN) {
		this.serviceName_CN = serviceName_CN;
	}

	public String getCreator() {
		return Creator;
	}

	public void setCreator(String creator) {
		Creator = creator;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getParentNodeName() {
		return parentNodeName;
	}
	
	public void setParentNodeName(String parentNodeName) {
		this.parentNodeName = parentNodeName;
	}
	
    public boolean isAccessable() {
        return accessable;
    }
    public void setAccessable(boolean accessable) {
        this.accessable = accessable;
    } 
	

    public boolean getAccessable() {
        return accessable;
    }
	

}
