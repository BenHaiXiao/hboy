var sys_datatable_1_1 = false , sys_datatable_1_2 = false ;
function Sys_ShowServiceList(){
	//服务列表的DataTable只能被初始化一次
	if(!sys_datatable_1_1){
		$('#applist_table_1').dataTable({           
		    "aaSorting": [],
		    "aLengthMenu": [
				[1, 2, 3, -1],
				[1, 2, 3, "All"]  
		    ],
		    "iDisplayLength":8
		});
		sys_datatable_1_1 = true ; 
	}
}
/**
 * 展示当前用户具备权限能够看到的所有应用
 * @param keywords
 */
function getSystemAppList(key){
		var keywords = $("#sys_searchInput").val() ; 
//		if(key!=undefined){
//			keywords = key;
//		}
	$.ajax({
		url:'getAuthAppsByUidAsGrid.do',
		type: "POST",
		data:'keywords='+keywords,
		dataType:'json',
		success:function(responseText,opts){
			 templateFunc('#sysappListtbody1','sysappListscript1',responseText);  
			 templateFunc('#jxt_know_applist','all_userListscript_1',responseText);  
			 Sys_ShowServiceList();
			 
			//在增强表格上去掉控件自带的翻页，添加按钮新增服务
				$("#jxt-container-applist div.span6.table-list-count").remove();
				$("#applist_table_1_filter").width(890);
				//如果按钮已经存在则不再重复加载
				if($("button#jxt-addapplist-bt").length == 0 || $("button#jxt-returnapplist-bt").length ==0 ){
					var dom1 ="<button id='jxt-addapplist-bt' class='btn blue' style='background-color:#b4cef8;' onclick='gotoAppform()'>添加应用<i class='m-icon-swapright m-icon-white'></i></button>";
					var dom2 ="<button id='jxt-returnapplist-bt' class='btn blue' style='background-color:#b4cef8;display:none;' onclick='returnApplist()'>返回应用 <i class='m-icon-swapleft m-icon-white'></i></button>";
					var dom = dom1 + dom2 ; 
					//
					$("#tab_1_1 .dataTables_filter").append(dom);
				}
				//添加授权按钮  和 返回应用列表
				if($("button#jxt-addAuth-bt").length == 0 ){
					var dom ="<button id='jxt-addAuth-bt' class='btn blue ' style='margin-left:20px;background-color:#b4cef8;' onclick='gotoGrantAuthApp2Userform()'>用户授权<i class='m-icon-swapright m-icon-white'></i></button>";
					//var dom2 ="<button id='jxt-return2applist-bt' class='btn blue' style='margin-left:20px;background-color:#b4cef8;display:none;' onclick='returnApplist()'>返回应用 <i class='m-icon-swapleft m-icon-white'></i></button>";
					//
					$("#tab_1_1 .dataTables_filter").append(dom);
				}
				
				
		},error:function(data,opts){
			
		}
	});
}
/**
 * 点击应用列表中应用， 显示具备访问当前应用的用户
 * @param appName
 */
