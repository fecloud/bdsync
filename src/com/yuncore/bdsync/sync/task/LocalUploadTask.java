/**
 * @(#) LocalUploadTask.java Created on Sep 14, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import java.io.File;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.upload.LocalUpload;

/**
 * The class <code>LocalUploadTask</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class LocalUploadTask implements SyncStepTask {

	private LocalUpload localUpload;

	private String[] args;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#start()
	 */
	/**
	 * @param args
	 */
	public LocalUploadTask(String[] args) {
		this.args = args;
	}

	@Override
	public boolean start() {
		localUpload = new LocalUpload(args[1], args[1] + File.separator
				+ Environment.SYNCTMPDIR);
		return localUpload.start();
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
		return "com.yuncore.bdsync.sync.task.LocalUploadTask";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "上传本地文件到云端";
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getCode()
	 */
	@Override
	public int getCode() {
		return 8;
	}

}
