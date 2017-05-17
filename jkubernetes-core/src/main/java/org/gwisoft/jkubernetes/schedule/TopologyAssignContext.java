package org.gwisoft.jkubernetes.schedule;

import java.util.Map;

import org.gwisoft.jkubernetes.apiserver.yaml.ApiServerYaml;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignEvent;

public class TopologyAssignContext {

	private String topologyId;
	private ApiServerYaml apiServerYaml;
	private TopologyAssignEvent event;

	public String getTopologyId() {
		return topologyId;
	}

	public void setTopologyId(String topologyId) {
		this.topologyId = topologyId;
	}

	public ApiServerYaml getApiServerYaml() {
		return apiServerYaml;
	}

	public void setApiServerYaml(ApiServerYaml apiServerYaml) {
		this.apiServerYaml = apiServerYaml;
	}

	public TopologyAssignEvent getEvent() {
		return event;
	}

	public void setEvent(TopologyAssignEvent event) {
		this.event = event;
	}
	
	
}
