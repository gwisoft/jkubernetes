package org.gwisoft.jkubernetes.daemon.kubelet;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.daemon.pod.StatePodHeartbeat;
import org.gwisoft.jkubernetes.utils.DateUtils;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.gwisoft.jkubernetes.utils.NetWorkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KubeletHeartbeatRunnable implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(KubeletHeartbeatRunnable.class);
	
	private String kubeletId;
	
	private final int startTime;
	
	private KubeletHeartbeat heartbeat;
	
	private static KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
	
	private static AtomicBoolean shutdown = new AtomicBoolean(false);
	
	public KubeletHeartbeatRunnable(String kubeletId){
		this.kubeletId = kubeletId;
		this.startTime = DateUtils.getCurrentTimeSecs();
		
		initKubeletHeartbeat();
	}
	
	public void initKubeletHeartbeat(){
		String hostName = NetWorkUtils.getHostname();
		List<Integer> podIds = KubernetesConfig.getKubeletPodIdList();
		Set<Integer> podIdsSet = KubernetesUtils.listToSet(podIds);
		
		heartbeat = new KubeletHeartbeat(hostName, kubeletId);
	}
	@Override
	public void run() {
		
		while(!shutdown.get()){
			try{
				List<Integer> podList = calculatorAvailablePodIds();
		        Set<Integer> podIds = KubernetesUtils.listToSet(podList);
		        heartbeat.setPodIds(podIds);
		        heartbeat.setUnassignedPodIds(getUnassignedPodIds(podList));
		        heartbeat.setAvailablePodHeartbeats(KubeletLocalState.getValidPodHeartbeats());
		        
		        heartbeat.setTimeSecs(DateUtils.getCurrentTimeSecs());
				heartbeat.setUptimeSecs((int) (DateUtils.getCurrentTimeSecs() - startTime));
		        coordination.setKubeletHeartbeat(heartbeat);
		        
		        Integer interval = KubernetesConfig.getPodHeartbeatIntervalMs();
				Thread.sleep(interval);
			}catch(Throwable e){
				logger.error("",e);
			}
		}
		
		
	}
	
	private List<Integer> calculatorAvailablePodIds(){
		return KubernetesConfig.getKubeletPodIdList();
	}
	
	private Set<Integer> getUnassignedPodIds(List<Integer> podList){
		Set<Integer> unusedPodIds = new HashSet<Integer>();
		try {
			Map<Integer, ResourcePodSlot> hbMap = KubeletLocalState.getLocalAssignments(kubeletId);
			for(Integer podId:podList){
				if(hbMap.get(podId) == null){
					unusedPodIds.add(podId);
				}
			}
			
			return unusedPodIds;
		} catch (IOException e) {
			logger.error("",e);
			return unusedPodIds;
		}
	}

	public static AtomicBoolean getShutdown() {
		return shutdown;
	}

	public static void setShutdown(AtomicBoolean shutdown) {
		KubeletHeartbeatRunnable.shutdown = shutdown;
	}

}
