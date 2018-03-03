 
package com.github.hboy.center.cluster;

import java.util.List;

import com.github.hboy.common.util.Constants;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 15:35
 */
public interface LoadBalance {

	
	Constants.LoadBalanceType getLoadBalanceType();
	
	<T> Invoker<T> select(List<Invoker<T>> invokers,InvokerConfig url,Invocation invocation) throws RemotingException;

}