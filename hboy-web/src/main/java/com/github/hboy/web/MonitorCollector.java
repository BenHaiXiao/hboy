package com.github.hboy.web;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hboy.center.monitor.api.Statistics;
import com.github.hboy.web.service.MonitorApiServiceImpl;
import com.github.hboy.web.util.HttpClientUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class MonitorCollector extends HttpServlet{
    
    private static final long serialVersionUID = -5634905679745881512L;
    private static final Logger logger = LoggerFactory.getLogger(MonitorCollector.class);
    private static final String EMPTY_STR = "";
    private static final ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        // 忽略不存在的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(Include.NON_NULL); 
    }

    private MonitorApiServiceImpl monitorApiService;

    @Override
    public void init(){
        try {
            super.init();
            WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            monitorApiService = applicationContext.getBean(MonitorApiServiceImpl.class);
        } catch (Throwable t) {
            logger.error("Init monitor collector error! Please check! error message: " + t.getMessage(), t);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        String statisticsListJson = "";
        try{
            String ipFromReq = HttpClientUtil.getRequestorIp(request);
            statisticsListJson = request.getParameter("statisticsList");
            List<Statistics> statistics = mapper.readValue(statisticsListJson, new TypeReference<List<Statistics>>(){});
            monitorApiService.collect(statistics, ipFromReq);
        }catch(Throwable t){
            logger.error("access statistics: " + statisticsListJson + ", error message: " + t.getMessage(), t);
        }
        writeResp(response, EMPTY_STR);
    }

    private void writeResp(HttpServletResponse response, String content){
        try {
            response.getWriter().write(content);
        } catch (IOException e) {
            logger.error("IO error while write response! error message: " + e.getMessage(), e);
        }
    }
    
}
