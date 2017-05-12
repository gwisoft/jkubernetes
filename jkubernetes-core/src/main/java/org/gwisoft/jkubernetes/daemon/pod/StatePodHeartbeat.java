package org.gwisoft.jkubernetes.daemon.pod;

public class StatePodHeartbeat{
	
	private static final long serialVersionUID = 8411498219732886438L;
	
	public enum PodState{
		valid,timeout
	}
	
	private PodState podState;
	
	private PodHeartbeat podHb;

	public StatePodHeartbeat(PodHeartbeat podHb,PodState podState) {
		this.podState = podState;
		this.podHb = podHb;
	}

	public PodState getPodState() {
		return podState;
	}

	public void setPodState(PodState podState) {
		this.podState = podState;
	}

	public PodHeartbeat getPodHb() {
		return podHb;
	}

	public void setPodHb(PodHeartbeat podHb) {
		this.podHb = podHb;
	}
	
	
}
