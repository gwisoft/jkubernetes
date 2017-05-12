package org.gwisoft.jkubernetes.config;

public class KubernetesConfigConstant {

	public static final String KUBERNETES_CLUSTER_MODE = "kubernetes.cluster.mode";
	public static final String KUBERNETES_LOCAL_DIR = "kubernetes.local.dir";
	
	//api server
	public static final String KUBERNETES_APISERVER_PORT = "kubernetes.apiserver.port";
	public static final String KUBERNETES_APISERVER_MAXBUFFERSIZE = "kubernetes.apiserver.max_buffer_size";
	
	//kube server
	public static final String KUBERNETES_KUBE_MONITOR_TOPOLOGY_PERIOD_SECS = "kubernetes.kube.monitor.topology.period.secs";
	public static final String KUBERNETES_KUBE_KUBELET_HEARTBEAT_TIMEOUT_SECS = "kubernetes.kube.kubelet.heartbeat.timeout.secs";
	
	//zookeeper base config
	public static final String KUBERNETES_ZOOKEEPER_ROOT = "kubernetes.zookeeper.root";
	public static final String KUBERNETES_ZOOKEEPER_SERVERS = "kubernetes.zookeeper.servers";	
	public static final String KUBERNETES_ZOOKEEPER_PORT = "kubernetes.zookeeper.port";	
	public static final String KUBERNETES_ZOOKEEPER_SESSION_TIMEOUT = "kubernetes.zookeeper.session.timeout";	
	public static final String KUBERNETES_ZOOKEEPER_CONNECTIONG_TIMEOUT = "kubernetes.zookeeper.connection.timeout";
	public static final String KUBERNETES_ZOOKEEPER_RETRY_TIMES = "kubernetes.zookeeper.retry.times";
	public static final String KUBERNETES_ZOOKEEPER_RETRY_INTERVAL = "kubernetes.zookeeper.retry.interval";
	public static final String KUBERNETES_ZOOKEEPER_RETRY_INTERVAL_CEILING = "kubernetes.zookeeper.retry.interval.ceiling";
	
	//http server
	public static final String KUBERNETES_HTTP_SERVER_VIEW_LOG_PORT = "kubernetes.http_server.view.log.port";
	
	//kubelet
	public static final String KUBERNETES_KUBELET_SLOTS_PODIDS = "kubernetes.kubelet.slots.podids";
	protected static final String KUBERNETES_KUBELET_RESOURCE_RESERVE_MEM = "kubernetes.kubelet.resource.reserve.mem";
	public static final String KUBERNETES_KUBELET_HEARTBEAT_INTERVAL_MS = "kubernetes.kubelet.heartbeat.interval.ms";
	public static final String KUBERNETES_KUBELET_POD_HEARTBEAT_TIMEOUT_SECS = "kubernetes.kubelet.pod.heartbeat.timeout.secs";
	
	//pod
	public static final String KUBERNETES_POD_HEARTBEAT_INTERVAL_MS = "kubernetes.pod.heartbeat.interval.ms";
	public static final String KUBERNETES_POD_START_TIMEOUT_SECS = "kubernetes.pod.start.timeout.secs";
	
	//api server client
	public static final String KUBERNETES_APISERVER_CLIENT_TIMEOUT = "kubernetes.apiserver.client.timeout.secs";
	
	
	
	
}