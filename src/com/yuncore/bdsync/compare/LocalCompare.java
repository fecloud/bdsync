package com.yuncore.bdsync.compare;

import java.util.List;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.dao.LocalCompareDao;
import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.dao.LocalHistoryDao;
import com.yuncore.bdsync.util.Log;

public class LocalCompare {

	protected LocalCompareDao compareDao;

	public LocalCompare() {
		compareDao = new LocalCompareDao();
	}

	protected long getSession() {
		return Long.parseLong(Environment.getLocallistSession());
	}

	/**
	 * 是否需要和以前的数据进行对比
	 * 
	 * @return
	 */
	public synchronized boolean needCompareBefore() {
		final LocalFileDao localFileDao = new LocalFileDao();
		return localFileDao.count() > 0;
	}

	/**
	 * 把最新的扫描时间加入数据库中
	 * 
	 * @return
	 */
	public synchronized boolean addNewHistory() {
		final LocalHistoryDao localHistoryDao = new LocalHistoryDao();
		final long time = Long.parseLong(Environment.getLocallistSession());
		return localHistoryDao.insert(time);
	}

	/**
	 * 分发不同的部分到对应的表
	 * 
	 * @return
	 */
	private synchronized boolean dispathDeleteAndAction() {
		final List<Long> ss = compareDao.groupBySession(compareDao
				.getTableName());
		if (ss != null && !ss.isEmpty()) {
			final long cs = getSession();

			if (ss.size() == 1) {
				final long s = ss.get(0);
				if (s == cs) {
					// 把最新的扫描结果放上action表
					return compareDao.copyTableData(compareDao.getTableName(),
							compareDao.getActionTableName());
				} else {
					// 把最新的扫描结果放上delete表
					return compareDao.copyTableData(compareDao.getTableName(),
							compareDao.getDeleteTableName());
				}

			} else if (ss.size() == 2) {
				if (ss.contains(cs)) {
					// 把最新的扫描结果放上action表
					compareDao.copyTableData(compareDao.getTableName(),
							compareDao.getActionTableName(),
							String.format("session=%s", cs));
					
					// 把最新的扫描结果放上delete表
					compareDao.copyTableData(compareDao.getDeleteTableName(),
							compareDao.getActionTableName());
				}
			}
		}
		return false;
	}

	public synchronized boolean compare() {

		compareDao.clearTables();

		if (needCompareBefore()) {
			Log.d(getTag(), "before has data");
			compareDao.copyBeforeAndNow();
			compareDao.findSame();
			compareDao.deleteSame();
			dispathDeleteAndAction();
		} else {
			// 从来没有同步过,本地上传,云端下载
			Log.d(getTag(), "before no data");
			compareDao.copyTableData(compareDao.getNowTableName(),
					compareDao.getActionTableName());
		}

		compareDao.delete(compareDao.getBeforeTableName());
		compareDao.rename(compareDao.getNowTableName(),
				compareDao.getBeforeTableName());

		addNewHistory();

		compareDao.clearTables();

		return true;
	}

	public String getTag() {
		return "LocalCompare";
	}

}
