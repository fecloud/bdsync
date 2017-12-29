package com.yuncore.bdsync.files.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.files.FileExclude;
import com.yuncore.bdsync.task.Task;
import com.yuncore.bdsync.task.TaskContainer;
import com.yuncore.bdsync.task.TaskExecute;
import com.yuncore.bdsync.task.TaskStatus;

public class GetLocalFileExecute extends TaskExecute {

	private FileExclude exclude;

	private String root;

	private LocalFileDao localFileDao;

	private static final boolean file_separator = File.separator.equals("\\");

	public GetLocalFileExecute(String root, TaskStatus taskStatus,
			TaskContainer taskContainer, FileExclude exclude,
			LocalFileDao localFileDao) {
		super(taskStatus, taskContainer);
		this.root = root;
		this.exclude = exclude;
		this.localFileDao = localFileDao;
	}

	protected void getDirFiles(GetLocalFileTask task) {
		final List<LocalFile> listFiles = listFiles(root, task.getDir());
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

	/**
	 * 读取目录
	 * 
	 * @param dir
	 * @return
	 */
	private static final List<LocalFile> listFiles(String root, String dir) {
		final File file = new File(root, dir);
		if (file.exists() && file.isDirectory()) {
			File[] listFiles = file.listFiles();
			if (null != listFiles) {
				final List<LocalFile> list = new ArrayList<LocalFile>();
				LocalFile localFile = null;

				if (file_separator) {
					for (File f : listFiles) {
						localFile = new LocalFile();
						localFile.setPath(f.getAbsolutePath()
								.substring(root.length()).replace("\\", "/"));
						localFile.setMtime((int) (f.lastModified() / 1000));
						if (f.isFile())
							localFile.setLength(f.length());
						localFile.setDir(f.isDirectory());
						localFile.setNewest(true);
						localFile.setfId(localFile.toFid());
						
						if (exIncludeFile(f)) {
							list.add(localFile);
						}
					}
				} else {
					for (File f : listFiles) {
						localFile = new LocalFile();
						localFile.setPath(f.getAbsolutePath().substring(
								root.length()));
						localFile.setMtime((int) (f.lastModified() / 1000));
						if (f.isFile())
							localFile.setLength(f.length());
						localFile.setDir(f.isDirectory());
						localFile.setNewest(true);
						localFile.setfId(localFile.toFid());
						
						if (exIncludeFile(f)) {
							list.add(localFile);
						}
					}
				}

				return list;
			}
		}
		return null;
	}

	/**
	 * 排除系统自动生成的文件
	 * 
	 * @param f
	 * @return
	 */
	private static boolean exIncludeFile(File f) {
		if (f.isFile()) {
			if (f.getName().startsWith("._")) {
				return false;
			} else if (f.getName().equalsIgnoreCase(".DS_store")) {
				return false;
			}
		}
		return true;
	}
	
}
