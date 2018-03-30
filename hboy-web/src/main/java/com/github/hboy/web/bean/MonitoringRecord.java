package com.github.hboy.web.bean;

/**
 * 监控统计记录
 * @author wenziheng
 *
 */
public class MonitoringRecord {

    private String service;       // 服务接口名称
    
    private String localAddress;  // 如果是调用发起方，则不显示port
    private String remoteAddress; // 如果是调用发起方，则不显示port
    
    private String tps;           // 每秒平均调用次数

    private String averElapsed;   // 平均耗时
    private String maxElapsed;    // 最大耗时
    private String minElapsed;    // 最小耗时
    
    private String successNums;  // 成功次数
    private String failureNums;  // 失败次数
    private String successRate;   // 成功率
    
    private String periodStart;        // 统计开始时间点（时间戳）
    private String periodEnd;        // 统计结束时间点(时间戳)

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
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

    public String getTps() {
        return tps;
    }

    public void setTps(String tps) {
        this.tps = tps;
    }

    public String getAverElapsed() {
        return averElapsed;
    }

    public void setAverElapsed(String averElapsed) {
        this.averElapsed = averElapsed;
    }

    public String getMaxElapsed() {
        return maxElapsed;
    }

    public void setMaxElapsed(String maxElapsed) {
        this.maxElapsed = maxElapsed;
    }

    public String getMinElapsed() {
        return minElapsed;
    }

    public void setMinElapsed(String minElapsed) {
        this.minElapsed = minElapsed;
    }

    public String getSuccessNums() {
        return successNums;
    }

    public void setSuccessNums(String successNums) {
        this.successNums = successNums;
    }

    public String getFailureNums() {
        return failureNums;
    }

    public void setFailureNums(String failureNums) {
        this.failureNums = failureNums;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }

    @Override
    public String toString() {
        return "MonitoringRecord [service=" + service + ", localAddress=" + localAddress + ", remoteAddress="
                + remoteAddress + ", tps=" + tps + ", averElapsed=" + averElapsed + ", maxElapsed=" + maxElapsed
                + ", minElapsed=" + minElapsed + ", successNums=" + successNums + ", failureNums=" + failureNums
                + ", successRate=" + successRate + ", periodStart=" + periodStart + ", periodEnd=" + periodEnd + "]";
    }
    
}
