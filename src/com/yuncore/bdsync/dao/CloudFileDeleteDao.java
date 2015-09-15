package com.yuncore.bdsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.util.Log;

public class CloudFileDeleteDao extends DownloadDao {

	@Override
	public String getTableName() {
		return "clouddelete";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.DownloadDao#query(long, int)
	 */
	@Override
	public List<LocalFile> query(long start, int num) {
		try {
			final List<LocalFile> list = new ArrayList<LocalFile>();
			final String sql = String.format("SELECT * FROM %s ORDER BY isdir ASC LIMIT %s,%s", getTableName(), start,
					num);

			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection.prepareStatement(sql);
			final ResultSet resultSet = prepareStatement.executeQuery();
			while (resultSet.next()) {
				list.add(buildLocalFile(resultSet));
			}
			resultSet.close();
			prepareStatement.close();
			connection.close();
			return list;
		} catch (SQLException e) {
			Log.e(getTag(), "query error", e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.DownloadDao#getTag()
	 */
	@Override
	public String getTag() {
		return "CloudFileDeleteDao";
	}

}
