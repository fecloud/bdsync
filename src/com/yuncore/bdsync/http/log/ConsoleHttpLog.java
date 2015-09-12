/**
 * @(#) ConsoleHttpLog.java Created on 2015年9月9日
 *
 * 
 */
package com.yuncore.bdsync.http.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The class <code>ConsoleHttpLog</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class ConsoleHttpLog implements HttpLog {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.log.HttpLog#log(java.lang.String)
	 */
	@Override
	public void log(String message) {
		final String str = String.format("%s [%s] %s :%s%s", getTime(), Thread.currentThread().getName(),
				"ConsoleHttpLog", message, "\n");
		System.out.print(str);
	}

	private final static String getTime() {
		final SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss,SSS");
		return format.format(new Date());
	}

}
