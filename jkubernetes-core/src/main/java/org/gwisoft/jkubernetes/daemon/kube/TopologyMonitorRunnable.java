package org.gwisoft.jkubernetes.daemon.kube;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gwisoft.jkubernetes.apiserver.ApiServerConstant;
import org.gwisoft.jkubernetes.apiserver.ApiServerYamlAnalyzer;
import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.daemon.kubelet.KubeletHeartbeat;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.exception.FailedAssignTopologyException;
import org.gwisoft.jkubernetes.schedule.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyMonitorRunnable implements Runnable {

	private static KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
	private static final Logger logger = LoggerFactory.getLogger(TopologyMonitorRunnable.class);

	
	@Override
	public void run() {
		logger.debug("TopologyMonitorRunnable running");
		try{
			List<String> assignments = coordination.getAssignments();

			boolean isAnew = false;
			for(String topologyId:assignments){
				
				HashMap<String, KubeletHeartbeat> vaildPods = new HashMap<String, KubeletHeartbeat>();
				List<KubeletHeartbeat> hbs = coordination.getValidKubeletHeartbeats();
				for(KubeletHeartbeat hb:hbs){
					vaildPods.put(hb.getKubeletId(), hb);
				}
				
				Assignment assignment = coordination.getAssignment(topologyId);
				Iterator<ResourcePodSlot> iterator = assignment.getPods().iterator();
				while(iterator.hasNext()){
					ResourcePodSlot slot = iterator.next();
					
					KubeletHeartbeat hb = vaildPods.get(slot.getKubeletId());
					if(hb == null || !hb.getPodIds().contains(slot.getPodId())){
						isAnew = true;
						break;
					}
				}
				
				if(isAnew){
					TopologyAssignEvent assignEvent = new TopologyAssignEvent();
			        assignEvent.setTopologyId(topologyId);
			        assignEvent.setTopologyName((String)ApiServerYamlAnalyzer.getYamlValue(ApiServerConstant.METADATA_NAME,assignment.getYamlMap()));
			        assignEvent.setYamlMap(assignment.getYamlMap());
			        assignEvent.setAssignType(TopologyAssignEvent.AssignType.anewAssign);

			        TopologyAssignRunnable.push(assignEvent);

			        boolean isSuccess = assignEvent.waitFinish();
			        if (isSuccess == true) {
			            logger.info("Finish submit for " + assignEvent.getTopologyName());
			        } else {
			        	logger.error(assignEvent.getErrorMsg() + " TopologyAssignEvent:" + assignEvent.toString());
			        }
				}
			}
			
			Integer interval = KubernetesConfig.getPodHeartbeatIntervalMs();
			Thread.sleep(interval);
		}catch(Exception e){
			logger.error("",e);
		}

	}
	
	
}
