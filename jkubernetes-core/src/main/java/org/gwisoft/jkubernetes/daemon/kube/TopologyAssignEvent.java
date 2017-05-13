package org.gwisoft.jkubernetes.daemon.kube;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TopologyAssignEvent {

	private String topologyId;
	
	private String topologyName;
	
	private AssignType assignType;
	
	private long timestamp = System.currentTimeMillis();
	
	public enum AssignType{
		anewAssign,assign,delete
	}
	
	private Map<String, Object> yamlMap;
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	private boolean isSuccess = false;
	
	// unit is minutes
    private static final int DEFAULT_WAIT_TIME = 5;
    
    private String errorMsg;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getTopologyId() {
		return topologyId;
	}

	public void setTopologyId(String topologyId) {
		this.topologyId = topologyId;
	}

	public Map<String, Object> getYamlMap() {
		return yamlMap;
	}

	public void setYamlMap(Map<String, Object> yamlMap) {
		this.yamlMap = yamlMap;
	}

	public String getTopologyName() {
		return topologyName;
	}

	public void setTopologyName(String topologyName) {
		this.topologyName = topologyName;
	}

	public boolean waitFinish() {
        try {
            latch.await(DEFAULT_WAIT_TIME, TimeUnit.MINUTES);
        } catch (InterruptedException e) {

        }
        return isSuccess;
    }

    public boolean isFinish() {
        return latch.getCount() == 0;
    }

    public void done() {
        isSuccess = true;
        latch.countDown();
    }

    public void fail(String errorMsg) {
        isSuccess = false;
        this.errorMsg = errorMsg;
        latch.countDown();
    }

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public AssignType getAssignType() {
		return assignType;
	}

	public void setAssignType(AssignType assignType) {
		this.assignType = assignType;
	}

	@Override
	public String toString() {
		return "TopologyAssignEvent [topologyId=" + topologyId + ", topologyName=" + topologyName + ", assignType="
				+ assignType + ", timestamp=" + timestamp + ", yamlMap=" + yamlMap + "]";
	}
	
	
	
}
