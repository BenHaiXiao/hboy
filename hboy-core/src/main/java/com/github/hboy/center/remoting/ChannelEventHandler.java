package com.github.hboy.center.remoting;



/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 14:31
 */
public interface ChannelEventHandler {

	/**
	 * 
	 * @param client
	 * @throws RemotingException
	 */
    void connected(Channel client) throws RemotingException;

    /**
     * 
     * @param client
     * @throws RemotingException
     */
    void disconnected(Channel client) throws RemotingException;

    /**
     * 
     * @param client
     * @param message
     * @throws RemotingException
     */
    void sent(Channel client, Object message) throws RemotingException;

    /**
     * 
     * @param client
     * @param message
     * @throws RemotingException
     */
    void received(Channel client, Object message) throws RemotingException;

    /**
     * 
     * @param client
     * @param exception
     * @throws RemotingException
     */
    void caught(Channel client, Throwable exception) throws RemotingException;


}
