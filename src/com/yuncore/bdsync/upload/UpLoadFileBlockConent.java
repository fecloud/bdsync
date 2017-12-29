/**
 * @(#) UpLoadFileBlockConent.java Created on 2015年9月28日
 *
 * 
 */
package com.yuncore.bdsync.upload;

import java.io.File;
import java.io.IOException;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.CloudFile;
import com.yuncore.bdsync.entity.DoingFile;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>UpLoadFileBlockConent</code>
 * <p>
 * 分块上传(每10m一块)
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadFileBlockConent implements UpLoadCheckFileStep {

	private static final String TAG = "UpLoadFileBlockConent";
	/**
	 * 每10m一块
	 */
	public static final long SCLIE_SIZE = 1024l * 1024l * 10l;

	private String croot;
	
	private String root;

	private String tmpDir;

	private FSApi fsApi;

	/**
	 * @param root
	 * @param fsApi
	 */
	public UpLoadFileBlockConent(String croot, String root, String tmpDir, FSApi fsApi) {
		super();
		this.croot = croot;
		this.root = root;
		this.tmpDir = tmpDir;
		this.fsApi = fsApi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.upload.UpLoadCheckFileStep#check(com.yuncore.bdsync.
	 * entity.LocalFile, com.yuncore.bdsync.upload.UpLoadOperate)
	 */
	@Override
	public boolean check(LocalFile uploadFile, UpLoadOperate uploadOperate) {

		final UploadBlockContentFileSourceOutputListener listener = new UploadBlockContentFileSourceOutputListener(
				croot, uploadFile, uploadOperate, tmpDir + File.separator
						+ uploadFile.getfId() + ".sclies");
		Log.d(TAG, "UpLoadFileBlockConent uploading...");

		listener.readSlicesMd5();

		final int nums = listener.coutBlock();
		for (int i = listener.getSclies().size(); i < nums; i++) {
			if (!uploadOperate.getUpLoadStatus()) {
				StatusMent.getDoingfile().put(uploadFile.getAbsolutePath(), 
						new DoingFile(uploadFile).setDoingSize(0));
				return true;
			}

			String md5 = null;

			try {
				Log.d(TAG, "uploading block " + i);
				md5 = fsApi.uploadTmpFile(listener, listener);
			} catch (ApiException e) {
				i--;
				continue;
			} finally {
				try {
					if (null != listener.getInputStream()) {
						listener.getInputStream().close();
					}
				} catch (IOException e) {
				}
			}

			if (md5 == null) {
				i--;
				continue;
			} else {
				listener.getSclies().add(md5);
				listener.writeSlicesMd5(md5);
			}

		}

		if (!uploadOperate.getUpLoadStatus()) {
			StatusMent.getDoingfile().remove(uploadFile.getAbsolutePath());
			return true;
		}

		try {
			final CloudFile createSuperFile = fsApi.createSuperFile(croot + uploadFile.getAbsolutePath(), listener.scliesToArray());
			if (createSuperFile != null) {
				listener.deleteSlicesMd5();
				Log.w(TAG, "upload " + uploadFile.getAbsolutePath() + " success");
				final boolean uploaddelfile = Boolean.valueOf(Environment.getUploadDelFile());
				if (uploaddelfile) {
					final boolean del = new File(root + uploadFile.getAbsolutePath()).delete();
					if (del) {
						Log.w(TAG, "upload delfile " + uploadFile.getAbsolutePath() + " success");
					} else {
						Log.w(TAG, "upload delfile " + uploadFile.getAbsolutePath() + " fail");
					}
				}
				uploadOperate.addAnotherRecord(uploadFile);
				uploadOperate.deleteRecord(uploadFile);
			}

		} catch (ApiException e) {
		}

		StatusMent.getDoingfile().remove(uploadFile.getAbsolutePath());

		return true;
	}


}
