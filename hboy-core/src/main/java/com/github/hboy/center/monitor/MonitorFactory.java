package com.github.hboy.center.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.monitor.api.MonitorService;
import com.github.hboy.center.proxy.JDKProxy;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.protocol.http.HttpProtocol;
import com.github.hboy.common.config.InvokerConfig;


/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 14:49
 */
public class MonitorFactory {
    
    private static Logger logger = LoggerFactory.getLogger(MonitorFactory.class);
    // 
    private static final ReentrantLock LOCK = new ReentrantLock();
    
    // <Address, MonitorService>
    private static final Map<String, Monitor> MONITORS = new ConcurrentHashMap<String, Monitor>();
    
    public static Collection<Monitor> getMonitors() {
        return Collections.unmodifiableCollection(MONITORS.values());
    }

    public static Monitor getMonitor(InvokerConfig invokerConfig) {
        String key = invokerConfig.getMonitorAddress();
        LOCK.lock();
        try {
            Monitor monitor = MONITORS.get(key);
            if (monitor != null) {
                return monitor;
            }
            try {
                InvokerConfig monitorConfig = new InvokerConfig();
                monitorConfig.setInterfaceName(MonitorService.class.getName());
                String[] address = key.split(":");
                monitorConfig.setHost(address[0]);
                monitorConfig.setPort(Integer.valueOf(address[1]));
                monitor = createMonitor(monitorConfig);
            } catch (RemotingException e) {
//                throw new IllegalStateException("Can not create monitor " + invokerConfig);
                logger.error("Can not create monitor :"  + invokerConfig + ", error msg :" + e.getMessage(),e);
                
            }
            MONITORS.put(key, monitor);
            return monitor;
        } finally {
            // 释放锁
            LOCK.unlock();
        }
    }

    private static Monitor createMonitor(InvokerConfig invokerConfig) throws RemotingException {
         
        HttpProtocol httpProtocol = new HttpProtocol();
        Invoker<MonitorService> monitorInvoker = httpProtocol.refer(MonitorService.class, invokerConfig,null);
        MonitorService monitorService = JDKProxy.getInvokerProxy(monitorInvoker,MonitorService.class);
        return new Monitor(monitorInvoker, monitorService);
    }
    
}