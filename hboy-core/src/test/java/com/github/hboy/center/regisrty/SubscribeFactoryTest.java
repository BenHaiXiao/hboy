package com.github.hboy.center.regisrty;

import java.util.List;

import com.github.hboy.center.subscribe.Subscribe;
import com.github.hboy.center.subscribe.SubscribeFactory;
import com.github.hboy.center.subscribe.support.ZookeeperSubscribe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.hboy.center.subscribe.directory.SubscribeDirectory;
import com.github.hboy.common.config.InvokerConfig;

public class SubscribeFactoryTest {
	
	Subscribe s;
	InvokerConfig url = new InvokerConfig();
	@Before
	public void createSubscribe(){
		url.setSubscribeAddress("172.19.103.104:2181,172.19.103.102:2181,172.19.103.102:2182");
		s = SubscribeFactory.getSubscribe(SubscribeFactory.SubscribeType.ZK, url);
		Assert.assertEquals(s.getClass(),ZookeeperSubscribe.class);
    }
	
	@Test
	public void subscribe(){
		url.setInterfaceName("com.yy.cs.base.nyy.remoting.thrift.nyyService.NyyService$Iface");
		url.setApplication("test");
		SubscribeDirectory directory = new SubscribeDirectory(url); 
		List<InvokerConfig> urls = s.subscribe(url, directory);
		System.out.println(urls);
    }
	
	@Test
	public void unsubscribe(){
		url.setInterfaceName("com.yy.cs.base.nyy.remoting.thrift.nyyService.NyyService$Iface");
		url.setApplication("test");
		SubscribeDirectory directory = new SubscribeDirectory(url); 
		s.unsubscribe(url, directory);
	}
	
	@Test
	public void register()  {
		url.setInterfaceName("com.yy.cs.base.nyy.remoting.thrift.nyyService.NyyService$Iface");
		url.setApplication("test");
		url.setHost("127.0.0.1");
		url.setPort(8086);
		s.register(url);
	}
	
	
	@Test
	public void unregister()  {
		url.setInterfaceName("com.yy.cs.base.nyy.remoting.thrift.nyyService.NyyService$Iface");
		url.setApplication("test");
		url.setHost("127.0.0.1");
		url.setPort(8086);
		s.unregister(url);
	}
}
