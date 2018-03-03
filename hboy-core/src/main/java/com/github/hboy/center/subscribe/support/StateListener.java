package com.github.hboy.center.subscribe.support;
/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 19:58
 */
public interface StateListener {

	int DISCONNECTED = 0;

	int CONNECTED = 1;

	int RECONNECTED = 2;

	void stateChanged(int connected);

}
