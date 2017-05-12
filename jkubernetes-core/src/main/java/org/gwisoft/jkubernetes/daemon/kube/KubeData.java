package org.gwisoft.jkubernetes.daemon.kube;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubeData {
	private static final Logger logger = LoggerFactory.getLogger(KubeData.class);
	
	private Long uptime;
	
	private volatile boolean isLeader = false;
	
	public void setIsLeader(boolean isLeader){
		this.isLeader = isLeader;
	}
	
	public boolean isLeader(){
		return this.isLeader;
	}
	
	public Long getUptime(){
		return this.uptime;
	}
	
	
	KubeData(){
		uptime = System.currentTimeMillis();
	}
}
