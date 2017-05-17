package org.gwisoft.jkubernetes.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwisoft.jkubernetes.apiserver.yaml.ApiServerYaml;
import org.yaml.snakeyaml.Yaml;

public class YamlUtils {

	/**
	* @Title: readYaml 
	* @Description: 读取yaml的配置
	* @param fileName 文件名
	* @param mustExist 文件是否必须要存在
	* @param canMultiple 文件是否可以多个
	* @return map
	 */
	public static Map readYaml(String fileName,boolean mustExist,boolean canMultiple){
		Map rets = new HashMap();
		
		try{
			Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(fileName);
			
			List<URL> urls = new ArrayList<URL>();
			while(resources.hasMoreElements()){
				urls.add(resources.nextElement());
			}
			
			if(mustExist == true && urls.isEmpty()){
				throw new RuntimeException("Could not find config file on classpath " + fileName);
			}
			
			
			for(URL url:urls){
				InputStream in = null;
				try{
					in = url.openStream();
					
					Yaml yaml = new Yaml();
					Map ret = (Map)yaml.load(new InputStreamReader(in));
					if(ret != null){
						rets.putAll(ret);
					}
				}finally{
					if (in != null){
						in.close();
					}
				}
				
			}
			
			if(mustExist == true && rets.isEmpty()){
				throw new RuntimeException("file " + fileName + " doesn't have any valid kubernetes configs");
			}
			
			return rets;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static Map YamlToMap(byte[] yamlByte){
		Yaml yaml = new Yaml();
		InputStream is = new ByteArrayInputStream(yamlByte);
		Map ret = (Map)yaml.load(new InputStreamReader(is));
		
		return ret;
	}
	
	public static <T> T YamlToObject(byte[] yamlByte,Class<T> type){
		Yaml yaml = new Yaml();
		InputStream is = new ByteArrayInputStream(yamlByte);
		T ret = yaml.loadAs(is, type);
		
		return ret;
	}
}
