package org.gwisoft.jkubernetes.docker;

public class KubernetesDockerFactory {

	private static KubernetesDocker kubernetesDocker;

	public static KubernetesDocker getInstance(){
//		if (kubernetesDocker == null){
//			synchronized(KubernetesDocker.class){
//				if(kubernetesDocker == null){
//					kubernetesDocker = new KubernetesDockerJava();
//				}
//			}
//			
//		}
		return new KubernetesDockerJava();
	}
}
