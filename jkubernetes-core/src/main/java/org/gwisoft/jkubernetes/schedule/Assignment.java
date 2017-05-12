package org.gwisoft.jkubernetes.schedule;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;


public class Assignment implements Serializable {

	private static final long serialVersionUID = 7085695686147515767L;
	
	private final Set<ResourcePodSlot> pods;
	
	private String topologyId;

	private Map<String,Object> yamlMap;
	
	//assignment timestamp
	private long timestamp;
	
	public Assignment(String topologyId,Set<ResourcePodSlot> pods,Map yamlMap,long timestamp){
		this.topologyId = topologyId;
		this.pods = pods;
		this.yamlMap = yamlMap;
		this.timestamp = timestamp;
	}

	public ResourcePodSlot.AssignmentState getState(){
		ResourcePodSlot.AssignmentState state = ResourcePodSlot.AssignmentState.AssignmentSuccess;
		for(ResourcePodSlot slot:pods){
			if(slot.getState().equals(ResourcePodSlot.AssignmentState.AssignmentFail)){
				return ResourcePodSlot.AssignmentState.AssignmentFail;
			}
			if(slot.getState().equals(ResourcePodSlot.AssignmentState.Assignmenting)){
				return ResourcePodSlot.AssignmentState.Assignmenting;
			}
		}
		
		return state;
	}

	public String getTopologyId() {
		return topologyId;
	}

	public void setTopologyId(String topologyId) {
		this.topologyId = topologyId;
	}

	public Assignment(){
		this.pods = new HashSet<ResourcePodSlot>();
	}
	
	public Set<Integer> getCurrentPodContainerIds(String kubeletId, int podId){
		for(ResourcePodSlot slot:pods){
			if(slot.getKubeletId().equals(kubeletId) && slot.getPodId() == podId){
				return slot.getContainerIds();
			}
		}
		return new HashSet<Integer>();
	}

	public Set<ResourcePodSlot> getPods() {
		return pods;
	}

	public Map<String,Object> getYamlMap() {
		return yamlMap;
	}

	public void setYamlMap(Map<String,Object> yamlMap) {
		this.yamlMap = yamlMap;
	}

	public boolean isUpdateChange(long oldTimestamp){
		if(timestamp > oldTimestamp){
			return true;
		}else{
			return false;
		}
	}
	
}
