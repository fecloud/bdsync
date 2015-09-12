package com.yuncore.bdsync.files;

import java.util.HashSet;
import java.util.Set;

public class BDSyncFileExclude implements FileExclude {

	protected Set<String> excludes = new HashSet<String>();

	@Override
	public synchronized Set<String> getExcludes() {
		return excludes;
	}

	@Override
	public synchronized boolean rmExclude(String exclude) {
		if (!excludes.isEmpty() && excludes.contains(exclude)) {
			return excludes.remove(exclude);
		}
		return false;
	}

	@Override
	public synchronized void addExclude(Set<String> files) {
		excludes.addAll(files);
	}

	@Override
	public synchronized void addExclude(String file) {
		excludes.add(file);
	}

}
