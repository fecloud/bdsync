/**
 * @(#) CloudFileTmpDao.java Created on 2015年9月8日
 *
 * 
 */
package com.yuncore.bdsync.dao;

/**
 * The class <code>CloudFileTmpDao</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class CloudFileTmpDao extends CloudFileDao {

	public CloudFileTmpDao() {
		executeSQL(
				"CREATE TABLE IF NOT EXISTS cloudfile_tmp (id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT , length INTEGER, isdir INTEGER, mtime INTEGER, fid TEXT, md5 TEXT, session INTEGER);");

	}

	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.dao.CloudFileDao#getTag()
	 */
	@Override
	public String getTag() {
		return "CloudFileTmpDao";
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.CloudFileDao#getTableName()
	 */
	@Override
	public String getTableName() {
		return "cloudfile_tmp";
	}

}
