package com.yuncore.bdsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.yuncore.bdsync.entity.History;
import com.yuncore.bdsync.util.Log;

public class LocalHistoryDao extends BaseDao {

	@Override
	public String getTableName() {
		return "localhistory";
	}

	public synchronized boolean insert(long time) {
		String sql = String.format("INSERT INTO %s (time) VALUES (?)", getTableName());
		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection.prepareStatement(sql);
			prepareStatement.setString(1, "" + new Date().getTime());
			connection.setAutoCommit(false);
			boolean execute = prepareStatement.execute();
			connection.commit();
			connection.setAutoCommit(true);
			connection.close();

			return execute;
		} catch (Exception e) {
			Log.e(getTag(), "", e);
		}
		return false;
	}

	public synchronized List<History> getHistory() {
		return getHistory(0);
	}

	public synchronized List<History> getHistory(int count) {

		String sql = String.format("SELECT id,time FROM %s ORDER by id DESC", getTableName());
		if (count > 0) {
			sql = String.format("SELECT id,time FROM %s ORDER by id DESC LIMIT 0,%s", getTableName(), count);
		}

		final List<History> list = new ArrayList<History>();
		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection.prepareStatement(sql);
			final ResultSet resultSet = prepareStatement.executeQuery();
			History history = null;
			while (resultSet.next()) {
				history = new History();
				history.setTime(resultSet.getLong("time"));
				history.setId(resultSet.getLong("id"));
				list.add(history);
			}
			prepareStatement.close();
			connection.close();
			return list;
		} catch (SQLException e) {
			Log.e(getTag(), "", e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.BaseDao#getTag()
	 */
	@Override
	public String getTag() {
		return getClass().getSimpleName();
	}
}
