package com.yuncore.bdsync.entity;

import org.json.JSONObject;

public class MkDirResult implements EntityJSON {

	private long fs_id;

	private String path;

	private long ctime;

	private long mtime;

	private int status;

	private int isdir;

	private String name;

	private long size;

	public long getFs_id() {
		return fs_id;
	}

	public void setFs_id(long fs_id) {
		this.fs_id = fs_id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime(long ctime) {
		this.ctime = ctime;
	}

	public long getMtime() {
		return mtime;
	}

	public void setMtime(long mtime) {
		this.mtime = mtime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getIsdir() {
		return isdir;
	}

	public void setIsdir(int isdir) {
		this.isdir = isdir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public boolean formJOSN(JSONObject object) {
		if (null != object) {
			if (object.has("fs_id")) {
				fs_id = object.getLong("fs_id");
			}
		}
		if (null != object) {
			if (object.has("path")) {
				path = object.getString("path");
			}
		}

		if (null != object) {
			if (object.has("ctime")) {
				ctime = object.getLong("ctime");
			}
		}
		if (null != object) {
			if (object.has("mtime")) {
				mtime = object.getLong("mtime");
			}
		}
		if (null != object) {
			if (object.has("status")) {
				status = object.getInt("status");
			}
		}
		if (null != object) {
			if (object.has("isdir")) {
				isdir = object.getInt("isdir");
			}
		}
		if (null != object) {
			if (object.has("name")) {
				name = object.getString("name");
			}
		}

		if (null != object) {
			if (object.has("size")) {
				size = object.getLong("size");
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "MkDirResult [fs_id=" + fs_id + ", path=" + path + ", ctime="
				+ ctime + ", mtime=" + mtime + ", status=" + status
				+ ", isdir=" + isdir + ", name=" + name + "]";
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

	@Override
	public void toJSON(JSONObject object) {

	}

}
