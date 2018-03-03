package com.github.hboy.center.remoting;


import java.util.List;

import com.github.hboy.center.subscribe.directory.Directory;
import com.github.hboy.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.cluster.fault.AbstractFault;
import com.github.hboy.center.cluster.fault.FaultInvokerFactory;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.common.config.InvokerConfig;

/**
 * ZK有变化时，可能需要调整实际的Invoker，有了这里的封装，
 * 只需要变动这里的invoker实例，上层的使用都就不需要有变动。
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 10:50
 */
public class InvokerAgent<T> implements Invoker<T>{
	
    Logger logger = LoggerFactory.getLogger(AbstractFault.class);
    
    private volatile Constants.FaultType faultType = Constants.FaultType.FAILFASE;
    
    private volatile Invoker<T> invoker;
    
    protected final Directory<T> directory;
    
    protected final InvokerConfig clientUrl;
    
    protected boolean destroyed = false;
    
    public InvokerAgent(InvokerConfig clientUrl,Directory<T> directory) {
        if (directory == null){
            throw new IllegalArgumentException("service directory == null");
        }
        this.directory = directory ;
        this.clientUrl = clientUrl;
    }
 

    public InvokerConfig getUrl() {
        return clientUrl;
    }

    public boolean isAvailable() {
        return directory.isAvailable();
    }

    public void destroy() {
        destroyed = true;
        directory.destroy();
    }

    
    public Object invoke(final Invocation invocation) throws Throwable {
        checkIsDestoried();
        List<Invoker<T>> invokers = directory.list(invocation);
        checkInvokers(invokers,invocation);
        Constants.FaultType type = null;
        if (invokers != null && invokers.size() > 0) {
            type = invokers.get(0).getUrl().getFault();
            //如果容错规则发生变化,重新设置invoker
            if(type != faultType){
                faultType = type;
                invoker = FaultInvokerFactory.getFault(clientUrl,faultType,directory);
            }
        }
        //如果第一次 invoke == null,获取fault invoke
        if(invoker == null){
            invoker = FaultInvokerFactory.getFault(clientUrl,faultType,directory);
        }
        return invoker.invoke(invocation);
    }
    
    protected void checkIsDestoried() throws RemotingException{
        if(destroyed){
            throw new RemotingException("service： " + getInterface() + " is now destroyed");
        }
    }
    @Override
    public Class<T> getInterface() {
        return clientUrl.getInterface();
    }
   
    protected void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) throws RemotingException{
        if (invokers == null || invokers.size() == 0) {
            throw new RemotingException("Failed to invoke the method "
                    + invocation.getMethod().getName() + " in the service " + getInterface() + ",Invokers is null");
        }
    }
    
}
