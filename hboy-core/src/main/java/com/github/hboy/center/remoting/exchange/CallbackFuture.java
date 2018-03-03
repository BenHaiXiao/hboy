package com.github.hboy.center.remoting.exchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hboy.center.remoting.Channel;
import com.github.hboy.center.remoting.MethodCallback;
import com.github.hboy.center.remoting.netty.NettyChannel;
import com.github.hboy.common.config.InvokerConfig;
import com.github.hboy.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 15:29
 */
public class CallbackFuture implements ResponseFuture {

    private static final Logger                   logger = LoggerFactory.getLogger(CallbackFuture.class);

    //<通道-------<uri----队列> >
    private static final Map<NettyChannel, Map<String, ConcurrentLinkedQueue<CallbackFuture>>> FUTURES = new ConcurrentHashMap<NettyChannel, Map<String, ConcurrentLinkedQueue<CallbackFuture>>>();

    private final NettyChannel                         client;
    
    private final MethodCallback<Object> callback;
    
    @SuppressWarnings("unused")
	private final InvokerConfig url;

    private final int                             timeout;

    private final Lock                            lock = new ReentrantLock();

    private final Condition                       done = lock.newCondition();

    private final long                            start = System.currentTimeMillis();

    private volatile Response                     response;
    
    private final String callbackType;

    public CallbackFuture(NettyChannel client,String callbackType,MethodCallback<Object> callback, int timeout){
        this.client = client;
        this.callback = callback;
        this.timeout = timeout > 0 ? timeout : client.getUrl().getTimeout();
        this.url = client.getUrl();
        this.callbackType = callbackType;
        Map<String, ConcurrentLinkedQueue<CallbackFuture>> callbackTypeMap = FUTURES.get(client);
        if(callbackTypeMap == null){
            FUTURES.put(client, new ConcurrentHashMap<String, ConcurrentLinkedQueue<CallbackFuture>>());
            callbackTypeMap = FUTURES.get(client);
        }
        Queue<CallbackFuture> messgaeQueue = callbackTypeMap.get(callbackType);
        if(messgaeQueue == null){
            callbackTypeMap.put(callbackType, new ConcurrentLinkedQueue<CallbackFuture>());
            messgaeQueue = callbackTypeMap.get(callbackType);
        }
        messgaeQueue.add(this);
    }
    
    
    @Override
    public Object get() throws Throwable {
        return get(timeout);
    }

    /**
     * 等待返回
     */
    public Object get(int timeout) throws Throwable {
        if (timeout <= 0) {
            timeout = Constants.DEFAULT_TIMEOUT;
        }
        if (! isDone()) {
            long start = System.currentTimeMillis();
            lock.lock();
            try {
                while (! isDone()) {
                    done.await(timeout, TimeUnit.MILLISECONDS);
                    if (isDone() || System.currentTimeMillis() - start > timeout) {
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
            if (! isDone()) {
                throw new TimeoutException(getTimeoutMessage());
            }
        }
        return returnFromResponse();
    }
    
    public void cancel(){
        Map<String, ConcurrentLinkedQueue<CallbackFuture>> callbackTypeMap = FUTURES.get(client);
        if(callbackTypeMap == null){
            return;
        }
        ConcurrentLinkedQueue<CallbackFuture> queue = callbackTypeMap.get(callbackType);
        queue.poll();
    }

    public boolean isDone() {
        return response != null;
    }


    private Object returnFromResponse() throws Throwable {
        return null;   
    }
    
    
   
    private int getTimeout() {
        return timeout;
    }

    private long getStartTimestamp() {
        return start;
    }


	public static void received(Channel client, Response response) {
	    Map<String, ConcurrentLinkedQueue<CallbackFuture>> callbackType = FUTURES.get(client);
		if (callbackType != null) {
		    Queue<CallbackFuture> queue = callbackType.get(response.getMethodName());
		    CallbackFuture future = queue.poll();
		    if(future != null){
		        future.doReceived(response);
		        return;
		    }
		    logger.warn("The timeout response finally returned at "
		            + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
		            .format(new Date())) + ", response " + response);
		}  
		 
	}

    private void doReceived(Response res) {
        lock.lock();
        try {
          response = res;
          if (done != null) {
              done.signal();
          }
          if (response == null) {
              throw new IllegalStateException("response cannot be null");
          }
          if (res.getStatus() == Response.OK) {
               callback.onSuccess(res.getResult());
               return;
          }
          if (res.getStatus() == Response.TIMEOUT) {
              callback.onError(new TimeoutException(res.getErrorMessage()));
              return;
          }
          if (res.getStatus() == Response.BAD_ERROR) {
              callback.onError(response.getError());
              return;
          }
        } finally {
            lock.unlock();
        }
    }
    
    public Channel getClient() {
		return client;
	}

	private String getTimeoutMessage() {
        return "Waiting response timeout"
                    + ". start time: " 
                    + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(start))) + ", end time: " 
                    + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())) + ","
                    + " timeout: "
                    + timeout + " ms, MethodCallback: " + callback + ", client: " + client.getLocalAddress()
                    + " -> " + client.getRemoteAddress();
    }
	/**
	 * response超时检查线程。
	 */
    private static class ResponseTimeoutScanTimer implements Runnable {

        public void run() {
            while (true) {
                try {
                    for (Map<String, ConcurrentLinkedQueue<CallbackFuture>> callbackMap : FUTURES.values()) {
                        if (callbackMap == null) {
                            continue;
                        }
                        for (ConcurrentLinkedQueue<CallbackFuture> callbackFuture : callbackMap.values()) {
                            CallbackFuture future = callbackFuture.peek();
                            if(future == null){
                                break;
                            }
                            if (System.currentTimeMillis() - future.getStartTimestamp() > future.getTimeout()) {
                                future = callbackFuture.poll();
                                Response timeoutResponse = new Response();
                                timeoutResponse.setStatus(Response.TIMEOUT);
                                timeoutResponse.setErrorMessage(future.getTimeoutMessage());
                                timeoutResponse.setMethodName(future.callbackType);
                                CallbackFuture.received(future.getClient(),timeoutResponse);
                            }
                        }
                    }
                    Thread.sleep(30);
                } catch (Throwable e) {
                    logger.error("Exception when scan the timeout invocation of remoting.", e);
                }
            }
        }
    }

    static {
        Thread th = new Thread(new ResponseTimeoutScanTimer(), "ResponseTimeoutScanTimer");
        th.setDaemon(true);
        th.start();
    }


}