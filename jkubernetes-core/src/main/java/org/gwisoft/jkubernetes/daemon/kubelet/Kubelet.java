package org.gwisoft.jkubernetes.daemon.kubelet;

import java.io.IOException;
import java.util.UUID;

import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.utils.DefaultUncaughtExceptionHandler;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kubelet {
	
	private static final Logger logger = LoggerFactory.getLogger(Kubelet.class);

	public static void main(String[] args){
		
		testRun();
		
		Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
		
		Kubelet instance = new Kubelet();
		
		instance.run();
	}
	
	public static void testRun(){
		if(System.getProperty("kubernetes.home") == null){
			System.setProperty("kubernetes.home", "C:\\Users\\Lincm\\git\\jkubernetes\\jkubernetes-all\\jkubernetes-core\\target");
		}
	}
	
	public void run(){
		try{
			KubernetesConfig.validateDistributedMode();
			
			//load Kubernetes config to memory
		    KubernetesConfigLoad.initKubernetesConfig();
		    
		    //kubelet Id
		    String kubeletId = createKubeletId();
		    
		    //kubelet heartbeat
		    KubeletHeartbeatRunnable kubeletHBRunnable = new KubeletHeartbeatRunnable(kubeletId);
		    Thread kubeletHBThread = new Thread(kubeletHBRunnable);
		    kubeletHBThread.start();
		    
		    //assign resource
		    KubeletAssignRunnable kubeletAssignRunnable = new KubeletAssignRunnable(kubeletId);
		    Thread kubeletAssignThread = new Thread(kubeletAssignRunnable);
		    kubeletAssignThread.start();
		    
			while(true){
				Thread.sleep(1000);
			}
		}catch(Exception e){
			logger.error("",e);
		}
		
	}
	
	public static String createKubeletId(){
		String pidsDir;
		try {
			//kubelet uniqueness
			pidsDir = KubernetesConfig.getKubeletPidDir();
			String pid = KubernetesUtils.createPid(pidsDir);
			
			//
			logger.debug("successful create pid");
			return UUID.randomUUID().toString();
		} catch (IOException e) {
			logger.error("failed to create pid",e);
			throw new RuntimeException(e);
		}
		
	}
}
