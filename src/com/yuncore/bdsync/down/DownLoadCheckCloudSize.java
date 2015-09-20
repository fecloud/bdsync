/**
 * @(#) DownLoadCheckCloudSize.java Created on Sep 19, 2015
 *
 * 
 */
package com.yuncore.bdsync.down;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DownLoadCheckCloudSize</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DownLoadCheckCloudSize implements DownLoadCheckFileStep {

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
		if (cloudFile == null) {
			// 要下载的文件被删除了
			downloadOperate.deleteRecord(downloadFile);
		} else {
			if (downloadFile.isDir() == cloudFile.isDir()) {
				return true;
			} else {
				// 类型不一样下载不了
				downloadOperate.deleteRecord(downloadFile);
			}
		}
		return false;
	}

}
