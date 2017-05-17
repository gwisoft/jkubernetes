package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;
import java.util.List;

public class TemplateSpec implements Serializable{

	private List<PodVolume> volumes;
	
	private List<PodContainer> containers;
	
	private String command;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<PodVolume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<PodVolume> volumes) {
		this.volumes = volumes;
	}

	public List<PodContainer> getContainers() {
		return containers;
	}

	public void setContainers(List<PodContainer> containers) {
		this.containers = containers;
	}
	
	
	
	
}
