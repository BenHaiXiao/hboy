package com.github.hboy.center;

import com.github.hboy.base.thrift.impl.ThriftNyyServiceImpl;
import com.github.hboy.base.thrift.protocol.NyyService;
import com.github.hboy.common.config.CenterConfig;
import com.github.hboy.common.config.ExportConfig;
import com.github.hboy.demo.SwiftScribe;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.hboy.demo.Scribe;


public class ServiceExporterTest {

	
 
	@Before
	public void init() throws TException  {
		
		CenterConfig centerConfig = new CenterConfig(
				"172.27.137.12:2181");
		centerConfig.setApplication("test");
//		centerConfig.setMonitorAddress("center-test.yy.com:80");
		ExportConfig exportConfig = new ExportConfig();
		exportConfig.setHost("127.0.0.1");
		exportConfig.setPort(8888);
		
		ServiceExporter<Scribe> exporter = new ServiceExporter<Scribe>();
		exporter.setCenterConfig(centerConfig);
		exporter.setExportConfig(exportConfig);
		exporter.setService(new SwiftScribe());
		exporter.setInterface(Scribe.class);
		exporter.setAnnotation(true);
		exporter.export();
		
		ServiceExporter<NyyService.Iface> exporter2 = new ServiceExporter<NyyService.Iface>();
		exporter2.setCenterConfig(centerConfig);
		exporter2.setExportConfig(exportConfig);
		exporter2.setService(new ThriftNyyServiceImpl());
		exporter2.setInterface(NyyService.Iface.class);
		exporter2.export();
	}
	
	
	@Test
	public void test() throws Exception {
		synchronized (ServiceExporterTest.class) {
			ServiceExporterTest.class.wait();
		}
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	
}
