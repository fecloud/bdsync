/**
 * @(#) DownLoadCheckLocalFile.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.down;

import java.io.File;

import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.util.FileUtil;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>DownLoadCheckLocalFile</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DownLoadCheckLocalFile implements DownLoadCheckFileStep {

	private String root;

	private LocalFileDao localFileDao;
	/**
	 * @param root
	 */
	public DownLoadCheckLocalFile(String root) {
		super();
		this.root = root;
		this.localFileDao = new LocalFileDao();
	}

	private static final boolean mkdir(String root, String path) {
		try {
			final File file = new File(root, path);
			return file.mkdirs();
		} catch (Exception e) {
		}
		return false;
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
		final LocalFile loalFile = FileUtil.getLocalFile(root, downloadFile.getAbsolutePath());
		// 本地文件不在
		if (loalFile == null) {
			if (downloadFile.isDir()) {
				// 如果是文件夹,直接建立
				if (mkdir(root, downloadFile.getAbsolutePath())) {
					addDirToLocalFile(downloadFile);
					Log.d("DownLoadCheckLocalFile", "mkdir:" + downloadFile.getAbsolutePath());
					downloadOperate.deleteRecord(downloadFile);
				}
				return false;
			}
			return true;
		}

		if (downloadFile.isDir() && loalFile.isDir()) {
			// 两个都是文件夹
			downloadOperate.deleteRecord(downloadFile);
			return false;
		}

		if (downloadFile.isFile() && loalFile.isFile()) {
			if (downloadFile.getLength() == loalFile.getLength()) {
				// 两个文件大小一样,进入文件修改时间检查
				downloadOperate.deleteRecord(downloadFile);
				return false;
			} else {
				// 如果要下载的文件跟本地一样大小,当要下载的文件修改时间大于本地文件,可以下载
				if (downloadFile.getMtime() > loalFile.getMtime()) {
					return true;
				} else {
					// 不能下载
					downloadOperate.deleteRecord(downloadFile);
				}
			}
		}
		// 两个文件大小不一样或者类型不一样
		downloadOperate.deleteRecord(downloadFile);
		return false;
	}
	
	/**
	 * 往本地列表里面添加一条数据,以免本直列表再一次上传
	 * @param downloadFile
	 */
	private final void addDirToLocalFile(LocalFile downloadFile){
		downloadFile.setNewest(false);
		localFileDao.insert(downloadFile);
	}

}
