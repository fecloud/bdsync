package com.yuncore.bdsync.upload;

import java.util.ArrayList;
import java.util.List;

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

	protected String root;

	protected UploadDao uploadDao;

	protected CloudFileDao cloudFileDao;

	private List<UpLoadCheckFileStep> steps = new ArrayList<UpLoadCheckFileStep>();

	protected volatile boolean flag;

	public LocalUpload(String root, String tmpDir) {
		this.root = root;
		uploadDao = new UploadDao();
		api = new FSApiImple();
		cloudFileDao = new CloudFileDao();

		steps.add(new UpLoadCheckLocalFile(root));
		steps.add(new UpLoadCheckCloudFile(api));
		steps.add(new UpLoadFileConent(root, api));
	}

	public boolean start() {

		LocalFile upLocalFile = null;
		flag = true;

		while (flag) {

			upLocalFile = uploadDao.query();
			if (upLocalFile != null) {
				if (upLocalFile.isFile()) {
					Log.d(TAG,
							"getUpload file "
									+ upLocalFile.getAbsolutePath()
									+ " size:"
									+ FileUtil.byteSizeToHuman(upLocalFile
											.getLength()));
				} else {
					Log.d(TAG,
							"getUpload dir " + upLocalFile.getAbsolutePath());
				}
				
				StatusMent.setProperty(StatusMent.UPLOADING, upLocalFile);
				StatusMent.setProperty(StatusMent.UPLOAD_SIZE, 0);

				checkAndUpLoad(upLocalFile);
				
				StatusMent.setProperty(StatusMent.UPLOADING, 0);
			} else {
				break;
			}
		}
		return true;
	}

	/**
	 * 
	 */
	private final void checkAndUpLoad(LocalFile upLocalFile) {
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
	public boolean deleteRecord(LocalFile file) {
		if (uploadDao.deleteByFid(file.getfId())) {
			Log.i(TAG, "delUpload " + file.getAbsolutePath());
			return true;
		}
		return false;
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

}
