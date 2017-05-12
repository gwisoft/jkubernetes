package org.gwisoft.jkubernetes.daemon.pod;

import java.io.IOException;
import java.util.UUID;

import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.utils.DefaultUncaughtExceptionHandler;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pod {
	
	private static final Logger logger = LoggerFactory.getLogger(Pod.class);
	
	private PodData podData;

	public static void main(String[] args) {
		
		
		String topologyId = args[0];
        String kubeletId = args[1];
        Integer podId = Integer.valueOf(args[2]);
        String image = args[3];
        
        logger.info("**********starting pod: (topologyId:"  + topologyId + ",kubeletId:" + kubeletId + ",podId:" + podId + ",image:" + image + ")**************");
        
        //set uncaught exception to main thread
		Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
		
		//load Kubernetes config to memory
	    KubernetesConfigLoad.initKubernetesConfig();
	    
	    //pid notification kubelet
	    notificationKubeletByPid(podId);
	    
	    //start pod node
	    Pod instance = new Pod(topologyId,kubeletId,podId,image);
	    instance.launchPod();
	}
	
	public Pod(String topologyId,String kubeletId,Integer podId,String image){
		podData = new PodData(topologyId,kubeletId,podId,image);
	}
	
	private void launchPod(){
		
		PodHeartbeatRunable podHBRunable = new PodHeartbeatRunable(podData);
		Thread podThread = new Thread(podHBRunable);
		podThread.start();
		
		//start container
		try {
			podThread.join();
		} catch (InterruptedException e) {
			logger.error("",e);
		}
	}
	
	public static void notificationKubeletByPid(Integer podId){
		String pidsDir;
		try {
			//podId uniqueness
			pidsDir = KubernetesConfig.getLocalPodPidsDir(podId);
			String pid = KubernetesUtils.createPid(pidsDir);
			
			//
			logger.debug("successful create pid");
		} catch (Exception e) {
			logger.error("failed to create pid",e);
			throw new RuntimeException(e);
		}
		
	}

}
