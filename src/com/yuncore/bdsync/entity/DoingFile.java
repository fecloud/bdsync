package com.yuncore.bdsync.entity;

import org.json.JSONObject;

public class DoingFile implements EntityJSON{

	private LocalFile file;
	
	private long doingSize;
	
	public DoingFile(LocalFile file) {
		super();
		this.file = file;
	}

	@Override
	public boolean formJOSN(String json) {
		return false;
	}

	@Override
	public boolean formJOSN(JSONObject object) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toJSON() {
		return null;
	}

	@Override
	public void toJSON(JSONObject object) {
		if(null !=file){
			file.toJSON(object);
		}
		object.put("doingSize", doingSize);
	}

	public LocalFile getFile() {
		return file;
	}

	public DoingFile setFile(LocalFile file) {
		this.file = file;
		return this;
	}

	public long getDoingSize() {
		return doingSize;
	}

	public DoingFile setDoingSize(long doingSize) {
		this.doingSize = doingSize;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DoingFile){
			final DoingFile other = (DoingFile) obj;
			return file.toString().equals(other.file.toString());
		}
		return super.equals(obj);
	}
}
