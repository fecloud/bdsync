/**
 * UploadBlockContentFileSourceOutputListener.java Created on 2017年12月28日
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
import com.yuncore.bdsync.entity.DoingFile;
import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>UploadBlockContentFileSourceOutputListener</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UploadBlockContentFileSourceOutputListener extends
		UploadFileSourceOutputListener {

	protected String sclieFileName;

	protected List<String> sclies = new ArrayList<String>();

	/**
	 * @param root
	 * @param uploadFile
	 * @param uploadOperate
	 */
	public UploadBlockContentFileSourceOutputListener(String root,
			LocalFile uploadFile, UpLoadOperate uploadOperate,
			String sclieFileName) {
		super(root, uploadFile, uploadOperate);
		this.sclieFileName = sclieFileName;
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
		final long commitd = sclies.size() * UpLoadFileBlockConent.SCLIE_SIZE
				+ commit;
		StatusMent.getDoingfile().put(uploadFile.getAbsolutePath(),
				new DoingFile(uploadFile).setDoingSize(commitd));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.HttpUploadFile.FileSource#getFileLength()
	 */
	@Override
	public long getFileLength() {
		final long uploaded = sclies.size() * UpLoadFileBlockConent.SCLIE_SIZE;
		final long waitUpload = uploadFile.getLength() - uploaded;
		if (waitUpload >= UpLoadFileBlockConent.SCLIE_SIZE) {
			return UpLoadFileBlockConent.SCLIE_SIZE;
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
		long uploaded = sclies.size() * UpLoadFileBlockConent.SCLIE_SIZE;
		if (uploadFile.getLength() > uploaded) {
			if (fileInputStream != null) {
				fileInputStream = new FileInputStream(root
						+ uploadFile.getAbsolutePath());
				while (uploaded > 0) {
					// 因为这个skip不一定每次都跳过指定的字节,所以要对返回值作判断
					final long skip = fileInputStream.skip(uploaded);
					uploaded -= skip;
				}
			}
			return fileInputStream;
		}
		return null;
	}

	public final String[] scliesToArray() {
		final int size = sclies.size();
		final String[] array = new String[size];
		sclies.toArray(array);
		return array;
	}

	/**
	 * 写入已上传的md5
	 * 
	 * @param slice_md5
	 */
	public final void writeSlicesMd5(String slice_md5) {
		try {
			final OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(sclieFileName, true), "UTF-8");
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
	public final void deleteSlicesMd5() {
		final File file = new File(sclieFileName);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 读取已上传的md5
	 */
	public final void readSlicesMd5() {
		try {
			sclies.clear();
			final File file = new File(sclieFileName);
			if (file.exists()) {
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file),
								"UTF-8"));
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
	public final int coutBlock() {
		int nums = (int) (uploadFile.getLength() / UpLoadFileBlockConent.SCLIE_SIZE);
		if (uploadFile.getLength() % UpLoadFileBlockConent.SCLIE_SIZE > 0) {
			nums++;
		}
		return nums;
	}

	/**
	 * @return the sclieFileName
	 */
	public String getSclieFileName() {
		return sclieFileName;
	}

	/**
	 * @param sclieFileName
	 *            the sclieFileName to set
	 */
	public void setSclieFileName(String sclieFileName) {
		this.sclieFileName = sclieFileName;
	}

	/**
	 * @return the sclies
	 */
	public List<String> getSclies() {
		return sclies;
	}

	/**
	 * @param sclies
	 *            the sclies to set
	 */
	public void setSclies(List<String> sclies) {
		this.sclies = sclies;
	}

}
