package com.github.hboy.center.filter;


import java.util.ArrayList;
import java.util.List;

import com.github.hboy.center.monitor.MonitorFilter;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/18
 * Time: 11:39
 */
public class FilterWrapper{

 
	public static <T> Invoker<T> buildFilterChain(final Invoker<T> invoker, List<Filter> userFilters) {
        Invoker<T> last = invoker;
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new ContextFilter());
        filters.add(new MonitorFilter());
        if(userFilters != null && userFilters.size() != 0){
            filters.addAll(userFilters);
        }
        if (filters.size() > 0) {
            for (int i = filters.size() - 1; i >= 0; i--) {
                final Filter filter = filters.get(i);
                final Invoker<T> next = last;
                last = new Invoker<T>() {
                    public Class<T> getInterface() {
                        return invoker.getInterface();
                    }
                    public InvokerConfig getUrl() {
                        return invoker.getUrl();
                    }
                    public boolean isAvailable() {
                        return invoker.isAvailable();
                    }
                    public Object invoke(Invocation invocation) throws Throwable {
                        return filter.invoke(next, invocation);
                    }
                    public void destroy() {
                        invoker.destroy();
                    }
                    public String toString() {
                        return invoker.toString();
                    }
                };
            }
        }
        return last;
    }
}
