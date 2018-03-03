package com.github.hboy.center.subscribe;


/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 11:19
 */
public class SubscribeException extends RuntimeException {

	private static final long serialVersionUID = 7815426752583648734L;


    public SubscribeException() {
        super();
    }

    public SubscribeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscribeException(String message) {
        super(message);
    }

    public SubscribeException(Throwable cause) {
        super(cause);
    }

}