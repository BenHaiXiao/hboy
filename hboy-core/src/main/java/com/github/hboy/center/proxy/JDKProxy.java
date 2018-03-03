package com.github.hboy.center.proxy;


import java.lang.reflect.InvocationHandler;

import com.github.hboy.center.remoting.Invoker;

/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 10:33
 */
public class JDKProxy{

	@SuppressWarnings("unchecked")
	public static <T> T getInvokerProxy(Invoker<T> invoker, Class<?> interfaces) {
		return (T) java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread()
				.getContextClassLoader(), new Class<?>[]{interfaces},
				new RemotingInvocationHandler(invoker));
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInvokerProxy(InvocationHandler handler, Class<?> interfaces) {
		return (T) java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread()
				.getContextClassLoader(), new Class<?>[]{interfaces},
				handler);
	}

}
