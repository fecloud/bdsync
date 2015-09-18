/**
 * @(#) DeleteCheckFileStep.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.delete;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DeleteCheckFileStep</code>
 * <p>
 * 删除文件时的检查文件步骤
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public interface DeleteCheckFileStep {

	/**
	 * 检查文件
	 * 
	 * @param deleteFile
	 * @param compareFile
	 * @param args
	 * @return false不再继续检查了
	 */
	boolean check(LocalFile deleteFile, LocalFile compareFile, Object... args);

}
