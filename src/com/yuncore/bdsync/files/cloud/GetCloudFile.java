/**
 * 
 */
package com.yuncore.bdsync.files.cloud;

import java.util.HashSet;
import java.util.Set;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.dao.CloudFileDao;
import com.yuncore.bdsync.dao.CloudFileTmpDao;
import com.yuncore.bdsync.files.BDSyncFileExclude;
import com.yuncore.bdsync.task.PreemptiveTaskService;
import com.yuncore.bdsync.task.TaskExecute;
import com.yuncore.bdsync.util.Stopwatch;

/**
 * @author ouyangfeng
 * 
 */
public class GetCloudFile extends PreemptiveTaskService {

	private String dir;

	private CloudFileDao cloudFileDao;

	private BDSyncFileExclude exclude;

	public GetCloudFile(int threads, String dir) {
		this.threads = threads;
		exclude = new BDSyncFileExclude();
		this.dir = dir;
		cloudFileDao = new CloudFileTmpDao();
	}

	public synchronized boolean list() {
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		Environment.setCloudlistSession("" + System.currentTimeMillis());
		taskContainer.addTask(new GetCloudFileTask(dir));
		waitTaskFinish();
		cloudFileDao.insertAllCacaheFlush();
		stopwatch.stop(getTaskExecuteName());
		return true;
	}

	public synchronized boolean setList(String dir) {
		this.dir = dir;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.task.TaskService#newTaskExecute()
	 */
	@Override
	protected TaskExecute newTaskExecute() {
		return new GetCloudFileExecute(taskStatus, taskContainer, exclude,
				cloudFileDao);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.task.TaskService#getTaskExecuteName()
	 */
	@Override
	protected String getTaskExecuteName() {
		return "GetCloudFile";
	}

	/**
	 * 添加要过滤的目录或者文件
	 * 
	 * @param file
	 */
	public synchronized void addExclude(Set<String> files) {
		final Set<String> list = new HashSet<String>();
		String filename = null;
		for (String f : files) {
			filename = "/" + f;
			if (!list.contains(filename)) {
				list.add(filename);
			}
		}
		exclude.addExclude(list);
	}

}
