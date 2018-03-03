 
package com.github.hboy.center.cluster.fault;

import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.subscribe.directory.Directory;
import com.github.hboy.common.util.Constants;
import com.github.hboy.common.config.InvokerConfig;


/**
 * 调用服务失败，间隔时间屏蔽
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 12:59
 */
public class FaultInvokerFactory {

	
	public static <T> Invoker<T> getFault(InvokerConfig clientUrl, Constants.FaultType type, Directory<T> directory){
	    Invoker<T>  fault = null;
		
		if(Constants.FaultType.FAILOVER == type){
			fault = new FailoverFault<T>(clientUrl,directory);
		}else if(Constants.FaultType.FAILFASE == type){
			fault = new FailfastFault<T>(clientUrl,directory);
		}else{
			fault = new FailfastFault<T>(clientUrl,directory);
		}
		return fault;
	}
	
	
}