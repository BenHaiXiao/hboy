package com.github.hboy.center.remoting;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.hboy.common.config.InvokerConfig;


public class EventHandlerWrapperTest {
    
    
    ChannelEventHandler  handler =  new ChannelEventHandler(){

        @Override
        public void connected(Channel client) throws RemotingException {
            System.out.println("invoker-------------");
        }

        @Override
        public void disconnected(Channel client) throws RemotingException {
        }

        @Override
        public void sent(Channel client, Object message) throws RemotingException {
        }

        @Override
        public void received(Channel client, Object message) throws RemotingException {
        }

        @Override
        public void caught(Channel client, Throwable exception) throws RemotingException {
        }
        
    };
    List<ChannelEventHandler> eventHandlers;
    
    @Before
    public void before() throws Exception {
        
        eventHandlers = new ArrayList<ChannelEventHandler>();
        eventHandlers.add(new ChannelEventHandler() {
            @Override
            public void connected(Channel client) throws RemotingException {
                System.out.println("11111111111111");
            }

            @Override
            public void disconnected(Channel client) throws RemotingException {
            }

            @Override
            public void sent(Channel client, Object message) throws RemotingException {
            }

            @Override
            public void received(Channel client, Object message) throws RemotingException {
            }

            @Override
            public void caught(Channel client, Throwable exception) throws RemotingException {
            }
            
        });
        eventHandlers.add(new ChannelEventHandler() {

            @Override
            public void connected(Channel client) throws RemotingException {
                System.out.println("222222222222222");
            }

            @Override
            public void disconnected(Channel client) throws RemotingException {
            }

            @Override
            public void sent(Channel client, Object message) throws RemotingException {
            }

            @Override
            public void received(Channel client, Object message) throws RemotingException {
            }

            @Override
            public void caught(Channel client, Throwable exception) throws RemotingException {
            }
            
        });
    }
    
    @Test
    public void testRetrunException() throws Throwable{
        ChannelEventHandler t = EventHandlerWrapper.buildEventHandlerChain(handler, eventHandlers);
        t.connected(new Channel() {
            @Override
            public void send(Object message) throws RemotingException {
            }
            @Override
            public boolean isConnected() {
                return false;
            }
            @Override
            public InvokerConfig getUrl() {
                return null;
            }
            @Override
            public SocketAddress getRemoteAddress() {
                return null;
            }
            @Override
            public SocketAddress getLocalAddress() {
                return null;
            }
            @Override
            public void close() {
            }
        });
//        Assert.assertEquals(invokerAgent, t);
    }
}
