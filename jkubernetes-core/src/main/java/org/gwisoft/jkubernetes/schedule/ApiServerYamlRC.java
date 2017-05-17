package org.gwisoft.jkubernetes.schedule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwisoft.jkubernetes.apiserver.ApiServerConstant;
import org.gwisoft.jkubernetes.apiserver.ApiServerYamlAnalyzer;
import org.gwisoft.jkubernetes.apiserver.yaml.PodContainer;
import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignEvent;
import org.gwisoft.jkubernetes.daemon.kubelet.KubeletHeartbeat;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.docker.ResourcePodSlotCommand;
import org.gwisoft.jkubernetes.docker.ResourcePodSlotDocker;
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
		HashMap<String, ResourcePodSlot> keeperDeployed = new HashMap<String, ResourcePodSlot>();
		if(context.getEvent().getAssignType().equals(TopologyAssignEvent.AssignType.anewAssign)){
			oldAssignment = coordination.getAssignment(context.getTopologyId());
			for(KubeletHeartbeat hb:hbs){
				Set<ResourcePodSlot> pods = oldAssignment.getPods();
				for(ResourcePodSlot slot:pods){
					KubeletHeartbeat hb1 = hbMap.get(slot.getKubeletId());
					if(hb1 != null && hb1.getPodIds().contains(slot.getPodId())){
						keeperDeployed.put(slot.getKubeletId() + slot.getPodId(), slot);
						hb1.getUnassignedPodIds().add(slot.getPodId());
					}
				}
			}
		}
		
		
		Set<ResourcePodSlot> assigned = new HashSet<ResourcePodSlot>();
		KubeletHeartbeat maxWeight;
		Integer taskNum = context.getApiServerYaml().getSpec().getReplicas();
		for(int i = 0 ;i < taskNum;i++){
			maxWeight = null;
			Iterator iterator = hbMap.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry entry = (Map.Entry) iterator.next();
				KubeletHeartbeat hb = (KubeletHeartbeat)entry.getValue();
				
				if(hb.getUnassignedPodIds() == null || hb.getUnassignedPodIds().size() == 0){
					continue;
				}
				
				if(maxWeight == null){
					maxWeight = hb;
					continue;
				}
				
				if(hb.getUnassignedPodIds().size() > maxWeight.getUnassignedPodIds().size()){
					maxWeight = hb;
				}
			}
			
			if(maxWeight == null){
				throw new BusinessException("not vaild resource!");
			}
			
			// balance keeper old slot
			ResourcePodSlot oldSlot = keeperDeployed.get(maxWeight.getKubeletId());
			if(oldSlot == null){
				ResourcePodSlot slot = null;
				if(getPodType().equals(ResourcePodSlot.PodType.docker)){
					ResourcePodSlotDocker dockerSlot = new ResourcePodSlotDocker();
					dockerSlot.setTemplateSpec(context.getApiServerYaml().getSpec().getTemplate().getSpec());
					slot = dockerSlot;
				}else{
					ResourcePodSlotCommand commandSlot = new ResourcePodSlotCommand();
					String command = context.getApiServerYaml().getSpec().getTemplate().getSpec().getCommand();
					commandSlot.setRunCommand(command);
					slot = commandSlot;
				}
				
				Iterator<Integer> iterable = maxWeight.getUnassignedPodIds().iterator();
				Integer podId = iterable.next();
				slot.setPodId(podId);
				slot.setHostname(maxWeight.getHostName());
				slot.setState(ResourcePodSlot.AssignmentState.Assignmenting);
				slot.setTopologyId(context.getTopologyId());
				slot.setTimestamp(context.getEvent().getTimestamp());
				slot.setKubeletId(maxWeight.getKubeletId());
				maxWeight.getUnassignedPodIds().remove(podId);
				assigned.add(slot);
			}else{
				keeperDeployed.remove(oldSlot.getKubeletId() + oldSlot.getPodId());
				assigned.add(oldSlot);
			}
			
		}

		return assigned;
		
	}
	
	public ResourcePodSlot.PodType getPodType(){
		List<PodContainer> containers = context.getApiServerYaml().getSpec().getTemplate().getSpec().getContainers();
		if(containers != null){
			return ResourcePodSlot.PodType.docker;
		}
		
		String command = context.getApiServerYaml().getSpec().getTemplate().getSpec().getCommand();
		if(command != null && !command.isEmpty()){
			return ResourcePodSlot.PodType.command;
		}
		
		throw new BusinessException("nonsupport pod type (support type:" + ResourcePodSlot.PodType.values() +")");
	}
	

}
