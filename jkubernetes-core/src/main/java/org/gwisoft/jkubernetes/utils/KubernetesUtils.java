package org.gwisoft.jkubernetes.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gwisoft.jkubernetes.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(KubernetesUtils.class);
	
	public static long SIZE_1_K = 1024;
	public static long SIZE_1_M = SIZE_1_K * 1024;
	public static long SIZE_1_G = SIZE_1_M * 1024;
	public static long SIZE_1_T = SIZE_1_G * 1024;
	public static long SIZE_1_P = SIZE_1_T * 1024;

	/**
	* @Title: kill 
	* @Description: kill the specified pid
	* @param pid
	 */
	public static void kill(String pid){
		logger.info("kill pid:" + pid);
		processKill(pid);
		
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			logger.debug("",e);
		}
		if(!ExecCommandUtils.isWindows()){
			ensureProcessKill(pid);
		}
	}
	
	/**
	* @Title: processKill 
	* @Description: standard kill the specified pid
	* @param pid
	 */
	private static void processKill(String pid){
		String command = ExecCommandUtils.isWindows()?"tskill " + pid:"kill " + pid;
		try {
			ExecCommandUtils.execCommand(command);
		} catch (IOException e) {
			logger.error("Failed to Exec Command: " + command,e);
		}
	}
	
	/**
	* @Title: ensureProcessKill 
	* @Description: force kill the specified pid and reprocessing 5 times
	* @param pid
	 */
	private static void ensureProcessKill(String pid){
		String command = "kill -9 " + pid;
		for (int i = 0; i < 5; i++) {
            try {
            	ExecCommandUtils.execCommand(command);
                Thread.sleep(100);
            }catch (Exception e) {
            	logger.error("Failed to Exec Command: " + command,e);
            }
        }
	}
	
	/**
	 * 
	* @Title: getProcessPid 
	* @Description: get current process pid
	* @return
	 */
	public static String getProcessPid(){
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String[] split = name.split("@");
		
		if(split.length != 2){
			throw new RuntimeException("got unexpected process name:" + name + ", expected result ***@***");
		}
		
		return split[0];
	}

    public static void haltProcess(int val, String msg) {
        logger.info("Halting process: " + msg);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        Runtime.getRuntime().halt(val);
    }
    
    public static Long getPhysicMemorySize(){
    	Object object;
        try {
            object = ManagementFactory.getPlatformMBeanServer().getAttribute(
                    new ObjectName("java.lang", "type", "OperatingSystem"), "TotalPhysicalMemorySize");
        } catch (Exception e) {
            logger.warn("Failed to get system physical memory size,", e);
            return null;
        }

        Long ret = (Long) object;

        return ret;
    }
    
    public static <V> Set<V> listToSet(List<V> list){
    	if(list == null){
    		return null;
    	}
    	
    	Set<V> set = new HashSet<V>();
    	set.addAll(list);
    	return set;
    }
    
    public static String topologyNameToId(String topologyName){
    	return topologyName + "-" + DateUtils.getCurrentTimeSecs(); 
    }
    
    /**
	* @Title: createPid 
	* @Description: create pid
	* 1、verify file is exist and is directory
	* 2、delete old pid
	* 3、create new current pid
	* @param pidsDir
	 */
	public static String createPid(String pidsDir){
		File file = new File(pidsDir);
		if(!file.exists()){
			file.mkdir();
		}else if(!file.isDirectory()){
			throw new RuntimeException("pids dir: " + pidsDir + " isn't directory");
		}
		
		String[] existPids = file.list();
		for(String existPid:existPids){
			KubernetesUtils.kill(existPid);
			PathUtils.rmPath(pidsDir + File.separator + existPid);
		}
		
		String pid = KubernetesUtils.getProcessPid();
		
		String pidPath = pidsDir + File.separator + pid;
		try {
			FileUtils.touch(new File(pidPath));
			return pid;
		} catch (IOException e) {
			logger.error("failed to create pid:" + pidPath,e);
			throw new BusinessException("failed to create pid:" + pidPath,e);
		}
	}

}
