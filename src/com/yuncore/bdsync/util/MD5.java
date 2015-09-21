package com.yuncore.bdsync.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class MD5 {

	/**
	 * 字节数组转换为 十六进制数组
	 * 
	 * @param bytes
	 * @return
	 */
	public static final String bytes2String(byte[] bytes) {
		if (bytes == null) {
			return "";
		}
		final StringBuilder hexString = new StringBuilder();
		String shaHex = null;
		int one = 0x0;
		for (int i = 0; i < bytes.length; i++) {
			one = bytes[i] & 0xFF;
			if (one < 0x10) {
				hexString.append("0");
			}
			shaHex = Integer.toHexString(one);
			hexString.append(shaHex);
		}
		return hexString.toString();
	}

	/**
	 * md5加密
	 * 
	 * @param s
	 * @return
	 */
	public final static String md5(String s) {
		try {
			final byte[] btInput = s.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest digest = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			digest.update(btInput);
			// 获得密文
			final byte[] md = digest.digest();
			return bytes2String(md);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 计算文件的md5
	 * 
	 * @param path
	 * @return
	 */
	public final static String md5File(String path) {
		final File file = new File(path);
		if (file.exists() && file.canRead()) {
			try {
				// 获得MD5摘要算法的 MessageDigest 对象
				final MessageDigest digest = MessageDigest.getInstance("MD5");
				final FileInputStream in = new FileInputStream(file);
				
				byte[] buffer = null;
				if (file.length() >= (1024 * 1024 * 500)) {
					// 30M读取缓存
					buffer = new byte[1024 * 1024 * 30];
				} else {
					// 5M读取缓存
					buffer = new byte[1024 * 1024 * 5];
				}
				
				int len = -1;
				while (-1 != (len = in.read(buffer))) {
					digest.update(buffer, 0, len);
				}
				in.close();

				// 获得密文
				final byte[] md = digest.digest();
				return bytes2String(md);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * 计算文件的md5,文件前多少字节
	 * 
	 * @param path
	 * @param count
	 * @return
	 */
	public final static String md5File(String path, int count) {
		final File file = new File(path);
		if (file.exists() && file.canRead() && file.length() >= count) {
			try {
				int unRead = count;
				// 获得MD5摘要算法的 MessageDigest 对象
				final MessageDigest digest = MessageDigest.getInstance("MD5");
				final FileInputStream in = new FileInputStream(file);
				final byte[] buffer = new byte[1024 * 1024];
				int len = -1;
				while (-1 != (len = in.read(buffer))) {
					if (unRead > len) {
						digest.update(buffer, 0, len);
					} else {
						digest.update(buffer, 0, unRead);
					}
					unRead -= len;
					if (unRead <= 0) {
						break;
					}
				}
				in.close();

				// 获得密文
				final byte[] md = digest.digest();
				return bytes2String(md);
			} catch (Exception e) {
			}
		}
		return null;
	}

}
