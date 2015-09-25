/**
 * @(#) DeleteFile.java Created on Sep 25, 2015
 *
 * 
 */
package com.yuncore.bdsync.delete;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DeleteFile</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DeleteFile implements DeleteCheckFileStep {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.DeleteCheckFileStep#check(com.yuncore.bdsync
	 * .entity.LocalFile, com.yuncore.bdsync.entity.LocalFile,
	 * com.yuncore.bdsync.delete.DeleteOperate)
	 */
	@Override
	public boolean check(LocalFile deleteFile, LocalFile compareFile,
			DeleteOperate deleteOperate) {
		if (deleteOperate.deleteFile(deleteFile)) {
			deleteOperate.deleteRecord(deleteFile);
			deleteOperate.deleteAnotherRecord(deleteFile);
			return true;
		}
		return true;
	}
}
