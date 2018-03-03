package com.github.hboy.center.subscribe.support;

/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 14:45
 */
public interface DataListener {

    public void dataChange(String dataPath, String data) throws Exception;

    public void dataDeleted(String dataPath) throws Exception;

}
