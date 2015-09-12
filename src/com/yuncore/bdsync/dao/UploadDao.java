package com.yuncore.bdsync.dao;

public class UploadDao extends DownloadDao{

	@Override
	public String getTableName() {
		return "localupload";
	}
	
}
