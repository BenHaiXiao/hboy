package com.github.hboy.web.controller;


import com.github.hboy.web.bean.ApplicationBean;
import com.github.hboy.web.bean.ExceptionRecord;
import com.github.hboy.web.bean.MonitoringRecord;
import com.github.hboy.web.service.ApplicationService;
import com.github.hboy.web.service.MonitorApiServiceImpl;
import com.github.hboy.web.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    MonitorApiServiceImpl monitorApiService;
    
    @Autowired
    ApplicationService applicationService;

    @RequestMapping("/applist")
    public ModelAndView queryAppList(){
        List<ApplicationBean> authApplist = applicationService.queryAllApplication();

        ModelAndView view = new ModelAndView();
        List<ApplicationBean> result = new ArrayList<ApplicationBean>();
        
        if(authApplist == null || authApplist.size() == 0){
            view.setViewName("applist");
            view.addObject("monitorApplist", result);
            return view;
        }
        List<ApplicationBean> monitorApplist = monitorApiService.getMonitoringApps();
        for(ApplicationBean authApp : authApplist){
            for(ApplicationBean monitorApp :monitorApplist){
               if(authApp.getAppName_EN().equals(monitorApp.getAppName_EN())){
                   result.add(authApp);
                   break;
               } 
            }
        }
        view.addObject("monitorApplist", result);
        view.setViewName("applist");
        return view;
    }
    
    @RequestMapping("/queryServiceMonitorList")
    public ModelAndView queryServiceMonitorList(String appName, boolean isClient){
        
        
        Map<String, List<MonitoringRecord>> serviceMointorMap = monitorApiService.calculateRecords(appName,isClient);
        ModelAndView view = new ModelAndView();
        view.addObject("appName", appName);
        view.addObject("isClient", isClient);
        view.addObject("serviceMointorMap", serviceMointorMap);
        view.setViewName("serviceMonitorList");
        return view;
    }

    
    @RequestMapping("/queryFailureMessage")
    public ModelAndView queryFailureMessage(String appName, boolean isClient, String serviceName, String localAddress,
            String remoteAddress, String periodStart, String periodEnd){
        
        List<ExceptionRecord> exceptionRecordList = monitorApiService.getExceptionRecords(appName, serviceName, isClient, localAddress, remoteAddress, periodStart, periodEnd);
        if(exceptionRecordList == null || exceptionRecordList.size() == 0){
            ExceptionRecord exceptionRecord1 = new ExceptionRecord();
            exceptionRecord1.setElapsed("10ms");
            exceptionRecord1.setException("ExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionException");
            exceptionRecord1.setInput("inputinputinputinputinputinputinputinputinput");
            exceptionRecord1.setTimePoint("2014-09-28 19:12:32.123");
            exceptionRecordList.add(exceptionRecord1);
            
            ExceptionRecord exceptionRecord2 = new ExceptionRecord();
            exceptionRecord2.setElapsed("10ms");
            exceptionRecord2.setException("ExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionException");
            exceptionRecord2.setInput("inputinputinputinputinputinputinputinputinput");
            exceptionRecord2.setTimePoint("2014-09-28 19:12:32.123");
            exceptionRecordList.add(exceptionRecord2);
            
            
            ExceptionRecord exceptionRecord3 = new ExceptionRecord();
            exceptionRecord3.setElapsed("10ms");
            exceptionRecord3.setException("ExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionException");
            exceptionRecord3.setInput("inputinputinputinputinputinputinputinputinput");
            exceptionRecord3.setTimePoint("2014-09-28 19:12:32.123");
            exceptionRecordList.add(exceptionRecord3);
            
            
            ExceptionRecord exceptionRecord4 = new ExceptionRecord();
            exceptionRecord4.setElapsed("10ms");
            exceptionRecord4.setException("ExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionException");
            exceptionRecord4.setInput("inputinputinputinputinputinputinputinputinput");
            exceptionRecord4.setTimePoint("2014-09-28 19:12:32.123");
            exceptionRecordList.add(exceptionRecord4);
            
            ExceptionRecord exceptionRecord5 = new ExceptionRecord();
            exceptionRecord5.setElapsed("10ms");
            exceptionRecord5.setException("ExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionException");
            exceptionRecord5.setInput("inputinputinputinputinputinputinputinputinput");
            exceptionRecord5.setTimePoint("2014-09-28 19:12:32.123");
            exceptionRecordList.add(exceptionRecord5);
            
            ExceptionRecord exceptionRecord6 = new ExceptionRecord();
            exceptionRecord6.setElapsed("10ms");
            exceptionRecord6.setException("ExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionExceptionException");
            exceptionRecord6.setInput("inputinputinputinputinputinputinputinputinput");
            exceptionRecord6.setTimePoint("2014-09-28 19:12:32.123");
            exceptionRecordList.add(exceptionRecord6);
        }
        ModelAndView view = new ModelAndView();
        view.addObject("appName", appName);
        view.addObject("isClient", isClient);
        view.addObject("serviceName", serviceName);
        view.addObject("exceptionRecordList", exceptionRecordList);
        view.setViewName("failureMessage");
        return view;
    }
}
