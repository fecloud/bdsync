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

	private List<LocalFile> cache = new ArrayList<LocalFile>();

	private int size;

	private static final int CACHE_SIZE = 5000;

	@Override
	public String getTableName() {
		return "localfile";
	}

	public synchronized boolean insertAllCacaheFlush() {
		if (size != 0) {
			final boolean result = insertAll();
			if(result){
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
					"INSERT INTO %s ('path','length','isdir','mtime' ,'fid','session') VALUES (?,?,?,?,?,?)",
					getTableName());
			final PreparedStatement prepareStatement = connection.prepareStatement(sql);

			for (LocalFile f : cache) {
				prepareStatement.setString(1, f.getPath());
				prepareStatement.setLong(2, f.getLength());
				prepareStatement.setInt(3, f.isDir() ? 1 : 0);
				prepareStatement.setLong(4, f.getMtime());
				prepareStatement.setString(5, f.toFid());
				prepareStatement.setLong(6, f.getSession());
				prepareStatement.addBatch();
			}
			connection.setAutoCommit(false);
			prepareStatement.executeBatch();

			connection.commit();
			connection.setAutoCommit(true);
			connection.close();

			stopwatch.stop("LocalFileDao insertAll " + size);
			return true;
		} catch (SQLException e) {
			Log.e(getTag(), "", e);
		}
		return false;
	}

	protected static LocalFile buildLocalFile(ResultSet resultSet) throws SQLException {
		final LocalFile file = new LocalFile();
		file.setId(resultSet.getString("id"));
		file.setPath(resultSet.getString("path"));
		file.setLength(resultSet.getLong("length"));
		file.setDir(resultSet.getBoolean("isdir"));
		file.setSession(resultSet.getLong("session"));
		return file;
	}
	
	/* (non-Javadoc)
	 * @see com.yuncore.bdsync.dao.BaseDao#getTag()
	 */
	@Override
	public String getTag() {
		return "LocalFileDao";
	}

}
