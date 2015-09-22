/**
 * @(#) UpLoadCheckFileStep.java Created on Sep 22, 2015
 *
 * 
 */
package com.yuncore.bdsync.upload;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>UpLoadCheckFileStep</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public interface UpLoadCheckFileStep {

	/**
	 * 检查文件,如果这一个步骤成功完成不要进行下一个返回true;
	 * 
	 * @param downloadFile
	 * @param downloadOperate
	 * @return false不再继续检查了
	 */
	boolean check(LocalFile uploadFile, UpLoadOperate uploadOperate);
	
}
