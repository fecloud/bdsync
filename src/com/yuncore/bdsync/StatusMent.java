package com.yuncore.bdsync;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONObject;

import com.yuncore.bdsync.entity.EntityJSON;

public class StatusMent {

	/**
	 * 正在操作的文件
	 */
	public static final String DOFILE = "dofile";

	/**
	 * 正在操作的文件已完成大小
	 */
	public static final String DOFILE_SIZE = "dofile_size";

	/**
	 * 同步服务正在进行的任务
	 */
	public static final String SYNCWORKING = "syncworking";

	/**
	 * 同步服务正在进行的任务code
	 */
	public static final String SYNCWORKINGCODE = "syncworkingcode";
	
	private static final Hashtable<String, Object> env = new Hashtable<String, Object>();

	public static final void setProperty(String key, Object value) {
		env.put(key, value);
	}
	
	public static final void removeProperty(String key) {
		if (env.containsKey(key)) {
			env.remove(key);
		}
	}

	public static final Object getProperty(String key, Object defaltValue) {
		if (env.containsKey(key)) {
			return env.get(key);
		}
		return defaltValue;
	}

	public static final JSONObject listJson() {
		final Enumeration<String> keys = env.keys();
		final JSONObject jsonObject = new JSONObject();
		String key = null;
		Object value = null;
		JSONObject item = null;
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			value = env.get(key);
			if (value instanceof EntityJSON) {
				item = new JSONObject();
				((EntityJSON) value).toJSON(item);
				jsonObject.put(key, item);
			} else {
				jsonObject.put(key, value);
			}
		}
		return jsonObject;
	}

}
