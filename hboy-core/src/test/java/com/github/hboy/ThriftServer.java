package com.github.hboy;

import java.net.InetSocketAddress;

import com.github.hboy.base.thrift.impl.ThriftNyyServiceImpl;
import com.github.hboy.base.thrift.protocol.NyyService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TNonblockingServer.Args;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

public class ThriftServer {
	
	
	
	public static void main(String[] args) {
		ThriftServer b = new ThriftServer();
		b.startServer();
		//b.startServer2();
//		byte[] b = new byte[1024];
// 		System.out.println(new String(b).length());
	    
	}

	public void startServer() {
		try {
			InetSocketAddress address = new InetSocketAddress("127.0.0.1",8190);
			TNonblockingServerSocket serverTransport = new  TNonblockingServerSocket(address);

			NyyService.Processor process = new NyyService.Processor(
					new ThriftNyyServiceImpl());
			Factory portFactory = new TBinaryProtocol.Factory();

			Args args = new Args(serverTransport);
			args.processor(process);
			args.protocolFactory(portFactory);
			args.transportFactory(new TFramedTransport.Factory());

			//use TNonblockingServer, Clients must also use TFramedTransport
			TServer server = new TNonblockingServer(args);
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}

	public void startServer2() {
		try {
			InetSocketAddress address = new InetSocketAddress("127.0.0.1",8185);
			TNonblockingServerSocket serverTransport = new  TNonblockingServerSocket(address);

			NyyService.Processor process = new NyyService.Processor(
					new ThriftNyyServiceImpl());
			Factory portFactory = new TBinaryProtocol.Factory();

			Args args = new Args(serverTransport);
			args.processor(process);
			args.protocolFactory(portFactory);
			args.transportFactory(new TFramedTransport.Factory());

			//use TNonblockingServer, Clients must also use TFramedTransport
			TServer server = new TNonblockingServer(args);
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}
	
}
