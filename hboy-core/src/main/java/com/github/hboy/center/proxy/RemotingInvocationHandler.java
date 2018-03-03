package com.github.hboy.center.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.github.hboy.center.remoting.Invoker;

/**
 *
 */
/**
 * TODO:应该改名为RemotingInvocationHandler，原来为 ThriftInvocation Handler，因为通用于各种协议
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 11:11
 */
public class RemotingInvocationHandler implements InvocationHandler {
	
	
	private final Invoker<?> invoker;
	
    public RemotingInvocationHandler(Invoker<?> invoker){
        this.invoker = invoker;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    	String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        // 下面的方法不需要也不应该调用远程的接口
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }
        return invoker.invoke(new Invocation(method,args,invoker.getInterface(),methodName));
    }

}
