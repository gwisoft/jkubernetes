package org.gwisoft.jkubernetes.cluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.config.KubernetesConfigConstant;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.daemon.kube.FollowerRunnable;
import org.gwisoft.jkubernetes.daemon.kubelet.KubeletHeartbeat;
import org.gwisoft.jkubernetes.exception.BusinessException;
import org.gwisoft.jkubernetes.schedule.Assignment;
import org.gwisoft.jkubernetes.utils.DateUtils;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.gwisoft.jkubernetes.utils.PathUtils;
import org.gwisoft.jkubernetes.utils.SerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KubernetesZkClusterCoordination implements KubernetesClusterCoordination {

	private static final Logger logger = LoggerFactory.getLogger(KubernetesZkClusterCoordination.class);
	
	private ZkClusterCoordination zkClusterCoordination;
	
	private String hostPort;
	
	private static Map kubernetesConfig = KubernetesConfigLoad.getKubernetesConfig();
	
	
	KubernetesZkClusterCoordination(){
		zkClusterCoordination = new CuratorZkClusterCoordination();
		
		try {
			hostPort = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			logger.error("get host name error:",e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isKubeMasterExisted() {
		try{
			return zkClusterCoordination.isNodeExisted(KubernetesCluster.MASTER_SUBTREE, false);
		}catch(Exception e){
			logger.error("",e);
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public boolean tryToLeaderMaster() {
		try {
			if(!zkClusterCoordination.isNodeExisted(KubernetesCluster.MASTER_SUBTREE, false)){
				zkClusterCoordination.mkdirs(PathUtils.getParentPath(KubernetesCluster.MASTER_SUBTREE), CreateMode.PERSISTENT);
			}
			zkClusterCoordination.createNode(KubernetesCluster.MASTER_SUBTREE, hostPort.getBytes(), CreateMode.EPHEMERAL);
			return true;
		} catch (NodeExistsException e) {
			boolean is = isKubeMasterExisted();
			if(is != true){
				logger.error("",e);
				throw new RuntimeException(e);
			}
			
		}catch(Exception e){
			logger.error("",e);
			throw new RuntimeException(e);
		}
		return false;
	}

	@Override
	public boolean isCurrentMasterKube() {
		try {
			
			String masterHostPort =  new String(zkClusterCoordination.getData(KubernetesCluster.MASTER_SUBTREE, false));
			
			if(masterHostPort.equals(hostPort)){
				return true;
			}else{
				return false;
			}
		}catch(NoNodeException e){
			return false;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> getAssignments() {
		List<String> datas;
		try {
			if(zkClusterCoordination.isNodeExisted(KubernetesCluster.ASSIGNMENTS_SUBTREE, false)){
				datas = zkClusterCoordination.getChildren(KubernetesCluster.ASSIGNMENTS_SUBTREE,false);
			}else{
				datas = new ArrayList<String>();
			}
			
			return datas;
		} catch (NoNodeException e) {
			return new ArrayList<String>();
		} catch(Exception e){
			throw new RuntimeException(e); 
		}
		
	}

	@Override
	public boolean unregisterCurrentKubeFromSlave() {
		try{
			try{
				zkClusterCoordination.deleteNode(KubernetesCluster.KUBE_SLAVE_SUBTREE + KubernetesCluster.ZK_SEPERATOR + hostPort);
			}catch(NoNodeException e){
				return true;
			}
			
			try{
				zkClusterCoordination.deleteNode(KubernetesCluster.KUBE_SLAVE_DETAIL_SUBTREE + KubernetesCluster.ZK_SEPERATOR + hostPort);
			}catch(NoNodeException e){
				return true;
			}

			return true;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public boolean registerCurrentKubeToSlave() {
		try{
			if(!zkClusterCoordination.isNodeExisted(KubernetesCluster.KUBE_SLAVE_SUBTREE, false)){
				zkClusterCoordination.mkdirs(PathUtils.getParentPath(KubernetesCluster.KUBE_SLAVE_SUBTREE), CreateMode.PERSISTENT);
			}
			zkClusterCoordination.createNode(
					KubernetesCluster.KUBE_SLAVE_SUBTREE + 
					KubernetesCluster.ZK_SEPERATOR + 
					hostPort, null, CreateMode.EPHEMERAL);
			
			
			
			byte[] slaveDetails = zkClusterCoordination.getData(KubernetesCluster.KUBE_SLAVE_DETAIL_SUBTREE + 
					KubernetesCluster.ZK_SEPERATOR + 
					hostPort , false);
			Map map = null;
			if(slaveDetails == null){
				map = (Map)SerializeUtils.javaDeserialize(slaveDetails);
			}else{
				map = new HashMap<Object,Object>();
			}
			List<String> Assdiff = getAssignmentDiffFromLocalAndCluster();
			map.put(FollowerRunnable.KUBE_DIFFER_COUNT_ZK, Assdiff.size());
			setData(KubernetesCluster.KUBE_SLAVE_DETAIL_SUBTREE + 
					KubernetesCluster.ZK_SEPERATOR + 
					hostPort, SerializeUtils.javaSerialize(map));
			return true;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public List<String> getAssignmentDiffFromLocalAndCluster(){
		String localDistDir = KubernetesConfig.getMasterAssignmentLocalPath();
		List<String> localDistAssignments = PathUtils.readSubFileNames(localDistDir);
		List<String> clusterDistAssignments = getAssignments();
		
		clusterDistAssignments.removeAll(localDistAssignments);
		
		return clusterDistAssignments;
	}
	
	@Override
	public void setData(String path, byte[] data) {
		if(data.length > KubernetesUtils.SIZE_1_K * 800){
			throw new RuntimeException("Writing 800K+ data into zk is not allowed!,current data size is " + data.length);
		}
		
		try{
			if(zkClusterCoordination.isNodeExisted(path, false)){
				zkClusterCoordination.setData(path, data);
			}else{
				zkClusterCoordination.mkdirs(PathUtils.getParentPath(path), CreateMode.PERSISTENT);
				zkClusterCoordination.createNode(path, data, CreateMode.PERSISTENT);
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> getAllKubeSlaveHostPort() {
		List<String> dirList = null;
		try{
			if(zkClusterCoordination.isNodeExisted(KubernetesCluster.KUBE_SLAVE_DETAIL_SUBTREE, false)){
				dirList = zkClusterCoordination.getChildren(KubernetesCluster.KUBE_SLAVE_DETAIL_SUBTREE, false);
			}else{
				dirList = new ArrayList<String>();
			}
			
			return dirList;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public Map getKubeSlaveDetail(String hostPort) {
		
		try{
			byte[] slaveDetails = zkClusterCoordination.getData(KubernetesCluster.KUBE_SLAVE_DETAIL_SUBTREE + 
					KubernetesCluster.ZK_SEPERATOR + 
					hostPort , false);
			
			Map map = null;
			if(slaveDetails == null){
				map = (Map)SerializeUtils.javaDeserialize(slaveDetails);
			}else{
				map = new HashMap<Object,Object>();
			}
			
			return map;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public Assignment getAssignment(String topologyId) {
		String assignmentPath = KubernetesCluster.getAssignmentPath(topologyId);
		try {
			byte[] data = zkClusterCoordination.getData(assignmentPath, false);
			
			if(data == null){
				throw new BusinessException("zk data:" + assignmentPath + "is not exist!");
			}
			
			Assignment assignment = (Assignment)SerializeUtils.javaDeserialize(data);
			return assignment;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void setAssignment(Assignment assignment) {
		String assignmentPath = KubernetesCluster.getAssignmentPath(assignment.getTopologyId());
		byte[] data = SerializeUtils.javaSerialize(assignment);
		try {
			setData(assignmentPath, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void setKubeletHeartbeat(KubeletHeartbeat heartbeat) {
		String kubeletHeartbeatPath = KubernetesCluster.getKubeletHeartbeat(heartbeat.getKubeletId());
		byte[] data = SerializeUtils.javaSerialize(heartbeat);
		try {
			setData(kubeletHeartbeatPath, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public List<KubeletHeartbeat> getValidKubeletHeartbeats() {
		String kubeletHeartbeatRoot = KubernetesCluster.KUBELET_HEARTBEAT_SUBTREE;
		
		List<String> list = null;
		try {
			if(zkClusterCoordination.isNodeExisted(KubernetesCluster.KUBELET_HEARTBEAT_SUBTREE, false)){
				list = zkClusterCoordination.getChildren(kubeletHeartbeatRoot, false);
			}
			
		} catch(NoNodeException e){
			logger.debug("no node",e);
		}catch (Exception e) {
			logger.error("",e);
		}
		
		if(list == null || list.isEmpty()){
			return new ArrayList<KubeletHeartbeat>();
		}
		
		List<KubeletHeartbeat> kubeletHeartbeats = new ArrayList<KubeletHeartbeat>();
		for(String kubeletId:list){
			String kubeletHeartbeatPath = kubeletHeartbeatRoot + KubernetesCluster.ZK_SEPERATOR + kubeletId;
			try {
				byte[] data = zkClusterCoordination.getData(kubeletHeartbeatPath, false);
				if(data == null){
					continue;
				}
				
				KubeletHeartbeat hb = (KubeletHeartbeat)SerializeUtils.javaDeserialize(data);
				
				if(DateUtils.getCurrentTimeSecs() - hb.getTimeSecs() > 
					(int)kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_KUBELET_POD_HEARTBEAT_TIMEOUT_SECS)){
					continue;
				}
				kubeletHeartbeats.add(hb);
			} catch (Exception e) {
				throw new BusinessException("",e);
			}
		}
		
		return kubeletHeartbeats;
	}

	@Override
	public void deleteAssignment(String topologyId) {
		String assignmentPath = KubernetesCluster.getAssignmentPath(topologyId);
		try {
			if(zkClusterCoordination.isNodeExisted(assignmentPath, false)){
				zkClusterCoordination.deleteNode(assignmentPath);
			}
			
		} catch(NoNodeException e){
			logger.warn("",e);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public Assignment getAssignmentByName(String topologyName) {
		List<String> topolgyIds = getAssignments();
		Assignment currentTopology = null;
		for(String topologyId:topolgyIds){
			Assignment assignment = getAssignment(topologyId);
			if(assignment != null && assignment.getTopologyName().equals(topologyName)){
				currentTopology = assignment;
			}
		}
		
		return currentTopology;
	}

}
