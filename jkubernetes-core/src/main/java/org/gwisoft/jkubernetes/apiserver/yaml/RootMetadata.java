package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;

public class RootMetadata implements Serializable{

	private static final long serialVersionUID = 7407984233447262503L;
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
