package org.gwisoft.jkubernetes.docker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.gwisoft.jkubernetes.apiserver.yaml.ContainerPort;
import org.gwisoft.jkubernetes.apiserver.yaml.ContainerVolumeMount;
import org.gwisoft.jkubernetes.apiserver.yaml.PodContainer;
import org.gwisoft.jkubernetes.apiserver.yaml.PodVolume;
import org.gwisoft.jkubernetes.apiserver.yaml.TemplateSpec;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.daemon.pod.PodHeartbeat;
import org.gwisoft.jkubernetes.daemon.pod.PodLocalState;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.exception.BusinessException;
import org.gwisoft.jkubernetes.utils.DateUtils;
import org.gwisoft.jkubernetes.utils.JsonUtils;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

public class KubernetesDockerJava implements KubernetesDocker {

	private static final Logger logger = LoggerFactory.getLogger(KubernetesDockerJava.class);
	
	protected DockerClient dockerClient;
	
	protected DockerCmdExecFactory dockerCmdExecFactory = DockerClientBuilder.getDefaultDockerCmdExecFactory();
	
	public KubernetesDockerJava(){
		initDocker();
	}
	
	private void initDocker(){
		logger.info("Connecting to Docker server");
        dockerClient = DockerClientBuilder.getInstance(config(null))
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
	}
	
	protected DefaultDockerClientConfig config(String password) {
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withRegistryUrl("https://index.docker.io/v1/");
        if (password != null) {
            builder = builder.withRegistryPassword(password);
        }

        return builder.build();
    }
	
	@Override
	public boolean startContainer(ResourcePodSlotDocker slot) {
		logger.info("start_running_container...");
		TemplateSpec templateSpec = slot.getTemplateSpec();
		List<PodVolume> podVolumes = templateSpec.getVolumes();
		Volume[] volumes;
		Map<String, Volume> volumeMap;
		
		if(podVolumes != null){
			volumes = new Volume[podVolumes.size()];
			volumeMap= new HashMap<String, Volume>();
			int i = 0;
			for(PodVolume podVolume:podVolumes){
				Volume volume = new Volume(podVolume.getHostPath());
				volumes[i] = volume;
				volumeMap.put(podVolume.getName(), volume);
				i++;
			}
		}else{
			volumes = new Volume[0];
			volumeMap= new HashMap<String, Volume>();
		}
		
		
		List<PodContainer> subContainers = templateSpec.getContainers();
		if(subContainers == null){
			return true;
		}
		
		for(PodContainer subContainer:subContainers){
			
			//1、image is exist local
			try {
	            dockerClient.inspectImageCmd(subContainer.getImage()).exec();
	        } catch (NotFoundException e) {
	            logger.info("Pulling image " + subContainer.getImage());
	            // need to block until image is pulled completely
	            dockerClient.pullImageCmd(subContainer.getImage()).withTag("latest").exec(new PullImageResultCallback()).awaitSuccess();
	        }
			
			//2、cleanup old container
			String containerName = subContainer.getName() + "_" +slot.getPodId();
			try{
				List<Container> dockerContainers = dockerClient.listContainersCmd().withShowAll(true).exec();
				logger.info("find_container_number " + dockerContainers.size() + " currentContainerName=" + containerName);
				for(Container dockerContainer:dockerContainers){
					logger.info("container_name=" + dockerContainer.getNames()[0]);
					if(dockerContainer.getNames()[0].contains(containerName)){
						try{
							dockerClient.stopContainerCmd(dockerContainer.getId()).exec();
							logger.info("stop container id:" + dockerContainer.getId() + " container name:" + containerName);
						}catch(Exception e){
							logger.warn("stop container id:" + dockerContainer.getId() + " container name:" + containerName + " fail!",e);
						}
						
						try{
							dockerClient.removeContainerCmd(dockerContainer.getId()).exec();
							logger.info("remove container id:" + dockerContainer.getId() + " container name:" + containerName);
						}catch(Exception e){
							logger.warn("remove container id:" + dockerContainer.getId() + " container name:" + containerName + " fail",e);
						}
						
						break;
					}
				}
			}catch(Exception e){
				logger.warn("",e);
			}
			
			
			CreateContainerCmd  cmd = dockerClient.createContainerCmd(subContainer.getImage()).withCmd("/bin/bash").withTty(true);
			//3、bind port
			if(subContainer.getContainerPorts() != null && !subContainer.getContainerPorts().isEmpty()){
				ExposedPort[] exposedPorts = new  ExposedPort[subContainer.getContainerPorts().size()];
				Ports portBindings = new Ports();
				int i = 0;
				for(ContainerPort port:subContainer.getContainerPorts()){
					ExposedPort exposedPort = new ExposedPort(port.getContainerPort());
					portBindings.bind(exposedPort, Binding.bindPort(Integer.valueOf(port.getHostPort()  + "" +  slot.getPodId())));
					exposedPorts[i] = exposedPort;
				}
				
				cmd.withExposedPorts(exposedPorts).withPortBindings(portBindings);
			}
			
			
			//4、bind volume
			Bind[] binds = new Bind[subContainer.getVolumeMounts() == null?0:subContainer.getVolumeMounts().size()];
			int i = 0;
			if(subContainer.getVolumeMounts() != null){
				for(ContainerVolumeMount volumeMount:subContainer.getVolumeMounts()){
					Volume volume = volumeMap.get(volumeMount.getName());
					if(volume == null){
						throw new BusinessException("Undefined volume " + volumeMount.getName());
					}
					binds[i] = new Bind(volumeMount.getMountPath(), volume);
					i++;
				}
				cmd.withVolumes(volumes).withBinds(binds);
			}
			
			
			
			//5、create new container
			CreateContainerResponse dockerContainer = cmd.withName(containerName).exec();
			
			
			//6、start container
			dockerClient.startContainerCmd(dockerContainer.getId()).exec();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//7、query state
//			InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(dockerContainer.getId()).exec();
//			ContainerState state = inspectContainerResponse.getState();
//			String stateJson = JsonUtils.toJson(state);
//			logger.debug("start container running state = " + stateJson);
			
			//7、save container id
			String pidsDir = KubernetesConfig.getLocalPodPidsDir(slot.getPodId());
			KubernetesUtils.savePid(pidsDir, dockerContainer.getId());
			
			//8、save initial heartbeat
			int timeSecs = DateUtils.getCurrentTimeSecs();
			PodHeartbeat podHeartbeat = new PodHeartbeat(
					timeSecs, slot.getTopologyId(), 
					slot.getPodId(),slot.getKubeletId(),ResourcePodSlot.PodType.docker);
			PodLocalState.setPodHeartbeat(podHeartbeat);
			
			logger.info("run success container name:" + containerName + 
					" image:" + subContainer.getImage() + " container id:" + dockerContainer.getId());
		}
		
		return true;
	}

