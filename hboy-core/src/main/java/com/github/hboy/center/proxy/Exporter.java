package com.github.hboy.center.proxy;

import com.github.hboy.center.remoting.Invoker;

/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 10:30
 */
public interface Exporter<T> {
	
	
	/**
     * 
     */
    Invoker<T> getInvoker();
    
    /**
     */
    void unexport();
    
    
    
}
