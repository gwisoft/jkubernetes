package org.gwisoft.jkubernetes.exception;

public class BusinessException extends RuntimeException {

	private static final String BACKGROUND_EXCEPTION = "-999";
	private String errorCode;
	
	public BusinessException(String errorCode,String msg){
		super(msg);
		this.errorCode = errorCode;
	}
	
	public BusinessException(String msg){
		super(msg);
		this.errorCode = BACKGROUND_EXCEPTION;
	}
	
	public BusinessException(String msg,Throwable cause){
		super(msg, cause);
		this.errorCode = BACKGROUND_EXCEPTION;
	}
	
	public BusinessException(Throwable cause){
		super(cause);
		this.errorCode = BACKGROUND_EXCEPTION;
	}
	
	public String getErrorCode(){
		return this.errorCode;
	}
	
	
}
