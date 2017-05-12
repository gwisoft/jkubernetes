package org.gwisoft.jkubernetes.kubectl.remote;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.gwisoft.jkubernetes.apiserver.thrift.ApiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ThriftClient {

	private static final Logger logger = LoggerFactory.getLogger(ThriftClient.class);
	
	private ApiServer.Client client;
	private TTransport transport;
	private TProtocol protocol;
	
	public ThriftClient(String host,int port,int timeout){
		  
        try {  
            transport = new TFramedTransport(new TSocket(host, port));  
            protocol = new TBinaryProtocol(transport);  
            client = new ApiServer.Client(protocol);  
            transport.open();  
        } catch (TTransportException e) {  
            logger.error("",e);
            throw new RuntimeException(e);
        } catch (TException e) {  
        	logger.error("",e);
        	throw new RuntimeException(e);
        }  
	}
	
	public synchronized void close() {
        if (transport != null) {
        	transport.close();
        	transport = null;
        	protocol = null;
        }
    }
	
	public ApiServer.Client getClient(){
		return client;
	}
}
