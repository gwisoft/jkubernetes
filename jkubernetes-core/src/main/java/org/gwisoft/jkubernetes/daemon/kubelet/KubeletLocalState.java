package org.gwisoft.jkubernetes.daemon.kubelet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.config.KubernetesConfigConstant;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.daemon.pod.PodHeartbeat;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.daemon.pod.StatePodHeartbeat;
import org.gwisoft.jkubernetes.docker.KubernetesDocker;
import org.gwisoft.jkubernetes.docker.KubernetesDockerFactory;
import org.gwisoft.jkubernetes.exception.BusinessException;
import org.gwisoft.jkubernetes.utils.DateUtils;
import org.gwisoft.jkubernetes.utils.ExecCommandUtils;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.gwisoft.jkubernetes.utils.PathUtils;
import org.gwisoft.jkubernetes.utils.SerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubeletLocalState {
	
	private static final Logger logger = LoggerFactory.getLogger(KubeletLocalState.class);
	
	private static Map kubernetesConfig = KubernetesConfigLoad.getKubernetesConfig();

	public static Map<Integer,PodHeartbeat> getValidPodHeartbeats(Map<Integer,StatePodHeartbeat> statePodHb){
		Map<Integer,PodHeartbeat> podHbs = new HashMap<Integer,PodHeartbeat>();
		try{
			Iterator<Map.Entry<Integer,StatePodHeartbeat>> sPHb = statePodHb.entrySet().iterator();
			while(sPHb.hasNext()){
				Map.Entry<Integer,StatePodHeartbeat> sPHbEntry = sPHb.next();
				StatePodHeartbeat sPodHb = sPHbEntry.getValue();
				if(sPodHb.getPodState().equals(StatePodHeartbeat.PodState.valid)){
					podHbs.put(sPHbEntry.getKey(), sPodHb.getPodHb());
				}
			}
		}catch(Exception e){
			logger.error("",e);
		}
		
		
		return podHbs;
	}
	
	public static Map<Integer,PodHeartbeat> getValidPodHeartbeats(){
		Map<Integer,StatePodHeartbeat> statePodHb = getStatePodHeartbeats();
		return getValidPodHeartbeats(statePodHb);
	} 
	
	public static Map<Integer,StatePodHeartbeat> getStatePodHeartbeats(){
		Map<Integer,StatePodHeartbeat> statePodHbs = new HashMap<Integer,StatePodHeartbeat>();
		String parentDir = KubernetesConfig.getLocalPodsRootDir();
		List<String> podIds = PathUtils.readSubFileNames(parentDir);
		for(String podId:podIds){
			String podIdFilePath = KubernetesConfig.getPodHeartbeatsFilePath(Integer.valueOf(podId));
			try {
				File file = new File(podIdFilePath);
				if(file.exists()){
					byte[] podHbByte = FileUtils.readFileToByteArray(file);
					
					PodHeartbeat podHb = (PodHeartbeat)SerializeUtils.javaDeserialize(podHbByte);
					StatePodHeartbeat.PodState podState = null;
					
					if(podHb.getPodType().equals(ResourcePodSlot.PodType.java_thread)){
						if(DateUtils.getCurrentTimeSecs() - podHb.getTimeSecs() > 
							(int)kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_KUBELET_POD_HEARTBEAT_TIMEOUT_SECS)){
							podState = StatePodHeartbeat.PodState.timeout;
						}else{
							podState = StatePodHeartbeat.PodState.valid;
						}
					}else if(podHb.getPodType().equals(ResourcePodSlot.PodType.command)){
						List<String> pids = KubeletLocalState.getPodPids(Integer.valueOf(podId));
						podState = StatePodHeartbeat.PodState.timeout;
						for(String pid:pids){
							boolean isExistRunning = ExecCommandUtils.isExistPidRunning(pid);
							if(isExistRunning){
								podState = StatePodHeartbeat.PodState.valid;
							}else{
								podState = StatePodHeartbeat.PodState.timeout;
							}
						}
					}else if(podHb.getPodType().equals(ResourcePodSlot.PodType.docker)){
						KubernetesDocker docker = KubernetesDockerFactory.getInstance();
						List<String> pids = KubeletLocalState.getPodPids(Integer.valueOf(podId));
						podState = StatePodHeartbeat.PodState.timeout;
						for(String pid:pids){
							boolean isExistRunning = docker.isRunningContainer(pid);
							if(isExistRunning){
								podState = StatePodHeartbeat.PodState.valid;
							}else{
								podState = StatePodHeartbeat.PodState.timeout;
							}
						}
					}else{
						throw new BusinessException("Pod type error,correct range(" + ResourcePodSlot.PodType.values() + ")");
					}
					
					StatePodHeartbeat sHb = new StatePodHeartbeat(podHb,podState);
					statePodHbs.put(podHb.getPodId(), sHb);
					
				}
				
			} catch (IOException e) {
				logger.error("",e);
				throw new BusinessException(e);
			}
		}
		
		return statePodHbs;
	}
	
	public static String getLocalKubeletId(){
		String pidsDir;
		try {
			pidsDir = KubernetesConfig.getKubeletPidDir();
			String pid = KubernetesUtils.createPid(pidsDir);
			logger.debug("successful create pid");
			return pid;
		} catch (IOException e) {
			logger.error("failed to create pid",e);
			throw new RuntimeException(e);
		}
	}
	
	public static Map<Integer, ResourcePodSlot> getLocalAssignments(String kubeletId) throws IOException{
		String kubeletAssignPath = KubernetesConfig.getKubeletAssignPath(kubeletId);
		
		File file = new File(kubeletAssignPath);
		if(file.exists()){
			byte[] data = FileUtils.readFileToByteArray(file);
			
			Map<Integer, ResourcePodSlot> assignments = (Map<Integer, ResourcePodSlot>)SerializeUtils.javaDeserialize(data);
			return assignments;
		}else{
			return new HashMap<Integer, ResourcePodSlot>();
		}
	}
	
	public static void setLocalAssignments(String kubeletId,Map<Integer, ResourcePodSlot> data) throws IOException{
		String kubeletAssignPath = KubernetesConfig.getKubeletAssignPath(kubeletId);
		File file = new File(kubeletAssignPath);
		
		byte[] dataByte = SerializeUtils.javaSerialize(data);
		FileUtils.writeByteArrayToFile(file, dataByte);
	}
	
	public static List<String> getPodPids(Integer podId){
		String PodPidPath = KubernetesConfig.getLocalPodPidsDir(podId);
		List<String> pids = PathUtils.readSubFileNames(PodPidPath);
		return pids;
	}
	
	public static void cleanupPidsByPodId(Integer podId){
		String PodPidPath = KubernetesConfig.getLocalPodPidsDir(podId);
		try {
			FileUtils.deleteDirectory(new File(PodPidPath));
		} catch (IOException e) {
			logger.error("",e);
		}
	}
	
	public static void cleanupInvalidPodHb(Integer podId){
		String podIdFilePath = KubernetesConfig.getPodHeartbeatsFilePath(Integer.valueOf(podId));
		File file = new File(podIdFilePath);
		try {
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			logger.warn("",e);
		}
	}
	
	
}
