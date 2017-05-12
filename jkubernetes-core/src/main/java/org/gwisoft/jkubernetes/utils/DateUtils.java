package org.gwisoft.jkubernetes.utils;

public class DateUtils {

	public static int getCurrentTimeSecs(){
		return (int)(System.currentTimeMillis() / 1000);
	}
}
