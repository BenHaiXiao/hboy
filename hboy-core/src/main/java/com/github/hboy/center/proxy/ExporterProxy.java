package com.github.hboy.center.proxy;

import java.lang.reflect.Method;

import com.github.hboy.center.remoting.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.filter.FilterWrapper;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 9:56
 */
public class ExporterProxy<T> implements Exporter<T>{
	
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private volatile boolean unexported = false;
	
    private final Invoker<T> invoker;

	@Override
    public void unexport() {
		if (unexported) {
			return;
		}
		unexported = true;
		invoker.destroy();
    }
	
	public ExporterProxy(final Method method,final Object service,InvokerConfig url){
		if (method == null)
			throw new IllegalStateException("method == null");
		if (service == null)
			throw new IllegalStateException("service == null");
		this.invoker = getInvoker(method,service,url);
	}
	
	public Invoker<T> getInvoker(final Method method, final Object service , InvokerConfig url) {
	    return FilterWrapper.buildFilterChain(new AbstractProxyInvoker<T>(method, service, url) {
            
           protected Object doInvoke(Method method, Object service, 
                                     Class<?>[] parameterTypes, 
                                     Object[] arguments) throws Throwable {
               return method.invoke(service, arguments);
           }
       }, url.getFilters());
    }
	
	
	@Override
	public Invoker<T> getInvoker() {
		return invoker;
	}
	  
	public String toString() {
		return getInvoker().toString();
	}
	
}
