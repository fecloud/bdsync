package com.yuncore.bdsync.down;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.api.imple.FSApiImple;
import com.yuncore.bdsync.dao.DownloadDao;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.FileUtil;
import com.yuncore.bdsync.util.Log;

public class CloudDownLoad implements DownloadOperate {

	static final String TAG = "CloudDownLoad";

	private String root;

	private FSApi fsApi;

	private DownloadDao downloadDao;

	private List<DownLoadCheckFileStep> steps = new ArrayList<DownLoadCheckFileStep>();

	protected volatile boolean flag;

	public CloudDownLoad(String root, String tmpDir) {
		this.root = root;
		fsApi = new FSApiImple();
		downloadDao = new DownloadDao();

		steps.add(new DownLoadCheckLocalSize());
		steps.add(new DownLoadCheckLocalMtime());
		steps.add(new DownLoadCheckCloudSize());
		steps.add(new DownLoadCheckCloudMtime());
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

			downloadFile = getDownLoad();
			if (downloadFile != null) {
				StatusMent.setProperty(StatusMent.DOWNLOADING, downloadFile);
				StatusMent.setProperty(StatusMent.DOWNLOAD_SIZE, 0);

				checkAndDownLoad(downloadFile);

			} else {
				return true;
			}
		}
		StatusMent.setProperty(StatusMent.DOWNLOADING, false);
		return true;
	}

	/**
	 * @param cloudFile
	 * @throws ApiException
	 */
	private void checkAndDownLoad(LocalFile file) {
		final LocalFile downloadFile = file;
		LocalFile cloudFile = null;
		LocalFile localFile = null;
		try {
			cloudFile = getCloudFile(downloadFile);
			localFile = getLocalFile(downloadFile);
		} catch (Exception e) {
			return;
		}

		// 如果云端文件还在
		for (DownLoadCheckFileStep step : steps) {
			if (!step.check(downloadFile, cloudFile, localFile, this)) {
				break;
			}
		}
	}

	private LocalFile getDownLoad() {
		return downloadDao.query();
	}

	protected LocalFile getCloudFile(LocalFile downloadFile)
			throws ApiException {
		return fsApi.getMeta(downloadFile.getAbsolutePath());
	}

	protected LocalFile getLocalFile(LocalFile downloadFile) {
		return FileUtil.getLocalFile(root, downloadFile.getAbsolutePath());
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
		StatusMent.setProperty(StatusMent.DOWNLOADING, "");
		final boolean result = downloadDao.deleteByFid(file.getfId());
		if (result) {
			Log.d(TAG, "delDownLoad " + file.getAbsolutePath());
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

}
