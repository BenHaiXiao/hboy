package com.github.hboy.center.cluster.fault;

import java.util.ArrayList;
import java.util.List;

import com.github.hboy.center.cluster.LoadBalance;
import com.github.hboy.center.cluster.loadbalance.LoadBalanceFactory;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.subscribe.directory.Directory;
import com.github.hboy.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 12:35
 */
public abstract class AbstractFault<T> implements Invoker<T> {

	Logger logger = LoggerFactory.getLogger(AbstractFault.class);
	
	protected final Directory<T> directory;
	
	protected final InvokerConfig clientUrl;
	
    public AbstractFault(Directory<T> directory) {
        this(directory.getUrl(),directory);
    }
    
    public AbstractFault(InvokerConfig clientUrl,Directory<T> directory) {
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
        directory.destroy();
    }

    
    public Object invoke(final Invocation invocation) throws Throwable {
        Constants.LoadBalanceType type = Constants.LoadBalanceType.RANDOM;
        List<Invoker<T>> invokers = directory.list(invocation);
        if (invokers != null && invokers.size() > 0) {
        	type = invokers.get(0).getUrl().getLoadBalance();
        }
        LoadBalance loadbalance = LoadBalanceFactory.getLoadBalance(type);
        return doInvoke(invocation, invokers, loadbalance);
    }

    
    
    /**
	 * 选择invoker，从在已经选择的列表之外选择。
	 * @param invokers
	 * @param invoked
	 * @param loadbalance
	 * @return
	 * @throws RemotingException
	 */
	protected Invoker<T> select(List<Invoker<T>> invokers, List<Invoker<T>> selected,
				LoadBalance loadbalance,Invocation invocation) throws RemotingException {

		if (invokers.size() == 1){
			return invokers.get(0);
		}
		 
		Invoker<T> invoker = loadbalance.select(invokers, getUrl(), invocation);

		// 如果 selected中包含 ,则选出已经包含的invoker。重新loadbalance
		if ((selected != null && selected.contains(invoker))) {
			try {
				List<Invoker<T>> reselectInvokers = new ArrayList<Invoker<T>>();
				for (Invoker<T> iv : invokers) {
					if (!selected.contains(iv)) {
						reselectInvokers.add(iv);
					}
				}
				if (reselectInvokers.size() > 0) {
					return loadbalance.select(reselectInvokers, getUrl(),
							invocation);
				}
			} catch (Throwable t) {
				logger.error(
						"failover relselect fail error :"
								+ t.getMessage(),t);
			}
		}
		return invoker;
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

    protected abstract Object doInvoke(Invocation invocation, List<Invoker<T>> invokers,LoadBalance loadbalance) throws Throwable;
    
    
}