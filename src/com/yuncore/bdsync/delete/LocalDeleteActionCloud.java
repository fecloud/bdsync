/**
 * @(#) LocalDeleteActionCloud.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.delete;

import java.util.List;

import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.LocalFileDeleteDao;
import com.yuncore.bdsync.entity.CloudFile;
import com.yuncore.bdsync.entity.CloudRmResult;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;

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

	private CloudFile cloudFile;

	/**
	 * @param root
	 */
	public LocalDeleteActionCloud(String root) {
		super();
		this.root = root;
		this.fsApi = new FSApiImple();
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
		List<LocalFile> list = null;
		int length = 0;

		LocalFile deleteFile = null;
		while (null != (list = queryList(10))) {
			length = list.size();
			if (length != 0) {
				for (int i = 0; i < length; i++) {
					deleteFile = list.get(i);
					try {
						if (checkAndDelete(deleteFile)) {
							if (!deleteRecord(deleteFile)) {
								i--;
							}
						} else {
							i--;
						}
					} catch (Exception e) {
						// 去云端查看文件存在时可能会出错
						i--;
					}
				}
			} else {
				return true;
			}
		}

		return false;
	}

	/**
	 * 检查和删除文件
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private boolean checkAndDelete(LocalFile file) throws Exception {
		if (fileExists(file)) {
			if (file.isDir()) {
				// 如果是文件夹
				return deleteFile(file);
			} else {
				// 如果是文件
				if (fileSizeSame(file)) {
					return deleteFile(file);
				} else { // 文件大小不一样
					if (fileMtime(file)) {
						return deleteFile(file);
					} else {
						return true;
					}
				}
			}
		} else {
			// 文件不在了,直接删除
			return true;
		}
	}

	/**
	 * 检查要删除的文件大于是否一致
	 * 
	 * @param file
	 * @return
	 */
	protected boolean fileSizeSame(LocalFile file) {
		if (cloudFile.getLength() == file.getLength()) {
			return true;
		}
		return false;
	}

	/**
	 * 检查文件修改时间
	 * 
	 * @param file
	 * @return
	 */
	protected boolean fileMtime(LocalFile file) {
		final long destTime = cloudFile.getMtime();// 精确到秒
		final long targetTime = file.getMtime();
		if (destTime <= targetTime) {
			// 本地文件修改时间大于等于云端,则可以删了
			return true;
		}
		return false;
	}

	/**
	 * 检查要删除的文件还在不在
	 * 
	 * @param file
	 * @return
	 * @throws ApiException
	 */
	protected boolean fileExists(LocalFile file) throws Exception {
		this.cloudFile = fsApi.exists(file.getAbsolutePath(), file.isDir());
		return cloudFile != null;
	}

	/**
	 * 删除真正要删除的文件
	 * 
	 * @param deleteFile
	 * @return
	 * @throws Exception
	 */
	protected boolean deleteFile(LocalFile deleteFile) throws Exception {
		final CloudRmResult rmResult = fsApi.rm(deleteFile.getAbsolutePath());
		if (rmResult != null) {
			return rmResult.getErrno() == 0;
		}
		return false;
	}

	/**
	 * 删除数据库记录
	 * 
	 * @param deleteFile
	 * @return
	 */
	protected boolean deleteRecord(LocalFile deleteFile) {
		final LocalFileDeleteDao fileDeleteDao = new LocalFileDeleteDao();
		return fileDeleteDao.deleteByFid(deleteFile.getfId());
	}

	/**
	 * 查询要删除的数据,先查文件
	 * 
	 * @param size
	 * @return
	 */
	protected List<LocalFile> queryList(int size) {
		final LocalFileDeleteDao localFileDeleteDao = new LocalFileDeleteDao();
		return localFileDeleteDao.query(0, size);
	}

	public String getTag() {
		return "LocalDeleteActionCloud";
	}

}
