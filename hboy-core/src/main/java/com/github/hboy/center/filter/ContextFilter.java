package com.github.hboy.center.filter;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.RemotingContext;

/**
 * @author xiaobenhai
 * Date: 2016/3/18
 * Time: 10:45
 */
public class ContextFilter implements Filter {

    public Object invoke(Invoker<?> invoker, Invocation invocation) throws Throwable {
       try{
           return invoker.invoke(invocation);
       }finally{
           RemotingContext.removeContext();
       }
            
    }
}