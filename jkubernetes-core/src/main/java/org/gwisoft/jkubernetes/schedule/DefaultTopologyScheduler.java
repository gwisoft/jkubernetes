package org.gwisoft.jkubernetes.schedule;

import java.util.Set;

import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;

public class DefaultTopologyScheduler implements IToplogyScheduler {

	@Override
	public Set<ResourcePodSlot> assignTasks(TopologyAssignContext context) {

		AbstractApiServerYaml yamlProcess = AbstractApiServerYaml.getYamlTypeImpl(context);
		
		return yamlProcess.process();
	}

}
