package org.gwisoft.jkubernetes.apiserver;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.thrift.TException;
import org.gwisoft.jkubernetes.apiserver.thrift.ApiServer.Iface;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.daemon.kube.KubeData;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignEvent;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignEvent.AssignType;
import org.gwisoft.jkubernetes.daemon.kube.TopologyAssignRunnable;
import org.gwisoft.jkubernetes.exception.FailedAssignTopologyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceHandler implements Iface {

	private static final Logger logger = LoggerFactory.getLogger(ServiceHandler.class);
	
	public final static int MIN_THREAD_NUM = 1;
	public final static int MAX_THREAD_NUM = 10;
	
	private KubeData data;
	
	public ServiceHandler(KubeData data){
		this.data = data;
	}
	@Override
	public void submitTopology(ByteBuffer yaml) throws TException {
		byte[] bytes = new byte[yaml.limit() - yaml.position()];
		yaml.get(bytes);
		System.out.println(DigestUtils.md5Hex(bytes));
		ApiServerYamlAnalyzer analyzer = new ApiServerYamlAnalyzer(bytes);

		TopologyAssignEvent assignEvent = new TopologyAssignEvent();
		String topologyId = analyzer.getTopologyId();
        assignEvent.setTopologyId(topologyId);
        assignEvent.setTopologyName(analyzer.getToplogyName());
        assignEvent.setYamlMap(analyzer.getYamlMap());
        assignEvent.setAssignType(AssignType.assign);

        TopologyAssignRunnable.push(assignEvent);

        boolean isSuccess = assignEvent.waitFinish();
        if (isSuccess == true) {
            logger.info("Finish submit for " + assignEvent.getTopologyName());
        } else {
            throw new FailedAssignTopologyException(assignEvent.getErrorMsg());
        }
	}
	@Override
	public void submitTopologyStr(String yaml) throws TException {
		byte[] bytes = null;
		try {
			bytes = yaml.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		System.out.println(DigestUtils.md5Hex(bytes));
		ApiServerYamlAnalyzer analyzer = new ApiServerYamlAnalyzer(bytes);

		TopologyAssignEvent assignEvent = new TopologyAssignEvent();
		String topologyId = analyzer.getTopologyId();
        assignEvent.setTopologyId(topologyId);
        assignEvent.setTopologyName(analyzer.getToplogyName());
        assignEvent.setYamlMap(analyzer.getYamlMap());
        assignEvent.setAssignType(AssignType.assign);

        TopologyAssignRunnable.push(assignEvent);

        boolean isSuccess = assignEvent.waitFinish();
        if (isSuccess == true) {
            logger.info("Finish submit for " + assignEvent.getTopologyName());
        } else {
            throw new FailedAssignTopologyException(assignEvent.getErrorMsg());
        }
		
	}

}
