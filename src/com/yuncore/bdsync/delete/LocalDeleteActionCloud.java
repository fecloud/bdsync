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
public class LocalDeleteActionCloud implements DeleteOperate {

	protected String root;

	private FSApi fsApi;

	protected volatile boolean flag;
	
	private LocalFileDeleteDao localFileDeleteDao ;

	private CloudFileDao cloudFileDao;

	protected List<DeleteCheckFileStep> steps = new ArrayList<DeleteCheckFileStep>();

	/**
	 * @param root
	 */
	public LocalDeleteActionCloud(String root) {
		super();
		this.root = root;
		this.localFileDeleteDao = new LocalFileDeleteDao();
		this.cloudFileDao = new CloudFileDao();
		steps.add(new DeteleCheckLocal());
		steps.add(new DeleteFile());
		// steps.add(new DeteleCheckMtime());
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
			checkAndDelete(deleteFile);
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
	private void checkAndDelete(LocalFile file) {
		LocalFile compareFile = null;
		try {
			compareFile = getCompareFile(file);
		} catch (Exception e) {
			return;
		}
		for (DeleteCheckFileStep step : steps) {
			if (!step.check(file, compareFile, this)) {
				break;
			}
		}

	}

	/**
	 * 删除真正要删除的文件
	 * 
	 * @param deleteFile
	 * @return
	 * @throws Exception
	 */
	public boolean deleteFile(LocalFile deleteFile) {
		if (null == fsApi) {
			fsApi = new FSApiImple();
		}

		boolean result = false;
		CloudRmResult rmResult;
		try {
			rmResult = fsApi.rm(deleteFile.getAbsolutePath());
			if (rmResult != null) {
				result = rmResult.getErrno() == 0;
			}
		} catch (ApiException e) {
			Log.e(getTag(), "", e);
		}

		if (result) {
			Log.w(getTag(), "deleteFile " + deleteFile.getAbsolutePath()
					+ " success");
		} else {
			Log.w(getTag(), "deleteFile " + deleteFile.getAbsolutePath()
					+ " fail");
		}
		return result;
	}

	/**
	 * 删除数据库记录
	 * 
	 * @param deleteFile
	 * @return
	 */
	public boolean deleteRecord(LocalFile deleteFile) {
		return localFileDeleteDao.delete(deleteFile);
	}

	protected LocalFile query() {
		return localFileDeleteDao.query();
	}

	protected LocalFile getCompareFile(LocalFile deleteFile)
			throws ApiException {
		if (null == fsApi) {
			fsApi = new FSApiImple();
		}

		return fsApi.getMeta(deleteFile.getAbsolutePath());
	}

	public String getTag() {
		return "LocalDeleteActionCloud";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.delete.DeleteOperate#getDeleteStatus()
	 */
	@Override
	public boolean getDeleteStatus() {
		return flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.DeleteOperate#deleteAnotherRecord(com.yuncore
	 * .bdsync.entity.LocalFile)
	 */
	@Override
	public boolean deleteAnotherRecord(LocalFile file) {
		// 如果云端最后的列表里面有本地删除的文件,也删除了,以名下次对比的时候发现删除了,再来一次删除本地文件
		return cloudFileDao.deleteByFid(file.getfId());
	}

}
