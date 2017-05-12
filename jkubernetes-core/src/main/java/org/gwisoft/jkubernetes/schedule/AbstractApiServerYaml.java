package org.gwisoft.jkubernetes.schedule;

import java.util.Map;
import java.util.Set;

import org.gwisoft.jkubernetes.apiserver.ApiServerConstant;
import org.gwisoft.jkubernetes.apiserver.ApiServerYamlAnalyzer;
import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;
import org.gwisoft.jkubernetes.exception.BusinessException;

public abstract class AbstractApiServerYaml {

	public abstract Set<ResourcePodSlot> process();
	
	public static AbstractApiServerYaml  getYamlTypeImpl(TopologyAssignContext contex){
		Map yamlMap = contex.getYamlMap();
		String kind = (String)ApiServerYamlAnalyzer.getYamlValue(ApiServerConstant.KIND,yamlMap);
		if(kind.equalsIgnoreCase(ApiServerConstant.Kind.ReplicationController.toString())){
			return new ApiServerYamlRC(contex);
		}else{
			throw new BusinessException("nonsupport kind:" + kind);
		}
	}
}
