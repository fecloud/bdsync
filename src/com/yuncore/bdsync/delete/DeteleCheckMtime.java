/**
 * @(#) DeteleCheckMtime.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.delete;

import com.yuncore.bdsync.delete.DeleteCheckFileStep;
import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DeteleCheckMtime</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DeteleCheckMtime implements DeleteCheckFileStep {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.DeleteCheckFileStep#check(com.yuncore.bdsync.
	 * entity.LocalFile, com.yuncore.bdsync.entity.LocalFile,
	 * java.lang.Object[])
	 */
	@Override
	public boolean check(LocalFile deleteFile, LocalFile compareFile, Object... args) {
		if (deleteFile.isDir() && compareFile.isDir()) {
			// 两个都是文件夹
			if (deleteFile.getMtime() >= compareFile.getMtime()) {
				// 如果本地文件夹修改时间大于或者等于云端的,可以删除云端的,否则文件夹可能被修改了
				return false;
			}
		} else if (deleteFile.isFile() && compareFile.isFile()) {
			// 两个都是文件
			if (deleteFile.getMtime() >= compareFile.getMtime()) {
				// 如果本地文件修改时间大于或者等于云端的,可以删除云端的,否则文件可能被修改了
				return false;
			}
		}
		return true;
	}

}
