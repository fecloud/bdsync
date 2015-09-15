package com.yuncore.bdsync.upload;

import java.io.File;

import com.yuncore.bdsync.Argsment;
import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.CloudFileDao;
import com.yuncore.bdsync.dao.UploadDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.entity.CloudFile;
import com.yuncore.bdsync.entity.MkDirResult;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.http.HttpFormOutput.OutputDataListener;
import com.yuncore.bdsync.util.FileUtil;
import com.yuncore.bdsync.util.Log;

public class LocalUpload implements OutputDataListener {

	static final String TAG = "LocalUpload";

	protected FSApi api;

	protected String root;

	protected String tmpDir;

	protected UploadDao uploadDao;

	protected CloudFileDao cloudFileDao;

	public LocalUpload(String root, String tmpDir) {
		this.root = root;
		this.tmpDir = tmpDir;
		uploadDao = new UploadDao();
		api = new FSApiImple();
		cloudFileDao = new CloudFileDao();
	}

	public boolean start() {
		// Log.d(TAG, String.format("LocalUpload root:%s tmpDir:%s", root,
		// tmpDir));

		LocalFile localFile = null;
		boolean upload = true;
		while (Argsment.getBDSyncAllow()) {

			localFile = getUpload();
			if (localFile != null) {
				Log.d(TAG,
						"getUpload "
								+ localFile.getAbsolutePath()
								+ " size:"
								+ FileUtil.byteSizeToHuman(localFile
										.getLength()));
				StatusMent.setProperty(StatusMent.UPLOADING, localFile);
				upload = uploadFile(localFile);
				if (upload) {
					Log.i(TAG, "upload " + localFile.getParentPath()
							+ " success");
					StatusMent.setProperty(StatusMent.UPLOADING, "");
					delUpload(localFile);
				}
			} else {
				return true;
			}
		}
		return false;
	}

	private LocalFile getUpload() {
		return uploadDao.query();
	}

	private void delUpload(LocalFile localFile) {
		if (uploadDao.deleteByFid(localFile.getfId())) {
			Log.i(TAG, "delUpload " + localFile.getAbsolutePath());
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param localFile
	 * @return
	 * @throws ApiException
	 */
	private boolean uploadFile(LocalFile localFile) {
		try {
			StatusMent.setProperty(StatusMent.UPLOAD_SIZE, 0);
			if (!checkLocalFile(localFile)) {// 本地文件不在了,直接删除任务
				Log.w(TAG, "local file " + localFile.getAbsolutePath()
						+ " is deleted");
				return true;
			}

			if (fileExistsCloudFileDB(localFile)) {
				return true;
			}

			if (fileExists(localFile)) {
				return true;
			}

			//
			if (localFile.isDirectory()) {
				return mkdirCloud(localFile);
			} else if (localFile.isFile()) {
				return uploadFileContext(localFile);
			}
		} catch (ApiException e) {
			Log.e(TAG, "uploadFile", e);
		}
		return false;
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
	 * 检查本地文件是否还在
	 * 
	 * @param localFile
	 * @return
	 */
	private boolean checkLocalFile(LocalFile localFile) {
		final String localpath = String.format("%s/%s", root,
				localFile.getAbsolutePath());
		final File file = new File(localpath);
		if (file.exists() && file.length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 是否存在云端文件列表中(必免http网盘接口查询)
	 * 
	 * @param localFile
	 * @return
	 */
	private boolean fileExistsCloudFileDB(LocalFile localFile) {
		final CloudFile file = cloudFileDao.queryByFid(localFile.getfId());
		if (file != null) {
			return true;
		}
		return false;
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
			final String localpath = String.format("%s/%s", root,
					localFile.getAbsolutePath());
			final String cloudpath = localFile.getAbsolutePath();
			return api.upload2(localpath, cloudpath, this);
		} catch (ApiException e) {
			StatusMent.setProperty(StatusMent.UPLOAD_SIZE, 0);
			throw new ApiException(String.format(
					"uploadFileContext file:%s error",
					localFile.getAbsolutePath()), e);
		}
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
			final String localpath = String.format("%s/%s", root,
					localFile.getAbsolutePath());
			final String cloudpath = localFile.getAbsolutePath();
			return api.secondUpload(localpath, cloudpath);
		} catch (ApiException e) {
			throw new ApiException(String.format(
					"secondFileContext file:%s error",
					localFile.getAbsolutePath()), e);
		}
	}

	/**
	 * 检查文件在云端是否存在 如果文件存在,长度跟本地不一样,删了
	 * 
	 * @param localFile
	 * @return
	 * @throws ApiException
	 */
	private boolean fileExists(LocalFile localFile) throws ApiException {
		final CloudFile fileExists = api.exists(localFile.getAbsolutePath(),
				localFile.isDir());
		if (fileExists != null) {
			Log.d(TAG,
					String.format("%s exists cloud",
							localFile.getAbsolutePath()));
			// 两个都是文件
			if (localFile.isFile() && fileExists.isFile()) {
				// 两个文件长度一样
				if (localFile.getLength() == fileExists.getLength()) {
					Log.d(TAG, String.format("%s exists cloud len equal",
							localFile.getAbsolutePath()));
					return true;
				} else {
					// 判断文件哪个是最新的,把文件删了
					if (fileExists.getMtime() < new File(root,
							localFile.getAbsolutePath()).lastModified()) { // 本地文件是最新的
						api.rm(localFile.getAbsolutePath());
					} else {
						// 云端文件最新
						return true;
					}
				}
			} else if (localFile.isDir() && fileExists.isDir()) {
				Log.d(TAG,
						String.format("%s exists cloud isdir",
								localFile.getAbsolutePath()));
				return true;
			}
		} else {
			Log.d(TAG,
					String.format("%s not exists cloud",
							localFile.getAbsolutePath()));
		}

		return false;
	}

	/**
	 * 在云端创建目录
	 * 
	 * @param localFile
	 * @return
	 * @throws ApiException
	 */
	private boolean mkdirCloud(LocalFile localFile) throws ApiException {
		try {

			final MkDirResult mkdir = api.mkdir(localFile.getAbsolutePath());
			if (null != mkdir) {
				Log.d(TAG, "mkir " + localFile.getAbsolutePath());
				if (mkdir.getStatus() == 0) {
					return true;
				}
				Log.w(TAG, "mkdirCloud " + localFile.getAbsolutePath()
						+ " error:" + mkdir.getStatus());
			}
		} catch (ApiException e) {
			throw new ApiException("mkdirCloud error", e);
		}
		return false;
	}

	@Override
	public void onWrite(long sum, long commit) {
		StatusMent.setProperty(StatusMent.UPLOAD_SIZE, commit);
	}
}
