package org.gwisoft.jkubernetes.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesConfig {

	private static final String FILE_SEPERATEOR = File.separator;
	
	private static final Logger logger = LoggerFactory.getLogger(KubernetesConfig.class);
	
	private static final Map kubernetesConfig;
	
	static{
		kubernetesConfig = KubernetesConfigLoad.getKubernetesConfig();
	}
	
	public static Map getKubernetesconfig() {
		return kubernetesConfig;
	}

	/**
	* @Title: vaildateDistributedMode 
	* @Description: 验证当前运行的是分布式模式，不然报错
	 */
	public static void validateDistributedMode(){
		if(localMode()){
			throw new IllegalArgumentException("Cannot start server in local mode!");
		}
		logger.debug("start server in distributed mode");
	}
	
	/**
	* @Title: localMode 
	* @Description: 是否本地模式
	* @return
	 */
	public static boolean localMode(){
		String mode = (String)kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_CLUSTER_MODE);
		
		if(mode != null){
			if ("local".equals(mode)) {
                return true;
            }

            if ("distributed".equals(mode)) {
                return false;
            }
		}
		
		throw new IllegalArgumentException("Illegal cluster mode in " + 
				KubernetesConfigConstant.KUBERNETES_CLUSTER_MODE +":" + mode + ",The range of valid value are local and distributed");
	}
	
	/**
	* @Title: getKubePidsDir 
	* @Description: get pids dir in nimber
	* @return
	* @throws IOException
	 */
	public static String getKubePidsDir() throws IOException{
		String pidsDir = getKubeLocalDir() + FILE_SEPERATEOR + "pids";
		
		try{
			FileUtils.forceMkdir(new File(pidsDir));
		}catch(IOException e){
			logger.error("Failed to create dir " + pidsDir,e);
			throw e;
		}
		
		return pidsDir;
	}
	
	/**
	* @Title: getKubeLocalDir 
	* @Description: get nimber local dir
	* @return String
	* @throws IOException
	 */
	public static String getKubeLocalDir() throws IOException{
		String localKubeDir = kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_LOCAL_DIR) + FILE_SEPERATEOR + "kube";
		try {
			FileUtils.forceMkdir(new File(localKubeDir));
		} catch (IOException e) {
			logger.error("Failed to create dir " + localKubeDir,e);
			throw e; 
		}
		
		return localKubeDir;
	}
	
	public static String getKubeletLocalRootDir() throws IOException{
		String localKubeletDir = kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_LOCAL_DIR) + FILE_SEPERATEOR + "kubelet";
		try {
			FileUtils.forceMkdir(new File(localKubeletDir));
		} catch (IOException e) {
			logger.error("Failed to create dir " + localKubeletDir,e);
			throw e; 
		}
		return localKubeletDir;
	}
	
	public static String getKubeletAssignPath(String kubeletId) throws IOException{
		String assignPath = getKubeletLocalRootDir() + FILE_SEPERATEOR + "assignment";
		
		try {
			FileUtils.forceMkdir(new File(assignPath));
		} catch (IOException e) {
			logger.error("Failed to create dir " + assignPath,e);
			throw e; 
		}
		
		String assignFile = assignPath + FILE_SEPERATEOR + kubeletId;
		return assignFile;
		
	}
	
	public static String getKubeletPidDir() throws IOException{
		String pidsDir = getKubeletLocalRootDir() + FILE_SEPERATEOR + "pids";
		
		try{
			FileUtils.forceMkdir(new File(pidsDir));
		}catch(IOException e){
			logger.error("Failed to create dir " + pidsDir,e);
			throw e;
		}
		
		return pidsDir;
	}
	
	public static String getKubeletIdDir() throws IOException{
		String idsDir = getKubeletLocalRootDir() + FILE_SEPERATEOR + "ids";
		
		try{
			FileUtils.forceMkdir(new File(idsDir));
		}catch(IOException e){
			logger.error("Failed to create dir " + idsDir,e);
			throw e;
		}
		
		return idsDir;
	}
	
	/**
	* @Title: getMasterDistLocalPath 
	* @Description: get master kube local dist path
	* @return
	 */
	public static String getMasterAssignmentLocalPath(){
		String kubeLocalDir;
		try {
			kubeLocalDir = getKubeLocalDir();
			String distPath = kubeLocalDir + FILE_SEPERATEOR + "masterassignment";
			
			try {
				FileUtils.forceMkdir(new File(distPath));
			} catch (IOException e) {
				logger.error("Failed to create dir " + distPath,e);
				throw e; 
			}
			
			return distPath;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static int getHttpServerViewLogPort(){
		Integer port = (Integer)kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_HTTP_SERVER_VIEW_LOG_PORT);
		return port;
	}
	
	public static String getLocalPodsRootDir(){
		return kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_LOCAL_DIR) + FILE_SEPERATEOR + "pods";
	}
	
	public static String getLocalPodRootDir(Integer podId){
		String ret = getLocalPodsRootDir() + FILE_SEPERATEOR + podId;
		try {
			FileUtils.forceMkdir(new File(ret));
		} catch (IOException e) {
			logger.error("Failed to create dir " + ret,e);
			throw new RuntimeException(e);
		}
        return ret;

	}
	
	public static String getLocalPodPidsDir(Integer podId){
		String ret = getLocalPodRootDir(podId) + FILE_SEPERATEOR + "pids";
		try {
			FileUtils.forceMkdir(new File(ret));
		} catch (IOException e) {
			logger.error("Failed to create dir " + ret,e);
			throw new RuntimeException(e);
		}
        return ret;
	}
	
	public static String getPodHeartbeatsDir(Integer podId){
		String ret = getLocalPodRootDir(podId) + FILE_SEPERATEOR + "heartbeats";
        try {
			FileUtils.forceMkdir(new File(ret));
		} catch (IOException e) {
			logger.error("Failed to create dir " + ret,e);
			throw new RuntimeException(e);
		}
        return ret;
	}
	
	public static String getPodHeartbeatsFilePath(Integer podId){
		String ret = getPodHeartbeatsDir(podId) + FILE_SEPERATEOR + podId;
		return ret;
	}
	
	public static String getTopologyYamlFileRootDir() throws Exception{
		String fileDir = getKubeLocalDir() + FILE_SEPERATEOR + "topolgy_yaml_file_temp";
		
		try{
			FileUtils.forceMkdir(new File(fileDir));
		}catch(IOException e){
			logger.error("Failed to create dir " + fileDir,e);
			throw e;
		}
		
		return fileDir;
	}
	
	public static String getTopologyYamlFile(String topologyId) throws Exception{
		String filePath = getTopologyYamlFileRootDir() + FILE_SEPERATEOR + topologyId;
		
		return filePath;
	}
	
	public static Integer getPodHeartbeatIntervalMs(){
		Integer interval = (Integer)kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_POD_HEARTBEAT_INTERVAL_MS);
		return interval;
	}
	
	public static List<Integer> getKubeletPodIdList(){
		List<Integer> portList = (List<Integer>) kubernetesConfig.get(KubernetesConfigConstant.KUBERNETES_KUBELET_SLOTS_PODIDS);

        if (portList != null && portList.size() > 0) {
            return new ArrayList<Integer>(portList);
        }else{
        	throw new RuntimeException("please config :" + KubernetesConfigConstant.KUBERNETES_KUBELET_SLOTS_PODIDS);
        }

	}

	
}
