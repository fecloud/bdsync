/**
 * @(#) Environment.java Created on 2015-9-7
 *
 * 
 */
package com.yuncore.bdsync;

/**
 * The class <code>Environment</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public final class Environment {

	/**
	 * 数据库文件路径
	 */
	public static final String DB_FILE = "bdsync.db";
	
	/**
	 * cookie文件路径
	 */
	public static final String COOKIE_FILE = "bdsync.cookie";

	/**
	 * 日志文件路径
	 */
	public static final String LOG_FILE = "bdsync.log.file";

	/**
	 * 日志等级
	 */
	public static final String LOG_PRIORITY = "bdsync.log.priority";

	public static final String SYNCDIR = "bdsync.syncdir";

	public static final String SYNCTMPDIR = "bdsync.synctmpcdir";

	public static final String TMP = "bdsync.tmpdir";

	public static final String CONTEXT = "bdsync.context";

	public static final String COOKIECONTAINER = "bdsync.cookiecontainer";

	public static final String LOCALLIST_SESSION = "bdsync.locallist_session";

	public static final String CLOUDLIST_SESSION = "bdsync.cloudlist_session";

	/**
	 * 程序启动时间
	 */
	public static final String BOOT_TIME = "start.runtime";
	/**
	 * 程序的命令行
	 */
	public static final String CMD = "sun.java.command";

	static {
		System.getProperty(BOOT_TIME, "" + System.currentTimeMillis());
	}

	public static String getProperty(String key) {
		return System.getProperty(key);
	}

	public static String getProperty(String key, String def) {
		return System.getProperty(key, def);
	}

	public static void setProperty(String key, String value) {
		System.setProperty(key, value);
	}

	public static final String getDBFile() {
		return System.getProperty(DB_FILE, "bdsync.db");
	}
	
	public static final String getCookieFile(){
		return System.getProperty(COOKIE_FILE,"resource/cookie.json");
	}

	public static final String getLogFile() {
		return System.getProperty(LOG_FILE, "bdsync.log");
	}

	public static final String getLogPriority() {
		return System.getProperty(LOG_PRIORITY, "VERBOSE");
	}

	public static final String getJavaTmpDir() {
		return System.getProperty("java.io.tmpdir", "");
	}

	public static final String getSyncDir() {
		return System.getProperty(SYNCDIR, null);
	}

	public static final void setSyncDir(String dir) {
		System.setProperty(SYNCDIR, dir);
	}

	public static final String getSyncTmpDir() {
		return System.getProperty(SYNCTMPDIR, null);
	}

	public static final void setSyncTmpDir(String dir) {
		System.setProperty(SYNCTMPDIR, dir);
	}

	public static final String getContextClassName() {
		return System.getProperty(CONTEXT, null);
	}

	public static final void setContextClassName(String className) {
		System.setProperty(CONTEXT, className);
	}

	public static final void setCookiecontainerClassName(String className) {
		System.setProperty(COOKIECONTAINER, className);
	}

	public static final String getCookiecontainerClassName() {
		return System.getProperty(COOKIECONTAINER, null);
	}

	public static final void setLocallistSession(String session) {
		System.setProperty(LOCALLIST_SESSION, session);
	}

	public static final String getLocallistSession() {
		return System.getProperty(LOCALLIST_SESSION, "0");
	}

	public static final void setCloudlistSession(String session) {
		System.setProperty(CLOUDLIST_SESSION, session);
	}

	public static final String getCloudlistSession() {
		return System.getProperty(CLOUDLIST_SESSION, "0");
	}

	public static final String getCommdLine() {
		return System.getProperty(CMD, "");
	}
}
