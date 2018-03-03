package com.github.hboy.center.remoting;


/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 11:31
 */
public interface MethodCallback<T> {

    
    public void onSuccess(T message);

    
    public void onError(Throwable exception);
}
