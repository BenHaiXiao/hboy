package com.github.hboy.center.thrift;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 23:12
 */
public class ClientFactory extends BasePoolableObjectFactory<Object> {
	private static Logger log = LoggerFactory.getLogger(ClientFactory.class);

	private final InvokerConfig url;

	public ClientFactory(InvokerConfig url) {
		this.url = url; //config;
	}

	/**
	 * 创建Client
	 */
	public Client  makeObject() throws Exception {
		try {
		    Client client = new SyncClient(url);
			client.open();
			log.info("client created, [{}:{}]", url.getHost(), url.getPort());
	        return client;
		} catch (Exception e) {
			log.error("error creating client to:" + url.getAddress(), e);
			throw e;
		}  
	}

	 
	public void destroyObject(Object obj) throws Exception {
		super.destroyObject(obj);
		Client client = (Client) obj;
		client.close();
		log.info("client closed, [{}]", url.getAddress());
	}
	
	public boolean validateObject(Object obj) {
		Client client = (Client) obj;
		log.debug("validate client , [{}] isClose：{}", url.getAddress(), client.isClose());
		return !client.isClose();
	}

	public void activateObject(Object obj) throws Exception {
		Client client = (Client) obj;
		if(client.isClose()){
			client.open();
		}
	}

	public void passivateObject(Object obj) throws Exception {
	}

	public enum ClientType{
		SYNC,ASYNC;
	}
}
