package com.github.hboy.center.remoting.exchange;

import java.net.SocketAddress;

import com.github.hboy.center.remoting.RemotingContext;
import com.github.hboy.center.remoting.netty.AbstractClient;
import com.github.hboy.center.remoting.netty.NettyChannel;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.MethodCallback;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 17:25
 */
public class HeaderExchangeClient implements ExchangeClient {

    private static final org.slf4j.Logger logger      = org.slf4j.LoggerFactory.getLogger(HeaderExchangeClient.class);

    private final AbstractClient client;

    private volatile boolean    closed      = false;

    public HeaderExchangeClient(AbstractClient client){
        if (client == null) {
            throw new IllegalArgumentException("client == null");
        }
        this.client = client;
    }

    public ResponseFuture request(Invocation inv) throws RemotingException {
        return request(inv, client.getUrl().getTimeout());
    }

    public ResponseFuture request(Invocation inv, int timeout) throws RemotingException {
        if (closed) {
            throw new RemotingException(this.getLocalAddress().toString() + " Failed to send Invocation " + inv + ", cause: The channel " + this + " is closed!");
        }
        Request req = new Request();
        req.setData(inv);
        DefaultFuture future = new DefaultFuture(client, req, timeout);
        try{
            client.send(req);
        }catch (RemotingException e) {
            future.cancel();
            throw e;
        }finally{
            RemotingContext context = RemotingContext.getContext();
            context.setLocalAddress(client.getLocalAddress());
            context.setRemoteAddress(client.getRemoteAddress());
        }
        return future;
    }
    
    @Override
    public void send(Invocation inv) throws RemotingException {
        if (closed) {
            throw new RemotingException(this.getLocalAddress().toString() + " Failed to send Invocation " + inv + ", cause: The channel " + this + " is closed!");
        }
        Request req = new Request(0);
        req.setData(inv);
        try{
            client.send(req);
        }catch (RemotingException e) {
            throw e;
        }finally{
            RemotingContext context = RemotingContext.getContext();
            context.setLocalAddress(client.getLocalAddress());
            context.setRemoteAddress(client.getRemoteAddress());
        }
    }
    
    
    @Override
    public void send(Invocation inv,MethodCallback<Object> callback) throws RemotingException {
        if (closed) {
            throw new RemotingException(this.getLocalAddress().toString() + " Failed to send Invocation " + inv + ", cause: The channel " + this + " is closed!");
        }
        new CallbackFuture(NettyChannel.getChannel(client.getChannel()), inv.getPath(), callback,client.getUrl().getTimeout());
        this.send(inv);
    }
    
    
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public void close() {
        try {
        	closed = true;
            client.close();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private SocketAddress getLocalAddress() {
        return client.getLocalAddress();
    }
    
    @Override
    public InvokerConfig getUrl() {
        return client.getUrl();
    }
    
    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HeaderExchangeClient other = (HeaderExchangeClient) obj;
        if (client == null) {
            if (other.client != null) return false;
        } else if (!client.equals(other.client)) return false;
        return true;
    }

    @Override
    public String toString() {
        return client.toString();
    }


}