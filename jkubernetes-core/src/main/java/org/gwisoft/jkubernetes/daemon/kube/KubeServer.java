package org.gwisoft.jkubernetes.daemon.kube;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.gwisoft.jkubernetes.apiserver.ServiceHandler;
import org.gwisoft.jkubernetes.apiserver.thrift.ApiServer;
import org.gwisoft.jkubernetes.apiserver.thrift.ApiServer.Iface;
import org.gwisoft.jkubernetes.cluster.KubernetesCluster;
import org.gwisoft.jkubernetes.cluster.KubernetesClusterCoordination;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.config.KubernetesConfigConstant;
import org.gwisoft.jkubernetes.config.KubernetesConfigLoad;
import org.gwisoft.jkubernetes.utils.DefaultUncaughtExceptionHandler;
import org.gwisoft.jkubernetes.utils.KubeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @ClassName: KubeServer
 * @author: Lincm
 * @Description: Kube progress start class
 * @date: 2017年3月28日 下午3:56:35
 *
 */
public class KubeServer {

	private static final Logger logger = LoggerFactory.getLogger(KubeServer.class);
	
	public static KubeData kubeData;
	
	public HttpServerViewLog httpServerViewLog; 
	
	private TServer thriftServer;
	
	/**
	* @Title: main 
	* @Description: main method
	* 1、set uncaught exception to main thread
	* 2、load Kubernetes config to memory
	* 3、start kube server
	* @param arg
	 */
	public static void main(String arg[]){
		
		//set uncaught exception to main thread
		Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
		
		//load Kubernetes config to memory
        KubernetesConfigLoad.initKubernetesConfig();
        
        //start kube server
        KubeServer instance = new KubeServer();
        instance.launchServer();
	}
	
	/**
	* @Title: launchServer 
	* @Description: start server
	* 1、verification run mode(must distributed mode)
	* 2、create pid file(A server can only start on)
	* 3、set shutdown hook 
	* 4、start sync zk thread
	* 5、init kube date
	* 6、start command server(http server)
	* 7、await kube Leadership is determined
	* 8、cleanup Corrupt Topologies
	* 9、start Topology assign thread
	* 10、start Topology state sync
	* 11、start clean Corrupt Topology jar
	* 12、start Monitor(Scan all task's heartbeat, if task isn't alive, DO KubeUtils.transition(monitor))
	* 13、start Thrift Server
	 */
	private void launchServer(){
		
		//verification run mode(must distributed mode)
		KubernetesConfig.validateDistributedMode();
		
		//create pid file(A server can only start on)
		KubeUtils.createPid();
		
		initKubeletHB();
		
		//set shutdown hook 
		initShutdownHook();
		
		//start sync zk thread
		initFollowerRunnable();
		
		//init kube date
		kubeData = new KubeData();
		
		//start command server(http server)
		httpServerViewLog = new HttpServerViewLog(KubernetesConfig.getHttpServerViewLogPort());
		httpServerViewLog.start();
		
		//await kube Leadership is determined
		while(!kubeData.isLeader()){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.debug("",e);
			}
		}
		
		//start Topology assign thread
		initTopologyAssignThread();
		
		//start Topology Monitor
		initTopologyMonitor();
		
		//start Thrift Server
		initThrift();
	}
	
	public void initKubeletHB(){
		KubernetesClusterCoordination coordination = KubernetesCluster.instanceCoordination();
		coordination.clearKubeletHeartbeats();
	}
	
	public void initThrift(){
		Integer thriftPort = (Integer)KubernetesConfigLoad.getKubernetesConfig()
				.get(KubernetesConfigConstant.KUBERNETES_APISERVER_PORT);
		try {
			TNonblockingServerSocket nbServerSocket = new TNonblockingServerSocket(thriftPort);
			Integer maxReadBufSize = (Integer)KubernetesConfigLoad.getKubernetesConfig()
					.get(KubernetesConfigConstant.KUBERNETES_APISERVER_MAXBUFFERSIZE);
			
			THsHaServer.Args args = new THsHaServer.Args(nbServerSocket);
			
	        args.protocolFactory(new TBinaryProtocol.Factory(false, true, maxReadBufSize, -1));

	        ServiceHandler serviceHandler = new ServiceHandler(kubeData);
	        args.processor(new ApiServer.Processor<Iface>(serviceHandler));
	        args.transportFactory(new TFramedTransport.Factory());
	        args.maxReadBufferBytes = maxReadBufSize;

	        thriftServer = new THsHaServer(args);

	        logger.info("*****************Successfully started kube: started Thrift server...*******************");
	        thriftServer.serve();
		} catch (TTransportException e) {
			logger.error("",e);
		}
	}
	
	public void initTopologyMonitor(){
		ScheduledExecutorService execSer = Executors.newScheduledThreadPool(1);
		TopologyMonitorRunnable monitorRun = new TopologyMonitorRunnable();
		
		Integer period = (Integer)KubernetesConfigLoad.getKubernetesConfig().get(
				KubernetesConfigConstant.KUBERNETES_KUBE_MONITOR_TOPOLOGY_PERIOD_SECS);
		execSer.scheduleAtFixedRate(monitorRun, 0, period, TimeUnit.SECONDS);
		logger.info("************TopologyMonitorRunnable thread has bean started*****************");
	}
	
	public void initTopologyAssignThread(){
		TopologyAssignRunnable runnable = new TopologyAssignRunnable();
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.setName("Topology_Assign");
		thread.start();
	}
	
	public void initShutdownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				KubeServer.this.cleanup();
			}
		});
	}
	
	public void initFollowerRunnable(){
		FollowerRunnable followerRunnable = new FollowerRunnable();
		Thread thread = new Thread(followerRunnable);
		thread.start();
		logger.info("***************Successfully init Follower thread******************");
	}
	
	public void cleanup(){
		logger.debug("shutdownhook cleanup!");
	}
}
