package com.yuncore.bdsync.down;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.DownloadInputStream;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.DownloadDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.FileMV;
import com.yuncore.bdsync.util.Log;

public class CloudDownLoad {

	static final String TAG = "CloudDownLoad";

	private String root;

	private String tmpDir;

	private FSApi fsApi;

	private DownloadDao downloadDao;

	private List<DownLoadCheckFileStep> steps = new ArrayList<DownLoadCheckFileStep>();

	protected volatile boolean flag;

	public CloudDownLoad(String root, String tmpDir) {
		this.root = root;
		this.tmpDir = tmpDir;
		fsApi = new FSApiImple();
		downloadDao = new DownloadDao();

		steps.add(new DownLoadCheckLocalSize());

		final File file = new File(tmpDir);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public boolean start() {
		LocalFile downloadFile = null;
		flag = true;
		while (flag) {

			downloadFile = getDownLoad();
			if (downloadFile != null) {
				StatusMent.setProperty(StatusMent.DOWNLOADING, downloadFile);
				StatusMent.setProperty(StatusMent.DOWNLOAD_SIZE, 0);
				try {
					checkAndDownLoad(downloadFile);
				} catch (Exception e) {
				}
			} else {
				return true;
			}
		}
		StatusMent.setProperty(StatusMent.DOWNLOADING, false);
		return true;
	}

	/**
	 * @param cloudFile
	 * @throws ApiException
	 */
	private void checkAndDownLoad(LocalFile file) throws Exception {
		final LocalFile downloadFile = file;
		final LocalFile cloudFile = getCloudFile(downloadFile);
		final LocalFile localFile = getLocalFile(downloadFile);
		// 如果云端文件还在
		for (DownLoadCheckFileStep step : steps) {
			if (!step.check(downloadFile, cloudFile, localFile)) {
				delDownLoad(downloadFile);
			}
		}
	}

	private LocalFile getDownLoad() {
		return downloadDao.query();
	}

	private void delDownLoad(LocalFile cloudFile) {
		StatusMent.setProperty(StatusMent.DOWNLOADING, "");
		if (downloadDao.deleteByFid(cloudFile.getfId())) {
			Log.d(TAG, "delDownLoad " + cloudFile.getAbsolutePath());
		}
	}

	/**
	 * 返回flase下载文件
	 * 
	 * @param cloudFile
	 * @return
	 */
	private boolean checkFile(LocalFile cloudFile) {
		final String file = root + cloudFile.getAbsolutePath();
		final File targetFile = new File(file);
		if (targetFile.exists()) {
			if (targetFile.isFile()) {
				if (cloudFile.isFile() && cloudFile.getLength() == targetFile.length()) {
					Log.d(TAG, "file local exists");
					return true;
				} else {
					Log.d(TAG, "file local exists but length not equals,file was changed");
					return true;
				}
			} else if (targetFile.isDirectory()) {
				if (cloudFile.isDirectory()) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean downloadFile(LocalFile cloudFile) {
		final boolean checkResult = checkFile(cloudFile);
		final String file = root + cloudFile.getAbsolutePath();
		if (!checkResult) {
			if (cloudFile.isDirectory()) {
				return new File(file).mkdirs();
			} else if (cloudFile.isFile()) {
				for (int i = 0; i < 10; i++) {
					if (downloadFileContext(cloudFile)) {
						return true;
					}
				}
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	/**
	 * 下载文件内容
	 * 
	 * @param cloudFile
	 * @return
	 */
	private boolean downloadFileContext(LocalFile cloudFile) {
		Log.d(TAG, "downloadFileContext " + cloudFile.getAbsolutePath());
		final String tmpFile = tmpDir + File.separator + cloudFile.getfId();
		final String file = root + cloudFile.getAbsolutePath();
		boolean reslut = false;
		long sum = 0;
		try {
			long fileStart = checkTempFile(tmpFile);
			// 如果文件下载完成没有移动到目录路径
			if (fileStart == cloudFile.getLength()) {
				if (new FileMV(tmpFile, file).mv()) {
					return true;
				} else {
					fileStart = 0;
					// 重新下载
				}
			}

			StatusMent.setProperty(StatusMent.DOWNLOAD_SIZE, fileStart);
			DownloadInputStream in = null;
			FileOutputStream out = null;
			if (fileStart > 0) {
				Log.d(TAG, "continue download file start:" + fileStart);
				sum = fileStart;
				in = fsApi.download(cloudFile, fileStart);
				if (in.getLength() + sum != cloudFile.getLength()) {
					// 下载返回来的文件大小与要下载的大小不一致,可能文件被改了
					return true;
				}
				out = new FileOutputStream(tmpFile, true);
			} else {
				Log.d(TAG, "new download file start:0");
				in = fsApi.download(cloudFile);
				if (in.getLength() != cloudFile.getLength()) {
					// 下载返回来的文件大小与要下载的大小不一致,可能文件被改了
					return true;
				}
				out = new FileOutputStream(tmpFile);
			}

			if (in != null && (in.getLength() == -1)) {
				/*
				 * || !in.getContentMd5() .equalsIgnoreCase (cloudFile.getMd5())
				 */
				// 文件被删除了,可能之前有临时文件 删除,或者md5不对的
				Log.w(TAG, "cloudfile is delete can not down");
				final File file2 = new File(tmpFile);
				file2.delete();
				in.close();
				out.close();
				return true;
			}

			if (in != null) {
				final byte[] buffer = new byte[1024 * 1024];
				int len = -1;

				while (-1 != (len = in.read(buffer))) {
					StatusMent.setProperty(StatusMent.DOWNLOAD_SIZE, sum);
					out.write(buffer, 0, len);
					sum += len;
					if (sum == cloudFile.getLength()) {
						break;
					}
				}
				out.flush();
				out.close();
				if (sum == cloudFile.getLength()) {
					if (new FileMV(tmpFile, file).mv()) {
						reslut = true;
					}
				}
				in.close();
				Log.i(TAG, "download " + cloudFile.getParentPath() + " success");
			}
		} catch (Exception e) {
			Log.e(TAG, "downloadFileContext error", e);
		}
		return reslut;
	}

	/**
	 * 检查临时文件
	 * 
	 * @param file
	 * @return
	 */
	private long checkTempFile(String file) {
		final File f = new File(file);
		if (f.exists()) {
			return f.length();
		} else {
			return -1;
		}
	}

	protected LocalFile getCloudFile(LocalFile downloadFile) throws ApiException {
		return fsApi.getMeta(downloadFile.getAbsolutePath());
	}

	protected LocalFile getLocalFile(LocalFile downloadFile) throws ApiException {
		final File file = new File(root, downloadFile.getAbsolutePath());
		if (file.exists()) {
			final LocalFile localFile = new LocalFile();
			localFile.setDir(file.isDirectory());
			if (file.isDirectory()) {
				localFile.setLength(0);
			} else {
				localFile.setLength(file.length());
			}
			localFile.setMtime(file.lastModified());
			localFile.setPath(downloadFile.getAbsolutePath());
			return localFile;
		}
		return null;
	}

}
