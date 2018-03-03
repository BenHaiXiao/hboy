 
package com.github.hboy.center.cluster.loadbalance;

import java.util.HashMap;
import java.util.Map;

import com.github.hboy.common.util.Constants;
import com.github.hboy.center.cluster.LoadBalance;


/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 16:29
 */
public class LoadBalanceFactory {

	
	private static Map<Constants.LoadBalanceType,LoadBalance> loadBalances = new HashMap<Constants.LoadBalanceType,LoadBalance>();
	
	public static LoadBalance getLoadBalance(Constants.LoadBalanceType type){
		LoadBalance lb = loadBalances.get(type);
		if(lb == null){
			synchronized(loadBalances){
				if(lb == null){
					if(Constants.LoadBalanceType.RANDOM == type){
						loadBalances.put(Constants.LoadBalanceType.RANDOM, new RandomLoadBalance());
					}else if(Constants.LoadBalanceType.ROUND == type){
						loadBalances.put(Constants.LoadBalanceType.ROUND, new RoundLoadBalance());
					}else{
						loadBalances.put(Constants.LoadBalanceType.RANDOM, new RandomLoadBalance());
					}
					lb = loadBalances.get(type);
				}
			}
		}
		return lb;
	}
	
	
	
}