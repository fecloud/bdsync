/**
 * @(#) SyncSetpTask.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.sync.task;

/**
 * The class <code>SyncStepTask</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public interface SyncStepTask {

	boolean start();

	boolean stop();

	/**
	 * 步骤名称
	 * 
	 * @return
	 */
	String getStepName();
	
	/**
	 * 真实名字
	 * @return
	 */
	String getRealName();

}
