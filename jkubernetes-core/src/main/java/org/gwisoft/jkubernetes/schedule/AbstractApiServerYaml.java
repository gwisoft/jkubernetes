package org.gwisoft.jkubernetes.schedule;

import java.util.Set;

import org.gwisoft.jkubernetes.apiserver.ApiServerConstant;
import org.gwisoft.jkubernetes.apiserver.yaml.ApiServerYaml;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.exception.BusinessException;

public abstract class AbstractApiServerYaml {

	public abstract Set<ResourcePodSlot> process();
	
	public static AbstractApiServerYaml  getYamlTypeImpl(TopologyAssignContext contex){
		ApiServerYaml apiServerYaml = contex.getApiServerYaml();
		String kind = apiServerYaml.getKind();
		if(kind.equalsIgnoreCase(ApiServerConstant.Kind.ReplicationController.toString())){
			return new ApiServerYamlRC(contex);
		}else{
			throw new BusinessException("nonsupport kind:" + kind);
		}
	}
}
