package org.gwisoft.jkubernetes.cluster;

import java.util.List;

import org.apache.zookeeper.CreateMode;

public interface ZkClusterCoordination {

	public boolean isNodeExisted(String path,boolean watch) throws Exception;
	
	public boolean createNode(String path, byte[] data,CreateMode mode) throws Exception;
	
	public byte[] getData(String path, boolean watch) throws Exception;
	
	public List<String> getChildren(String path, boolean watch) throws Exception;
	
	public boolean deleteNode(String path)  throws Exception;
	
	public boolean mkdirs(String path,CreateMode mode)   throws Exception ;
	
	public void setData(String path,byte[] data) throws Exception;
}
