package com.yuncore.bdsync.util;

import java.io.File;

import com.yuncore.bdsync.entity.LocalFile;

public class FileUtil {

	private static String BYTE_SIZE_UNIT[] = { "byte", "KB", "MB", "GB", "TB" };

	public static final LocalFile getLocalFile(String root, String path) {
		final File file = new File(root, path);
		if (file.exists()) {
			final LocalFile localFile = new LocalFile();
			localFile.setDir(file.isDirectory());
			if (file.isDirectory()) {
				localFile.setLength(0);
			} else {
				localFile.setLength(file.length());
			}
			localFile.setMtime((int) file.lastModified());
			localFile.setPath(path);
			return localFile;
		}
		return null;
	}

	public static final boolean rmDirFile(String dir) {
		final File dirFile = new File(dir);
		if (dirFile.exists()) {
			final File[] listFiles = dirFile.listFiles();
			if (listFiles != null) {
				for (File f : listFiles) {
					if (!f.delete()) {
						return false;
					}
				}
			}
		}
		return false;
	}

	public static final String byteSizeToHuman(long size) {
		final StringBuilder builder = new StringBuilder();
		int i = 0;
		double unit = size;
		double temp = 0;
		while ((temp = (unit / 1024)) >= 1) {
			i++;
			unit = temp;
		}
		builder.append(String.format("%.1f", unit));
		// builder.append(".").append(size % 1024);
		builder.append(BYTE_SIZE_UNIT[i]);
		return builder.toString();
	}

}
