/**
 * @(#) UpLoadOperate.java Created on Sep 22, 2015
 *
 * 
 */
package com.yuncore.bdsync.upload;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>UpLoadOperate</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public interface UpLoadOperate {

	/**
	 * 删了数据库记录
	 * 
	 * @param file
	 * @return
	 */
	boolean deleteRecord(LocalFile file);

	/**
	 * 返回上传的状态
	 * 
	 * @return
	 */
	boolean getUpLoadStatus();
	
	/**
	 * 添加对方表的记录
	 * @param file
	 * @return
	 */
	boolean addAnotherRecord(LocalFile file);
	
}
