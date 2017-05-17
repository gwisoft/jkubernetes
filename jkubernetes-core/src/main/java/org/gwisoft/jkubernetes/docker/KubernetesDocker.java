package org.gwisoft.jkubernetes.docker;

public interface KubernetesDocker {

	public boolean startContainer(ResourcePodSlotDocker slot);
	
	public void stopContainer(ResourcePodSlotDocker slot);
	
	public void stopContainer(String containerId);
	
	public boolean isRunningContainer(String containerId);

}
