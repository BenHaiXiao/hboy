package com.github.hboy.center.cluster.fault;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.hboy.center.cluster.LoadBalance;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.subscribe.directory.Directory;
import com.github.hboy.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.common.config.InvokerConfig;

/**
 * 调用服务失败，间隔时间屏蔽
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 12:55
 */
public class FailtimeFault<T> extends AbstractFault<T> {
	Logger logger = LoggerFactory.getLogger(FailtimeFault.class);
	
	private final static ConcurrentMap<InvokerConfig, Long> intervalInvoke = new ConcurrentHashMap<InvokerConfig, Long>();

	public FailtimeFault(InvokerConfig clientUrl,Directory<T> directory){
		super(clientUrl,directory);
	}

	public Constants.FaultType name = Constants.FaultType.FAILTIME;
	
 
	@Override
	protected Object doInvoke(Invocation invocation, List<Invoker<T>> invokers,
                              LoadBalance loadbalance) throws Throwable {
		
		checkInvokers(invokers, invocation);
		
		long interval = invokers.get(0).getUrl().getInterval();
		
		List<Invoker<T>> invoked = new ArrayList<Invoker<T>>(invokers.size());  
		Invoker<T> invoker = null;
		for (int i = 0; i < invokers.size(); i++) {
			try {
				invoker = select(invokers, invoked, loadbalance, invocation);
				InvokerConfig url = invoker.getUrl();
				if(intervalInvoke.containsKey(url)){
					//校验是否过了时间隔
					 if(System.currentTimeMillis() -
							 (intervalInvoke.get(url).longValue()) < interval){
						 invoked.add(invoker);
						 continue;
					 }
					 intervalInvoke.remove(url);
				}
				return invoker.invoke(invocation);
			} catch (Exception e) {
				// 重试日志
				logger.warn("failtime relselect fail error :" + e.getMessage(),e);
				intervalInvoke.putIfAbsent(invoker.getUrl(),
						System.currentTimeMillis());
			}
		}
		invoker = loadbalance.select(invokers, clientUrl, invocation);
		return invoker.invoke(invocation);
	}
	
	@Override
	public void destroy() {
		intervalInvoke.clear();
		super.destroy();
	}
	
}

	
