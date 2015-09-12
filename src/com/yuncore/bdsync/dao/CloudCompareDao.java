/**
 * 
 */
package com.yuncore.bdsync.dao;

import com.yuncore.bdsync.Environment;


/**
 * @author ouyangfeng
 * 
 */
public class CloudCompareDao extends LocalCompareDao {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.db.LocalCompareDao#getTableName()
	 */
	@Override
	public String getTableName() {
		return "cloudcompare";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.LocalCompareDao#getBeforeTableName()
	 */
	@Override
	public String getBeforeTableName() {
		return "cloudfile";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.LocalCompareDao#getNowTableName()
	 */
	@Override
	public String getNowTableName() {
		return "cloudfile_tmp";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.LocalCompareDao#getSameTableName()
	 */
	@Override
	public String getSameTableName() {
		return "cloudcomparesame";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.LocalCompareDao#getActionTableName()
	 */
	@Override
	public String getActionTableName() {
		return "clouddownload";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.LocalCompareDao#getDeleteTableName()
	 */
	@Override
	public String getDeleteTableName() {
		return "clouddelete";
	}

	@Override
	public String getSession() {
		return Environment.CLOUDLIST_SESSION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.LocalCompareDao#getTag()
	 */
	@Override
	public String getTag() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected String getCopyTableDataSql() {
		return "INSERT INTO %s SELECT id,path,length,isdir,mtime,fid,md5,session FROM %s";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.LocalCompareDao#createCompareSql()
	 */
	@Override
	public synchronized boolean createCompareSql() {
		String sql = String.format(
				"CREATE TABLE %s (id INTEGER, path TEXT , length INTEGER, isdir INTEGER, mtime INTEGER, fid TEXT, md5 TEXT, session INTEGER);",
				getTableName());
		return executeSQL(sql);
	}
}
