package com.github.hboy.common.config;
/**
 * @author xiaobenhai
 * Date: 2016/3/26
 * Time: 15:22
 */
import com.github.hboy.common.util.Constants;

public class Configuration extends ServerInfo{
    
    protected Constants.LoadBalanceType loadBalance;  //负载规则
    
    protected Constants.FaultType fault;              //容错机制
    
    protected int retries = 3;              //失败重试次数
    
    protected long interval = 30 * 1000;    //异常间隔调用规则,间隔时间默认30秒
    
    protected Constants.ServiceProtocolType serviceProtocol = Constants.ServiceProtocolType.thrift;
    
    protected boolean accessable =  true;
    
    private String category;
    
    public Configuration(){}
    
    public Configuration(Constants.LoadBalanceType loadBalance, Constants.FaultType fault, int retries, long interval,
                         Constants.ServiceProtocolType serviceProtocol, boolean accessable) {
        super();
        this.loadBalance = loadBalance;
        this.fault = fault;
        this.retries = retries;
        this.interval = interval;
        this.serviceProtocol = serviceProtocol;
        this.accessable = accessable;
    }
    
    public Configuration(String host, int port, String interfaceName, int timeout, int weight, int poolSize,
                         String group, Constants.LoadBalanceType loadBalance, Constants.FaultType fault, int retries, long interval,
                         Constants.ServiceProtocolType serviceProtocol, boolean accessable, String category) {
        super(host, port, interfaceName, timeout, weight, poolSize, group);
        this.loadBalance = loadBalance;
        this.fault = fault;
        this.retries = retries;
        this.interval = interval;
        this.serviceProtocol = serviceProtocol;
        this.accessable = accessable;
        this.category = category;
    }

    
    public Configuration(String host, int port, String interfaceName, int timeout, int weight, int poolSize,
            String group) {
        super(host, port, interfaceName, timeout, weight, poolSize, group);
    }
    
    public Constants.LoadBalanceType getLoadBalance() {
        return loadBalance;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public boolean isAccessable() {
        return accessable;
    }
    
    public boolean getAccessable() {
        return this.accessable;
    }
    
    public void setAccessable(boolean accessable) {
        this.accessable = accessable;
    }

    public Constants.ServiceProtocolType getServiceProtocol() {
        return serviceProtocol;
    }

    public void setServiceProtocol(Constants.ServiceProtocolType serviceProtocol) {
        this.serviceProtocol = serviceProtocol;
    }
    
    
    public ServerInfo toServerInfo(){
        return new ServerInfo(host,port,interfaceName,timeout,weight,poolSize,group);
    }
}
