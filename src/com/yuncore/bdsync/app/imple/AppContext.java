package com.yuncore.bdsync.app.imple;

import java.util.Properties;

import com.yuncore.bdsync.app.Context;

public abstract class AppContext implements Context {

	protected Properties properties = new Properties();

	/**
	 * 10分针刷新一次
	 */
	protected static int interval = 10 * 60 * 1000;

	protected volatile long time;

	@Override
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	@Override
	public Object setProperty(String key, String value) {
		return properties.setProperty(key, value);
	}

}
