package com.github.hboy.center.filter;
/**
 * @author xiaobenhai
 * Date: 2016/3/18
 * Time: 10:57
 */
public class AccessAuthException extends Exception {
	
	private static final long serialVersionUID = 608130562921113220L;

	// 禁止权限
	 public static final int AUTH_FORBIDDEN = 0;
	 
	 private int code ; 
	 
    public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public AccessAuthException() {
        super();
    }

    public AccessAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessAuthException(String message) {
        super(message);
    }

    public AccessAuthException(Throwable cause) {
        super(cause);
    }

    public AccessAuthException(int code) {
        super();
        this.code = code;
    }

    public AccessAuthException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public AccessAuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AccessAuthException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }
}
