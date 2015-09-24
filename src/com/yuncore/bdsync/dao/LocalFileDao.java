package com.yuncore.bdsync.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.util.Log;
import com.yuncore.bdsync.util.Stopwatch;

public class LocalFileDao extends BaseDao {

	protected List<LocalFile> cache = new ArrayList<LocalFile>();

	protected int size;

	protected static final int CACHE_SIZE = 5000;

	@Override
	public String getTableName() {
		return "localfile";
	}

	public synchronized boolean insertAllCacaheFlush() {
		if (size != 0) {
			final boolean result = insertAll();
			if (result) {
				size = 0;
				cache.clear();
			}
			return result;
		} else {
			return true;
		}
	}

	public synchronized boolean insertCache(List<LocalFile> files) {
		if (size < CACHE_SIZE) {
			size += files.size();
			return cache.addAll(files);
		} else {
			size += files.size();
			cache.addAll(files);
			return insertAllCacaheFlush();
		}
	}

	public synchronized boolean insertAll() {
		try {

			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start();
			final Connection connection = getConnection();

			final String sql = String.format(
					"INSERT INTO %s ('path','length','isdir','mtime','fid','md5','newest') VALUES (?,?,?,?,?,?,?)",
					getTableName());
			final PreparedStatement prepareStatement = connection.prepareStatement(sql);

			for (LocalFile f : cache) {
				prepareStatement.setString(1, f.getPath());
				prepareStatement.setLong(2, f.getLength());
				prepareStatement.setBoolean(3, f.isDir());
				prepareStatement.setLong(4, f.getMtime());
				prepareStatement.setString(5, f.toFid());
				prepareStatement.setString(6, f.getMd5());
				prepareStatement.setBoolean(7, f.isNewest());
				prepareStatement.addBatch();
			}
			connection.setAutoCommit(false);
			prepareStatement.executeBatch();

			connection.commit();
			connection.setAutoCommit(true);
			connection.close();

			stopwatch.stop(getTag() + " insertAll " + size);
			return true;
		} catch (SQLException e) {
			Log.e(getTag(), "", e);
		}
		return false;
	}

	/**
	 * 根据fid查询
	 * 
	 * @param fid
	 * @return
	 */
	public LocalFile queryByFid(String fid) {
		final Connection connection = getConnection();

		try {
			final PreparedStatement prepareStatement = connection
					.prepareStatement(String.format("SELECT * FROM %s WHERE fid=?", getTableName()));
			prepareStatement.setString(1, fid);
			final ResultSet executeQuery = prepareStatement.executeQuery();
			LocalFile localFile = null;
			if (executeQuery.next()) {
				localFile = buildLocalFile(executeQuery);
			}

			executeQuery.close();
			prepareStatement.close();
			connection.close();
			return localFile;
		} catch (SQLException e) {
		}
		return null;
	}

	protected LocalFile buildLocalFile(ResultSet resultSet) throws SQLException {
		final LocalFile file = new LocalFile();
		file.setId(resultSet.getInt("id"));
		file.setfId(resultSet.getString("fid"));
		file.setPath(resultSet.getString("path"));
		file.setLength(resultSet.getLong("length"));
		file.setDir(resultSet.getBoolean("isdir"));
		file.setNewest(resultSet.getBoolean("newest"));
		file.setMd5(resultSet.getString("md5"));
		file.setMtime(resultSet.getInt("mtime"));
		return file;
	}

	public boolean deleteByFid(String fid) {
		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection
					.prepareStatement(String.format("DELETE FROM %s WHERE fid=?", getTableName()));
			prepareStatement.setString(1, fid);
			connection.setAutoCommit(false);
			int result = prepareStatement.executeUpdate();
			connection.commit();
			connection.setAutoCommit(true);
			connection.close();

			return result > 0;
		} catch (SQLException e) {
			Log.e(getTag(), "delete error", e);
		}
		return false;
	}

	public synchronized boolean insert(LocalFile file) {
		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection.prepareStatement(String.format(
					"INSERT INTO %s ('path','length','isdir','mtime','fid','md5','newest') VALUES (?,?,?,?,?,?,?)",
					getTableName()));
			prepareStatement.setString(1, file.getPath());
			prepareStatement.setLong(2, file.getLength());
			prepareStatement.setBoolean(3, file.isDir());
			prepareStatement.setLong(4, file.getMtime());
			prepareStatement.setString(5, file.toFid());
			prepareStatement.setString(6, file.getMd5());
			prepareStatement.setBoolean(7, file.isNewest());
			prepareStatement.addBatch();
			connection.setAutoCommit(false);
			int result = prepareStatement.executeUpdate();
			connection.commit();
			connection.setAutoCommit(true);
			connection.close();

			return result > 0;
		} catch (Exception e) {

		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.dao.BaseDao#getTag()
	 */
	@Override
	public String getTag() {
		return "LocalFileDao";
	}

}
