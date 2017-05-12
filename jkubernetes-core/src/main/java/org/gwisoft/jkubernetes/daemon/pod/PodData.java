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
	private String image;
	private Set<Integer> containerIds;
	
	private KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();;
	
	public PodData(String topologyId,String kubeletId,Integer podId,String image){
		this.topologyId = topologyId;
		this.kubeletId = kubeletId;
		this.podId = podId;
		this.image = image;
		
		Assignment assignment = coordination.getAssignment(topologyId);
		if (assignment == null) {
            String errMsg = "Failed to get Assignment of " + topologyId;
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
		
		containerIds = assignment.getCurrentPodContainerIds(kubeletId, podId);
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
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public Set<Integer> getContainerIds() {
		return containerIds;
	}
	public void setContainerIds(Set<Integer> containerIds) {
		this.containerIds = containerIds;
	}
	
	
}
