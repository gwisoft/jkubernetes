package org.gwisoft.jkubernetes.daemon.pod;

import java.io.Serializable;

public class PodSlot implements Serializable{

	private static final long serialVersionUID = -5796974232266984443L;
	
	private String kubeletId;
    private int podId;
    
	public String getKubeletId() {
		return kubeletId;
	}
	public void setKubeletId(String kubeletId) {
		this.kubeletId = kubeletId;
	}
	public int getPodId() {
		return podId;
	}
	public void setPodId(int podId) {
		this.podId = podId;
	}
    
    
}
