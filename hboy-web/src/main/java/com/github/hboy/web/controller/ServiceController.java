package com.github.hboy.web.controller;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hboy.common.config.Configuration;
import com.github.hboy.common.config.ServerInfo;
import com.github.hboy.common.util.Constants;
import com.github.hboy.web.bean.ConfigurationBean;
import com.github.hboy.web.bean.ServerInfoBean;
import com.github.hboy.web.bean.ServiceBean;
import com.github.hboy.web.service.CenterService;
import com.github.hboy.web.util.Constant;
import com.github.hboy.web.util.Json;
import com.github.hboy.web.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Controller
public class ServiceController {

	private static final Logger logger = LoggerFactory
			.getLogger(ServiceController.class);

	@Autowired
	private CenterService centerService;


	 private static ObjectMapper mapper;
	    static {
	        mapper = new ObjectMapper();
	        // 忽略不存在的属性
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        mapper.setSerializationInclusion(Include.NON_NULL); 
	    }
	
	// 列出服务列表
	@RequestMapping("/listService")
	public String listService(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		List<String> serviceList = centerService.listService();
		model.addAttribute("serviceList", serviceList);
		return "serviceList";
	}

	// 输入服务名称，添加服务
	@RequestMapping("/createService")
	@ResponseBody
	public String createService(HttpServletRequest request,
			HttpServletResponse response, ModelMap model,String appName, String serviceName) {
		if (serviceName == null || "".equals(serviceName.trim())) {
			logger.info(
					"[/HandleNodeController/createService] fail to create service:{}",
					serviceName);
			return JsonUtil.formJson("code", Constant.ERROR_ARGUMENTS_CODE);
		}
		if (appName == null || "".equals(appName.trim())) {
			logger.info(
					"[/HandleNodeController/createService] fail to create service:{}",
					serviceName);
			return JsonUtil.formJson("code", Constant.ERROR_ARGUMENTS_CODE); 
		}
		
		String path = appName + Constants.PATH_SPLIT +serviceName.trim();
		boolean flag = centerService.createService(path);
		if (flag) {
			logger.info(
					"[/HandleNodeController/createService] success to create service:{}",
					serviceName);
			return JsonUtil.formJson("code", Constant.SUCCESS_CODE);
		} else {
			return JsonUtil.formJson("code", Constant.ERROR_ARGUMENTS_CODE);
		}
	}

	// 删除服务 有子节点不能删除。
	@ResponseBody
	@RequestMapping(value = "/deleteService")
	public String deleteService(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) 
					throws UnsupportedEncodingException {
		String applicationName = request.getParameter("appName")==null ?"":request.getParameter("appName") ; 
		String serviceName = request.getParameter("serviceName")==null ? "":request.getParameter("serviceName");
		serviceName = applicationName + Constants.PATH_SPLIT +serviceName.trim();
		boolean isChildNode = centerService.isChildNode(serviceName);
		//如果存在子节点，返回0，不能删除。
		if(isChildNode){
			return JsonUtil.formJson("code", "0");
		}
		boolean flag = centerService.deleteService(serviceName);
		if (flag) {
			logger.info(
					"[/HandleNodeController/deleteService] sucess delete service:{}",
					serviceName);
			//删除成功
			return JsonUtil.formJson("code", "1");
		}
		//删除失败
		return JsonUtil.formJson("code", "-1");
	}

