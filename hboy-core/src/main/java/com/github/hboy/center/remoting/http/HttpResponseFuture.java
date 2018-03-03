package com.github.hboy.center.remoting.http;

import com.github.hboy.center.remoting.exchange.Response;
import com.github.hboy.center.remoting.exchange.ResponseFuture;

/**
 * 根据http同步响应流生成的结果
 * 注：
 * 由于目前是实现同步返回，所以简单粗暴
 * @author xiaobenhai
 * Date: 2016/5/17
 * Time: 18:36
 */
public class HttpResponseFuture implements ResponseFuture{

    private final Response response;
    private final Boolean done;
    
    public HttpResponseFuture(Response response){
        this.response = response;
        this.done = true;
    }

    @Override
    public Object get() throws Throwable {
        return response.getResult();
    }

    @Override
    public Object get(int timeoutInMillis) throws Throwable {
        throw new RuntimeException("This interface has not been implemented yet!");
    }

    @Override
    public boolean isDone() {
        return done;
    }

}
