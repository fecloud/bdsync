package com.yuncore.bdsync.api;

import java.util.Map;

import com.yuncore.bdsync.entity.CloudFile;
import com.yuncore.bdsync.entity.CloudPageFile;
import com.yuncore.bdsync.entity.CloudRmResult;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.entity.MkDirResult;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.http.HttpUploadFile.FileOutputListener;
import com.yuncore.bdsync.http.HttpUploadFile.FileSource;

public interface FSApi {

	/**
	 * 分块上传块大小
	 */
	int RAPIDUPLOAD = (256 * 1024);
	
	/**
	 * bdstoken
	 */
	String BDSTOKEN = "MYBDSTOKEN";
	
	/**
	 * 每页数量
	 */
	int PAGESIZE = 200000;
	
//	/**
//	 * 登录
//	 * 
//	 * @param username
//	 * @param password
//	 * @return
//	 */
//	public boolean login(String username, String password) throws ApiException;
//
//	/**
//	 * 检测是否登录
//	 * 
//	 * @return
//	 */
//	public boolean islogin() throws ApiException;

	/**
	 * 下载文件
	 * 
	 * @param file
	 * @return
	 */
	public DownloadInputStream download(LocalFile file) throws ApiException;

	/**
	 * 下载文件(断点)
	 * 
	 * @param file
	 * @return
	 */
	public DownloadInputStream download(LocalFile file, long range)
			throws ApiException;

	/**
	 * 到page 取参数
	 * 
	 * @return
	 */
	public Map<String, String> diskHomePage() throws ApiException;
	
//	/**
//	 * 上传文件
//	 * @param filename 本地文件路径
//	 * @param dir 服务器路径
//	 * @return
//	 * @throws ApiException
//	 */
//	public boolean upload(String localpath, String cloudpath)throws ApiException;
//	
//	/**
//	 * 上传文件
//	 * @param filename 本地文件路径
//	 * @param dir 服务器路径
//	 * @param listener 数据写入监听
//	 * @return
//	 * @throws ApiException
//	 */
//	public boolean upload(String localpath, String cloudpath, OutputDataListener listener)throws ApiException;
	
	/**
	 * 当前用户
	 * 
	 * @return
	 */
	public String who() throws ApiException;
	
	
	/**
	 * 上传文件
	 * @param filename 本地文件路径
	 * @param dir 服务器路径
	 * @param listener 数据写入监听
	 * @return
	 * @throws ApiException
	 */
	public String uploadTmpFile(FileSource soure, FileOutputListener listener)throws ApiException;
	
	/**
	 * 根据md5创建文件
	 * @param path
	 * @param size
	 * @param block_list
	 * @return
	 */
	public boolean createFile(String path, long size, String [] block_list, boolean overwrite) throws ApiException;
	
	/**
	 * 秒传
	 * @param filename 本地文件路径
	 * @param dir 服务器路径
	 * @return
	 * @throws ApiException
	 */
	public boolean secondUpload(String localpath, String content_md5, String cloudpath, boolean overwrite)
			throws ApiException;
	
	/**
	 * 
	 * @param dir
	 * @return
	 * @throws ApiException
	 */
	public MkDirResult mkdir(String dir) throws ApiException ;
	
//	/**
//	 * 文件或者目录是否存在
//	 * 
//	 * @param file
//	 * @return
//	 */
//	public CloudFile fileExists(String file) throws ApiException;
	
	/**
	 * 文件或者目录是否存在
	 * 
	 * @param file
	 * @return
	 */
	public boolean exists(String file) throws ApiException;
	
	/**
	 * 删除文件或文件夹
	 * 
	 * @param filename
	 * @return
	 */
	public CloudRmResult rm(String filename) throws ApiException;
	
	/**
	 * 删除文件或文件夹
	 * 
	 * @param filename
	 * @return
	 */
	public CloudRmResult rm(String [] filename) throws ApiException;
	

	/**
	 * 列表当前目录的文件(包含文件夹)
	 * {"errno":-9,"request_id":8897598895336496977} //目录不存在
	 * @param dir
	 * @return
	 */
	public CloudPageFile list(String dir) throws ApiException;

	/**
	 * 列表当前目录的文件(包含文件夹)
	 * 
	 * @param dir
	 * @return
	 
	public CloudPageFile list(String dir, int page) throws ApiException;
*/
	/**
	 * 列表当前目录的文件(包含文件夹)
	 * 
	 * @param dir
	 * @return
	 */
	public CloudPageFile list(String dir, int page, int page_num)
			throws ApiException;
	
	/**
	 * 取文件或者文件夹元信息
	 * @param file
	 * @return
	 */
	public CloudFile getMeta(String file)  throws ApiException;
	
	/**
	 * 合并上传文件
	 * @param file
	 * @param block_list
	 * @return
	 * @throws ApiException
	 */
	public CloudFile createSuperFile(String path, String [] block_list)throws ApiException;
	
}
