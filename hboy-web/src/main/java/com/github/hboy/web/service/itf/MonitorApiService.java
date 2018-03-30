package com.github.hboy.web.service.itf;


import com.github.hboy.center.monitor.api.Statistics;
import com.github.hboy.web.bean.ApplicationBean;
import com.github.hboy.web.bean.ExceptionRecord;
import com.github.hboy.web.bean.MonitoringRecord;

import java.util.List;
import java.util.Map;

/**
 * 集线塔监控中心业务收集统计接口
 * @author wenziheng
 *
 */
public interface MonitorApiService {

    /**
     * 监控信息收集接口
     */
    void collect(List<Statistics> statisticsList, String ipFromReq);

    /**
     * 获取被监控的app列表 
     */
    List<ApplicationBean> getMonitoringApps();

    /**
     * 统计某app的详细监控数据记录
     */
    Map<String, List<MonitoringRecord>> calculateRecords(String appName, boolean isClient);
    
    /**
     * 获取异常详细记录
     */
    List<ExceptionRecord> getExceptionRecords(String appName, String service, boolean isClient, String localAddress, String remoteAddress, String periodStart, String periodEnd);
}
