/**
 * @(#) HttpUploadFile.java Created on 2015年9月28日
 *
 * 
 */
package com.yuncore.bdsync.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.yuncore.bdsync.util.MD5;

/**
 * The class <code>HttpUploadFile</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class HttpUploadFile extends Http {

	private String filestart;

	private String fileend;

	private long filesize;

	private String BOUNDARY = "---------------------------" + new Random().nextLong();

	private FileOutputListener output;

	private FileSource source;

	private String filemd5;

	/**
	 * @param output
	 * @param source
	 */
	public HttpUploadFile(String url, FileSource source, FileOutputListener output) {
		super(url, Method.POST);
		this.output = output;
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.http.PCSHttp#addRequestProperty()
	 */
	@Override
	protected void addRequestProperty() {
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		conn.setFixedLengthStreamingMode(getInputSize());
	}

	protected int getInputSize() {
		try {
			filesize = source.getFileLength();

			final StringBuffer strBuf = new StringBuffer();
			strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");

			strBuf.append("Content-Disposition: form-data; name=\"Filename\"").append("\r\n\r\n");
			strBuf.append(source.getFileName());
			strBuf.append("\r\n--" + BOUNDARY + "\r\n");

			strBuf.append(
					"Content-Disposition: form-data; name=\"Filedata\"; filename=\"" + source.getFileName() + "\"\r\n");
			strBuf.append("Content-Type:application/octet-stream\r\n\r\n");

			filestart = strBuf.toString();

			final StringBuffer eStrBuf = new StringBuffer();
			eStrBuf.append("\r\n--" + BOUNDARY + "\r\n");
			eStrBuf.append("Content-Disposition: form-data; name=\"Upload\"\r\n\r\n");
			eStrBuf.append("Submit Query");
			eStrBuf.append("\r\n--" + BOUNDARY + "--\r\n");
			fileend = eStrBuf.toString();

			filesize += filestart.getBytes("UTF-8").length;
			filesize += fileend.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
		}
		return (int) filesize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.http.PCSHttp#addFormData()
	 */
	@Override
	protected boolean addFormData() throws IOException {
		final OutputStream out = conn.getOutputStream();
		out.write(filestart.getBytes("UTF-8"));

		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
		final InputStream in = source.getInputStream();
		long commit = 0;
		int len = 0;
		// 缓存大小
		final byte[] buffer = new byte[1024 * 50];
		while ((len = in.read(buffer)) != -1) {

			if (!source.isInterrupt()) {
				return false;
			}

			digest.update(buffer, 0, len);
			out.write(buffer, 0, len);
			out.flush();

			if (!source.isInterrupt()) {
				return false;
			}

			commit += len;
			if (null != output) {
				output.onWrite(source.getFileLength(), commit);
			}
		}
		final byte[] md = digest.digest();
		this.filemd5 = MD5.bytes2String(md);
		out.write(fileend.getBytes("UTF-8"));
		out.flush();

		return true;
	}

	public String getFilemd5() {
		return filemd5;
	}

	/**
	 * 写出数据监听
	 * 
	 * @author wjh
	 *
	 */
	public interface FileOutputListener {

		/**
		 * 
		 * @param sum
		 *            总长度
		 * @param commit
		 *            已完成长度
		 */
		void onWrite(long sum, long commit);

	}

	public interface FileSource {

		long getFileLength();

		String getFileName();

		/**
		 * 是否放弃发送
		 * 
		 * @return
		 */
		boolean isInterrupt();

		InputStream getInputStream() throws IOException;

	}

}
