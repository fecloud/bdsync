/**
 * @(#) ProcessDao.java Created on Sep 12, 2015
 *
 * 
 */
package com.yuncore.bdsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.yuncore.bdsync.entity.SyncProcess;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>SyncProcessDao</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class SyncProcessDao extends BaseDao {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.BaseDao#getTableName()
	 */
	@Override
	public String getTableName() {
		return "process";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.BaseDao#getTag()
	 */
	@Override
	public String getTag() {
		return "SyncProcessDao";
	}

	/**
	 * 取当前任务
	 * 
	 * @return
	 */
	public synchronized SyncProcess getSyncProcess() {
		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection
					.prepareStatement(String.format(
							"SELECT id,process FROM %s LIMIT 0,1",
							getTableName()));

			final ResultSet resultSet = prepareStatement.executeQuery();
			SyncProcess result = null;
			if (resultSet.next()) {
				result = new SyncProcess();
				result.setProcess(resultSet.getString("process"));
				result.setName(resultSet.getString("name"));
			}
			prepareStatement.close();
			connection.close();
			return result;
		} catch (SQLException e) {
			Log.e(getTag(), "getProcess", e);
		}
		return null;
	}

	/**
	 * 设置当前任务
	 * 
	 * @param process
	 * @return
	 */
	public synchronized boolean setSyncProcess(SyncProcess process) {
		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection
					.prepareStatement(String.format(
							"REPLACE INTO %s (id,process,name) VALUES (1,?,?)",
							getTableName()));

			prepareStatement.setString(1, process.getProcess());
			prepareStatement.setString(2, process.getName());

			connection.setAutoCommit(false);

			final boolean result = prepareStatement.executeUpdate() > 0;
			prepareStatement.close();

			connection.commit();
			connection.setAutoCommit(true);
			connection.close();
			return result;
		} catch (SQLException e) {
			Log.e(getTag(), "setProcess", e);
		}
		return false;
	}

}
