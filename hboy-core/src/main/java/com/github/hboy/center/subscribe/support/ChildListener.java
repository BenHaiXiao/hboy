package com.github.hboy.center.subscribe.support;

import java.util.List;
/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 16:37
 */
public interface ChildListener {

	void childChanged(String path, List<String> children);

}
