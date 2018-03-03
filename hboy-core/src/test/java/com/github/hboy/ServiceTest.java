package com.github.hboy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.hboy.base.thrift.protocol.NyyService;
import com.github.hboy.base.thrift.protocol.ServiceException;
import com.github.hboy.center.ReferenceFactory;
import com.github.hboy.center.ReferenceFactoryTest;
import com.github.hboy.common.config.LocalConfig;
import com.github.hboy.common.util.Constants;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.base.thrift.protocol.TestObject;
import com.github.hboy.common.config.CenterConfig;

public class ServiceTest {

	
	private NyyService.Iface face;

	private ReferenceFactory<NyyService.Iface> thriftClient;
	
	private Logger log = LoggerFactory.getLogger(ReferenceFactoryTest.class);
 
	
	@Before
	public void before() throws Exception {
		LocalConfig localConfig2 = new LocalConfig();
		localConfig2.setHost("localhost");
		localConfig2.setPort(8186);
		localConfig2.setMaxActive(1);
		localConfig2.setTimeout(1000);
		
		CenterConfig centerConfig = new CenterConfig();
		centerConfig.setAddress("172.27.137.12:2181");
		centerConfig.setApplication("test");
		
		List<LocalConfig> l = new ArrayList<LocalConfig>();
		l.add(localConfig2);
		
		thriftClient = new ReferenceFactory<NyyService.Iface>();
	//	thriftClient.setLocalConfigs(l);
		thriftClient.setCenterConfig(centerConfig);
		//thriftClient.setInterface(NyyService.Iface.class);
		thriftClient.setInterface("com.github.hboy.base.nyy.remoting.thrift.nyyService.NyyService$Iface");
		thriftClient.setGroup("*");
		thriftClient.setLoadBalance(Constants.LoadBalanceType.ROUND);
		thriftClient.setFault(Constants.FaultType.FAILOVER);
		
		face = thriftClient.getClient();
	}
	
	
	@After
	public void tearDown() throws Exception {
		thriftClient.destroy();
	}

 

	@Test
	public void send()
			throws TException {
		String rst = face.send("appid", "sina", "data");
		System.out.println(rst);
		Assert.assertNotNull(rst);
	}


	@Test
	public void testVoid() throws TException {
		face.testVoid();
	}


//	@Test
	public void testObj() throws TException {
		TestObject obj = new TestObject();
		obj.setTbl(false);
		obj.setTShort(Short.MAX_VALUE);
		obj.setTint(Integer.MAX_VALUE);
		obj.setTlong(Long.MAX_VALUE);
		obj.setTbyte(Byte.MAX_VALUE);
		byte[] tbinary = new byte[]{Byte.MAX_VALUE,Byte.MIN_VALUE,41};
		obj.setTbinary(tbinary);
		obj.setTdouble(Double.MAX_VALUE);
		obj.setTstring("tstring");
		TestObject o = face.testObj(obj);
		Assert.assertEquals(obj, o);
	}


//	@Test
	public void testSet() throws ServiceException,
			TException {
		Set<Integer> set = new HashSet<Integer>();
		set.add(Integer.MAX_VALUE);
		set.add(Integer.MIN_VALUE);
		set.add(Integer.SIZE);
		Set<Integer> t = face.testSet(set);
		Assert.assertEquals(t, set);
	}


//	@Test
	public void testList() throws ServiceException,
			TException {
		List<Long> uids = new ArrayList<Long>();
		uids.add(Long.MAX_VALUE);
		uids.add(Long.MIN_VALUE);
		List<Long> l = face.testList(uids);
		Assert.assertEquals(l, uids);
	}


//	@Test
	public void testMap()
			throws ServiceException, TException {
		List<Long> uids = new ArrayList<Long>();
		uids.add(Long.MAX_VALUE);
		uids.add(Long.MIN_VALUE);
		Map<Long, Integer> appid = new HashMap<Long, Integer>();
		appid.put(Long.MAX_VALUE, Integer.MAX_VALUE);
		appid.put(Long.MIN_VALUE, Integer.MIN_VALUE);
		Map<Long, Integer>  m = face.testMap(uids, appid);
		Assert.assertEquals(m, appid);
	}


//	@Test
	public void testBool() throws ServiceException, TException {
		Assert.assertFalse(face.testBool(false));
	}


	@Test
	public void testShort() throws ServiceException,
			TException {
		short uid = Short.MAX_VALUE;
		short appid = Short.MIN_VALUE;
		short s = face.testShort(uid,appid);
		Assert.assertEquals(s, appid);
	}


//	@Test
	public void testInt() throws ServiceException, TException {
		long uid = Long.MAX_VALUE;
		int appid = Integer.MAX_VALUE;
		int i = face.testInt(uid, appid);
		Assert.assertEquals(i, appid);
	}


//	@Test
	public void testLong() throws ServiceException,
			TException {
		long tuid = Long.MAX_VALUE;;
		long fuid = Long.MIN_VALUE;;
		long l = face.testLong(tuid,fuid);
		Assert.assertEquals(l, fuid);
	}


//	@Test
	public void testByte() throws ServiceException, TException {
		byte by = Byte.MAX_VALUE;
		byte b = face.testByte(by);
		Assert.assertEquals(b, by);
	}


//	@Test
	public void testDouble() throws ServiceException, TException {
		double db = Double.MAX_VALUE;
		double d = face.testDouble(db);
		Assert.assertEquals(d, db,db);
	}


//	@Test
	public void testBinary() throws ServiceException,
			TException {
		byte[] tbinary = new byte[]{Byte.MAX_VALUE,Byte.MIN_VALUE,12};
		ByteBuffer buffer = ByteBuffer.wrap(tbinary);
		ByteBuffer b = face.testBinary(buffer);
		Assert.assertEquals(b,buffer);
	}


	@Test
	public void testException() throws TException {
		String appId = null;
		try {
			face.testException(appId);
			//Assert.fail();
		} catch (ServiceException e) {
			 System.out.println("ServiceException");
		}
	}
	
	 
}
