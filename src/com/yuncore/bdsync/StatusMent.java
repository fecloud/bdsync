package com.yuncore.bdsync;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONObject;

import com.yuncore.bdsync.entity.EntityJSON;

public class StatusMent {

	/**
	 * 正在上传的文件
	 */
	public static final String key_uploading = "uploading";
	
	/**
	 * 正在上传的文件已上传大小
	 */
	public static final String key_upload_size = "upload_size";
	
	/**
	 * 正在下载的文件
	 */
	public static final String key_downloading = "downloading";
	
	/**
	 * 正在下载的文件已上传大小
	 */
	public static final String key_download_size = "download_size";
	
	private static final Hashtable<String, Object> env = new Hashtable<String, Object>();

	public static final void setProperty(String key, Object value) {
		env.put(key, value);
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
