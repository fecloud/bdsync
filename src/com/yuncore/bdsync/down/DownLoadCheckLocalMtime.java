/**
 * @(#) DownLoadCheckLocalMtime.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.down;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DownLoadCheckLocalMtime</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DownLoadCheckLocalMtime implements DownLoadCheckFileStep {

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
		if (loalFile == null) {
			return true;
		} else {

		}
		return true;
	}

}
