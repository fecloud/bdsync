/**
 * @(#) CloudDownloadTask.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import java.io.File;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.down.CloudDownLoad;

/**
 * The class <code>CloudDownloadTask</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class CloudDownloadTask implements SyncStepTask {

	private CloudDownLoad cloudDownLoad;

	protected String[] args;

	/**
	 * @param args
	 */
	public CloudDownloadTask(String[] args) {
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
		cloudDownLoad = new CloudDownLoad(Environment.getSyncDir(),
				Environment.getSyncDir() + File.separator
						+ Environment.SYNCTMPDIR, Environment.getCloudDir());
		return cloudDownLoad.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#stop()
	 */
	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getStepName()
	 */
	@Override
	public String getStepName() {
		return "com.yuncore.bdsync.sync.task.CloudDownloadTask";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "下载云端文件到本地";
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getCode()
	 */
	@Override
	public int getCode() {
		return 4;
	}

}
