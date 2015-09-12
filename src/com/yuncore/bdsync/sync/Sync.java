package com.yuncore.bdsync.sync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.Argsment;
import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.app.ClientContext;
import com.yuncore.bdsync.ctrl.Httpd;
import com.yuncore.bdsync.dao.SyncProcessDao;
import com.yuncore.bdsync.entity.SyncProcess;
import com.yuncore.bdsync.http.cookie.FileCookieContainer;
import com.yuncore.bdsync.sync.task.CloudCompareTask;
import com.yuncore.bdsync.sync.task.ListCloudFilesTask;
import com.yuncore.bdsync.sync.task.SyncStepTask;
import com.yuncore.bdsync.util.Log;

public class Sync implements Runnable {

	private static final String TAG = "Sync";

	private String syncdir;
	private String synctmpdir;

	private int httpPort = 18080;

	private Httpd httpd;

	private String[] args;

	private boolean flag;

	/**
	 * 当前正在执行的任务
	 */
	private SyncStepTask current;

	private int currentIndex;

	private List<SyncStepTask> steps;

	private SyncProcessDao syncProcessDao;

	private long lasttime;

	public Sync(String[] args) {
		this.args = args;
		syncdir = args[1];
		synctmpdir = args[1] + File.separator + ".bdsync";
		setHttpPort(args);
		startHttp();

		syncProcessDao = new SyncProcessDao();
	}

	private void setHttpPort(String[] args) {
		for (int i = 2; i < args.length; i++) {
			if (args[i].equals("-p") && (i + 1) <= args.length - 1) {
				try {
					httpPort = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException e) {
					Log.w(TAG, "parse httpport error use default " + httpPort);
				}
				break;
			}
		}
	}

	private void startHttp() {
		if (httpd == null) {
			try {
				httpd = new Httpd(httpPort);
			} catch (IOException e) {
				Log.w(TAG, "start httpd service error");
			}
		}
	}

	private void setEnv() {
		Environment.setSyncDir(syncdir);
		Environment.setSyncTmpDir(synctmpdir);

		Argsment.setBDSyncAllow("1");

		// System.setProperty(Const.TMP,
		// String.format("%s%s%s", syncdir, File.separator, Const.TMP_DIR));
		// System.setProperty("http_proxy", "localhost:8888");
		Environment.setContextClassName(ClientContext.class.getName());
		Environment.setCookiecontainerClassName(FileCookieContainer.class
				.getName());

	}

	public synchronized void start() {
		final Thread thread = new Thread(this);
		thread.setName(TAG);
		this.flag = true;
		thread.start();
	}

	/**
	 * 加入任务步骤
	 */
	private void addWorkStep() {
		steps = new ArrayList<SyncStepTask>();
		steps.add(new ListCloudFilesTask(args));
		steps.add(new CloudCompareTask());
		// steps.add(new ListLocalFilesTask(args));
		// steps.add(new LocalCompareTask());
	}

	/**
	 * 找到程序结束运行时最后在执行的任务
	 */
	private void lastWorkingStep() {

		final SyncProcess process = syncProcessDao.getSyncProcess();
		if (null != process) {
			final int size = steps.size();

			SyncStepTask temp = null;
			for (int i = 0; i < size; i++) {
				temp = steps.get(i);
				if (process.getProcess().equals(temp.getStepName())) {
					currentIndex = i;
					break;
				}
			}
		}
	}

	/**
	 * 循环执行所有任务
	 */
	private void work() {
		final int size = steps.size();
		final int lastIndex = size - 1;
		for (; currentIndex < size && flag;) {

			current = steps.get(currentIndex);
			syncProcessDao.setSyncProcess(new SyncProcess(
					current.getStepName(), current.getRealName()));

			// 任务执行成功
			if (current.start()) {

				// 任务头尾循环
				if (currentIndex == lastIndex) {
					currentIndex = 0;
					if (!waitAndContinue()) {
						continue;
					}

				} else {
					currentIndex++;
				}
			}
		}
	}

	public synchronized void stop() {
		if (httpd != null) {
			httpd.stop();
		}
		this.flag = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Log.w("Sync", "sync dir:" + syncdir);
		setEnv();
		addWorkStep();
		lastWorkingStep();
		setLasttime(System.currentTimeMillis());
		work();
	}

	/**
	 * @param lasttime
	 *            the lasttime to set
	 */
	private void setLasttime(long lasttime) {
		this.lasttime = lasttime;
	}

	public boolean waitAndContinue() {
		if (flag) {
			long time = System.currentTimeMillis() - lasttime;
			if (time < Argsment.getBDSyncInterval()) {
				synchronized (this) {
					try {
						// 允许同步
						if (Argsment.getBDSyncAllow()) {
							time = Argsment.getBDSyncInterval() - time;
							Log.d(TAG, "wait:" + time);
							wait(time);
						} else {
							// 不允许同步
							while (flag && !Argsment.getBDSyncAllow()) {
								wait(1000);
							}
						}

					} catch (InterruptedException e) {
					}
				}
			}
			setLasttime(System.currentTimeMillis());
		}

		return flag;
	}
}
