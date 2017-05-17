package org.gwisoft.jkubernetes.apiserver.yaml;

import java.io.Serializable;

public class ApiServerYaml implements Serializable{

	private String apiVersion;
	private String kind;
	private RootMetadata metadata;
	private RootSpec spec;
	
	public RootSpec getSpec() {
		return spec;
	}
	public void setSpec(RootSpec spec) {
		this.spec = spec;
	}
	public String getApiVersion() {
		return apiVersion;
	}
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public RootMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(RootMetadata metadata) {
		this.metadata = metadata;
	}
	
	
}
