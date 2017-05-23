package org.gwisoft.jkubernetes.kubectl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubectlGetTest {
	
	private static final String KUBERNETES_APISERVER_ADDRESS = "kubernetes.apiserver.address";
	
	private static final Logger logger = LoggerFactory.getLogger(KubectlGetTest.class);

	public static void main(String[] args){
		args = setTestParam(args);
		KubectlGet.main(args);
	}

	public static String[] setTestParam(String[] args){
		System.setProperty(KUBERNETES_APISERVER_ADDRESS, "");
		if(args == null || args.length < 1){
			args = new String[1];
			args[0] = "po";
			//args[1] = "test_app_1";
		}
		return args;
	}
}
