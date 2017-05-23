package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;

public class RootSpec implements Serializable{

	private static final long serialVersionUID = -5683465824543815590L;
	
	private Integer replicas;
	private Template template;
	public Integer getReplicas() {
		return replicas;
	}
	public void setReplicas(Integer replicas) {
		this.replicas = replicas;
	}
	public Template getTemplate() {
		return template;
	}
	public void setTemplate(Template template) {
		this.template = template;
	}
	
	
}
