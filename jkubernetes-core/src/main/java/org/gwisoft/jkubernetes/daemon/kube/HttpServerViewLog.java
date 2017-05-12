package org.gwisoft.jkubernetes.daemon.kube;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpServerViewLog {

	private static final Logger logger = LoggerFactory.getLogger(HttpServerViewLog.class);
	private HttpServer httpServer;
	private int port;
	private static final String HTTP_SERVER_VIEW_LOG_ROOT = "/viewlog";
	public static final String HTTPSERVER_LOGVIEW_PARAM_CMD = "cmd";
	
	public static final String HTTPSERVER_LOGVIEW_PARAM_CMD_JSTACK = "jstack";
	
	public HttpServerViewLog(int port){
		this.port = port;
	}
	
	class LogHandler implements HttpHandler{

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			URI uri = exchange.getRequestURI();
			Map map = parseRawQuery(uri.getRawQuery());
			String cmd = (String)map.get(HTTPSERVER_LOGVIEW_PARAM_CMD);
			if(cmd != null || cmd.isEmpty()){
				handlFailure(exchange,"Bad Request, Not set command type");
				return;
			}
			
			if(cmd.equals(HTTPSERVER_LOGVIEW_PARAM_CMD_JSTACK)){
				//TODO
			}else{
				handlFailure(exchange, "Bad Request,Not Support cmd type:" + cmd);
			}
		}
		
		public void handlFailure(HttpExchange exchange,String error){
			byte[] data = error.getBytes();
			OutputStream out = null;
			try {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, data.length);
				out = exchange.getResponseBody();
				out.write(data);
			} catch (IOException e) {
				logger.error("",e);
				throw new RuntimeException(e);
			}finally{
				if(out != null){
					try {
						out.close();
					} catch (IOException e) {
						logger.warn("",e);
					}
				}
			}
			
		}
		
		public Map parseRawQuery(String rawQuery){
			Map map = new HashMap<String,String>();
			if(rawQuery == null || rawQuery.isEmpty()){
				return map;
			}
			
			String[] keys = rawQuery.split("&");
			for(String key:keys){
				String[] cmds = key.split("=");
				if(cmds.length == 2){
					map.put(cmds[0], cmds[1]);
				}
			}
			return map;
		}
		
		public void HandleJstack(HttpExchange exchange,Map map){
			
		}
		
	}
	
	public void start(){
		int nThreads = 3;
		Executor ex = Executors.newFixedThreadPool(nThreads);
		InetSocketAddress ip = new InetSocketAddress(port);
		
		try {
			httpServer = HttpServer.create(ip, 0);
			httpServer.createContext(HTTP_SERVER_VIEW_LOG_ROOT,new LogHandler());
			httpServer.setExecutor(ex);
			httpServer.start();
		} catch(BindException e){
			logger.error("HttpServerViewLog is Already!",e);
			return;
		} catch (IOException e) {
			logger.error("HttpServer start failed!",e);
			return;
		}
		
	}
}
