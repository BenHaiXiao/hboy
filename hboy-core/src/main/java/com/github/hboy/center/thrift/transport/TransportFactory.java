package com.github.hboy.center.thrift.transport;

import java.io.IOException;

import com.github.hboy.common.util.Constants;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 21:48
 */
public class TransportFactory {
	
	private TransportFactory(){}
	
	public static TTransport getTTransport(final InvokerConfig url,Constants.TransportType type) throws IOException{
		
		TTransport transport = null;
		TSocket tsocket = new TSocket(url.getHost(), url.getPort());
		tsocket.getSocket().setKeepAlive(true);
		if(url.getTimeout() > 0){
			tsocket.setTimeout(url.getTimeout());
		}
		if(type == Constants.TransportType.TSOCKET){
			transport = tsocket;
		}else if(type == Constants.TransportType.TFRAMED){
			transport = new TFramedTransport(tsocket);
		}else{
			transport = new TFramedTransport(tsocket);
		}
		return transport;
	}

}


