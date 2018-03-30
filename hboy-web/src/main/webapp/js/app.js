$(function(){

});
var servicelist_table_1 = false ;
function getAppList(keywords){
	   		var keywords = $("#searchInput").val() ; 
        	$.ajax({
        		url:'getAuthAppsByUidAsGrid.do',
        		type: "POST",
        		data:'keywords='+keywords,
        		dataType:'json',
        		success:function(responseText,opts){
        			 templateFunc('#appList-tbody','appList-script',responseText);  
        		},error:function(data,opts){
    				
    			}
        	});
   }
 
   
    function showNodeConfigList(serviceName,parentNodeName){
    	if(isEmpty(serviceName) ||isEmpty(parentNodeName) ){
    		$.alert("没有选择正确的应用和服务!");
    		return ;
    	}
    	//设置全局变量  当前服务名称 的值
    	GL_jxt_serviceName = serviceName ;
    	
    	//修改被点击的行字体颜色
    	
    	$.each($("#serviceList-tbody tr td"),function(index,obj){
    		$(obj).css({"color":""});
    	});
    	$("tr[name='"+(parentNodeName+serviceName)+"'] td:eq(0)").css({"color":"red"});
    	
    	//发起ajax 显示当前服务的节点配置信息
    	$.ajax({
    		url:'./queryServerInfo.do',
    		type:'POST',
    		dataType:'json',
    		data:'appName='+parentNodeName+"&serviceName="+serviceName,
    		success:function(responseText,opts){
    			
    			if(responseText==500){
    				$.alert("参数错误!");
    				templateFunc('#nodeconfigList-tbody','nodeconfigList-script',null);
    				return ;
    			}
    			if(responseText==201){
    				$("#jxt-container-nodelist").show();
    				templateFunc('#nodeconfigList-tbody','nodeconfigList-script',null);
    				return ; 
    			}
    			templateFunc('#nodeconfigList-tbody','nodeconfigList-script',responseText);
    			
    			//控制容错 和 负载
    			var balance_rule = responseText[0].loadBalance ; 
    			var faultType = responseText[0].fault ;
    			if(balance_rule != null && balance_rule != ""){
    				GL_jxt_loadBalance  = balance_rule; 
    			}
    			if(faultType != null && faultType != ""){
    				GL_jxt_faultType = faultType ; 
    			}

    			if(balance_rule=='ROUND'){
    				$("#jxt-balance_rule").html("轮询选择(ROUND)");
    				//控制负载如果是轮询选择 则不显示 权重列
    				 $("table#nodeListTable tr th:eq(2)").hide();
    				 var arr = $("#nodeconfigList-tbody tr");
    				 $.each(arr,function(index,obj){
    					 $(obj).children("td:eq(2)").hide();
    				 });
    				 
    			}else{
    				$("#jxt-balance_rule").html("随机选择(RANDOM)");
    				
    				//控制负载如果是权重选择 则显示 权重列
    				$("table#nodeListTable tr th:eq(2)").show();
    				 var arr = $("#nodeconfigList-tbody tr");
    				 $.each(arr,function(index,obj){
    					 $(obj).children("td:eq(2)").show();
    				 });
    				 
    			}
    			if(faultType =='FAILFASE'){
    				$("#jxt-fault_rule").html("失败返回(FAILFALSE)");
    			}else if(faultType =='FAILOVER'){
    				// 设置失败从属次数
    				 GL_jxt_retries  = responseText[0].retries; 
    				$("#jxt-fault_rule").html("失败重试(FAILOVER)&nbsp;&nbsp;&nbsp;| &nbsp;&nbsp;&nbsp; 重试次数："+GL_jxt_retries );
    				
    				
    				
    			}else if(faultType=='FAILTIME'){
    				$("#jxt-fault_rule").html("间隔屏蔽(FAILTIME)");
    			}else{
    				$("#jxt-fault_rule").html("失败重试(FAILOVER)");
    			}
    			
    			//展示table
    			$("#jxt-container-nodelist").show();
    			
    			//控制表格均匀分布
//    			var arr = $("table#nodeListTable tr").children("th:visible");
//    			var len = arr.length;
//    			var tablesize = $("#nodeListTable").width();
//    			$("#nodeListTable thead").width(tablesize);
//    			var average = tablesize/(len+1) ; 
//    			$("table#nodeListTable tr th").width(average);
//    			$($("table#nodeListTable tr").children("th").last()[0]).width(tablesize-(average*(len-1)));
    			
    			//控制表格内容居中
    			$("#nodeListTable th").css({"text-align":"center"});
    			$("#nodeListTable td").css({"text-align":"center"});
    			
    		},error:function(responseText,opts){
    			
    		}
    	});
    }
    function delNodeConfig(appName,serviceName,path,_this){
    	if(isEmpty(serviceName) ||isEmpty(appName)|| isEmpty(path)){
    		alert("参数错误");
    		return ;
    	}
    	
    	$.confirm('确认删除当前配置节点',function(){ // 点击确定
    		$.ajax({
			  	  type:"POST",
			  	  url : "./deleteConfig.do",
			  	  dataType: 'text',
			  	  data:{
			  		  	path:path,
			  		    serviceName:serviceName,
			  		    applicationName:appName
			  	     },
			  	  success: function(data){
			  		var dataJson = JSON.parse(data);
			  		  if(dataJson['code'] == 1){
			  			//$("#"+path).parent('tr').remove();
			  			$.alert("删除操作成功!");
			  			$(_this).parent("td").parent("tr").remove();
						return ;
			  		  }else{
			  			$.alert("删除操作失败!");
			  		  };
			  	  },
			  	  error: function (data, status, e){  
			  		  $.alert("删除操作失败");
			   	   } 
			  	});	
        });
    }
    
    function UpddateNodeConfig(appName,serviceName,path){
    	if(isEmpty(serviceName) ||isEmpty(appName)|| isEmpty(path)){
    		$.alert("参数错误");
    		return ;
    	}
    	var data = getfillFormData();
    	data.path = path ; 
    	$.ajax({
		  	  type:"POST",
		  	  url : "./updNodeConfig.do",
		  	  dataType: 'json',
		  	  data:data,
		  	  success: function(data){
		  		  var obj  = JSON.parse(data);
		  		  if(obj.code == 200){
		  			$.alert("修改操作成功!");
		  			showNodeConfigList(serviceName, appName);
		  			$.dialog.get('updateNodeDialog').close();
					return ;
		  		  }else{
		  			$.alert("修改操作失败!");
		  			return ;
		  		  };
		  	  },
		  	  error: function (data, status, e){  
		  		  $.alert("修改操作失败");
		  		  return ;
		   	   } 
		  	});
    }
    
    //渲染模板
	function templateFunc(container,tmplId,json){
	    var html = template.render(tmplId, {list:json});
	    $(container).html(html);
	}
	 
	function showService(appName){
		//设置全局变量 当前点击应用的名称
		GL_jxt_appName = appName ;
		
		//发起ajax请求获取当前应用的服务列表
		$.ajax({
			url:'queryServiceListByAppName.do',
			type: "POST",
			dataType:'json',
			data:"appName="+appName,
			success:function(data,opts){
				generateServiceDom(data);
			},error:function(data,opts){
				
			}
		});
		//输入框为当前点击的应用名称
		$("#searchInput").val(GL_jxt_appName);
	}
	function delApp(appName){
		
	}
	

	
	
	/**
	 * 渲染服务列表
	 * @param data
	 */
	function generateServiceDom(data){
		var header = "<table class=\"table table-bordered table-hover\" id=\"servicelist_table_1\"  aria-describedby=\"sample_2_info\" style=\"display:none;margin-left: 0px;  border-top:1px solid #ebebeb\">"
					+"<thead>"
					+		"<tr role=\"row\" style=\"height: 0px;\">"
					+ 			"<th class=\"hidden-480\" style=\"text-align:center;witdh:8% !important;line-height:10px;\">服务名称</th>"
					+ 			"<th class=\"hidden-480\" style=\"text-align:center;witdh:2% !important;line-height:10px;\">使用状态</th>"
          			+			"<th class=\"hidden-480\" style=\"text-align:center;witdh:5% !important;line-height:10px;\">操作</th>"
          			+		"</tr>"
					+"</thead>" ; 
					
		//var header="";
		var tbody ="<tbody id=\"serviceList-tbody\"  role=\"alert\" aria-live=\"polite\" aria-relevant=\"all\" >"; 
		//var tbody =""; 
		for(var i = 0;  !isEmpty(data) && i <data.length;i++){
			var obj = data[i];
			var status = "使用中";
			var statusButton="禁止";
			var accessable = false;
			if(obj.accessable == false){
				status = "禁止"
				statusButton="开启";
				accessable = true;
			}
			var t = "<tr  name='"+(obj.parentNodeName+obj.serviceName)+"'>" +
						"<td class='hidden-480' style='cursor:pointer;line-height:10px;width:8%;' onclick=\"showNodeConfigList('"+obj.serviceName+"','"+obj.parentNodeName+"')\">"+obj.serviceName+"</td>"+
						"<td class='hidden-480' style='cursor:pointer;line-height:10px;width:3%;text-align:center;'>"+status+"</td>"+
						"<td style='text-align:center;line-height:10px;width:5%;'>" +
						"<a href='javascript:void(0);' onclick=\"delService('"+obj.serviceName+"','"+obj.parentNodeName+"')\">删除</a>" + " | "+
						"<a href='javascript:void(0);' onclick=\"updateAccessable('"+obj.serviceName+"','"+obj.parentNodeName+ "','" + accessable + "')\" > <span style='color:red;' >"+statusButton+"</span></a></td>" +
					"</tr>" ; 
			tbody+=t ; 
		}
		tbody+="</tbody></table>";
		var jq = $("#table_data_outer_1") ; 
		//var jq = $("#serviceList-tbody") ; 
		jq.empty();
		jq.html(header+tbody);
		//if(!servicelist_table_1){
			$('#servicelist_table_1').dataTable({           
			    "aaSorting": [],
			    "aLengthMenu": [
			        [5, 15, 20, -1],
			        [5, 15, 20, "All"] 
			    ],
			    "iDisplayLength": 10
			});
			//servicelist_table_1 = true ; 
		//}
		
		$("#jxt-container-applist").hide();
		$("#jxt-container-servicelist").show();
	//	$('#servicelist_table_1'+randomId).show();
		$('#servicelist_table_1').show();
		
		//在增强表格上去掉控件自带的翻页，添加按钮新增服务
		$("div.span6.table-list-count").remove();
		//如果按钮已经存在则不再重复加载
		if($("button#jxt-addService-bt").length ==0 || $("button#jxt-returnServicelist-bt").length ==0 ){
			var dom1 ="<button id='jxt-addService-bt' class='btn blue' style='background-color:#b4cef8;' onclick='gotoService()'>添加服务<i class='m-icon-swapright m-icon-white'></i></button>";
			var dom2 ="<button id='jxt-returnServicelist-bt' class='btn blue' style='background-color:#b4cef8;display:none;' onclick='returnServiceList()'>返回服务 <i class='m-icon-swapleft m-icon-white'></i></button>";
			var dom = dom1 + dom2 ; 
			//
			$(".dataTables_filter").append(dom);
		}
	};
	
	//禁止服务
	function updateAccessable(serviceName,appName,accessable){
		$.ajax({
			url:'./updateAccessable.do',
			dataType:'text',
			data:'serviceName='+serviceName+"&appName="+appName+"&accessable="+accessable,
			type:'POST',
			success:function(data,opts){
				var dataJson = JSON.parse(data);
				if (dataJson['code'] == 1) {
			//		$("tr[name='"+(appName+serviceName)+"']").remove();
					$.alert("修改成功!");
					//TODO://刷新dom
					showService(appName);
					return ;
				} else if(dataJson['code'] == -2) {
					$.alert("参数错误");
				}else{
					$.alert("修改失败!");
				}
			},
			error:function(responseText,opts){
				$.alert("删除失败!");
			}
		});
	}
	
	//删除服务
	function delService(serviceName,appName){
		$.ajax({
			url:'./deleteService.do',
			dataType:'text',
			data:'serviceName='+serviceName+"&appName="+appName,
			type:'POST',
			success:function(data,opts){
				var dataJson = JSON.parse(data);
				if (dataJson['code'] == 1) {
					$("tr[name='"+(appName+serviceName)+"']").remove();
					$.alert("删除成功!");
					//TODO://刷新dom
					showService(appName);
					return ;
				} else if(dataJson['code'] == 0) {
					$.alert("该服务还存在节点，不能删除!");
				}else{
					$.alert("删除失败!");
				}
			},
			error:function(responseText,opts){
				$.alert("删除失败!");
			}
		});
	}

	//添加新服务
	function addNewService(){
		var serviceName = $("#servicenameText").val();
		var appName= GL_jxt_appName;
		if(isEmpty(appName)){
			$.alert("应用名称不能为空");
			return ;
		};
		if(isEmpty(serviceName) ){
			$.alert("服务名称不能为空");
			return ;
		};
		$.ajax({
			url:'./createService.do',
			type: "POST",
			dataType:'text',
			data:"appName="+appName+"&serviceName="+serviceName,
			success:function(data,opts){
				var dataJson = JSON.parse(data);
				if (dataJson['code'] == 200) {
					$.alert("添加服务成功",returnServiceList(appName));
					return ;
				} else{
					$.alert("添加服务失败!");
				}
			},error:function(data,opts){
				alert("添加服务失败!");
			}
		});
	};
	
	//添加新的服务
	function gotoService(appName){
		$("#servicelist_table_1_info").hide();
		$("#servicelist_table_1_paginate").hide();
		$("#servicelist_table_1_filter label:eq(0)").hide();
		$("#jxt-addService-bt").hide();
		$("#jxt-returnServicelist-bt").show();
		$("#servicelist_table_1").hide();
		$("#jxt-service-addNewnode").show();
		//输入框为当前点击的应用名称
		$("#searchInput").val(GL_jxt_appName);
		$("#appnameText").val(GL_jxt_appName);
		
	}
	//添加新服务后,返回服务列表
	function returnServiceList(appName){
		$("#jxt-service-addNewnode").hide();
		$("#jxt-returnServicelist-bt").hide();
		$("#jxt-addService-bt").show();
		$("#servicelist_table_1_info").show();
		$("#servicelist_table_1_paginate").show();
		$("#servicelist_table_1_filter label:eq(0)").show();
		$("#servicelist_table_1").show();
		if(!isEmpty(appName)){
			showService(GL_jxt_appName);
		}
		
	}
	
	
	
	
	
	
	
	//切换添加节点 和 显示节点列表
	function troggleAddNode(){
		
		//设置默认值
		 $("#host").val("");
		 $("#port").val("");
		 $("#host").removeAttr("disabled");
		 $("#port").removeAttr("disabled");
		 $("#timeout").val(3000);
		 $("#weight").val(5);
		 $("#poolSize").val(8);
		 
		$("#nodeListTable").hide();
		$("#jxt-addNodeConfig_bt").hide();
		$("#jxt-nodeconfig-addNewnode").show();
		$("#jxt-addNodeConfig_return_bt").show();
		
		//if(GL_jxt_loadBalance == "ROUND"){
			$("#jxt-newnode-weight").show();
		//}else{
		//	$("#jxt-newnode-weight").hide();
		//}
		
	}
	function getfillFormData(){
		//jxt_newnode_save
		var host = $("#host").val(); 
		var port = $("#port").val(); 
		var timeout = $("#timeout").val(); 
		//处理容错方式，容错为失败重试才会 获取重试次数，默认为 3
		var retries = 3;
		if(GL_jxt_faultType=="FAILOVER"){
			retries = GL_jxt_retries ; 
		}
		//处理 负载规则，只有权重负载才会需要填写权重数，默认为 5 
		//var weight = 5;
		//if(GL_jxt_loadBalance == "ROUND"){
			weight = $("#weight").val() ; 
		//}
		var poolSize = $("#poolSize").val(); 
		var group = $("#group").val(); 
		var regex = /^\d+(\.\d+)?$/ ; 
		if(!regex.test(retries) || !regex.test(timeout) || !regex.test(poolSize) || !regex.test(weight)){
			$.alert("请填写数字格式的参数!");
			return ;
		}
		
		var data = {
			'appName':GL_jxt_appName,
			'serviceName':GL_jxt_serviceName,
			'host':host,
			'port':port,
			'timeout':timeout,
			'weight':weight,
			'poolSize':poolSize,
			'group':group,
			'loadBalance':GL_jxt_loadBalance,
			'fault':GL_jxt_faultType,
			'retries':retries
		};
		return data ;
	}
	/**
	 * 处理新添加节点
	 */
	function saveNewNode(){
		var jhost =  $("#host");
		 var jport =  $("#port");
		 // 修改节点配置, 与添加 新节点 公用 form表单。
		 if(jhost.attr("disabled")==true || jport.attr("disabled")){
			 var path = $("#jxt_urlpath").val();
			 UpddateNodeConfig(GL_jxt_appName,GL_jxt_serviceName,path);
			 return ;
		 }else{
				var jsondata = getfillFormData();
				$.ajax({
					url:'addServerInfoCommit.do',
					dataType:'json',
					type:'POST',
					data:jsondata,
					success:function(data,opts){
						 var obj =  JSON.parse(data);
						 if(obj.code == 200){
							 $.alert('添加节点操作成功',showNodeListAfter(jsondata.appName, jsondata.serviceName));
						 }else{
							 $.alert('添加节点操作失败');
						 }
					},error:function(data,opts){
						$.alert('添加节点操作失败');
					}
				});
			 
			 
		 }
		
		//jxt_newnode_save
//		var host = $("#host").val(); 
//		var port = $("#port").val(); 
//		var timeout = $("#timeout").val(); 
//		//处理容错方式，容错为失败重试才会 获取重试次数，默认为 3
//		var retries = 3;
//		if(GL_jxt_faultType=="FAILOVER"){
//			retries = GL_jxt_retries ; 
//		}
//		//处理 负载规则，只有权重负载才会需要填写权重数，默认为 5 
//		var weight = 5;
//		if(GL_jxt_loadBalance == "ROUND"){
//			weight = $("#weight").val() ; 
//		}
//		var poolSize = $("#poolSize").val(); 
//		var group = $("#group").val(); 
//		var regex = /^\d+(\.\d+)?$/ ; 
//		if(!regex.test(retries) || !regex.test(timeout) || !regex.test(poolSize) || !regex.test(weight)){
//			$.alert("请填写数字格式的参数!");
//			return ;
//		}
//		
//		var data = {
//			'appName':GL_jxt_appName,
//			'serviceName':GL_jxt_serviceName,
//			'host':host,
//			'port':port,
//			'timeout':timeout,
//			'weight':weight,
//			'poolSize':poolSize,
//			'group':group,
//			'loadBalance':GL_jxt_loadBalance,
//			'fault':GL_jxt_faultType,
//			'retries':retries
//		};
	}
	function cancelNewNode(){
		
	}
	
	
	//点击返回节点列表后，处理显示节点类表的函数
	function showNodeListAfter(appName,serviceName){
		$("#jxt-nodeconfig-addNewnode").hide();
		$("#jxt-addNodeConfig_return_bt").hide();
		$("#nodeListTable").show();
		$("#jxt-addNodeConfig_bt").show();
		if(!isEmpty(appName) && !isEmpty(serviceName)){
			showNodeConfigList(serviceName,appName);
		}
	}
	/**
	 * 提交更新服务的负载规则
	 */
	function commitUpdateBalance(){
		var balance = $("select#jxt-sel-loadBalance option:selected").val();
		var serviceName = GL_jxt_serviceName;
		var appName = GL_jxt_appName;
		var data = {
				'appName':appName,
				'serviceName':serviceName,
				'loadBalance':balance,
				'fault':GL_jxt_faultType,
				'retries':GL_jxt_retries
		};
		$.ajax({
			url:'updateClusterCommit.do',
			dataType:'json',
			type:'POST',
			data:data,
			success:function(data,opts){
				 var obj =  JSON.parse(data);
				 if(obj.code == 200){
					 GL_jxt_loadBalance = balance ; 
					 $.alert('修改容错规则操作成功');
					 showNodeConfigList(serviceName,appName);
					 $("#jxt-balance-info").hide();
					 //TODO:
					 return;
				 }else{
					 $.alert('修改容错规则操作失败');
				 }
			},error:function(data,opts){
				$.alert('修改容错规则操作失败');
			}
		});
	}
	
	function  faultTypeChangeEvent(){
		var fault = $("select#jxt-sel-faultType option:selected").val();
		if(fault == "FAILOVER"){
			$("#jxt-retries-update-div").show();
		}else{
			$("#jxt-retries-update-div").hide();
		}
	}
	/**
	 * 提交更新服务的容错规则
	 */
	function commitUpdateFault(){
		var fault = $("select#jxt-sel-faultType option:selected").val();
		var serviceName = GL_jxt_serviceName;
		var appName = GL_jxt_appName;
		var data = {
				'appName':appName,
				'serviceName':serviceName,
				'fault':fault,
				'loadBalance':GL_jxt_loadBalance
		};
		//判断如果是FAILOVER 容错方式下 则容错次数 输入框会显示。其他的容错方式下 不会显示输入框。
		if(!($("#jxt-retries-update-div").is(":hidden"))){
			data.retries = $("#update_retries").val();
		}
		$.ajax({
			url:'updateClusterCommit.do',
			dataType:'json',
			type:'POST',
			data:data,
			success:function(responseText,opts){
				 var obj =  JSON.parse(responseText);
				 if(obj.code == 200){
					 GL_jxt_faultType = fault ;
					 if(GL_jxt_faultType == "FAILOVER"){
						 //当修改为失败重试 容错时， 更新全局重试次数
						 GL_jxt_retries =  data.retries ;
					 }
					 $.alert('修改容错规则操作成功');
					 showNodeConfigList(serviceName,appName);
					 $("#jxt-fault-info").hide();
					 //TODO:
					 return ;
					 
				 }else{
					 $.alert('修改容错规则操作失败');
				 }
			},error:function(responseText,opts){
				 $.alert('修改容错规则操作失败');
			}
	 
	});
  }
	//app列表搜索
	function searchApp(){
		$("#searchInput").keydown(function(event){
	    	if(event.keyCode == 13){
	    		getAppList();
	    		$("#jxt-container-applist").show();
	    		$("#sample_2_wrapper_service table tbody").empty();
	    		$("#jxt-service-addNewnode").hide();
	    		$("#jxt-container-servicelist").hide();
	    		$("#jxt-container-nodelist").hide();
	    	}
	    });
		$("#search_app_bt").bind("click",function(){
			getAppList();
    		$("#jxt-container-applist").show();
    		$("#sample_2_wrapper_service table tbody").empty();
    		$("#jxt-service-addNewnode").hide();
    		$("#jxt-container-servicelist").hide();
    		$("#jxt-container-nodelist").hide();
		});
   }
	
