package org.gwisoft.jkubernetes.kubectl.remote;

import org.gwisoft.jkubernetes.apiserver.thrift.ApiServer;
import org.gwisoft.jkubernetes.config.KubernetesConfigConstant;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;



public class ApiServerClient {

	private ThriftClient thriftClient;
	
	public ApiServerClient(String host, Integer port){
		Integer timeout = (Integer)KubernetesConfigLoad.getKubernetesConfig().
				get(KubernetesConfigConstant.KUBERNETES_APISERVER_CLIENT_TIMEOUT);
		thriftClient = new ThriftClient(host, port, timeout);
	}
	
	public ApiServer.Client getClient(){
		return thriftClient.getClient();
	}
	
	public synchronized void close() {
		thriftClient.close();
    }
}
