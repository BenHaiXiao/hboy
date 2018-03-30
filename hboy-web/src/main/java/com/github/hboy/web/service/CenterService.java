package com.github.hboy.web.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hboy.center.subscribe.SubscribeException;
import com.github.hboy.common.config.Configuration;
import com.github.hboy.common.config.ServerInfo;
import com.github.hboy.common.util.Constants;
import com.github.hboy.web.bean.ConfigurationBean;
import com.github.hboy.web.bean.ServiceBean;
import com.github.hboy.web.util.Constant;
import com.github.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

@Service
public class CenterService {

    protected static final Logger LOG =  LoggerFactory.getLogger(CenterService.class);
    
    @Autowired
    private ZkClient zkClient;
    
    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        // 忽略不存在的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(Include.NON_NULL); 
    }
    
    public CenterService(){
    }

    public ZkClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}

	/**
     * 添加服务
     * @param path
     * @return true：添加成功，false：添加失败
     */
    public boolean createService(String path){
    	if(isExistService(path)){
    		return false;
    	}
    	path = toProviderPath(path);
    	this.create(path);
    	//扩展节点 /provide,作为服务的固定子节点
    	if(zkClient.exists(path)){
    		return true;
    	}
    	return false;
    }
	
    /**
     * TODO 禁止服务
     * @param applicationName
     * @param serviceName
     * @param accessable
     * @return
     * @throws IOException 
     */
    public boolean  updateAccessable(String applicationName,String serviceName, boolean accessable) throws IOException{
        /**
         * 修改老数据
         */
        updateOldConfigurators(applicationName,serviceName);
        
        String appAndService = applicationName +  Constants.PATH_SPLIT + serviceName; 
        String configuratorsPath = toConfiguratorsPath(appAndService);
        
        if(!zkClient.exists(configuratorsPath)){
            this.create(configuratorsPath);
        }
//        PATH_CONFIGURATION
        List<String> configuratorsNodes = zkClient.getChildren(configuratorsPath);
        if(configuratorsNodes == null || configuratorsNodes.size() == 0){
            ConfigurationBean configurationBean = new ConfigurationBean();
            configurationBean.setAccessable(accessable);
            configurationBean.setCategory(Constants.PATH_CONFIGURATION);
            String configurationBeanNode = toConfigurationBeanNode(configurationBean);
            this.create(configuratorsPath + Constants.PATH_SPLIT + configurationBeanNode);
        }else{
            String configurationBeanNode = configuratorsNodes.get(0);
            ConfigurationBean configurationBean = toConfigurationBean(configurationBeanNode);
            configurationBean.setAccessable(accessable);
            configurationBean.setCategory(Constants.PATH_CONFIGURATION);
            //删除在添加
            zkClient.delete(configuratorsPath + Constants.PATH_SPLIT+ configurationBeanNode);
            
            this.create(configuratorsPath + Constants.PATH_SPLIT+ toConfigurationBeanNode(configurationBean));
        }
        return true;
    }
    
    /**
     * 判断服务是否已存在
     * @param
     * @return
     */
    public boolean isExistService(String serviceName){
    	if(!validateService(serviceName)){
    		return true;
    	}
    	return zkClient.exists(Constants.PATH_SPLIT + serviceName);
    }
    
    /**
     * 列出所有服务
     * @return
     */
    public List<String> listService(){
    	//规定服务名称，服务都是"/"的子节点
    	List<String> serviceList= zkClient.getChildren(Constants.PATH_SPLIT);
    	serviceList = listUsefulService(serviceList);
    	return serviceList;
    }
    
    public List<String> listUsefulService(List<String> serviceList){
    	int i = serviceList.indexOf(Constants.ZOOKEEPER);
    	if(i>-1){
    		serviceList.remove(i);
    	}
    	return serviceList;
    }
    /**
     * 检查是否有子节点
     * @param serviceName
     * @return
     */
    public boolean isChildNode(String serviceName){
    	if(!validateService(serviceName)){
    		return false;
    	}
    	String path = toProviderPath(serviceName);
    	if(!zkClient.exists(path)){
    		return false;
    	}
    	List<String> children = zkClient.getChildren(path);
    	if(children != null && children.size() > 0){
    		return true;
    	}
    	return false;
    }
    
    private boolean validateService(String serviceName){
    	if(serviceName == null || "".equals(serviceName.trim()) || Constants.PATH_SPLIT.equals(serviceName)){
    		return false;
    	}
    	return true;
    }
    
    /**
     * 删除服务
     * @param path
     * @return
     */
    public boolean deleteService(String path){
    	if(!validateService(path)){
    		return false;
    	}
    	String p = addSlash(path);
    	if(!zkClient.exists(p)){
    		return false;
    	}
    	zkClient.deleteRecursive(p);
    	if(!zkClient.exists(p)){
    		return true;
    	}
    	return false;
    }
    
    /**
     * 删除节点
     * @param serviceName
     * @return
     * @throws UnsupportedEncodingException 
     */
    public boolean deleteServerInfo(String applicationName,String serviceName,String path){
    	if(!validateService(serviceName)){
    		return false;
    	}
    	if(path == null || path.length() == 0){
    		return false;
    	}
    	String ppath = applicationName + Constants.PATH_SPLIT + serviceName;
    	String s;
		try {
			String p[] = path.split(",");
			byte[] b = new byte[p.length];
			for(int i = 0; i<p.length ;i++){
				b[i] = (byte) Integer.parseInt(p[i]);
			}
			s = new String(b,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			return false;
		}
    	String  p = toProviderPath(ppath);
    	s = p + Constants.PATH_SPLIT +  s;
    	if(!zkClient.exists(s)){
    		return false;
    	}
    	zkClient.deleteRecursive(s);
    	
    	if(!zkClient.exists(s)){
    		return true;
    	}
    	return false;
    }
    
    /**
     * 编码解析
     * @param applicationName
     * @param serviceName
     * @param path
     * @return
     */
    public String decodeNodePath(String applicationName,String serviceName,String path){
    	if(!validateService(serviceName)){
    		return null;
    	}
    	if(path == null || path.length() == 0){
    		return null;
    	}
    	String ppath = applicationName + Constants.PATH_SPLIT + serviceName;
    	String s = null;
    	
		try {
			String p[] = path.split(",");
			byte[] b = new byte[p.length];
			for(int i = 0; i<p.length ;i++){
				b[i] = (byte) Integer.parseInt(p[i]);
			}
			s = new String(b,"UTF-8");
			String  pl = toProviderPath(ppath);
	    	s = pl + Constants.PATH_SPLIT +  s;
	    	return s ;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null ; 
		}
    	 
    }
    
    
    /**
     * 查询服务的节点列表,provider
     * @param serviceName
     * @return
     */
    public Map<String,Configuration> queryService(String applicationName, String serviceName){
        String appAndService = applicationName + Constants.PATH_SPLIT +serviceName.trim();
    	String providerPath = toProviderPath(appAndService);
    	
    	List<String> configList = zkClient.getChildren(providerPath);
    	Map<String,String> providerNodes = queryProviderData(providerPath,configList);
    	Map<String, Configuration> serviceInfos = null;
		try {
			serviceInfos = toServiceInfoList(providerNodes);
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
    	if(serviceInfos == null ){
    		return null;
    	}
    	return serviceInfos;
    }
    
    
    /**
     * 查询服务的配置,ConfiguratorsPath
     * @param serviceName
     * @return
     */
    public ConfigurationBean queryConfiguratorsPath(String applicationName,String serviceName){
        String appAndService = applicationName + Constants.PATH_SPLIT +serviceName.trim();
        String providerPath = toConfiguratorsPath(appAndService);
        
        List<String> configList = zkClient.getChildren(providerPath);
        if(configList == null || configList.size() == 0){
            return null;
        }
        ConfigurationBean configuration = null;
        try {
            configuration = toConfigurationBean(configList.get(0));
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }
        if(configuration == null ){
            return null;
        }
        return configuration;
    }
    
    
    
    @SuppressWarnings("unused")
    private List<String> queryProviderData(List<String> providerNodePaths) {
        List<String> datas = new ArrayList<String>();
        if(providerNodePaths == null || providerNodePaths.size() == 0){
            return datas;
        }
        for(String  providerPath : providerNodePaths){
            try {
                byte[] data = zkClient.readData(providerPath,true);
                if(data != null){
                    String str = new String(data,"UTF-8");
                    datas.add(str);
                }else{
                    datas.add(providerPath);
                }
            } catch (UnsupportedEncodingException e) {
                LOG.error("re error:" + e.getMessage(), e);
            }
        }
        return datas;
    }
    
   
    /**
     * 节点对应的数据  //node---data | byte-----value
     * @param providerPath
     * @param providerNodes
     * @return
     */
    private Map<String,String> queryProviderData(String providerPath,List<String> providerNodes) {
        Map<String,String> datas = new HashMap<String,String>();
        if(providerNodes == null || providerNodes.size() == 0){
            return datas;
        }
        for(String  providerNode : providerNodes){
            try {
                byte[] data = zkClient.readData(providerPath + Constants.PATH_SPLIT +providerNode,true);
                if(data != null){
                    String str = new String(data,"UTF-8");
                    datas.put(providerNode, str);
                }else{
                    datas.put(providerNode, providerNode);
                }
            } catch (UnsupportedEncodingException e) {
                LOG.error("re error:" + e.getMessage(), e);
            }
        }
        return datas;
    }
    
//    public ServerInfo queryPath(String applicationName,String serviceName,String path) throws UnsupportedEncodingException{
//    	Map<String,Configuration> map = this.queryService(applicationName,serviceName);
//		if (map.containsKey(path)) {
//			return map.get(path);
//		}
//    	return null;
//    }
    
    /**
     * 修改节点
     * @param applicationName
     * @param serviceName
     * @param path
     * @param serviceInfo
     * @return
     * @throws IOException
     */
    public boolean updateServerinfo(String applicationName,String serviceName,String path,ServerInfo serviceInfo) throws IOException{
    	
        String servicePath = applicationName + Constants.PATH_SPLIT + serviceName;
    	String serviceProviderPath = toProviderPath(servicePath);
    	
    	String p[] = path.split(",");
        byte[] b = new byte[p.length];
        for (int i = 0; i < p.length; i++) {
            b[i] = (byte) Integer.parseInt(p[i]);
        }
    	String oldServerInfoPath = new String(b, "UTF-8");
//        
    	//修改如果在data里面没有数据,说明是老数据,需要删除在重新添加
    	String providerNode = serviceProviderPath + Constants.PATH_SPLIT + oldServerInfoPath;
    	byte[] data = zkClient.readData(providerNode);
    	if(data != null){
    	    byte[] providerData = toProviderData(serviceInfo).getBytes("UTF-8");
    	    zkClient.writeData(providerNode, providerData);
    	    return true;
    	}
    	//    	
    	List<String> providerNodes = zkClient.getChildren(serviceProviderPath);
    	if(providerNodes == null){
            return false ;
        }
    	for(String list : providerNodes){
    		if(list.equals(oldServerInfoPath)){
    		    Configuration configuration = toConfiguration(oldServerInfoPath);
    		    configuration.setPoolSize(serviceInfo.getPoolSize());
    		    configuration.setTimeout(serviceInfo.getTimeout());
    		    configuration.setWeight(serviceInfo.getWeight());
    		    configuration.setGroup(serviceInfo.getGroup());
    		    
    		    ServerInfo serverinfo = configuration.toServerInfo();
    		    serverinfo.setInterfaceName(serviceName);
        		//如果添加成功，需要删除旧的配置
        		if(addServerInfo(applicationName,serviceName,serverinfo)){
                	String pa = serviceProviderPath + Constants.PATH_SPLIT +  list;
                	if(!zkClient.exists(pa)){
                		return false;
                	}
                	zkClient.delete(pa);
        		}
        		return true;
    		}
    	}
    	return false;
    }
    
    /**
     * 添加节点
     * @param applicationName
     * @param serviceName
     * @param serverinfo
     * @return
     */
    public boolean addServerInfo(String applicationName, String serviceName, ServerInfo serverinfo) {
        if (serverinfo == null) {
            return false;
        }
        try{
            String servicePath = applicationName + Constants.PATH_SPLIT + serviceName;
            servicePath = toProviderPath(servicePath);
            serverinfo.setInterfaceName(serviceName);

            if (!zkClient.exists(servicePath)) {
                zkClient.createPersistent(servicePath);
            }
            String providerNode = toProviderNode(serverinfo);
            String providerNodePath = servicePath + Constants.PATH_SPLIT + providerNode;
            byte[] providerData = toProviderData(serverinfo).getBytes("UTF-8");
            zkClient.createPersistent(providerNodePath, providerData);
            if (zkClient.exists(providerNodePath)) {
                return true;
            }
        }catch(IOException e){
            LOG.error(e.getMessage(), e);
        }
        return false;
    }
    
   private String toProviderData(ServerInfo serverInfo) {
       try {
           return  mapper.writeValueAsString(serverInfo);
       } catch (JsonProcessingException e) {
           throw new SubscribeException("server info to path is Error!, serverInfo:" + serverInfo);
       }
   }
   
   private String toProviderNode(ServerInfo serverInfo) {
       try {
           Map<String, Object> map = new HashMap<String, Object>();
           map.put("port", serverInfo.getPort());
           map.put("host", serverInfo.getHost());
           return  mapper.writeValueAsString(map);
       } catch (JsonProcessingException e) {
           throw new SubscribeException("server info to path is Error!, ServerInfo:" + serverInfo);
       }
   }
   
   private String toConfigurationBeanNode(ConfigurationBean serverInfo) {
       try {
           return  mapper.writeValueAsString(serverInfo);
       } catch (JsonProcessingException e) {
           throw new SubscribeException("server info to path is Error!, ServerInfo:" + serverInfo);
       }
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
     * @param
     * @return
     */
   private String addSlash(String path){
    	return Constants.PATH_SPLIT +path;
    }
    
    
    //构造URL配置的父节点  如：/xxx.yy.zz/provider
    private String toProviderPath(String appAndService){
    	String p = addSlash(appAndService);
    	p = p + Constants.PATH_SPLIT+ Constants.PATH_PROVIDER;
    	return p;
    }
    
//    //构造URL配置的父节点  如：/xxx.yy.zz/provider
    private String toConfiguratorsPath(String appAndService){
        String p = addSlash(appAndService);
        p =   p + Constants.PATH_SPLIT+ Constants.PATH_CONFIGURATION;
        return p;
    }
    
    //将配置信息拼装成：/xxx.yy.zz/provider/urlConfig 形式
//    private String getChildNode(String path,ServerInfo urlConfig) throws JsonProcessingException{
//    	String childPath = path + Constants.PATH_SPLIT + mapper.writeValueAsString(urlConfig);
//    	return childPath;
//    }
    
    //
    private Map<String,Configuration> toServiceInfoList(Map<String,String> configList) throws UnsupportedEncodingException,IOException{
    	if(configList == null || configList.isEmpty()){
    		return null;
    	}
    	Map<String,Configuration> urlList = new HashMap<String,Configuration>();
    	for(Entry<String, String> config : configList.entrySet()){
    	    
    	    //将json格式的字符串config转为ServerInfo对象urlConfig
    	    Configuration urlConfig = this.toConfiguration(config.getValue());
    		byte[] bs = config.getKey().getBytes("UTF-8");
    		StringBuffer c = new StringBuffer();
    		for(int i=0; i<bs.length; i++){
    			c.append(bs[i]);
    			if(i != bs.length -1){
    				c.append(',');	
    			}
    		}
    		//将URL串使用字节进行序列化，避免使用明文
    		urlList.put(c.toString(), urlConfig);
    	}
    	return urlList;
    }
    
    private Configuration toConfiguration(String configList) throws IOException{
        Configuration configuration = mapper.readValue(configList, Configuration.class);
    	return configuration;
    }
    
    private ConfigurationBean toConfigurationBean(String configList) throws IOException{
        ConfigurationBean configurationBean = mapper.readValue(configList, ConfigurationBean.class);
        return configurationBean;
    }
    
    
//    private ServerInfo toServerInfo(String configList) throws IOException{
//        ServerInfo serverInfo = mapper.readValue(configList, ServerInfo.class);
//        return serverInfo;
//    }
    /**
     * 修改负载和容错规则
     * @param applicationName
     * @param serviceName
     * @param lbtype
     * @param taultType
     * @param retries
     * @throws Exception
     */
    public void updateCluterWay(String applicationName, String serviceName, Constants.LoadBalanceType lbtype, Constants.FaultType taultType, int retries)
    		throws Exception{
        /**
         * 修改老数据
         */
        updateOldConfigurators(applicationName,serviceName);
        String appAndService = applicationName +  Constants.PATH_SPLIT + serviceName; 
        String configuratorsPath = toConfiguratorsPath(appAndService);
        
        if(!zkClient.exists(configuratorsPath)){
            this.create(configuratorsPath);
        }
//        PATH_CONFIGURATION
        List<String> configuratorsNodes = zkClient.getChildren(configuratorsPath);
        if(configuratorsNodes == null || configuratorsNodes.size() == 0){
            ConfigurationBean configurationBean = new ConfigurationBean();
            configurationBean.setFault(taultType);
            configurationBean.setLoadBalance(lbtype);
            configurationBean.setRetries(retries);
            configurationBean.setCategory(Constants.PATH_CONFIGURATION);
            String configurationBeanNode = toConfigurationBeanNode(configurationBean);
            this.create(configuratorsPath + Constants.PATH_SPLIT+ configurationBeanNode);
        }else{
            String configurationBeanNode = configuratorsNodes.get(0);
            ConfigurationBean configurationBean = toConfigurationBean(configurationBeanNode);
            configurationBean.setFault(taultType);
            configurationBean.setLoadBalance(lbtype);
            configurationBean.setRetries(retries);
            configurationBean.setCategory(Constants.PATH_CONFIGURATION);
            //删除在添加
            zkClient.delete(configuratorsPath + Constants.PATH_SPLIT+ configurationBeanNode);
            this.create(configuratorsPath + Constants.PATH_SPLIT+ toConfigurationBeanNode(configurationBean));
        }
    }
    /**
     * 查询服务列表
     * @param appName
     * @param uid
     * @return
     */
    public List<ServiceBean> queryServiceListByAppName(String appName,String uid){
    	String original =  appName ; 
    	List<ServiceBean> ls = new ArrayList<ServiceBean>();
    	if(appName == null){
    		return ls;
    	}
    	String path =  Constants.PATH_SPLIT+appName;
    	//服务列表
    	List<String> serviceList = zkClient.getChildren(path);
    	if(serviceList != null){
    		for(String s : serviceList){
    		    String appAndServiceName = path+ Constants.PATH_SPLIT+s;
        		Long date = zkClient.getCreationTime(appAndServiceName) ;
        		ServiceBean bean = new ServiceBean(s);
        		bean.setCreateTime(new Date(date));
        		bean.setParentNodeName(original);
        		//TODO
        		bean.setAccessable(queryAccessable(appAndServiceName));
        		ls.add(bean);
        	}
    	}
    	return ls;
    }
    
    /**
     * 修改存在serverinfo节点上的configuration数据
     * @param applicationName
     * @param serviceName
     */
    private void  updateOldConfigurators(String applicationName,String serviceName){
        String path = applicationName +  Constants.PATH_SPLIT + serviceName; 
        path = toProviderPath(path);
        
        if(!zkClient.exists(path)){
            return;
        };
        
        List<String> childen = zkClient.getChildren(path);
        if(childen == null || childen.size() <= 0){
            return;
        }
        for(String url : childen){
            try {
                //判断是否是老节点数据， 如果节点下还存在老数据
                if(url.contains("loadBalance")){
                  Configuration configuration = mapper.readValue(url,Configuration.class);
                  ServerInfo serverInfo = configuration.toServerInfo();
                  //添加新节点，删除老的节点
                  if(addServerInfo(applicationName, serviceName, serverInfo)){
                      String pa = path + Constants.PATH_SPLIT +  url;
                      if(zkClient.exists(pa)){
                          zkClient.delete(pa);
                      }
                  }
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
    /**
     * 查询禁止 开启参数
     * @param appAndServiceName
     * @return
     */
    private boolean queryAccessable(String appAndServiceName){
        List<String> urls = zkClient.getChildren(appAndServiceName + Constants.PATH_SPLIT+Constants.PATH_CONFIGURATION);
        if(urls != null && urls.size() > 0){
            String url = urls.get(0);
            try {
                JsonNode jsonNade = mapper.readTree(url).get(Constant.ACCESSABLE);
                if(jsonNade != null){
                    return jsonNade.asBoolean(true);
                } 
            } catch (Exception e) {
                LOG.error(e.getMessage(),e);
            }
        }
        
        List<String> config = zkClient.getChildren(appAndServiceName + Constants.PATH_SPLIT + Constants.PATH_PROVIDER);
        if (config != null && config.size() > 0) {
            String url = config.get(0);
            try {
                JsonNode jsonNade = mapper.readTree(url).get(Constant.ACCESSABLE);
                if (jsonNade != null) {
                    return jsonNade.asBoolean(true);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return true;
    }
 
}
