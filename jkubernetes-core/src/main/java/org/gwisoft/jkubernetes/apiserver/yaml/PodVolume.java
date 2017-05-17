package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;

public class PodVolume implements Serializable{

	private String name;
	private String hostPath;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHostPath() {
		return hostPath;
	}
	public void setHostPath(String hostPath) {
		this.hostPath = hostPath;
	} 
	
	
}
