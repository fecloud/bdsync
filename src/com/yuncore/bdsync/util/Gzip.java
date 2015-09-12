package com.yuncore.bdsync.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {

	/**
	 * 把指定的字节数组用gzip压缩
	 * 
	 * @param src
	 * @return 压缩过的字节数组
	 */
	public static byte[] gzip(byte[] src) {
		if (null != src) {
			final ByteArrayOutputStream os = new ByteArrayOutputStream(
					src.length);
			try {
				final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
						os);
				gzipOutputStream.write(src);
				gzipOutputStream.flush();
				gzipOutputStream.close();
				return os.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String readToStringByGzip(InputStream in, String charset)
			throws IOException {
		if (null != in) {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			// 1KB的缓冲区
			int read = -1;
			while ((read = in.read(buffer)) > 0) { // 读取到缓冲区
				bos.write(buffer, 0, read);
			}

			if (bos.size() > 0) {
				byte[] ungzip = ungzip(bos.toByteArray());
				return new String(ungzip, charset);
			}

		}
		return null;
	}

	/**
	 * 把指定的字节数组用gzip解压缩
	 * 
	 * @param src
	 * @return 解压缩过的字节数组
	 */
	public static byte[] ungzip(byte[] src) {
		if (null != src) {
			final ByteArrayInputStream is = new ByteArrayInputStream(src);
			final ByteArrayOutputStream os = new ByteArrayOutputStream(
					src.length);
			try {
				final GZIPInputStream in = new GZIPInputStream(is);
				final byte[] buffer = new byte[512];
				int len = 0;
				while ((len = in.read(buffer)) != -1) {
					os.write(buffer, 0, len);
					os.flush();
				}
				in.close();
				return os.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

}
