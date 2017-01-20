/**
 * @(#) DownLoadCheckCloudFile.java Created on Sep 19, 2015
 *
 * 
 */
package com.yuncore.bdsync.down;

import java.io.File;

import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>DownLoadCheckCloudFile</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DownLoadCheckCloudFile implements DownLoadCheckFileStep {

	private static final String TAG = "DownLoadCheckCloudFile";
	
	private String tmpDir;
	
	private String croot;
	
	private FSApi fsApi;

	/**
	 * @param fsApi
	 * @param croot 
	 */
	public DownLoadCheckCloudFile(String tmpDir,FSApi fsApi, String croot) {
		super();
		this.croot = croot;
		this.tmpDir = tmpDir;
		this.fsApi = fsApi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.down.DownLoadCheckFileStep#check(com.yuncore.bdsync.
	 * entity.LocalFile, com.yuncore.bdsync.down.DownloadOperate)
	 */
	@Override
	public boolean check(LocalFile downloadFile, DownloadOperate downloadOperate) {
		LocalFile cloudFile = null;
		try {
			cloudFile = fsApi.getMeta(croot + downloadFile.getAbsolutePath());
			if(null != cloudFile && croot.length() > 0){
				cloudFile.setPath(cloudFile.getAbsolutePath().substring(croot.length()));
			}
		} catch (ApiException e) {
			Log.e(TAG, "", e);
			return false;
		}

		if (cloudFile == null) {
			// 要下载的文件被删除了
			deleteDownLoadTmpFile(downloadFile);
			downloadOperate.deleteRecord(downloadFile);
			return false;
		}

		if (downloadFile.toFid().equals(cloudFile.toFid())) {
			if (cloudFile.getMtime() == downloadFile.getMtime() || cloudFile.getMd5() == downloadFile.getMd5()) {
				// 下载的文件的md5要与要下载的一样
				return true;
			}
		}
		// 类型不一样下载不了,修改时间不一样,md5不一样
		downloadOperate.deleteRecord(downloadFile);
		return false;
	}
	
	/**
	 * 删除下载的临时文件
	 * @param downloadFile
	 * @return
	 */
	private final boolean deleteDownLoadTmpFile(LocalFile downloadFile){
		final File file = new File(tmpDir, downloadFile.toFid());
		if(file.exists()){
			return file.delete();
		}
		return true;
	}

}
