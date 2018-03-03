package com.github.hboy.center.subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.hboy.center.remoting.ChannelEventHandler;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.center.remoting.InvokerAgent;
import com.github.hboy.center.subscribe.directory.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.protocol.ProtocolWrapper;
import com.github.hboy.center.subscribe.directory.SubscribeDirectory;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 20:33
 */
public class InvokerFactory {
	
	private final static  InvokerFactory  invokerFactory = new InvokerFactory();
	
	private final static java.util.concurrent.ConcurrentMap<InvokerConfig, InvokerAgent<?>> invokerAgents = new ConcurrentHashMap<InvokerConfig, InvokerAgent<?>>();
	
	private static Logger log = LoggerFactory.getLogger(InvokerFactory.class);
	
	public static InvokerFactory getInvokerFacoty(){
		return invokerFactory;
	}
	
	private InvokerFactory(){}
	
	
	public <T> Invoker<T> buildInvoker(InvokerConfig url, List<ChannelEventHandler> handlers){
		//1初始化zk,返回连接列表 ,放到目录对象里
		Subscribe sub = SubscribeFactory.getSubscribe(SubscribeFactory.SubscribeType.ZK, url);
		
		SubscribeDirectory<T> directory = new SubscribeDirectory<T>(url,sub,handlers);
		List<InvokerConfig> urls = sub.subscribe(url,directory);
		//初始化invker
		Invoker<T> invoker = getInvokerAgent(url,urls,directory);
		//3返回agentInvoke对象
		return invoker;
	}
	
	public  <T> Invoker<T>  buildInvoker(InvokerConfig url,List<InvokerConfig> urls,List<ChannelEventHandler> handlers){
		//1初始化目录列表
		SubscribeDirectory<T> directory = new SubscribeDirectory<T>(url,handlers);
		//初始化invker
		Invoker<T> invoker = getInvokerAgent(url,urls,directory);
		directory.notify(urls);
		//3返回agentInvoke对象
		return invoker;
	}
	
	@SuppressWarnings("unchecked")
	private <T> Invoker<T>  getInvokerAgent(InvokerConfig url,List<InvokerConfig> urls,Directory<T> directory){
		
        InvokerAgent<T> agent = (InvokerAgent<T>) invokerAgents.get(url);
		if(agent == null){
		    invokerAgents.putIfAbsent(url, new InvokerAgent<T>(url,directory));
			agent = (InvokerAgent<T>) invokerAgents.get(url);
		}
		return agent;
	}
	
	public  void destroy(InvokerConfig url){
		Invoker<?> agentInvoker = invokerAgents.remove(url);
		if(agentInvoker != null){
			agentInvoker.destroy();	
		}
	}
	
	
	public <T> void localExport(T service, Class<T> type,InvokerConfig url,List<ChannelEventHandler> handlers ){
		//export
		try {
		    ProtocolWrapper.getProtocol().export(service, type, url,handlers);
		} catch (RemotingException e) {
			throw new IllegalStateException(e.getMessage(),e);
		}
	}
	
	
	public <T> void export(T service, Class<T> type,InvokerConfig url,List<ChannelEventHandler> handlers){
	    //export
	    this.localExport(service, type, url,handlers);
	    //1初始化zk,向注册url
		Subscribe sub = SubscribeFactory.getSubscribe(SubscribeFactory.SubscribeType.ZK, url);
		sub.register(url);
	}
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if (log.isInfoEnabled()) {
					log.info("Run shutdown hook now.");
				}
				SubscribeFactory.destroy();
				for(InvokerConfig key : new ArrayList<InvokerConfig>(invokerAgents.keySet())){
					InvokerAgent<?> invoker = invokerAgents.remove(key);
					invoker.destroy();
				}
				ProtocolWrapper.getProtocol().destroy();
			}
		}, "ShutdownHook"));
	}
	
	
}
