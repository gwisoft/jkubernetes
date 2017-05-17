package org.gwisoft.jkubernetes.cluster;

public class KubernetesCluster {
	
	public static final String ZK_SEPERATOR = "/";
	private static final String MASTER_ROOT = "kube_master";
	private static final String ASSIGNMENTS_ROOT = "assignments";	
	private static final String KUBE_SLAVE_ROOT = "kube_slave";
	private static final String KUBE_SLAVE_DETAIL_ROOT= "kube_slave_detail";
	
	private static final String KUBELET_HEARTBEAT_ROOT = "kubelet_heartbeat_root";
	
	public static final String MASTER_SUBTREE;
	public static final String ASSIGNMENTS_SUBTREE;
	public static final String KUBE_SLAVE_DETAIL_SUBTREE;
	public static final String KUBE_SLAVE_SUBTREE;
	public static final String KUBELET_HEARTBEAT_SUBTREE;
	
	static{
		MASTER_SUBTREE = ZK_SEPERATOR + MASTER_ROOT;
		ASSIGNMENTS_SUBTREE = ZK_SEPERATOR + ASSIGNMENTS_ROOT;
		KUBE_SLAVE_DETAIL_SUBTREE = ZK_SEPERATOR + KUBE_SLAVE_DETAIL_ROOT;
		KUBE_SLAVE_SUBTREE = ZK_SEPERATOR + KUBE_SLAVE_ROOT;
		KUBELET_HEARTBEAT_SUBTREE = ZK_SEPERATOR + KUBELET_HEARTBEAT_ROOT;
	}

	private static KubernetesClusterCoordination coordination;
	
	/**
	 * get cluster coordination
	* @Title: instanceCoordination 
	* @return
	 */
	public static KubernetesClusterCoordination instanceCoordination(){
		if(coordination == null){
			synchronized (KubernetesCluster.class) {
				if(coordination == null){
					coordination = new KubernetesZkClusterCoordination();
				}
			}
		}
		
		return coordination;
	}
	
	public static String getAssignmentPath(String topologyId){
		return ASSIGNMENTS_SUBTREE + ZK_SEPERATOR + topologyId;
	}
	
	public static String getKubeletHeartbeat(String kubeletId){
		return KUBELET_HEARTBEAT_SUBTREE + ZK_SEPERATOR + kubeletId;
	}
}
