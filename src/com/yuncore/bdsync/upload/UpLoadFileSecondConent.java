/**
 * @(#) UpLoadFileSecondConent.java Created on 2015年9月28日
 *
 * 
 */
package com.yuncore.bdsync.upload;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.util.Log;
import com.yuncore.bdsync.util.MD5;

/**
 * The class <code>UpLoadFileSecondConent</code>
 * <p>
 * 秒传文件
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadFileSecondConent implements UpLoadCheckFileStep {

	private static final String TAG = "UpLoadFileSecondConent";

	private String croot;
	
	private String root;

	private FSApi fsApi;

	/**
	 * @param root
	 * @param root2 
	 * @param fsApi
	 */
	public UpLoadFileSecondConent(String croot, String root, FSApi fsApi) {
		super();
		this.croot = croot;
		this.root = root;
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
		if (uploadFile.isFile()) {
			try {
				final String localpath = String.format("%s/%s", root, uploadFile.getAbsolutePath());
				final File file = new File(localpath);
				if (file.exists() && file.canRead()) {

					if (file.length() > FSApi.RAPIDUPLOAD) {

						Log.d(TAG, "try secondFileContext");
						
						// 获得MD5摘要算法的 MessageDigest 对象
						final MessageDigest digest = MessageDigest.getInstance("MD5");
						final FileInputStream in = new FileInputStream(file);

						byte[] buffer = null;
						if (file.length() >= (1024 * 1024 * 500)) {
							// 30M读取缓存
							buffer = new byte[1024 * 1024 * 30];
						} else {
							// 5M读取缓存
							buffer = new byte[1024 * 1024 * 5];
						}

						int len = -1;
						while (-1 != (len = in.read(buffer))) {

							if (!uploadOperate.getUpLoadStatus()) {
								// 收到停止指令
								in.close();
								return false;
							}

							digest.update(buffer, 0, len);
						}
						in.close();

						// 获得密文
						final byte[] md = digest.digest();
						final String content_md5 = MD5.bytes2String(md);
						final boolean result = fsApi.secondUpload(localpath, content_md5, croot + uploadFile.getAbsolutePath(),true);
						if (result) {
							Log.d(TAG, "secondFileContext ok");
							Log.i(TAG, "upload " + uploadFile.getParentPath() + " success");
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
							return false;
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, String.format("secondFileContext file:%s error", uploadFile.getAbsolutePath()), e);
				return false;
			}
		}
		return true;
	}

}
