package org.gwisoft.jkubernetes.cluster;

import java.util.List;
import java.util.Map;

import org.gwisoft.jkubernetes.daemon.kubelet.KubeletHeartbeat;
import org.gwisoft.jkubernetes.schedule.Assignment;

public interface KubernetesClusterCoordination {
	
	public boolean isKubeMasterExisted();
	
	public boolean tryToLeaderMaster();
	
	public boolean isCurrentMasterKube();
	
	public List<String> getAssignments();
	
	public boolean unregisterCurrentKubeFromSlave();
	
	public boolean registerCurrentKubeToSlave();
	
	public void setData(String path, byte[] data);
	
	public List<String> getAssignmentDiffFromLocalAndCluster();
	
	public List<String> getAllKubeSlaveHostPort();
	
	public Map getKubeSlaveDetail(String hostPort);
	
	public Assignment getAssignment(String topologyId);
	
	public Assignment getAssignmentByName(String topologyName);
	
	public void setAssignment(Assignment assignment);
	
	public void deleteAssignment(String topologyId);
	
	public void setKubeletHeartbeat(KubeletHeartbeat heartbeat);
	
	public List<KubeletHeartbeat> getValidKubeletHeartbeats();
	

}