	// 输入服务名称或点击服务详情，查看服务配置信息列表
	@RequestMapping(value = "/queryServerInfo", produces = "text/json;charset=UTF-8")
	@ResponseBody
	public String queryServiceDetail(HttpServletRequest request,
			HttpServletResponse response) {
		String applicationName  = request.getParameter("appName");
		String serviceName = request.getParameter("serviceName");
		if (!Constant.isNotEmpty(serviceName) || !Constant.isNotEmpty(applicationName)) {
			return Constant.ERROR_ARGUMENTS_CODE+"" ;
		}
		Map<String,Configuration> configList = centerService.queryService(applicationName,serviceName);
		ConfigurationBean configurationBean = centerService.queryConfiguratorsPath(applicationName,serviceName);
		List<ServerInfoBean> result = new ArrayList<ServerInfoBean>();
		if(configList == null){
			return Constant.NO_AVAILIABE_VALUE+"" ;
		}

        for (Entry<String, Configuration> entry : configList.entrySet()) {
            Configuration info = entry.getValue();
            ServerInfoBean bean = new ServerInfoBean(applicationName,entry.getKey(),info);
            if(configurationBean != null){
                bean.setAccessable(configurationBean.isAccessable());
                bean.setFault(configurationBean.getFault());
                bean.setInterval(configurationBean.getInterval());
                bean.setLoadBalance(configurationBean.getLoadBalance());
                bean.setRetries(configurationBean.getRetries());
            }
            result.add(bean);
        }
        return Json.ObjToStr(result);
		
	}

//	// 添加服务配置初始化页面
//	@RequestMapping("/addServerInfoInit")
//	public String addServiceInfoInit(HttpServletRequest request,
//			HttpServletResponse response, ModelMap model,String applicationName,
//			String serviceName) {
//		String path = applicationName + Constants.PATH_SPLIT + serviceName.trim();
//		if(!centerService.isExistService(path)){
//			model.addAttribute(MSG_STATUS.MSG, "服务不存在。");
//			return "addConfig";
//		}
////		String path = applicationName + Constants.PATH_SPLIT +serviceName.trim();
//		Map<String,ServerInfo> map = centerService.queryService(path);
//		if(map != null && !map.isEmpty()){
//			Entry<String,ServerInfo> it = map.entrySet().iterator().next();
//			model.addAttribute("selectedLB", it.getValue().getLoadBalance());
//			model.addAttribute("selectedFa", it.getValue().getFault());
//			model.addAttribute("retries", it.getValue().getRetries());
//		}
//		model.addAttribute("serviceName", serviceName);
//		model.addAttribute("loadBalance", LoadBalanceType.values());
//		model.addAttribute("fault", FaultType.values());
//		model.addAttribute("applicationName", applicationName);
//		return "addConfig";
//	}
	
	@RequestMapping(value = "/addServerInfoCommit" , produces = "text/json;charset=UTF-8")
	@ResponseBody
	public String addServiceInfoCommit(HttpServletRequest request,
				HttpServletResponse response,String appName,String serviceName) throws JsonProcessingException {
				
				ServerInfo node = getServerInfo(request);
				
				StringBuffer buffer = new StringBuffer("\"{\\\"code\\\" :");
				boolean flag = centerService.addServerInfo(appName,serviceName, node);
				if (flag) {
					logger.info(
							"[/HandleNodeController/addConfig] sucess addConfig:{}",node);
					buffer.append(Constant.SUCCESS_CODE+"").append("}\""); ; 
				}else{
					logger.error(
							"[/HandleNodeController/addConfig] failure addConfig:{}",node);
					buffer.append(Constant.ERROR_ARGUMENTS_CODE+"").append("}\""); ; 
				}
			return buffer.toString();
		}


	// 删除配置信息
	@ResponseBody
	@RequestMapping("/deleteConfig")
	public String deleteConfig(HttpServletRequest request,
			HttpServletResponse response, ModelMap model,String applicationName,String serviceName, String path) {
		
		boolean flag = centerService.deleteServerInfo(applicationName,serviceName,path);
		String rs;
		if(flag){
			rs = JsonUtil.formJson("code", "1");
			logger.info(
					"[/HandleNodeController/deleteConfig] sucess delete node:{}",
					centerService.decodeNodePath(applicationName, serviceName, path));
		}else{
			rs = JsonUtil.formJson("code", "-1");
			logger.error(
					"[/HandleNodeController/deleteConfig] sucess delete node:{}",
					centerService.decodeNodePath(applicationName, serviceName, path));
		}
		return rs;
	}
	
