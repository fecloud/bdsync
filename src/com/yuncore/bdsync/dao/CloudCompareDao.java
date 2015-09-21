/**
 * 
 */
package com.yuncore.bdsync.dao;


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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.LocalCompareDao#getTag()
	 */
	@Override
	public String getTag() {
		return this.getClass().getSimpleName();
	}

}
