/**
 * @(#) LocalDeleteActionCloudTask.java Created on Sep 14, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import com.yuncore.bdsync.delete.LocalDeleteActionCloud;

/**
 * The class <code>LocalDeleteActionCloudTask</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class LocalDeleteActionCloudTask implements SyncStepTask {

	private String[] args;

	private LocalDeleteActionCloud localDeleteActionCloud;

	/**
	 * @param args
	 */
	public LocalDeleteActionCloudTask(String[] args) {
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
		localDeleteActionCloud = new LocalDeleteActionCloud(args[1]);
		return localDeleteActionCloud.deletes();
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
		return "com.yuncore.bdsync.sync.task.LocalDeleteActionCloudTask";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "执行本地文件删除并删除云端文件";
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getCode()
	 */
	@Override
	public int getCode() {
		return 7;
	}

}
