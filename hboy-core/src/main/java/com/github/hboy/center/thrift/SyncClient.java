package com.github.hboy.center.thrift;

import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 22:28
 */
public class SyncClient extends AbstractClient{

	private  final String name  = "Client";
	
	public SyncClient(InvokerConfig url){
		 super(url);
	}
	
	protected Class<?> createClient(Class<?> interfaces)
			throws ClassNotFoundException {
		String face = interfaces.getName();
		int f = face.lastIndexOf("$");
		if (f <= 0) {
			throw new ClassNotFoundException(face);
		}
		String clientName = face.substring(0, f + 1) + name;
		return Class.forName(clientName);
	}

	 
}
