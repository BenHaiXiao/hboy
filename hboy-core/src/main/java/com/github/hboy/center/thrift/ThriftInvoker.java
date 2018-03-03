package com.github.hboy.center.thrift;


import java.lang.reflect.InvocationTargetException;

import com.github.hboy.center.pooling.InitalizableObjectPool;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.RemotingContext;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 23:22
 */
public class ThriftInvoker<T> implements Invoker<T> {
	
	private static Logger log = LoggerFactory.getLogger(ThriftInvoker.class);
	
	private final InitalizableObjectPool objPoolMgr;
	
	private final InvokerConfig url;
	
	public ThriftInvoker(InvokerConfig url) throws Exception{
		this.url = url;
		objPoolMgr = new InitalizableObjectPool(new ClientFactory(url),url.getPoolSize());
		objPoolMgr.setTestOnBorrow(true);
		//从池中获取连接对象，超时设置。 取值为连接超时时间的一半，不能无限等待。
		objPoolMgr.setMaxWait(url.getTimeout()/2);
		objPoolMgr.addObject();
	}
 

	public Object invoke(final Invocation invocation) throws Throwable {
		Client client = null;
		long startTime = System.currentTimeMillis();
		boolean isSuccess = true;
		try{
			client = (Client) objPoolMgr.borrowObject();
	    	return invocation.getMethod().invoke(client.thriftClinet(), invocation.getParameters());
    	}catch (InvocationTargetException e) {
    		Throwable exception = e.getTargetException();
    		isSuccess = false;
    		//远程方法调用异常，回收client，并记录异常日志
    		invalidateClient(client);
    		log.error("service :" + url.getInterfaceName() + " host "
    		        +  url.getAddress()  + " invoker error. cause:" + exception.getMessage(),e);
    		client = null;
    		if(exception instanceof TApplicationException){
    			throw new RemotingException(2,exception.getMessage(),exception); 
    		}
    		if(exception instanceof TProtocolException){
                throw new RemotingException(2,exception.getMessage(),exception); 
            }
    		if(exception instanceof TTransportException){
                throw new RemotingException(2,exception.getMessage(),exception); 
            }
    		throw exception;
        }catch (Throwable e) {
        	isSuccess = false;
        	invalidateClient(client);
        	log.error("service :" + url.getInterfaceName() + " host "
    				+  url.getAddress()  + " invoker error. cause:" + e.getMessage(),e);
        	client = null;
			throw new RemotingException(e);
		}  finally{
			//这里只打印成功调用的日志，飞虎监控
			if(client != null){
				if(log.isInfoEnabled() && isSuccess){
					long exeTime = System.currentTimeMillis() - startTime;
					log.info("invoker thrift interface information : | "
							+ client.getInterface().getName() + " "
							+ invocation.getMethod().getName() + " "
							+ url.getAddress() + " "
							+ exeTime);
				}
				try {
					objPoolMgr.returnObject(client);
				} catch (Exception e) {
					log.error("service :" + url.getInterfaceName() + " host "
							+ url.getAddress() + "return Pool error. cause:" + e.getMessage(),e);
				}
			}
			RemotingContext context = RemotingContext.getContext();
			context.setRemoteAddress(url.getHost(),url.getPort());
    	}
	}

	private void  invalidateClient(Client client){
		if(client != null){
			try {
				objPoolMgr.invalidateObject(client);
			} catch (Exception e1) {
				log.error("service :" + url.getInterfaceName() + " host "
						+ url.getHost() + url.getPort() + "invalidate Pool error. cause:" + e1.getMessage(),e1);
			}
		}
	}
	
	@Override
	public Class<T> getInterface() {
		return url.getInterface();
	}


	@Override
	public InvokerConfig getUrl() {
		return url;
	}


	@Override
	public void destroy() {
		try {
			objPoolMgr.close();
		} catch (Exception e) {
			log.error(
					"service :" + url.getInterfaceName() +"host "
							+ url.getAddress() 
							+ " destroy objPoolMgr error. cause:" + e.getMessage(), e);
		}
	}

	@Override
	public boolean isAvailable() {
		return objPoolMgr.isAlive();
	}
	
}
