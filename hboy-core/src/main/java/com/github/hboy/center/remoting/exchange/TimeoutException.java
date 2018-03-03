package com.github.hboy.center.remoting.exchange;

import com.github.hboy.center.remoting.RemotingException;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 10:39
 */
public class TimeoutException extends RemotingException {

    private static final long serialVersionUID = 3122966731958222692L;

    public TimeoutException(String message){
        super(RemotingException.TIMEOUT_EXCEPTION,message);
    }

}