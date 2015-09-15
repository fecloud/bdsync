package com.yuncore.bdsync.dao;

import java.util.List;

import com.yuncore.bdsync.entity.CloudFile;

public class CloudFileDao extends LocalFileDao {

	private static String TAG = "CloudFileDao";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.BaseDao#getTableName()
	 */
	@Override
	public String getTableName() {
		return "cloudfile";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.BaseDao#getTag()
	 */
	@Override
	public String getTag() {
		return TAG;
	}

	public synchronized boolean insertCloudFileCache(List<CloudFile> files) {
		if (size < CACHE_SIZE) {
			size += files.size();
			return cache.addAll(files);
		} else {
			size += files.size();
			cache.addAll(files);
			return insertAllCacaheFlush();
		}
	}

}
