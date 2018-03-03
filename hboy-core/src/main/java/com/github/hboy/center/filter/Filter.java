package com.github.hboy.center.filter;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;


/**
 * @author xiaobenhai
 * Date: 2016/3/18
 * Time: 15:39
 */
public interface Filter {

	/**
     * 
	 * @param invoker service
	 * @param invocation invocation.
	 * @return invoke result.
	 * @throws Throwable
	 */
    Object invoke(Invoker<?> invoker, Invocation invocation) throws Throwable;

}