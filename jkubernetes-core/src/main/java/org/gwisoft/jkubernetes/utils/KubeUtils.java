package org.gwisoft.jkubernetes.utils;

import java.io.IOException;

import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubeUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(KubeUtils.class);

	public static void createPid(){
		String pidsDir;
		try {
			pidsDir = KubernetesConfig.getKubePidsDir();
			KubernetesUtils.createPid(pidsDir);
			logger.debug("successful create pid");
		} catch (IOException e) {
			logger.error("failed to create pid",e);
			throw new RuntimeException(e);
		}
		
	}
	
	
}
