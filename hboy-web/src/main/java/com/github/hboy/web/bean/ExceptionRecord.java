package com.github.hboy.web.bean;

/**
 * 监控统计的异常信息记录
 * @author wenziheng
 *
 */
public class ExceptionRecord {
    private String elapsed;        //耗时
    private String input;          //入参
    private String exception;      //异常日志详细
    private String timePoint;      //异常时间点
    
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
    public String getElapsed() {
        return elapsed;
    }
    public void setElapsed(String elapsed) {
        this.elapsed = elapsed;
    }
    public String getTimePoint() {
        return timePoint;
    }
    public void setTimePoint(String timePoint) {
        this.timePoint = timePoint;
    }
    @Override
    public String toString() {
        return "ExceptionRecord [elapsed=" + elapsed + ", input=" + input + ", exception=" + exception + ", timePoint="
                + timePoint + "]";
    }
    
}
