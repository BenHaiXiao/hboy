package com.github.hboy.center.remoting;


import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 18:31
 */
public class EventHandlerWrapper{

 
	public static ChannelEventHandler buildEventHandlerChain(final ChannelEventHandler porxyEventHandler, List<ChannelEventHandler> userEventHandlers) {
	    ChannelEventHandler porxy = porxyEventHandler;
        List<ChannelEventHandler> eventHandlers = new ArrayList<ChannelEventHandler>();
        if(userEventHandlers != null && userEventHandlers.size() != 0){
            eventHandlers.addAll(userEventHandlers);
        }
        if (eventHandlers.size() > 0) {
            for (int i = eventHandlers.size() - 1; i >= 0; i--) {
                final ChannelEventHandler handler  = eventHandlers.get(i);
                final ChannelEventHandler next = porxy;
                porxy = new ChannelEventHandler() {
                    @Override
                    public void connected(Channel client) throws RemotingException {
                        handler.connected(client);
                        next.connected(client);
                    }

                    @Override
                    public void disconnected(Channel client) throws RemotingException {
                        handler.disconnected(client);
                        next.disconnected(client);
                    }
                    @Override
                    public void sent(Channel client, Object message) throws RemotingException {
                        handler.sent(client,message);
                        next.sent(client,message);
                    }
                    @Override
                    public void received(Channel client, Object message) throws RemotingException {
                        handler.received(client,message);
                        next.received(client,message);
                    }
                    @Override
                    public void caught(Channel client, Throwable exception) throws RemotingException {
                        handler.caught(client,exception);
                        next.caught(client,exception);
                    }
                };
            }
        }
        return porxy;
    }
}
