package org.gwisoft.jkubernetes.daemon.pod;

import java.util.HashMap;

import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.docker.KubernetesDocker;
import org.gwisoft.jkubernetes.docker.KubernetesDockerJava;
import org.gwisoft.jkubernetes.docker.ResourcePodSlotCommand;
import org.gwisoft.jkubernetes.docker.ResourcePodSlotDocker;
import org.gwisoft.jkubernetes.exception.BusinessException;
import org.gwisoft.jkubernetes.utils.DefaultUncaughtExceptionHandler;
import org.gwisoft.jkubernetes.utils.ExecCommandUtils;
import org.gwisoft.jkubernetes.utils.ExecCommandUtils.ExecCommandASynCallBack;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Pod {
	
	private static final Logger logger = LoggerFactory.getLogger(Pod.class);
	
	private PodData podData;
	
	public static final String RESOURCE_POD_SLOT = "ResourcePodSlot";

	public static void main(String[] args) {
		try{

			ResourcePodSlot slot = getResourcePodSlot();
			
			String topologyId = slot.getTopologyId();
	        String kubeletId = slot.getKubeletId();
	        Integer podId = slot.getPodId();
	        
	        logger.info("**********starting pod: (topologyId:"  + topologyId + ",kubeletId:" + kubeletId + ",podId:" + podId + ")**************");
	        
	        //set uncaught exception to main thread
			Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
			
			//load Kubernetes config to memory
		    KubernetesConfigLoad.initKubernetesConfig();
		    
		    //pid notification kubelet
		    notificationKubeletByPid(podId);
		    
		    //start pod node
		    Pod instance = new Pod(topologyId,kubeletId,podId,slot);
		    instance.launchPod();
		}catch(Throwable e){
			logger.error("",e);
			throw new BusinessException(e);
		}

		
	}
	
	public static ResourcePodSlot getResourcePodSlot(){
		ResourcePodSlot slot = null;
		Gson gson = new GsonBuilder().create();
		
		String pod = System.getenv(RESOURCE_POD_SLOT);
		if(pod != null && !pod.isEmpty()){
			slot =  gson.fromJson(pod, ResourcePodSlot.class);
		}
		
		return slot;
	}
	
	public Pod(String topologyId,String kubeletId,Integer podId,ResourcePodSlot slot){
		podData = new PodData(topologyId,kubeletId,podId,slot);
	}
	
	private void launchPod(){
		
		//start heartbeat
		PodHeartbeatRunable podHBRunable = new PodHeartbeatRunable(podData);
		Thread podThread = new Thread(podHBRunable);
		podThread.start();
		
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
