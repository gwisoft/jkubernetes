package org.gwisoft.jkubernetes.apiserver;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.thrift.TException;
import org.gwisoft.jkubernetes.apiserver.thrift.ApiServer.Iface;
import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.daemon.kube.KubeData;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignEvent;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignEvent.AssignType;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignRunnable;
import org.gwisoft.jkubernetes.exception.BusinessException;
import org.gwisoft.jkubernetes.exception.FailedAssignTopologyException;
import org.gwisoft.jkubernetes.schedule.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ServiceHandler implements Iface {

	private static final Logger logger = LoggerFactory.getLogger(ServiceHandler.class);
	
	public final static int MIN_THREAD_NUM = 1;
	public final static int MAX_THREAD_NUM = 10;
	
	private KubeData data;
	
	public ServiceHandler(KubeData data){
		this.data = data;
	}
	
	@Override
	public void submitTopologyStr(String yaml) throws TException {
		byte[] bytes = null;
		try {
			bytes = yaml.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		System.out.println(DigestUtils.md5Hex(bytes));
		ApiServerYamlAnalyzer analyzer = new ApiServerYamlAnalyzer(bytes);

		TopologyAssignEvent assignEvent = new TopologyAssignEvent();
		String topologyId = analyzer.getTopologyId();
        assignEvent.setTopologyId(topologyId);
        assignEvent.setTopologyName(analyzer.getApiServerYaml().getMetadata().getName());
        assignEvent.setApiServerYaml(analyzer.getApiServerYaml());
        assignEvent.setAssignType(AssignType.assign);

        TopologyAssignRunnable.push(assignEvent);

        boolean isSuccess = assignEvent.waitFinish();
        if (isSuccess == true) {
            logger.info("Finish submit for " + assignEvent.getTopologyName());
        } else {
            throw new FailedAssignTopologyException(assignEvent.getErrorMsg());
        }
		
	}

	@Override
	public void deleteTopology(String yaml) throws TException {

		byte[] bytes = null;
		try {
			bytes = yaml.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		System.out.println(DigestUtils.md5Hex(bytes));
		ApiServerYamlAnalyzer analyzer = new ApiServerYamlAnalyzer(bytes);
		
		TopologyAssignEvent assignEvent = new TopologyAssignEvent();
        assignEvent.setTopologyName(analyzer.getTopologyName());
        assignEvent.setAssignType(AssignType.delete);

        TopologyAssignRunnable.push(assignEvent);

        boolean isSuccess = assignEvent.waitFinish();
        if (isSuccess == true) {
            logger.info("Finish submit for " + assignEvent.getTopologyName());
        } else {
            throw new FailedAssignTopologyException(assignEvent.getErrorMsg());
        }
		
	}

	@Override
	public void rollingUpdateTopology(String oldTopologyName, String yaml) throws TException {
		byte[] bytes = null;
		try {
			bytes = yaml.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		System.out.println(DigestUtils.md5Hex(bytes));
		ApiServerYamlAnalyzer newAnalyzer = new ApiServerYamlAnalyzer(bytes);

		KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
		Assignment assignment = coordination.getAssignmentByName(oldTopologyName);
		if(assignment == null){
			throw new BusinessException("old topology name " + oldTopologyName + "is not exist!");
		}

		if(assignment.getTopologyName().equalsIgnoreCase(newAnalyzer.getTopologyName())){
			throw new BusinessException("rolling update topology Name cannot be the same !");
		}
		
		Integer newRcNum = newAnalyzer.getApiServerYaml().getSpec().getReplicas();
		Integer oldRcNum = assignment.getApiServerYaml().getSpec().getReplicas();
		Integer newRcDeployedNum = 1;
		Integer oldRcUndeployNum = 1;
		for(int i = 1; i <= (newRcNum > oldRcNum?newRcNum:oldRcNum);i++){
			
			// undeploy old topology
			if(oldRcUndeployNum <= oldRcNum){
				assignment.getApiServerYaml().getSpec().setReplicas(oldRcNum - oldRcUndeployNum);
				TopologyAssignEvent assignEvent = new TopologyAssignEvent();
				String topologyId = assignment.getTopologyId();
		        assignEvent.setTopologyId(topologyId);
		        assignEvent.setTopologyName(assignment.getTopologyName());
		        assignEvent.setApiServerYaml(assignment.getApiServerYaml());
		        assignEvent.setAssignType(AssignType.anewAssign);

		        TopologyAssignRunnable.push(assignEvent);

		        boolean isSuccess = assignEvent.waitFinish();
		        if (isSuccess == true) {
		        	oldRcUndeployNum++;
		            logger.info("Finish submit for " + assignEvent.getTopologyName() + "rolling undeploy rc num=" + oldRcUndeployNum);
		        } else {
		            throw new FailedAssignTopologyException(assignEvent.getErrorMsg());
		        }
		        
		        //is success run pod
				while(true){
					Assignment oldAssignment = coordination.getAssignment(assignment.getTopologyId());
					
					if(oldAssignment != null
							&& oldAssignment.getState().equals(ResourcePodSlot.AssignmentState.AssignmentSuccess)){
						break;
					}

					try {
						Thread.sleep(1000);
						logger.info("rolling updateing...");
					} catch (InterruptedException e) {
						logger.warn("",e);
					}
				}
				
				if(oldRcUndeployNum == oldRcNum){
		        	deleteTopology(assignment.getTopologyName());
		        }
			}
			
			// deploy new topology
			if(newRcDeployedNum <= newRcNum){
				newAnalyzer.getApiServerYaml().getSpec().setReplicas(newRcDeployedNum);
				TopologyAssignEvent assignEvent = new TopologyAssignEvent();
				String topologyId = newAnalyzer.getTopologyId();
		        assignEvent.setTopologyId(topologyId);
		        assignEvent.setTopologyName(newAnalyzer.getTopologyName());
		        assignEvent.setApiServerYaml(newAnalyzer.getApiServerYaml());
		        
		        //if first deploy new topology
		        if(i != 1){
		        	assignEvent.setAssignType(AssignType.anewAssign);
		        }else{
		        	assignEvent.setAssignType(AssignType.assign);
		        }
		        

		        TopologyAssignRunnable.push(assignEvent);

		        boolean isSuccess = assignEvent.waitFinish();
		        if (isSuccess == true) {
		        	newRcDeployedNum++;
		            logger.info("Finish submit for " + assignEvent.getTopologyName() + "rolling deploy rc num=" + newRcDeployedNum);
		        } else {
		            throw new FailedAssignTopologyException(assignEvent.getErrorMsg());
		        }
		        
		        //is success run pod
				while(true){
					Assignment newAssignment = coordination.getAssignment(topologyId);
					
					if(newAssignment != null
							&& newAssignment.getState().equals(ResourcePodSlot.AssignmentState.AssignmentSuccess)){
						break;
					}

					try {
						Thread.sleep(1000);
						logger.info("rolling updateing...");
					} catch (InterruptedException e) {
						logger.warn("",e);
					}
				}
			}
			
			
		}
		logger.info("oldTopologyName=" + assignment.getTopologyName() + " and newTopologyName=" + newAnalyzer.getTopologyName() + "rolling update success!");
		
	}

	@Override
	public String getTopologyInfo(String topologyName) throws TException {
		KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
		Assignment assignment = coordination.getAssignmentByName(topologyName);
		if(assignment == null){
			throw new BusinessException("topology name " + topologyName + "is not exist!");
		}
		
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(assignment,Assignment.class);
		logger.info("query topology info success! (topology name=" + topologyName + ")");
		return json;
	}

	@Override
	public void replaceTopology(String yaml) throws TException {
		byte[] bytes = null;
		try {
			bytes = yaml.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		System.out.println(DigestUtils.md5Hex(bytes));
		ApiServerYamlAnalyzer analyzer = new ApiServerYamlAnalyzer(bytes);
		
		KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
		Assignment assignment = coordination.getAssignmentByName(analyzer.getTopologyName());
		if(assignment == null){
			throw new BusinessException("old topology name " + analyzer.getTopologyName() + "is not exist!");
		}

		TopologyAssignEvent assignEvent = new TopologyAssignEvent();
        assignEvent.setTopologyId(assignment.getTopologyId());
        assignEvent.setTopologyName(analyzer.getTopologyName());
        assignEvent.setApiServerYaml(analyzer.getApiServerYaml());
        assignEvent.setAssignType(AssignType.anewAssign);

        TopologyAssignRunnable.push(assignEvent);

        boolean isSuccess = assignEvent.waitFinish();
        if (isSuccess == true) {
            logger.info("Finish submit for " + assignEvent.getTopologyName());
        } else {
            throw new FailedAssignTopologyException(assignEvent.getErrorMsg());
        }
		
	}

	@Override
	public String getTopologyInfoAll() throws TException {
		KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
		List<String> topologyIds = coordination.getAssignments();
		List<Assignment> assignments = new ArrayList<>();
		for(String topologyId:topologyIds){
			Assignment assignment = coordination.getAssignment(topologyId);
			assignments.add(assignment);
		}

		
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(assignments);
		logger.info("query all topology info success!");
		return json;
		
	}

}
