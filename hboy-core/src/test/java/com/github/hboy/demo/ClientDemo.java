package com.github.hboy.demo;

import java.util.ArrayList;
import java.util.List;

import com.github.hboy.base.thrift.protocol.ServiceException;
import com.github.hboy.center.ReferenceFactory;
import com.github.hboy.common.util.Constants;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.ReferenceFactoryTest;
import com.github.hboy.common.config.CenterConfig;
import com.github.hboy.common.config.LocalConfig;

public class ClientDemo {

	
	private  Scribe face;

	private ReferenceFactory<Scribe> thriftClient;
	
	private Logger log = LoggerFactory.getLogger(ReferenceFactoryTest.class);
 
	
	@Before
	public void before() throws Exception {
		LocalConfig localConfig2 = new LocalConfig();
		localConfig2.setHost("127.0.0.1");
		localConfig2.setPort(8186);
		localConfig2.setMaxActive(1);
		localConfig2.setTimeout(100000);
		
		CenterConfig centerConfig = new CenterConfig();
		centerConfig.setAddress("172.27.137.12:2181");
		centerConfig.setApplication("dubbo");
		
		List<LocalConfig> l = new ArrayList<LocalConfig>();
		l.add(localConfig2);
		
		thriftClient = new ReferenceFactory<Scribe>();
//		thriftClient.setLocalConfigs(l);
		thriftClient.setCenterConfig(centerConfig);
		thriftClient.setInterface(Scribe.class);
		thriftClient.setLoadBalance(Constants.LoadBalanceType.ROUND);
		thriftClient.setFault(Constants.FaultType.FAILFASE);
		thriftClient.setAnnotation(true);
		face = thriftClient.getClient();
	}
	
	
	@After
	public void tearDown() throws Exception {
		thriftClient.destroy();
	}

 

	@Test
	public void send()
			throws TException, ServiceException {
		ResultCode rst = face.log(null);
		System.out.println(rst);
		Assert.assertNotNull(rst);
	}


	 
}
