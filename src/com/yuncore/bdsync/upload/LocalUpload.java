//package com.yuncore.bdsync.upload;
//
//import java.io.File;
//
//import com.yuncore.bdsync.Argsment;
//import com.yuncore.bdsync.StatusMent;
//import com.yuncore.bdsync.api.FSApi;
//import com.yuncore.bdsync.api.imple.FSApiImple;
//import com.yuncore.bdsync.dao.UploadDao;
//import com.yuncore.bdsync.entity.LocalFile;
//import com.yuncore.bdsync.entity.CloudFile;
//import com.yuncore.bdsync.entity.MkDirResult;
//import com.yuncore.bdsync.exception.ApiException;
//import com.yuncore.bdsync.http.HttpFormOutput.OutputDataListener;
//import com.yuncore.bdsync.util.FileUtil;
//import com.yuncore.bdsync.util.Log;
//
//public class LocalUpload extends Thread implements OutputDataListener {
//
//	static final String TAG = "LocalUpload";
//
//	protected FSApi api;
//
//	protected String root;
//
//	private UploadDao uploadDao;
//
//	public LocalUpload(String root, String tmpDir) {
//		this.root = root;
//		uploadDao = new UploadDao();
//		api = new FSApiImple();
//	}
//
//	@Override
//	public void run() {
//		setName(LocalUpload.class.getSimpleName());
//		Log.i(TAG, String.format("LocalUpload root:%s tmpDir:%s", root));
//
//		LocalFile file = null;
//		boolean upload = true;
//		while (true) {
//			if (Argsment.getBDSyncAllow()) {
//
//				file = getUpload();
//				if (file != null) {
//					Log.i(TAG, "getUpload " + file.getAbsolutePath() + " size:"
//							+ FileUtil.byteSizeToHuman(file.getLength()));
//					StatusMent.setProperty(StatusMent.uploading, file);
//					upload = uploadFile(file);
//					if (upload) {
//						StatusMent.setProperty(StatusMent.uploading, "");
//						delUpload(file);
//					}
//				} else {
//					try {
//						Thread.sleep(30000);
//						StatusMent.setProperty(StatusMent.uploading, false);
//					} catch (InterruptedException e) {
//						break;
//					}
//				}
//			} else {
//				Log.w(TAG, "not allow upload");
//				while (!Argsment.getBDSyncAllow()) {
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//					}
//				}
//			}
//		}
//	}
//
//	private LocalFile getUpload() {
//		return uploadDao.query();
//	}
//
//	private void delUpload(LocalFile file) {
//		if (uploadDao.deleteByFid(file.getfId())) {
//			Log.i(TAG, "delUpload " + file.getAbsolutePath());
//		}
//	}
//
//	/**
//	 * 上传文件
//	 * 
//	 * @param file
//	 * @return
//	 * @throws ApiException
//	 */
//	private boolean uploadFile(LocalFile file) {
//		try {
//			StatusMent.setProperty(StatusMent.key_upload_size, 0);
//			if (!checkLocalFile(file)) {// 本地文件不在了,直接删除任务
//				Log.w(TAG, "local file " + file.getAbsolutePath()
//						+ " is deleted");
//				return true;
//			}
//			if (fileExists(file)) {
//				return true;
//			}
//
//			//
//			if (file.isDirectory()) {
//				return mkdirCloud(file);
//			} else if (file.isFile()) {
//				return uploadFileContext(file);
//			}
//		} catch (ApiException e) {
//			Log.e(TAG, "uploadFile", e);
//		}
//		return false;
//	}
//
//	/**
//	 * 检查要上传的文件大小,http超过1G上传不鸟
//	 * 
//	 * @param file
//	 * @return
//	 */
//	private boolean checkBDFile(LocalFile file) {
//		final long max = 1024l * 1024l * 1024l * 3l;
//		return file.getLength() >= max;
//	}
//
//	/**
//	 * 检查本地文件是否还在
//	 * 
//	 * @param file
//	 * @return
//	 */
//	private boolean checkLocalFile(LocalFile file) {
//		final String localpath = String.format("%s/%s", root,
//				file.getAbsolutePath());
//		final File localFile = new File(localpath);
//		if (localFile.exists() && localFile.length() > 0) {
//			return true;
//		}
//		return false;
//	}
//
//	/**
//	 * 上传文件正文
//	 * 
//	 * @param file
//	 * @return
//	 * @throws ApiException
//	 */
//	private boolean uploadFileContext(LocalFile file) throws ApiException {
//		final long fileLen = file.getLength();
//		// 判断是否大小分块上传的单块数,可以用秒传试一下
//		if (fileLen > FSApi.RAPIDUPLOAD) {
//			Log.d(TAG, "try secondFileContext");
//			if (secondFileContext(file)) {
//				Log.d(TAG, "secondFileContext ok");
//				return true;
//			} else {
//				return norMalFileContext(file);
//			}
//		} else {
//			return norMalFileContext(file);
//		}
//	}
//
//	/**
//	 * 以普通的form上传文件(不可以断点的)
//	 * 
//	 * @param file
//	 * @return
//	 * @throws ApiException
//	 */
//	private boolean norMalFileContext(LocalFile file) throws ApiException {
//		Log.d(TAG, "norMalFileContext");
//		try {
//			if (checkBDFile(file)) {
//				Log.w(TAG, "file too big ,not upload");
//				return true;
//			}
//			final String localpath = String.format("%s/%s", root,
//					file.getAbsolutePath());
//			final String cloudpath = file.getAbsolutePath();
//			return api.upload2(localpath, cloudpath, this);
//		} catch (ApiException e) {
//			StatusMent.setProperty(StatusMent.key_upload_size, 0);
//			throw new ApiException(String.format(
//					"uploadFileContext file:%s error", file.getAbsolutePath()),
//					e);
//		}
//	}
//
//	/**
//	 * 秒传文件的方式
//	 * 
//	 * @param file
//	 * @return
//	 * @throws ApiException
//	 */
//	private boolean secondFileContext(LocalFile file) throws ApiException {
//		try {
//			final String localpath = String.format("%s/%s", root,
//					file.getAbsolutePath());
//			final String cloudpath = file.getAbsolutePath();
//			return api.secondUpload(localpath, cloudpath);
//		} catch (ApiException e) {
//			throw new ApiException(String.format(
//					"secondFileContext file:%s error", file.getAbsolutePath()),
//					e);
//		}
//	}
//
//	/**
//	 * 检查文件在云端是否存在 如果文件存在,长度跟本地不一样,删了
//	 * 
//	 * @param file
//	 * @return
//	 * @throws ApiException
//	 */
//	private boolean fileExists(LocalFile file) throws ApiException {
//		final CloudFile fileExists = api.exists(file.getAbsolutePath(),
//				file.isDir());
//		if (fileExists != null) {
//			Log.d(TAG, String.format("%s exists cloud", file.getAbsolutePath()));
//			// 两个都是文件
//			if (file.isFile() && fileExists.isFile()) {
//				// 两个文件长度一样
//				if (file.getLength() == fileExists.getLength()) {
//					Log.d(TAG,
//							String.format("%s exists cloud len equal",
//									file.getAbsolutePath()));
//					return true;
//				} else {
//					// 判断文件哪个是最新的,把文件删了
//					if (fileExists.getMtime() < new File(root,
//							file.getAbsolutePath()).lastModified()) { // 本地文件是最新的
//						api.rm(file.getAbsolutePath());
//					} else {
//						// 云端文件最新
//						return true;
//					}
//				}
//			} else if (file.isDir() && fileExists.isDir()) {
//				Log.d(TAG,
//						String.format("%s exists cloud isdir",
//								file.getAbsolutePath()));
//				return true;
//			}
//		} else {
//			Log.d(TAG, String.format("%s not exists cloud",
//					file.getAbsolutePath()));
//		}
//
//		return false;
//	}
//
//	/**
//	 * 在云端创建目录
//	 * 
//	 * @param file
//	 * @return
//	 * @throws ApiException
//	 */
//	private boolean mkdirCloud(LocalFile file) throws ApiException {
//		try {
//
//			final MkDirResult mkdir = api.mkdir(file.getAbsolutePath());
//			if (null != mkdir) {
//				Log.d(TAG, "mkir " + file.getAbsolutePath());
//				if (mkdir.getStatus() == 0) {
//					return true;
//				}
//				Log.w(TAG, "mkdirCloud " + file.getAbsolutePath() + " error:"
//						+ mkdir.getStatus());
//			}
//		} catch (ApiException e) {
//			throw new ApiException("mkdirCloud error", e);
//		}
//		return false;
//	}
//
//	@Override
//	public void onWrite(long sum, long commit) {
//		StatusMent.setProperty(StatusMent.key_upload_size, commit);
//	}
//}
