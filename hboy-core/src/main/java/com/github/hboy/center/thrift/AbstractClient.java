package com.github.hboy.center.thrift;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.hboy.center.thrift.protocol.ThriftProtocolFactory;
import com.github.hboy.center.thrift.transport.TransportFactory;
import com.github.hboy.common.config.InvokerConfig;
import com.github.hboy.common.util.Constants;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 22:38
 */
public abstract class AbstractClient  implements Client{
	
	private final InvokerConfig url;
	
	private final Class<?>  interfaces;
	
	private  TTransport transport;
	
	private  TProtocol protocol;
	
	private  Constructor<?> ctor;
	
	private  Class<?>  clinet;
	
	private  Object thriftClinet;
	
	private final Constants.TransportType ttype;
	
	private final Constants.ThriftProtocolType ptype;
	
	//     <interfaces, Clinet>
	private static final ConcurrentMap<Class<?>, Class<?>> concurrentMap = new ConcurrentHashMap<Class<?>, Class<?>>();
	
	public AbstractClient(InvokerConfig url){
		 this.url = url; 
		 this.interfaces = url.getInterface(); 
		 this.ttype = url.getThriftTransport();
		 this.ptype = url.getThriftProtocol();
	}
	
	
	public  boolean isClose(){
		if(transport != null){
			return !transport.isOpen();
		}
		return true;
	}
	
	public  void close(){
		if(transport != null){
			transport.close();
		}
		thriftClinet = null;
		protocol = null;
		transport = null;
		ctor = null;
		clinet = null;
	}
	
	public  Object thriftClinet(){
		return thriftClinet;
	}
	
	public void open() throws Exception {
		this.clinet = getClientClass(interfaces);
		this.ctor = clinet.getConstructor(TProtocol.class);
		transport = TransportFactory.getTTransport(url, ttype);
		protocol = ThriftProtocolFactory.getTProtocol(transport, ptype);// new TBinaryProtocol(transport);
		transport.open();
		thriftClinet = ctor.newInstance(protocol);
	}
	
	
	private Class<?> getClientClass(Class<?> interfaces) throws ClassNotFoundException{
		Class<?> clientClass = concurrentMap.get(interfaces);
		if(clientClass == null ){
			concurrentMap.putIfAbsent(interfaces, createClient(interfaces));
			clientClass = concurrentMap.get(interfaces);
		}
		return clientClass;
	}
	
	protected abstract Class<?> createClient(Class<?> interfaces)
			throws ClassNotFoundException ;

	@Override
	public Class<?> getInterface() {
		return this.interfaces;
	}
	
	@Override
	public InvokerConfig  getURL(){
		return this.url;
	}
}
