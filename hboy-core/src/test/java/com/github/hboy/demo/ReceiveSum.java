package com.github.hboy.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;



/**
 * 
 */
public class ReceiveSum  {
	
	
	    // error request per second
	   // private AtomicLong[]                 errorTPS           = null;
	    protected AtomicLong[]                 errorTPS           = null;

	    // tps per second
	    //private AtomicLong[]                tps                = null;
	    protected AtomicLong[]                tps                = null;

	    // benchmark startTime
	   // private long                  startTime;
	    protected long                  startTime;

	    // benchmark maxRange
	   // private int                   maxRange;
	    protected int                   maxRange;
	
    AtomicBoolean  isCount = new AtomicBoolean(false);
    
     
    public List<AtomicLong[]> getResults() {
        List<AtomicLong[]> results = new ArrayList<AtomicLong[]>();
        results.add(tps);
        results.add(errorTPS);
        return results;
    }
    
    public void doMessages(Scribe scribe,String requestName,byte[] requestSize) {
    	try {
    		
    		if(requestName.equals("testString")){
        		scribe.testString(new String(requestSize));
        	}
        	if(requestName.equals("testVoid")){
        		scribe.testVoid();
        	}
        	if(requestName.equals("testObj")){
        		scribe.testObj(new LogEntry(new String(requestSize)));
        	}
        	long currentTime = System.nanoTime() / 1000L;
    		if(currentTime < startTime){
    			return;
    		}
            int range = (int)(Long.parseLong(String.valueOf(currentTime - startTime)) / 1000000);
    		tps[range].getAndIncrement();
            
        } catch (Exception e) {
        	long currentTime = System.nanoTime() / 1000L;
        	int range = (int)(Long.parseLong(String.valueOf(currentTime - startTime)) / 1000000);
        	errorTPS[range].getAndIncrement();
        	
        }
    }
    
    
  
    public ReceiveSum(long endTime){
    	this.startTime = System.nanoTime() / 1000L + 30 * 1000 * 1000L;;
        maxRange =(int)(Long.parseLong(String.valueOf((endTime - startTime))) / 1000000) + 10;
        errorTPS = new AtomicLong[maxRange];
        tps = new AtomicLong[maxRange];
        // init
        for (int i = 0; i < maxRange; i++) {
            errorTPS[i] = new AtomicLong(0);
            tps[i] = new AtomicLong(0);
        }
    }
    
}
