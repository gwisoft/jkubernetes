package org.gwisoft.jkubernetes.docker;

import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;

public class ResourcePodSlotCommand extends ResourcePodSlot {

	private static final long serialVersionUID = 130579276289617786L;
	
	private String runCommand;

	public String getRunCommand() {
		return runCommand;
	}

	public void setRunCommand(String runCommand) {
		this.runCommand = runCommand;
	}

	
	
	
}
