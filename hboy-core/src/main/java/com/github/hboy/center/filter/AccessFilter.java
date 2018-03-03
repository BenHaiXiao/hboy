package com.github.hboy.center.filter;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/18
 * Time: 10:53
 */
public class AccessFilter implements Filter {
	
	protected  final Logger logger = LoggerFactory.getLogger(AccessFilter.class);
	
	@Override
	public Object invoke(Invoker<?> invoker, Invocation invocation)
			throws Throwable {
		if(invoker == null){
			return null ; 
		}
		InvokerConfig invokerConfig = invoker.getUrl();
		if(invokerConfig != null && !invokerConfig.isAccessable()){
			String error = "ServiceAccessFilter forbid the access to "
					+ ""+invokerConfig.getInterfaceName()+"/"+ invocation.getPath()+" ,"
					+ "because node config of 'accessable ' is false / "+invokerConfig ; 
			logger.error(error);
			throw new AccessAuthException(error);
		}
		return invoker.invoke(invocation);
	}

}
