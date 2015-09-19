/**
 * @(#) DownLoadCheckLocalSize.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.down;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DownLoadCheckLocalSize</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DownLoadCheckLocalSize implements DownLoadCheckFileStep {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.down.DownLoadCheckFileStep#check(com.yuncore.bdsync.
	 * entity.LocalFile, com.yuncore.bdsync.entity.LocalFile,
	 * com.yuncore.bdsync.entity.LocalFile)
	 */
	@Override
	public boolean check(LocalFile downloadFile, LocalFile cloudFile, LocalFile loalFile) {
		// 本地文件不在
		if (loalFile == null) {
			return true;
		}
		if (downloadFile.isDir() && loalFile.isDir()) {
			// 两个都是文件夹,进行下一步时间检查
			return true;
		} else if (downloadFile.isFile() && loalFile.isFile()) {
			if (downloadFile.getLength() == loalFile.getLength()) {
				// 两个文件大小一样,可以删除
				return true;
			}
		}
		// 两个文件大小不一样或者类型不一样
		return false;
	}

}
