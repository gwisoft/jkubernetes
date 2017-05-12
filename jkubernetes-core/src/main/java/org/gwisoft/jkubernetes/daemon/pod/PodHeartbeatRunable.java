package org.gwisoft.jkubernetes.daemon.pod;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodHeartbeatRunable implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(PodHeartbeatRunable.class);
	
	private PodData podData;
	
	private static AtomicBoolean shutdown = new AtomicBoolean(false);
	
	public PodHeartbeatRunable(PodData podData){
		this.podData = podData;
	}
	
	@Override
	public void run() {
		logger.info("*********starting PodHeartbeatRunable*************");
		try{
			while(!shutdown.get()){
				logger.debug("*******pod heartbeat alert*********");
				int timeSecs = DateUtils.getCurrentTimeSecs();
				PodHeartbeat podHeartbeat = new PodHeartbeat(
						timeSecs, podData.getTopologyId(), podData.getContainerIds(), 
						podData.getPodId(),podData.getKubeletId());
				PodLocalState.setPodHeartbeat(podHeartbeat);
				
				Integer interval = KubernetesConfig.getPodHeartbeatIntervalMs();
				Thread.sleep(interval);
			}
		}catch(Exception e){
			logger.error("",e);
		}
	}

}
