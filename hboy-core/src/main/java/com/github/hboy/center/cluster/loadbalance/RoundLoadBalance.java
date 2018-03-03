
package com.github.hboy.center.cluster.loadbalance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.hboy.common.util.Constants;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.common.config.InvokerConfig;


/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 15:39
 */
public class RoundLoadBalance  extends AbstractLoadBalance{
    
	private final static ConcurrentMap<String, AtomicInteger> sequences = new ConcurrentHashMap<String, AtomicInteger>();

	@Override
	public Constants.LoadBalanceType getLoadBalanceType() {
		return Constants.LoadBalanceType.ROUND;
	}

	@Override
	protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, InvokerConfig clientUrl,
			Invocation invocation) {
		String key = clientUrl.getInterface() + "."
				+ invocation.getMethod().getName();
		AtomicInteger sequence = sequences.get(key);
		if (sequence == null) {
			sequences.putIfAbsent(key, new AtomicInteger());
			sequence = sequences.get(key);
		}
		// 取模轮循
		int index = sequence.getAndIncrement();
		if (index >= Integer.MAX_VALUE || index < 0) {
			sequence.set(0);
			index = sequence.getAndIncrement();
		}
		return invokers.get(index % invokers.size());
	}

}