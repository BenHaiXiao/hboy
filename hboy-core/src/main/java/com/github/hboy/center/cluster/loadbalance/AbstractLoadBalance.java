 
package com.github.hboy.center.cluster.loadbalance;

import java.util.ArrayList;
import java.util.List;

import com.github.hboy.center.cluster.LoadBalance;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.common.util.Constants;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 14:39
 */
public abstract class AbstractLoadBalance implements LoadBalance {
	
    
    public <T> Invoker<T> select(List<Invoker<T>> invokers, InvokerConfig clientUrl, Invocation invocation) {
        if (invokers == null || invokers.size() == 0){
        	return null;
        }
        if (invokers.size() == 1){
        	return invokers.get(0);
        }
        /**
         * 做group选择
         */
        if(Constants.DEFAULT_GROUP.equals(clientUrl.getGroup())){
        	return doSelect(invokers, clientUrl, invocation);
        }
        
        List<Invoker<T>> groupInvokes = new ArrayList<Invoker<T>>();
    	for(Invoker<T> invoker : invokers){
			if ((clientUrl.getGroup().equals(invoker.getUrl().getGroup()) || Constants.DEFAULT_GROUP
					.equals(invoker.getUrl().getGroup()))
					&& invoker.isAvailable()) {
				groupInvokes.add(invoker);
			}
    	}
    	
    	if (groupInvokes.size() == 1){
        	return groupInvokes.get(0);
        }
    	if(groupInvokes.size() > 0){
    		invokers = groupInvokes;
    	}
        return doSelect(invokers, clientUrl, invocation);
    }
    
    
    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, InvokerConfig clientUrl, Invocation invocation);
}