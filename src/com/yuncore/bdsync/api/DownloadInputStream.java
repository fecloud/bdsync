/**
 * 
 */
package com.yuncore.bdsync.api;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ouyangfeng
 * 
 */
public class DownloadInputStream {

	private InputStream in;

	private long length;

	private boolean range;

	private String contentMd5;

	public DownloadInputStream(InputStream in) {
		super();
		this.in = in;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public int read() throws IOException {
		if (null == in) {
			return -1;
		}
		return in.read();
	}

	public int read(byte b[]) throws IOException {
		if (null == in) {
			return -1;
		}
		return in.read(b);
	}

	public int read(byte b[], int off, int len) throws IOException {
		if (null == in) {
			return -1;
		}
		return in.read(b, off, len);
	}

	public boolean isRange() {
		return range;
	}

	public void setRange(boolean range) {
		this.range = range;
	}

	/**
	 * @return the contentMd5
	 */
	public String getContentMd5() {
		return contentMd5;
	}

	/**
	 * @param contentMd5
	 *            the contentMd5 to set
	 */
	public void setContentMd5(String contentMd5) {
		this.contentMd5 = contentMd5;
	}

	public void close() throws IOException {
		if (null != in)
			in.close();
	}

}
