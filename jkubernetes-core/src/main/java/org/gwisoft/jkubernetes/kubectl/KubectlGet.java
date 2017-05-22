package org.gwisoft.jkubernetes.kubectl;

import java.io.File;
import java.nio.ByteBuffer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gwisoft.jkubernetes.kubectl.remote.ApiServerClient;
import org.gwisoft.jkubernetes.utils.JsonUtils;
import org.gwisoft.jkubernetes.utils.NetWorkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubectlGet {
	
	private static final String KUBERNETES_APISERVER_ADDRESS = "kubernetes.apiserver.address";
	
	private static final Logger logger = LoggerFactory.getLogger(KubectlGet.class);

	public static void main(String[] args){
		//args = setTestParam(args);
		start(args);
	}
	
	public static void start(String[] args){
		String httpPath = System.getProperty(KUBERNETES_APISERVER_ADDRESS);
		String[] ipPorts = NetWorkUtils.getIpAndPortByHttp(httpPath);
		ApiServerClient client = new ApiServerClient(ipPorts[0],Integer.valueOf(ipPorts[1]));
		
		if(args != null && args.length == 1){
			if(!args[0].equals("po")){
				throw new RuntimeException("param error");
			}
			try{
				String json = client.getClient().getTopologyInfoAll();
				System.out.println(JsonUtils.formatJson(json));
			}catch(Exception e){
				logger.error("",e);
	        	throw new RuntimeException("",e);
			}
			
		}else if(args != null && args.length == 2){
			if(!args[0].equals("po")){
				throw new RuntimeException("param error");
			}
			
			try{
				String topologyName = args[1];
				String json = client.getClient().getTopologyInfo(topologyName);
				System.out.println(JsonUtils.formatJson(json));
			}catch(Exception e){
				logger.error("",e);
	        	throw new RuntimeException("",e);
			}
			
		}else{
			throw new RuntimeException("param error");
		}
		
	}

	public static String[] setTestParam(String[] args){
		System.setProperty(KUBERNETES_APISERVER_ADDRESS, "");
		if(args == null || args.length < 1){
			args = new String[1];
			args[0] = "po";
			//args[1] = "test_app_1";
		}
		return args;
	}
}
