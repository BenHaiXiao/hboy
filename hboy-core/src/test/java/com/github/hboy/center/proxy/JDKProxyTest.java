package com.github.hboy.center.proxy;


import com.github.hboy.base.thrift.protocol.ServiceException;
import com.github.hboy.center.remoting.Invoker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.hboy.common.config.InvokerConfig;
import com.github.hboy.demo.Scribe;


public class JDKProxyTest{

    
    private JDKProxy proxy;
    
    @Before
    public void before() throws Exception {
        proxy = new JDKProxy();
    }
    
    @Test
    public void testRetrunException(){
        Invoker<Scribe> invoker = new Invoker<Scribe> () {
            @Override
            public void destroy() {
            }
            @Override
            public Class<Scribe> getInterface() {
                return null;
            }
            @Override
            public InvokerConfig getUrl() {
                return null;
            }
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                 throw new ServiceException();
            }
            @Override
            public boolean isAvailable() {
                return false;
            }
        };
        Scribe map = proxy.getInvokerProxy(invoker, Scribe.class);
        try{
            map.testString("");  
            Assert.fail();
        }catch(Throwable e){
            if(!(e instanceof ServiceException)){
                Assert.fail();
            }
        } 
    }
    
    
    @Test
    public void testRetrunObject(){
        Invoker<Scribe> invoker = new Invoker<Scribe> () {
            @Override
            public void destroy() {
            }
            @Override
            public Class<Scribe> getInterface() {
                return null;
            }
            @Override
            public InvokerConfig getUrl() {
                return null;
            }
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                 return "testString";
            }
            @Override
            public boolean isAvailable() {
                return false;
            }
        };
        Scribe map = proxy.getInvokerProxy(invoker, Scribe.class);
        try{
            Assert.assertEquals(map.testString(""), "testString");  
        }catch(Throwable e){
            Assert.fail();
        } 
    }
    
}
