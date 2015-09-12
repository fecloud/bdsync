package com.yuncore.bdsync.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HttpOutput extends Http {

	private String file;

	private long filesize;

	public HttpOutput(String url, String file) {
		super(url, Method.POST);
		this.file = file;

	}

	protected int getInputSize() {
		final File filename = new File(file);
		filesize = filename.length();
		return (int) filesize;
	}

	@Override
	protected boolean addFormData() throws IOException {
		conn.addRequestProperty("Content-Type", "application/octet-stream");
		OutputStream out = new DataOutputStream(conn.getOutputStream());
		FileInputStream in = new FileInputStream(file);
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while ((bytes = in.read(bufferOut)) != -1) {
			out.write(bufferOut, 0, bytes);
			out.flush();
		}
		in.close();
		out.flush();
		out.close();
		return true;
	}

}
