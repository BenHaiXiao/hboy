package com.github.hboy.common.exception;
/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 13:28
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = -9095702616213992961L;

    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
