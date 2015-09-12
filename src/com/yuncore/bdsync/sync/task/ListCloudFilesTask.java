/**
 * @(#) ListCloudFiles.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import com.yuncore.bdsync.files.cloud.GetCloudFile;

/**
 * The class <code>ListCloudFiles</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class ListCloudFilesTask extends ListLocalFilesTask {

	/**
	 * @param args
	 */
	public ListCloudFilesTask(String[] args) {
		super(args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.ListLocaFiles#getExcludeFilesFlag()
	 */
	@Override
	public String getExcludeFilesFlag() {
		return "-c";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.ListLocaFiles#listNewFiles()
	 */
	@Override
	protected boolean listNewFiles() {
		final GetCloudFile getCloudFile = new GetCloudFile(Runtime.getRuntime()
				.availableProcessors() * 2, "/");
		getCloudFile.addExclude(excludeFiles);
		return getCloudFile.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.ListLocalFiles#getStepName()
	 */
	@Override
	public String getStepName() {
		return "com.yuncore.bdsync.sync.task.ListCloudFiles";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.ListLocalFilesTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "获取云端文件列表";
	}

}
