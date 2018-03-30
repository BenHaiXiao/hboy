package com.github.hboy.web.controller;

import com.github.hboy.web.bean.ApplicationBean;
import com.github.hboy.web.service.ApplicationService;

import com.github.hboy.web.util.Constant;
import com.github.hboy.web.util.JsonUtil;
import com.github.hboy.web.util.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
public class ApplicationController {

	
	private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	@Autowired
	private ApplicationService applicationService;

	@RequestMapping("/index")
	public String index() {
		return "index";
	}
	@ResponseBody
	@RequestMapping(value = "/getAuthAppsByUidAsGrid", produces = "text/json;charset=utf8")
	public String getAuthAppsByUidAsGrid(HttpServletRequest request,
										 HttpServletResponse response) throws UnsupportedEncodingException {
		List<ApplicationBean> applist = applicationService.queryAllApplication();
		String gridJson = JsonUtil.toJson(applist);
		return gridJson;
	}

	// 输入服务名称，添加服务
	@RequestMapping(value="/createApplication" ,produces = "text/plain;charset=utf8")
	@ResponseBody
	public String createApplication(HttpServletRequest request,
			HttpServletResponse response, String appName_CN,String appName_EN,String userInfo) {
			if (appName_EN == null || "".equals(appName_EN)) {
				return JsonUtil.formJson("code", false);
			}
			ApplicationBean bean = new ApplicationBean();
			bean.setAppName_CN(appName_CN);
			bean.setAppName_EN(appName_EN);
			boolean flag = applicationService.createApplication(bean);
			boolean authflag = false ;
			//创建应用成功的情况下才进行授权
			if(flag && userInfo !=null &&  !"".equals(userInfo)){
				authflag = authApp(appName_EN,userInfo);
			}
			if (flag) {
				logger.info(
						"[/ApplicationController/createApplication] sucess create applicationName:{}",
						appName_EN+"  and success grant auth to "+userInfo);
				if(authflag){
					logger.info("[/ApplicationController/createApplication] sucess create  grant auth to "+userInfo);
				}
				
			}else{
				logger.info(
						"[/ApplicationController/createApplication] fail create applicationName:{}",
						appName_EN);
			} 
		return JsonUtil.formJson("createApp", flag,"grantAuth",authflag);
	}

	private boolean authApp(String appName_EN, String userInfo){
		boolean authflag = true ;
		return  authflag;
	}
	
	// 删除服务 有子节点不能删除。
	@ResponseBody
	@RequestMapping("/deleteApplication")
	public String deleteApp(HttpServletRequest request,
			HttpServletResponse response, ModelMap model, String applicationName) {
		
		boolean isChildNode = applicationService.isChildNode(applicationName);
		
		//如果存在子节点，返回0，不能删除。
		if(!isChildNode){
			return JsonUtil.formJson("code", "0");
			    
		}
		boolean flag = applicationService.deleteApp(applicationName);
		//删除失败
		return JsonUtil.formJson("code", "-1");
	}

	
	@RequestMapping("/updateAppName")
	@ResponseBody
	public String updateAppName(HttpServletRequest request,
			HttpServletResponse response, String appName_EN,String appName_CN) {
		StatusCode status = null ;
		if(!Constant.isNotEmpty(appName_EN) ||!Constant.isNotEmpty(appName_CN) ){
			status = StatusCode.INVALID_ARGUMENTS ; 
		}else{
			appName_CN = appName_CN.trim();
			appName_EN = appName_EN.trim();
			ApplicationBean bean = new ApplicationBean(appName_EN,appName_CN);
			status = applicationService.updateAppCnName(bean);
			logger.info(
					"[/ApplicationController/updateAppName] sucess updateAppName application:{"+appName_EN+"} chinese name",
					appName_CN);
		}
		 String jsonstr = "\"{\\\"status\\\":"+status.getValue()+"}\"" ; 
		 return jsonstr ; 
	}
}
