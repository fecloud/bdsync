package com.yuncore.bdsync;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONObject;

import com.yuncore.bdsync.entity.EntityJSON;

public class StatusMent {

	/**
	 * 正在上传的文件
	 */
	public static final String UPLOADING = "uploading";

	/**
	 * 正在上传的文件已上传大小
	 */
	public static final String UPLOAD_SIZE = "upload_size";

	/**
	 * 正在下载的文件
	 */
	public static final String DOWNLOADING = "downloading";

	/**
	 * 正在下载的文件已上传大小
	 */
	public static final String DOWNLOAD_SIZE = "download_size";

	/**
	 * 同步服务正在进行的任务
	 */
	public static final String SYNCWORKING = "syncworking";

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
