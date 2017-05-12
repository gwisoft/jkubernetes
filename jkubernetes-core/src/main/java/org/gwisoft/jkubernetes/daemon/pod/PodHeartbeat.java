package org.gwisoft.jkubernetes.daemon.pod;

import java.io.Serializable;
import java.util.Set;

public class PodHeartbeat implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int timeSecs;
    private String topologyId;
    private Set<Integer> containerIds;
    private Integer podId;
    private String kubeletId;
    
    public PodHeartbeat(int timeSecs,String topologyId,Set<Integer> containerIds,Integer podId,String kubeletId){
    	this.timeSecs = timeSecs;
    	this.topologyId = topologyId;
    	this.containerIds = containerIds;
    	this.podId = podId;
    	this.kubeletId = kubeletId;
    }
    
	public String getKubeletId() {
		return kubeletId;
	}

	public void setKubeletId(String kubeletId) {
		this.kubeletId = kubeletId;
	}

	public int getTimeSecs() {
		return timeSecs;
	}
	public void setTimeSecs(int timeSecs) {
		this.timeSecs = timeSecs;
	}
	public String getTopologyId() {
		return topologyId;
	}
	public void setTopologyId(String topologyId) {
		this.topologyId = topologyId;
	}
	public Set<Integer> getContainerIds() {
		return containerIds;
	}
	public void setContainerIds(Set<Integer> containerIds) {
		this.containerIds = containerIds;
	}
	public Integer getPodId() {
		return podId;
	}
	public void setPodId(Integer podId) {
		this.podId = podId;
	}
    
    
}