//	 function showUpdNodePanel(datastr,path){
//		 $.dialog({
//		        title: '修改节点配置信息', 
//		        content: $('#J_form_Newnode')[0],
//		        id:'updateNodeDialog'
//		    });
//		 var data = datastr[0];
//		 var jhost =  $("#host");
//		 var jport =  $("#port");
//		 jhost.val(data.host); 
//		 jport.val(data.port); 
//		 $("#poolSize").val(data.poolSize);
//		 $("#weight").val(data.weight);
//		 $("#timeout").val(data.timeout);
//		 $("#group").val(data.group);
//		 //$("#poolSize").val(8);
//		 //$("#weight").val(5);
//		 jhost.attr("disabled","disabled");
//		 jport.attr("disabled","disabled");
//		 $("#jxt_urlpath").val(path);
//	 }
	 
	 function showUpdNodePanel(appName,interfaceName,host,port,poolSize,weight,timeout,group,path){
		 $.dialog({
		        title: '修改节点配置信息', 
		        content: $('#J_form_Newnode')[0],
		        id:'updateNodeDialog'
		    });
		 var jhost =  $("#host");
		 var jport =  $("#port");
		 jhost.val(host); 
		 jport.val(port); 
		 $("#poolSize").val(poolSize);
		 $("#weight").val(weight);
		 $("#timeout").val(timeout);
		 $("#group").val(group);
		 //$("#poolSize").val(8);
		 //$("#weight").val(5);
		 jhost.attr("disabled","disabled");
		 jport.attr("disabled","disabled");
		 $("#jxt_urlpath").val(path);
	 }
	 
	 function showNewApplicationPanel(){
		 $.dialog({
		        title: '添加新应用', 
		        content: $('#J_form_Newapp')[0],
		        id:'commitNewAppform'
		   });
	 }
	 
	 function saveNewApp(){
		 var appNameEn = $("#appName_en").val();
		 var appNameCn = $("#appName_cn").val();
		 if(isEmpty(appNameEn)|| isEmpty(appNameCn)){
			 $.alert("名称为必填项,请填写完整!");
			 return ; 
		 }
		 var regex = /^[0-9A-Za-z][\w*|\.|-]*[0-9A-Za-z]$/gi ; 
		 if(!regex.test(appNameEn)){
			 $.alert("Application只能可由数字、字母下、划线、点组成，且开头和结尾为数字或字母!");
			 return ;
		 }
		 $.ajax({
			url:'createApplication.do',
			data:{
				"appName_EN":appNameEn,
				"appName_CN":appNameCn
			},
			dataType:'json',
			type:'POST',
			success:function(responseText,stauts,e){
				var code = responseText.createApp ; 
				if(code){
					$.alert("添加应用成功!",gotoHomePage);
				}else{
					$.alert("添加应用失败!");
				}
			},error:function(responseText,stauts,e){
				$.alert("添加应用失败!");
			}
		 });
		 
	 }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	