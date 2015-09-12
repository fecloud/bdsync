package com.yuncore.bdsync.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.yuncore.bdsync.Environment;

public final class Log {

	private FileWriter writer;

	private int priority;

	private static final String line_separator = System.getProperty(
			"line.separator", "\n");

	private Log() {
		try {
			writer = new FileWriter(new File(Environment.getLogFile()), true);
			priority = getPriority(Environment.getLogPriority());
		} catch (Exception e) {
		}
	}

	private static final Log log = new Log();

	/**
	 * Priority constant for the println method; use Log.v.
	 */
	public static final int VERBOSE = 2;

	/**
	 * Priority constant for the println method; use Log.d.
	 */
	public static final int DEBUG = 3;

	/**
	 * Priority constant for the println method; use Log.i.
	 */
	public static final int INFO = 4;

	/**
	 * Priority constant for the println method; use Log.w.
	 */
	public static final int WARN = 5;

	/**
	 * Priority constant for the println method; use Log.e.
	 */
	public static final int ERROR = 6;

	/**
	 * Priority constant for the println method.
	 */
	public static final int ASSERT = 7;

	public static int v(String tag, String msg) {
		return println_native(VERBOSE, tag, msg);
	}

	public static int v(String tag, String msg, Throwable tr) {
		return println_native(VERBOSE, tag, msg + line_separator
				+ getStackTraceString(tr));
	}

	public static int d(String tag, String msg) {
		return println_native(DEBUG, tag, msg);
	}

	public static int d(String tag, String msg, Throwable tr) {
		return println_native(DEBUG, tag, msg + line_separator
				+ getStackTraceString(tr));
	}

	public static int i(String tag, String msg) {
		return println_native(INFO, tag, msg);
	}

	public static int i(String tag, String msg, Throwable tr) {
		return println_native(INFO, tag, msg + line_separator
				+ getStackTraceString(tr));
	}

	public static int w(String tag, String msg) {
		return println_native(WARN, tag, msg);
	}

	public static int w(String tag, String msg, Throwable tr) {
		return println_native(WARN, tag, msg + line_separator
				+ getStackTraceString(tr));
	}

	public static int w(String tag, Throwable tr) {
		return println_native(WARN, tag, getStackTraceString(tr));
	}

	public static int e(String tag, String msg) {
		return println_native(ERROR, tag, msg);
	}

	public static int e(String tag, String msg, Throwable tr) {
		return println_native(ERROR, tag, msg + line_separator
				+ getStackTraceString(tr));
	}

	/**
	 * Handy function to get a loggable stack trace from a Throwable
	 * 
	 * @param tr
	 *            An exception to log
	 */
	public static String getStackTraceString(Throwable tr) {
		if (tr == null) {
			return "";
		}

		Throwable t = tr;
		while (t != null) {
			if (t instanceof UnknownHostException) {
				return "";
			}
			t = t.getCause();
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		return sw.toString();
	}

	private static int getPriority(String priority) {
		if (priority != null && priority.trim().length() > 0) {
			if ("VERBOSE".equalsIgnoreCase(priority)) {
				return VERBOSE;
			}
			if ("DEBUG".equalsIgnoreCase(priority)) {
				return DEBUG;
			}
			if ("INFO".equalsIgnoreCase(priority)) {
				return INFO;
			}
			if ("WARN".equalsIgnoreCase(priority)) {
				return WARN;
			}
			if ("ERROR".equalsIgnoreCase(priority)) {
				return ERROR;
			}

		}
		return 1;
	}

	private static String getPriority(int priority) {
		if (priority <= ERROR && priority >= VERBOSE) {
			switch (priority) {
			case VERBOSE:
				return "VERBOSE";
			case DEBUG:
				return "DEBUG";
			case INFO:
				return "INFO";
			case WARN:
				return "WARN";
			case ERROR:
				return "ERROR";
			}

		}
		return "";
	}

	private final static String getTime() {
		final SimpleDateFormat format = new SimpleDateFormat(
				"MM-dd HH:mm:ss,SSS");
		return format.format(new Date());
	}

	private final static synchronized int println_native(int priority,
			String tag, String msg) {
		if (log.priority <= priority) {
			final String str = String.format("%s [%s] [%s] %s :%s%s",
					getTime(), getPriority(priority), Thread.currentThread()
							.getName(), tag, msg, line_separator);
			try {
				System.out.print(str);
				log.writer.write(str);
				log.writer.flush();
			} catch (Exception e) {
			}
		}
		return 1;
	}

}
