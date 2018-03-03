package com.github.hboy.center.remoting.exchange;


/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 16:30
 */
public interface ResponseFuture {

    /**
     * 
     */
    Object get() throws Throwable;

    /**
     * 
     */
    Object get(int timeoutInMillis) throws Throwable;

    /**
     * 
     */
    boolean isDone();

}