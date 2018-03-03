package com.github.hboy.center.remoting.exchange;

import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.Channel;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 22:25
 */
public class HeaderExchangeServer implements ExchangeServer {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Channel server;

    private volatile boolean closed = false;

    public HeaderExchangeServer(Channel server) {
        if (server == null) {
            throw new IllegalArgumentException("server == null");
        }
        this.server = server;
    }
    
    public Channel getServer() {
        return server;
    }

    public boolean isClosed() {
        return closed;
    }


    public void close() {
    	if (closed) {
            return;
        }
        closed = true;
    	server.close();
    }
    

    public SocketAddress getLocalAddress() {
        return server.getLocalAddress();
    }

    public InvokerConfig getUrl() {
        return server.getUrl();
    }

    public void send(Object message) throws RemotingException {
        if (closed) {
            throw new RemotingException("Failed to send message " + message + ", cause: The server " + getLocalAddress() + " is closed!");
        }
        server.send(message);
    }

	@Override
	public boolean isConnected() {
		return server != null && server.isConnected();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return server.getRemoteAddress();
	}

    
}
