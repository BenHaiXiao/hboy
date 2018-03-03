package com.github.hboy.base.thrift.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.hboy.base.thrift.protocol.NyyService;
import com.github.hboy.base.thrift.protocol.ServiceException;
import org.apache.thrift.TException;

import com.github.hboy.base.thrift.protocol.TestObject;

public class ThriftNyyServiceImpl implements NyyService.Iface {
	
	java.util.concurrent.atomic.AtomicInteger i = new AtomicInteger();;
	public String send(String appId, String sign, String data)
			throws TException {
		System.out.println("appId:" + appId);
		System.out.println("sign:" + sign);
		System.out.println("data:" + data);
		return appId;
	}
	
	@Override
	public void testVoid() throws TException {
		System.out.println("------testVoid----------");
		
	}


	@Override
	public TestObject testObj(TestObject obj) throws TException {
		System.out.println("------testObj------TestObject----:"+obj);
		return obj;
	}

	@Override
	public Set<Integer> testSet(Set<Integer> uids) throws ServiceException,
			TException {
		System.out.println("------testSet------uids----:"+uids);
		return uids;
	}

	@Override
	public List<Long> testList(List<Long> uids) throws ServiceException,
			TException {
		System.out.println("------testList------uids----:"+uids);
		return uids;
	}

	@Override
	public Map<Long, Integer> testMap(List<Long> uids, Map<Long, Integer> appid)
			throws ServiceException, TException {
		System.out.println("------testMap------uids----:"+ uids + "appid" + appid);
		return appid;
	}

	@Override
	public boolean testBool(boolean bl) throws ServiceException, TException {
		System.out.println("------testBool------bl----:"+ bl);
		return bl;
	}

	@Override
	public short testShort(short uid, short appid) throws ServiceException,
			TException {
		System.out.println("------testShort------uid----:"+ uid + "appid" + appid);
		return appid;
	}

	@Override
	public int testInt(long uid, int appid) throws ServiceException, TException {
		System.out.println("------testInt------uid----:"+ uid + "appid" + appid);
		return appid;
	}

	@Override
	public long testLong(long tuid, long fuid) throws ServiceException,
			TException {
		System.out.println("------testLong------uid----:"+ tuid + "fuid" + fuid);
		return fuid;
	
	}

	@Override
	public byte testByte(byte by) throws ServiceException, TException {
		System.out.println("------testByte------by----:"+ by);
		return by;
	}

	@Override
	public double testDouble(double db) throws ServiceException, TException {
		System.out.println("------testDouble------db----:"+ db);
		return db;
	}

	@Override
	public ByteBuffer testBinary(ByteBuffer buffer) throws ServiceException,
			TException {
		System.out.println("------testBinary------buffer----:"+ buffer);
//		throw new ServiceException("ServiceException");
		return buffer;
	}

	@Override
	public void testException(String appId) throws ServiceException, TException {
		throw new ServiceException("ServiceException");
	}

}
