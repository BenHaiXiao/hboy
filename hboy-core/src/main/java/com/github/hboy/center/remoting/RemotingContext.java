package com.github.hboy.center.remoting;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 10:36
 */
public class RemotingContext {
	
	private static final ThreadLocal<RemotingContext> LOCAL = new ThreadLocal<RemotingContext>() {
		@Override
		protected RemotingContext initialValue() {
			return new RemotingContext();
		}
	};

	public static RemotingContext getContext() {
	    return LOCAL.get();
	}
	 
	public static void removeContext() {
	    LOCAL.remove();
	}

	private SocketAddress localAddress;

	private SocketAddress remoteAddress;

	protected RemotingContext() {
	}

	public RemotingContext setLocalAddress(SocketAddress address) {
	    this.localAddress = address;
	    return this;
	}

	public RemotingContext setLocalAddress(String host,int port) {
        this.localAddress = InetSocketAddress.createUnresolved(host, port);;
        return this;
    }
	
	public SocketAddress getLocalAddress() {
		return localAddress;
	}
	
    public RemotingContext setRemoteAddress(SocketAddress address) {
        this.remoteAddress = address;
        return this;
    }
    
    public RemotingContext setRemoteAddress(String host,int port) {
        this.remoteAddress = InetSocketAddress.createUnresolved(host, port);
        return this;
    }

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
}