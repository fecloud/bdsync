/**
 * @(#) SyncStatusDao.java Created on Sep 14, 2015
 *
 * 
 */
package com.yuncore.bdsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.yuncore.bdsync.util.Log;

/**
 * The class <code>SyncStatusDao</code>
 * <p>
 * 同步服务状态
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class SyncStatusDao extends BaseDao {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.BaseDao#getTableName()
	 */
	@Override
	public String getTableName() {
		return "sync_status";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.BaseDao#getTag()
	 */
	@Override
	public String getTag() {
		return "SyncStatusDao";
	}

	public synchronized String getSyncStatus() {
		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection
					.prepareStatement(String.format(
							"SELECT status FROM %s LIMIT 0,1", getTableName()));

			final ResultSet resultSet = prepareStatement.executeQuery();
			String result = null;
			if (resultSet.next()) {
				result = resultSet.getString("status");
			}
			prepareStatement.close();
			connection.close();

			if (result != null) {
				return result;
			}

		} catch (SQLException e) {
			Log.e(getTag(), "getSyncStatus", e);
		}
		return "1";
	}

	public synchronized boolean setSyncStatus(String status) {
		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection
					.prepareStatement(String.format(
							"REPLACE INTO %s (id,status) VALUES (1,?)",
							getTableName()));

			prepareStatement.setString(1, status);

			connection.setAutoCommit(false);

			final boolean result = prepareStatement.executeUpdate() > 0;
			prepareStatement.close();

			connection.commit();
			connection.setAutoCommit(true);
			connection.close();
			return result;
		} catch (SQLException e) {
			Log.e(getTag(), "setSyncStatus", e);
		}
		return false;
	}

}
