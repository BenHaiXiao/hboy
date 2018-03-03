package com.github.hboy.demo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

import com.github.hboy.center.ReferenceFactory;
import com.github.hboy.common.config.LocalConfig;
import com.github.hboy.common.util.Constants;


public class Client   {
	
	private static Scribe scribe;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static long                   maxTPS     = 0;

    private static long                   minTPS     = 0;

    private static int                    runtime;

    String serverIP1;
    String serverIP2;
    String serverIP3;
    int concurrents;
    
    Properties                            properties;
    
    {
    	properties = loadProperties("service.properties");
    }
	
	public static void main(String[] args) throws Exception {

		Client client = new Client();
		scribe = client.startClient();
		client.run(args);
	}


	    public void run(String[] args) throws Exception {

	    	
	        runtime = Integer.parseInt(properties.getProperty("runtime"));
	        final long endtime = System.nanoTime() / 1000L + (runtime+30) * 1000 * 1000L;
	        final int threadnum = Integer.parseInt(properties.getProperty("threadnum"));
	        final int requestSize = Integer.parseInt(properties.getProperty("requestSize"));
	        final String requestName = properties.getProperty("requestName");
	        // 打印开始信息
	        Date currentDate = new Date();
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTime(currentDate);
	        StringBuilder startInfo = new StringBuilder(dateFormat.format(currentDate));
	        startInfo.append(" ready to start client ,server is ");
	        startInfo.append(serverIP1);
	        startInfo.append(" ,");
	        startInfo.append(serverIP2);
	        startInfo.append(" ,");
	        startInfo.append(serverIP3);
	        startInfo.append(",concurrents is: ").append(concurrents);
	        startInfo.append(",threadnum is: ").append(threadnum);
	        startInfo.append(",requestName :").append(requestName);
	        startInfo.append(",requestSize :").append(requestSize);
	        calendar.add(Calendar.SECOND, runtime);
	        startInfo.append(" end time").append(dateFormat.format(calendar.getTime()));
	        System.out.println(startInfo.toString());
	        //初始化
	        
	        CyclicBarrier barrier = new CyclicBarrier(threadnum);
	        CountDownLatch latch = new CountDownLatch(threadnum);
	        List<ClientRunnable> runnables = new ArrayList<ClientRunnable>();
	        ReceiveSum receiveSum = new ReceiveSum(endtime);
	       
	        for (int i = 0; i < threadnum; i++) {
	        	ClientRunnable runnable = new ClientRunnable(scribe,receiveSum,barrier,latch,endtime,requestName,requestSize);
	            runnables.add(runnable);
	        }
	        
	        startRunnables(runnables);
	        latch.await();

	        //计算结果
	        List<AtomicLong[]> results = receiveSum.getResults();
	        //每秒成功的tps和每秒错误的tps
	        AtomicLong[] tps = results.get(0);
	        AtomicLong[] errorTPS = results.get(1);
	        long sumResp = 0;
	        long sumErrorResp = 0;
	        // 去掉后10秒的
	        maxTPS = tps[0].get();
	        minTPS = tps[0].get();
	        for(int i = 0; i<tps.length - 10; i++){
	        	sumResp += tps[i].get();
	        	sumErrorResp += errorTPS[i].get();
	        	if(maxTPS < tps[i].get()){
	        		maxTPS = tps[i].get();
	        	}
	        	if(minTPS > tps[i].get()){
	        		minTPS = tps[i].get();
	        	}
	        }
	        System.out.println("----------Benchmark Statistics--------------");
	        System.out.println(" Concurrents: " + concurrents);
	        System.out.println(" threadnum: " + threadnum);
	        System.out.println(" Runtime: " + runtime + " seconds");
	        long allResp = sumResp + sumErrorResp;
	        System.out.println(" Requests: " + allResp + " Success: " + (sumResp) * 100
	                           / allResp + "%, Error: "
	                           + (sumErrorResp) * 100 / allResp + "% ("
	                           + (sumErrorResp) + ")");
	        System.out.println(" Avg TPS: " + sumResp / (tps.length -10) + " Max TPS: " + maxTPS
	                           + " Min TPS: " + minTPS);
	       
	        System.exit(0);
	    }
	    public Scribe startClient() {

	    	serverIP1 = properties.getProperty("serverip1");
	    	serverIP2 = properties.getProperty("serverip2");
	    	serverIP3 = properties.getProperty("serverip3");
	    	LocalConfig localConfig1 = new LocalConfig();
	    	localConfig1.setHost(serverIP1);
			localConfig1.setPort(8186);
			localConfig1.setMaxActive(concurrents);
			localConfig1.setTimeout(3000);
	    	LocalConfig localConfig2 = new LocalConfig();
	    	localConfig2.setHost(serverIP2);
			localConfig2.setPort(8186);
			localConfig2.setMaxActive(concurrents);
			localConfig2.setTimeout(3000);
	    	concurrents = Integer.parseInt(properties.getProperty("concurrents"));
	    	LocalConfig localConfig3 = new LocalConfig();
			localConfig3.setHost(serverIP3);
			localConfig3.setPort(8186);
			localConfig3.setMaxActive(concurrents);
			localConfig3.setTimeout(3000);
			List<LocalConfig> l = new ArrayList<LocalConfig>();
			l.add(localConfig1);
			l.add(localConfig2);
			l.add(localConfig3);
			ReferenceFactory<Scribe> thriftClient = new ReferenceFactory<Scribe>();
			thriftClient.setLocalConfigs(l);
			thriftClient.setAnnotation(true);
			thriftClient.setInterface(Scribe.class);
			thriftClient.setLoadBalance(Constants.LoadBalanceType.RANDOM);
			thriftClient.setFault(Constants.FaultType.FAILFASE);
			Scribe scribe = thriftClient.getClient();
			return scribe;
		}
		 
	    protected void startRunnables(List<ClientRunnable> runnables) {
	        for (int i = 0; i < runnables.size(); i++) {
	            final ClientRunnable runnable = runnables.get(i);
	            Thread thread = new Thread(runnable, "benchmarkclient-" + i);
	            thread.start();
	        }
	    }
	    
	    
	    public static Properties loadProperties(String fileName) {
	        Properties properties = new Properties();
	        if (fileName.startsWith("/")) {
	            try {
	                FileInputStream input = new FileInputStream(fileName);
	                try {
	                    properties.load(input);
	                } finally {
	                    input.close();
	                }
	            } catch (Throwable e) {
	            }
	            return properties;
	        }
	        
	        List<java.net.URL> list = new ArrayList<java.net.URL>();
	        try {
	            Enumeration<java.net.URL> urls = Thread.currentThread().getContextClassLoader().getResources(fileName);
	            list = new ArrayList<java.net.URL>();
	            while (urls.hasMoreElements()) {
	                list.add(urls.nextElement());
	            }
	        } catch (Throwable t) {
	        }
	        
	        for(java.net.URL url : list) {
	            try {
	                Properties p = new Properties();
	                InputStream input = url.openStream();
	                if (input != null) {
	                    try {
	                        p.load(input);
	                        properties.putAll(p);
	                    } finally {
	                        try {
	                            input.close();
	                        } catch (Throwable t) {}
	                    }
	                }
	            } catch (Throwable e) {
	            }
	        }
	        return properties;
	    }
}

