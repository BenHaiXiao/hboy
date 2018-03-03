package com.github.hboy.center.thrift;

import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 21:33
 */
public class AsyncClient extends AbstractClient {
	
	public AsyncClient(InvokerConfig url) {
		super(url);
	}

	private  final String name  = "AsyncClient";
	
	protected  Class<?> createClient(Class<?> interfaces)
			throws ClassNotFoundException{
		String face = interfaces.getName();
		int f = face.lastIndexOf("$");
		if (f <= 0) {
			throw new ClassNotFoundException(face);
		}
		String clientName = face.substring(0, f + 1) + name;
		return Class.forName(clientName);
	}
	  
	
}
