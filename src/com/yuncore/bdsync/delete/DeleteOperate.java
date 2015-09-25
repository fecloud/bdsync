/**
 * @(#) DeleteOperate.java Created on Sep 25, 2015
 *
 * 
 */
package com.yuncore.bdsync.delete;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DeleteOperate</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public interface DeleteOperate {

	/**
	 * 删了数据库记录
	 * 
	 * @param file
	 * @return
	 */
	boolean deleteRecord(LocalFile file);
	
	/**
	 * 删除真实文件
	 * @param file
	 * @return
	 */
	boolean deleteFile(LocalFile file);
	
	/**
	 * 删除对方表的记录
	 * @param file
	 * @return
	 */
	boolean deleteAnotherRecord(LocalFile file);
	
	/**
	 * 返回下载的状态
	 * 
	 * @return
	 */
	boolean getDeleteStatus();
	
}
