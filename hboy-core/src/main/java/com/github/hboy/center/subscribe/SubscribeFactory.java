 
package com.github.hboy.center.subscribe;

import com.github.hboy.center.subscribe.support.ZookeeperSubscribe;
import com.github.hboy.common.config.InvokerConfig;


/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 21:19
 */
public class SubscribeFactory {

    /**
     * 
     */
	private SubscribeFactory(){}
	
	public static Subscribe subscribe = null;
	
	public static java.util.concurrent.locks.Lock lock = new java.util.concurrent.locks.ReentrantLock(); 
	
    public static Subscribe getSubscribe(SubscribeType type,InvokerConfig url){
    	if(subscribe != null){
    		return subscribe;
    	}
    	try {
    		lock.lock();
    		if(subscribe != null){
        		return subscribe;
        	}
			if(type == SubscribeType.ZK){
				subscribe = new ZookeeperSubscribe(url);
			}else if (type == SubscribeType.LOACL){
//			subscribe = new LocalSubscribe(url);//LocalSubscribe
			}else{
				subscribe = new ZookeeperSubscribe(url);
			}
		} finally{
			lock.unlock();
		}
		return subscribe;
    }
    
    public static void destroy(){
    	if(subscribe != null){
    		subscribe.destroy();
    	}
	}
    
    public  enum SubscribeType{
    	ZK,LOACL;
    }
 
}