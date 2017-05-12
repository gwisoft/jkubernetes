package org.gwisoft.jkubernetes.utils;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecCommandUtils {

	private static final Logger logger = LoggerFactory.getLogger(ExecCommandUtils.class);
	/**
	* @Title: execCommand 
	* @Description:  All exe command will go launchProcess
	* @param command
	* @throws IOException
	 */
	public static void execCommand(String command) throws IOException {
		launchProcess(command, new HashMap<String, String>(), false);
	}


	/**
	 * Attention
	 *
	 * All launchProcess should go this function
	 *
	 * it should use DefaultExecutor to start a process, but some little problem
	 * have been found, such as exitCode/output string so still use the old
	 * method to start process
	 *
	 * @param command
	 * @param environment
	 * @param backend
	 * @return outputString
	 * @throws IOException
	 */
	public static String launchProcess(final String command, final Map<String, String> environment, boolean backend) throws IOException {
        String[] cmds = command.split(" ");

        ArrayList<String> cmdList = new ArrayList<String>();
        for (String tok : cmds) {
            if (!StringUtils.isBlank(tok)) {
                cmdList.add(tok);
            }
        }

        return launchProcess(command, cmdList, environment, backend);
    }

	public static String launchProcess(final String command, final List<String> cmdlist,
			final Map<String, String> environment, boolean backend) throws IOException {
		if (backend) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					List<String> cmdWrapper = new ArrayList<String>();
					
					
					if(isWindows()){
						cmdWrapper.addAll(cmdlist);
					}else{
						cmdWrapper.add("nohup");
						cmdWrapper.addAll(cmdlist);
						cmdWrapper.add("&");
					}
					

					try {
						launchProcess(cmdWrapper, environment);
					} catch (IOException e) {
						logger.error("Failed to run nohup " + command + " &," + e.getCause(), e);
					}
				}
			}).start();
			return null;
		} else {
			try {
				Process process = launchProcess(cmdlist, environment);

				StringBuilder sb = new StringBuilder();
				String output = outputToString(process.getInputStream());
				String errorOutput = outputToString(process.getErrorStream());
				sb.append(output);
				sb.append("\n");
				sb.append(errorOutput);

				int ret = process.waitFor();
				if (ret != 0) {
					logger.warn(command + " is terminated abnormally. ret={}, str={}", ret, sb.toString());
				}
				return sb.toString();
			} catch (Throwable e) {
				logger.error("Failed to run " + command + ", " + e.getCause(), e);
			}

			return "";
		}
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
	
	/**
	* @Title: outputToString 
	* @Description: InputStream to String
	* @param in
	* @return
	 */
	public static String outputToString(InputStream input){
		BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
        	in = new BufferedReader(new InputStreamReader(input,isWindows()?"GB2312":"UTF-8"));
        	
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
	
	public static boolean isWindows(){
		String OS = System.getProperty("os.name").toLowerCase();  
		if(OS.startsWith("windows")){
			return true;
		}else{
			return false;
		}
	}
}
