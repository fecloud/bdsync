/**
 * @(#) CloudCompareTask.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import com.yuncore.bdsync.compare.CloudCompare;

/**
 * The class <code>CloudCompareTask</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class CloudCompareTask extends LocalCompareTask {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.LocalCompareTask#start()
	 */
	@Override
	public boolean start() {
		compare = new CloudCompare();
		return compare.compare();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.LocalCompareTask#getStepName()
	 */
	@Override
	public String getStepName() {
		return "com.yuncore.bdsync.sync.task.CloudCompareTask";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.LocalCompareTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "云端文件对比";
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.sync.task.LocalCompareTask#getCode()
	 */
	@Override
	public int getCode() {
		return 2;
	}
	
}
