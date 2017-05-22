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
import org.gwisoft.jkubernetes.utils.JsonUtils;
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

			HashMap<String, KubeletHeartbeat> vaildPods = new HashMap<String, KubeletHeartbeat>();
			List<KubeletHeartbeat> hbs = coordination.getValidKubeletHeartbeats();
			for(KubeletHeartbeat hb:hbs){
				vaildPods.put(hb.getKubeletId(), hb);
			}
			
			boolean isAnew = false;
			for(String topologyId:assignments){
				
				Assignment assignment = coordination.getAssignment(topologyId);
				
				updateTopologyState(assignment,vaildPods);
				
				Iterator<ResourcePodSlot> iterator = assignment.getPods().iterator();
				while(iterator.hasNext()){
					ResourcePodSlot slot = iterator.next();
					
					KubeletHeartbeat hb = vaildPods.get(slot.getKubeletId());
					if(hb == null || !hb.getPodIds().contains(slot.getPodId())){
						logger.warn("*************resource: kubelet_id=" + slot.getKubeletId() 
							+ " pod_id=" + slot.getPodId() + " Invaild,it will need restart assignment!***********");
						logger.warn("assigned_topology_name=" + assignment.getTopologyName() + " old_resource=" 
								+ JsonUtils.toJson(assignment.getPods()) 
								+ " vaild_resource:" + JsonUtils.toJson(vaildPods));
						
						isAnew = true;
						break;
					}
				}
				
				if(isAnew){
					TopologyAssignEvent assignEvent = new TopologyAssignEvent();
			        assignEvent.setTopologyId(topologyId);
			        assignEvent.setTopologyName(assignment.getTopologyName());
			        assignEvent.setApiServerYaml(assignment.getApiServerYaml());
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
		}catch(Throwable e){
			logger.error("",e);
		}
		
		

	}
	
	public void updateTopologyState(Assignment assignment,HashMap<String, KubeletHeartbeat> vaildPods){
		for(ResourcePodSlot slot:assignment.getPods()){
			KubeletHeartbeat hb = vaildPods.get(slot.getKubeletId());
			if(hb != null && hb.getAvailablePodHeartbeats().get(slot.getPodId()) != null){
				slot.setState(ResourcePodSlot.AssignmentState.AssignmentSuccess);
			}		
		}
		
		coordination.setAssignment(assignment);
	}
	
	
}
