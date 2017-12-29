/**
 * @(#) UpLoadFileNormalConent.java Created on 2015年9月28日
 *
 * 
 */
package com.yuncore.bdsync.upload;

import java.io.File;
import java.io.IOException;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>UpLoadFileNormalConent</code>
 * <p>
 * 普通文件上传
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadFileNormalConent implements UpLoadCheckFileStep {

	private static final String TAG = "UpLoadFileNormalConent";
	/**
	 * 普通http最大上传10M
	 */
	private static final long MAX_SIZE = 1024l * 1024l * 10l;

	private String croot;
	
	private String root;

	private FSApi fsApi;

	/**
	 * @param root
	 * @param fsApi
	 */
	public UpLoadFileNormalConent(String croot, String root, FSApi fsApi) {
		super();
		this.croot = croot;
		this.root = root;
		this.fsApi = fsApi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.upload.UpLoadCheckFileStep#check(com.yuncore.bdsync.
	 * entity.LocalFile, com.yuncore.bdsync.upload.UpLoadOperate)
	 */
	@Override
	public boolean check(LocalFile uploadFile, UpLoadOperate uploadOperate) {

		final UploadFileSourceOutputListener listener = new UploadFileSourceOutputListener(
				root, uploadFile, uploadOperate);

		Log.d(TAG, "UpLoadFileNormalConent uploading...");

		if (uploadFile.getLength() <= MAX_SIZE) {
			try {
				final String md5 = fsApi.uploadTmpFile(listener, listener);
				if (md5 == null) {
					return false;
				}

				if (!uploadOperate.getUpLoadStatus()) {
					return false;
				}

				final boolean createFile = fsApi.createFile(croot + uploadFile.getAbsolutePath(), listener.getFileLength(),
						new String[] { md5 }, true);

				if (createFile) {
					// 成功
					Log.w(TAG, "upload " + uploadFile.getAbsolutePath() + " success");
					final boolean uploaddelfile = Boolean.valueOf(Environment.getUploadDelFile());
					if (uploaddelfile) {
						final boolean del = new File(root + uploadFile.getAbsolutePath()).delete();
						if (del) {
							Log.w(TAG, "upload delfile " + uploadFile.getAbsolutePath() + " success");
						} else {
							Log.w(TAG, "upload delfile " + uploadFile.getAbsolutePath() + " fail");
						}
					}
					uploadOperate.addAnotherRecord(uploadFile);
					uploadOperate.deleteRecord(uploadFile);
				}
				return false;

			} catch (ApiException e) {
				return false;
			} finally {
				try {
					if (null != listener.getInputStream()) {
						listener.getInputStream().close();
					}
				} catch (IOException e) {
				}
				StatusMent.getDoingfile().remove(uploadFile.getAbsolutePath());
			}
		} else {
			Log.d(TAG, "file too big");
		}

		return true;
	}

}
