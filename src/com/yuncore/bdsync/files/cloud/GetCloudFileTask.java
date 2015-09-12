/**
 * 
 */
package com.yuncore.bdsync.files.cloud;

import com.yuncore.bdsync.task.Task;

/**
 * @author ouyangfeng
 * 
 */
public class GetCloudFileTask implements Task {

	private String dir;

	public GetCloudFileTask(String dir) {
		super();
		this.dir = dir;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}
}
