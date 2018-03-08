#  hboy 
   *  分布式服务框架，服务化高可用解决方案
## 将来
   支持hboy-web版本。
## 背景

* 在当前的很多项目中，服务与服务之间的调用，应用可能只是通过固定的协议，简单的暴露和引用远程服务，通过配置服务的ip地址进行调用。

* 服务节点上、下线，客户端需要手动更改配置重启，连接管理、路由功能相对薄弱。

* 随着应用间所依赖的服务越多，其他服务难免存在挂掉情况，缺少服务管理功能。

* 服务流量，tps统计监控，需要依赖运维的监控，或者进行日志打印收集，服务质量监控麻烦。

* 不同的项目需求中存使用相同的接口，某些接口非常常用，而数据解析繁琐，重复开发度高。

##  hboy能做什么 

*  高性能的RPC，轻量远程调用，同机房优先调用

  *灵活的服务发现机制，平滑扩展节点，自动注册，自动发现节点

  *提供软负载均衡和故障转移机制

  *支持thrift、HTTP协议

  *透明化的远程方法调用,面向接口，无API侵入，就像调用本地方法一样调用远程方法

  *集成服务监控 


##  基本原理 

![](http://img.blog.csdn.net/20160626200720063)



##### 节点角色说明：

*	Provider: 暴露服务的服务提供方。
  *Consumer: 调用远程服务的服务消费方。
  *服务中心: 服务注册与服务发现的服务中心。
  *监控中心: 统计服务的调用次调和调用时间的监控中心。

##### 1、连通性：

*  服务中心负责服务地址的查找与注册，可手动添加服务，相当于目录服务，注册中心不转发请求，压力较小

  *服务中心，服务提供者，服务消费者三者之间均为长连接，监控中心除外

  *服务中心通过长连接感知服务提供者的存在，服务提供者宕机，注册中心将立即推送事件通知消费者

  *服务中心和监控中心全部宕机，不影响已运行的提供者和消费者，消费者在本地缓存了提供者列表

  *服务中心和监控中心都是可选的，服务消费者可以直连服务提供者

  *监控中心负责统计各服务调用次数，调用时间等，统计先在内存汇总后每分钟一次发送到监控中心，并展示
##### 2、健状性：

*  服务中心宕掉后，仍能通过缓存提供服务列表查询

  *服务中心对等集群，任意一台宕掉后，将自动切换到另一台

  *服务中心全部宕掉后，服务提供者和服务消费者仍能通过本地缓存通讯

  *服务提供者全部宕掉后，服务消费者应用将无法使用，并无限次重连等待服务提供者恢复

  *监控中心宕掉不影响使用，只是丢失部分采样数据

##### 3、伸缩性：
*  服务中心为对等集群，可动态增加机器部署实例

  *服务提供者无状态，可动态增加机器部署实例，服务中心将推送新的服务提供者信息给消费者

----

## 使用说明


### 依赖

 	JDK1.6+
 	通过mvn dependency:tree 命令分析，缺省依赖以下三方库：

	[INFO] com.yy.cs:service-core:jar:2.1.3
	[INFO] +- org.jboss.netty:netty:jar:3.2.5.Final:compile
	[INFO] +- com.yy.cs:yy-codec:jar:2.1.2:compile
	[INFO] |  \- org.javassist:javassist:jar:3.18.1-GA:compile
	[INFO] +- com.facebook.swift:swift-codec:jar:0.12.0:compile
	[INFO] |  +- com.facebook.swift:swift-annotations:jar:0.12.0:compile
	[INFO] |  +- com.google.guava:guava:jar:16.0.1:compile
	[INFO] |  +- org.ow2.asm:asm-all:jar:4.1:compile
	[INFO] |  +- com.thoughtworks.paranamer:paranamer:jar:2.5.2:compile
	[INFO] |  \- com.google.code.findbugs:annotations:jar:2.0.2:compile
	[INFO] +- org.slf4j:slf4j-api:jar:1.7.2:compile
	[INFO] +- commons-pool:commons-pool:jar:1.6:compile
	[INFO] +- com.fasterxml.jackson.core:jackson-databind:jar:2.3.0:compile
	[INFO] +- com.github.adyliu:zkclient:jar:2.1.1:compile
	[INFO] |  \- org.apache.zookeeper:zookeeper:jar:3.4.5:compile
	[INFO] \- org.apache.thrift:libthrift:jar:0.6.1:compile
	[INFO]    +- commons-lang:commons-lang:jar:2.5:compile

这里所有依赖都是缺省配置:

*	netty.jar 可选依赖，网络传输。
  *yy-codec:jar 可选依赖，YY协议序列化包。
  *swift-codec:jar 可选依赖，thrift协议序列化包。
  *slf4j-api:jar 日志输出API包。
  *commons-pool.jar 可选依赖，连接池包，客户端使用BIO需要使用。
  *jackson-databind:jar 可选依赖，json序列化包，通过配置直连则不需要。
  *zkclient.jar 可选依赖，通过配置直连则不需要。


##  Thrift接口DEMO 
###  接口定义 	

	@ThriftService
	public interface Scribe
	{   
	    @ThriftDocumentation("这是注释")
	    @ThriftMethod("testObj")
	    LogEntry testObj(LogEntry messages);
	}
	
	@ThriftStruct
	public class LogEntry
	{
		private String message;
	    private int type;
	
	    @ThriftField(1)
	    public String getMessage()
	    {
	        return message;
	    }
	
	    @ThriftField
	    public void setMessage(String message)
	    {
	        this.message = message;
	    }
	
	    @ThriftField(2)
	    public int getType()
	    {
	        return type;
	    }
	
	    @ThriftField
	    public void setType(int type)
	    {
	        this.type = type;
	    }
	}
	
	public class SwiftScribe implements Scribe{
	
		@Override
		public LogEntry testObj(LogEntry messages)  {
			return messages;
		}
	}


### 客户端使用spring初始化
	<bean id="scribeFactory" class="com.github.hboy.center.ReferenceFactory" destroy-method="destroy"  >
		<!-- 设置服务管理中心bean -->
		<property name="centerConfig" ref="centerConfig" />
		<!-- 接口名称 -->
		<property name="interfaceName" value="com.yy.cs.demo.Scribe" /> 
	</bean>
	<bean id="centerConfig" class="com.github.hboy.common.service.config.CenterConfig">
		<constructor-arg index="0" value="192.168.0.1:2181"></constructor-arg>
		<!--  使用服务管理中心一定要配置application  -->
		<property name="application" value="xxxx" />  
	</bean>

### 调用代码
	scribeFactory.getClient().send();	
	//or 
	<bean  id="xxxService" factory-bean="xxxFactory" factory-method="getClient" />
	xxxService.send();
	
	// ps：同样也支持本地配置的方式
	<property name="centerConfig" ref="centerConfig" />
	// 修改为
	<property name="localConfigs" ref="locals" />


### 服务端通过spring暴露一个接口
	<bean id="scribeExporter" class="com.github.hboy.center.ServiceExporter" init-method="export" >
		<!-- 设置ip和开启的端口 -->
		<property name="exportConfig" ref="exportConfig" />
		<!-- 设置服务中心，如果不注册到服务中心，则不需要配置该属性 -->
		<property name="centerConfig" ref="centerConfig" />
		<!-- 暴露的接口 -->
		<property name="interfaceName" value="com.github.hboy.demo.Scribe" /> 
		<!-- 接口的实现实例 -->
		<property name="service" ref="scribeService" />
	</bean>
	
	<bean id="scribeService" class="com.github.hboy.demo.SwiftScribe"  >
	
	<bean id="centerConfig" class="com.github.hboy.common.service.config.CenterConfig">
		<constructor-arg index="0" value="192.168.0.1:2181"></constructor-arg>
		<!--  使用服务管理中心一定要配置application  -->
		<property name="application" value="xxxx" />   
	</bean>
	<!--配置本机的ip和port-->
	<bean id="exportConfig" class="com.github.hboy.common.service.config.ExportConfig">
		<property name="host" value="xxx.xx.xx.xx" />  
		<property name="port" value="xxxx" />  
	</bean>

----

## 功能介绍

### 协议
	支持thrift、HTTP协议,缺省协议为thrift协议
	属性配置：protocol = thrift

#### thrift协议

	支持thrift的Binary、Compact、JSON种序列化形式，默认使用TBinaryProtocol的序列化
	属性配置：thriftProtocol = BINARY
	
	支持通过thrift gen的方式生成的API，和采用swift注解的方式标注API
	通过thrift -gen java xxx.thrift生成API，使用thrift lib自带BIO。，
	通过plugin或者手动annotation方式标注API，使用netty，采用NIO。（推荐使用）annotation注解说明：[https://github.com/facebook/swift/tree/master/swift-codec](https://github.com/facebook/swift/tree/master/swift-codec "swift注解")
	swift相关工具的使用见： 附录swift工具使用
	
	ps：使用官方的libthrift，必须带上TFramed的Transport，会增加一个4位包长的协议头。

thrift协议格式
>	<img src="/posts/library/service-center/image/02.png" alt= "thrift协议格式"  />

#### http协议

	采用JDK的Http实现	,使用同样面向接口，Object的序列化参与json格式，支持get、post请求
	protocol = HTTP		
	
	ps：暂时还未实现http协议的服务端
	
	@HttpPath("userService")
	public interface HttpClientService {
	
	    @HttpPath("getUser")
	    User getUser(@HttpParam(name = "uid")      long    userId, 
	                 @HttpParam(name = "sex")      Integer sex,
	                 @HttpParam(name = "nick")     String  nick,
	                 @HttpParam(name = "isVIP")    boolean isVIP);
	
	    @HttpPath("report")
	    void reportLoginLogs(@HttpParam(name = "loginLogs") List<LoginLog> loginLogList);
	}
	
	public class LoginLog {
	
	    private long logId;
	    private String log;
	    
	    public LoginLog(long logId, String log){
	        this.logId = logId;
	        this.log = log;
	    }
	    public long getLogId() {
	        return logId;
	    }
	    public void setLogId(long logId) {
	        this.logId = logId;
	    }
	    public String getLog() {
	        return log;
	    }
	    public void setLog(String log) {
	        this.log = log;
	    }
	}

### 负载均衡

	在集群负载均衡时，提供了两种均衡策略，默认为RANDOM随机调用
	设属性 loadBalance = RANDOM
	随机，按权重设置随机概率。
	设属性 loadBalance = ROUND
	轮循，按公约轮循。

### 故障转移 

	在集群调用失败时，提供了三种容错方案，缺省为FAILFASE失败直接返回 
	fault = FAILFASE
	快速失败，只发起一次调用，失败立即报错
	fault = FAILOVER
	失败自动切换，当出现失败，重试其它服务器,可通过retries=3来设置重试次数(含第一次调用)。

### 服务分组，优先同机房调用

	客户端可通过group参数配置，来设置同机房优先调用。
	设属性 group='*'
	优先调用相同group的服务端，如果没有则用其他group的节点，默认值：'*' （不区分机房）

### 服务禁止

	禁止使用某一个接口，返回AccessAuthException异常。
	设属性 accessable = true;
	业务应用可能需要调用各种外部服务，当某一个服务卡主，可能会导致整个应用卡主，使用服务禁止功能，快速返回一个异常，不影响整个应用。

### 超时时间

	定义在连接上的超时时间，缺省值为3000ms。
	设属性 timeout = 5000

### 连接数

	定义与服务端创建的连接数量，缺省为8。
	设置属性 maxActive = 10

### 本地配置直连

	不配置服务管理中心，本地固定服务端ip，服务提供者地址通过本地配置进行配置，不能动态发现管理。
	创建LocalConfig
	设置属性 localConfigs = 配置LocalConfig的List对象 

### 设置hander

	设置在连接上的hander事件，可扩展，针对thrift协议
	监听连接连接事件
	实现com.github.hboy.center.remoting.ChannelEventHandler接口
	设属性 channelEventHandlers = 实现ChannelEventHandler接口的List对象

### 设置filter

	设置在接口上的filter,可正对接口进行参数拦截，
	扩展实现com.github.hboy.center.filter.Filter接口	
	设属性 filters = 实现Filter接口的List对象		

### 配置监控

	连接监控中心地址，统计一分钟内的调用情况，1分钟上报一次。


## 参数配置
#### ReferenceFactory 参数配置
| 属性                   | 类型                  | 必填   | 缺省值      | 描述                                       |
| -------------------- | ------------------- | ---- | -------- | ---------------------------------------- |
| centerConfig         | List<LocalConfig>   | 二选一  |          | 服务管理中心配置                                 |
| localConfigs         | CenterConfig        | 二选一  | 无        | 本地配置                                     |
| interfaceName        | String              | 二选一  |          | 服务名称(gen出来的thrift接口都为内部类,需要在服务后面加$Iface) |
| interface            | Class<?>            | 二选一  |          | 接口的class                                 |
| fault                | FaultType           | 否    | FAILFASE | 容错规则(FAILFASE,FAILOVER,FAILTIME)         |
| loadBalance          | LoadBalanceType     | 否    | RANDOM   | 负载规则(RANDOM,ROUND)                       |
| retries              | int                 | 否    | 3        | 重试次数(FAILOVER 时生效)，不含第一次                 |
| interval             | int                 | 否    | 30*1000  | 重试次数(FAILTIME 时生效)                       |
| group                | String              | 否    | *        | 服务分组，适用于优先调用同机房节点                        |
| protocol             | ServiceProtocolType | 否    | thrift   | 协议，（thrift、YY、HTTP）                      |
| channelEventHandlers | boolean             | 否    | null     | 设置在连接上的handler，需要实现ChannelEventHandler接口，针对thrift协议和YY协议有效 |
| filters              | boolean             | 否    | null     | 设置控制在接口上的filter，需要实现Filter接口             |
| transport            | TransportType       | 否    | TFRAMED  | thrift协议有效，transport方式(TFRAMED,TSOCKET)  |
| thriftProtocol       | ProtocolType        | 否    | BINARY   | thrift协议有效，序列化方式(BINARY,COMPACT,JSON,SIMPLEJSON,TUPLE) |
| isAnnotation         | boolean             | 否    | false    | thrift协议有效，是否采用swift的Annotation方式，如果为false，将依赖通过thrift文件gen的代码 |

### ServiceExporter参数配置
| 属性                   | 类型                  | 必填   | 缺省值      | 描述                                       |
| -------------------- | ------------------- | ---- | -------- | ---------------------------------------- |
| centerConfig         | List<LocalConfig>   | 否    |          | 服务管理中心配置                                 |
| service              | Object              | 是    |          | 接口的实现实例                                  |
| interfaceName        | String              | 二选一  |          | 服务名称(gen出来的thrift接口都为内部类,需要在服务后面加$Iface) |
| interface            | Class<?>            | 二选一  |          | 接口的class                                 |
| fault                | FaultType           | 否    | FAILFASE | 容错规则(FAILFASE,FAILOVER,FAILTIME)         |
| loadBalance          | LoadBalanceType     | 否    | RANDOM   | 负载规则(RANDOM,ROUND)                       |
| retries              | int                 | 否    | 3        | 重试次数(FAILOVER 时生效)                       |
| interval             | int                 | 否    | 30*1000  | 重试次数(FAILTIME 时生效)                       |
| group                | String              | 否    | *        | 服务分组，适用于优先调用同机房节点                        |
| transport            | TransportType       | 否    | TFRAMED  | thrift协议有效，transport方式(TFRAMED,TSOCKET)  |
| thriftProtocol       | ProtocolType        | 否    | BINARY   | thrift协议有效，序列化方式(BINARY,COMPACT,JSON,SIMPLEJSON,TUPLE) |
| protocol             | ServiceProtocolType | 否    | thrift   | 协议，（thrift、YY、HTTP）                      |
| channelEventHandlers | boolean             | 否    | null     | 设置在连接上的handler，需要实现ChannelEventHandler接口，针对thrift协议和YY协议有效 |
| filters              | boolean             | 否    | null     | 设置控制在接口上的filter，需要实现Filter接口             |

### CenterConfig参数配置
| 属性                | 类型     | 必填   | 缺省值     | 描述                    |
| ----------------- | ------ | ---- | ------- | --------------------- |
| address           | String | 是    |         | 地址                    |
| application       | String | 是    | 无       | 应用名称                  |
| connectionTimeOut | int    | 否    | 10000ms | 默认连接服务管理中心超时时间        |
| sessionTimeOut    | int    | 否    | 30000ms | 默认连接服务管理中心session过期时间 |
| weight            | int    | 否    | 5       | 默认服务节点的权重值            |
| maxActive         | int    | 否    | 8       | 默认服务节最大链接数            |
| timeout           | int    | 否    | 3000ms  | 超时时间                  |
| monitorAddress    | String | 否    | null    | 监控地址                  |

### ExportConfig参数配置
| 属性        | 类型     | 必填   | 缺省值    | 描述         |
| --------- | ------ | ---- | ------ | ---------- |
| host      | String | 是    |        | 地址         |
| port      | int    | 是    |        | 端口         |
| weight    | int    | 否    | 5      | 默认服务节点的权重值 |
| maxActive | int    | 否    | 8      | 默认服务节最大链接数 |
| timeout   | int    | 否    | 3000ms | 超时时间       |


### LocalConfig参数配置
| 属性          | 类型     | 必填   | 缺省值    | 描述       |
| ----------- | ------ | ---- | ------ | -------- |
| host        | String | 是    |        | 地址       |
| port        | int    | 是    |        | 端口       |
| application | String | 否    | 无      | 应用名称     |
| timeout     | int    | 否    | 3000ms | 超时时间     |
| weight      | int    | 否    | 5      | 多地址时，池权重 |
| maxActive   | int    | 否    | 8      | 最大链接数    |

### thrift协议注解标识：

	使用了Facebook开源的swift-codec包描述thrift协议。
	swift描述thrift协议的详细用法参见： [https://github.com/facebook/swift/tree/master/swift-codec](https://github.com/facebook/swift/tree/master/swift-codec "swift注解") 
	ps:暂时只支持0.12.0以下版本	

### http协议注解标识：

	| 属性         | vulue类型   | 	描述      |
	| ----	      | ----   |  ----      |
	| @HttpPath    | String   | 	http协议的path定义，标示在接口上     |
	| @HttpMethod  | string   | 	定义方法上,和@HttpPath组合成完整的http协议的path     |
	| @HttpParam   | String   | 	参数注解，http协议参数名称      |


## 设计
------
## 常用接口概览

	项目中存在很多常用接口，使用频率比较高，为了减少重复开发，所以基于集线塔，进行了一些常用接口的封装。

### 依赖	
	根据不同的业务方，进行了分包，主要分为webdb,IM,business,channel,具体的版本可以在私服库上进行查找。
	pom.xml

### 第三方服务接入

##  版本CHANGES

1. 支持thrift协议动态发现节点机制
2. 服务可视化服务管理界面
3. 针对服务节点负载：轮询、随机
4. 针对服务调用情况容错:失败快速返回、重试其他服务器
5. 池化thrift连接
6. 强制超时时间
7. 服务节点配置客户端内存缓存文件缓存。
8. 修改服务端，端口不绑定固定ip上。
9. 增加支持pipe项目:service-pipe模块
10. 强化http协议功能，支持get/post。（采用原生jdk实现http，每个host默认五条http长连接）
11. 增加支持YY协议。
12. 增加支持接口禁止调用功能。
13. 增加支持http协议。
14. 增加在接口上的 filter 功能。
15. 增加thrift协议的Swift注解方式，自动date to long 类型。
16. 增加关注在连接上的 Handler 事件。
17. 调整zookeeper的存储关系。
18. 增加监控功能：统计tps，最大耗时，最小耗时，平均耗时、成功次数、异常请求入参出参信 息。
19. 增加监控管理中心界面。
20. 修改自动注册节点，在服务管理中心修改之后，自动注册节点不能自动删除bug。
21. 增加集线塔连接层
22. 增加thrift协议采用Swift注解的形式发布接口
23. 发布接口自动注册到服务管理中心,在服务管理中心创建节点,随应用与服务管理中心的会话来保持节点状态
24. 客户端引用接口,支持Swift注解形式
25. 修改commonpool管理的socket池，在activateObject的时候重新open，导致socket连接增多。
26. 修改在调用服务异常，invalidateObject后还进行了returnObject逻辑，改调用多余。
27. 发布group的配置功能，以做同机房优先调用区分。
28. 调整包结构，抽象ReferenceFactory的配置。
29. 修改潜龙发布时，用户没有user.home权限创建缓存文件异常。
30. 服务destroy，连接资源没回收完全。
31. 更变Failover的容错规则，retries配置没有生效。
32. 新增服务做应用区分的管理。

--------
##  附录Swift工具使用
	swift注解标识方式详见：https://github.com/facebook/swift
	Swift工具主要包括两种：
	* 通过使用Swift Generator CLI工具，将定义好的.thrift文件gen出java文件;
	* 通过使用Swift2Thrift Generator CLI工具，将编写好的、符合swift注解规范的.java文件转化为.thrift文件。

### Swift Generator CLI 工具使用介绍

	Swift Generator CLI工的使用有两种方式，
	(1) 直接在Eclipse中集成插件，运行maven generate-sources 生成
	(2) 控制台运行jar包，生成文件


#### 方法一，Eclipse中集成插件（推荐）

    * 在pom.xml的插件配置项中，加入如下一段插件配置，在Eclipse中右键选择 maven generate-sources； linux下，cd到pom.xml目录，运行mvn generate-sources 
    	<!-- Swift Maven Plugin -->
    			<plugin>
    		      <groupId>com.facebook.mojo</groupId>
    		      <artifactId>swift-maven-plugin</artifactId>
    		      <version>0.13.0-SNAPSHOT</version>
    		      <executions>
    		        <execution>
    		          <goals>
    		            <goal>generate</goal>
    		          </goals>
    		        </execution>
    		      </executions>
    		     <configuration>
    		     	<idlFiles>
    		     		<directory>${basedir}/scripts/</directory>
    					  <includes>
    					     <include>**/channel/*.thrift</include>
    					  </includes>
    		     	</idlFiles>	
    		     	<generateIncludedCode>true</generateIncludedCode>
    				<!--下面这些参数可选配置-->
    				<overridePackage>com.yy.cs.test</overridePackage>
    				<addThriftExceptions>false</addThriftExceptions>
    				<!--可选配置end-->
    		     </configuration>
    			</plugin>
    * 其中，
    	- ${basedir}/scripts/ 为根目录下存放thrift脚本文件的base目录，scripts是样例目录可自行指定，include 代表的是thrift所在目录， ** 匹配任何祖先目录， *.thrift为通配符匹配
    	- generateIncludedCode： 是否将include进来的文件也生成出来。需要注意的是，被include文件 与 include文件的路径。建议将存在include情况的文件都放在同一个目录下，避免不必要的麻烦。
    	- 生成java文件默认放在 target/generated-sources/swift 下，也可以自行设定。
    	- overridePackage: 将覆盖在thrift文件中的命名空间，生成的java package都是这里指定的值，不设置的情况下则生成的java package与thrift文件中namespace 一致。
    	- addThriftExceptions 是否在生成的方法上都加上 org.apache.thrift.TException 异常。
<a href="https://github.com/facebook/swift/tree/master/swift-maven-plugin"  target="_blank">详细参数配置</a>

<br/>
<br/>
#### 方法二，控制台运行jar包

     * 运用maven命令，下载最新的工具，如：mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get 
       -DremoteRepositories=central::default::http://repo1.maven.apache.org/maven2 
       -Dartifact=com.facebook.swift:swift-generator-cli:RELEASE:jar:standalone -Ddest=/tmp/
     * 也可以在yy 私服中下载 : http://jrepo2.yypm.com/nexus/index.html#nexus-search;quick~swift-generator-cli
     * 其中-Ddest 是下载工具的目标文件夹，可自行设置。这里假设下载到/temp目录下,下载的0.12.0版本的

#### 快速入门

	 *常用命令：(cd 到 swift-generator-cli-0-12.0-standalone.jar 所在目录)
	      java -jar swift-generator-cli-0-12.0-standalone.jar  -override_package  com.mytest.xxx -use_java_namespace ./test.thrift
	
	* 说明：
	        设置-use_java_namespace参数后可以在test.thrift文件中使用namespace java com.mytest.xxx 的方式命名pakcage
	        如果不设置-use_java_namespace参数，则在test.thrift文件中需要用namespace java.swift com.mytest.xxx 的方式命名pakcage，否则报错


#### 参数及使用介绍

	 * 参数介绍
	   -default_package xxx ：当在thrift文件中没有写明namespace时，生成的java源文件的package就默认为 xxx
	   -generate_beans :当运行命令时，如果设置这个参数，则生成的java源码是javabean形式，默认不加则不为bean
	   -generate_included_files :设置此参数，如果thrift文件inclue其它文件，生成java源码时，被include的thrift文件也会被生成
	   -out xxx :指明生成的java源文件 输出到目标文件夹
	   -override_package xxx: 设置此参数时，强制将生成的java文件的package更改为xxx，此时在thrift文件中使用namespace定义的package将失效
	   -use_java_namespace： 当设置这个命令时，在thrift文件中定义namespace时可以使用"namespace java com.yy.cs.base"的格式，否则需要安装 "namespace java.swift com.yy.cs.base" 来定义。
	   
	 * 使用案例
	   java -jar /temp/swift-generator-cli-0.12.0-standalone.jar  ./test.thrift   （注意，这个文件中的namespace格式为 namespace java.swift com.yy.cs.xxx）
	   java -jar /temp/swift-generator-cli-0.12.0-standalone.jar  -use_java_namespace ./test.thrift  (当设置了-use_java_namespace参数时，文件中namespace则可写成 namespace java  com.yy.cs.xxx)  
	   java -jar /temp/swift-generator-cli-0-12.0-standalone.jar  -override_package  com.mytest.xxx  (此时，生成的java文件的package被强制改为 com.mytest.xxx)

###  Swift2Thrift Generator CLI 工具使用介绍 

	 *在进行介绍之前，必须谨记一点，Swift2Thrift Generator CLI在java代码转化为thrift文件时，要确保.class文件时在同一个package中，也就是当前工具不支持不同package的.class通过运行一条命令生成thrift文件。
	   如果.class文件所在的包不相同，则需执行多次命令。


​	   
​	   
----	   
#### 获取并安装Swift2Thrift Generator CLI工具

	 * 运用maven命令，下载最新的工具，如：mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get 
	   -DremoteRepositories=central::default::http://repo1.maven.apache.org/maven2 
	   -Dartifact=com.facebook.swift:swift2thrift-generator-cli:RELEASE:jar:standalone -Ddest=/tmp/ 
	 * 其中-Ddest 是下载工具的目标文件夹，可自行设置。这里假设下载到/temp目录下,下载的0.12.0版本的

#### 快速入门

	*  常用命令：(cd 到 swift2thrift-generator-cli-0.12.0-standalone.jar 所在目录)
	   java -cp swift2thrift-generator-cli-0.12.0-standalone.jar;F:\swift\tools\GCTools com.facebook.swift.generator.swift2thrift.Main 
	   -package com.yy.cs.demo LogEntry ResultCode Scribe -namespace java com.xxx.javaspace.xxx -namespace cpp cppspace -out service.thrift -v
	   
	*  说明：
	    java -cp swift2thrift-generator-cli-0.12.0-standalone.jar;F:\swift\tools\GCTools ————设置两个classpath，window下使用分号";"分割多个cp,linux下使用冒号":"多个cp。本次测试jar和.class文件的顶层包com都在F:\swift\tools\GCTools下
	    com.facebook.swift.generator.swift2thrift.Main ———— 启动Main类的main方法，固定写法。
		-package com.yy.cs.demo LogEntry ResultCode Scribe ———— 设置java包名+类名,指定了包名后，LogEntry ResultCode Scribe 的包默认是com.yy.cs.demo
		-namespace java com.xxx.javaspace.xxx -namespace cpp cppspace ———— 定义两个命名空间，在生成的thrif文件的开头处添加两个空间定义： namespace java com.xxx.javaspace.xxx namespace cpp cppspace 
		-out service.thrift ————输出生成的thrift文件目录
		-v, -verbose	 用来设置在生成thrift文件时，打印相关debug信息

####  参数及使用介绍

      * 参数介绍
    	 -allow_multiple_packages ：设置这个参数时，允许class文件来自不同的文件目录。
    	 -package: 这个参数设置默认的包名，对应没有fully qualified的类而言，其默认的包为这个设置的包名。
    	 -map：用来设置在生成thfit文件时，将已经存在的thrift文件include进来
    	 -namespace 用来设置thrift文件中定义的多个基于不同语言的namespace，例如 namespace java myspace 、namespace cpp cppspace 等
    	 -out 用例指定 thrift文件的输入目标，默认在控制台输出，可以指定特定的文件
    	 -v, -verbose	 用来设置在生成thrift文件时，打印相关debug信息

​			 


​	