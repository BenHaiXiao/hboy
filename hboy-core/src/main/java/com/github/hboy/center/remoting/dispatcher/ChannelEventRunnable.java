package com.github.hboy.center.remoting.dispatcher;

import com.github.hboy.center.remoting.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hboy.center.remoting.ChannelEventHandler;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 23:31
 */
public class ChannelEventRunnable implements Runnable {
    private static final Logger logger             = LoggerFactory.getLogger(ChannelEventRunnable.class);

    private final ChannelEventHandler handler;
    private final Channel channel;
    private final ChannelState state;
    private final Throwable exception;
    private final Object message;
    
    public ChannelEventRunnable(Channel channel, ChannelEventHandler handler, ChannelState state) {
        this(channel, handler, state, null);
    }
    
    public ChannelEventRunnable(Channel channel, ChannelEventHandler handler, ChannelState state, Object message) {
        this(channel, handler, state, message, null);
    }
    
    public ChannelEventRunnable(Channel channel, ChannelEventHandler handler, ChannelState state, Throwable t) {
        this(channel, handler, state, null , t);
    }

    public ChannelEventRunnable(Channel channel, ChannelEventHandler handler, ChannelState state, Object message, Throwable exception) {
        this.channel = channel;
        this.handler = handler;
        this.state = state;
        this.message = message;
        this.exception = exception;
    }
    
    public void run() {
        switch (state) {
            case CONNECTED:
                try{
                    handler.connected(channel);
                }catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + state + " operation error, channel is " + channel, e);
                }
                break;
            case DISCONNECTED:
                try{
                    handler.disconnected(channel);
                }catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + state + " operation error, channel is " + channel, e);
                }
                break;
            case SENT:
                try{
                    handler.sent(channel,message);
                }catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + state + " operation error, channel is " + channel
                            + ", message is "+ message,e);
                }
                break;
            case RECEIVED:
                try{
                    handler.received(channel, message);
                }catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + state + " operation error, channel is " + channel
                            + ", message is "+ message,e);
                }
                break;
            case CAUGHT:
                try{
                    handler.caught(channel, exception);
                }catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + state + " operation error, channel is "+ channel
                            + ", message is: " + message + ", exception is " + exception,e);
                }
                break;
            default:
                logger.warn("unknown state: " + state + ", message is " + message);
        }
    }

    public enum ChannelState{
        
        /**
         * CONNECTED
         */
        CONNECTED,
        
        /**
         * DISCONNECTED
         */
        DISCONNECTED,
        
        /**
         * SENT
         */
        SENT,
        
        /**
         * RECEIVED
         */
        RECEIVED,
        
        /**
         * CAUGHT
         */
        CAUGHT
    }

}