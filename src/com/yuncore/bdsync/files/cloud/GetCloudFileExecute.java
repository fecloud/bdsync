/**
 * 
 */
package com.yuncore.bdsync.files.cloud;

import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.CloudFileDao;
import com.yuncore.bdsync.entity.CloudFile;
import com.yuncore.bdsync.entity.CloudPageFile;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.files.FileExclude;
import com.yuncore.bdsync.task.Task;
import com.yuncore.bdsync.task.TaskContainer;
import com.yuncore.bdsync.task.TaskExecute;
import com.yuncore.bdsync.task.TaskStatus;
import com.yuncore.bdsync.util.Log;

/**
 * @author ouyangfeng
 * 
 */
public class GetCloudFileExecute extends TaskExecute {

	private static final String TAG = "GetCloudFileExecute";

	private FileExclude exclude;

	private CloudFileDao cloudFileDao;

	/**
	 * @param taskStatus
	 * @param taskContainer
	 */
	public GetCloudFileExecute(TaskStatus taskStatus, TaskContainer taskContainer, FileExclude exclude,
			CloudFileDao cloudFileDao) {
		super(taskStatus, taskContainer);
		this.exclude = exclude;
		this.cloudFileDao = cloudFileDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.dbpcs.task.TaskExecute#doTask(com.yuncore.dbpcs.task.Task)
	 */
	@Override
	protected void doTask(Task task) {
		final GetCloudFileTask fileTask = (GetCloudFileTask) task;
		try {
			final CloudPageFile listFiles = new FSApiImple().list(fileTask.getDir(), 1, 200000);
			if (listFiles != null) {
				if (listFiles.getErrno() == 0 && listFiles.getList() != null) {

					checkExcludeAndAddTask(listFiles.getList());

					cloudFileDao.insertCloudFileCache(listFiles.getList());

				} else if (listFiles.getErrno() == -9) {
					Log.w(TAG, "dir:" + fileTask.getDir() + " is not exits"); // 目录不存在了
				} else {
					if (listFiles.getErrno() == -6) {
						Log.w(TAG, "cookie problem"); // cookie有错误
//						System.setProperty(AppCookieContainer.COOKIE_LOAD, "false");
					}
					Log.w(TAG, "CloudPageFile listFiles error:" + listFiles.getErrno());
					// 因为某种原因没有取得成功
					taskContainer.addTask(task);
				}

			} else {
				Log.w(TAG, "CloudPageFile listFiles null");
				// 因为某种原因没有取得成功
				// 休息1s
				taskContainer.addTask(task);
			}
		} catch (ApiException e) {
			Log.e(TAG, "GetCloudFileExecute list", e);
			// 因为网络失败取得失败
			taskContainer.addTask(task);
		}
	}

	/**
	 * 检查并排除目录
	 * 
	 * @param files
	 */
	private void checkExcludeAndAddTask(List<CloudFile> files) {

		final List<LocalFile> deletes = new ArrayList<LocalFile>();
		for (CloudFile f : files) {
			if (f.isDirectory()) {
				if (exclude.rmExclude(f.getAbsolutePath())) {
					deletes.add(f);
				} else {
					taskContainer.addTask(new GetCloudFileTask(f.getAbsolutePath()));
				}

			}
		}
		files.removeAll(deletes);
	}
}
