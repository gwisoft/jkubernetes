package org.gwisoft.jkubernetes.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetWorkUtils {

	private static final Logger logger = LoggerFactory.getLogger(NetWorkUtils.class);
	
	public static String getHostname(){
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			logger.error("",e);
			throw new RuntimeException(e);
		}
	}
	
	public static String[] getIpAndPortByHttp(String httpPath){
		String[] ipPort = new String[2];
		if(httpPath == null || httpPath.trim().equals("")){
			ipPort[0] = "localhost";
			ipPort[1] = "8001";
			return ipPort;
		}
		
		int startIndex;
		String sub;
		if(httpPath.startsWith("http://")){
			sub = httpPath.substring(8);
			
		}else if(httpPath.startsWith("https://")){
			sub = httpPath.substring(9);
		}else{
			sub = httpPath;
		}
		String ip = sub.substring(0,httpPath.indexOf(":"));
		String port = sub.substring(httpPath.indexOf(":") + 1);
		ipPort[0] = ip;
		ipPort[1] = port;
		return  ipPort;
	}
}
