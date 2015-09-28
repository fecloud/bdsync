/**
 * @(#) DeteleCheckLocal.java Created on 2015年9月18日
 *
 * 
 */
package com.yuncore.bdsync.delete;

import com.yuncore.bdsync.delete.DeleteCheckFileStep;
import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DeteleCheckLocal</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class DeteleCheckLocal implements DeleteCheckFileStep {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.delete.DeleteCheckFileStep#check(com.yuncore.bdsync
	 * .entity.LocalFile, com.yuncore.bdsync.entity.LocalFile,
	 * com.yuncore.bdsync.delete.DeleteOperate)
	 */
	@Override
	public boolean check(LocalFile deleteFile, LocalFile compareFile, DeleteOperate deleteOperate) {
		if (compareFile == null) {
			// 说明目录可能是自己删除的,进入删除文件
			deleteOperate.deleteRecord(deleteFile);
			deleteOperate.deleteAnotherRecord(deleteFile);
			return false;
		} else {
			if (deleteFile.toFid().equals(compareFile.toFid())) {
				if (deleteFile.isFile()) {
					return true;
				} else if (deleteFile.getMtime() > compareFile.getMtime()) {
					return true;
				}
			}
		}
		deleteOperate.deleteRecord(deleteFile);
		return false;
	}
}
