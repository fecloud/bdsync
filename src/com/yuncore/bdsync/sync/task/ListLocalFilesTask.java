/**
 * @(#) ListLocaFiles.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import java.util.HashSet;
import java.util.Set;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.files.local.GetLocalFile;

/**
 * The class <code>ListLocalFiles</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class ListLocalFilesTask implements SyncStepTask {

	protected Set<String> excludeFiles = new HashSet<String>();

	public ListLocalFilesTask(String[] args) {
		addExcludeFiles(args);
	}

	protected String getTag() {
		return "ListLocaFiles";
	}

	public String getExcludeFilesFlag() {
		return "-l";
	}

	private void addExcludeFiles(String[] args) {
		
		excludeFiles.add("tmp");
		excludeFiles.add(Environment.SYNCTMPDIR);
		
		if (args.length > 3) {
			boolean startAdd = false;
			for (int i = 2; i < args.length; i++) {
				if (args[i].equals(getExcludeFilesFlag())) {
					startAdd = true;
				} else if (startAdd && args[i].startsWith("-")) {
					break;
				} else if (startAdd) {
					excludeFiles.add(args[i]);
				}
			}
		}
	}

	/**
	 * 读取最新的文件
	 * 
	 * @return
	 */
	protected boolean listNewFiles() {
		final GetLocalFile getLocalFile = new GetLocalFile(Runtime.getRuntime()
				.availableProcessors() * 2, Environment.getSyncDir());
		getLocalFile.addExclude(excludeFiles);
		return getLocalFile.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getStepName()
	 */
	@Override
	public String getStepName() {
		return "com.yuncore.bdsync.sync.task.ListLocalFiles";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#start()
	 */
	@Override
	public boolean start() {
		return listNewFiles();
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
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "获取本地文件列表";
	}

}
