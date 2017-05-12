package org.gwisoft.jkubernetes.daemon.kubelet;

import java.io.Serializable;
import java.util.Set;

public class KubeletHeartbeat implements Serializable {

	private static final long serialVersionUID = 2031331515718221372L;
	
	private final String hostName;
    private final String kubeletId;

    private Integer timeSecs;
    private Integer uptimeSecs;

    private Set<Integer> podIds;

    private Set<Integer> availableWorkerPorts;
    
    public KubeletHeartbeat(String hostName,String kubeletId){
    	this.hostName = hostName;
    	this.kubeletId = kubeletId;
    }

	public Integer getTimeSecs() {
		return timeSecs;
	}

	public void setTimeSecs(Integer timeSecs) {
		this.timeSecs = timeSecs;
	}

	public Integer getUptimeSecs() {
		return uptimeSecs;
	}

	public void setUptimeSecs(Integer uptimeSecs) {
		this.uptimeSecs = uptimeSecs;
	}

	public Set<Integer> getPodIds() {
		return podIds;
	}

	public void setPodIds(Set<Integer> podIds) {
		this.podIds = podIds;
	}

	public Set<Integer> getAvailableWorkerPorts() {
		return availableWorkerPorts;
	}

	public void setAvailableWorkerPorts(Set<Integer> availableWorkerPorts) {
		this.availableWorkerPorts = availableWorkerPorts;
	}

	public String getHostName() {
		return hostName;
	}

	public String getKubeletId() {
		return kubeletId;
	}

    
}
