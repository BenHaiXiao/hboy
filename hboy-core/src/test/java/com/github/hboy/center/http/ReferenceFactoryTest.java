package com.github.hboy.center.http;

import java.util.ArrayList;
import java.util.List;

import com.github.hboy.center.ReferenceFactory;
import com.github.hboy.common.config.LocalConfig;
import com.github.hboy.common.util.Constants;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReferenceFactoryTest {

    private HttpClientService service;

    @Before
    public void init(){
        ArrayList<LocalConfig> localConfigs = new ArrayList<LocalConfig>();
	    LocalConfig l = new LocalConfig();
	    l.setHost("127.0.0.1");
	    l.setPort(8080);
	    localConfigs.add(l);

	    ReferenceFactory<HttpClientService> rf = new ReferenceFactory<HttpClientService>();
	    rf.setInterface(HttpClientService.class);
	    rf.setLocalConfigs(localConfigs);
	    rf.setProtocol(Constants.ServiceProtocolType.HTTP);
	    service = rf.getClient();
    }

    @Test
    public void testPrimitive(){
        User user = service.getUser(123L, 1, "James", false);
        Assert.assertEquals(123L,    user.getUid());
        Assert.assertEquals(1,       user.getSex());
        Assert.assertEquals("James", user.getNick());
        Assert.assertEquals(false,   user.isVIP());
    }

    @Test
    public void testCollection(){
        List<LoginLog> logs = new ArrayList<LoginLog>();
        logs.add(new LoginLog(123L, "测试中文！"));
        logs.add(new LoginLog(123L, "just test..."));
        service.reportLoginLogs(logs);
    }

    @After
    public void tearDown(){
    }

}