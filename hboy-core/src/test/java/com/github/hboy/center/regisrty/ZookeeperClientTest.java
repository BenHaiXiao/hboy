package com.github.hboy.center.regisrty;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.github.hboy.center.subscribe.support.ChildListener;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hboy.center.subscribe.support.ZookeeperClient;
import com.github.hboy.common.config.InvokerConfig;

public class ZookeeperClientTest {
	
	ZookeeperClient client;
	InvokerConfig url = new InvokerConfig();
	ObjectMapper mapper = new ObjectMapper();
//	183.60.218.202:2181,58.215.180.233:2181,58.215.180.234:2181
	//172.19.103.104:2181,172.19.103.102:2181,172.19.103.102:2182
	@Before
	public void before(){
		url.setSubscribeAddress("127.0.0.1:2181");
		client =  new ZookeeperClient(url);
		client.connect();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setSerializationInclusion(Include.NON_NULL); 
    }
	@Test
	public  void testTojson() throws JsonParseException, JsonMappingException, IOException{
	    String jsonStr1 = "{\"accessable322\":\"" + false + "\",\"poolSize\":" + 3 + "}";
	    InvokerConfig c  = mapper.readValue(jsonStr1, InvokerConfig.class);
	    System.out.println(c);
	}
	
	@Test
    public void getAllChildren() throws JsonProcessingException, UnsupportedEncodingException{
	    List<String> paths = client.getChildren("/turnover");
	    for(String p : paths){
	        System.out.println(p);   
	        List<String> subpath = client.getChildren("/turnover/" + p);
	        for(String s : subpath){
	            System.out.println(s);    
	            List<String> ssp = client.getChildren("/turnover/" + p + "/" +s);
	            for(String ss : ssp){
	                System.out.println("aaaaaaaa "+ss);    
	                byte[] b =  client.readData("/turnover/" + p + "/" +s + "/"+ss);
	                if(b != null){
	                    System.out.println(new String(b,"UTF-8"));
	                }
	            }
	        }
	    }
        
    }
	
	
	@Test
	public void create() throws JsonProcessingException{
		InvokerConfig url = new InvokerConfig();
		url.setPort(8186);
		url.setHost("127.0.0.1");
		mapper.setSerializationInclusion(Include.NON_NULL); 
		System.out.println(mapper.writeValueAsString(url));
		client.create("/com.yy.cs.base.nyy.remoting.thrift.nyyService.NyyService$Iface/provider/" + mapper.writeValueAsString(url), false);
		
    }
	
	@Test
	public void getChildren() throws JsonParseException, JsonMappingException, IOException{
//		System.out.println(client.getChildren(Constants.PATH_SPLIT));
//		System.out.println(client.getClient().readData("/zookeeper/quota"));
		List<String> s = client.getChildren("/AppUserRelManage/App2User/live_center_data");
//		System.out.println(s);
		int i = 0;
		for(String t : s){
			System.out.println(t);
			System.out.println(++i);
		}
    }
	
	
	@Test
	public void delete() throws JsonParseException, JsonMappingException, IOException{
		client.delete("/AppUserRelManage/App2User/live_center_data/null");
	}
	
	
	@Test
	public void addChildListener() throws JsonProcessingException{
		List<String> s = client.addChildListener("/com.yy.cs.test/provider", new ChildListener() {
			@Override
			public void childChanged(String path, List<String> children) {
				System.out.println("--------------addChildListener-----------------");
				System.out.println("----path-----" + path);
				System.out.println("----children-----" + children);
			}
		});
		System.out.println(s);
		url.setHost("127.0.0.1");
		client.create("/com.yy.cs.test/provider/" + mapper.writeValueAsString(url), false);
//		client.delete("/com.yy.cs.test/provider/"+ mapper.writeValueAsString(url));
//		client.create("/com.yy.cs.test/provider/" + mapper.writeValueAsString(url), false);
		synchronized (ZookeeperClientTest.class) {
			try {
				ZookeeperClientTest.class.wait();
			} catch (InterruptedException e) {
//				 TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	
	
	
}