	@ResponseBody
    @RequestMapping("/updateAccessable")
    public String updateAccessable(HttpServletRequest request,
            HttpServletResponse response, ModelMap model,String appName,String serviceName, String accessable) throws IOException {
//	    ,String applicationName,String serviceName, boolean accessable
	    if(accessable == null || "null".equals(accessable) || "".equals(accessable)){
	        //参数错误
	        return JsonUtil.formJson("code", "-2");
	    }
	    
	    if(appName == null || "null".equals(appName) || "".equals(appName)){
            //参数错误
            return JsonUtil.formJson("code", "-2");
        }
	    
	    if(serviceName == null || "null".equals(serviceName) || "".equals(serviceName)){
            //参数错误
            return JsonUtil.formJson("code", "-2");
        }
	    
        boolean isAccessable = Boolean.valueOf(accessable);
        
        boolean isOk = centerService.updateAccessable(appName.trim(), serviceName.trim(), isAccessable);
        if (isOk) {
            logger.info(
                    "[/ServiceController/updateAccessable] sucess updateAccessable service:{} accessable:{},",
                    serviceName,isAccessable);
            //修改成功
            return JsonUtil.formJson("code", "1");
        }
        //修改失败 
        return JsonUtil.formJson("code", "-1");
    }
	
	

//	// 更新配置信息
//	@RequestMapping("/updateNodeConfig")
//	public String update(HttpServletRequest request,
//			HttpServletResponse response, ModelMap model,String applicationName,String serviceName) {
//		
//		if (serviceName == null || "".equals(serviceName)) {
//			model.addAttribute(MSG_STATUS.MSG, "查询失败,请输入正确服务名称");
//			return "clusterInit";
//		}
//		if (applicationName == null || "".equals(applicationName)) {
//			model.addAttribute(MSG_STATUS.MSG, "查询失败,请输入正确应用名称");
//			return "clusterInit";
//		}
//		String path = applicationName + Constants.PATH_SPLIT +serviceName.trim();
//		
//		Map<String,ServerInfo> configList = centerService.queryService(path);
//		model.addAttribute("serviceName", serviceName);
//		if(configList != null && !configList.isEmpty()){
//			Entry<String,ServerInfo> it = configList.entrySet().iterator().next();
//			model.addAttribute("selectedLB", it.getValue().getLoadBalance());
//			model.addAttribute("selectedFa", it.getValue().getFault());
//			model.addAttribute("retries", it.getValue().getRetries());
//		}
//		model.addAttribute("configList", configList);
//		model.addAttribute("loadBalance", LoadBalanceType.values());
//		model.addAttribute("fault", FaultType.values());
//		model.addAttribute("applicationName", applicationName);
//		return "clusterInit";
//	}
//	
	//更新服务的配置信息
	@RequestMapping(value = "/updateClusterCommit" , produces = "text/json;charset=UTF-8")
	@ResponseBody
	public String updateClusterCommit(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String appName = request.getParameter("appName") ; 
		String serviceName = request.getParameter("serviceName") ; 
		String retries = request.getParameter("retries") == null ?"0":request.getParameter("retries");
		String loadBalance = request.getParameter("loadBalance");
		String fault = request.getParameter("fault");
		StringBuffer buffer = new StringBuffer("\"{\\\"code\\\" :");
		try {
			centerService.updateCluterWay(appName, serviceName,
					Constants.LoadBalanceType.valueOf(loadBalance), Constants.FaultType.valueOf(fault),
					Integer.parseInt(retries));
			buffer.append(Constant.SUCCESS_CODE+"") ; 
			logger.info("[/HandleNodeController/updateConfig] sucess update node:{}","appName="+appName
					+",serviceName = "+serviceName+",loadBalance = "+loadBalance+",fault = "+fault+",retries = "+retries);
		} catch (Exception e) {
			logger.error("[/HandleNodeController/updateConfig] fail to update node:{}", e.getMessage());
			buffer.append(Constant.ERROR_ARGUMENTS_CODE+"") ; 
		}
		buffer.append("}\"");
		return buffer.toString();
	}
	
	
//	@RequestMapping("/updateServerInfoInit")
//	public String updateServerInfoInit(HttpServletRequest request,
//			HttpServletResponse response, ModelMap model,String applicationName,String serviceName,String path) throws UnsupportedEncodingException {
//		
//		ServerInfo serverInfo = centerService.queryPath(applicationName,serviceName, path);
//		model.addAttribute("serviceName", serviceName);
//		model.addAttribute("path", path);
//		model.addAttribute("serverInfo", serverInfo);
//		model.addAttribute("applicationName", applicationName);
//		return "updateServerInfoInit";
//	}
	
