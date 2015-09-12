package com.yuncore.bdsync.entity;

import org.json.JSONObject;

public class CloudRmResult implements EntityJSON {

	private int errno;
	
	private long taskid;

	public long getTaskid() {
		return taskid;
	}

	public void setTaskid(long taskid) {
		this.taskid = taskid;
	}

	@Override
	public boolean formJOSN(JSONObject object) {
		if (null != object) {
			if (object.has("errno")) {
				errno = object.getInt("errno");
			}
			if (object.has("taskid")) {
				taskid = object.getLong("taskid");
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "PCSRmResult [taskid=" + taskid + "]";
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

	public int getErrno() {
		return errno;
	}

	public void setErrno(int errno) {
		this.errno = errno;
	}

}
