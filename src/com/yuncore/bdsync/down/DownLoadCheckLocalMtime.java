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
	 * com.yuncore.bdsync.down.DownLoadCheckFileStep#check(com.yuncore.bdsync
	 * .entity.LocalFile, com.yuncore.bdsync.entity.LocalFile,
	 * com.yuncore.bdsync.entity.LocalFile,
	 * com.yuncore.bdsync.down.DownloadOperate)
	 */
	@Override
	public boolean check(LocalFile downloadFile, LocalFile cloudFile,
			LocalFile loalFile, DownloadOperate downloadOperate) {
		if (loalFile == null) {
			return true;
		} else {
			// 如果要下载的文件跟本地一样大小,当要下载的文件修改时间大于本地文件
			if (downloadFile.getMtime() > loalFile.getMtime()) {
				return true;
			} else {
				downloadOperate.deleteRecord(downloadFile);
			}
		}
		return false;
	}

}
