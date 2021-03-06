package com.yuncore.bdsync.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.Argsment;
import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.ctrl.Httpd;
import com.yuncore.bdsync.dao.SyncProcessDao;
import com.yuncore.bdsync.entity.SyncProcess;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.sync.task.CloudCompareTask;
import com.yuncore.bdsync.sync.task.CloudDeleteActionLocalTask;
import com.yuncore.bdsync.sync.task.CloudDownloadTask;
import com.yuncore.bdsync.sync.task.ListCloudFilesTask;
import com.yuncore.bdsync.sync.task.ListLocalFilesTask;
import com.yuncore.bdsync.sync.task.LocalCompareTask;
import com.yuncore.bdsync.sync.task.LocalDeleteActionCloudTask;
import com.yuncore.bdsync.sync.task.LocalUploadTask;
import com.yuncore.bdsync.sync.task.SyncSleepTask;
import com.yuncore.bdsync.sync.task.SyncStepTask;
import com.yuncore.bdsync.sync.task.SyncStopTask;
import com.yuncore.bdsync.util.Log;

public class Sync implements Runnable {

	private static final String TAG = "Sync";

	private String syncdir;
	
	private String clouddir;

	private int httpPort = 18080;

	private Httpd httpd;

	private String[] args;

	/**
	 * 当前正在执行的任务
	 */
	private volatile SyncStepTask current;

	private int currentIndex;

	private List<SyncStepTask> steps;

	private SyncProcessDao syncProcessDao;

	private volatile boolean flag;
	
	private String action;

	public Sync(String[] args) {
		this.args = args;
		this.action = args[0];
		clouddir = args[1];
		syncdir = args[2];
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
		if (clouddir.equals("/")) {
			Environment.setCloudDir("");
		} else {
			Environment.setCloudDir(clouddir);
		}

		//设置临时目录,如果没有设置的话是在同步目录下的.bdsync
		if (args.length > 3) {
			boolean startAdd = false;
			for (int i = 2; i < args.length; i++) {
				if (args[i].equals("-t")) {
					startAdd = true;
				} else if (startAdd && args[i].startsWith("-")) {
					break;
				} else if (startAdd) {
					Environment.setSyncTmpDir(args[i]);
				}
			}
		}

	}

	/**
	 * 检查cookie有效性
	 * 
	 * @return
	 */
	private boolean checkCookie() {
		final FSApi fsApi = new FSApiImple();
		try {
			final String who = fsApi.who();
			if (null != who && who.trim().length() > 0) {
				Log.w(TAG, "current cookie user:" + who);
				return true;
			}
		} catch (ApiException e) {
		}
		Log.w(TAG, "cookie error exit");
		System.exit(1);
		return false;
	}

	public synchronized void start() {
		final Thread thread = new Thread(this);
		thread.setName(TAG);
		thread.start();
	}

	/**
	 * 加入任务步骤
	 */
	private void addWorkStep() {
		steps = new ArrayList<SyncStepTask>();
		steps.add(new SyncStopTask());

		if (action.equalsIgnoreCase("sync")) {
			steps.add(new ListCloudFilesTask(args));
			steps.add(new CloudCompareTask());
			steps.add(new CloudDeleteActionLocalTask(args));
			steps.add(new CloudDownloadTask(args));

			steps.add(new ListLocalFilesTask(args));
			steps.add(new LocalCompareTask());
			steps.add(new LocalDeleteActionCloudTask(args));
			steps.add(new LocalUploadTask(args));
		} else if (action.equalsIgnoreCase("up")) {
			steps.add(new ListLocalFilesTask(args));
			steps.add(new LocalCompareTask());
//			steps.add(new LocalDeleteActionCloudTask(args));
			steps.add(new LocalUploadTask(args));
		} else if (action.equalsIgnoreCase("down")) {

			steps.add(new ListCloudFilesTask(args));
			steps.add(new CloudCompareTask());
			steps.add(new CloudDeleteActionLocalTask(args));
			steps.add(new CloudDownloadTask(args));
		}
		


		steps.add(new SyncSleepTask());
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
		new DiskHomeTimer().start();
		final int size = steps.size();
		final int lastIndex = size - 1;
		for (; currentIndex < size && flag;) {

			if (!Argsment.getBDSyncAllow()) {
				currentIndex = 0;
			}

			current = steps.get(currentIndex);
			syncProcessDao.setSyncProcess(new SyncProcess(
					current.getStepName(), current.getRealName()));

			StatusMent.setProperty(StatusMent.SYNCWORKING,
					current.getRealName());
			StatusMent.setProperty(StatusMent.SYNCWORKINGCODE, current.getCode());

			Log.w(TAG, current.getRealName() + " start");
			// 任务执行成功
			if (current.start()) {
				Log.w(TAG, current.getRealName() + " end");
				// 任务头尾循环
				if (currentIndex == lastIndex) {
					currentIndex = 0;
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

		if (current != null) {
			current.stop();
		}
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
		if(checkCookie()){
			addWorkStep();
			lastWorkingStep();
			this.flag = true;
			work();
		}
		
	}

}
