package com.github.hboy.center.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 9:44
 */
public class Invocation implements Serializable {

    private static final long serialVersionUID = -4355285085441097045L;
    
    private final Class<?> serviceType;
    
    private final Method method;
    
    private final Object[] args;
    
    private final String path;
    
    /**
     * 在annotation的Thrift中，methodName与method.getName()可能并不一样，所以这里需要分开设置。 
     */
	public Invocation(Method method, Object[] args,Class<?> serviceType,String path) {
		super();
		this.method = method;
		this.args = args;
		this.path = path;
		this.serviceType = serviceType;
	}


	public Method getMethod() {
		return method;
	}

	
	public Object[] getParameters() {
		return args;
	}
	
	public String getPath() {
        return path;
    }

    public Class<?>[] getParameterTypes(){
		return this.method.getParameterTypes();
	}
	
	
	public Class<?> getServiceType(){
		return this.serviceType;
	}


	@Override
	public String toString() {
		return "Invocation [serviceType=" + serviceType + ", method=" + method
				+ ", args=" + Arrays.toString(args) + ", path="
				+ path + "]";
	}
 

	
}