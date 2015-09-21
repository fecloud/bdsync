package com.yuncore.bdsync.entity;

import org.json.JSONObject;

import com.yuncore.bdsync.entity.LocalFile;

public class CloudFile extends LocalFile {

	@Override
	public boolean formJOSN(JSONObject object) {
		if (null != object) {

			if (object.has("size")) {
				this.length = object.getLong("size");
			}
			if (object.has("isdir")) {
				this.isdir = object.getInt("isdir") == 1 ? true : false;
			}

			if (object.has("path")) {
				this.path = object.getString("path");
			}

			if (object.has("md5")) {
				md5 = object.getString("md5");
			}
			if (object.has("server_mtime")) {
				mtime = object.getInt("server_mtime");
			}
			this.newest = true;
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.entity.EntityJSONObject#formJOSN(java.lang.String)
	 */
	@Override
	public boolean formJOSN(String json) {
		return formJOSN(new JSONObject(json));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.entity.EntityJSONObject#toJSON()
	 */
	@Override
	public String toJSON() {
		return null;
	}

	/**
	 * 取文件的所在的路径
	 * 
	 * @param file
	 * @return
	 */
	public static String getUnixPath(String file) {
		if (null != file) {
			int lastIndexOf = file.lastIndexOf("/");
			if (lastIndexOf == 0) {
				return "/";
			} else {
				return file.substring(0, lastIndexOf);
			}
		}
		return null;
	}

	/**
	 * 取文件的所在的文件
	 * 
	 * @param file
	 * @return
	 */
	public static String getUnixFileName(String file) {
		if (null != file) {
			int lastIndexOf = file.lastIndexOf("/");
			return file.substring(lastIndexOf + 1);
		}
		return null;
	}

	public boolean formSuperJOSN(String json) {
		return super.formJOSN(new JSONObject(json));
	}

}
