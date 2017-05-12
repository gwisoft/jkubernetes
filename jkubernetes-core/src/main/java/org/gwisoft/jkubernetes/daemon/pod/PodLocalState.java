package org.gwisoft.jkubernetes.daemon.pod;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gwisoft.jkubernetes.config.KubernetesConfig;
import org.gwisoft.jkubernetes.utils.SerializeUtils;

public class PodLocalState {

	public static void setPodHeartbeat(PodHeartbeat podHeartbeat){
		byte[] data = SerializeUtils.javaSerialize(podHeartbeat);
		try {
			FileUtils.writeByteArrayToFile(
					new File(KubernetesConfig.getPodHeartbeatsFilePath(podHeartbeat.getPodId())), data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
