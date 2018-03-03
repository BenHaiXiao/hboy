package com.github.hboy.center;

import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.base.thrift.protocol.NyyService;
import com.github.hboy.base.thrift.protocol.NyyService.Iface;
import com.github.hboy.common.config.CenterConfig;
import com.github.hboy.demo.Scribe;


public class ReferenceFactoryTest {

	
	private  NyyService.Iface face;

	
	private Scribe scribe;
	
 
	@Before
	public void init() throws TException  {
		CenterConfig centerConfig = new CenterConfig(
				"172.27.137.12:2181");
		centerConfig.setApplication("test");
//		centerConfig.setMonitorAddress("192.168.0.1:8090");
		ReferenceFactory<NyyService.Iface> thriftClient = new ReferenceFactory<Iface>();
		thriftClient.setCenterConfig(centerConfig);
		thriftClient.setInterface(Iface.class.getName());
		face = thriftClient.getClient();
//		face.send("appId", "sign", "data");
		ReferenceFactory<Scribe> client = new ReferenceFactory<Scribe>();
		client.setCenterConfig(centerConfig);
		client.setInterface(Scribe.class);
		client.setAnnotation(true);
		scribe = client.getClient();
		scribe.testVoid();
	}
	
	
	@Test
	public void test() throws Exception {
		for(int i=0; i < 1001; i++){
			try{
//				System.out.println(face.send("" + i, "", ""));
				System.out.println(scribe.testString("asdasdasdasdasdasdasdasdasdasdasdasdsadasdsadadsd"));
//				Thread.sleep(1000);
			}catch(Exception e){
				e.printStackTrace();
			}
            try {
                System.out.println(scribe.testString("1"));
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	     
	    
		for(int i=0; i < 100; i++){
			GetThread t = new GetThread(scribe);
			t.start();
		}
		synchronized (ReferenceFactoryTest.class) {
			ReferenceFactoryTest.class.wait();
		}
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	
	static class GetThread extends Thread {

        private final Scribe face;
        private Logger log = LoggerFactory.getLogger(GetThread.class);
        
        public GetThread(Scribe face) {
            this.face = face;
        }

        
        @Override
        public void run() {
        	long t = System.currentTimeMillis();
        	log.info("begin" + Thread.currentThread().getName()+ ":"  +t);
            for(;;){
            	try{
//            	    System.out.println(face.testString("1")); 
            	    face.testString("asdasdasdasdasdasdasdasdasdasdasdasdsadasdsadadsd");
            	}catch (Exception e){
            	    log.error(" - error: " , e);
            	}
            }
//            log.info("end" + Thread.currentThread().getName()+ ":"  +(System.currentTimeMillis()-t));
        }
    }
	
}
