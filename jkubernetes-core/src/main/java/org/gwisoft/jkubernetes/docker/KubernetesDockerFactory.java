package org.gwisoft.jkubernetes.docker;

public class KubernetesDockerFactory {
	
	private static ThreadLocal<KubernetesDocker> kubernetesDockers = new ThreadLocal<>();

	public static KubernetesDocker getInstance(){
//		KubernetesDocker kubernetesDocker = kubernetesDockers.get();
//		if (kubernetesDocker == null){
//			synchronized(KubernetesDocker.class){
//				if(kubernetesDocker == null){
//					kubernetesDocker = new KubernetesDockerJava();
//					kubernetesDockers.set(kubernetesDocker);
//				}
//			}
//			
//		}
		//TODO 线程不完全问题
		return new KubernetesDockerJava();
	}
}
