package org.gwisoft.jkubernetes.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

public class ProcessPidUtils {

	public static Integer getPidByProcess(Process process){
		Integer pid = null;
		try{
			pid = getPidByUNIXProcess(process);
		}catch(Throwable e){
		}
		
		if(pid != null){
			return pid;
		}
		
		try{
			pid = getPidByWin32Process(process);
		}catch(Throwable e){
		}
		
		if(pid != null){
			return pid;
		}
		
		try{
			pid = getPidByRuntimeMXBean();
		}catch(Throwable e){
		}
		
		if(pid != null){
			return pid;
		}
		
		throw new RuntimeException("not get pid");
		
	}
	private static Integer getPidByRuntimeMXBean() {
		RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
		String processName = rtb.getName();
		Integer pid = null;
		Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(processName);
		if (matcher.matches()) {
			pid = new Integer(Integer.parseInt(matcher.group(1)));
		}
		return pid;
	}

	private static Integer getPidByUNIXProcess(Process process) {
		Integer pid = null;
		if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
			/* get the PID on unix/linux systems */
			try {
				Field f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getInt(process);
			} catch (Throwable e) {
			}
		}
		return pid;
	}

	private static Integer getPidByWin32Process(Process process) {
		Integer pid = null;
		if (process.getClass().getName().equals("java.lang.Win32Process")
				|| process.getClass().getName().equals("java.lang.ProcessImpl")) {
			/* determine the pid on windows plattforms */
			try {
				Field f = process.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				long handl = f.getLong(process);

				Kernel32 kernel = Kernel32.INSTANCE;
				W32API.HANDLE handle = new W32API.HANDLE();
				handle.setPointer(Pointer.createConstant(handl));
				pid = kernel.GetProcessId(handle);
			} catch (Throwable e) {
			}
		}
		
		return pid;
	}

	public interface Kernel32 extends W32API {
		Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, DEFAULT_OPTIONS);

		/* http://msdn.microsoft.com/en-us/library/ms683179(VS.85).aspx */
		HANDLE GetCurrentProcess();

		/* http://msdn.microsoft.com/en-us/library/ms683215.aspx */
		int GetProcessId(HANDLE Process);
	}

	/**
	 * Base type for most W32 API libraries. Provides standard options for
	 * unicode/ASCII mappings. Set the system property w32.ascii to true to
	 * default to the ASCII mappings.
	 */
	public interface W32API extends StdCallLibrary, W32Errors {

		/** Standard options to use the unicode version of a w32 API. */
		Map UNICODE_OPTIONS = new HashMap() {
			{
				put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
				put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
			}
		};

		/** Standard options to use the ASCII/MBCS version of a w32 API. */
		Map ASCII_OPTIONS = new HashMap() {
			{
				put(OPTION_TYPE_MAPPER, W32APITypeMapper.ASCII);
				put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.ASCII);
			}
		};

		Map DEFAULT_OPTIONS = Boolean.getBoolean("w32.ascii") ? ASCII_OPTIONS : UNICODE_OPTIONS;

		public class HANDLE extends PointerType {
			@Override
			public Object fromNative(Object nativeValue, FromNativeContext context) {
				Object o = super.fromNative(nativeValue, context);
				if (INVALID_HANDLE_VALUE.equals(o))
					return INVALID_HANDLE_VALUE;
				return o;
			}
		}

		/** Constant value representing an invalid HANDLE. */
		HANDLE INVALID_HANDLE_VALUE = new HANDLE() {
			{
				super.setPointer(Pointer.createConstant(-1));
			}

			@Override
			public void setPointer(Pointer p) {
				throw new UnsupportedOperationException("Immutable reference");
			}
		};
	}

	public interface W32Errors {

		int NO_ERROR = 0;
		int ERROR_INVALID_FUNCTION = 1;
		int ERROR_FILE_NOT_FOUND = 2;
		int ERROR_PATH_NOT_FOUND = 3;

	}

}
