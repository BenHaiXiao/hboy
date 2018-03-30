package com.github.hboy.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 */
public class Constant {
	
	protected static final Logger LOG = LoggerFactory
			.getLogger(Constant.class);

	/*测试用,上线时请向 陈征 申请属于自己系统的appid/appkey*/
	public static final String APPID = "5279";//udb分配的appid，即系统标识
	public static final String APPKEY = "JSHNqFfiwu3QLDfjIcPRnOvnvHv3R4QD";//appid对应的密码
	
	public static final String AUTH_APP_USER_REL ="/AppUserRelManage" ;   //用户与应用管理顶级节点
	
	public static final String AUTH_USER_TO_APP_REL =AUTH_APP_USER_REL+"/User2App" ;   //用户：应用  关系顶级节点
	
	public static final String AUTH_APP_TO_USER_REL =AUTH_APP_USER_REL+"/App2User" ;   //应用：用户 关系顶级节点
	 /**
	  * 用户管理顶级节点
	  */
	 public static final String AUTH_USER_MANAGE_NODE ="/UserManage" ;  
	 
	 public static final String ROOT = "/Root" ; 
	 
	 public static final String MANAGER = "/Manager" ; 
	 
	 
	 private static  Map <String,String> map = new HashMap<String,String>();
	 
	 public static int ERROR_ARGUMENTS_CODE  =500 ; 
	 
	 public static int NO_AVAILIABE_VALUE = 201 ; 
	 
	 public static int SUCCESS_CODE = 200 ;  
	 
	 public static final String  ACCESSABLE                      = "accessable";
	 
	 /**
	  * 判断字符串 是否 不为空 值
	  * @param str
	  * @return
	  */
	 
	 public static boolean isNotEmpty(String str){
		 if(str==null){
			 return false;
		 }else if("".equals(str.trim())){
			 return false ; 
		 }else if("null".equals(str.trim())){
			 return false ; 
		 }else if("'null'".equals(str.trim())){
			 return false ; 
		 }else if("undifined".equals(str.trim())){
			 return false ; 
		 }else{
			 return true ; 
		 }
	 }
	 
	/**
	 * 将 list转化成 字符串
	 * @param ls
	 * @param splitprex :分隔符
	 * @return
	 */
	 public static String List2StringHelper(List<String> ls,String splitprex){
		 if(ls==null) return "" ; 
		 StringBuffer buffer = new StringBuffer();
		 
		 for(String str : ls){
			 buffer.append(str).append(splitprex) ; 
		 }
		 
		 return buffer.toString() ; 
	 }
	 
	 
	 public static String GenExtTreeJsonData(List<String> ls){
		 List list = new ArrayList();
		 if(ls != null){
			 for(int i = 0;i<ls.size();i++){
					String str = ls.get(i) ; 
					Map map = new HashMap();
					 map.put("text",str);
					 map.put("leaf", true);
					// {
						 map.put("checked",false);
					// }
					 
					 list.add(map);
				}
		 }
		 String str = Json.ObjToStr(list) ; 
		 LOG.info("JSON = "+str) ; 
		 return str;
	 }
	 
	 
	 public static String getPropertyValue(String key){
		 if(map.get(key)==null){
			 ResourceBundle rb =  ResourceBundle.getBundle("configs/service-web");
				if(rb.containsKey(key)){
					map.put(key,rb.getString(key)) ; 
				}
		 }
		 String s = map.get(key);
		 if(s == null) {
			 s = "" ; 
		 }
		 return s.trim(); 
	 }
	 
	 
}

