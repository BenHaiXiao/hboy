package com.github.hboy.center.http;

public class LoginLog {

    private long logId;
    private String log;
    
    public LoginLog(long logId, String log){
        this.logId = logId;
        this.log = log;
    }

    public long getLogId() {
        return logId;
    }
    public void setLogId(long logId) {
        this.logId = logId;
    }
    public String getLog() {
        return log;
    }
    public void setLog(String log) {
        this.log = log;
    }
}
