package org.gwisoft.jkubernetes.apiserver;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.utils.KubernetesUtils;
import org.gwisoft.jkubernetes.utils.YamlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServerYamlAnalyzer {
	
	private Map yamlMap;
	private String topologyId;
	private static final Logger logger = LoggerFactory.getLogger(ApiServerYamlAnalyzer.class);

	public ApiServerYamlAnalyzer(byte[] yaml){
		yamlMap = YamlUtils.YamlToMap(yaml);
		tempSaveFile(yaml);
		formatCheck();
	}
	
	public Map getYamlMap() {
		return yamlMap;
	}

	public void setYamlMap(Map yamlMap) {
		this.yamlMap = yamlMap;
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
	
	public String getTopologyId(){
		if(topologyId != null){
			return topologyId;
		}else{
			topologyId = KubernetesUtils.topologyNameToId((String)getYamlValue(ApiServerConstant.METADATA_NAME));
			return topologyId;
		}
		
	}
	
	public String getTopologyName(){
		return (String)getYamlValue(ApiServerConstant.METADATA_NAME);
	}
	
	public Object getYamlValue(String key){
		return this.getYamlValue(key, yamlMap);
	}
	
	public void setYamlValue(String key,Object value){
		Map tempMap = yamlMap;
		
		String[] keys = key.split("\\.");
		Object object = null;
		String lastKey = null;
		for(String keyNode:keys){
			if(object != null){
				tempMap = (Map)object;
			}
			object = tempMap.get(keyNode);
			lastKey = keyNode;
		}
		tempMap.put(lastKey,value);
		
	}
	
	public static Object getYamlValue(String key,Map yamlMap){
		if(yamlMap == null || key == null || key.trim().equals("")){
			return null;
		}
		
		String[] keys = key.split("\\.");
		Object object = null;
		for(String keyNode:keys){
			if(object != null){
				yamlMap = (Map)object;
			}
			object = yamlMap.get(keyNode);
		}
		
		return object;
	}
	
	public static void setYamlValue(String key,Object value,Map yamlMap){
		Map tempMap = yamlMap;
		
		String[] keys = key.split("\\.");
		Object object = null;
		String lastKey = null;
		for(String keyNode:keys){
			if(object != null){
				tempMap = (Map)object;
			}
			object = tempMap.get(keyNode);
			lastKey = keyNode;
		}
		tempMap.put(lastKey,value);
	}
}
