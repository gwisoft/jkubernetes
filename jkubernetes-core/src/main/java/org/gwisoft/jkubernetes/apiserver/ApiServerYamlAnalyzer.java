package org.gwisoft.jkubernetes.apiserver;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.gwisoft.jkubernetes.apiserver.yaml.ApiServerYaml;
import org.gwisoft.jkubernetes.apiserver.yaml.PodContainer;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.gwisoft.jkubernetes.utils.YamlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServerYamlAnalyzer {
	
	private String topologyId;
	private ApiServerYaml apiServerYaml;
	private static final Logger logger = LoggerFactory.getLogger(ApiServerYamlAnalyzer.class);

	public ApiServerYamlAnalyzer(byte[] yaml){
		apiServerYaml = YamlUtils.YamlToObject(yaml, ApiServerYaml.class);
		tempSaveFile(yaml);
		formatCheck();
	}

	public void tempSaveFile(byte[] yaml){
		try {
			FileUtils.writeByteArrayToFile(
					new File(KubernetesConfig.getTopologyYamlFile(getTopologyId())), yaml);
		} catch (IOException e) {
			logger.error("",e);
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	/**
	 * topology format check
	* @Title: formatCheck 
	* @Description: 
	 */
	private void formatCheck(){
		//TODO 
	}
	
	public String getTopologyName(){
		return apiServerYaml.getMetadata().getName();
	}
	
	public String getTopologyId(){
		if(topologyId != null){
			return topologyId;
		}else{
			topologyId = KubernetesUtils.topologyNameToId(apiServerYaml.getMetadata().getName());
			return topologyId;
		}
		
	}

	public ApiServerYaml getApiServerYaml() {
		return apiServerYaml;
	}

	public void setApiServerYaml(ApiServerYaml apiServerYaml) {
		this.apiServerYaml = apiServerYaml;
	}

	
	

}
