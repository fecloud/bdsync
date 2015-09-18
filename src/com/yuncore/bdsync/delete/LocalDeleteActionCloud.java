/**
 * @(#) LocalDeleteActionCloud.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.delete;

import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.CloudFileDao;
import com.yuncore.bdsync.dao.LocalFileDeleteDao;
import com.yuncore.bdsync.entity.CloudRmResult;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>LocalDeleteActionCloud</code>
 * <p>
 * 本地删除了,执行删除云端文件
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class LocalDeleteActionCloud {

	protected String root;

	private FSApi fsApi;

	protected volatile boolean flag;

	protected List<DeleteCheckFileStep> steps = new ArrayList<DeleteCheckFileStep>();

	/**
	 * @param root
	 */
	public LocalDeleteActionCloud(String root) {
		super();
		steps.add(new DeteleCheckSize());
		steps.add(new DeteleCheckMtime());
	}

	/**
	 * @return the root
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * @param root
	 *            the root to set
	 */
	public void setRoot(String root) {
		this.root = root;
	}

	public synchronized boolean deletes() {
		LocalFile deleteFile = null;
		while (null != (deleteFile = query())) {
			try {
				if (checkAndDelete(deleteFile)) {
					deleteRecord(deleteFile);
				}
			} catch (Exception e) {
				// 去云端查看文件存在时可能会出错
			}
		}

		return true;
	}

	/**
	 * 检查和删除文件
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private boolean checkAndDelete(LocalFile file) throws Exception {
		final LocalFile deleteFile = file;
		final LocalFile compareFile = getCompareFile(deleteFile);
		if (compareFile != null) {
			for (DeleteCheckFileStep step : steps) {
				if (!step.check(deleteFile, compareFile)) {
					return deleteFile(deleteFile);
				}
			}

		} else {
			Log.d(getTag(), "compareFile not exists");
		}
		return true;
	}

	/**
	 * 删除真正要删除的文件
	 * 
	 * @param deleteFile
	 * @return
	 * @throws Exception
	 */
	protected boolean deleteFile(LocalFile deleteFile) throws Exception {
		if (null == fsApi) {
			fsApi = new FSApiImple();
		}

		final CloudRmResult rmResult = fsApi.rm(deleteFile.getAbsolutePath());
		boolean result = false;
		if (rmResult != null) {
			result = rmResult.getErrno() == 0;
		}
		if (result) {
			Log.w(getTag(), "deleteFile:" + deleteFile.getAbsolutePath() + " success");
		} else {
			Log.w(getTag(), "deleteFile:" + deleteFile.getAbsolutePath() + " fail");
		}
		return result;
	}

	/**
	 * 删除数据库记录
	 * 
	 * @param deleteFile
	 * @return
	 */
	protected boolean deleteRecord(LocalFile deleteFile) {
		final LocalFileDeleteDao fileDeleteDao = new LocalFileDeleteDao();
		boolean result = fileDeleteDao.deleteByFid(deleteFile.getfId());
		if (result) {
			// 如果云端最后的列表里面有本地删除的文件,也删除了,以名下次对比的时候发现删除了,再来一次删除本地文件
			final CloudFileDao cloudFileDao = new CloudFileDao();
			cloudFileDao.deleteByFid(deleteFile.getfId());
		}
		return result;
	}

	protected LocalFile query() {
		final LocalFileDeleteDao localFileDeleteDao = new LocalFileDeleteDao();
		return localFileDeleteDao.query();
	}

	protected LocalFile getCompareFile(LocalFile deleteFile) throws ApiException {
		if (null == fsApi) {
			fsApi = new FSApiImple();
		}

		return fsApi.getMeta(deleteFile.getAbsolutePath());
	}

	public String getTag() {
		return "LocalDeleteActionCloud";
	}

}
