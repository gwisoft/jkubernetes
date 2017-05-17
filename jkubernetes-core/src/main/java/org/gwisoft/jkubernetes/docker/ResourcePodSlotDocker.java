package org.gwisoft.jkubernetes.docker;

import org.gwisoft.jkubernetes.apiserver.yaml.TemplateSpec;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;

public class ResourcePodSlotDocker extends ResourcePodSlot {

	private TemplateSpec templateSpec;

	public TemplateSpec getTemplateSpec() {
		return templateSpec;
	}

	public void setTemplateSpec(TemplateSpec templateSpec) {
		this.templateSpec = templateSpec;
	}
}
