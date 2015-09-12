package com.yuncore.bdsync.app;

import com.yuncore.bdsync.exception.BDSyncException;

public interface Context {

	String getProperty(String key);

	String getProperty(String key, String defaultValue);

	Object setProperty(String key, String value);

	boolean load() throws BDSyncException;

}
