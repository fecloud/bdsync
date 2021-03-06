/**
 * @(#) CloudDeleteActionLocalTask.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.delete.CloudDeleteActionLocal;

/**
 * The class <code>CloudDeleteActionLocalTask</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class CloudDeleteActionLocalTask implements SyncStepTask {

	protected String[] args;

	private CloudDeleteActionLocal cloudDeleteActionLocal;

	/**
	 * @param args
	 */
	public CloudDeleteActionLocalTask(String[] args) {
		super();
		this.args = args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#start()
	 */
	@Override
	public boolean start() {
		cloudDeleteActionLocal = new CloudDeleteActionLocal(Environment.getSyncDir());
		return cloudDeleteActionLocal.deletes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#stop()
	 */
	@Override
	public boolean stop() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getStepName()
	 */
	@Override
	public String getStepName() {
		return "com.yuncore.bdsync.sync.task.CloudDeleteActionLocalTask";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "执行云端文件删除并删除本地文件";
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getCode()
	 */
	@Override
	public int getCode() {
		return 3;
	}

}
