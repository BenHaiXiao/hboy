package com.github.hboy.center.monitor.api;

import java.util.List;

import com.github.hboy.common.config.annotation.http.HttpParam;
import com.github.hboy.common.config.annotation.http.HttpPath;

/**
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 10:24
 */
@HttpPath("monitor")
public interface MonitorService {
    
    @HttpPath("api/collect")
    void collect(@HttpParam(name="statisticsList") List<Statistics> statistics);

}