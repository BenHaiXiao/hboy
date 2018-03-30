package com.github.hboy.web.util;

public enum StatusCode {
	
	/**
	 * 处理成功
	 */
	OK(200),

	
	/**
	 * 页面不存在
	 */
	 NOT_EXISIT(404),
	 /**
	  * 请求成功，但没有可用的值
	  */
	 NO_AVAILABLE_VALUE(201),
	 /**
	  * 内部程序异常错误
	  */
	 INTERVAL_ERROR(500),
	 /**
	  * 服务器不可用
	  */
	 SERVER_UNAVAILABLE(503 ),
	 /**
	  * 网关超时
	  */
	 GATEWAY_ERROR(502),
	 /**
	  * 网关错误
	  */
	 GATEWAY_TIMEOUT(504),
	 /**
	  * 请求参数错误
	  */
	 INVALID_ARGUMENTS(501),
	 /**
	  * 没有授权
	  */
	 NO_GRANT_ANTH(401);
	 
	private final int value ; 
	
	StatusCode(int value){
		this.value = value ; 
	}
	
	public int getValue(){
		return this.value ; 
	}
	
}
