/**
 * UploadFileSourceOutputListener.java Created on 2017年12月28日
 */
package com.yuncore.bdsync.upload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.entity.DoingFile;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.http.HttpUploadFile.FileOutputListener;
import com.yuncore.bdsync.http.HttpUploadFile.FileSource;

/**
 * The class <code>UploadFileSourceOutputListener</code>	
 * @author Feng OuYang
 * @version 1.0
 */
public class UploadFileSourceOutputListener implements FileOutputListener, FileSource {

	protected String root;
	
	protected LocalFile uploadFile;
	
	protected FileInputStream fileInputStream;
	
	protected UpLoadOperate uploadOperate;
	

	/**
	 * @param uploadFile
	 * @param uploadOperate
	 */
	public UploadFileSourceOutputListener(String root, LocalFile uploadFile,
			UpLoadOperate uploadOperate) {
		super();
		this.root = root;
		this.uploadFile = uploadFile;
		this.uploadOperate = uploadOperate;
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getFileLength()
	 */
	@Override
	public long getFileLength() {
		return uploadFile.getLength();
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getFileName()
	 */
	@Override
	public String getFileName() {
		return uploadFile.getName();
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#isInterrupt()
	 */
	@Override
	public boolean isInterrupt() {
		return uploadOperate.getUpLoadStatus();
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if(null == fileInputStream) {
			fileInputStream = new FileInputStream(root + uploadFile.getAbsolutePath());
		}
		return fileInputStream;
	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileOutputListener#onWrite(long, long)
	 */
	@Override
	public void onWrite(long sum, long commit) {
		StatusMent.getDoingfile().put(uploadFile.getAbsolutePath(), 
				new DoingFile(uploadFile).setDoingSize(commit));
	}

}
