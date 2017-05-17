package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;

public class ContainerPort implements Serializable {

	private Integer containerPort;
	private Integer hostPort;
	public Integer getContainerPort() {
		return containerPort;
	}
	public void setContainerPort(Integer containerPort) {
		this.containerPort = containerPort;
	}
	public Integer getHostPort() {
		return hostPort;
	}
	public void setHostPort(Integer hostPort) {
		this.hostPort = hostPort;
	}
	
	
}