function showAuthUserList(appName){
	$("#tab_2_2_active_jxt").hide();
	$("#tab_2_2").hide();
	$("#bottom_inline_part").show();
	$("#tab_2_1_active_jxt").show();
	$("#tab_2_1").show();
	
	//修改被点击的行字体颜色
	$.each($("#sysappListtbody1 tr td"),function(index,obj){
		$(obj).css({"color":""});
	});
	$("tr[name='"+(appName)+"'] td:eq(0)").css({"color":"red"});
	$("tr[name='"+(appName)+"'] td:eq(1)").css({"color":"red"});
	
	//ajax请求 动态生成用户列表
	$.ajax({
		url:'getAuthUsersByAppName.do',
		data:'appName='+encodeURIComponent(appName),
		dataType:'json',
		type:'POST',
		success:function(responseText,opts){	
				var header = "<table class=\"table table-striped table-bordered table-hover dataTable \" id='userlist_table_2' aria-describedby=\"sample_2_info\" style=\"display:block;margin-left: 0px; width: 100%; border-right:1px solid #ebebeb;border-top:1px solid #ebebeb\">"
				+"<thead>"
					+"<tr role=\"row\" style=\"height: 0px;\">"
							 +"<th class=\"hidden-480\" style=\"line-height:10px;width:8% !important;text-align:center\">用户昵称</th>"
							 +"<th class=\"hidden-480\" style=\"line-height:10px;width:8% !important;text-align:center\">用户uid</th>"
							 +"<th class=\"hidden-480\" style=\"line-height:10px;width:8% !important;text-align:center\">用户通行证</th>"
			      			+"<th class=\"hidden-480\" style=\"line-height:10px;width:4% !important;text-align:center\">操作</th>"
					+"</tr>"
				+"</thead>" ; 
				var tbody = "<tbody id=\"sysuserListbody2\"  role=\"alert\" aria-live=\"polite\" aria-relevant=\"all\" >";
				for(var i = 0 ;!isEmpty(responseText) && i<responseText.length;i++){
					var jsonobj = responseText[i];
					var id = "td_app2user"+jsonobj.uid;
					var temp = "<tr>"
									+"<td class=\"hidden-480\" style=\"line-height:10px;width:8% !important;cursor:pointer\">"+jsonobj.nick +"</td>" 
									+"<td class=\"hidden-480\" style=\"line-height:10px;width:8% !important;cursor:pointer\">"+jsonobj.uid +"</td>"
									+"<td class=\"hidden-480\" style=\"line-height:10px;width:8% !important;cursor:pointer\">"+jsonobj.passport +"</td>"
									+"<td id='"+id+"'style=\"line-height:10px;text-align:center;width:4%\" class=\"hidden-480\"><a href=\"javascript:void(0);\" onclick=\"delAppAuth('"+jsonobj.uid+"','"+appName+"','app2user')\">删除授权</a></td>" 
						    +"</tr>";
					tbody +=temp;
				}
				tbody+="</tbody>";
				var dom = header + tbody+"</table>";
				var table_outer = $("#userlist-dataouter-div-2-1");
				table_outer.empty();
				table_outer.html(dom);
				$('#userlist_table_2').dataTable({  
				    "aaSorting": [],
				    "aLengthMenu": [
				        [1, 2, 3, -1],
				        [1, 2, 3, "All"] 
				    ],
				    "iDisplayLength":8
				});
				
		},error:function(responseText,opts){
			
		}
	});	
}
/**
 * 展示当前登录用户能够管理的所有用户
 */
function showAllCanAdminUsers(){
	$.ajax({
		url:'queryCanAdminUsers.do',
		dataType:'json',
		type:'POST',
		success:function(responseText,opts){
			templateFunc('#sysuserListtbody1','sysuserListscript1',responseText);  
			 if(!sys_datatable_1_2){
					$('#userlist_table_1').dataTable({           
					    "aaSorting": [],
					    "aLengthMenu": [
					        [1, 2, 3, -1],
					        [8, 2, 3, "All"] 
					    ],
					    "iDisplayLength":8
					});
					sys_datatable_1_2 = true;
			}
			 /* showKownedUsers() 的内容 公用同一个ajax请求，避免发起两次ajax请求  */
			 var selections = $(".sys_auth_kownedUsers");
				var domArray = []  ;
				for(var i = 0 ; !isEmpty(responseText) && i<responseText.length ; i++){
					var obj = responseText[i];
					var dom = 	"<option value='"+obj.uid+"'>"+obj.nick+"</option>" ; 
					domArray.push(dom);
				}
				selections.empty();
				selections.html(domArray.join(""));
				$(".sys_auth_kownedUsers option").each(function(index,dom){
					$(dom).css("display","block");
				});
			/* end */
		},error:function(responseText,opts){
			
		}
	});
	
}
/**
 * 点击用户列表中的用户，展示点击的用户 具备访问权限的应用 列表
 * @param userName 当前用户的相关参数
 */
