package com.yuncore.bdsync.entity;

import org.json.JSONObject;

import com.yuncore.bdsync.util.MD5;

public class LocalFile implements EntityJSON {

	protected int id;

	protected String path;

	protected long length;

	protected String fId;

	protected String md5;

	protected boolean newest;

	protected int mtime;

	/**
	 * 0文件 1文件夹
	 */
	protected boolean isdir;

	public LocalFile() {

	}

	public LocalFile(String path) {
		this.path = path;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public boolean isDir() {
		return isdir;
	}

	public void setDir(boolean isdir) {
		this.isdir = isdir;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	/**
	 * @return the newest
	 */
	public boolean isNewest() {
		return newest;
	}

	/**
	 * @param newest
	 *            the newest to set
	 */
	public void setNewest(boolean newest) {
		this.newest = newest;
	}

	public String getAbsolutePath() {
		return path;
	}

	public String getfId() {
		return fId;
	}

	public void setfId(String fId) {
		this.fId = fId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isDirectory() {
		return isdir;
	}

	public boolean isFile() {
		return !isdir;
	}

	public String toFid() {
		final String str = path + length + (isdir ? "1" : "0");
		this.fId = MD5.md5(str);
		return fId;
	}

	public int getMtime() {
		return mtime;
	}

	public void setMtime(int mtime) {
		this.mtime = mtime;
	}

	public String getParentPath() {
		if (isDir()) {
			return path.substring(0, path.lastIndexOf("/"));
		} else {
			String tmpPath = path;
			if (path.endsWith("/")) {
				tmpPath = path.substring(0, path.length() - 1);
			}
			tmpPath = tmpPath.substring(0, tmpPath.lastIndexOf("/"));
			if (tmpPath.length() == 0) {
				return "/";
			}
			return tmpPath;
		}
	}

	public String getName() {
		if (isDir()) {
			return path.substring(path.lastIndexOf("/"));
		} else {
			String tmpPath = path;
			if (path.endsWith("/")) {
				tmpPath = path.substring(0, path.length() - 1);
			}
			return tmpPath.substring(tmpPath.lastIndexOf("/") + 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.entity.EntityJSONObject#formJOSN(java.lang.String)
	 */
	@Override
	public boolean formJOSN(String json) {
		return formJOSN(new JSONObject(json));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.entity.EntityJSONObject#formJOSN(org.json.JSONObject)
	 */
	@Override
	public boolean formJOSN(JSONObject object) {
		if (null != object) {
			if (object.has("path")) {
				this.path = object.getString("path");
			}
			if (object.has("length")) {
				this.length = object.getLong("length");
			}
			if (object.has("isdir")) {
				this.isdir = object.getBoolean("isdir");
			}
			if (object.has("mtime")) {
				this.mtime = object.getInt("mtime");
			}
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LocalFile [id=" + id + ", path=" + path + ", length=" + length
				+ ", fId=" + fId + ", md5=" + md5 + ", newest=" + newest
				+ ", mtime=" + mtime + ", isdir=" + isdir + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.entity.EntityJSONObject#toJSON()
	 */
	@Override
	public String toJSON() {
		final JSONObject object = new JSONObject();
		toJSON(object);
		return object.toString();
	}

	@Override
	public void toJSON(JSONObject object) {
		object.put("id", id);
		object.put("path", path);
		object.put("length", length);
		object.put("isdir", isdir);
		object.put("fId", fId);
		object.put("newest", newest);
		object.put("mtime", mtime);
	}

}
