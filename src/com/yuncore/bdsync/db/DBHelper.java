package com.yuncore.bdsync.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.util.Log;

public class DBHelper {

	private static final String TAG = "DBHelper";

	private static final int DB_VERSION = 1;

	public DBHelper() {

		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "DBHelper Class.forName(\"org.sqlite.JDBC\")", e);
		}
		try {

			final int dbVersion = dbVersion();
			if (dbVersion == 0) {
				createVersion();
				updateVersion();
				onCreateDB();
			} else if (DB_VERSION != dbVersion) {
				onUpdateDB(dbVersion, DB_VERSION);
				updateVersion();
			}
		} catch (Exception e) {
			Log.e(TAG, "DBHelper error", e);
		}

	}

	public synchronized Connection getConnection() {
		try {
			final Connection conn = DriverManager.getConnection(String.format(
					"jdbc:sqlite:%s", Environment.getDBFile()));
			return conn;
		} catch (Exception e) {
			Log.e(TAG, "DBHelper getConnection", e);
		}
		return null;
	}

	public synchronized boolean versionExists() {

		try {
			final Connection connection = getConnection();
			final PreparedStatement prepareStatement = connection
					.prepareStatement("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='version';");

			final ResultSet resultSet = prepareStatement.executeQuery();

			int version = 0;
			if (resultSet.next()) {
				version = resultSet.getInt(1);
			}
			resultSet.close();
			prepareStatement.close();
			connection.close();
			return version > 0;
		} catch (SQLException e) {
			Log.e(TAG, "dbVersion", e);
		}
		return false;
	}

	protected synchronized int dbVersion() {

		int dbVersion = 0;
		if (versionExists()) {
			final String sql = "SELECT version FROM version";

			try {
				final Connection connection = getConnection();
				final PreparedStatement prepareStatement = connection
						.prepareStatement(sql);

				final ResultSet resultSet = prepareStatement.executeQuery();

				if (resultSet.next()) {
					dbVersion = resultSet.getInt(1);
				}
				resultSet.close();
				prepareStatement.close();
				connection.close();
			} catch (SQLException e) {
				Log.e(TAG, "dbVersion", e);
			}
		}
		return dbVersion;

	}

	private synchronized void createVersion() throws SQLException {
		Log.i(TAG, "createVersion");
		final Connection connection = getConnection();
		final PreparedStatement prepareStatement = connection
				.prepareStatement("CREATE TABLE IF NOT EXISTS version(id INTEGER PRIMARY KEY AUTOINCREMENT,version INTEGER);");
		connection.setAutoCommit(false);
		prepareStatement.execute();
		connection.commit();
		connection.setAutoCommit(true);
		prepareStatement.close();
		connection.close();
	}

	private synchronized void updateVersion() throws SQLException {
		Log.i(TAG, "updateVersion");
		final Connection connection = getConnection();
		final PreparedStatement prepareStatement = connection
				.prepareStatement("REPLACE INTO version (id,version) values (1,?)");
		prepareStatement.setInt(1, DB_VERSION);
		connection.setAutoCommit(false);
		prepareStatement.execute();
		connection.commit();
		connection.setAutoCommit(true);
		prepareStatement.close();
		connection.close();
	}

	protected synchronized void onCreateDB() {
		Log.i(TAG, "onCreateDB");

		executeSQL("CREATE TABLE IF NOT EXISTS process (id INTEGER PRIMARY KEY AUTOINCREMENT, process TEXT, name TEXT);"); // 本地文件记录

		executeSQL("CREATE TABLE IF NOT EXISTS sync_status (id INTEGER PRIMARY KEY AUTOINCREMENT, status INTEGER);");
		
		executeSQL("CREATE TABLE IF NOT EXISTS localfile (id INTEGER PRIMARY KEY AUTOINCREMENT,p ath TEXT, length INTEGER, isdir INTEGER, mtime INTEGER, fid TEXT, session INTEGER);"); // 本地文件记录
		executeSQL("CREATE TABLE IF NOT EXISTS localdelete (id INTEGER, path TEXT, length INTEGER, isdir INTEGER, mtime INTEGER, fid TEXT, session INTEGER);");// 本地被删除了
		executeSQL("CREATE TABLE IF NOT EXISTS localupload (id INTEGER, path TEXT, length INTEGER, isdir INTEGER, mtime INTEGER, fid TEXT, session INTEGER);");// 本地要被上传的

		executeSQL("CREATE TABLE IF NOT EXISTS cloudfile (id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT , length INTEGER, isdir INTEGER, mtime INTEGER, fid TEXT, md5 TEXT, session INTEGER);");
		executeSQL("CREATE TABLE IF NOT EXISTS clouddelete (id INTEGER, path TEXT, length INTEGER, isdir INTEGER, mtime INTEGER, fid TEXT, md5 varchar(32), session INTEGER);");
		executeSQL("CREATE TABLE IF NOT EXISTS clouddownload (id INTEGER, path TEXT, length INTEGER, isdir INTEGER, mtime INTEGER, fid TEXT, md5 varchar(32), session INTEGER);");

		executeSQL("CREATE TABLE IF NOT EXISTS cloudhistory (id INTEGER PRIMARY KEY AUTOINCREMENT, time INTEGER );");
		executeSQL("CREATE TABLE IF NOT EXISTS localhistory (id INTEGER PRIMARY KEY AUTOINCREMENT, time INTEGER );");

	}

	protected void onUpdateDB(int old_version, int newversion) {
		Log.w(TAG, String.format("onUpdateDB old_version:%s newversion:%s",
				old_version, newversion));

	}

	public synchronized boolean executeSQL(String sql) {
		try {
			final Connection connection = getConnection();
			Statement createStatement = connection.createStatement();
			connection.setAutoCommit(false);
			createStatement.execute(sql);
			connection.commit();
			connection.setAutoCommit(true);
			createStatement.close();
			connection.close();
			return true;
		} catch (SQLException e) {
			Log.e(TAG, "executeSQL error", e);
		}
		return false;
	}
}
