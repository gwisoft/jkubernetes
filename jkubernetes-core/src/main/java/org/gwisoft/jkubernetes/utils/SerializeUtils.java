package org.gwisoft.jkubernetes.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializeUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(SerializeUtils.class);

	public static byte[] javaSerialize(Object obj){
		ByteArrayOutputStream outputStream = null;
		ObjectOutputStream oos = null;
		try {
			outputStream = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(outputStream);
			oos.writeObject(obj);
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					logger.warn("",e);
				}
			}
			
			if(outputStream != null){
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.warn("",e);
				}
			}
			
		}
		
	}
	
	public static Object javaDeserialize(byte[] buf){
		ByteArrayInputStream inputStream = null;
		ObjectInputStream ois = null;
		try {
			inputStream = new ByteArrayInputStream(buf);
			ois = new ObjectInputStream(inputStream);
			return ois.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally {
			if(ois != null){
				try {
					ois.close();
				} catch (IOException e) {
					logger.warn("",e);
				}
			}
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.warn("",e);
				}
			}
		}
		
	}
}
