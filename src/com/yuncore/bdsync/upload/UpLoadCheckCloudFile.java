/**
 * @(#) UpLoadCheckCloudFile.java Created on Sep 22, 2015
 *
 * 
 */
package com.yuncore.bdsync.upload;

import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;

/**
 * The class <code>UpLoadCheckCloudFile</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadCheckCloudFile implements UpLoadCheckFileStep {

	private FSApi fsApi;

	/**
	 * @param fsApi
	 */
	public UpLoadCheckCloudFile(FSApi fsApi) {
		super();
		this.fsApi = fsApi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.upload.UpLoadCheckFileStep#check(com.yuncore.bdsync
	 * .entity.LocalFile, com.yuncore.bdsync.upload.UpLoadOperate)
	 */
	@Override
	public boolean check(LocalFile uploadFile, UpLoadOperate uploadOperate) {
		LocalFile cloudFile = null;
		try {
			cloudFile = fsApi.getMeta(uploadFile.getAbsolutePath());
		} catch (ApiException e) {
			return false;
		}

		if (cloudFile == null) {
			// 云端没有这个文件,可以上传
			return true;
		}

		if (uploadFile.isDir() == uploadFile.isDir()) {
			// 类型相同
			if (uploadFile.isDir()) {
				// 是文件夹
				uploadOperate.deleteRecord(uploadFile);
				return false;
			} else {
				// 是文件
				if (uploadFile.getLength() != cloudFile.getLength()) {
					// 如果它们俩长度不一样时,本地文件大于云端则可以上传
					if (uploadFile.getMtime() > cloudFile.getMtime()) {
						return true;
					}
				}
			}
		}

		// 类型不同,不要上传
		uploadOperate.deleteRecord(uploadFile);
		return false;
	}

}
