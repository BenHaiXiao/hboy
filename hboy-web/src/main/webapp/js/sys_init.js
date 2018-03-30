//$(function(){
//	
//	
//});

//function menu(){
//	$.ajax({
//		url:'/validateRoot.do',
//		type:'GET',
//		dataType:'json',
//		success:function(data,opts){
//			var obj = JSON.parse(data);
//			//开发环境 显示添加应用
//			if(obj.isRoot){
//				var nav = $("#jxt_menunav");
//				var dom = "<li class= '' id='jxt_navmenu_system'><a href='/system.do'>"
//							+"系统管理 <span class = 'selected'></span>"
//							+"</a></li>";
//				$("#jxt_navmenu_system").remove();
//				nav.append(dom);
//				$("#jxt_dev_newapp_bt").show();
//			}
//			
//			if(obj.isDev){
//				$("#jxt_dev_newapp_bt").show();
//			}
//			
//		}
//	}) ;
//}


function menu(activeTab){
	$.ajax({
		url:'/validateRoot.do',
		type:'GET',
		dataType:'json',
		success:function(data,opts){
			var obj = JSON.parse(data);
			if(!obj.isRoot){
				$("#jxt_navmenu_system").remove();
			}else{
				$("#jxt_dev_newapp_bt").show();
			}
			
			if(obj.isDev){
				$("#jxt_dev_newapp_bt").show();
			}
			
			if(activeTab == "home"){
				$("#jxt_navmenu_home").addClass("active");
			}
			if(activeTab == "system"){
				$("#jxt_navmenu_system").addClass("active");
			}
			if(activeTab == "monitor"){
				$("#jxt_navmenu_monitor").addClass("active");
			}
		}
	}) ;
}

/**
* 各个系统的初始化函数，例如：菜单、昵称、ip地址，以及放一些公共函数 by zenglican 2013-11-07
*/
// 快速通道
function listApps(){ 
  //  if( !sessionStorage.listApps){
        $.getJSON("/common/listUserApp.do",function(json){
            if(!json.success) {
                $.alert(json.message);
                return;
            }
            var apps = json.object, html = '';
            sessionStorage.listApps = JSON.stringify(apps);
            $.each(apps,function(){
                html += '<li><a href="'+this.url+'" target="_blank">'+this.cn_name+'</a></li>';
            });
            $("#J_appList").html(html);
        }); 
//    } else{ 
//    	//存在
//        var html = '';
//        var listApps = sessionStorage['listApps'];
//        var apps = $.sysop.kit.str2Obj(listApps);
//        $.each(apps,function(){
//            html += '<li><a href="' + this.url + '" target="_blank">' + this.cn_name + '</a></li>';
//        })
//        $("#J_appList").html(html);
//    }       
}
// 用户名
function loadUserInfo(){ 
 //   if( !sessionStorage.userName){
        $.getJSON("/common/loadUserInfo.do",function(json){
            if(!json.success) {
                $.alert(json.message);
                return;
            }
            var user = json.object;
            $("#userInfo").html(user.nick_name);
            sessionStorage.userName = user.nick_name;
        });     
//    } else{
//        $("#userInfo").html(sessionStorage.userName);
//    }
}
// IP地址
function loadServerIp(){ 
 //   if( !sessionStorage.ips){
    	$.ajax({
    		url:'/common/loadServerIp.do',
    		dataType:'json',
    		success:function(responseText){
    			var data = responseText.host;
    			 $("#serverIps").html(data);
    			 sessionStorage.ips  = data ; 
    		}
    	});
//    } else{
//        $("#serverIps").html(sessionStorage.ips);
//    }
}
// 退出
function closeWindow(){ 
    $('#J_logout').on('click',function(){
        if( !confirm('确定退出？')){
            return;
        }
        sessionStorage.userName = '';
        sessionStorage.listApps = '';
        sessionStorage.menuData = '';
        // 清除cookie
        systemLogout();
        // $.post('/logout.do',function(data){
        //     console.log(data)
        // })
        /*window.open('', '_self', '');
        window.close();*/
    })
}
function systemLogout(){
    var host_name = window.location.hostname;
    $.removeCookie('sysop_privilege_user_name',{domain:'duowan.com',path:'/'});
    $.removeCookie('sysop_privilege_user_name',{domain:host_name,path:'/'});
    // console.log($.cookie('sysop_privilege_user_name'))
    location.reload();
}
// loading effect
function loadingEffect(){
    $.sysop.popup.init();
}
// 导航菜单高亮
var menuHighlight = {
    save:function(container){
        var trigger = $(container).find('.J_menu_item_trigger');
        $(container).on('click','.J_menu_item_trigger',function(e){
            sessionStorage.currentMenuId = $(this).attr('id');
        })
    },
    init:function(){
        var _id = sessionStorage.currentMenuId;
        if(_id !== undefined){
            var p_id = _id.slice(0,_id.lastIndexOf('_'));
            $('#' + _id).closest('li').addClass('active');
            $('#' + p_id).addClass('active open');
            $('#' + p_id).children('a').find('.arrow').addClass('open');
        } 
    }
}
