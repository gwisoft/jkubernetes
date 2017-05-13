package org.gwisoft.jkubernetes.daemon.kube;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.exception.BusinessException;
import org.gwisoft.jkubernetes.schedule.Assignment;
import org.gwisoft.jkubernetes.schedule.DefaultTopologyScheduler;
import org.gwisoft.jkubernetes.schedule.IToplogyScheduler;
import org.gwisoft.jkubernetes.schedule.TopologyAssignContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyAssignRunnable implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(TopologyAssignRunnable.class);

	protected static volatile LinkedBlockingQueue<TopologyAssignEvent> queue = new LinkedBlockingQueue<TopologyAssignEvent>();
	
	private static KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
	
	private static AtomicBoolean shutdown = new AtomicBoolean(false);
	
	public static void push(TopologyAssignEvent event){
		queue.offer(event);
	}
	@Override
	public void run() {
		logger.info("TopologyAssignRunnable thread has bean started");
		
		while(!shutdown.get()){
			TopologyAssignEvent event;
			try {
				event = queue.take();
				logger.info("*************read topology assign event:" + event.toString() + "****************");
			} catch (InterruptedException e) {
				continue;
			}
			
			if(event == null){
				continue;
			}
			
			doTopologyAssign(event);
		}

	}

	public void doTopologyAssign(TopologyAssignEvent event){
		if(event.getAssignType().equals(TopologyAssignEvent.AssignType.anewAssign)
				|| event.getAssignType().equals(TopologyAssignEvent.AssignType.assign)){
			assign(event);
		}else if(event.getAssignType().equals(TopologyAssignEvent.AssignType.delete)){
			deleteAssign(event);
		}
	}
	
	public void deleteAssign(TopologyAssignEvent event){
		try{
			Assignment assignment = coordination.getAssignmentByName(event.getTopologyName());
			if(assignment == null){
				throw new BusinessException("topolgy name:" + event.getTopologyName() + " is not exist!");
			}
			coordination.deleteAssignment(assignment.getTopologyId());
			deleteMasterAssignmentLocalInfo(assignment);
			event.done();
		}catch(Throwable e){
			logger.error("",e);
			event.fail(e.getMessage());
		}
	}
	
	public void assign(TopologyAssignEvent event){
		try{
			Set<ResourcePodSlot> assignments = null;
			TopologyAssignContext context = prepareTopologyAssign(event);
			IToplogyScheduler scheduler = new DefaultTopologyScheduler();
			assignments = scheduler.assignTasks(context);
			Assignment assignment = new Assignment(event.getTopologyId(),event.getTopologyName(),assignments,event.getYamlMap(),event.getTimestamp());
			coordination.setAssignment(assignment);
			saveMasterAssignmentLocalInfo(assignment);
			event.done();

		}catch(Throwable e){
			logger.error("",e);
			event.fail(e.getMessage());
		}
	}
	
	public void saveMasterAssignmentLocalInfo(Assignment assignment){
		String localDistDir = KubernetesConfig.getMasterAssignmentLocalPath();
		try {
			FileUtils.forceMkdir(new File(localDistDir + File.separator + assignment.getTopologyId()));
		} catch (IOException e) {
			throw new BusinessException(e);
		}
	}
	public void deleteMasterAssignmentLocalInfo(Assignment assignment){
		String localDistDir = KubernetesConfig.getMasterAssignmentLocalPath();
		try {
			FileUtils.forceDelete(new File(localDistDir + File.separator + assignment.getTopologyId()));
		} catch (IOException e) {
			throw new BusinessException(e);
		}
	}
	
	public TopologyAssignContext prepareTopologyAssign(TopologyAssignEvent event){
		TopologyAssignContext context = new TopologyAssignContext();
		context.setTopologyId(event.getTopologyId());
		context.setYamlMap(event.getYamlMap());
		context.setEvent(event);
		return context;
	}
}
