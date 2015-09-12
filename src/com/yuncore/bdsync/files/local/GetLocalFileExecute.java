package com.yuncore.bdsync.files.local;

import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.files.FileExclude;
import com.yuncore.bdsync.task.Task;
import com.yuncore.bdsync.task.TaskContainer;
import com.yuncore.bdsync.task.TaskExecute;
import com.yuncore.bdsync.task.TaskStatus;
import com.yuncore.bdsync.util.FileUtil;

public class GetLocalFileExecute extends TaskExecute {

	private FileExclude exclude;

	private String root;

	private LocalFileDao localFileDao;

	private long session;

	public GetLocalFileExecute(String root, TaskStatus taskStatus,
			TaskContainer taskContainer, FileExclude exclude,
			LocalFileDao localFileDao, long session) {
		super(taskStatus, taskContainer);
		this.root = root;
		this.exclude = exclude;
		this.localFileDao = localFileDao;
		this.session = session;
	}

	protected void getDirFiles(GetLocalFileTask task) {

		final List<LocalFile> listFiles = FileUtil.listFiles(root,
				task.getDir(), session);
		if (listFiles != null) {
			excute(listFiles, task.getDir());
		}
	}

	protected void excute(List<LocalFile> files, String dir) {

		checkExcludeAndAddTask(files);

		// 把最新本地结果放入数据库
		localFileDao.insertCache(files);

	}

	@Override
	protected void doTask(Task task) {
		getDirFiles((GetLocalFileTask) task);
	}

	/**
	 * 检查并排除目录
	 * 
	 * @param files
	 */
	private void checkExcludeAndAddTask(List<LocalFile> files) {

		final List<LocalFile> deletes = new ArrayList<LocalFile>();
		for (LocalFile f : files) {
			if (f.isDirectory()) {
				if (exclude.rmExclude(f.getAbsolutePath())) {
					deletes.add(f);
				} else {
					taskContainer.addTask(new GetLocalFileTask(f
							.getAbsolutePath()));
				}

			}
		}
		files.removeAll(deletes);
	}
}
