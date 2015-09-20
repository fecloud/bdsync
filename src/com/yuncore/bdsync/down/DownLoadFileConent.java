/**
 * @(#) DownLoadFileConent.java Created on Sep 19, 2015
 *
 * 
 */
package com.yuncore.bdsync.down;

import java.io.File;
import java.io.FileOutputStream;

import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.DownloadInputStream;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.util.FileMV;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>DownLoadFileConent</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DownLoadFileConent implements DownLoadCheckFileStep {

	private static final String TAG = "DownLoadFileConent";

	private String root;

	private String tmpDir;

	private FSApi fsApi;

	/**
	 * @param root
	 */
	public DownLoadFileConent(String root, String tmpDir, FSApi fsApi) {
		super();
		this.root = root;
		this.tmpDir = tmpDir;
		this.fsApi = fsApi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.down.DownLoadCheckFileStep#check(com.yuncore.bdsync
	 * .entity.LocalFile, com.yuncore.bdsync.entity.LocalFile,
	 * com.yuncore.bdsync.entity.LocalFile,
	 * com.yuncore.bdsync.down.DownloadOperate)
	 */
	@Override
	public boolean check(LocalFile downloadFile, LocalFile cloudFile,
			LocalFile loalFile, DownloadOperate downloadOperate) {
		if (downloadFile.isDir()) {
			mkdirLocal(downloadFile, downloadOperate);
		} else if (downloadFile(downloadFile, downloadOperate)) {
			// 成功下载后删了记录
			downloadOperate.deleteRecord(downloadFile);
		}
		return true;
	}

	private final boolean downloadFile(LocalFile downloadFile,
			DownloadOperate downloadOperate) {
		Log.d(TAG, "downloadFileContext " + downloadFile.getAbsolutePath());
		final String tmpFile = tmpDir + File.separator + downloadFile.getfId();
		final String finalFile = root + downloadFile.getAbsolutePath();

		boolean reslut = false;
		long sum = 0;
		try {
			long fileStart = checkTempFile(tmpFile);
			// 如果文件下载完成没有移动到目录路径
			if (fileStart == downloadFile.getLength()) {
				if (new FileMV(tmpFile, finalFile).mv()) {
					return true;
				} else {
					// 重新下载
					fileStart = 0;
				}
			}

			StatusMent.setProperty(StatusMent.DOWNLOAD_SIZE, fileStart);
			DownloadInputStream in = null;
			FileOutputStream out = null;

			if (fileStart > 0) {
				Log.d(TAG, "continue download file start:" + fileStart);
				sum = fileStart;
				in = fsApi.download(downloadFile, fileStart);
				if (in.getLength() + sum != downloadFile.getLength()) {
					// 下载返回来的文件大小与要下载的大小不一致,可能文件被改了
					return true;
				}
				out = new FileOutputStream(tmpFile, true);
			} else {
				Log.d(TAG, "new download file start:0");
				in = fsApi.download(downloadFile);
				if (in.getLength() != downloadFile.getLength()) {
					// 下载返回来的文件大小与要下载的大小不一致,可能文件被改了
					return true;
				}
				out = new FileOutputStream(tmpFile);
			}

			if (in != null && (in.getLength() == -1)) {
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

				while (downloadOperate.getDownLoadStatus()
						&& -1 != (len = in.read(buffer))) {
					StatusMent.setProperty(StatusMent.DOWNLOAD_SIZE, sum);
					out.write(buffer, 0, len);
					sum += len;
					if (sum == downloadFile.getLength()) {
						break;
					}
				}
				out.flush();
				out.close();
				if (sum == downloadFile.getLength()) {
					if (new FileMV(tmpFile, finalFile).mv()) {
						reslut = true;
					}
				}
				in.close();
				Log.i(TAG, "download " + downloadFile.getParentPath()
						+ " success");
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

	/**
	 * 在本地新建文件夹
	 * 
	 * @param downloadFile
	 * @param downloadOperate
	 */
	private final void mkdirLocal(LocalFile downloadFile,
			DownloadOperate downloadOperate) {
		try {
			final File file = new File(root, downloadFile.getAbsolutePath());
			if (file.exists()) {
				downloadOperate.deleteRecord(downloadFile);
			} else {
				final boolean result = file.mkdirs();
				if (result) {
					downloadOperate.deleteRecord(downloadFile);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "mkdirLocal", e);
		}
	}

}
