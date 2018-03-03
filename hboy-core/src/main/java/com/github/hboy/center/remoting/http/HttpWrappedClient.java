package com.github.hboy.center.remoting.http;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.MethodCallback;
import com.github.hboy.center.remoting.exchange.ExchangeClient;
import com.github.hboy.center.remoting.exchange.ResponseFuture;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.exchange.Request;
import com.github.hboy.common.config.InvokerConfig;

/**
 * httpClient的包装类
 * TODO:是否有机会把HttpWrappedClient和HeaderExchangeClient再抽象一层？
 * Date: 2016/5/19
 * Time: 16:31
 */
public class HttpWrappedClient implements ExchangeClient {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpWrappedClient.class);
    
    private final HttpClient client;

    private volatile boolean closed = false;

    public HttpWrappedClient(HttpClient client){
        if (client == null) {
            throw new IllegalArgumentException("http client == null");
        }
        this.client = client;
    }

    @Override
    public ResponseFuture request(Invocation inv) throws RemotingException {
        return request(inv, client.getInvokerConfig().getTimeout());
    }

    @Override
    public ResponseFuture request(Invocation inv, int timeout) throws RemotingException {
        if (closed) {
            throw new RemotingException("Failed to send Invocation " + inv + ", cause: The http client " + this + " is closed!");
        }
        Request req = new Request();
        req.setData(inv);
        
        ResponseFuture future;
        try{
            // TODO: 处理timeout
            future = client.executeRequest(req);
        }catch (RemotingException e) {
            throw e;
        }
        return future;
    }

    @Override
    public void send(Invocation inv) throws RemotingException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void send(Invocation inv, MethodCallback<Object> callback) throws RemotingException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void close() {
        try {
            closed = true;
//            client.close();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public InvokerConfig getUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

}
