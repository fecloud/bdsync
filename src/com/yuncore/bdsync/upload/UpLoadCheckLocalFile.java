/**
 * @(#) UpLoadCheckLocalFile.java Created on Sep 22, 2015
 *
 * 
 */
package com.yuncore.bdsync.upload;

import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.util.FileUtil;

/**
 * The class <code>UpLoadCheckLocalFile</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadCheckLocalFile implements UpLoadCheckFileStep {

	private String root;

	/**
	 * @param root
	 */
	public UpLoadCheckLocalFile(String root) {
		super();
		this.root = root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.upload.UpLoadCheckFileStep#check(com.yuncore.bdsync
	 * .entity.LocalFile, com.yuncore.bdsync.upload.UpLoadOperate)
	 */
	@Override
	public boolean check(LocalFile uploadFile, UpLoadOperate uploadOperate) {
		final LocalFile loalFile = FileUtil.getLocalFile(root,
				uploadFile.getAbsolutePath());
		if (loalFile == null) {
			// 本地文件都不在了怎么上传
			uploadOperate.deleteRecord(uploadFile);
			return false;
		}

		if (uploadFile.toFid().equals(loalFile.toFid())) {
			// 类型,长度相同
			return true;
		}

		uploadOperate.deleteRecord(uploadFile);
		return false;
	}

}
