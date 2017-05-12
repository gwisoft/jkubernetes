package org.gwisoft.jkubernetes.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.gwisoft.jkubernetes.utils.YamlUtils;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: KubernetesConfig
 * @author: Lincm
 * @Description: kubernetes系统配置类
 * @date: 2017年3月28日 下午8:20:26
 *
 */
public class KubernetesConfigLoad {
	
	private static final Logger logger = LoggerFactory.getLogger(KubernetesConfigLoad.class);
	
	private static Map kubernetesConfig;
	
	/**
	* @Title: initKubernetesConfig 
	* @Description: load kubernetes config
	 */
	public static void initKubernetesConfig(){
		logger.debug("Start load kubernetes config");
		kubernetesConfig = readKubernetesConfig();
		logger.debug("Successful load kubernetes config");
	}
	
	/**
	* @Title: getKubernetesConfig 
	* @Description: get kubernetes config
	* @return
	 */
	public synchronized static Map getKubernetesConfig(){
		if (kubernetesConfig == null){
			initKubernetesConfig();
		}
		
		return kubernetesConfig;
	}

	/**
	* @Title: readKubernetesConfig 
	* @Description: 读取kubernetes的配置文件
	* @return Map
	 */
	private static Map readKubernetesConfig(){
		Map defaultConf = readDefaultKubernetesConfig();
		
		String confFile = System.getProperty("kubernetes.conf.file");
		Map kubernetes;
		if(StringUtils.isBlank(confFile) == true){
			kubernetes = YamlUtils.readYaml("kubernetes.yaml",false,false);
		}else{
			kubernetes = YamlUtils.readYaml(confFile,true,false);
		}
		
		Map commandOpts = readCommandLineOpts();
		
		defaultConf.putAll(kubernetes);
		defaultConf.putAll(commandOpts);
		
		return defaultConf;
	}
	
	/**
	* @Title: readCommandLineOpts 
	* @Description: 读取启动命令行参数（kubernetes.options）
	* @return map
	 */
	private static Map readCommandLineOpts(){
		Map ret = new HashMap();
		String opts = System.getProperty("kubernetes.options");
		
		if(opts == null){
			return ret;
		}
		
		String[] configs = opts.split(",");
		for(String config: configs){
			String[] confOpts = config.split("=",2);
			if(confOpts.length == 2){
				Object val = JSONValue.parse(confOpts[1]);
				if(val == null){
					val = confOpts[1];
				}
				ret.put(confOpts[0], val);
			}
		}
		return ret;
		
		//TODO 实现了部分功能
	}

	
	/**
	* @Title: readDefaultKubernetesConfig 
	* @Description: 读取默认的配置(defaults.yaml)
	* @return map
	 */
	private static Map readDefaultKubernetesConfig(){
		return YamlUtils.readYaml("defaults.yaml", true, false);
	}
	
}
