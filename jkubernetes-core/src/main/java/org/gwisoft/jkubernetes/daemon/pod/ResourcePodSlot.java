package org.gwisoft.jkubernetes.daemon.pod;

import java.io.Serializable;
import java.util.Set;

public class ResourcePodSlot extends PodSlot implements Serializable{

	private static final long serialVersionUID = 7567717054005232881L;
	
	private String hostname;
    private long memSize;
    private int cpu;
    private String jvm;
    private Set<Integer> containerIds;
    private AssignmentState state;
    private String topologyId;
    
    //assignment timestamp
    private long timestamp;
	
	public enum AssignmentState{
		Assignmenting,AssignmentFail,AssignmentSuccess
	}
    
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getTopologyId() {
		return topologyId;
	}
	public void setTopologyId(String topologyId) {
		this.topologyId = topologyId;
	}
	public Set<Integer> getContainerIds() {
		return containerIds;
	}
	public void setContainerIds(Set<Integer> containerIds) {
		this.containerIds = containerIds;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public long getMemSize() {
		return memSize;
	}
	public void setMemSize(long memSize) {
		this.memSize = memSize;
	}
	public int getCpu() {
		return cpu;
	}
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	public String getJvm() {
		return jvm;
	}
	public void setJvm(String jvm) {
		this.jvm = jvm;
	}
	public AssignmentState getState() {
		return state;
	}
	public void setState(AssignmentState state) {
		this.state = state;
	}
	@Override
	public String toString() {
		return "ResourcePodSlot [hostname=" + hostname + ", memSize=" + memSize + ", cpu=" + cpu + ", jvm=" + jvm
				+ ", containerIds=" + containerIds + ", state=" + state + ", topologyId=" + topologyId + ", timestamp="
				+ timestamp + "]";
	}
    
    
}
