package com.yuncore.bdsync.dao;

public class CloudHistoryDao extends LocalHistoryDao {

	@Override
	public String getTableName() {
		return "cloudhistory";
	}
	
}
