//定义全局变量

// 当前服务名称
var GL_jxt_ServiceName ;

// 当前应用名称
var GL_jxt_appName ; 

var GL_jxt_loadBalance="ROUND" ; 

var GL_jxt_faultType="FAILOVER" ; 

var GL_jxt_retries=3 ; 


var chars = ['0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
/**
 * 产生随机数
 * @param n
 * @returns {String}
 */
function generateMixed(n) {
     var res = "";
     for(var i = 0; i < n ; i ++) {
         var id = Math.ceil(Math.random()*35);
         res += chars[id];
     }
     return res;
}

function isEmpty(obj){
	if($.isEmptyObject(obj)){
		return true;
	} 
	if($.isEmptyObject($.trim(obj))){
		return true ; 
	}
	if(obj == undefined){
		return true;
	}
	if(obj == null){
		return true  ;
	}
	if(obj == 'null'){
		return true ; 
	}
	if(obj == 'undefined'){
		return true;
	}
	if(obj=="" || "" == $.trim(obj)){
		return true;
	}
}

function gotoHomePage(){
	window.location.href="/home.do";
	//window.setTimeout(function(){window.location.href="./home.do";}, 500);
}
function gotoSystePage(){
	window.location.href="/system.do";
}

