/**
 * @(#) UpLoadFileConent.java Created on Sep 22, 2015
 *
 * 
 */
package com.yuncore.bdsync.upload;

import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.entity.MkDirResult;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>UpLoadFileConent</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadFileConent implements UpLoadCheckFileStep {

	private static final String TAG = "UpLoadFileConent";

	private String croot;
	
	private FSApi fsApi;

	/**
	 * @param croot 
	 * @param fsApi
	 */
	public UpLoadFileConent(String croot, FSApi fsApi) {
		super();
		this.croot = croot;
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
		if (uploadFile.isDir()) {
			try {
				final MkDirResult mkdir = fsApi.mkdir(croot + uploadFile.getAbsolutePath());
				if (null != mkdir && mkdir.getStatus() == 0) {
					uploadFile.setMtime(mkdir.getMtime());
					uploadOperate.addAnotherRecord(uploadFile);
					Log.d(TAG, "mkdir cloud " + uploadFile.getAbsolutePath() + " success");
					uploadOperate.deleteRecord(uploadFile);
					return false;
				}
			} catch (ApiException e) {
			}
			Log.d(TAG, "mkdir cloud " + uploadFile.getAbsolutePath() + " success");
			return false;
		}
		return true;
	}

}
