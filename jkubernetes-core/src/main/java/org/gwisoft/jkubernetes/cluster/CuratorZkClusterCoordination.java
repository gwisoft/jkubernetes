package org.gwisoft.jkubernetes.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.gwisoft.jkubernetes.config.KubernetesConfigConstant;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CuratorZkClusterCoordination implements ZkClusterCoordination {
	
	private static final Logger logger = LoggerFactory.getLogger(CuratorZkClusterCoordination.class);

	private CuratorFramework cf;
	
	CuratorZkClusterCoordination(){
		cf = createCurator();
		initListener(cf);
		cf.start();
	}
	
	private void initListener(CuratorFramework cf){
		//cf.get
	}
	
	private CuratorFramework createCurator(){
		logger.info("start create kubernetes coordination--zookeeper instance");
		Map conf = KubernetesConfigLoad.getKubernetesConfig();
		
		//server list
		List<String> zkServers = (List<String>)conf.get(KubernetesConfigConstant.KUBERNETES_ZOOKEEPER_SERVERS);	
		Integer zkPort = (Integer)conf.get(KubernetesConfigConstant.KUBERNETES_ZOOKEEPER_PORT);
		String serverRoot = (String)conf.get(KubernetesConfigConstant.KUBERNETES_ZOOKEEPER_ROOT);
		List<String> serverPorts = new ArrayList<String>();
		for(String zkServer:zkServers){
			serverPorts.add(zkServer + ":" + zkPort);
		}
		String serverPortStr = StringUtils.join(serverPorts, ",") + serverRoot;
		
		//other config
		Integer zkConnectionTimeout = (Integer)conf.get(KubernetesConfigConstant.KUBERNETES_ZOOKEEPER_CONNECTIONG_TIMEOUT);
		Integer zkSessionTimeout = (Integer)conf.get(KubernetesConfigConstant.KUBERNETES_ZOOKEEPER_SESSION_TIMEOUT);
		Integer zkRetryTimes = (Integer)conf.get(KubernetesConfigConstant.KUBERNETES_ZOOKEEPER_RETRY_TIMES);
		Integer zkRetryInterval = (Integer)conf.get(KubernetesConfigConstant.KUBERNETES_ZOOKEEPER_RETRY_INTERVAL);
		Integer zkRetryIntervalCeiling = (Integer)conf.get(KubernetesConfigConstant.KUBERNETES_ZOOKEEPER_RETRY_INTERVAL_CEILING);
		
		//builder
		Builder builder = CuratorFrameworkFactory.builder();
		builder.connectString(serverPortStr)
			.connectionTimeoutMs(zkConnectionTimeout)
			.sessionTimeoutMs(zkSessionTimeout)
			.retryPolicy(new BoundedExponentialBackoffRetry(
					zkRetryInterval, 
					zkRetryIntervalCeiling, 
					zkRetryTimes));
		cf = builder.build();
		
		return cf;
		//TODO authorization
	}


	public boolean isNodeExisted(String path, boolean watch) throws Exception {
		String newPath = PathUtils.normalizePath(path);
		Stat stat = null;

		if(watch){
			stat = cf.checkExists().watched().forPath(newPath);
		}else{
			stat = cf.checkExists().forPath(newPath);
		}

		return stat != null;
	}

	@Override
	public boolean createNode(String path, byte[] data, CreateMode mode) throws Exception {
		String newPath = PathUtils.normalizePath(path);
		
		if(isNodeExisted(newPath, false)){
			cf.setData().forPath(newPath,data);
		}else{
			cf.create().withMode(mode).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(newPath, data);
		}
		
		return true;
	}

	@Override
	public byte[] getData(String path, boolean watch) throws Exception {
		String newPath = PathUtils.normalizePath(path);
		if(isNodeExisted(newPath,watch)){
			if(watch){
				return cf.getData().watched().forPath(newPath);
			}else{
				return cf.getData().forPath(newPath);
			}
		}else{
			logger.warn("node:" + newPath + " isn't exist!");
		}
		return null;
	}

	@Override
	public List<String> getChildren(String path, boolean watch) throws Exception {
		String newPath = PathUtils.normalizePath(path);
		try{
			if(watch){
				return cf.getChildren().watched().forPath(newPath);
			}else{
				return cf.getChildren().forPath(newPath);
			}
		}catch(KeeperException e){
			logger.warn("",e);
			return new ArrayList<String>();
		}
		
	}

	@Override
	public boolean deleteNode(String path)  throws Exception {
		String newPath = PathUtils.normalizePath(path);
		
		if(isNodeExisted(newPath, false)){
			cf.delete().guaranteed().deletingChildrenIfNeeded().forPath(newPath);
		}
		return true;
	}

	@Override
	public boolean mkdirs(String path, CreateMode mode)  throws Exception  {
		String npath = PathUtils.normalizePath(path);
		
		if(npath.equals("/")){
			return true;
		}
		
		if(isNodeExisted(npath, false)){
			return true;
		}
		
		mkdirs(PathUtils.getParentPath(npath), mode);
		
		try{
			createNode(npath, new byte[0], mode);
		}catch(KeeperException e){
			logger.warn("zookeeper mkdirs for path" + path,e);
		}
		
		return false;
	}

	@Override
	public void setData(String path, byte[] data) throws Exception {
		String nPath = PathUtils.normalizePath(path);
		cf.setData().forPath(nPath,data);
	}
	
	


}
