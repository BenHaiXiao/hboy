package com.github.hboy.center.cluster.fault;

import java.util.List;

import com.github.hboy.center.subscribe.directory.Directory;
import com.github.hboy.common.util.Constants;
import com.github.hboy.center.cluster.LoadBalance;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.common.config.InvokerConfig;

/**
 * 失败直接返回
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 12:38
 */
public class FailfastFault<T> extends AbstractFault<T> {
	
	
	public FailfastFault(InvokerConfig clientUrl,Directory<T> directory){
		super(clientUrl,directory);
	}

	public Constants.FaultType name = Constants.FaultType.FAILFASE;
	
	/**
	 * 容错规则，失败直接返回
	 */
	@Override
	protected Object doInvoke(Invocation invocation, List<Invoker<T>> invokers,
			LoadBalance loadbalance) throws Throwable {
		checkInvokers(invokers,invocation);
		Invoker<T>  invoker = loadbalance.select(invokers, clientUrl, invocation);
		Object rs = invoker.invoke(invocation);
		return rs;
	}
	
}
