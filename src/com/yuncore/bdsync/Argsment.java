/**
 * @(#) Argsment.java Created on 2015年9月8日
 *
 * 
 */
package com.yuncore.bdsync;

import com.yuncore.bdsync.dao.SyncStatusDao;

/**
 * The class <code>Argsment</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public final class Argsment {

	/**
	 * 同步间隔时间
	 */
	public static final String INTERVAL = "bdsync.interval";

	/**
	 * 是否允许下载
	 */
	public static final String ALLOW = "bdsync.allow";

	private static volatile boolean get_interval_from_db = false;

	public static String getProperty(String key) {
		return System.getProperty(key);
	}

	public static String getProperty(String key, String def) {
		return System.getProperty(key, def);
	}

	public static void setProperty(String key, String value) {
		System.setProperty(key, value);
	}

	public static final long getBDSyncInterval() {
		return Long.parseLong(System.getProperty(INTERVAL, "60000"));
	}

	public static final void getBDSyncInterval(long interval) {
		System.setProperty(INTERVAL, "" + interval);
	}

	public static final boolean getBDSyncAllow() {
		if (!get_interval_from_db) {
			System.setProperty(ALLOW, new SyncStatusDao().getSyncStatus());
			get_interval_from_db = true;
		}
		return !System.getProperty(ALLOW, "0").equals("0");
	}

	public static final void setBDSyncAllow(String allow) {
		new SyncStatusDao().setSyncStatus(allow);
		System.setProperty(ALLOW, allow);
	}

}
