/**
 * @(#) UpLoadFileBlockConent.java Created on 2015年9月28日
 *
 * 
 */
package com.yuncore.bdsync.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.StatusMent;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.CloudFile;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.http.HttpUploadFile.FileOutputListener;
import com.yuncore.bdsync.http.HttpUploadFile.FileSource;
import com.yuncore.bdsync.util.Log;

/**
 * The class <code>UpLoadFileBlockConent</code>
 * <p>
 * 分块上传(每10m一块)
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadFileBlockConent implements UpLoadCheckFileStep, FileSource, FileOutputListener {

	private static final String TAG = "UpLoadFileBlockConent";
	/**
	 * 每10m一块
	 */
	private static final long SCLIE_SIZE = 1024l * 1024l * 10l;

	private String root;

	private String tmpDir;

	private FSApi fsApi;

	private LocalFile uploadFile;

	private UpLoadOperate uploadOperate;

	private FileInputStream fileInputStream;

	private String sclieFileName;

	private List<String> sclies = new ArrayList<String>();

	/**
	 * @param root
	 * @param fsApi
	 */
	public UpLoadFileBlockConent(String root, String tmpDir, FSApi fsApi) {
		super();
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
		this.uploadFile = uploadFile;
		this.uploadOperate = uploadOperate;
		this.sclieFileName = tmpDir + File.separator + uploadFile.getfId() + ".sclies";

		Log.d(TAG, "UpLoadFileBlockConent uploading...");

		readSlicesMd5();

		final int nums = coutBlock();
		for (int i = sclies.size(); i < nums; i++) {
			if (!uploadOperate.getUpLoadStatus()) {
				StatusMent.setProperty(StatusMent.UPLOAD_SIZE, 0);
				return true;
			}

			String md5 = null;

			try {
				Log.d(TAG, "uploading block " + i);
				md5 = fsApi.uploadTmpFile(this, this);
			} catch (ApiException e) {
				i--;
				continue;
			} finally {
				if (null != fileInputStream) {
					try {
						fileInputStream.close();
					} catch (IOException e) {
					}
				}
			}

			if (md5 == null) {
				i--;
				continue;
			} else {
				sclies.add(md5);
				writeSlicesMd5(md5);
			}

		}

		if (!uploadOperate.getUpLoadStatus()) {
			StatusMent.setProperty(StatusMent.UPLOAD_SIZE, 0);
			return true;
		}

		try {
			final CloudFile createSuperFile = fsApi.createSuperFile(uploadFile.getAbsolutePath(), scliesToArray());
			if (createSuperFile != null) {
				deleteSlicesMd5();
				uploadOperate.addAnotherRecord(uploadFile);
				uploadOperate.deleteRecord(uploadFile);
			}

		} catch (ApiException e) {
		}

		StatusMent.setProperty(StatusMent.UPLOAD_SIZE, 0);

		return true;
	}

	/**
	 * 写入已上传的md5
	 * 
	 * @param slice_md5
	 */
	private final void writeSlicesMd5(String slice_md5) {
		try {
			final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(sclieFileName, true),
					"UTF-8");
			writer.write(slice_md5);
			writer.write("\r\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
		}
	}

	/**
	 * 
	 */
	private final void deleteSlicesMd5() {
		final File file = new File(sclieFileName);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 读取已上传的md5
	 */
	private final void readSlicesMd5() {
		try {
			final File file = new File(sclieFileName);
			if (file.exists()) {
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file), "UTF-8"));
				String line = null;
				while (null != (line = reader.readLine())) {
					sclies.add(line);
				}
				reader.close();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 计算要几块
	 * 
	 * @return
	 */
	private final int coutBlock() {
		int nums = (int) (uploadFile.getLength() / SCLIE_SIZE);
		if (uploadFile.getLength() % SCLIE_SIZE > 0) {
			nums++;
		}
		return nums;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.http.HttpUploadFile.FileOutputListener#onWrite(long,
	 * long)
	 */
	@Override
	public void onWrite(long sum, long commit) {
		final long commitd = sclies.size() * SCLIE_SIZE + commit;
		StatusMent.setProperty(StatusMent.UPLOAD_SIZE, commitd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getFileLength()
	 */
	@Override
	public long getFileLength() {
		final long uploaded = sclies.size() * SCLIE_SIZE;
		final long waitUpload = uploadFile.getLength() - uploaded;
		if (waitUpload >= SCLIE_SIZE) {
			return SCLIE_SIZE;
		} else {
			return waitUpload;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getFileName()
	 */
	@Override
	public String getFileName() {
		return "part";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#isInterrupt()
	 */
	@Override
	public boolean isInterrupt() {
		return uploadOperate.getUpLoadStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		long uploaded = sclies.size() * SCLIE_SIZE;
		if (uploadFile.getLength() > uploaded) {
			fileInputStream = new FileInputStream(root + uploadFile.getAbsolutePath());
			while(uploaded > 0){
				//因为这个skip不一定每次都跳过指定的字节,所以要对返回值作判断
				final long skip = fileInputStream.skip(uploaded);
				uploaded -=skip;
			}
			
			return fileInputStream;
		}
		return null;
	}

	private final String[] scliesToArray() {
		final int size = sclies.size();
		final String[] array = new String[size];
		sclies.toArray(array);
		return array;
	}

}
