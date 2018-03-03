package com.github.hboy.center.remoting.codec;


import org.jboss.netty.buffer.ChannelBuffer;

import com.github.hboy.center.remoting.RemotingException;

/**
 *
 */
public interface Codec {

    
	/**
	 *
	 * @param buffer
	 * @return
	 * @throws RemotingException
	 */
    public Object decode(ChannelBuffer buffer) throws RemotingException;
    
    /**
     * 
     * @param msg
     * @return
     * @throws RemotingException
     */
    public ChannelBuffer encode(Object message) throws RemotingException;
}
