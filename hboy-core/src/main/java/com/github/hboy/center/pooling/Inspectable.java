package com.github.hboy.center.pooling;


/**
 * 监控组件是否正常工作正常的接口。用于InitalizableObjectPool，监控对象池是否正常。
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 16:47
 */
public interface Inspectable {
    public boolean isAlive();
}
