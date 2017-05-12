package org.gwisoft.jkubernetes.schedule;

import java.util.Map;

import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignEvent;

public class TopologyAssignContext {

	private String topologyId;
	private Map yamlMap;
	private TopologyAssignEvent event;

	public String getTopologyId() {
		return topologyId;
	}

	public void setTopologyId(String topologyId) {
		this.topologyId = topologyId;
	}

	public Map getYamlMap() {
		return yamlMap;
	}

	public void setYamlMap(Map yamlMap) {
		this.yamlMap = yamlMap;
	}

	public TopologyAssignEvent getEvent() {
		return event;
	}

	public void setEvent(TopologyAssignEvent event) {
		this.event = event;
	}
	
	
}
