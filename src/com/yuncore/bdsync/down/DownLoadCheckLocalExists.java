/**
 * @(#) DownLoadCheckLocalExists.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.down;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DownLoadCheckLocalExists</code>
 * <p>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DownLoadCheckLocalExists implements DownLoadCheckFileStep {

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
		//本直文件不在
		if (loalFile == null) {
			return false;
		}
		return true;
	}

}
