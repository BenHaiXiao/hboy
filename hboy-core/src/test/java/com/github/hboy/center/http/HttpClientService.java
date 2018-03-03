package com.github.hboy.center.http;

import java.util.List;

import com.github.hboy.common.config.annotation.http.HttpParam;
import com.github.hboy.common.config.annotation.http.HttpPath;

@HttpPath("userService")
public interface HttpClientService {
    
    @HttpPath("getUser")
    User getUser(@HttpParam(name = "uid")      long    userId, 
                 @HttpParam(name = "sex")      Integer sex,
                 @HttpParam(name = "nick")     String  nick,
                 @HttpParam(name = "isVIP")    boolean isVIP);

    @HttpPath("report")
    void reportLoginLogs(@HttpParam(name = "loginLogs") List<LoginLog> loginLogList);
}