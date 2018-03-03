package com.github.hboy.center.filter;

import java.util.ArrayList;
import java.util.List;

import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.Invoker;
import com.github.hboy.demo.Scribe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.hboy.common.config.InvokerConfig;


public class FilterWrapperTest {
    
    
    Invoker<?> invoker;
    List<Filter> userFilters;
    
    private String  rs = "test Invoker";
    
    @Before
    public void before() throws Exception {
        invoker = new Invoker<Scribe>() {

            @Override
            public Class<Scribe> getInterface() {
                return null;
            }

            @Override
            public InvokerConfig getUrl() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                return rs;
            }

            @Override
            public boolean isAvailable() {
                // TODO Auto-generated method stub
                return false;
            }
            @Override
            public void destroy() {
            }
        };
        userFilters = new ArrayList<Filter>();
        userFilters.add(new Filter() {
            @Override
            public Object invoke(Invoker<?> invoker, Invocation invocation) throws Throwable {
                System.out.println("Filter 1111111");
                return invoker.invoke(invocation);
            }
        });
        userFilters.add(new Filter() {
            @Override
            public Object invoke(Invoker<?> invoker, Invocation invocation) throws Throwable {
                System.out.println("Filter 222222");
                return invoker.invoke(invocation);
            }
        });
    }
    
    @Test
    public void testRetrunException() throws Throwable{
        Invoker<?> t = FilterWrapper.buildFilterChain(invoker, userFilters);
        Assert.assertEquals(t.invoke(null), rs);
//        Assert.assertEquals(invokerAgent, t);
    }
}
