/**
 * @(#) DeteleCheckSize.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.delete;

import com.yuncore.bdsync.delete.DeleteCheckFileStep;
import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DeteleCheckSize</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DeteleCheckSize implements DeleteCheckFileStep {

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
			// 两个都是文件夹,进行下一步时间检查
			return true;
		} else if (deleteFile.isFile() && compareFile.isFile()) {
			if (deleteFile.getLength() == compareFile.getLength()) {
				// 两个文件大小一样,可以删除
				return false;
			}
		}
		// 两个文件大小不一样或者类型不一样
		return true;
	}

}
