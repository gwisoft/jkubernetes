package org.gwisoft.jkubernetes.daemon.pod;

import java.io.Serializable;

public class PodHeartbeat implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int timeSecs;
    private String topologyId;
    private Integer podId;
    private String kubeletId;
    private ResourcePodSlot.PodType podType;
    
    public PodHeartbeat(int timeSecs,String topologyId,Integer podId,String kubeletId,ResourcePodSlot.PodType podType){
    	this.timeSecs = timeSecs;
    	this.topologyId = topologyId;
    	this.podId = podId;
    	this.kubeletId = kubeletId;
    	this.podType = podType;
    }
    
	public ResourcePodSlot.PodType getPodType() {
		return podType;
	}

	public void setPodType(ResourcePodSlot.PodType podType) {
		this.podType = podType;
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
	public Integer getPodId() {
		return podId;
	}
	public void setPodId(Integer podId) {
		this.podId = podId;
	}
    
    
}
