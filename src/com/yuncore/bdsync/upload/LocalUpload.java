package com.yuncore.bdsync.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.CloudFileDao;
import com.yuncore.bdsync.dao.UploadDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.util.FileUtil;
import com.yuncore.bdsync.util.Log;

public class LocalUpload implements UpLoadOperate {

	static final String TAG = "LocalUpload";

	protected FSApi api;

	protected String croot;
	
	protected String root;

	protected UploadDao uploadDao;

	protected CloudFileDao cloudFileDao;

	private List<UpLoadCheckFileStep> steps = new ArrayList<UpLoadCheckFileStep>();

	protected volatile boolean flag;
	
	protected Hashtable<Object, Object> lock = new Hashtable<Object, Object>();

	public LocalUpload(String croot, String root, String tmpDir) {
		this.croot = croot;
		this.root = root;
		uploadDao = new UploadDao();
		api = new FSApiImple();
		cloudFileDao = new CloudFileDao();
		steps.add(new UpLoadCheckLocalFile(root));
		steps.add(new UpLoadCheckCloudFile(croot, api));
		steps.add(new UpLoadFileConent(croot, api));
		steps.add(new UpLoadFileSecondConent(croot, root,api));
		steps.add(new UpLoadFileNormalConent(croot, root,api));
		steps.add(new UpLoadFileBlockConent(croot, root, tmpDir, api));
		
		// 建立临时文件目录
		final File file = new File(tmpDir);
		if (!file.exists()) {
			file.mkdirs();
		}
				
	}

	public boolean start() {
		flag = true;

		StatusMent.getDoingfile().clear();
		
		int upThread = Integer.valueOf(Environment.getUpThread());
		int cpuNum = Runtime.getRuntime().availableProcessors() * upThread;
		
		final List<LocalUploadThread> list = new ArrayList<LocalUploadThread>(cpuNum);
		LocalUploadThread localUploadThread = null;
		for(int i = 0;i < cpuNum;i++){
			localUploadThread = new LocalUploadThread(i);
			list.add(localUploadThread);
			localUploadThread.start();
		}
		while (flag) {
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			boolean isEnd = true;
			for (LocalUploadThread thread : list) {
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
	 * 
	 */
	private final void checkAndUpLoad(LocalFile upLocalFile) {

		if (upLocalFile.isFile()) {
			Log.d(TAG,
					"getUpload file " + upLocalFile.getAbsolutePath()
							+ " size:"
							+ FileUtil.byteSizeToHuman(upLocalFile.getLength()));
		} else {
			Log.d(TAG, "getUpload dir " + upLocalFile.getAbsolutePath());
		}
		// 如果云端文件还在
		for (UpLoadCheckFileStep step : steps) {
			if (!flag) {
				break;
			}
			if (!step.check(upLocalFile, this)) {
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.upload.UpLoadOperate#deleteRecord(com.yuncore.bdsync
	 * .entity.LocalFile)
	 */
	@Override
	public synchronized boolean deleteRecord(LocalFile file) {
		StatusMent.getDoingfile().remove(file.getAbsolutePath());
		final boolean result = uploadDao.delete(file);
		if (result) {
			lock.remove(file);
			Log.i(TAG, "deleteRecord " + file.getAbsolutePath());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.upload.UpLoadOperate#getUpLoadStatus()
	 */
	@Override
	public boolean getUpLoadStatus() {
		return flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.upload.UpLoadOperate#addAnotherRecord(com.yuncore.
	 * bdsync.entity.LocalFile)
	 */
	@Override
	public boolean addAnotherRecord(LocalFile file) {
		file.setNewest(false);
		if (cloudFileDao.queryByPath(file.getAbsolutePath()) == null) {
			return cloudFileDao.insert(file);
		} else {
			return cloudFileDao.updateByPath(file);
		}
	}
	
	public synchronized LocalFile getUpLoadTask(LocalFile file) {

		LocalFile temp = null;
		if (null != file) {
			// 任务完成失败了
			if (lock.containsKey(file)) {
				return  file;
			}
		}
		List<LocalFile> querys = uploadDao.query(lock.size(), 1);
		if (querys != null && !querys.isEmpty()) {
			LocalFile once = querys.get(0);
			lock.put(once, once);
			temp = once;
		}

		return temp;
	}

	/**
	 * 上传线程
	 * @author FENG
	 *
	 */
	private class LocalUploadThread extends Thread {

		private int id;
		
		private boolean runing;
		
		public LocalUploadThread(int id) {
			super();
			this.id = id;
		}

		@Override
		public void run() {
			runing = true;
			setName("LocalUploadThread-" + id);
			LocalFile upLoadTask = null;
			while(getUpLoadStatus()){
				upLoadTask = getUpLoadTask(upLoadTask);
				if(null != upLoadTask){
					checkAndUpLoad(upLoadTask);
				}else {
					break;
				}
			}
			runing = false;
		}
		
	}
}
