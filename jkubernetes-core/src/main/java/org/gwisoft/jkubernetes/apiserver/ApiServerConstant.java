package org.gwisoft.jkubernetes.apiserver;

public class ApiServerConstant {

	public static final String APIVERSION = "apiVersion";
	public static final String KIND = "kind";
	
	public static final String METADATA_NAME = "metadata.name";
	
	public static final String SPEC_REPLICAS = "spec.replicas";
	public static final String SPEC_TEMPLATE_SPEC_CONTAINERS = "spec.template.spec.containers";
	
	public enum Kind{
		ReplicationController
	}
}
