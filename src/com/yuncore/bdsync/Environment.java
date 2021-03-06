/**
 * @(#) Environment.java Created on 2015-9-7
 *
 * 
 */
package com.yuncore.bdsync;

import java.io.File;

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

	/**
	 * 本地文件夹
	 */
	public static final String SYNCDIR = "bdsync.syncdir";
	
	/**
	 * 本地文件夹
	 */
	public static final String SYNC_TMPDIR = "bdsync.synctmpdir";
	
	/**
	 * 云端文件夹
	 */
	public static final String CLOUDCDIR = "bdsync.clouddir";

	public static final String SYNCTMPDIR = ".bdsync";

	public static final String TMP = "bdsync.tmpdir";

	public static final String LOCALLIST = "bdsync.locallist";

	public static final String CLOUDLIST = "bdsync.cloudlist";
	
	/**
	 * 上传限速
	 */
	public static final String UPLOAD_SPEED = "bdsync.uploadspeed";
	
	/**
	 * 上传完成是否删除文件
	 */
	public static final String UPLOAD_DELFILE = "bdsync.uploaddelfile";
	
	/**
	 * 下载限速
	 */
	public static final String DOWN_SPEED = "bdsync.downspeed";
	
	/**
	 * 机器标识
	 */
	public static final String NAME = "bdsync.name";
	
	/**
	 * 发送邮件的收件人
	 */
	public static final String MAILTO = "bdsync.mailto";
	
	/**
	 * 发送邮件的发件人
	 */
	public static final String MAILFROM = "bdsync.mailfrom";
	
	/**
	 * 发送邮件的发件人的密码
	 */
	public static final String MAILFROMPASS = "bdsync.mailfrompass";
	
	/**
	 * 下载线程的倍数cpu个数*
	 */
	public static final String DOWNTHREAD = "bdsync.downthread";
	
	/**
	 * 上传线程的倍数cpu个数*
	 */
	public static final String UPTHREAD = "bdsync.upthread";

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
	
	public static final String getCloudDir() {
		return System.getProperty(CLOUDCDIR, "/");
	}

	public static final void setCloudDir(String dir) {
		System.setProperty(CLOUDCDIR, dir);
	}
	
	public static final void setLocallist(String ms) {
		System.setProperty(LOCALLIST, ms);
	}

	public static final String getLocallist() {
		return System.getProperty(LOCALLIST, "0");
	}

	public static final void setCloudlist(String ms) {
		System.setProperty(CLOUDLIST, ms);
	}

	public static final String getCloudlist() {
		return System.getProperty(CLOUDLIST, "0");
	}

	public static final String getCommdLine() {
		return System.getProperty(CMD, "");
	}
	
	public static final String getDownSpeed() {
		return System.getProperty(DOWN_SPEED, "0");
	}
	
	public static final String getUploadSpeed() {
		return System.getProperty(UPLOAD_SPEED, "0");
	}
	
	public static final String getUploadDelFile() {
		return System.getProperty(UPLOAD_DELFILE, "false");
	}
	
	public static final String getName() {
		return System.getProperty(NAME, System.getProperty("user.name"));
	}
	
	public static final String getMailTo() {
		return System.getProperty(MAILTO);
	}
	public static final String getMailFrom() {
		return System.getProperty(MAILFROM);
	}
	
	public static final String getMailFromPass() {
		return System.getProperty(MAILFROMPASS);
	}
	
	public static final String getDownThread() {
		return System.getProperty(DOWNTHREAD,"1");
	}
	
	public static final String getUpThread() {
		return System.getProperty(UPTHREAD,"1");
	}
	
	public static final String getSyncTmpDir() {
		return System.getProperty(SYNC_TMPDIR, Environment.getSyncDir()
				+ File.separator + Environment.SYNCTMPDIR);
	}
	
	public static final void setSyncTmpDir(String dir) {
		System.setProperty(SYNC_TMPDIR, dir);
	}
}
