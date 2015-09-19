/**
 * @(#) DownLoadCheckFileStep.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.down;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DownLoadCheckFileStep</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public interface DownLoadCheckFileStep {
	
	/**
	 * 检查文件,如果这一个步骤成功完成不要进行下一个返回true;
	 * 
	 * @param deleteFile
	 * @param compareFile
	 * @param args
	 * @return false不再继续检查了
	 */
	boolean check(LocalFile downloadFile, LocalFile cloudFile, LocalFile loalFile);
}
