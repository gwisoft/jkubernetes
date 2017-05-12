package org.gwisoft.jkubernetes.schedule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwisoft.jkubernetes.apiserver.ApiServerConstant;
import org.gwisoft.jkubernetes.apiserver.ApiServerYamlAnalyzer;
import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignEvent;
import org.gwisoft.jkubernetes.daemon.kubelet.KubeletHeartbeat;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.exception.BusinessException;

public class ApiServerYamlRC extends AbstractApiServerYaml {

	private TopologyAssignContext context;
	private static KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
	
	public ApiServerYamlRC(TopologyAssignContext context){
		this.context = context;
	}
	@Override
	public Set<ResourcePodSlot> process() {
		List<KubeletHeartbeat> hbs = coordination.getValidKubeletHeartbeats();
		HashMap<String, KubeletHeartbeat> hbMap = new HashMap<String, KubeletHeartbeat>();
		for(KubeletHeartbeat hb:hbs){
			hbMap.put(hb.getKubeletId(), hb);
		}
		
		//reset current old assignment resource
		Assignment oldAssignment = null;
		if(context.getEvent().getAssignType().equals(TopologyAssignEvent.AssignType.anewAssign)){
			oldAssignment = coordination.getAssignment(context.getTopologyId());
			for(KubeletHeartbeat hb:hbs){
				Set<ResourcePodSlot> pods = oldAssignment.getPods();
				for(ResourcePodSlot slot:pods){
					KubeletHeartbeat hb1 = hbMap.get(slot.getKubeletId());
					if(hb1 != null){
						hb1.getAvailableWorkerPorts().add(slot.getPodId());
					}
				}
			}
		}
		
		
		Set<ResourcePodSlot> assigned = new HashSet<ResourcePodSlot>();
		KubeletHeartbeat maxWeight;
		Integer taskNum = (Integer)ApiServerYamlAnalyzer.getYamlValue(ApiServerConstant.SPEC_REPLICAS, context.getYamlMap());
		for(int i = 0 ;i < taskNum;i++){
			maxWeight = null;
			Iterator iterator = hbMap.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry entry = (Map.Entry) iterator.next();
				KubeletHeartbeat hb = (KubeletHeartbeat)entry.getValue();
				
				if(hb.getAvailableWorkerPorts() == null || hb.getAvailableWorkerPorts().size() == 0){
					continue;
				}
				
				if(maxWeight == null){
					maxWeight = hb;
					continue;
				}
				
				if(hb.getAvailableWorkerPorts().size() > maxWeight.getAvailableWorkerPorts().size()){
					maxWeight = hb;
				}
			}
			
			if(maxWeight == null){
				throw new BusinessException("not vaild resource!");
			}
			
			ResourcePodSlot slot = new ResourcePodSlot();
			Iterator<Integer> iterable = maxWeight.getAvailableWorkerPorts().iterator();
			Integer podId = iterable.next();
			slot.setPodId(podId);
			slot.setHostname(maxWeight.getHostName());
			slot.setState(ResourcePodSlot.AssignmentState.Assignmenting);
			slot.setTopologyId(context.getTopologyId());
			slot.setTimestamp(context.getEvent().getTimestamp());
			slot.setKubeletId(maxWeight.getKubeletId());
			maxWeight.getAvailableWorkerPorts().remove(podId);
			assigned.add(slot);
		}

		return assigned;
		
	}

}
