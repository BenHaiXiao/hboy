package com.github.hboy.center.cluster.fault;

import java.util.ArrayList;
import java.util.List;

import com.github.hboy.center.cluster.LoadBalance;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.subscribe.directory.Directory;
import com.github.hboy.common.config.InvokerConfig;
import com.github.hboy.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 失败可以重试
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 12:39
 */
public class FailoverFault<T> extends AbstractFault<T> {
	
	Logger logger = LoggerFactory.getLogger(FailoverFault.class);
	
	public FailoverFault(InvokerConfig clientUrl, Directory<T> directory){
		super(clientUrl,directory);
	}

	public Constants.FaultType name = Constants.FaultType.FAILOVER;
	
	/**
	 * 容错规则，失败重试
	 */
	@Override
	protected Object doInvoke(Invocation invocation, List<Invoker<T>> invokers,
                              LoadBalance loadbalance) throws Throwable {
		
		checkInvokers(invokers, invocation);
		int len = invokers.get(0).getUrl().getRetries();
		if (len <= 0) {
			len = 1;
		}
		List<Invoker<T>> invoked = new ArrayList<Invoker<T>>(invokers.size()); // invoked
		for (int i = 0; i < len; i++) {
			try{
				Invoker<T> invoker = select(invokers, invoked, loadbalance,invocation);
	            invoked.add(invoker);
	    		return invoker.invoke(invocation);
			}catch(RemotingException e){
				//重试日志
				logger.warn("failover relselect fail error :" + e.getMessage(),e);
				if(i == len -1){
					throw new RemotingException(e.getMessage(),e);
				}
			}
		}
		Invoker<T> invoker = loadbalance.select(invokers, clientUrl, invocation);
		Object rs = invoker.invoke(invocation);
		return rs;
	}
	
	
}

	
