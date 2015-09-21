/**
 * @(#) DownLoadCheckCloudMtime.java Created on Sep 19, 2015
 *
 * 
 */
package com.yuncore.bdsync.down;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DownLoadCheckCloudMtime</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DownLoadCheckCloudMtime implements DownLoadCheckFileStep {

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
		if (downloadFile.isFile() && cloudFile.isFile()) {
			if (cloudFile.getMtime() == downloadFile.getMtime()
					|| cloudFile.getMd5() == downloadFile.getMd5()) {
				// 下载的文件的md5要与要下载的一样
				return true;
			}

		} else {
			// 文件夹就不检查修改时间了
			return true;
		}
		downloadOperate.deleteRecord(downloadFile);
		return false;
	}

}
