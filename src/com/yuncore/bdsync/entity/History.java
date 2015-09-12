package com.yuncore.bdsync.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class History implements EntityJSON {

	private long id;

	private Date time;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void setTime(long time) {
		this.time = new Date(time);
	}

	@Override
	public boolean formJOSN(String json) {
		return false;
	}

	@Override
	public boolean formJOSN(JSONObject object) {
		return false;
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	public void toJSON(JSONObject object) {
	}

	public JSONObject toJSONObject() {
		final JSONObject object = new JSONObject();
		object.put("id", id);
		object.put("time", formatTime(time.getTime()));
		return object;
	}

	private final static String formatTime(long time) {
		final SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(time));
	}

}