	@Override
	public void stopContainer(ResourcePodSlotDocker slot) {
		logger.info("start stop container...");
		try{
			TemplateSpec templateSpec = slot.getTemplateSpec();
			List<PodContainer> podContainers = templateSpec.getContainers();
			if(podContainers == null){
				return;
			}
			
			for(PodContainer podContainer:podContainers){
				List<Container> dockerContainers = dockerClient.listContainersCmd().withShowAll(true).exec();
				for(Container dockerContainer:dockerContainers){
					if(dockerContainer.getNames()[0].equals(podContainer.getName())){
						dockerClient.stopContainerCmd(dockerContainer.getId()).exec();
						logger.info("stop container id:" + dockerContainer.getId());
						break;
					}
				}
			}
			
		}catch(Exception e){
			logger.warn("stop topology id(" + slot.getTopologyId() +") Container",e);
		}
		
	}

	@Override
	public boolean isRunningContainer(String containerId) {
        try {
        	logger.debug("start_monitor_containerId(" + containerId  + ")_is_running!");
        	CountDownLatch countDownLatch = new CountDownLatch(5);
    		
    		StatsCallbackTest statsCallback = dockerClient.statsCmd(containerId).exec(
                    new StatsCallbackTest(countDownLatch));
    		
			countDownLatch.await(3, TimeUnit.SECONDS);
			
			Boolean gotStats = statsCallback.gotStats();
			logger.debug("monitor return " + gotStats);
			return gotStats;
		} catch (InterruptedException e) {
			logger.error("",e);
			throw new BusinessException(e);
		}
        
		
	}
	
	private class StatsCallbackTest extends ResultCallbackTemplate<StatsCallbackTest, Statistics> {
        private final CountDownLatch countDownLatch;

        private Boolean gotStats = false;

        public StatsCallbackTest(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onNext(Statistics stats) {
            logger.debug("Received stats #{}: {}", countDownLatch.getCount(), stats);
            if (stats != null) {
                gotStats = true;
            }
            countDownLatch.countDown();
        }

        public Boolean gotStats() {
            return gotStats;
        }
    }

	@Override
	public void stopContainer(String containerId) {
		try{
			dockerClient.stopContainerCmd(containerId);
			logger.info("stop container id:" + containerId);
		}catch(Exception e){
			logger.warn("stop container id:" + containerId,e);
		}
		
		
	}

}
