/**
 * @(#) UpLoadFileConent.java Created on Sep 22, 2015
 *
 * 
 */
package com.yuncore.bdsync.upload;

import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.entity.MkDirResult;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.http.HttpFormOutput.OutputDataListener;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>UpLoadFileConent</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadFileConent implements UpLoadCheckFileStep, OutputDataListener {

	private static final String TAG = "UpLoadFileConent";

	private String root;

	private FSApi fsApi;
	
	private LocalFileDao localFileDao;

	/**
	 * @param fsApi
	 */
	public UpLoadFileConent(String root, FSApi fsApi) {
		super();
		this.root = root;
		this.fsApi = fsApi;
		this.localFileDao = new LocalFileDao();
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
			if (mkdir(uploadFile)) {
				Log.d(TAG, "mkdir cloud " + uploadFile.getAbsolutePath() + " success");
				uploadOperate.deleteRecord(uploadFile);
			} else {
				Log.d(TAG, "mkdir cloud " + uploadFile.getAbsolutePath() + " success");
			}
		} else {
			try {
				uploadFileContext(uploadFile);
				uploadOperate.deleteRecord(uploadFile);
			} catch (ApiException e) {
			}
		}
		return true;
	}

	/**
	 * 上传文件正文
	 * 
	 * @param localFile
	 * @return
	 * @throws ApiException
	 */
	private boolean uploadFileContext(LocalFile localFile) throws ApiException {
		final long fileLen = localFile.getLength();
		// 判断是否大小分块上传的单块数,可以用秒传试一下
		if (fileLen > FSApi.RAPIDUPLOAD) {
			Log.d(TAG, "try secondFileContext");
			if (secondFileContext(localFile)) {
				Log.d(TAG, "secondFileContext ok");
				Log.i(TAG, "upload " + localFile.getParentPath() + " success");
				return true;
			} else {
				return norMalFileContext(localFile);
			}
		} else {
			return norMalFileContext(localFile);
		}
	}

	/**
	 * 以普通的form上传文件(不可以断点的)
	 * 
	 * @param localFile
	 * @return
	 * @throws ApiException
	 */
	private boolean norMalFileContext(LocalFile localFile) throws ApiException {
		Log.d(TAG, "norMalFileContext");
		try {
			if (checkBDFile(localFile)) {
				Log.w(TAG, "file too big ,not upload");
				return true;
			}
			final String localpath = String.format("%s/%s", root, localFile.getAbsolutePath());
			final String cloudpath = localFile.getAbsolutePath();
			final boolean result = fsApi.upload2(localpath, cloudpath, this);
			if (result) {
				Log.i(TAG, "upload " + localFile.getParentPath() + " success");
			}
			return result;
		} catch (ApiException e) {
			StatusMent.setProperty(StatusMent.UPLOAD_SIZE, 0);
			throw new ApiException(String.format("uploadFileContext file:%s error", localFile.getAbsolutePath()), e);
		}
	}

	/**
	 * 检查要上传的文件大小,http超过1G上传不鸟
	 * 
	 * @param localFile
	 * @return
	 */
	private boolean checkBDFile(LocalFile localFile) {
		final long max = 1024l * 1024l * 1024l * 3l;
		return localFile.getLength() >= max;
	}

	/**
	 * 秒传文件的方式
	 * 
	 * @param localFile
	 * @return
	 * @throws ApiException
	 */
	private boolean secondFileContext(LocalFile localFile) throws ApiException {
		try {
			final String localpath = String.format("%s/%s", root, localFile.getAbsolutePath());
			final String cloudpath = localFile.getAbsolutePath();
			return fsApi.secondUpload(localpath, cloudpath);
		} catch (ApiException e) {
			throw new ApiException(String.format("secondFileContext file:%s error", localFile.getAbsolutePath()), e);
		}
	}

	/**
	 * 在云端创建文件夹
	 * 
	 * @param uploadFile
	 * @return
	 */
	private final boolean mkdir(LocalFile uploadFile) {
		try {
			final MkDirResult mkdir = fsApi.mkdir(uploadFile.getAbsolutePath());
			if (null != mkdir && mkdir.getStatus() == 0) {
				uploadFile.setMtime(mkdir.getMtime());
				addDirToLocalFile(uploadFile);
				return true;
			}
		} catch (ApiException e) {
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.http.HttpFormOutput.OutputDataListener#onWrite(long,
	 * long)
	 */
	@Override
	public void onWrite(long sum, long commit) {
		StatusMent.setProperty(StatusMent.UPLOAD_SIZE, commit);
	}

	/**
	 * 往本地列表里面添加一条数据,以免本直列表再一次上传
	 * 
	 * @param downloadFile
	 */
	private final void addDirToLocalFile(LocalFile downloadFile) {
		downloadFile.setNewest(false);
		localFileDao.insert(downloadFile);
	}

}
