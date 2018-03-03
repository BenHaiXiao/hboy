package com.github.hboy.center.remoting.exchange;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.hboy.center.proxy.Invocation;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 10:45
 */
public class Request {
    
    private static final AtomicInteger INVOKE_ID = new AtomicInteger(0);

    private final int    myId;

    private Invocation myData;

    public Request() {
    	myId = newId();
    }

    public Request(int id){
    	myId = id;
    }
    
    public int getId() {
        return myId;
    }
    
    public Invocation getData() {
        return myData;
    }

    public void setData(Invocation msg) {
    	myData = msg;
    }


    private static int newId() {
        //ID有可能增长到负数
        return INVOKE_ID.getAndIncrement();
    }

	@Override
	public String toString() {
		return "Request [myId=" + myId + ", myData=" + myData + "]";
	}

}