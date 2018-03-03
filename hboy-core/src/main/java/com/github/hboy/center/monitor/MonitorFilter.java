package com.github.hboy.center.monitor;

import java.net.SocketAddress;

import com.github.hboy.center.filter.Filter;
import com.github.hboy.center.monitor.api.FailureMessage;
import com.github.hboy.center.monitor.api.MonitorService;
import com.github.hboy.center.monitor.api.StatisticsTitle;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.RemotingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 10:10
 */
public class MonitorFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MonitorFilter.class);
    
    public Object invoke(Invoker<?> invoker, Invocation invocation) throws Throwable{
        if (!MonitorService.class.getName().equals(invoker.getUrl().getInterfaceName())
                && invoker.getUrl().getMonitorAddress() != null && !"".equals(invoker.getUrl().getMonitorAddress())) {
            long start = System.nanoTime(); // 记录起始时间戮
            try {
                Object result = invoker.invoke(invocation); // 让调用链往下执行
                collect(invoker, invocation,start);
                return result;
            } catch (Throwable e) {
                collect(invoker, invocation,start,e);
                throw e;
            } 
        } else {
            return invoker.invoke(invocation);
        }
    }
    
    
    private void collect(Invoker<?> invoker, Invocation invocation, long start,Throwable e) {
        try {
            InvokerConfig invokerConfig = invoker.getUrl();
            long timestamp = System.nanoTime();
            long elapsed = timestamp - start; // 计算调用耗时
            StatisticsTitle title =  toStatisticsTitle(invoker, invocation);
            Monitor monitor = MonitorFactory.getMonitor(invokerConfig);
            FailureMessage failureMessage = new FailureMessage();
            failureMessage.setElapsed(elapsed);
            failureMessage.setTimestamp(System.currentTimeMillis());
            failureMessage.setInput(invocation.toString());
            failureMessage.setException(throwableToString(e));
            // 本机地址，和远程地址
            // 相对客户端来说，getAddress 是远程地址，
            // 相对服务端来说，getAddress 是暴露的端口和地址
            String localAddress = "";
            String remoteAddress = ""; // 远程地址 ip:port
            if (invokerConfig.isClient()) {
                remoteAddress = invokerConfig.getAddress(); // 本地地址 ip:port
                SocketAddress address = RemotingContext.getContext().getLocalAddress();
                if(address != null){
                    localAddress = address.toString();
                }
            } else {
                SocketAddress address = RemotingContext.getContext().getRemoteAddress();
                if(address != null){
                    remoteAddress = address.toString();
                }
                localAddress = invokerConfig.getAddress(); // 本地地址 ip:port;
            }
            monitor.collectFailure(title, failureMessage,localAddress,remoteAddress);
        } catch (Throwable t) {
            logger.error("Failed to monitor count service " + invoker.getUrl() + ", cause: " + t.getMessage(), t);
        }
    }
    
    private String throwableToString(Throwable e){
        if(e == null){
            return "";
        }
        StringBuffer s = new StringBuffer();
        s.append(e.getMessage());
        StackTraceElement[] elements = e.getStackTrace();
        if(elements == null || elements.length == 0){
            return s.toString();
        }
        for(StackTraceElement element : elements){
            s.append(element.toString());
        }
        return s.toString();
    }
    
    private StatisticsTitle toStatisticsTitle(Invoker<?> invoker, Invocation invocation) {
        InvokerConfig invokerConfig = invoker.getUrl();
        // ---- 服务信息获取 ----
        String application = invokerConfig.getApplication();
        String service = invoker.getInterface().getName(); // 获取服务名称
        String method = invocation.getPath(); // 获取方法名
        StatisticsTitle title = new StatisticsTitle();
        title.setApplication(application);
        title.setService(service);
        title.setMethod(method);
        boolean isClient = invokerConfig.isClient(); // 客户端or服务端
        title.setClient(isClient);
        return title;
    }
    // 信息采集
    private void collect(Invoker<?> invoker, Invocation invocation, long start) {
        try {
            InvokerConfig invokerConfig = invoker.getUrl();
            long elapsed = System.nanoTime() - start; // 计算调用耗时
            StatisticsTitle title =  toStatisticsTitle(invoker, invocation);
            // 本机地址，和远程地址
            // 相对客户端来说，getAddress 是远程地址，
            // 相对服务端来说，getAddress 是暴露的端口和地址
            String localAddress = "";
            String remoteAddress = ""; // 远程地址 ip:port
            if (invokerConfig.isClient()) {
                remoteAddress = invokerConfig.getAddress(); // 本地地址 ip:port
                SocketAddress address = RemotingContext.getContext().getLocalAddress();
                if(address != null){
                    localAddress = address.toString();
                }
            } else {
                SocketAddress address = RemotingContext.getContext().getRemoteAddress();
                if(address != null){
                    remoteAddress = address.toString();
                }
                localAddress = invokerConfig.getAddress(); // 本地地址 ip:port;
            }
            Monitor monitor = MonitorFactory.getMonitor(invokerConfig);
            monitor.collectSuccess(title, elapsed,localAddress,remoteAddress);
        } catch (Throwable t) {
            logger.error("Failed to monitor count service " + invoker.getUrl() + ", cause: " + t.getMessage(), t);
        }
    }
    
}