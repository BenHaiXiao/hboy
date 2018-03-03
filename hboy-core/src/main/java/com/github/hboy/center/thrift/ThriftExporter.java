package com.github.hboy.center.thrift;


import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TNonblockingServer.Args;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.thrift.protocol.ThriftProtocolFactory;
import com.github.hboy.common.config.InvokerConfig;


/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 21:29
 */
public class ThriftExporter<T> {
	
	private static Logger log = LoggerFactory.getLogger(ThriftExporter.class);
	
	private final InvokerConfig url;
	
	private final T service;
	
	private final Class<?> type;
	
	private final String processor = "Processor";
	
	private TServer server;
	
	public ThriftExporter(T service, Class<?> type,InvokerConfig url){
		this.url = url;
		this.service = service;
		this.type = type;
	}

	public void export()  {
		try{
			InetSocketAddress address = new InetSocketAddress(url.getHost(),url.getPort());
			TNonblockingServerSocket serverTransport = new  TNonblockingServerSocket(address);
			TProcessor processor = createProcessor(type,service);
			TProtocolFactory portFactory = ThriftProtocolFactory.getTProtocolFactory(url.getThriftProtocol());
			Args args = new Args(serverTransport);
			args.processor(processor);
			args.protocolFactory(portFactory);
			args.transportFactory(new TFramedTransport.Factory());
			server = new TNonblockingServer(args);
			if(log.isInfoEnabled()){
				log.info("export " + portFactory.getClass().getSimpleName() + " server , url:" + url);
			}
			server.serve();
    	} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(),e);
		}  
	}

	
	private  TProcessor createProcessor(Class<?> interfaces,T service)
			throws RemotingException{
		try {
			String face = interfaces.getName();
			int f = face.lastIndexOf("$");
			if (f <= 0) {
				throw new ClassNotFoundException(face);
			}
			String calssName = face.substring(0, f + 1) + processor;
			Class<?>  pClass = Class.forName(calssName);
			Constructor<?> constructor = pClass.getConstructor(interfaces);
			TProcessor processor = (TProcessor) constructor.newInstance(service);
			return processor;
		} catch (Exception e) {
			throw new RemotingException(e);
		}		
	}
	
	
	public void destroy() {
		 if(server != null){
			 server.stop();
		 }
	} 
	 
}
