package org.gwisoft.jkubernetes.kubectl;

import java.io.File;
import java.nio.ByteBuffer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gwisoft.jkubernetes.kubectl.remote.ApiServerClient;
import org.gwisoft.jkubernetes.utils.NetWorkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubectlCreate {
	
	private static final String KUBERNETES_CREATE_YAML = "kubernetes.create.yaml";
	private static final String KUBERNETES_APISERVER_ADDRESS = "kubernetes.apiserver.address";
	
	private static final Logger logger = LoggerFactory.getLogger(KubectlCreate.class);

	public static void main(String[] args){
		setTestParam(args);
		start(args);
	}
	
	public static void start(String[] args){
		String yamlPath = System.getProperty(KUBERNETES_CREATE_YAML);
		if(yamlPath != null && !yamlPath.trim().equals("")){
			File file = FileUtils.getFile(yamlPath);
			if(!file.exists()){
				throw new RuntimeException(yamlPath + ",file is not exist");
			}
			
			String httpPath = System.getProperty(KUBERNETES_APISERVER_ADDRESS);
			String[] ipPorts = NetWorkUtils.getIpAndPortByHttp(httpPath);
			ApiServerClient client = new ApiServerClient(ipPorts[0],Integer.valueOf(ipPorts[1]));
	        try {
	        	byte[] yamlByte = FileUtils.readFileToByteArray(file);
	        	System.out.println(DigestUtils.md5Hex(yamlByte));
				ByteBuffer byteBuffer = ByteBuffer.wrap(yamlByte);
	        	String dataStr = new String(yamlByte,"utf-8");
	        	client.getClient().submitTopologyStr(dataStr);
	        	//client.getClient().submitTopology(byteBuffer);
	        } catch(Exception e){
	        	logger.error("",e);
	        	throw new RuntimeException("",e);
	        }finally {
	            client.close();
	        }
		}else{
			throw new RuntimeException("-f param is not exist");
		}
	}

	public static void setTestParam(String[] args){
		System.setProperty(KUBERNETES_CREATE_YAML, "C:\\Users\\Lincm\\git\\jkubernetes\\jkubernetes-all\\jkubernetes-core\\src\\test\\java\\org\\jkubernetes\\core\\kubectl\\test.yaml");
		System.setProperty(KUBERNETES_APISERVER_ADDRESS, "");
	}
}
