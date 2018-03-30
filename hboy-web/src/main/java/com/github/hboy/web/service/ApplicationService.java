package com.github.hboy.web.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hboy.common.config.ServerInfo;
import com.github.hboy.common.util.Constants;
import com.github.hboy.web.bean.ApplicationBean;
import com.github.hboy.web.util.Constant;
import com.github.hboy.web.util.StatusCode;
import com.github.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ZkClient zkClient;

    
    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        // 忽略不存在的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(Include.NON_NULL); 
    }
    
    public ApplicationService(){
    }

    public ZkClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZkClient zkClient) {
		
		this.zkClient = zkClient;
	}
	
	/**
	 * ApplcationService bean 注入初始化完成后 即 执行此方法
	 */
	@PostConstruct
	public void GenAppAuthArchetypePath(){
		//初始化 应用与用户关系 基本路径 /AppUserRelManage/User2App
		// /AppUserRelManage/App2User
		try {
			if(!zkClient.exists(Constant.AUTH_USER_TO_APP_REL)){
				create(Constant.AUTH_USER_TO_APP_REL);
			}
			if(!zkClient.exists(Constant.AUTH_APP_TO_USER_REL)){
				create(Constant.AUTH_APP_TO_USER_REL);
			}
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e.getMessage()+
					"\n\t"+ApplicationService.class.getName()+"zk 创建应用与用户关系初始化路径 失败");
		}
		
	}
	

	/**
     * 创建应用
     * @param
     * @return true：添加成功，false：添加失败
     */
    public boolean createApplication(ApplicationBean bean){
    	if(isExistService(bean.getAppName_EN())){
    		return false;
    	}
    	String path = Constants.PATH_SPLIT + bean.getAppName_EN();
    	if(zkClient.exists(path)){
    		return false;
    	}
		if(!zkClient.exists(path)){
			try {
				String cnName = bean.getAppName_CN();
				if(cnName == null) cnName="";
				zkClient.createPersistent(path,cnName.getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
    	
    	if(zkClient.exists(path)){
    		return true;
    	}
    	return false;
    }
    
    public StatusCode updateAppCnName(ApplicationBean bean){
    	//应用中文名称为null 则返回非法参数
    	if(bean.getAppName_CN() == null || bean.getAppName_EN() == null){
    		return StatusCode.INVALID_ARGUMENTS;
    	}
    	String name = bean.getAppName_EN().trim();
    	String path = Constants.PATH_SPLIT + name;
    	if(!zkClient.exists(path)){
    		return StatusCode.NOT_EXISIT ; 
    	}
    	//String path = Constants.PATH_SPLIT + bean.getAppName_EN();
    	try {
    		zkClient.writeData(path, bean.getAppName_CN().getBytes("UTF-8"));
    		return StatusCode.OK;
		} catch (Exception e) {
			e.printStackTrace();
			return StatusCode.INTERVAL_ERROR;
		}
    }
   
    
    
	
    /**
     * 判断服务是否已存在
     * @param
     * @return
     */
    public boolean isExistService(String serviceName){
    	
    	
    	if(!validatePath(serviceName)){
    		return true;
    	}
    	return zkClient.exists(Constants.PATH_SPLIT + serviceName);
    }
    
    /**
     * 列出所有服务
     * @return
     */
    public List<ApplicationBean> queryAllApplication(){
    	//规定服务名称，服务都是"/"的子节点
    	List<String> serviceList= zkClient.getChildren(Constants.PATH_SPLIT);
    	serviceList = listUsefulService(serviceList);
    	List<ApplicationBean> ls = new ArrayList<ApplicationBean>();
    	for(String s : serviceList){
    		ApplicationBean bean = new ApplicationBean();
    		bean.setAppName_EN(s);
    		byte[] data = zkClient.readData("/"+s, true);
			String name_cn = null;
			try {
				if(data != null){
					name_cn = new String(data,"utf-8");
					bean.setAppName_CN(name_cn);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} 
    		ls.add(bean);
    	}
    	return ls;
    }
    
    private List<String> listUsefulService(List<String> serviceList){
    	int i = serviceList.indexOf(Constants.ZOOKEEPER);
    	//去掉ZK自带的
    	if(i>-1){
    		serviceList.remove(i);
    	}
    	int j  =serviceList.indexOf(Constant.AUTH_USER_MANAGE_NODE.replace("/", ""));
    	if(j>-1){
    		serviceList.remove(j);
    	}
    	int k = serviceList.indexOf(Constant.AUTH_APP_USER_REL.replace("/", ""));
    	if(k>-1){
    		serviceList.remove(k);
    	}
    	return serviceList;
    }
  
    
    public List<String> queryListService(String applicationName){
    	if(!validatePath(applicationName)){
    		return null;
    	}
    	String path = Constants.PATH_SPLIT + applicationName;
    	List<String> serviceList= zkClient.getChildren(path);
    	return serviceList;
    }
    /**
     * 检查是否有子节点  
     * 第一层：Application
     * 第二层 :Service
     * 第三是
     * 第四是 URI,节点
     * @param applicatinName  
     * @return
     */
    public boolean isChildNode(String applicatinName){
    	if(!validatePath(applicatinName)){
    		return false;
    	}
    	String path = addSlash(applicatinName);
    	
    	if(!zkClient.exists(path)){
    		return false;
    	}
    	List<String> children = zkClient.getChildren(path);
    	if(children != null && children.size() > 0){ //size>0 有子节点 返回false 
    		return false;
    	}
    	return true;
    }
    
    private boolean validatePath(String path){
    	if(path == null || "".equals(path.trim()) || Constants.PATH_SPLIT.equals(path)){
    		return false;
    	}
    	return true;
    }
    
    public boolean deleteApp(String appName){
    	if(!validatePath(appName)){
    		return false;
    	}
    	String path = addSlash(appName);
    	//删除之前 检验一次，如果不存在则直接return false
    	if(!zkClient.exists(path)){
    		return false;
    	}
    	zkClient.deleteRecursive(path);
    	//删除之后 再检测一次,如果不存在则说明删除成功
    	if(!zkClient.exists(path)){
    		return true;
    	}
    	return false;
    }
    
    /**
     * 删除服务
     * @param serviceName
     * @return
     * @throws UnsupportedEncodingException 
     */
//    public boolean deleteServerInfo(String serviceName,String path){
//
//    	if(!validatePath(serviceName)){
//    		return false;
//    	}
//    	if(path == null || path.length() == 0){
//    		return false;
//    	}
//    	String s;
//		try {
//			String p[] = path.split(",");
//			byte[] b = new byte[p.length];
//			for(int i = 0; i<p.length ;i++){
//				b[i] = (byte) Integer.parseInt(p[i]);
//			}
//			s = new String(b,"UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			return false;
//		}
//    	String  p = preHandle(serviceName);
//    	s = p + Constants.PATH_SPLIT +  s;
//    	if(!zkClient.exists(s)){
//    		return false;
//    	}
//    	zkClient.deleteRecursive(s);
//    	if(!zkClient.exists(s)){
//    		return true;
//    	}
//    	return false;
//    }
    
//    /**
//     * 查询服务的配置
//     * @param serviceName
//     * @return
//     */
//    public Map<String,ServerInfo> queryService(String appNameService){
//    	String path = preHandle(appNameService);
//    	List<String> configList = zkClient.getChildren(path);
//
//    	Map<String, ServerInfo> serviceInfos = null;
//		try {
//			serviceInfos = toServiceInfoList(configList);
//		} catch (IOException e) {
//			LOG.error(e.getMessage(),e);
//		}
//    	if(serviceInfos == null ){
//    		return null;
//    	}
//    	return serviceInfos;
//    }
    
    
//    public ServerInfo queryPath(String serviceName,String path) throws UnsupportedEncodingException{
//    	
//    	Map<String,ServerInfo> map = this.queryService(serviceName);
////    	String s = "";
//		if (map.containsKey(path)) {
////			String p[] = path.split(",");
////			byte[] b = new byte[p.length];
////			for (int i = 0; i < p.length; i++) {
////				b[i] = (byte) Integer.parseInt(p[i]);
////			}
////			s = new String(b, "UTF-8");
//			return map.get(path);
//		}
//    	return null;
//    }
    
//    public boolean updateServerinfo(String serviceName,String path,ServerInfo serviceInfo) throws IOException{
//    	
//    	String ppath = preHandle(serviceName);
//    	List<String> configList = zkClient.getChildren(ppath);
//    	
//		String p[] = path.split(",");
//		byte[] b = new byte[p.length];
//		for (int i = 0; i < p.length; i++) {
//			b[i] = (byte) Integer.parseInt(p[i]);
//		}
//		String s = new String(b, "UTF-8");
//    	for(String list : configList){
//    		if(list.equals(s)){
//    			ServerInfo serverinfo = toServiceInfo(s);
//        		serverinfo.setPoolSize(serviceInfo.getPoolSize());
//        		serverinfo.setTimeout(serviceInfo.getTimeout());
//        		serverinfo.setWeight(serviceInfo.getWeight());
//        		//如果添加成功，需要删除久的配置
//        		if(addServerInfo(serviceName,serverinfo)){
//                	String pa = ppath + Constants.PATH_SPLIT +  list;
//                	if(!zkClient.exists(pa)){
//                		return false;
//                	}
//                	zkClient.delete(pa);
//        		}
//    			break;
//    		}
//    	}
//    	return true;
//    }
    
    
   public  boolean addServerInfo(String serviceName,ServerInfo serverinfo) throws JsonProcessingException{
	   if(serverinfo == null){
		   return false;
	   }
	   String path = preHandle(serviceName);
//	   URL url = toURL(serverinfo);
//	   url.setInterfaceName(serviceName);
	   serverinfo.setInterfaceName(serviceName);
	   path = getChildNode(path,serverinfo);
	   if(zkClient.exists(path)){
		   return false;
	   }
	   this.create(path);
//	   zkClient.create(childPath, null, CreateMode.PERSISTENT);
	   if(zkClient.exists(path)){
		   return true;
	   }
	   return false;
   }
    
   private void create(String path) {
		int i = path.lastIndexOf(Constants.PATH_SPLIT);
		if (i > 0) {
			create(path.substring(0, i));
		}
		if(!zkClient.exists(path)){
			zkClient.createPersistent(path);
		}
	}
    /**
     * 在输入的服务名称前添加"/"
     * @param serviceName
     * @return
     */
   private String addSlash(String serviceName){
    	String path = Constants.PATH_SPLIT +serviceName;
    	return path;
    }
    
    /**
     * 扩展节点 "/provide",作为服务的固定子节点
     * @param serviceName
     * @return
     */
    private String addSuffix(String serviceName){
    	
    	return serviceName + Constants.PATH_SPLIT+ Constants.PATH_PROVIDER;
    }
    
    //构造URL配置的父节点  如： 
    private String preHandle(String serviceName){
    	String path = addSlash(serviceName);
    	path = addSuffix(path);
    	return path;
    }
    
    //将配置信息拼装成：/xxx.yy.zz/provider/urlConfig 形式
    private String getChildNode(String path,ServerInfo urlConfig) throws JsonProcessingException{
    	String childPath = path + Constants.PATH_SPLIT + mapper.writeValueAsString(urlConfig);
    	return childPath;
    }
    
    //
//    private Map<String,ServerInfo> toServiceInfoList(List<String> configList) throws UnsupportedEncodingException,IOException{
//    	if(configList == null || configList.isEmpty()){
//    		return null;
//    	}
//    	Map<String,ServerInfo> urlList = new HashMap<String,ServerInfo>();
//    	for(String config : configList){
//    		ServerInfo urlConfig = this.toServiceInfo(config);
//    		byte[] bs = config.getBytes("UTF-8");
//    		StringBuffer c = new StringBuffer();
//    		for(int i=0; i<bs.length; i++){
//    			c.append(bs[i]);
//    			if(i != bs.length -1){
//    				c.append(',');	
//    			}
//    		}
//    		urlList.put(c.toString(), urlConfig);
//    	}
//    	return urlList;
//    }
    
    private ServerInfo toServiceInfo(String configList) throws IOException{
    	ServerInfo serverInfo = mapper.readValue(configList, ServerInfo.class);
    	return serverInfo;
    	
    }
    
    public void updateCluterWay(String serviceName, Constants.LoadBalanceType lbtype, Constants.FaultType taultType, int retries) throws IOException{
    	String path = preHandle(serviceName);
    	List<String> configList = zkClient.getChildren(path);
    	
    	for(String s : configList){
    		ServerInfo serverinfo = toServiceInfo(s);
//    		serverinfo.setLoadBalance(lbtype);
//    		serverinfo.setFault(taultType);
//    		serverinfo.setRetries(retries);
    		//如果添加成功，需要删除久的配置
    		if(addServerInfo(serviceName,serverinfo)){
        		String  p = preHandle(serviceName);
            	p = p + Constants.PATH_SPLIT +  s;
            	if(!zkClient.exists(p)){
            		continue;
            	}
            	zkClient.delete(p);
    		}
    	}
    }

	

}
