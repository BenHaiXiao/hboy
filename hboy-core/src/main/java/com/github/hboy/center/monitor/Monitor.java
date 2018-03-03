package com.github.hboy.center.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hboy.center.pooling.thread.NamedThreadFactory;
import com.github.hboy.center.remoting.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.monitor.api.FailureMessage;
import com.github.hboy.center.monitor.api.MonitorService;
import com.github.hboy.center.monitor.api.Statistics;
import com.github.hboy.center.monitor.api.StatisticsMessage;
import com.github.hboy.center.monitor.api.StatisticsTitle;

/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 10:46
 */
public class Monitor {
    
    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
    
    // 定时任务执行器
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2, new NamedThreadFactory("MonitorSendTimer", true));

    // 统计信息收集定时器
    private final ScheduledFuture<?> sendFuture;
    
    private final Invoker<MonitorService> monitorInvoker;

    private final MonitorService monitorService;

    private final long monitorInterval = 60000;
    
    private final ConcurrentMap<StatisticsTitle, ConcurrentMap<String,StatisticsMessage>> statisticsMap = new ConcurrentHashMap<StatisticsTitle, ConcurrentMap<String,StatisticsMessage>>();

    private final java.util.concurrent.locks.ReentrantLock successLock = new ReentrantLock();
    
    private final java.util.concurrent.locks.ReentrantLock failureLock = new ReentrantLock();
    
    public Monitor(Invoker<MonitorService> monitorInvoker, MonitorService monitorService) {
        this.monitorInvoker = monitorInvoker;
        this.monitorService = monitorService;
        // 启动统计信息收集定时器
        sendFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                // 收集统计信息
                try {
                    send();
                } catch (Throwable t) { // 防御性容错
                    logger.error("Unexpected error occur at send statistic, cause: " + t.getMessage(), t);
                }
            }
        }, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
    }
    
    public void send() {
        if (logger.isInfoEnabled()) {
            logger.info("Send statistics to monitor " + monitorInvoker.getUrl());
        }
        String timestamp = String.valueOf(System.currentTimeMillis());

        List<Statistics> statiscicsList = new ArrayList<Statistics>();
        for (Entry<StatisticsTitle, ConcurrentMap<String, StatisticsMessage>> en : statisticsMap.entrySet()) {
            ConcurrentMap<String, StatisticsMessage> statisticsMessage = statisticsMap.put(en.getKey(),
                    new ConcurrentHashMap<String, StatisticsMessage>());
            if (statisticsMessage == null) {
                continue;
            }
            for (StatisticsMessage message : statisticsMessage.values()) {
                long  successNum = message.getSuccessNum().get();
                if(successNum != 0){
                    message.setAvgElapsed(message.getAvgElapsed() / successNum);
                }
                Statistics statistics = new Statistics(en.getKey(), message, timestamp);
                statiscicsList.add(statistics);
            }
        }
        monitorService.collect(statiscicsList);
    }
    
    public void collectFailure(StatisticsTitle statistics, FailureMessage failureMessage, String localAddress, String remoteAddress) {
        if(statistics == null){
            return;
        }
        StatisticsMessage message = getStatisticsMessage(statistics,localAddress,remoteAddress);
        List<FailureMessage> failureList = message.getFailure();
        if(failureList == null){
            try{
                failureLock.lock();
                failureList = message.getFailure();
                if(failureList == null){
                    message.setFailure(Collections.synchronizedList(new ArrayList<FailureMessage>()));
                    failureList = message.getFailure();
                }
            }finally{
                failureLock.unlock();
            }
        }
        failureList.add(failureMessage);
    }

    
    private  StatisticsMessage getStatisticsMessage(StatisticsTitle statistics,String localAddress,String remoteAddress){
        ConcurrentMap<String, StatisticsMessage> statisticsMessages = statisticsMap.get(statistics);
        String key = localAddress + remoteAddress;
        if(statisticsMessages == null){
            statisticsMap.putIfAbsent(statistics, new ConcurrentHashMap<String, StatisticsMessage>());
            statisticsMessages = statisticsMap.get(statistics);
        }
        StatisticsMessage message = statisticsMessages.get(key);
        if(message == null){
            statisticsMessages.putIfAbsent(key, new StatisticsMessage(localAddress,remoteAddress));
            message = statisticsMessages.get(key);
        }
        return message;
    }
    
    
    public void collectSuccess(StatisticsTitle statistics,Long elapsed,String localAddress,String remoteAddress) {
        if(statistics == null){
            return;
        }
        StatisticsMessage  message = getStatisticsMessage(statistics,localAddress,remoteAddress);
        message.addSuccessNum();
        try{
            successLock.lock();
            if(elapsed > message.getMaxElapsed()){
                message.setMaxElapsed(elapsed);
            }
            if(message.getMinElapsed() == -1){
                message.setMinElapsed(elapsed);
            }
            if(elapsed < message.getMinElapsed()){
                message.setMinElapsed(elapsed);
            }
            message.setAvgElapsed(message.getAvgElapsed() + elapsed);
        }finally{
            successLock.unlock();
        }
    }
    
    public void destroy() {
        try {
            sendFuture.cancel(true);
        } catch (Throwable t) {
            logger.error("Unexpected error occur at cancel sender timer, cause: " + t.getMessage(), t);
        }
        monitorInvoker.destroy();
    }

}