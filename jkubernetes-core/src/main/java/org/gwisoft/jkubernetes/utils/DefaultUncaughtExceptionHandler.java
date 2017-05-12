package org.gwisoft.jkubernetes.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: DefaultUncaughtExceptionHandler
 * @author: Lincm
 * @Description: 主线程未捕获异常处理
 * @date: 2017年3月28日 下午3:54:44
 *
 */
public class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{

	private static final Logger logger = LoggerFactory.getLogger(DefaultUncaughtExceptionHandler.class);
	
	public void uncaughtException(Thread t, Throwable e) {
		if(e != null && e instanceof Error){
			if(e instanceof OutOfMemoryError){
				System.err.println("Halting due to Out Of Memory Error..." + Thread.currentThread().getName());
				Runtime.getRuntime().halt(-1);
			}else{
				logger.info("Received error in main thread.. terminating server...",e);
	            Runtime.getRuntime().exit(-2);
			}
		}else{
			logger.error("",e);
		}
		
	}

}
