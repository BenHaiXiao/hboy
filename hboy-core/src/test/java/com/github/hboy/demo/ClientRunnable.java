package com.github.hboy.demo;

 
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class ClientRunnable implements Runnable {

    private byte[]                requestSize;

    private CyclicBarrier         barrier;

    private CountDownLatch        latch;

    private long                  endTime;

    private boolean               running            = true;

    private  Scribe scribe;
    
    private  String requestName;
    
    ReceiveSum receiveSum;
    
    public ClientRunnable(Scribe scribe,ReceiveSum receiveSum,CyclicBarrier barrier, CountDownLatch latch,
                                                  long endTime,String requestName,int requestSize){
        this.barrier = barrier;
        this.latch = latch;
        this.endTime = endTime * 1000L;
        this.requestSize =  new byte[requestSize]; 
        this.receiveSum = receiveSum;
        this.scribe = scribe; 
        this.requestName = requestName;
    }

    public void run() {
        try {
            barrier.await();
        } catch (Exception e) {
            // IGNORE
        }
        doRun();
        latch.countDown();
    }

    private void doRun() {
        while (running) {
            long beginTime = System.nanoTime() ;
            if (beginTime >= endTime) {
                running = false;
                break;
            }
            try {
            	receiveSum.doMessages(scribe, requestName, requestSize);
            } catch (Exception e) {
            	System.err.println("send error");
            }
        }
    }


}
