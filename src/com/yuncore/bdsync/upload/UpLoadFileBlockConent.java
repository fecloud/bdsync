/**
 * @(#) UpLoadFileBlockConent.java Created on 2015年9月28日
 *
 * 
 */
package com.yuncore.bdsync.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>UpLoadFileBlockConent</code>
 * <p>
 * 分块上传(每10m一块)
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class UpLoadFileBlockConent implements UpLoadCheckFileStep {

	/**
	 * 每10m一块
	 */
	private static final long SCLIE_SIZE = 1024l * 1024l * 10l;

	private String root;

	private String tmpDir;

	private FSApi fsApi;

	private LocalFile uploadFile;

	private UpLoadOperate uploadOperate;

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
		this.sclies.clear();

		return false;
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
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(sclieFileName), "UTF-8"));
			String line = null;
			while (null != (line = reader.readLine())) {
				sclies.add(line);
			}
			reader.close();
		} catch (Exception e) {
		}
	}

}
