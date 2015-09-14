package com.yuncore.bdsync.dao;

import java.util.List;

import com.yuncore.bdsync.entity.LocalFile;

public class LocalFileDeleteDao extends UploadDao {

	@Override
	public String getTableName() {
		return "localdelete";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.DownloadDao#query(long, int)
	 */
	@Override
	public List<LocalFile> query(long start, int num) {
		return super.query(start, num);
	}

}