	@RequestMapping(value = "/updNodeConfig" ,produces="text/json;charset=UTF-8" )
	@ResponseBody
	public String updateServerinfoCommit(HttpServletRequest request,
 HttpServletResponse response, String appName,
            String serviceName, String path) throws IOException {
        StringBuffer buffer = new StringBuffer("\"{\\\"code\\\" :");

        if (!Constant.isNotEmpty(appName)) {
            buffer.append(Constant.ERROR_ARGUMENTS_CODE + "");
            buffer.append("}\"");
            return buffer.toString();
        }
        ServerInfo node = getServerInfo(request);
        boolean isUpdate = centerService.updateServerinfo(appName, serviceName, path, node);
        if (!isUpdate) {
            logger.error("[/HandleNodeController/updateNodeConfig] fail to update node:{}");
            buffer.append(Constant.ERROR_ARGUMENTS_CODE + "");
            buffer.append("}\"");
            return buffer.toString();
        } else {
            logger.info("[/HandleNodeController/updateNodeConfig] success to update node:[" + node + "]");
            buffer.append(Constant.SUCCESS_CODE + "");
            buffer.append("}\"");
            return buffer.toString();
        }
    }
	
	@RequestMapping(value = "/queryServiceListByAppName", produces = "text/json;charset=UTF-8")
	@ResponseBody
	public String queryServiceListByAppName(HttpServletRequest request,
			HttpServletResponse response){
		String appName = request.getParameter("appName") ;
		
		String uid = (String)request.getSession(true).getAttribute("yyuid");
		List<ServiceBean> servicelist = centerService.queryServiceListByAppName(appName, uid);
		String gridJson = JsonUtil.toJson(servicelist);
		return gridJson;
	}
	
	private ServerInfo getServerInfo(HttpServletRequest request){
//		String appName = request.getParameter("appName");
		String serviceName = request.getParameter("serviceName");
		String host = request.getParameter("host");
		String port = request.getParameter("port");
		String timeout = request.getParameter("timeout");
		//默认的重试次数为 3 / 权重为 5
//		String retries = request.getParameter("retries")==null?"3":request.getParameter("retries");
		String weight = request.getParameter("weight")==null?"5":request.getParameter("weight");
		String poolSize = request.getParameter("poolSize");
		String group = request.getParameter("group");
//		String loadBalance = request.getParameter("loadBalance");
//		String fault = request.getParameter("fault");
		ServerInfo node = new ServerInfo();
//		node.setAppName(appName);
//		node.setFault(FaultType.valueOf(fault));
		node.setGroup(group);
		node.setHost(host);
		node.setInterfaceName(serviceName);
//		node.setLoadBalance(LoadBalanceType.valueOf(loadBalance));
		node.setPoolSize(Integer.parseInt(poolSize));
		node.setPort(Integer.parseInt(port));
//		node.setRetries(Integer.parseInt(retries));
		node.setTimeout(Integer.parseInt(timeout));
		node.setWeight(Integer.parseInt(weight));
		return node ; 
	}
	
}
