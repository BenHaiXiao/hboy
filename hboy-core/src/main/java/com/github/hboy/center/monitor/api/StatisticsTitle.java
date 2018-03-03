package com.github.hboy.center.monitor.api;

import java.io.Serializable;

/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 10:58
 */
public class StatisticsTitle implements Serializable{
    
    private static final long serialVersionUID = -354666124610174582L;

    protected String application; //应用名称
    
    protected String service; //接口名称

    protected String method;  //方法
    
    protected boolean isClient;  //客户端or服务端
    
    public StatisticsTitle() {
        super();
    }

    public StatisticsTitle(String application, String service, String method, boolean isClient) {
        super();
        this.application = application;
        this.service = service;
        this.method = method;
        this.isClient = isClient;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isClient() {
        return isClient;
    }

    public void setClient(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        result = prime * result + (isClient ? 1231 : 1237);
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatisticsTitle other = (StatisticsTitle) obj;
        if (application == null) {
            if (other.application != null)
                return false;
        } else if (!application.equals(other.application))
            return false;
        if (isClient != other.isClient)
            return false;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        if (service == null) {
            if (other.service != null)
                return false;
        } else if (!service.equals(other.service))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StatisticsTitle [application=" + application + ", service=" + service + ", method=" + method
                + ", isClient=" + isClient + "]";
    }

}
