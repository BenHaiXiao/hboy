package com.github.hboy.center.remoting.exchange;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 11:15
 */
public class Response {
    
	
	/**
     * 成功
     */
    public static final byte OK                = 20;

    /**
     *超时 timeout.
     */
    public static final byte TIMEOUT		   = 30;
    
    /**
     *  错误
     */
    public static final byte BAD_ERROR		   = 50;

    private int              mId               = 0;

    private byte             mStatus           = OK;

    private String           mErrorMsg;
    
    private Throwable        mError;

    private Object           mResult;
    
    private String           mServiceName;
    
    private String           mMethodName;

    public Response(){
    }

    public Response(int id){
        mId = id;
    }
 

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }


    public byte getStatus() {
        return mStatus;
    }

    public void setStatus(byte status) {
        mStatus = status;
    }

    public Object getResult() {
        return mResult;
    }

    public void setResult(Object msg) {
        mResult = msg;
    }

    public String getErrorMessage() {
        return mErrorMsg;
    }

    public void setErrorMessage(String msg) {
        mErrorMsg = msg;
    }

    public String getServiceName() {
		return mServiceName;
	}

	public void setServiceName(String serviceName) {
		this.mServiceName = serviceName;
	}
	
	public String getMethodName() {
		return mMethodName;
	}

	public void setMethodName(String methodName) {
		this.mMethodName = methodName;
	}
	
	public Throwable getError() {
		return mError;
	}

	public void setError(Throwable mError) {
		this.mError = mError;
	}

	@Override
	public String toString() {
		return "Response [mId=" + mId + ", mStatus=" + mStatus + ", mErrorMsg="
				+ mErrorMsg + ", mError=" + mError + ", mResult=" + mResult
				+ ", mServiceName=" + mServiceName + ", mMethodName="
				+ mMethodName + "]";
	}
		
	
 
}