package com.yuncore.bdsync.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileGzip {

	private String src;

	private String dest;

	public FileGzip(String src, String dest) {
		this.src = src;
		this.dest = dest;
	}

	/**
	 * 压缩
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean gzip() throws IOException {
		final File file = new File(src);
		if (file.exists()) {
			final File destFile = new File(dest);
			if (!destFile.exists()) {
				destFile.getParentFile().mkdirs();
			}

			final GZIPOutputStream out = new GZIPOutputStream(
					new FileOutputStream(dest));
			final FileInputStream in = new FileInputStream(src);

			final byte[] buffer = new byte[1024 * 100];
			int len = -1;

			while (-1 != (len = in.read(buffer))) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.flush();
			out.close();
			return true;

		}
		return false;
	}

	/**
	 * 解压
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean unGzip() throws IOException {

		final File file = new File(src);
		if (file.exists()) {
			final File destFile = new File(dest);
			if (!destFile.exists()) {
				destFile.getParentFile().mkdirs();
			}

			final GZIPInputStream in = new GZIPInputStream(new FileInputStream(
					src));
			final FileOutputStream out = new FileOutputStream(dest);

			final byte[] buffer = new byte[1024 * 100];
			int len = -1;

			while (-1 != (len = in.read(buffer))) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.flush();
			out.close();
			return true;

		}
		return false;

	}
}
