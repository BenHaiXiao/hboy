package com.github.hboy.center.pooling;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

/**
 * 对原有Config的改造，增加getter/setter以便spring配置。
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 17:49
 */
public class PoolConfig extends Config {
    public int getMaxIdle() {
        return maxIdle;
    }
    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }
    public int getMinIdle() {
        return minIdle;
    }
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }
    public int getMaxActive() {
        return maxActive;
    }
    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }
    public long getMaxWait() {
        return maxWait;
    }
    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }
    public byte getWhenExhaustedAction() {
        return whenExhaustedAction;
    }
    public void setWhenExhaustedAction(byte whenExhaustedAction) {
        this.whenExhaustedAction = whenExhaustedAction;
    }
    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }
    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }
    public boolean isTestOnReturn() {
        return testOnReturn;
    }
    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }
    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }
    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }
    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }
    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }
    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }
    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }
    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }
    public long getSoftMinEvictableIdleTimeMillis() {
        return softMinEvictableIdleTimeMillis;
    }
    public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }
    public boolean isLifo() {
        return lifo;
    }
    public void setLifo(boolean lifo) {
        this.lifo = lifo;
    }
}
