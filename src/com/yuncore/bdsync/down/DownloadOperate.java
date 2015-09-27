/**
 * @(#) DownloadOperate.java Created on Sep 19, 2015
 *
 * 
 */
package com.yuncore.bdsync.down;

import com.yuncore.bdsync.entity.LocalFile;

/**
 * The class <code>DownloadOperate</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public interface DownloadOperate {

	/**
	 * 删了数据库记录
	 * 
	 * @param file
	 * @return
	 */
	boolean deleteRecord(LocalFile file);

	/**
	 * 添加对方表的记录
	 * @param file
	 * @return
	 */
	boolean addAnotherRecord(LocalFile file);
	
	/**
	 * 返回下载的状态
	 * 
	 * @return
	 */
	boolean getDownLoadStatus();

}
