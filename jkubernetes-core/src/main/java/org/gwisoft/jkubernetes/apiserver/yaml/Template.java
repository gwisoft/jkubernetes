package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;

public class Template implements Serializable{

	private static final long serialVersionUID = 9176583856101813261L;
	
	private TemplateSpec spec;

	public TemplateSpec getSpec() {
		return spec;
	}

	public void setSpec(TemplateSpec spec) {
		this.spec = spec;
	}
	
	
}
