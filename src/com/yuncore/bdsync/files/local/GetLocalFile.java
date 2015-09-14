
package com.yuncore.bdsync.files.local;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.dao.LocalFileTmpDao;
import com.yuncore.bdsync.files.BDSyncFileExclude;
import com.yuncore.bdsync.task.PreemptiveTaskService;
import com.yuncore.bdsync.task.TaskExecute;

public class GetLocalFile extends PreemptiveTaskService {

	static final String TAG = "GetLocalFile";

	private File dir;

	private BDSyncFileExclude exclude;

	private LocalFileDao localFileDao;

	private long session;

	public GetLocalFile(int threads, String dir) {
		this.threads = threads;
		exclude = new BDSyncFileExclude();
		this.dir = new File(dir);
		localFileDao = new LocalFileTmpDao();
	}

	@Override
	protected TaskExecute newTaskExecute() {
		final GetLocalFileExecute getLocalExecute = new GetLocalFileExecute(dir.getAbsolutePath(), taskStatus,
				taskContainer, exclude, localFileDao, session);
		return getLocalExecute;
	}

	public synchronized boolean list() {
		if (dir.exists()) {
			session = System.currentTimeMillis();
			Environment.setLocallistSession("" + session);
			taskContainer.addTask(new GetLocalFileTask(""));
			waitTaskFinish();
			localFileDao.insertAllCacaheFlush();
			return true;
		}
		return false;
	}

	public synchronized boolean setList(String dir) {
		if (new File(dir).exists()) {
			this.dir = new File(dir);
			return true;
		}
		return false;
	}

	@Override
	protected String getTaskExecuteName() {
		return "GetLocalFile";
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
