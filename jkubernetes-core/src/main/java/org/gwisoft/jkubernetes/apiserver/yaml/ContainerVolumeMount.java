package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;

public class ContainerVolumeMount implements Serializable{

	private static final long serialVersionUID = -3941514362022288640L;
	
	private String name;
	private String mountPath;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMountPath() {
		return mountPath;
	}
	public void setMountPath(String mountPath) {
		this.mountPath = mountPath;
	}
	
	
}
