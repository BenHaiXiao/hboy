package com.github.hboy.center.thrift;

import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 23:48
 */
public interface Client {

	public void open() throws Exception;

	public  Object thriftClinet();
	
	public  void close();
	
	public  boolean isClose();
	
	public Class<?>  getInterface();
	
	public InvokerConfig  getURL();

}