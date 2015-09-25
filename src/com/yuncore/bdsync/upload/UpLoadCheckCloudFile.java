/**
 * @(#) UpLoadCheckCloudFile.java Created on Sep 22, 2015
 *
 * 
 */
package com.yuncore.bdsync.upload;

import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>UpLoadCheckCloudFile</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadCheckCloudFile implements UpLoadCheckFileStep {

	private static final String TAG = "UpLoadCheckCloudFile";

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

		if (uploadFile.toFid().equals(cloudFile.toFid())) {
			Log.d(TAG,
					String.format("cloud has %s", uploadFile.getAbsolutePath()));
			// 是文件夹或者文件,云端也有
			uploadOperate.deleteRecord(uploadFile);
			return false;
		}

		if (uploadFile.isFile() && cloudFile.isFile()) {
			// 是文件
			if (uploadFile.getLength() != cloudFile.getLength()) {
				// 如果它们俩长度不一样时,本地文件大于云端则可以上传
				if (uploadFile.getMtime() > cloudFile.getMtime()) {
					return true;
				}
			}
		}

		// 类型不同,不要上传
		uploadOperate.deleteRecord(uploadFile);
		return false;
	}

}
