package com.github.hboy.web.bean;


import com.github.hboy.common.util.Constants;


public class ConfigurationBean {

    protected Constants.LoadBalanceType loadBalance = Constants.LoadBalanceType.RANDOM;  //负载规则
    
    protected Constants.FaultType fault =  Constants.FaultType.FAILFASE;              //容错机制
    
    protected int retries = 3;              //失败重试次数
    
    protected long interval = 30 * 1000;    //异常间隔调用规则,间隔时间默认30秒
    
    protected Constants.ServiceProtocolType serviceProtocol = Constants.ServiceProtocolType.thrift;
    
    protected boolean accessable =  true;
    
    private String category = Constants.PATH_CONFIGURATION;
    
    public ConfigurationBean(){}

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

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public Constants.ServiceProtocolType getServiceProtocol() {
        return serviceProtocol;
    }

    public void setServiceProtocol(Constants.ServiceProtocolType serviceProtocol) {
        this.serviceProtocol = serviceProtocol;
    }

    public boolean isAccessable() {
        return accessable;
    }
    
    public boolean getAccessable() {
        return accessable;
    }

    public void setAccessable(boolean accessable) {
        this.accessable = accessable;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
}
