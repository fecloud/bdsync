/**
 * @(#) SyncSleepTask.java Created on Sep 14, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

import com.yuncore.bdsync.Argsment;

/**
 * The class <code>SyncSleepTask</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class SyncSleepTask implements SyncStepTask {

	private long lasttime = System.currentTimeMillis();

	private volatile boolean flag;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#start()
	 */
	@Override
	public boolean start() {
		flag = true;
		while (flag && Argsment.getBDSyncAllow()) {
			long time = System.currentTimeMillis() - lasttime;
			if (time < Argsment.getBDSyncInterval()) {
				synchronized (this) {
					try {
						// 允许同步
						if (Argsment.getBDSyncAllow()) {
							time = Argsment.getBDSyncInterval() - time;
							wait(time);
							break;
						}

					} catch (InterruptedException e) {
					}
				}
			}
		}
		this.lasttime = System.currentTimeMillis();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#stop()
	 */
	@Override
	public boolean stop() {
		this.flag = true;
		synchronized (this) {
			notifyAll();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getStepName()
	 */
	@Override
	public String getStepName() {
		return "com.yuncore.bdsync.sync.task.SyncSleepTask";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.sync.task.SyncStepTask#getRealName()
	 */
	@Override
	public String getRealName() {
		return "等待下一次同步中";
	}

}
