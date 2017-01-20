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
import com.yuncore.bdsync.util.FileUtil;
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

	private CloudFileDeleteDao cloudFileDeleteDao;

	private LocalFileDao localFileDao;

	/**
	 * @param root
	 */
	public CloudDeleteActionLocal(String root) {
		super(root, null);
		localFileDao = new LocalFileDao();
		cloudFileDeleteDao = new CloudFileDeleteDao();
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
	public boolean deleteFile(LocalFile deleteFile) {
		boolean result = false;
		final File file = new File(getRoot(), deleteFile.getAbsolutePath());
		try {
			if (file.exists()) {
				// 如果要删除的文件存在
				result = file.delete();
			} else {
				// 如果要删除的文件不见了
				result = true;
			}
		} catch (Exception e) {
			Log.e(getTag(), "", e);
		}
		if (result) {
			Log.w(getTag(), "deleteFile " + file.getAbsolutePath() + " success");
		} else {
			Log.w(getTag(), "deleteFile " + file.getAbsolutePath() + " fail");
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
	public boolean deleteRecord(LocalFile deleteFile) {
		return cloudFileDeleteDao.delete(deleteFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.delete.LocalDeleteActionCloud#getCompareFile(com.
	 * yuncore.bdsync.entity.LocalFile)
	 */
	@Override
	protected LocalFile getCompareFile(LocalFile deleteFile)
			throws ApiException {
		return FileUtil.getLocalFile(getRoot(), deleteFile.getAbsolutePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.LocalDeleteActionCloud#deleteAnotherRecord(
	 * com.yuncore.bdsync.entity.LocalFile)
	 */
	@Override
	public boolean deleteAnotherRecord(LocalFile file) {
		// 如果云端最后的列表里面有本地删除的文件,也删除了,以名下次对比的时候发现删除了,再来一次删除本地文件
		return localFileDao.deleteByFid(file.getfId());
	}

}
