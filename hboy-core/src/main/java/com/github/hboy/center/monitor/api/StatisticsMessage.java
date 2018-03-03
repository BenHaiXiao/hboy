package com.github.hboy.center.monitor.api;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author Administrator
 *
 */
public class StatisticsMessage implements Serializable{
    
    private static final long serialVersionUID = -2586558645059651564L;

    //成功次数 ----->
    private final AtomicLong successNum = new AtomicLong();  //成功时间list

    //失败次数 -----> 失败的详细信息
    private List<FailureMessage> failure; //失败
    
    private long maxElapsed;
    
    private long avgElapsed;
    
    private long minElapsed = -1;

    private String localAddress;   //本地地址  ip:port
    
    private String remoteAddress;  //远程地址  ip:port
    
    public StatisticsMessage(){}
    
    
    public StatisticsMessage(String localAddress,String remoteAddress){
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }
    
    public StatisticsMessage(List<FailureMessage> failure, long maxElapsed,long avgElapsed, long minElapsed,
            String localAddress, String remoteAddress) {
        super();
        this.failure = failure;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.maxElapsed = maxElapsed;
        this.avgElapsed = avgElapsed;
        this.minElapsed = minElapsed;;
    }


    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    
    public List<FailureMessage> getFailure() {
        return failure;
    }

    public void setFailure(List<FailureMessage> failure) {
        this.failure = failure;
    }

    public long getMaxElapsed() {
        return maxElapsed;
    }

    public void setMaxElapsed(long maxElapsed) {
        this.maxElapsed = maxElapsed;
    }

    public long getMinElapsed() {
        return minElapsed;
    }

    public void setMinElapsed(long minElapsed) {
        this.minElapsed = minElapsed;
    }
    
    public long getAvgElapsed() {
        return avgElapsed;
    }

    public void setAvgElapsed(long avgElapsed) {
        this.avgElapsed = avgElapsed;
    }


    public AtomicLong getSuccessNum() {
        return successNum;
    }

    public void addSuccessNum(){
        successNum.getAndIncrement();
    }


    @Override
    public String toString() {
        return "StatisticsMessage [successNum=" + successNum + ", failure=" + failure + ", maxElapsed=" + maxElapsed
                + ", avgElapsed=" + avgElapsed + ", minElapsed=" + minElapsed + ", localAddress=" + localAddress
                + ", remoteAddress=" + remoteAddress + "]";
    }
    
}
