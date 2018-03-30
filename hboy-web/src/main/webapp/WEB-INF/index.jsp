<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<jsp:include page="head.html" />
<body class="page-header-fixed page-full-width">
	<!-- BEGIN 页面头部 -->
	<div class="header navbar navbar-inverse navbar-fixed-top">
		<!-- BEGIN 顶部导航条 -->
		<jsp:include page="headerdev.html" />
		<!-- END 顶部导航条 -->
	</div>
	<!-- END 页面头部 -->
	<!-- BEGIN 页面主容器 -->
	<div class="page-container row-fluid">
		<!-- BEGIN PAGE -->
		<div class="page-content" id="page-content">
			<!-- BEGIN 右边容器-->
			<div class="container-fluid">
				<div class="row-fluid">
					<div class="controls"
						style="margin-top: 10px; margin-left: auto; margin-right: auto; width: 50%"">
						<input id="searchInput" type="text"
							style="width: 30%; text-align: left" name="name" value=""
							class="span12">
						<button type="button" class="btn blue" id="search_app_bt"
							style="margin-right: 50px; height: 30px; vertical-align: middle; margin-bottom: 10px;">搜索应用</button>
						<!-- <span style="margin-left:30px;margin-right:10px;">当前位置 ：<a href="#">应用</a></span><span style="" id="currLocation"></span> -->
					</div>
					<div id="jxt-container-center"
						style="width: 50%; margin-left: auto; margin-right: auto; display: block">
						<div class="span12" style="margin-left: 0"
							id="jxt-container-applist">
							<div class="portlet box blue">
								<div class="portlet-title">
									<div class="caption">
										<i class="icon-globe"></i>应用列表
									</div>
								</div>
								<div class="portlet-body">
									<div id="sample_2_wrapper_app"
										class="dataTables_wrapper form-inline" role="grid">
										<div class="dataTables_scroll">
											<div class="dataTables_scrollBody"
												style="overflow: auto; width: 100%;">
												<button id="jxt_dev_newapp_bt" class="btn blue"
													style="background-color: #b4cef8; padding: 4px; display: none;">
													添加应用 <i class="m-icon-swapright m-icon-white"></i>
												</button>
												<table class="table table-bordered table-hover"
													id="appListTable">
													<thead>
														<tr>
															<td
																style="text-align: center; line-height: 10px; border-top: 1px solid #ebebeb; border-bottom: 1px solid #ebebeb">应用英文名称
															</th>
															<td
																style="text-align: center; line-height: 10px; border-top: 1px solid #ebebeb; border-bottom: 1px solid #ebebeb">应用中文名称
															</th>
															<!-- <td style="text-align:center;border-top:1px solid #ebebeb;border-bottom:1px solid #ebebeb" >操作</th> -->
														</tr>
													</thead>
													<tbody id="appList-tbody"
														style="border-top: 1px solid #ebebeb">
														<script id="appList-script" type="text/html">
       						 								{{each list as v i}}
        														<tr>
           															<td class="hidden-480" style="line-height:10px;text-align:center;cursor:pointer" onclick="showService('{{v.appName_EN}}')">{{v.appName_EN || "--"}}</td>
           															<td class="hidden-480" style="line-height:10px;text-align:center;cursor:pointer" onclick="showService('{{v.appName_EN}}')">{{v.appName_CN || "--"}}</td>
       															</tr>
        		   											{{/each}}
    													</script>
													</tbody>
												</table>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>

						<!-- 增强表格 start-->
						<div class="span12" style="margin-left: 0; display: none;"
							id="jxt-container-servicelist">
							<div class="portlet box blue">
								<div class="portlet-title">
									<div class="caption">
										<i class="icon-globe"></i>服务列表
									</div>
								</div>
								<div class="portlet-body">
									<div id="sample_2_wrapper_service"
										class="dataTables_wrapper form-inline" role="grid">
										<div class="dataTables_scroll">
											<div class="dataTables_scrollBody"
												id="servivelist-data-outer-div"
												style="overflow: auto; width: 100%;">
												<!-- <button id="jxt-addNodeConfig_bt" class="btn blue" style="background-color:#b4cef8;" onclick="troggleAddNode()">添加节点 <i class="m-icon-swapright m-icon-white"></i></button> -->
												<!-- <button id="jxt-addNodeConfig_return_bt" class="btn blue" style="display:none;background-color:#b4cef8;">返回列表<i class="m-icon-swapleft m-icon-white"></i></button> -->
												<div id="table_data_outer_1">
													<!-- 服务列表 -->
													<!--  <table class="table table-bordered table-hover" id="servicelist_table_1" style="display:none;margin-left: 0px;  border-top:1px solid #ebebeb;">
																 	<thead style="width:100%">
																          <tr style="width:100%">
																              <th class="hidden-480" style="text-align:center;width:8% !important;line-height:10px;" >服务名称</th>
																              <th class="hidden-480"  style="text-align:center;width:3% !important;line-height:10px; ">使用状态</th>
																              <th class="hidden-480" style="text-align:center;width:5% !important;line-height:10px;">操作</th>
																          </tr>
																     </thead>
																	<tbody id="serviceList-tbody"  role="alert" aria-live="polite" aria-relevant="all">
																	</tbody>
														    </table>
														   -->
												</div>
												<!--  Add New Service start -->
												<div class="span6" id="jxt-service-addNewnode"
													style="display: none; width: 80%; margin-left: 19%;">
													<div class="portlet box white">
														<div class="portlet-body">
															<form id="J_form_newservice" action="#" method="POST"
																class="form form-horizontal" novalidate="novalidate">
																<div class="control-group">
																	<label class="control-label"><span
																		class="required">*</span>应用名称</label>
																	<div class="controls">
																		<input type="text" name="appnameText" value=""
																			id="appnameText" disabled="disabled" />
																	</div>
																</div>
																<div class="control-group">
																	<label class="control-label"><span
																		class="required">*</span>服务名称</label>
																	<div class="controls">
																		<input type="text" name="servicenameText"
																			id="servicenameText" />
																	</div>
																</div>
																<!--  
								                                    <div class="control-group">
								                                        <label class="control-label"><span class="required">*</span>负载规则</label>
								                                        <div class="controls">
								                                            <input type="text" id="port" name="port" class="">
								                                        </div>
								                                    </div>
								                                    <div class="control-group">
								                                        <label class="control-label"><span class="required">*</span>容错规则</label>
								                                        <div class="controls">
								                                            <input type="text" name="timeout" value='3000' id="timeout"  class="" >
								                                        </div>
								                                    </div>
																	<div class="control-group" id="jxt-newnode-weight" style="display:none">
								                                        <label class="control-label"><span class="required">*</span>重试次数</label>
								                                        <div class="controls">
								                                            <input type="text" name="weight" id="weight" class="">
								                                        </div>
								                                    </div>
								                                  -->
																<div class="form-actions"
																	style="background-color: white; border-top: none">
																	<button type="button" class="btn blue"
																		id="jxt_newservice_save" style="margin-right: 50px"
																		onclick="addNewService()">保存</button>
																	<button type="button" class="btn"
																		id="jxt_newservice_cancel" style="margin-left: 50px"
																		onclick="">取消</button>
																</div>
															</form>
														</div>
													</div>
												</div>
												<!--  Add New Service end -->
											</div>
										</div>
									</div>
								</div>
							</div>
							<!-- 增强表格 end -->
							<div class="span6 portlet box white"
								style="width: 100%; margin-left: 0; display: none;"
								id="jxt-container-nodelist">
								<!--BEGIN TABS-->
								<div class="tabbable tabbable-custom" style="margin-bottom: 0">
									<ul class="nav nav-tabs">
										<li id="tab_1_active_jxt" class="active"
											style="font-size: 18px; font-weight: 400"><a
											href="#tab_1_jxt_nodelist" data-toggle="tab">节点列表</a></li>
										<li class="" style="font-size: 18px; font-weight: 400"><a
											href="#tab_1_2" data-toggle="tab">负载配置</a></li>
										<li class="" style="font-size: 18px; font-weight: 400"><a
											href="#tab_1_3" data-toggle="tab">容错配置</a></li>
									</ul>
									<div class="tab-content " style="border-color: #b4cef8">
										<!-- Tab_1_1 start -->
										<div class="tab-pane active caption" id="tab_1_jxt_nodelist">
											<button id="jxt-addNodeConfig_bt" class="btn blue"
												style="background-color: #b4cef8;"
												onclick="troggleAddNode()">
												添加节点 <i class="m-icon-swapright m-icon-white"></i>
											</button>
											<button id="jxt-addNodeConfig_return_bt" class="btn blue"
												style="display: none; background-color: #b4cef8;">
												返回列表<i class="m-icon-swapleft m-icon-white"></i>
											</button>
											<!-- Node List start -->
											<table class="table table-bordered table-hover"
												id="nodeListTable" style="display: block;">
												<thead style="width: 100%">
													<tr style="width: 100%">
														<th class="hidden-480"
															style="text-align: center; width: 5%">服务地址</th>
														<th class="hidden-480"
															style="text-align: center; width: 5%">超时时间</th>
														<th class="hidden-480"
															style="text-align: center; width: 5%">服务权重</th>
														<th class="hidden-480"
															style="text-align: center; width: 5%">连接池</th>
														<th class="hidden-480"
															style="text-align: center; width: 5%">服务分组</th>
														<th class="hidden-480"
															style="text-align: center; width: 5%" colspan="2">操作</th>
													</tr>
												</thead>
												<tbody id="nodeconfigList-tbody" role="alert"
													aria-live="polite" aria-relevant="all">
													<script id="nodeconfigList-script" type="text/html">
       						 									{{each list as v i}}
        														<tr>
																	<td class="hidden-480" style="line-height:10px;cursor:pointer;width:5%" >{{v.host}}:{{v.port}}</td>
																	<td class="hidden-480" style="line-height:10px;cursor:pointer;width:5%" >{{v.timeout}}</td>
																	<td class="hidden-480" style="line-height:10px;cursor:pointer;width:5%" >{{v.weight || "--"}}</td>
																	<td class="hidden-480" style="line-height:10px;cursor:pointer;width:5%" >{{v.poolSize || "--"}}</td>
																	<td class="hidden-480" style="line-height:10px;cursor:pointer;width:5%" >{{v.group || "--"}}</td>
            														<td style="line-height:10px;text-align:center;width:5%" id="{{v.path}}">
																			<a href="javascript:void(0);" onclick="delNodeConfig('{{v.appName}}','{{v.interfaceName}}','{{v.path}}',this)">删除</a>
																		|   <a href="javascript:void(0);" onclick="showUpdNodePanel('{{v.appName}}','{{v.interfaceName}}','{{v.host}}','{{v.port}}','{{v.poolSize}}','{{v.weight}}','{{v.timeout}}','{{v.group}}','{{v.path}}',this)">修改</a>
																	</td>
       															</tr>
        		   											{{/each}}
    													</script>
													<!-- <td class="hidden-480" style="cursor:pointer"><a href="javascript:void(0);" onclick="updateNodeConfig('{{v.appName}}','{{v.serviceName}}','{{v.path}}')">修改</a></td> -->
												</tbody>
											</table>
											<!-- Node List end -->
											<!--  Add New Node start -->
											<div class="span6" id="jxt-nodeconfig-addNewnode"
												style="display: none; width: 80%; margin-left: 19%;">
												<div class="portlet box white">
													<div class="portlet-body">
														<div id="J_form_Newnode" action="#" method="POST"
															style="padding: 20px 100px 0px 20px"
															class="form form-horizontal" novalidate="novalidate">
															<div class="control-group">
																<label class="control-label" style="width: 110px"><span
																	class="required">*</span>地址：</label>
																<div class="controls" style="margin-left: 150px">
																	<input type="text" name="host" id="host">
																</div>
															</div>
															<div class="control-group">
																<label class="control-label" style="width: 110px"><span
																	class="required">*</span>端口：</label>
																<div class="controls" style="margin-left: 150px">
																	<input type="text" id="port" name="port" class="">
																</div>
															</div>
															<div class="control-group">
																<label class="control-label" style="width: 110px"><span
																	class="required">*</span>超时时间：</label>
																<div class="controls" style="margin-left: 150px">
																	<input type="text" name="timeout" id="timeout" class="">
																</div>
															</div>
															<div class="control-group" id="jxt-newnode-weight"
																style="">
																<!-- display:none -->
																<label class="control-label" style="width: 110px"><span
																	class="required">*</span>权重：</label>
																<div class="controls" style="margin-left: 150px">
																	<input type="text" name="weight" id="weight" class="">
																</div>
															</div>
															<div class="control-group">
																<label class="control-label" style="width: 110px"><span
																	class="required">*</span>连接数：</label>
																<div class="controls" style="margin-left: 150px">
																	<input type="text" name="poolSize" id="poolSize" class="">
																</div>
															</div>
															<div class="control-group">
																<label class="control-label" style="width: 110px"><span
																	class="required">*</span>服务分组：</label>
																<div class="controls" style="margin-left: 150px">
																	<input type="text" name="group" id="group"
																		class="">
																</div>
															</div>
															<div class="form-actions"
																style="background-color: white; border-top: none; margin-left: 150px !important; padding: 0px">
																<button type="button" class="btn blue"
																	id="jxt_newnode_save" style="margin-right: 40px">保存</button>
																<button type="button" class="btn"
																	id="jxt_newnode_cancel" style="margin-left: 40px"
																	onclick="javascript:eval($.dialog.get('updateNodeDialog').close())">取消</button>
															</div>
														</div>
													</div>
												</div>
											</div>
											<!--  Add New Node end -->
										</div>
										<!-- Tab_1_1 end -->

										<div class="tab-pane caption" id="tab_1_2">
											<div class="">
												<span
													style="font-size: 16px; margin-top: 6px; margin-left: 30px; vertical-align: middle; display: inline-block;">当前负载规则:&nbsp;&nbsp;&nbsp;<i
													id="jxt-balance_rule">随机选择(RANDOM)</i></span> <span
													style="display: inline-block; margin-left: 180px;"><a
													href="#" style="font-size: 16px; vertical-align: middle"
													class=""
													onclick="javascript:eval($('#jxt-balance-info').show());">修改
														</i>
												</a></span>
											</div>
											<div class="control-group" id="jxt-balance-info"
												style="margin-left: 30px; margin-top: 20px; display: none;">
												<div style="display: inline-block">
													<span class="required" style="font-size: 16px;">新负载规则:&nbsp;&nbsp;&nbsp;</span>
												</div>
												<div class="controls" style="display: inline-block">
													<select name="loadBalance" id="jxt-sel-loadBalance"
														class="" style="width: 150px;">
														<option selected value="ROUND">轮询选择(ROUND)</option>
														<option value="RANDOM">随机选择(RANDOM)</option>
													</select>
												</div>
												<!--  
		                                        <div class="controls">
								                       <span class="required" style="font-size:16px;">新负载规则:&nbsp;&nbsp;&nbsp;</span> <input type="text" name="weight" id="weight" class="">
								                 </div>
								                -->
												<div id="jxt-updatebalance-bt" style="display: inline-block">
													<a href="#" style="margin-left: 120px;" class="btn blue"
														onclick="commitUpdateBalance()">提交 <i
														class="m-icon-swapright m-icon-white"></i>
													</a>
												</div>
											</div>
										</div>
										<div class="tab-pane caption" id="tab_1_3">
											<span
												style="font-size: 16px; margin-top: 6px; margin-left: 30px; vertical-align: middle; display: inline-block;">当前容错规则:&nbsp;&nbsp;&nbsp;<i
												id="jxt-fault_rule">失败重试(FAILOVER)</i></span> <span
												style="display: inline-block; margin-left: 180px;"><a
												href="#" style="font-size: 16px; vertical-align: middle"
												class=""
												onclick="javascript:eval($('#jxt-fault-info').show());">修改
													</i>
											</a></span>
											<div class="control-group" id="jxt-fault-info"
												style="margin-left: 30px; margin-top: 20px; display: none;">
												<div style="display: inline-block">
													<span class="required" style="font-size: 16px;">新容错方式：&nbsp;&nbsp;&nbsp;</span>
												</div>
												<div class="controls" style="display: inline-block">
													<select name="faultType" id="jxt-sel-faultType" class=""
														style="width: 150px;" onchange="faultTypeChangeEvent()">
														<option style="" value="FAILFASE">失败返回(FAILFALSE)</option>
														<option value="FAILOVER">失败重试(FAILOVER)</option>
														<!--  	 <option value="FAILTIME">隔时屏蔽(FAILTIME)</option>-->
													</select>
												</div>
												<div class="controls" style="display: none"
													id="jxt-retries-update-div">
													<span class="required" style="font-size: 16px;">&nbsp;&nbsp;&nbsp;&nbsp;重试次数：&nbsp;&nbsp;&nbsp;</span>
													<input type="text" value="3" style="width: 110px;"
														name="retries" id="update_retries" class="">
												</div>
												<div id="jxt-updateFaultType-bt"
													style="display: inline-block">
													<a href="#" style="margin-left: 120px;" class="btn blue"
														onclick="commitUpdateFault()">提交 <i
														class="m-icon-swapright m-icon-white"></i>
													</a>
												</div>
											</div>
										</div>

									</div>
								</div>
								<!--END TABS-->
							</div>
						</div>
					</div>
					<!-- END 右容器 main-->
					<!-- END 右边容器-->
				</div>
				<!-- END PAGE -->
			</div>
		</div>
	</div>
	<!-- END 页面主容器 -->


	<input type="hidden" id="jxt_urlpath" />

	<div id="J_form_Newapp" action="#" method="POST" style="display: none"
		class="form form-horizontal" novalidate="novalidate">
		<div class="control-group">
			<label class="control-label" style="width: 125px"><span
				class="required">*</span>名称(Application)</label>
			<div class="controls" style="margin-left: 135px; margin-right: 20px;">
				<input type="text" name="appName_en" id="appName_en">
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" style="width: 125px"><span
				class="required">*</span>中文名称</label>
			<div class="controls" style="margin-left: 135px; margin-right: 20px;">
				<input type="text" id="appName_cn" name="appName_cn" class="">
			</div>
		</div>

		<div class="form-actions"
			style="background-color: white; border-top: none; margin-left: 150px !important; padding: 0px">
			<button type="button" class="btn blue" id="jxt_dev_newapp_save"
				style="margin-right: 30px">保存</button>
			<button type="button" class="btn" id="jxt_dev_newnode_cancel"
				style="margin-left: 30px"
				onclick="javascript:eval($.dialog.get('commitNewAppform').close())">取消</button>
		</div>
	</div>
	<script>
        $(document).ready(function() {  
        	//设置全局的ajax变量
        	//POST请求方式, 超时时间 10分钟,
        	$.ajaxSetup({
        		  type: "POST",
        		  timeout:1000*60*10
        	});
			//运维组件初始化
            App.init();
			
            menu("home");
            loadUserInfo();
            loadServerIp();
            
            //获取AppList
            getAppList();
            //创建节点
            $("#jxt_newnode_save").bind("click",saveNewNode) ; 
            //取消创建节点
            $("#jxt_newnode_cancel").bind("click",cancelNewNode) ; 
            //创建完成心节点后，点击tab页返回列表
            $("#tab_1_active_jxt").bind("click",showNodeListAfter) ; 
           //返回节点列表
            $("#jxt-addNodeConfig_return_bt").bind("click",showNodeListAfter) ; 
			
           //开发测试环境显示添加应用按钮
           	$("#jxt_dev_newapp_bt").bind("click",showNewApplicationPanel);
           
          	$("#jxt_dev_newapp_save").bind("click",saveNewApp);
            //绑定搜索app列表
            searchApp();
            
            var screenhight = window.screen.availHeight ; 
        	var s = screenhight * 0.8 ; 
        	$("#page-content").css("min-height",s);
        });
       

    </script>
	<!-- END 页面基本js -->

</body>
</html>