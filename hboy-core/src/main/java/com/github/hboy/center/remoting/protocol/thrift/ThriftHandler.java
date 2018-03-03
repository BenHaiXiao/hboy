package com.github.hboy.center.remoting.protocol.thrift;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.github.hboy.center.proxy.Exporter;
import com.github.hboy.center.remoting.Channel;
import com.github.hboy.center.remoting.RemotingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.exchange.DefaultFuture;
import com.github.hboy.center.remoting.exchange.Request;
import com.github.hboy.center.remoting.exchange.Response;


/**
 * 真正执行thrift协议的handler
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 23:30
 */
public class ThriftHandler implements ChannelEventHandler {
	
	Logger logger = LoggerFactory.getLogger(ThriftHandler.class);
	//methodName ---->  Exporter
	protected final ConcurrentMap<String, Exporter<?>> exporterMap = new ConcurrentHashMap<String, Exporter<?>>();
	
	@Override
	public void connected(Channel channel) throws RemotingException {
		if (logger.isInfoEnabled()) {
			logger.info("connected from " + channel.getRemoteAddress()
					+ ",url:" + channel.getUrl());
		}
	}

	@Override
	public void disconnected(Channel channel) throws RemotingException {
		if (logger.isInfoEnabled()) {
			logger.info("disconected " + channel.getLocalAddress() + " from " + channel.getRemoteAddress()
					+ ",url:" + channel.getUrl());
		}
	}

	@Override
	public void sent(Channel channel, Object message) throws RemotingException {
//		if (logger.isDebugEnabled()) {
//			logger.debug("sent message " + channel.getLocalAddress() + " -> "+  channel.getRemoteAddress()
//					+ ",message:" + message);
//		}
	}

	@Override
	public void received(Channel channel, Object message)
			throws RemotingException {
		if (message instanceof Request) {
			Request req = (Request) message;
			Invocation invocation = req.getData();
			int id = req.getId();
			Response response = new Response();
			response.setId(id);
			Object object = null;
			response.setServiceName(invocation.getServiceType().getName());
			response.setMethodName(invocation.getPath());
			try {
				object = reply(channel,invocation);
				response.setResult(object);
			} catch (InvocationTargetException e) {
				response.setError(e.getTargetException());
				response.setStatus(Response.BAD_ERROR);
	        } catch (Throwable e) {
	        	throw new RemotingException("Failed to invoke remote proxy method " + invocation.getPath() + " to " + channel + ", cause: " + e.getMessage(), e);
			}
			channel.send(response);
		} else if (message instanceof Response) {
			DefaultFuture.received((Response)message);
		} else{
			throw new RemotingException("ThriftHandler received only support encode" + Request.class.getName() + " or " + Response.class.getName());
		}

	}
	 

	@Override
	public void caught(Channel channel, Throwable exception)
			throws RemotingException {
			logger.warn("caught : " + channel.getRemoteAddress(),exception);
	}
	
	public Object reply(Channel channel,Invocation invocation ) throws Throwable  {
		Exporter<?> exporter = exporterMap.get(invocation.getPath());
		
        RemotingContext.getContext().setLocalAddress(channel.getLocalAddress())
                .setRemoteAddress(channel.getRemoteAddress());
        
        return exporter.getInvoker().invoke(invocation);
    }
}
