package com.github.hboy.center.remoting;


/**
 * @author xiaobenhai
 * Date: 2016/3/15
 * Time: 10:31
 */
public class RemotingException extends Exception {

	private static final long serialVersionUID = 7815426752583648734L;

    public static final int UNKNOWN_EXCEPTION = 0;
    
    public static final int NETWORK_EXCEPTION = 1;
    
    public static final int TIMEOUT_EXCEPTION = 2;
    
    public static final int BIZ_EXCEPTION = 3;
    
    public static final int SERIALIZATION_EXCEPTION = 4;
    
    private int code; // RemotingException不能有子类，异常类型用ErrorCode表示，以便保持兼容。

    public RemotingException() {
        super();
    }

    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemotingException(String message) {
        super(message);
    }

    public RemotingException(Throwable cause) {
        super(cause);
    }

    public RemotingException(int code) {
        super();
        this.code = code;
    }

    public RemotingException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public RemotingException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RemotingException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public boolean isBiz() {
        return code == BIZ_EXCEPTION;
    }

    public boolean isTimeout() {
        return code == TIMEOUT_EXCEPTION;
    }

    public boolean isNetwork() {
        return code == NETWORK_EXCEPTION;
    }

    public boolean isSerialization() {
        return code == SERIALIZATION_EXCEPTION;
    }
}