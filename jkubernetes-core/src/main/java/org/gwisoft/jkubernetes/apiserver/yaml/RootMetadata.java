package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;

public class RootMetadata implements Serializable{

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
