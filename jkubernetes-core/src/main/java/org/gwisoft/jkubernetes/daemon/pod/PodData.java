package org.gwisoft.jkubernetes.daemon.pod;

import java.util.Set;

import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.schedule.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PodData {
	
	private static final Logger logger = LoggerFactory.getLogger(PodData.class);

	private String topologyId;
	private String kubeletId;
	private Integer podId;
	private ResourcePodSlot slot;
	
	public PodData(String topologyId,String kubeletId,Integer podId,ResourcePodSlot slot){
		this.topologyId = topologyId;
		this.kubeletId = kubeletId;
		this.podId = podId;
		this.slot = slot;
	}
	public String getTopologyId() {
		return topologyId;
	}
	public void setTopologyId(String topologyId) {
		this.topologyId = topologyId;
	}
	public String getKubeletId() {
		return kubeletId;
	}
	public void setKubeletId(String kubeletId) {
		this.kubeletId = kubeletId;
	}
	public Integer getPodId() {
		return podId;
	}
	public void setPodId(Integer podId) {
		this.podId = podId;
	}
	public ResourcePodSlot getSlot() {
		return slot;
	}
	public void setSlot(ResourcePodSlot slot) {
		this.slot = slot;
	}

	
	
	
}
