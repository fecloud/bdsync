/**
 * @(#) UpLoadFileNormalConent.java Created on 2015年9月28日
 *
 * 
 */
package com.yuncore.bdsync.upload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.http.HttpUploadFile.FileOutputListener;
import com.yuncore.bdsync.http.HttpUploadFile.FileSource;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>UpLoadFileNormalConent</code>
 * <p>
 * 普通文件上传
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadFileNormalConent implements UpLoadCheckFileStep, FileSource, FileOutputListener {

	private static final String TAG = "UpLoadFileNormalConent";
	/**
	 * 普通http最大上传10M
	 */
	private static final long MAX_SIZE = 1024l * 1024l * 10l;

	private String root;

	private FSApi fsApi;

	private LocalFile uploadFile;

	private UpLoadOperate uploadOperate;

	/**
	 * @param root
	 * @param fsApi
	 */
	public UpLoadFileNormalConent(String root, FSApi fsApi) {
		super();
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
		
		this.uploadFile = uploadFile;
		this.uploadOperate = uploadOperate;
		
		if (uploadFile.getLength() <= MAX_SIZE) {
			try {
				final String md5 = fsApi.uploadTmpFile(this, this);
				if (md5 == null) {
					return false;
				}

				if (!uploadOperate.getUpLoadStatus()) {
					return false;
				}

				final boolean createFile = fsApi.createFile(uploadFile.getAbsolutePath(), getFileLength(),
						new String[] { md5 }, true);

				if (createFile) {
					// 成功
					Log.w(TAG, "upload " + uploadFile.getAbsolutePath() + " success");
					uploadOperate.addAnotherRecord(uploadFile);
					uploadOperate.deleteRecord(uploadFile);
					return true;
				} else {
					// 失败
				}
				return false;

			} catch (ApiException e) {
			} finally {
				StatusMent.setProperty(StatusMent.UPLOAD_SIZE, 0);
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.http.HttpUploadFile.FileOutputListener#onWrite(long,
	 * long)
	 */
	@Override
	public void onWrite(long sum, long commit) {
		StatusMent.setProperty(StatusMent.UPLOAD_SIZE, commit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getFileLength()
	 */
	@Override
	public long getFileLength() {
		return uploadFile.getLength();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getFileName()
	 */
	@Override
	public String getFileName() {
		return uploadFile.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#isInterrupt()
	 */
	@Override
	public boolean isInterrupt() {
		return uploadOperate.getUpLoadStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		final FileInputStream in = new FileInputStream(root + uploadFile.getAbsolutePath());
		return in;
	}

}