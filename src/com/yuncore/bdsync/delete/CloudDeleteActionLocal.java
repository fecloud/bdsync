/**
 * @(#) CloudDeleteActionLocal.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.delete;

import java.io.File;
import java.util.List;

import com.yuncore.bdsync.dao.CloudFileDeleteDao;
import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.entity.LocalFile;
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
	 * @see com.yuncore.bdsync.delete.LocalDeleteActionCloud#queryList(int)
	 */
	@Override
	protected List<LocalFile> queryList(int size) {
		final CloudFileDeleteDao cloudFileDeleteDao = new CloudFileDeleteDao();
		return cloudFileDeleteDao.query(0, size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.LocalDeleteActionCloud#fileExists(com.yuncore
	 * .bdsync.entity.LocalFile)
	 */
	@Override
	protected boolean fileExists(LocalFile file) throws Exception {
		final File targetFile = new File(getRoot(), file.getAbsolutePath());
		return targetFile.exists();
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
			final LocalFileDao localFileDao = new LocalFileDao();
			localFileDao.deleteByFid(deleteFile.getfId());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.LocalDeleteActionCloud#fileSizeSame(com.yuncore
	 * .bdsync.entity.LocalFile)
	 */
	@Override
	protected boolean fileSizeSame(LocalFile file) {
		final File destFile = new File(getRoot(), file.getAbsolutePath());
		if (destFile.length() == file.getLength()) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.LocalDeleteActionCloud#fileMtime(com.yuncore
	 * .bdsync.entity.LocalFile)
	 */
	@Override
	protected boolean fileMtime(LocalFile file) {
		final long destTime = new File(getRoot(), file.getAbsolutePath())
				.lastModified() / 1000;// 精确到秒
		final long targetTime = file.getMtime();
		if (destTime <= targetTime) {
			// 云端文件修改时间大于等于云端,则可以删了
			return true;
		}
		return false;
	}
}
