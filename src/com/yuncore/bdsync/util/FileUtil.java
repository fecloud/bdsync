package com.yuncore.bdsync.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.yuncore.bdsync.entity.LocalFile;

public class FileUtil {

	private static String BYTE_SIZE_UNIT[] = { "BYTE", "KB", "MB", "GB", "TB" };

	/**
	 * 读取目录
	 * 
	 * @param dir
	 * @return
	 */
	public static List<LocalFile> listFiles(String root, String dir, long session) {
		final File file = new File(root, dir);
		if (file.exists() && file.isDirectory()) {
			File[] listFiles = file.listFiles();
			if (null != listFiles) {
				final List<LocalFile> list = new ArrayList<LocalFile>();
				LocalFile localFile = null;
				boolean file_separator = File.separator.equals("\\");
				if (file_separator) {
					for (File f : listFiles) {
						localFile = new LocalFile();
						localFile.setPath(f.getAbsolutePath().substring(root.length()).replace("\\", "/"));
						localFile.setMtime(f.lastModified() / 1000);
						localFile.setLength(f.length());
						localFile.setDir((f.isFile() ? false : true));
						localFile.setSession(session);
						localFile.setfId(localFile.toFid());
						list.add(localFile);
					}
				} else {
					for (File f : listFiles) {
						localFile = new LocalFile();
						localFile.setPath(f.getAbsolutePath().substring(root.length()));
						localFile.setMtime(f.lastModified() / 1000);
						localFile.setLength(f.length());
						localFile.setDir((f.isFile() ? false : true));
						localFile.setSession(session);
						localFile.setfId(localFile.toFid());
						list.add(localFile);
					}
				}

				return list;
			}
		}
		return null;
	}

	public static boolean rmDirFile(String dir) {
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

	public static String byteSizeToHuman(long size) {
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
