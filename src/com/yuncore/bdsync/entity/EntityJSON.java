package com.yuncore.bdsync.entity;

import org.json.JSONObject;

public interface EntityJSON {

	/**
	 * 从json解析过来
	 * @param json
	 * @return
	 */
	boolean formJOSN(String json);
	
	/**
	 * 从json解析过来
	 * @param json
	 * @return
	 */
	boolean formJOSN(JSONObject object);
	
	/**
	 * 格式化成json
	 * @return
	 */
	String toJSON();
	
	/**
	 * 添加到json中
	 * @param object
	 */
	void toJSON(JSONObject object);
	
}
