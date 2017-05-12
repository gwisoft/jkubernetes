package org.jkubernetes.core.kubectl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

public class TestApp {

	public static void main(String[] args) {
//		File file = new File("F:\\work\\eclipse-java-neon_work\\jkubernetes-all\\jkubernetes-core\\src\\test\\java\\org\\jkubernetes\\core\\kubectl\\test.yaml");
//		byte[] data;
//		try {
//			data = FileUtils.readFileToByteArray(file);
//			System.out.println(DigestUtils.md5Hex(data));
//			
//			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
//			byte[] data1 = byteBuffer.array();
//			
//			System.out.println(DigestUtils.md5Hex(data1));
//			YamlUtils.YamlToMap(data1);
//			System.out.println("successful");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		launch();

	}
	
	public static void launch(){
		
		String command = "java -server  -cp \\Users\\Lincm\\.m2\\repository\\io\\netty\\netty\\3.7.0.Final\\netty-3.7.0.Final.jar;C:C:\\Users\\Lincm\\.m2\\repository\\org\\apache\\curator\\curator-client\\2.12.0\\curator-client-2.12.0.jar;C:\\Users\\Lincm\\.m2\\repository\\com\\google\\guava\\guava\\16.0.1\\guava-16.0.1.jar;C:\\Users\\Lincm\\.m2\\repository\\org\\yaml\\snakeyaml\\1.11\\snakeyaml-1.11.jar;C:\\Users\\Lincm\\.m2\\repository\\org\\apache\\curator\\curator-framework\\2.12.0\\curator-framework-2.12.0.jar;C:\\Users\\Lincm\\.m2\\repository\\commons-lang\\commons-lang\\2.5\\commons-lang-2.5.jar;C:\\Users\\Lincm\\.m2\\repository\\org\\apache\\zookeeper\\zookeeper\\3.4.8\\zookeeper-3.4.8.jar;C:\\Users\\Lincm\\.m2\\repository\\org\\slf4j\\slf4j-api\\1.7.21\\slf4j-api-1.7.21.jar;C:\\Users\\Lincm\\.m2\\repository\\org\\apache\\httpcomponents\\httpcore\\4.4.1\\httpcore-4.4.1.jar:\\Users\\Lincm\\.m2\\repository\\ch\\qos\\logback\\logback-classic\\1.1.7\\logback-classic-1.1.7.jar;C:\\Users\\Lincm\\.m2\\repository\\log4j\\log4j\\1.2.16\\log4j-1.2.16.jar;C:\\Users\\Lincm\\git\\jkubernetes\\jkubernetes-all\\jkubernetes-core\\target\\classes;C:\\Users\\Lincm\\.m2\\repository\\ch\\qos\\logback\\logback-core\\1.1.7\\logback-core-1.1.7.jar;C:\\Users\\Lincm\\.m2\\repository\\commons-io\\commons-io\\2.4\\commons-io-2.4.jar;C:\\Users\\Lincm\\.m2\\repository\\com\\googlecode\\json-simple\\json-simple\\1.1\\json-simple-1.1.jar;C:\\Users\\Lincm\\.m2\\repository\\commons-codec\\commons-codec\\1.9\\commons-codec-1.9.jar;C:\\Users\\Lincm\\.m2\\repository\\org\\apache\\httpcomponents\\httpclient\\4.4.1\\httpclient-4.4.1.jar;C:\\Users\\Lincm\\.m2\\repository\\jline\\jline\\0.9.94\\jline-0.9.94.jar;C:\\Users\\Lincm\\.m2\\repository\\commons-logging\\commons-logging\\1.2\\commons-logging-1.2.jar;C:\\Users\\Lincm\\.m2\\repository\\org\\apache\\thrift\\libthrift\\0.10.0\\libthrift-0.10.0.jar;C: com.jkubernetes.daemon.pod.Pod test_app-1494411492 f3958b2f-b234-4afe-b3ab-02c5f88ba16b 1002 null";
		String[] cmds = command.split(" ");
		List<String> cmdList = new ArrayList<String>();
        for (String tok : cmds) {
            if (!StringUtils.isBlank(tok)) {
                cmdList.add(tok);
            }
        }
		Map<String, String> environment = new HashMap<>();
		environment.put("kubernetes.home", "./");

		try {
			Process process = launchProcess(cmdList, environment);

			StringBuilder sb = new StringBuilder();
			String output = getOutput(process.getInputStream());
			String errorOutput = getOutput(process.getErrorStream());
			sb.append(output);
			sb.append("\n");
			sb.append(errorOutput);

			int ret = process.waitFor();
			if (ret != 0) {
				System.out.println(command + " is terminated abnormally. ret={" + ret + "}, str={ " + sb.toString() + "}");
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static String getOutput(InputStream input) {
		BufferedReader in = null;
		StringBuilder sb = new StringBuilder();
        String line;
        try {
        	in = new BufferedReader(new InputStreamReader(input,"GB2312"));
            
            while ((line = in.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	if(in != null){
            		in.close();
            	}
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
	
	protected static java.lang.Process launchProcess(final List<String> cmdlist, final Map<String, String> environment)
			throws IOException {
		ProcessBuilder builder = new ProcessBuilder(cmdlist);
		builder.redirectErrorStream(true);
		Map<String, String> process_evn = builder.environment();
		for (Entry<String, String> entry : environment.entrySet()) {
			process_evn.put(entry.getKey(), entry.getValue());
		}

		return builder.start();
	}

}
