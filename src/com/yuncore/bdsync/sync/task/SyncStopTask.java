/**
 * @(#) SyncStopTask.java Created on Sep 14, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import com.yuncore.bdsync.Argsment;

/**
 * The class <code>SyncStopTask</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class SyncStopTask implements SyncStepTask {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#start()
	 */
	@Override
	public boolean start() {
		while (!Argsment.getBDSyncAllow()) {
			try {
				synchronized (this) {
					wait(1000);
				}
			} catch (InterruptedException e) {
			}
		}
		return true;
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
		return "com.yuncore.bdsync.sync.task.SyncStopTask";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "同步服务已停止";
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getCode()
	 */
	@Override
	public int getCode() {
		return 10;
	}

}
