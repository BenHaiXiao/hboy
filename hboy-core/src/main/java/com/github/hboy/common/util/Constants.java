package com.github.hboy.common.util;

/**
 * @author xiaobenhai
 * Date: 2016/3/10
 * Time: 11:28
 */
public class Constants {
	
	
	 public static final String  INTERFACE_KEY                      = "interface";
	 
	 public static final String  PATH_PROVIDER                      = "provider";
	 
	 public static final String  PATH_SPLIT                      	= "/";
	 
	 public static final String  ZOOKEEPER                          = "zookeeper";
	 
	 public static final String  DEFAULT_GROUP                      = "*";
	 
	 public static final int     DEFAULT_IO_THREADS         		=  Runtime.getRuntime().availableProcessors() + 1;

	 public static final int     DEFAULT_MAX_MESSAGE_SIZE           = 8 * 1024 * 1024;    
	 
	 public static final int     DEFAULT_TIMEOUT					= 3 * 1000;
	 
	 public static final boolean DEFAULT_SENT                       = false;
	 
	 public static final int     DEFAULT_RECONNECT_PERIOD           = 2000;
	 
	 public static final String  PATH_CONFIGURATION                 = "configurators";
	 
	 public static final String  MONITOR_KEY                        = "monitor";
	 
	 public enum ServiceProtocolType{
		thrift,HTTP;
	}
	 
	 
	 public enum TransportType{
		 TSOCKET,TFRAMED;
	 }
		
	 public enum ThriftProtocolType{
			BINARY,COMPACT,JSON,SIMPLEJSON,TUPLE;
	 }
	 
	 public enum LoadBalanceType{
			RANDOM("random"),ROUND("round");
			
			private String name = "";
			
			private LoadBalanceType(String name){
				this.name = name;
			}
			
			public static LoadBalanceType getType(String name){
				for (LoadBalanceType type : LoadBalanceType.values()) {
					if (type.getName().equalsIgnoreCase(name)) {
						return type;
					}
				}
				return RANDOM;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}
		}
	 public enum FaultType{
			FAILFASE("failfast"),FAILOVER("failover"),FAILTIME("failtime");
			private String name = "";
			
			private FaultType(String name){
				this.name = name;
			}
			
			public static FaultType getType(String name){
				for (FaultType type : FaultType.values()) {
					if (type.getName().equalsIgnoreCase(name)) {
						return type;
					}
				}
				return FAILOVER;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}
		}
}
