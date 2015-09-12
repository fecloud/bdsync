package com.yuncore.bdsync.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextUtil {

	public static String readToString(InputStream in, String charset)
			throws IOException {
		if (null != in) {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			// 1KB的缓冲区
			int read = -1;
			while ((read = in.read(buffer)) > 0) { // 读取到缓冲区
				bos.write(buffer, 0, read);
			}
			return new String(bos.toByteArray(), charset);

		}
		return null;
	}

	public static String readFile(String path) {
		try {
			final FileInputStream in = new FileInputStream(path);
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			// 1KB的缓冲区
			int read = -1;
			while ((read = in.read(buffer)) > 0) { // 读取到缓冲区
				bos.write(buffer, 0, read);
			}
			in.close();
			return new String(bos.toByteArray(), "UTF-8");
		} catch (IOException e) {
		}
		return null;
	}
	
	public static final String readResoure(String path){
		try {
			final InputStream in = TextUtil.class.getResourceAsStream(path);
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			// 1KB的缓冲区
			int read = -1;
			while ((read = in.read(buffer)) > 0) { // 读取到缓冲区
				bos.write(buffer, 0, read);
			}
			in.close();
			return new String(bos.toByteArray(), "UTF-8");
		} catch (IOException e) {
		}
		return null;
	}
}