function showAuthServiceList(uid){
	$("#tab_2_1_active_jxt").hide();
	$("#tab_2_1").hide();
	$("#bottom_inline_part").show();
	$("#tab_2_2_active_jxt").show();
	$("#tab_2_2").show();
	
	//修改被点击的行字体颜色
	$.each($("#sysuserListtbody1 tr td"),function(index,obj){
		$(obj).css({"color":""});
	});
	$("tr[name='"+(uid)+"'] td:eq(0)").css({"color":"red"});
	$("tr[name='"+(uid)+"'] td:eq(1)").css({"color":"red"});
	$("tr[name='"+(uid)+"'] td:eq(2)").css({"color":"red"});
	
	$.ajax({
		url:'getAuthAppsByUidAsGrid.do',
		data:'isSystem='+true+'&childuid='+uid,
		dataType:'json',
		type:'POST',
		success:function(responseText,opts){
			
			var header = "<table class=\"table table-striped table-bordered table-hover dataTable \" id='applist_table_2' aria-describedby=\"sample_2_info\" style=\"display:block;margin-left: 0px; width: 100%; border-right:1px solid #ebebeb;border-top:1px solid #ebebeb\">"
			+"<thead>"
				+"<tr role=\"row\" style=\"height: 0px;\">"
						 +"<th class=\"hidden-480\" style=\"line-height:10px;width:8% !important;text-align:center\">应用名称</th>"
						 +"<th class=\"hidden-480\" style=\"line-height:10px;width:8% !important;text-align:center\">中文名称</th>"
              			+"<th class=\"hidden-480\" style=\"line-height:10px;width:4% !important;text-align:center\">操作</th>"
				+"</tr>"
			+"</thead>" ; 
			var tbody = "<tbody id=\"sysappListbody2\"  role=\"alert\" aria-live=\"polite\" aria-relevant=\"all\" >";
			for(var i = 0 ;i<responseText.length;i++){
				var jsonobj = responseText[i];
				var enname = jsonobj.appName_EN.replace(/[^a-zA-Z0-9]/gi,"");
				var id = "td_user2app"+uid+enname;
				var temp = "<tr>"
								+"<td class=\"hidden-480\" style=\"line-height:10px;width:8% !important;cursor:pointer\">"+jsonobj.appName_EN +"</td>" 
								+"<td class=\"hidden-480\" style=\"line-height:10px;width:8% !important;cursor:pointer\">"+(jsonobj.appName_CN ||"--")+"</td>"
								+"<td id='"+id+"'style=\"line-height:10px;text-align:center;width:4% !important;\" class=\"hidden-480\"><a href=\"javascript:void(0);\" onclick=\"delAppAuth('"+uid+"','"+jsonobj.appName_EN+"','user2app')\">删除授权</a></td>" 
					    +"</tr>";
				tbody +=temp;
			}
			tbody+="</tbody>";
			var dom = header + tbody+"</table>";
			var table_outer = $("#servivelist-data-outer-div-2-2");
			table_outer.empty();
			table_outer.html(dom);
			$('#applist_table_2').dataTable({
			    "aaSorting": [],
			    "aLengthMenu": [
			        [1, 2, 3, -1],
			        [1, 2, 3, "All"] 
			    ],
			    "iDisplayLength":8
			});		
		}
	});
}
	// 根据uid 和 应用名称 删除授权信息
	 function delAppAuth(uid,appName,td){
		// var t = eval(_this);
		 $.ajax({
			url:'deleteAppAuth2User.do',
			data:'uid='+encodeURIComponent(uid)+"&appName="+encodeURIComponent(appName),
			dataType:'json',
			type:'POST',
			success:function(responseText,opts){
				if(responseText.success){
					if(td=='user2app'){
						var enname = appName.replace(/[^a-zA-Z0-9]/gi,"");
						$("#td_user2app"+uid+enname).parent("tr").remove();
					}
					if(td="app2user"){
						$("#td_app2user"+uid).parent("tr").remove();
					}
					//
					$.alert("删除成功!");
					return ;
				}else{
					$.alert("删除失败!");
				}
			},error:function(responseText,opts){
				$.alert("删除失败!");
			}
			 
		 });
		 
	 }
 
	
	//添加新的应用
	function gotoAppform(){
		//隐藏授权按钮
		$("#jxt-addAuth-bt").hide();
		//隐藏应用列表表格
		$("#applist_table_1_info").hide();
		$("#applist_table_1_paginate").hide();
		$("#applist_table_1_filter label:eq(0)").hide();
		$("#applist_table_1").hide();
		//隐藏添加应用按钮
		$("#jxt-addapplist-bt").hide();
		$("#jxt-returnapplist-bt").show();
		
		$("#jxt-app-addNewapp").show();
		$("#jxt-app-addNewapp").css("margin-top","-50px");
		
		//展示因为授权应用时隐藏的 checkbox

	}
	//添加应用务后,返回应用列表
	function returnApplist(){
		//隐藏添加应用 outer div
		$("#jxt-app-addNewapp").hide();
		
		//展示应用列表表格
		$("#jxt-returnapplist-bt").hide();
		$("#jxt-addapplist-bt").show();
		$("#applist_table_1_info").show();
		$("#applist_table_1_paginate").show();
		$("#applist_table_1_filter label:eq(0)").show();
		$("#applist_table_1").show();
		
		//隐藏授权div
		$("#jxt-app-grantAuth2User").hide();
		
		//展示添加应用授权按钮
		$("#jxt-addAuth-bt").show();
		
		
	}
	
	function gotoGrantAuthApp2Userform(){
		//隐藏应用表格
		$("#applist_table_1_info").hide();
		$("#applist_table_1").hide();
		$("#applist_table_1_paginate").hide();
		$("#applist_table_1_filter label:eq(0)").hide();
		
		//展示授权div form表单
		$("#jxt-app-grantAuth2User").show();
		//隐藏添加应用按钮
		$("#jxt-addapplist-bt").hide();
		//展示返回应用列表按钮
		$("#jxt-returnapplist-bt").show();
		//隐藏授权按钮
		$("#jxt-addAuth-bt").hide();
		$("#jxt-app-grantAuth2User").css("margin-top","-50px");
	}
	
	
	
	//展示user列表combox
	function ShowUserCombox(){
		var authContainer = $("#grant_auth_container");
		if($("#troggleShowUserCombox").is(':checked')){
			if($(".sys_auth_kownedUsers option").length == 0){
				showKownedUsers();
			}
			authContainer.show();
		}else{
			authContainer.hide();
		}
	}
	
	//添加应用时，下拉显示所有的用户
	function showKownedUsers(){
		$.ajax({
			url:'queryCanAdminUsers.do',
			dataType:'json',
			type:'POST',
			 timeout:1000*60*10,
			success:function(responseText,opts){
				var selections = $(".sys_auth_kownedUsers");
				var domArray = []  ;
				for(var i = 0 ; !isEmpty(responseText) && i<responseText.length ; i++){
					var obj = responseText[i];
					var dom = 	"<option value='"+obj.uid+"'>"+obj.nick+"</option>" ; 
					domArray.push(dom);
				}
				selections.empty();
				selections.html(domArray.join(""));
				$(".sys_auth_kownedUsers option").each(function(index,dom){
					$(dom).css("display","block");
				});
			},error:function(responseText,opts){
				
			}
		});
	}
	//下拉框选择事件
	function getSelected(){
		var sel = $("#sys_auth_kownedUsers option:selected")[0];
		var text = $(sel).text();
		$("#grantAuth_Username").val(text);
		//选中时，将选中的值存入到hidden中
		$("#realSelValue").val($(sel).val());
	}

	
	// 添加新应用，提交按钮 submmit事件
	function addNewApp(){
		var checked = $("#troggleShowUserCombox").is(':checked');
		var  selOpts = $($("#sys_auth_kownedUsers option:selected")[0]);
		var appName_EN = $("#appName_EN").val();
		var appName_CN = $("#appName_CN").val();
		
		if(isEmpty(appName_EN)||isEmpty(appName_CN)){
			$.alert("请完整填写参数!");
			return ;
		}
		var realInput = $("#realSelValue");
		//如果选中项text不等于input值，则说明人为输入了其他值，此时将hidden的值置为实际输入值
		if($("#grantAuth_Username").val() != selOpts.text()){
			if(checked){
				var regxs = /^[0-9a-zA-Z]+@yy\.com$/gi ; 
				var flag = regxs.test($("#grantAuth_Username").val());
				if(!flag){
					$.alert("邮箱为xxx@yy.com，正确填写!");
					return;
				}
			}
			realInput.val($("#grantAuth_Username").val());
		}
		var value = realInput.val() ||"";
		
		//输入内容校验正则表达式
		var regex = /^\d[\d]+\d$|^[0-9a-zA-Z]+@yy\.com$/gi;
		
		var datajson = {
				'appName_CN':appName_CN,
				'appName_EN':appName_EN
		};
		if(checked){
			if(isEmpty(value) || !regex.test(value.trim()) ){
				$.alert("邮箱为xxx@yy.com，正确填写!");
				return ;
			}else{
				//如果自行输入，且通过验证则在数据中 动态添加userInfo属性
				datajson.userInfo = value;
			}
		}

		$.ajax({
			url:'createApplication.do',
			dataType:'json',
			data:datajson,
			type:'POST',
			success:function(responseText,opts){
				if(responseText.createApp && !isEmpty(datajson.userInfo) && responseText.grantAuth){
					$.alert("创建应用,并授权成功!",gotoSystePage);
					return ;
				}
				if(responseText.createApp){
					if(!checked){
						$.alert("创建应用成功",gotoSystePage);
						return ;
					}
					if(checked &&(isEmpty(datajson.userInfo) || !responseText.grantAuth)){
						$.alert("创建应用成功，但授权失败!",gotoSystePage);
						return ;
					}
					
				}
				getSystemAppList();
				return ;
				if(!responseText.createApp){
					$.alert("创建应用失败!");
					return ;
				}
			},error:function(responseText,opts){
				alert("创建应用失败!");
				return ;
			}
		});
	}
	//下拉框选择事件
	function getSelected2(){
		var sel = $("#sys_auth_kownedUsers2 option:selected")[0];
		var text = $(sel).text();
		$("#grantAuth_Username2").val(text);
		//选中时，将选中的值存入到hidden中
		$("#realSelValue2").val($(sel).val());
	}

	/**
	 * 添加应用的授权信息
	 */
	function grantAppAuth2User(){
		var selAppOpts = $("#jxt_know_applist option:selected");
		var selappName_EN = selAppOpts.val();
		var realInput = $("#realSelValue2");
		//如果选中项text不等于input值，则说明人为输入了其他值，此时将hidden的值置为实际输入值
		var userSelOpts = $("#sys_auth_kownedUsers2 option:selected");
		if($("#grantAuth_Username2").val() != userSelOpts.text()){
			var regxs = /^[0-9a-zA-Z]+@yy\.com$/gi ; 
			var flag = regxs.test($("#grantAuth_Username2").val());
			if(!flag){
				$.alert("邮箱为xxx@yy.com，正确填写!");
				return;
			}
			realInput.val($("#grantAuth_Username2").val());
		}
		var value = realInput.val() ||"";
		var regex = /^\d[\d]+\d$|^[0-9a-zA-Z]+@yy\.com$/gi;
		var datajson = {
				'appName_EN':selappName_EN
		};
		if(isEmpty(value) || !regex.test(value.trim())){
			$.alert("邮箱为xxx@yy.com，正确填写!");
			return ;
		}else{
			//如果自行输入，且通过验证则在数据中 动态添加userInfo属性
			datajson.userInfo = value;
		}
		if(value.trim().indexOf("@yy.com")>-1){
			//邮箱
			datajson.addType = 2 ;
		}else{
			//uid
			datajson.addType = 1 ;
		}
		$.ajax({
			url:'addAppAuth2User.do',
			data:datajson,
			dataType:'json',
			type:'POST',
			success:function(responseText,opts){
				if(responseText.success){
					$.alert("授权成功!");
					getSystemAppList();
					returnApplist();
					return ;
				}else{
					$.alert("授权失败!");
					return ;
				}
			},error:function(responseText,opts){
				$.alert("授权失败!");
				return ;
			}
		});
	}
	/**
	 * 删除应用，同时删除与当前应用有关联的所有授权信息
	 * @param appName_EN
	 */
	function delApp(appName_EN){
		if(isEmpty(appName_EN)){
			return ;
		}
		$.ajax({
			url:'deleteApplication.do',
			data:"applicationName="+appName_EN,
			dataType:'json',
			type:'POST',
			success:function(responseText,opts){
				var code = responseText.code;
				if(code == 0){
					$.alert("存在子节点，不能删除");
					return ;
				}if(code == -1){
					$.alert("删除失败!");
					return ;
				}
				if(code == 1){
					$("#td_app_"+appName_EN).parent("tr").remove();
					$.alert("删除成功!",gotoSystePage);
					return ;
				}
			},error:function(responseText,opts){
				$.alert("删除失败!");
				return ;
			}
		});
	}
	
	
	//app列表搜索
	function sys_searchApp(){
		$("#sys_searchInput").keydown(function(event){
	    	if(event.keyCode == 13){
	    		getSystemAppList();
	    		showTabs(1);
	    	}
	    });
		$("#search_app_bt").bind("click",function(){
				getSystemAppList();
	    		showTabs(1);
		});
   }
	
	
  function showTabs(index){
	  $('#bottom_inline_part').hide();
	  if(index == 1){
		  $("#tab_2_active_jxt").removeClass("active");
  		  $("#tab_1_active_jxt").addClass("active");
		  $("#tab_1_2").hide();
		  $("#jxt-app-addNewapp").hide();
		  $("#jxt-app-grantAuth2User").hide();
		  $("#tab_1_1").show();
		  $("#jxt-container-applist").show();
		  $("#applist_table_1").show();
		  $("#jxt-addapplist-bt").show();
		  $("#jxt-addAuth-bt").show();
		  $("#jxt-returnapplist-bt").hide();
		  
	  }else{
		  $("#tab_1_active_jxt").removeClass("active");
  		  $("#tab_2_active_jxt").addClass("active");
		  $("#tab_1_1").hide();
		  $("#jxt-addapplist-bt").hide();
		  $("#jxt-addAuth-bt").hide();
		  $("#tab_1_2").show();
		  $("#jxt-container-userlist").show();
		  $("#userlist_table_1").show();
		  showAllCanAdminUsers();
	  }
  }
  
  function triggerUpdateApp(appName_EN,appName_CN){
	  $("#updateApp_appOldNameText").val(appName_CN||"空值");
	  $("#appNameText_EN").val(appName_EN);
	  $.dialog({
	        title: '修改应用中文名称', 
	        content: $('#updateApp')[0],
	        id:'showUpdateAppDialog'
	    });
  }
  
  function commitUpdateApp(){
	  var appName_EN = $("#appNameText_EN").val();
	  var appName_CN = $("#updateApp_appNewNameText").val();
	  if(isEmpty(appName_CN)){
		  return ;
	  }
	  var datajson = {
			  'appName_EN':$.trim(appName_EN),
			  'appName_CN':$.trim(appName_CN)
	  };
	  $.ajax({
		 url:'./updateAppName.do' ,
		 data:datajson,
		 dataType:'json',
		 type:'POST',
		 timeout:10*1000*60,
		 success:function(responseText,opts){
			 var obj = JSON.parse(responseText);
			 var status = obj.status;
			 if(status == 200){
				 $.alert("修改成功!",getSystemAppList);
				 $.dialog.get('showUpdateAppDialog').close();
				 return ;
			 }else if(status==500){
				 $.alert("修改失败!");
				 return ;
			 }else if(status==404){
				 $.alert("应用不存在");
				 return;
			 }else if(status==501){
				 $.alert("参数错误!");
				 return ;
			 }else{
				 $.alert("修改失败!");
				 return ;
			 }
		 },error:function(responseText,opts){
			 $.alert("修改失败!");
			 return ;
		 }
	  });
	  
  }
//  / $.dialog.get('showUpdateAppDialog').close()
  
  
  
	
	