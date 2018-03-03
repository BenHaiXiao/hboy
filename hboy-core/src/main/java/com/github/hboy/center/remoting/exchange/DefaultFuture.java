package com.github.hboy.center.remoting.exchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hboy.center.remoting.Channel;
import com.github.hboy.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 15:57
 */
public class DefaultFuture implements ResponseFuture {

    private static final Logger                   logger = LoggerFactory.getLogger(DefaultFuture.class);

    private static final Map<Integer, DefaultFuture> FUTURES   = new ConcurrentHashMap<Integer, DefaultFuture>();

    private final int                             id;

    private final Channel client;
    
    private final Request                         request;
    
    @SuppressWarnings("unused")
	private final InvokerConfig                         	  url;

    private final int                             timeout;

    private final Lock                            lock = new ReentrantLock();

    private final Condition                       done = lock.newCondition();

    private final long                            start = System.currentTimeMillis();

    private volatile Response                     response;
    

    public DefaultFuture(Channel client, Request request, int timeout){
        this.client = client;
        this.request = request;
        this.id = request.getId();
        this.timeout = timeout > 0 ? timeout : client.getUrl().getTimeout();
        this.url = client.getUrl();
        FUTURES.put(id, this);
    }
    
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
        Response errorResult = new Response(id);
        errorResult.setStatus(Response.BAD_ERROR);
        errorResult.setErrorMessage("request future has been canceled.");
        response = errorResult ;
        FUTURES.remove(id);
    }

    public boolean isDone() {
        return response != null;
    }


    private Object returnFromResponse() throws Throwable {
        Response res = response;
        if (res == null) {
            throw new IllegalStateException("response cannot be null");
        }
        if (res.getStatus() == Response.OK) {
            return res.getResult();
        }
        if (res.getStatus() == Response.TIMEOUT) {
            throw new TimeoutException(res.getErrorMessage());
        }
        if (res.getStatus() == Response.BAD_ERROR) {
        	throw res.getError();
        }
        throw new RemotingException(RemotingException.UNKNOWN_EXCEPTION,res.getErrorMessage());
    }
    
    
    private int getId() {
        return id;
    }
    
    public Request getRequest() {
        return request;
    }

    private int getTimeout() {
        return timeout;
    }

    private long getStartTimestamp() {
        return start;
    }

    public static DefaultFuture getFuture(long id) {
        return FUTURES.get(id);
    }


	public static void received(Response response) {
		DefaultFuture future = FUTURES.remove(response.getId());
		if (future != null) {
			future.doReceived(response);
		} else {
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
                    + timeout + " ms, request: " + request + ", client: " + client.getLocalAddress()
                    + " -> " + client.getRemoteAddress();
    }
	/**
	 * response超时检查线程。
	 */
    private static class ResponseTimeoutScanTimer implements Runnable {

        public void run() {
            while (true) {
                try {
                    for (DefaultFuture future : FUTURES.values()) {
                        if (future == null || future.isDone()) {
                            continue;
                        }
                        if (System.currentTimeMillis() - future.getStartTimestamp() > future.getTimeout()) {
                            
                            Response timeoutResponse = new Response(future.getId());
                            timeoutResponse.setStatus(Response.TIMEOUT);
                            timeoutResponse.setErrorMessage(future.getTimeoutMessage());
                            DefaultFuture.received(timeoutResponse);
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