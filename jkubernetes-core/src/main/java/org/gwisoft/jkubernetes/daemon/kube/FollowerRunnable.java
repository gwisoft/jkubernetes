package org.gwisoft.jkubernetes.daemon.kube;

import java.util.List;
import java.util.Map;

import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FollowerRunnable implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(FollowerRunnable.class);
	
	public static final String KUBE_DIFFER_COUNT_ZK = "kube.differ.count.zk";

	private static KubernetesClusterCoordination coordination;
	
	public FollowerRunnable(){
		coordination = KubernetesCluster.instanceCoordination();
	}
	
	/**
	 * 1、check kube master is exist,if isn't exist,try to be leader
	 * 2、check whether current progress ,if exist kube master
	 * 3、unregister from slave list,if current progress is kube master
	 * 4、register slave list ,if exist master and isn't current progress
	 */
	@Override
	public void run() {
		while(true){
			try{
				Thread.sleep(5000);
				boolean leaderSuccess = false;
				
				if(!coordination.isKubeMasterExisted()){
					leaderSuccess = this.tryToLeaderMaster();
					if(leaderSuccess){
						logger.info("current kube be leader to master!");
						continue;
					}else{
						logger.info("master has been restored,current master leader fail!");
					}
				}
				
				
				if(coordination.isCurrentMasterKube()){
					coordination.unregisterCurrentKubeFromSlave();
					KubeServer.kubeData.setIsLeader(true);
					continue;
				}else{
					if(KubeServer.kubeData.isLeader() == true){
						logger.info("existed other master kube,kill current master kube!");
						KubernetesUtils.haltProcess(1, "Lose ZK master node, halt process");
						return;
					}else{
						logger.debug("existed other master kube,current kube leader fail!");
						coordination.registerCurrentKubeToSlave();
					}
					
				}
					

			}catch(Throwable e){
				logger.error("Unknow exception",e);
			}
		}
		
		
	}

	private boolean tryToLeaderMaster(){
		boolean isCheckPass = checkKubePriority();
		if(isCheckPass){
			return coordination.tryToLeaderMaster();
		}else{
			logger.info("current kube isn‘t be leader!");
			return false;
		}
	}
	
	/**
	* @Title: checkKubePriority 
	* @Description: 检查其它kube结点是否优先级更高
	* @return
	 */
	private boolean checkKubePriority(){
		List<String> assDiffs = coordination.getAssignmentDiffFromLocalAndCluster();
		if(assDiffs == null || assDiffs.isEmpty()){
			return true;
		}
		
		List<String> hostPorts = coordination.getAllKubeSlaveHostPort();
		for(String hostPort:hostPorts){
			Map map = coordination.getKubeSlaveDetail(hostPort);
			Object diffSize = map.get(KUBE_DIFFER_COUNT_ZK);
			if(diffSize != null && Integer.parseInt(String.valueOf(diffSize)) < assDiffs.size()){
				logger.warn("current node can't be leader,due to {} has higher priority",hostPort);
				return false;
			}
		}
		return true;
	}
	
	
	
	
	

}
