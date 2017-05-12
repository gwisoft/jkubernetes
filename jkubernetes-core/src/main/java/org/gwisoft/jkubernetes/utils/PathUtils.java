package org.gwisoft.jkubernetes.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathUtils {

	private static final Logger logger = LoggerFactory.getLogger(PathUtils.class);
	
	public static void rmPath(String path){
		logger.debug("delete path: " + path);
		
		boolean isDelete = new File(path).delete();
		
		if(!isDelete){
			throw new RuntimeException("failed to delete: " + path);
		}
	}
	
	/**
	* @Title: normalizePath 
	* @Description: normalize path(for example: /node//subNode -----> /node/subNode)
	* @param path
	* @return
	 */
	public static String normalizePath(String path){
		return unTokenizerPath(tokenizerPath(path));
	}
	
	public static List<String> readSubFileNames(String parentDir){
		List<String> fileNames = new ArrayList<String>();
		File dir = new File(parentDir);
		if(!dir.exists()){
			return fileNames;
		}
		if(!dir.isDirectory()){
			throw new RuntimeException("Current dir:" + parentDir + " isn't directory");
		}
		
		File[] files = dir.listFiles();
		for(File file:files){
			fileNames.add(file.getName());
		}
		
		return fileNames;
	}
	
	public static List<String> tokenizerPath(String path){
		String[] pathNodes = path.split("/");
		
		List<String> newPathNodes = new ArrayList<String>();
		for(String pathNode:pathNodes){
			if(!pathNode.isEmpty()){
				newPathNodes.add(pathNode);
			}
		}
		
		return newPathNodes;
	}
	
	public static String unTokenizerPath(List<String> paths){
		StringBuffer newPath = new StringBuffer();
		
		for(String pathNode:paths){
			newPath.append("/");
			newPath.append(pathNode);
		}
		if(newPath.length() == 0){
			newPath.append("/");
		}
		
		return newPath.toString();
	}
	
	public static String getParentPath(String path){
		List<String> newPathNodes = tokenizerPath(path);
		
		if(newPathNodes.size() > 0){
			newPathNodes.remove(newPathNodes.size() - 1);
		}
		return unTokenizerPath(newPathNodes);
		
		
	}
}
