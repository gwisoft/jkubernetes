package org.gwisoft.jkubernetes.schedule;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gwisoft.jkubernetes.apiserver.yaml.ApiServerYaml;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;


public class Assignment implements Serializable {

	private static final long serialVersionUID = 7085695686147515767L;
	
	private final Set<ResourcePodSlot> pods;
	
	private String topologyId;
	
	private String topologyName;

	private ApiServerYaml apiServerYaml;
	
	//assignment timestamp
	private long timestamp;
	
	public String getTopologyName() {
		return topologyName;
	}

	public void setTopologyName(String topologyName) {
		this.topologyName = topologyName;
	}

	public Assignment(String topologyId,String topolgyName,Set<ResourcePodSlot> pods,ApiServerYaml apiServerYaml,long timestamp){
		this.topologyId = topologyId;
		this.pods = pods;
		this.apiServerYaml = apiServerYaml;
		this.timestamp = timestamp;
		this.topologyName = topolgyName;
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

	public Set<ResourcePodSlot> getPods() {
		return pods;
	}



	public ApiServerYaml getApiServerYaml() {
		return apiServerYaml;
	}

	public void setApiServerYaml(ApiServerYaml apiServerYaml) {
		this.apiServerYaml = apiServerYaml;
	}

	public boolean isUpdateChange(long oldTimestamp){
		if(timestamp > oldTimestamp){
			return true;
		}else{
			return false;
		}
	}
	
}
