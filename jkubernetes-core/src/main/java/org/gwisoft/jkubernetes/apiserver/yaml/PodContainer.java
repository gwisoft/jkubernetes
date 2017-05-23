package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;
import java.util.List;

public class PodContainer implements Serializable{

	private static final long serialVersionUID = 8214020217163549498L;
	
	private String name;
	private String image;
	private List<ContainerVolumeMount> volumeMounts;
	private List<ContainerPort> containerPorts;
	
	public List<ContainerPort> getContainerPorts() {
		return containerPorts;
	}
	public void setContainerPorts(List<ContainerPort> containerPorts) {
		this.containerPorts = containerPorts;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public List<ContainerVolumeMount> getVolumeMounts() {
		return volumeMounts;
	}
	public void setVolumeMounts(List<ContainerVolumeMount> volumeMounts) {
		this.volumeMounts = volumeMounts;
	}
	
	
}
