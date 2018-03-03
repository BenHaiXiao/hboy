package com.github.hboy.common.config;

/**
 * @author xiaobenhai
 * Date: 2016/3/26
 * Time: 12:28
 */
public class ServerInfo {
	
	protected  String host;	//地址

	protected  int port;		//端口
	
	protected String interfaceName;
	
	protected int timeout = 3000;			//超时时间
    
	protected int weight = 5;				//权重
	
	protected int poolSize = 8;				//连接池大小
	
	protected String group = "*";           //group 所属组id,默认是 *
	
	public ServerInfo(){
	}
 
    public ServerInfo(String host, int port, String interfaceName, int timeout, int weight, int poolSize,
           String group) {
        super();
        this.host = host;
        this.port = port;
        this.interfaceName = interfaceName;
        this.timeout = timeout;
        this.weight = weight;
        this.poolSize = poolSize;
        this.group = group;
    }

    
    
    public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}


}
