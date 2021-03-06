package com.yuncore.bdsync.down;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.DownloadDao;
import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.FileUtil;
import com.yuncore.bdsync.util.Log;

public class CloudDownLoad implements DownloadOperate {

	static final String TAG = "CloudDownLoad";

	private FSApi fsApi;

	private DownloadDao downloadDao;

	private LocalFileDao localFileDao;

	private List<DownLoadCheckFileStep> steps = new ArrayList<DownLoadCheckFileStep>();

	protected volatile boolean flag;
	
	protected Hashtable<Object, Object> lock = new Hashtable<Object, Object>();

	public CloudDownLoad(String root, String tmpDir,String croot) {
		fsApi = new FSApiImple();
		downloadDao = new DownloadDao();
		localFileDao = new LocalFileDao();

		steps.add(new DownLoadCheckLocalFile(root));
		steps.add(new DownLoadCheckCloudFile(tmpDir, fsApi, croot));
		steps.add(new DownLoadFileConent(root, tmpDir, fsApi, croot));

		// 建立临时文件目录
		final File file = new File(tmpDir);
		if (!file.exists()) {
			file.mkdirs();
		}

	}

	public boolean start() {
		flag = true;
		
		StatusMent.getDoingfile().clear();
		
		int downThread = Integer.valueOf(Environment.getDownThread());
		int cpuNum = Runtime.getRuntime().availableProcessors() * downThread;
		
		final List<CloudDownLoadThread> list = new ArrayList<CloudDownLoadThread>(cpuNum);
		CloudDownLoadThread cloudDownLoadThread = null;
		for(int i = 0;i < cpuNum;i++){
			cloudDownLoadThread = new CloudDownLoadThread(i);
			list.add(cloudDownLoadThread);
			cloudDownLoadThread.start();
		}
		while (flag) {
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			boolean isEnd = true;
			for (CloudDownLoadThread thread : list) {
				if (thread.runing) {
					isEnd = false;
					break;
				}
			}
			if (isEnd) {
				break;
			}
		}
		
		
		StatusMent.getDoingfile().clear();
		return true;
	}

	/**
	 * @param cloudFile
	 * @throws ApiException
	 */
	private void checkAndDownLoad(LocalFile file) {
		if (file.isFile()) {
			Log.d(TAG,
					"getDownload file " + file.getAbsolutePath()
							+ " size:"
							+ FileUtil.byteSizeToHuman(file.getLength()));
		} else {
			Log.d(TAG, "getDownload dir " + file.getAbsolutePath());
		}
		// 如果云端文件还在
		for (DownLoadCheckFileStep step : steps) {
			if (!flag) {
				break;
			}
			if (!step.check(file, this)) {
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.down.DeleteRecord#deleteRecord(com.yuncore.bdsync.
	 * entity.LocalFile)
	 */
	@Override
	public synchronized boolean deleteRecord(LocalFile file) {
		StatusMent.getDoingfile().remove(file.getAbsolutePath());
		final boolean result = downloadDao.delete(file);
		if (result) {
			lock.remove(file);
			Log.d(TAG, "deleteRecord " + file.getAbsolutePath());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.down.DownloadOperate#getDownLoadStatus()
	 */
	@Override
	public synchronized boolean getDownLoadStatus() {
		return flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.down.DownloadOperate#addAnotherRecord(com.yuncore.
	 * bdsync.entity.LocalFile)
	 */
	@Override
	public synchronized boolean addAnotherRecord(LocalFile file) {
		file.setNewest(false);
		if (localFileDao.queryByPath(file.getAbsolutePath()) == null) {
			return localFileDao.insert(file);
		} else {
			return localFileDao.updateByPath(file);
		}
	}
	
	public synchronized LocalFile getDownLoadTask(LocalFile file) {

		LocalFile temp = null;
		if (null != file) {
			// 任务完成失败了
			if (lock.containsKey(file)) {
				return  file;
			}
		}
		List<LocalFile> querys = downloadDao.query(lock.size(), 1);
		if (querys != null && !querys.isEmpty()) {
			LocalFile once = querys.get(0);
			lock.put(once, once);
			temp = once;
		}

		return temp;
	}

	
	/**
	 * 下载线程
	 * @author FENG
	 *
	 */
	private class CloudDownLoadThread extends Thread {

		private int id;
		
		private boolean runing;
		
		public CloudDownLoadThread(int id) {
			super();
			this.id = id;
		}

		@Override
		public void run() {
			runing = true;
			setName("CloudDownLoadThread-" + id);
			LocalFile downLoadTask = null;
			while(getDownLoadStatus()){
				downLoadTask = getDownLoadTask(downLoadTask);
				if(null != downLoadTask){
					checkAndDownLoad(downLoadTask);
				}else {
					break;
				}
			}
			runing = false;
		}
		
	}
	
}
