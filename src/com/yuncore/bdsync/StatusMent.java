package com.yuncore.bdsync;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.yuncore.bdsync.entity.DoingFile;
import com.yuncore.bdsync.entity.EntityJSON;

public class StatusMent {

	/**
	 * 正在操作的文件
	 */
	private static final String DOFILES = "dofiles";

	/**
	 * 同步服务正在进行的任务
	 */
	public static final String SYNCWORKING = "syncworking";

	/**
	 * 同步服务正在进行的任务code
	 */
	public static final String SYNCWORKINGCODE = "syncworkingcode";
	
	private static final Hashtable<String, Object> env = new Hashtable<String, Object>();

	private static final Hashtable<String, DoingFile>  doingFile = new Hashtable<String, DoingFile>();
	
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
		
		if(!doingFile.isEmpty()){
			JSONArray array = new JSONArray();
			for (String keyName : doingFile.keySet()) {
				final JSONObject once = new JSONObject();
				doingFile.get(keyName).toJSON(once);
				array.put(once);
			}
			jsonObject.put(DOFILES, array);
		}
		
		return jsonObject;
	}

	public static Hashtable<String, DoingFile> getDoingfile() {
		return doingFile;
	}

}
