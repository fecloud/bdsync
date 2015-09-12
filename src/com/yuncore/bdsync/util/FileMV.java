/**
 * 
 */
package com.yuncore.bdsync.util;

import java.io.File;

/**
 * @author ouyangfeng 移动文件或者文件夹
 */
public class FileMV {

	private String path;

	private String topath;

	public FileMV() {
	}

	public FileMV(String path, String topath) {
		super();
		this.path = path;
		this.topath = topath;
	}

	public boolean mv() {
		Log.d("FileMV", String.format("%s to %s", path, topath));
		try {
			if (path != null && topath != null) {
				final File file = new File(path);
				if (file.exists()) {
					if (file.isFile()) {
						return renameFile(path, topath);
					} else {
						return renameDir(path, topath);
					}
				}
			}
		} catch (Exception e) {
			Log.e("FileMV", "rm error", e);
		}
		return false;
	}

	private boolean renameDir(String path, String topath) {
		if (topath.endsWith("/")) {
			final File file = new File(path);
			String name = file.getName();
			final String dest = topath + name;
			return renameFile(path, dest);
		}
		return renameFile(path, topath);
	}

	private boolean renameFile(String src, String dest) {

		if (src != null && dest != null) {
			final File srcfile = new File(src);
			if (srcfile.exists()) {
				final File destfile = new File(dest);
				if (destfile.exists()) {
					destfile.delete();
				}

				if (!destfile.getParentFile().exists()) {
					destfile.getParentFile().mkdirs();
				}
				return srcfile.renameTo(destfile);
			}
		}
		return false;
	}

	public static void main(String[] args) {

		new FileMV("j:\\a", "j:\\b/").mv();

	}

}
