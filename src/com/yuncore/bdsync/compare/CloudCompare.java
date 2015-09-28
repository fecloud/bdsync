/**
 * 
 */
package com.yuncore.bdsync.compare;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.dao.CloudCompareDao;
import com.yuncore.bdsync.dao.CloudFileDao;
import com.yuncore.bdsync.dao.CloudHistoryDao;
import com.yuncore.bdsync.util.Log;

/**
 * @author ouyangfeng
 * 
 */
public class CloudCompare extends LocalCompare {

	public CloudCompare() {
		compareDao = new CloudCompareDao();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.compare.LocalCompare#needCompareBefore()
	 */
	@Override
	public synchronized boolean needCompareBefore() {
		final CloudFileDao cloudFileDao = new CloudFileDao();
		return cloudFileDao.count() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.compare.LocalCompare#addNewHistory()
	 */
	@Override
	public synchronized boolean addNewHistory() {
		final CloudHistoryDao cloudHistoryDao = new CloudHistoryDao();
		final long time = Long.parseLong(Environment.getCloudlist());
		return cloudHistoryDao.insert(time);
	}
	
	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.compare.LocalCompare#printCounts()
	 */
	@Override
	public void printCounts() {
		final long actionNums = compareDao.count(compareDao.getActionTableName());
		Log.w(getTag(), "需下载" + actionNums);
		final long deleteNums = compareDao.count(compareDao.getDeleteTableName());
		Log.w(getTag(), "删除了" + deleteNums);
	}
	
	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.compare.LocalCompare#getTag()
	 */
	@Override
	public String getTag() {
		return "CloudCompare";
	}
	
}
