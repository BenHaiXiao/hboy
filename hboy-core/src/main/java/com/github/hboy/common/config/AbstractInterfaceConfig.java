package com.github.hboy.common.config;

/**
 * @author xiaobenhai
 * Date: 2016/3/26
 * Time: 22:28
 */
public abstract class AbstractInterfaceConfig{
    
    private int timeout = 3000;	//超时时间
    
    private int weight = 5;		//如果有池,池权重大小
    
    private int maxActive = 8;	//最大可用连接

    
    public int getMaxActive() {
        return maxActive;
    }
    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
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
}
