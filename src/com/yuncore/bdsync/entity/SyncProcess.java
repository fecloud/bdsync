/**
 * @(#) Process.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.entity;

import org.json.JSONObject;

/**
 * The class <code>SyncProcess</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class SyncProcess implements EntityJSON {

	private String process;

	private String name;

	public SyncProcess() {

	}

	/**
	 * @param process
	 * @param name
	 */
	public SyncProcess(String process, String name) {
		super();
		this.process = process;
		this.name = name;
	}

	/**
	 * @return the process
	 */
	public String getProcess() {
		return process;
	}

	/**
	 * @param process
	 *            the process to set
	 */
	public void setProcess(String process) {
		this.process = process;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.entity.EntityJSON#formJOSN(java.lang.String)
	 */
	@Override
	public boolean formJOSN(String json) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.entity.EntityJSON#formJOSN(org.json.JSONObject)
	 */
	@Override
	public boolean formJOSN(JSONObject object) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.entity.EntityJSON#toJSON()
	 */
	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.entity.EntityJSON#toJSON(org.json.JSONObject)
	 */
	@Override
	public void toJSON(JSONObject object) {
		// TODO Auto-generated method stub

	}

}
