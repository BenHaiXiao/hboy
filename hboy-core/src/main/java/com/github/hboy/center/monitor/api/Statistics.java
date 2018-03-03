package com.github.hboy.center.monitor.api;

import java.io.Serializable;

/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 10:33
 */
public class Statistics extends StatisticsTitle implements Serializable {
    
    private static final long serialVersionUID = -4927784760557651150L;

    public Statistics(){}

    public Statistics(String application, String service, String method, boolean isClient, StatisticsMessage message, String timestamp) {
        super(application, service, method, isClient);
        this.message = message;
        this.timestamp = timestamp;
    }

    public Statistics(StatisticsTitle title, StatisticsMessage message, String timestamp) {
        super(title.application, title.service, title.method, title.isClient);
        this.message = message;
        this.timestamp = timestamp;
    }
    
    
    private StatisticsMessage message;  //监控信息

    private String timestamp;   //发送时间
    
    public StatisticsMessage getMessage() {
        return message;
    }

    public void setMessage(StatisticsMessage message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Statistics " + super.toString() +"[message=" + message + ", timestamp=" + timestamp + "]" ;
    }
    
}
