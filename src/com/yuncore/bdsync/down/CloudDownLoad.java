package com.yuncore.bdsync.down;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.DownloadDao;
import com.yuncore.bdsync.dao.LocalFileDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

public class CloudDownLoad implements DownloadOperate {

	static final String TAG = "CloudDownLoad";

	private FSApi fsApi;

	private DownloadDao downloadDao;

	private LocalFileDao localFileDao;

	private List<DownLoadCheckFileStep> steps = new ArrayList<DownLoadCheckFileStep>();

	protected volatile boolean flag;

	public CloudDownLoad(String root, String tmpDir) {
		fsApi = new FSApiImple();
		downloadDao = new DownloadDao();
		localFileDao = new LocalFileDao();

		steps.add(new DownLoadCheckLocalFile(root));
		steps.add(new DownLoadCheckCloudFile(tmpDir, fsApi));
		steps.add(new DownLoadFileConent(root, tmpDir, fsApi));

		// 建立临时文件目录
		final File file = new File(tmpDir);
		if (!file.exists()) {
			file.mkdirs();
		}

	}

	public boolean start() {
		LocalFile downloadFile = null;
		flag = true;
		while (flag) {

			downloadFile = downloadDao.query();
			if (downloadFile != null) {
				StatusMent.setProperty(StatusMent.DOFILE, downloadFile);
				StatusMent.setProperty(StatusMent.DOFILE_SIZE, 0);

				checkAndDownLoad(downloadFile);

			} else {
				break;
			}
		}
		StatusMent.removeProperty(StatusMent.DOFILE);
		StatusMent.removeProperty(StatusMent.DOFILE_SIZE);
		return true;
	}

	/**
	 * @param cloudFile
	 * @throws ApiException
	 */
	private void checkAndDownLoad(LocalFile file) {
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
	public boolean deleteRecord(LocalFile file) {
		StatusMent.removeProperty(StatusMent.DOFILE);
		final boolean result = downloadDao.delete(file);
		if (result) {
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
	public boolean getDownLoadStatus() {
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
	public boolean addAnotherRecord(LocalFile file) {
		file.setNewest(false);
		if (localFileDao.queryByPath(file.getAbsolutePath()) == null) {
			return localFileDao.insert(file);
		} else {
			return localFileDao.updateByPath(file);
		}
	}

}
