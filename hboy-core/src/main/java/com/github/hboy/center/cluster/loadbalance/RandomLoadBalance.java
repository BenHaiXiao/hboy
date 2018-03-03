 
package com.github.hboy.center.cluster.loadbalance;

import java.util.List;
import java.util.Random;

import com.github.hboy.common.util.Constants;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 16:20
 */
public class RandomLoadBalance extends AbstractLoadBalance{
	
	private final Random random = new Random();
	
	@Override
	public Constants.LoadBalanceType getLoadBalanceType() {
		return Constants.LoadBalanceType.RANDOM;
	}

    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, InvokerConfig url, Invocation invocation) {
        int length = invokers.size(); // 总个数
        int totalWeight = 0; // 总权重
        boolean sameWeight = true; // 权重是否都一样
        for (int i = 0; i < length; i++) {
            int weight = invokers.get(i).getUrl().getWeight();
            totalWeight += weight; // 累计总权重
            if (sameWeight && i > 0
                    && weight != invokers.get(i - 1).getUrl().getWeight()) {
                sameWeight = false; // 计算所有权重是否一样
            }
        }
        if (totalWeight > 0 && ! sameWeight) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offset = random.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (int i = 0; i < length; i++) {
                offset -= invokers.get(i).getUrl().getWeight();
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }
        // 如果权重相同或权重为0则均等随机
        return invokers.get(random.nextInt(length));
    }
    

}