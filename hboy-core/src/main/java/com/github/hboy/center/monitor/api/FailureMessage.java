package com.github.hboy.center.monitor.api;

import java.io.Serializable;
/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 9:10
 */
public class FailureMessage implements Serializable{
    
    private static final long serialVersionUID = -8578050115143498001L;

    //执行耗时时间
    private long elapsed;
    
    private String input;
    
    private String exception;

    private long timestamp;
    
    public FailureMessage(long elapsed, String input, String exception,long timestamp) {
        super();
        this.elapsed = elapsed;
        this.input = input;
        this.exception = exception;
        this.timestamp = timestamp;
    }

    public FailureMessage() {
        super();
    }
    
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "FailureMessage [elapsed=" + elapsed + ", input=" + input + ", exception=" + exception + ", timestamp="
                + timestamp + "]";
    }
    
}
