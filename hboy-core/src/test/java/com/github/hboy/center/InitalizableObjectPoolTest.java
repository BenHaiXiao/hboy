package com.github.hboy.center;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.hboy.center.pooling.InitalizableObjectPool;
import com.github.hboy.center.thrift.ClientFactory;
import com.github.hboy.common.config.InvokerConfig;

public class InitalizableObjectPoolTest {

	
	private final InvokerConfig url =  new InvokerConfig();
	
	private  InitalizableObjectPool objPoolMgr;
	{
		
	}
	
	@Before
	public void init() throws Exception  {
		url.setHost("127.0.0.1");
		url.setPort(8185);
		url.setPoolSize(10);
		url.setInterfaceName("com.yy.cs.base.nyy.remoting.thrift.nyyService.NyyService$Iface");
		objPoolMgr = new InitalizableObjectPool(new ClientFactory(url),url.getPoolSize());
		//从池中获取连接对象，超时设置。 取值为连接超时时间的一半，不能无限等待。
		objPoolMgr.setMaxWait(url.getTimeout()/2);
		objPoolMgr.addObject();
	}
	
//	thriftClient.setTransport(TransportType.TFRAMED);
//	@Before
//	public void before() throws Exception {
//		LocalConfig localConfig = new LocalConfig();
//		localConfig.setHost("127.0.0.1");
//		localConfig.setPort(8186);
//		LocalConfig localConfig2 = new LocalConfig();
//		localConfig2.setHost("127.0.0.1");
//		localConfig2.setPort(8185);
//		
//		List<LocalConfig> l = new ArrayList<LocalConfig>();
//		l.add(localConfig);
//		l.add(localConfig2);
//		thriftClient = new ReferenceFactory<Iface>();
//		thriftClient.setLocalConfigs(l);
//		thriftClient.setInterface(Iface.class.getName());
//		thriftClient.setTransport(TransportType.TFRAMED);
//		thriftClient.setLoadBalance(LoadBalanceType.ROUND);
//		thriftClient.setFault(FaultType.FAILOVER);
//		face = thriftClient.getClient();
//	}
	
	@Test
	public void test() throws Exception {
		
	}
//	com.yy.cs.center.web.service.CenterService
	
	@After
	public void tearDown() throws Exception {
	}
	
	
	
}
