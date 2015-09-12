/**
 * 
 */
package com.yuncore.bdsync.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.yuncore.bdsync.util.MD5;

/**
 * @author ouyangfeng
 * 
 */
public class HttpFormOutput extends Http {

	private String file;

	private String filestart;

	private String fileend;

	private long filesize;

	private String BOUNDARY = "---------------------------"
			+ new Random().nextLong();

	private OutputDataListener listener;
	
	private boolean upload2;
	
	private String filemd5;

	public HttpFormOutput(String url, String file) {
		super(url, Method.POST);
		this.file = file;
	}
	public HttpFormOutput(String url, String file, boolean upload2) {
		super(url, Method.POST);
		this.file = file;
		this.upload2  = upload2;
	}

	public HttpFormOutput(String url, String file, OutputDataListener listener) {
		this(url, file);
		this.listener = listener;
	}
	
	public HttpFormOutput(String url, String file, OutputDataListener listener, boolean upload2) {
		this(url, file);
		this.listener = listener;
		this.upload2  = upload2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.http.PCSHttp#addRequestProperty()
	 */
	@Override
	protected void addRequestProperty() {
		conn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + BOUNDARY);
		if(upload2){
			conn.setFixedLengthStreamingMode(getInputSize2());
		}	else  {
			conn.setFixedLengthStreamingMode(getInputSize());
		}
	}

	/**
	 * 取文件内容大小
	 * 
	 * @return
	 */
	protected int getInputSize() {

		try {
			final File filename = new File(file);
			filesize = filename.length();

			StringBuffer strBuf = new StringBuffer();
			strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
			strBuf.append("Content-Disposition: form-data; name=\"file\"; filename=\""
					+ filename.getName() + "\"\r\n");
			strBuf.append("Content-Type:application/octet-stream\r\n\r\n");

			filestart = strBuf.toString();

			fileend = ("\r\n--" + BOUNDARY + "--\r\n");

			filesize += filestart.getBytes("UTF-8").length;
			filesize += fileend.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
		}
		return (int) filesize;
	}
	
	protected int getInputSize2(){
		try {
			final File filename = new File(file);
			filesize = filename.length();

			final StringBuffer strBuf = new StringBuffer();
			strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
			
			strBuf.append("Content-Disposition: form-data; name=\"Filename\"").append("\r\n\r\n");
			strBuf.append(filename.getName());
			strBuf.append("\r\n--" + BOUNDARY + "\r\n");
			
			strBuf.append("Content-Disposition: form-data; name=\"Filedata\"; filename=\""
					+ filename.getName() + "\"\r\n");
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
		final File infile = new File(file);
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
		final FileInputStream in = new FileInputStream(infile);
		long commit = 0;
		int len = 0;
		// 缓存大小
		final byte[] buffer = new byte[1024 * 50];
		while ((len = in.read(buffer)) != -1) {
			digest.update(buffer, 0, len);
			out.write(buffer, 0, len);
			out.flush();
			commit += len;
			if(null != listener){
				listener.onWrite(infile.length(), commit);
			}
		}
		in.close();
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
	public interface OutputDataListener {

		/**
		 * 
		 * @param sum
		 *            总长度
		 * @param commit
		 *            已完成长度
		 */
		void onWrite(long sum, long commit);

	}

}
