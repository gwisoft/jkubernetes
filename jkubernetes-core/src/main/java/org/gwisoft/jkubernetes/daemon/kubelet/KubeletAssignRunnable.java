package org.gwisoft.jkubernetes.daemon.kubelet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.config.KubernetesConfigConstant;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.daemon.pod.Pod;
import org.gwisoft.jkubernetes.daemon.pod.PodHeartbeat;
import org.gwisoft.jkubernetes.daemon.pod.PodLocalState;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.daemon.pod.StatePodHeartbeat;
import org.gwisoft.jkubernetes.docker.KubernetesDocker;
import org.gwisoft.jkubernetes.docker.KubernetesDockerFactory;
import org.gwisoft.jkubernetes.docker.ResourcePodSlotCommand;
import org.gwisoft.jkubernetes.docker.ResourcePodSlotDocker;
import org.gwisoft.jkubernetes.exception.BusinessException;
import org.gwisoft.jkubernetes.schedule.Assignment;
import org.gwisoft.jkubernetes.utils.DateUtils;
import org.gwisoft.jkubernetes.utils.ExecCommandUtils;
import org.gwisoft.jkubernetes.utils.JsonUtils;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.gwisoft.jkubernetes.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class KubeletAssignRunnable implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(KubeletAssignRunnable.class);

	private static KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
	
	private static AtomicBoolean shutdown = new AtomicBoolean(false);
	
	private static Map kubernetesConfig = KubernetesConfigLoad.getKubernetesConfig();
	
	private String kubeletId;
	
	private Map<Integer,Integer> podIdToStartTime;
	
	public KubeletAssignRunnable(String kubeletId){
		this.kubeletId = kubeletId;
		this.podIdToStartTime = new HashMap<>();
	}
	@Override
	public void run() {
		while(!shutdown.get()){
			try{
			
				process();
				
				Integer interval = KubernetesConfig.getPodHeartbeatIntervalMs();
				Thread.sleep(interval);
			}catch(Throwable e){
				logger.error("",e);
			}
		}
	}

	private void process(){
		logger.debug("start KubeletAssignRunnable...");
		
		//get all assignments
		List<String> assignments = coordination.getAssignments();
		Map<String, Assignment> allAssignments = new HashMap<String, Assignment>();
		for (String topologyId : assignments) {
			Assignment assignment = coordination.getAssignment(topologyId);
			allAssignments.put(assignment.getTopologyId(), assignment);
		}
		logger.debug("************read all assignment :" + JsonUtils.toJson(allAssignments) + "***********") ;
		
		//get current kubelet assignments by zk
		Map<Integer, ResourcePodSlot> localZkAssignments = getLocalzkAssign(allAssignments);
		logger.debug("************read local zk assignment :" + JsonUtils.toJson(localZkAssignments) + "************");
		
		//get assigned local assignments and save localzkAssignment
		Map<Integer, ResourcePodSlot> localAssignments = null;
		try{
			localAssignments = KubeletLocalState.getLocalAssignments(kubeletId);
			logger.debug("************read local assignment : " + JsonUtils.toJson(localAssignments) + "************");
			KubeletLocalState.setLocalAssignments(kubeletId, localZkAssignments);
		}catch(Exception e){
			throw new BusinessException(e);
		}
		
		//get need update assigned data(server update)
		Set<String> localUpdateAssignments = getUpdateAssignments(allAssignments,localZkAssignments,localAssignments);
		logger.debug("************need update assignment topologyId:" + JsonUtils.toJson(localUpdateAssignments) + "************");
		
		//get local pod state heartbeat
		Map<Integer, StatePodHeartbeat> podHbMappodHbMap = KubeletLocalState.getStatePodHeartbeats();
		
		//check pod start info and update podIdToStartTime
		updateStartingPodId(podHbMappodHbMap);
		
		//kill Invalid pods and remove killed pod from localPodStats
		Set<Integer> keepPodIds = killUselessPods(podHbMappodHbMap,localZkAssignments);
		
		//start new pod
		startNewPod(keepPodIds,localUpdateAssignments,localZkAssignments);
	
	}
	
	private void startNewPod(Set<Integer> keepPodIds,Set<String> localUpdateAssignments,
			Map<Integer, ResourcePodSlot> localZkAssignments){
		
				
		Map<Integer, ResourcePodSlot> newWorkers = new HashMap<Integer, ResourcePodSlot>();
		for(Entry<Integer, ResourcePodSlot> entry : localZkAssignments.entrySet()){
			if(!keepPodIds.contains(entry.getKey()) && !podIdToStartTime.containsKey(entry.getKey())){
				newWorkers.put(entry.getKey(), entry.getValue());
			}
		}
		
		for(Entry<Integer, ResourcePodSlot> entry : newWorkers.entrySet()){
			Integer port = entry.getKey();
			ResourcePodSlot slot = entry.getValue();
			
			try {
				logger.info("start pod:" + slot.toString());
				launchPod(slot);
				
			} catch (IOException e) {
				logger.error("",e);
				throw new BusinessException(e);
			}
		}
		
		markAllNewStarting(newWorkers);
		
		
	}
	
	
	public void updateStartingPodId(Map<Integer, StatePodHeartbeat> statePodHbMap){
		Set<Integer> podIds = new HashSet<>();
		for(Entry<Integer,Integer> entry:podIdToStartTime.entrySet()){
			Integer podId = entry.getKey();
			Integer startTime = entry.getValue();

			if((DateUtils.getCurrentTimeSecs() - startTime) < 
				(int)kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_POD_START_TIMEOUT_SECS)){
				logger.info(podId + " still hasn't started");
			}else{
				logger.error("***************Failed to start Pod " + podId + "********************");
				podIds.add(podId);
			}
		}
		
		for(Integer podId:podIds){
			podIdToStartTime.remove(podId);
		}
		
	}
	
	public void markAllNewStarting(Map<Integer, ResourcePodSlot> newWorkers) {
        int startTime = DateUtils.getCurrentTimeSecs();

        for (Entry<Integer, ResourcePodSlot> entry : newWorkers.entrySet()) {
        	podIdToStartTime.put(entry.getKey(), startTime);
        }
    }
	
	public void launchPod(ResourcePodSlot slot) throws IOException {

		if (slot instanceof ResourcePodSlotDocker) {
			ResourcePodSlotDocker slotDocker = (ResourcePodSlotDocker) slot;
			launchDockerPod(slotDocker);
		}else if(slot instanceof ResourcePodSlotCommand){
			ResourcePodSlotCommand slotCommand = (ResourcePodSlotCommand)slot;
			launchCommandPod(slotCommand);
		}else{
			launchJavaThreadPod(slot);
		}
	}
	
	public void launchDockerPod(ResourcePodSlotDocker slot) throws IOException{
		KubernetesDocker docker = KubernetesDockerFactory.getInstance();
		docker.startContainer(slot);
	}
	
	public void launchCommandPod(ResourcePodSlotCommand slot) throws IOException{
		ExecCommandUtils.ExecCommandASynCallBack callBack = new ExecCommandUtils.ExecCommandASynCallBack(){
			@Override
			public void callBackPid(Integer pid) {
				if(pid != null){
					String pidsDir = KubernetesConfig.getLocalPodPidsDir(slot.getPodId());
					KubernetesUtils.savePid(pidsDir, pid.toString());
					
					//save initial heartbeat
					int timeSecs = DateUtils.getCurrentTimeSecs();
					PodHeartbeat podHeartbeat = new PodHeartbeat(
							timeSecs, slot.getTopologyId(), 
							slot.getPodId(),slot.getKubeletId(),ResourcePodSlot.PodType.command);
					PodLocalState.setPodHeartbeat(podHeartbeat);
				}
			}
		};
		
		ExecCommandUtils.launchProcess(slot.getRunCommand(), new HashMap<String, String>(), callBack);
	}
	
	public void launchJavaThreadPod(ResourcePodSlot slot) throws IOException{
		Map totalConf = KubernetesConfig.getKubernetesconfig();

		Map<String, String> environment = new HashMap<String, String>();
		
		setPodEnvironment(slot,environment);
		
		String launcherCmd = getLauncherParameter(slot);
		
		String podCmd = getPodParameter(slot);
		String cmd = launcherCmd + " " + podCmd;
		
		logger.info("Launching pod with command: " + cmd);
		logger.info("Environment:" + environment.toString());
		
		ExecCommandUtils.launchProcess(cmd, environment, true);
	}
	
	public String getLauncherParameter(ResourcePodSlot slot) throws IOException {
    	
    	return "";
    }
	
	public void setPodEnvironment(ResourcePodSlot slot,Map<String, String> environment){
		String kubernetesHome = System.getProperty("kubernetes.home");
		if (StringUtils.isBlank(kubernetesHome)) {
			kubernetesHome = "./";
		} 
		environment.put("kubernetes.home", kubernetesHome);
		
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(slot);
		environment.put(Pod.RESOURCE_POD_SLOT, json);
	}
	
	public String getPodMemParameter(ResourcePodSlot slot){
		return "";
	}
	
	public String getPodParameter(ResourcePodSlot slot) throws IOException {
        
    	StringBuilder commandSB = new StringBuilder();

        commandSB.append("java -server ");
        commandSB.append(getPodMemParameter(slot));

        // get child process parameter
        String classpath = getClassPath(System.getProperty("kubernetes.home"));     

        commandSB.append(" -cp ");

        commandSB.append(classpath);

        commandSB.append(" org.gwisoft.jkubernetes.daemon.pod.Pod ");
        
        return commandSB.toString();

    }
	
	private String getClassPath(String kubernetesHome) {

        String classpath = System.getProperty("java.class.path");

        String[] classPaths = ExecCommandUtils.isWindows()?classpath.split(";"):classpath.split(":");

        Set<String> classSet = new HashSet<>();

        for (String classJar : classPaths) {
            if (StringUtils.isBlank(classJar)) {
                continue;
            }
            classSet.add(classJar);
        }

        if (kubernetesHome != null) {
            List<String> kubernetesHomeFiles = PathUtils.readSubFileNames(kubernetesHome);

            for (String file : kubernetesHomeFiles) {
                if (file.endsWith(".jar")) {
                    classSet.add(kubernetesHome + File.separator + file);
                }
            }

            List<String> stormLibFiles = PathUtils.readSubFileNames(kubernetesHome + File.separator + "lib");
            for (String file : stormLibFiles) {
                if (file.endsWith(".jar")) {
                    classSet.add(kubernetesHome + File.separator + "lib" + File.separator + file);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String jar : classSet) {
            sb.append(jar).append(ExecCommandUtils.isWindows()?";":":");
        }

        return sb.toString();
    }
	
	private Set<Integer> killUselessPods(Map<Integer, StatePodHeartbeat> podHbMappodHbMap,
			Map<Integer, ResourcePodSlot> localZkAssignments){
		Iterator<Map.Entry<Integer, StatePodHeartbeat>> hbIterator = podHbMappodHbMap.entrySet().iterator();
		Set<Integer> keepPodIds = new HashSet<>();
		Set<StatePodHeartbeat> killPodIds = new HashSet<>();
		while(hbIterator.hasNext()){
			Map.Entry<Integer, StatePodHeartbeat> hbEntry = hbIterator.next();
			StatePodHeartbeat shb = hbEntry.getValue();
			ResourcePodSlot slot = localZkAssignments.get(hbEntry.getKey());
			if(shb.getPodState().equals(StatePodHeartbeat.PodState.valid)
					&& slot != null
					&& shb.getPodHb().getTopologyId().equals(slot.getTopologyId())){
				keepPodIds.add(hbEntry.getKey());
			}else{
				if(!podIdToStartTime.containsKey(hbEntry.getKey())){
					killPodIds.add(hbEntry.getValue());
				}
			}

		}
		
		shutPod(killPodIds);
		return keepPodIds;
	}
	
	private void shutPod(Set<StatePodHeartbeat> killPodIds){
		for(StatePodHeartbeat podHB:killPodIds){
			List<String> pids = KubeletLocalState.getPodPids(podHB.getPodHb().getPodId());
			
			if(podHB.getPodHb().getPodType().equals(ResourcePodSlot.PodType.java_thread)){
				for (String pid : pids) {
	                KubernetesUtils.kill(pid);
	            }
			}else if(podHB.getPodHb().getPodType().equals(ResourcePodSlot.PodType.docker)){
				KubernetesDocker kubernetesDocker = KubernetesDockerFactory.getInstance();
				for (String pid : pids) {
					kubernetesDocker.stopContainer(pid);
				}
			}else if(podHB.getPodHb().getPodType().equals(ResourcePodSlot.PodType.command)){
				for (String pid : pids) {
	                KubernetesUtils.kill(pid);
	            }
			}
			
			KubeletLocalState.cleanupPidsByPodId(podHB.getPodHb().getPodId());
			KubeletLocalState.cleanupInvalidPodHb(podHB.getPodHb().getPodId());
		}
	}
	
	private Map<Integer, ResourcePodSlot> getLocalzkAssign(Map<String, Assignment> allAssignments){
		Map<Integer, ResourcePodSlot> portLA = new HashMap<Integer, ResourcePodSlot>();
		
		Iterator<Map.Entry<String, Assignment>> assignIterator = allAssignments.entrySet().iterator();
		while(assignIterator.hasNext()){
			Map.Entry<String, Assignment> entry = assignIterator.next();
			Assignment assignment = entry.getValue();
			
			Set<ResourcePodSlot> slots = assignment.getPods();
			for(ResourcePodSlot slot:slots){
				if(slot.getKubeletId().equals(kubeletId)){
					portLA.put(slot.getPodId(), slot);
				}
			}
			
		}
		
		return portLA;
	}
	
	public Set<String> getUpdateAssignments(Map<String, Assignment> assignments,
			Map<Integer, ResourcePodSlot> localzkAssignments,
			Map<Integer, ResourcePodSlot> localAssignments){
		Set<String> updateAssignments = new HashSet<String>();
		if(localzkAssignments !=null && localAssignments != null){
			Iterator<Map.Entry<Integer, ResourcePodSlot>> localAssignmentsIterator = localAssignments.entrySet().iterator();
			while(localAssignmentsIterator.hasNext()){
				Map.Entry<Integer, ResourcePodSlot> slotEntry = localAssignmentsIterator.next();
				Integer port = slotEntry.getKey();
				ResourcePodSlot slot = slotEntry.getValue();
				
				ResourcePodSlot zkSlot = localzkAssignments.get(port);
				
				if(zkSlot == null || slot == null){
					continue;
				}
				
				Assignment assignment = assignments.get(slot.getTopologyId());
				if(zkSlot.getTopologyId().equals(slot.getTopologyId()) 
						&& assignment != null
						&& assignment.isUpdateChange(slot.getTimestamp())){
					updateAssignments.add(slot.getTopologyId());
				}
			}
		}
		return updateAssignments;
	}
	public static AtomicBoolean getShutdown() {
		return shutdown;
	}
	public static void setShutdown(AtomicBoolean shutdown) {
		KubeletAssignRunnable.shutdown = shutdown;
	}
	
	
	
}
