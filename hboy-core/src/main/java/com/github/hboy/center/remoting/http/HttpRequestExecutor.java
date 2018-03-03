package com.github.hboy.center.remoting.http;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.exchange.Request;
import com.github.hboy.center.remoting.exchange.ResponseFuture;

/**
 * http请求执行接口
 * @author wenziheng
 *
 */


public interface HttpRequestExecutor {

    /**
     * 执行同步http请求
     * @param request
     * @return
     */
    ResponseFuture executeRequest(Request request) throws RemotingException;
    
}
