/**
 * @(#) CloudDeleteActionLocal.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.delete;

import java.io.File;

import com.yuncore.bdsync.dao.CloudFileDeleteDao;
import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>CloudDeleteActionLocal</code>
 * <p>
 * 云端删除了,执行删除本地文件
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class CloudDeleteActionLocal extends LocalDeleteActionCloud {

	/**
	 * @param root
	 */
	public CloudDeleteActionLocal(String root) {
		super(root);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.delete.LocalDeleteActionCloud#getTag()
	 */
	@Override
	public String getTag() {
		return "CloudDeleteActionLocal";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.delete.LocalDeleteActionCloud#query()
	 */
	@Override
	protected LocalFile query() {
		final CloudFileDeleteDao cloudFileDeleteDao = new CloudFileDeleteDao();
		return cloudFileDeleteDao.query();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.LocalDeleteActionCloud#deleteFile(com.yuncore
	 * .bdsync.entity.LocalFile)
	 */
	@Override
	protected boolean deleteFile(LocalFile deleteFile) throws Exception {
		final File file = new File(getRoot(), deleteFile.getAbsolutePath());
		final boolean result = file.delete();
		if (result) {
			Log.w(getTag(), "deleteFile:" + file.getAbsolutePath() + " success");
		} else {
			Log.w(getTag(), "deleteFile:" + file.getAbsolutePath() + " fail");
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.LocalDeleteActionCloud#deleteRecord(com.yuncore
	 * .bdsync.entity.LocalFile)
	 */
	@Override
	protected boolean deleteRecord(LocalFile deleteFile) {
		final CloudFileDeleteDao fileDeleteDao = new CloudFileDeleteDao();
		boolean result = fileDeleteDao.deleteByFid(deleteFile.getfId());
		if (result) {
			// 如果云端最后的列表里面有本地删除的文件,也删除了,以名下次对比的时候发现删除了,再来一次删除本地文件
			final LocalFileDao localFileDao = new LocalFileDao();
			localFileDao.deleteByFid(deleteFile.getfId());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.delete.LocalDeleteActionCloud#getCompareFile(com.
	 * yuncore.bdsync.entity.LocalFile)
	 */
	@Override
	protected LocalFile getCompareFile(LocalFile deleteFile) throws ApiException {
		final File file = new File(getRoot(), deleteFile.getAbsolutePath());
		if (file.exists()) {
			final LocalFile localFile = new LocalFile();
			localFile.setDir(file.isDirectory());
			if (file.isDirectory()) {
				localFile.setLength(0);
			} else {
				localFile.setLength(file.length());
			}
			localFile.setMtime(file.lastModified());
			localFile.setPath(deleteFile.getAbsolutePath());
			return localFile;
		}
		return null;
	}
}
