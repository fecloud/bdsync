package com.yuncore.bdsync.api.imple;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.api.BDSYNCURL;
import com.yuncore.bdsync.api.DownloadInputStream;
import com.yuncore.bdsync.api.FSApi;
import com.yuncore.bdsync.app.Context;
import com.yuncore.bdsync.entity.LocalFile;
import com.yuncore.bdsync.entity.CloudFile;
import com.yuncore.bdsync.entity.CloudPageFile;
import com.yuncore.bdsync.entity.CloudRmResult;
import com.yuncore.bdsync.entity.MkDirResult;
import com.yuncore.bdsync.exception.ApiException;
import com.yuncore.bdsync.http.Http;
import com.yuncore.bdsync.http.Http.Method;
import com.yuncore.bdsync.http.HttpFormOutput;
import com.yuncore.bdsync.http.HttpFormOutput.OutputDataListener;
import com.yuncore.bdsync.http.HttpInput;
import com.yuncore.bdsync.http.cookie.HttpCookieContainer;
import com.yuncore.bdsync.util.DateUtil;
import com.yuncore.bdsync.util.Log;
import com.yuncore.bdsync.util.MD5;

public class FSApiImple implements FSApi {

	static final String TAG = "FSApiImple";

	private static boolean DEBUG = false;

	private static volatile Context context;

	public FSApiImple() {
		super();
		inStanceContext();
	}

	private void inStanceContext() {
		try {
			if (null == context)
				context = (Context) Class.forName(Environment.getContextClassName()).newInstance();
		} catch (Exception e) {
		}
	}

	private String getpassport() {
		try {
			final long c_time = DateUtil.current_time_ss();
			final String url = BDSYNCURL.getpassport(c_time);
			final Http http = new Http(url, Method.GET);
			if (http.http()) {
				return http.result();
			}
		} catch (Exception e) {
			Log.e(TAG, "getpassport error", e);
		}
		return null;
	}

	private boolean getbaidu() {
		try {
			final String url = "http://www.baidu.com";
			final Http http = new Http(url, Method.GET);
			if (http.http()) {
				if (http.getResponseCode() == HttpsURLConnection.HTTP_MOVED_TEMP) {
					String location = http.getConnet().getHeaderField("Location");
					Log.d(TAG, "location:" + location);
					if (location != null) {
						final Http httpsbaidu = new Http(location, Method.GET);
						if (httpsbaidu.http()) {
							return true;
						}
					}
				} else if (http.getResponseCode() == HttpsURLConnection.HTTP_OK) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "vistWWWBAIDUCOM error", e);
		}
		return false;
	}

	private String logincheck(String token, String username) {
		try {
			final long c_time = DateUtil.current_time_ss();
			final String url = BDSYNCURL.getlogincheck(token, c_time, username);
			final Http http = new Http(url, Method.GET);
			if (http.http()) {
				return http.result();
			}
		} catch (Exception e) {
			Log.e(TAG, "logincheck error", e);
		}
		return null;
	}

	private String getpublickey(String token, String username) {
		try {
			final long c_time = DateUtil.current_time_ss();
			final String url = BDSYNCURL.getpublickey(token, c_time);
			final Http http = new Http(url, Method.GET);
			if (http.http()) {
				return http.result();
			}
		} catch (Exception e) {
			Log.e(TAG, "logincheck error", e);
		}
		return null;
	}

	private String buildloginform(String token, long time, String username, String password) {
		String ex = "staticpage=http://pan.baidu.com/res/static/thirdparty/pass_v3_jump.html&charset=utf-8&token=%s&tpl=netdisk&subpro=&apiver=v3&tt=%s&codestring=&safeflg=0&u=http://pan.baidu.com/&isPhone=&quick_user=0&logintype=basicLogin&logLoginType=pc_loginBasic&idc=&loginmerge=true&username=%s&password=%s&verifycode=&mem_pass=on&rsakey=&crypttype=&ppui_logintime=2602&callback=parent.bd__pcbs__msdlhs";
		return String.format(ex, token, time, username, password);
	}

	private String passportlogin(String token, String username, String password) {
		final String url = BDSYNCURL.getloginurl();
		final long c_time = DateUtil.current_time_ss();

		Http http = new Http(url, Method.POST, buildloginform(token, c_time, username, password));
		try {
			if (http.http()) {
				return http.result();
			}
		} catch (IOException e) {
			Log.e(TAG, "passportlogin error", e);
		}
		return null;
	}

	@Override
	public boolean login(String username, String password) throws ApiException {
		Log.w(TAG, String.format("use name:%s pwd:%s login", username, password));
		// 1.判断是否登录
		if (islogin()) {
			return true;
		}

		if (!getbaidu()) {
			return false;
		}

		// 2.得到cookie
		String passport = getpassport();

		if (passport == null) {
			return false;
		}

		passport = passport.replace("bd__cbs__nflaog(", "");
		passport = passport.substring(0, passport.length() - 1);
		if (DEBUG)
			Log.d(TAG, "passport:\n" + passport);
		JSONObject object = new JSONObject(passport);
		String token = null;

		String logincheck = null;
		if (object.has("data")) {
			token = object.getJSONObject("data").getString("token");
			if (object.getJSONObject("data").has("codeString")) {
				Log.w(TAG, "login need codeString");
			}
			logincheck = logincheck(token, username);
		} else {
			return false;
		}

		if (null == logincheck)
			return false;

		String publickey = getpublickey(token, username);
		if (null == publickey)
			return false;

		publickey = publickey.replace("bd__cbs__wl95ks(", "");
		publickey = publickey.substring(0, publickey.length() - 1);

		String publickey_keystring = new JSONObject(publickey).getString("pubkey");
		String publickey_key = new JSONObject(publickey).getString("key");
		if (DEBUG)
			Log.d(TAG, String.format("publickey_keystring:%s\npublickey_key:%s", publickey_keystring, publickey_key));

		String loginresult = passportlogin(token, username, password);
		if (null == loginresult) {
			return false;
		}
		if (DEBUG)
			Log.d(TAG, "loginresult :" + loginresult);
		if (loginresult.contains("err_no=0")) {
			return true;
		}
		return false;
	}

	/**
	 * 下载文件 注意响应头有Content-MD5
	 */
	@Override
	public DownloadInputStream download(LocalFile file) throws ApiException {
		try {
			if (context.load()) {

				final String url = BDSYNCURL.download(file.getAbsolutePath());
				final HttpInput http = new HttpInput(url, Method.GET);
				if (http.http()) {
					final DownloadInputStream in = new DownloadInputStream(http.getInputStream());
					in.setLength(http.getConnet().getContentLength());
					if (http.getConnet().getHeaderFields().containsKey("Accept-Ranges")) {
						in.setRange(true);
					}

					if (http.getConnet().getHeaderFields().containsKey("Content-MD5")) {
						in.setContentMd5(http.getConnet().getHeaderFields().get("Content-MD5").toString());
					} else if (http.getConnet().getHeaderFields().containsKey("content-md5")) {
						in.setContentMd5(http.getConnet().getHeaderFields().get("content-md5").toString());
					}

					return in;
				} else {
					final int code = http.getResponseCode();
					Log.i(TAG, "download " + code);
					if (code == HttpURLConnection.HTTP_NOT_FOUND) {
						final DownloadInputStream in = new DownloadInputStream(null);
						in.setLength(-1);
						return in;
					}
				}
			}
		} catch (Exception e) {
			throw new ApiException("download error", e);
		}
		return null;
	}

	@Override
	public DownloadInputStream download(LocalFile file, long range) throws ApiException {
		try {
			if (context.load()) {

				final String url = BDSYNCURL.download(file.getAbsolutePath());
				final HttpInput http = new HttpInput(url, Method.GET);
				http.addRequestProperty("Range", String.format("bytes=%s- ", range));
				if (http.http()) {
					final DownloadInputStream in = new DownloadInputStream(http.getInputStream());
					in.setLength(http.getContentLength());
					if (http.getConnet().getHeaderFields().containsKey("Accept-Ranges")) {
						in.setRange(true);
					}

					if (http.getConnet().getHeaderFields().containsKey("Content-MD5")) {
						in.setContentMd5(http.getConnet().getHeaderFields().get("Content-MD5").toString());
					} else if (http.getConnet().getHeaderFields().containsKey("content-md5")) {
						in.setContentMd5(http.getConnet().getHeaderFields().get("content-md5").toString());
					}

					return in;
				} else {
					final int code = http.getResponseCode();
					Log.i(TAG, "download " + code);
					if (code == HttpURLConnection.HTTP_NOT_FOUND) {
						final DownloadInputStream in = new DownloadInputStream(null);
						in.setLength(-1);
						return in;
					}
				}
			}
		} catch (Exception e) {
			throw new ApiException("download error", e);
		}
		return null;
	}

	@Override
	public Map<String, String> diskHomePage() throws ApiException {
		final String url = BDSYNCURL.diskHomePage();

		final Http http = new Http(url, Method.GET);
		try {

			if (http.http()) {
				if (DEBUG)
					Log.d(TAG, String.format("diskHomePage:%s", http.result()));
				final Pattern pattern = Pattern.compile("yunData\\.\\w+\\s*=\\s*['|\"]\\w*['|\"];");
				final Matcher matcher = pattern.matcher(http.result());
				String temp = null;
				String[] strings = null;
				Map<String, String> maps = new Hashtable<String, String>();
				while (matcher.find()) {
					temp = matcher.group();
					if (null != temp) {
						strings = temp.split("=");
						if (null != strings && strings.length == 2) {
							maps.put(strings[0].trim().replaceAll("yunData.", ""),
									strings[1].trim().replaceAll("'", "").replaceAll(";", "").replaceAll("\"", ""));
						}
					}
				}
				return maps;
			}
		} catch (IOException e) {
			throw new ApiException("diskHomePage error", e);
		}
		return null;
	}

	@Override
	public boolean islogin() throws ApiException {
		final String url = BDSYNCURL.diskHomePage();
		final Http http = new Http(url, Method.GET);
		try {
			if (http.http()) {
				if (http.getResponseCode() == HttpsURLConnection.HTTP_OK) {
					// if (DEBUG)
					// logger.debug("islogin\n" + http.result());
					return true;
				}
			}
		} catch (IOException e) {
			throw new ApiException("search error", e);
		}
		return false;
	}

	// @Override
	// public boolean upload(String localpath, String cloudpath)
	// throws ApiException {
	// return upload(localpath, cloudpath, null);
	// }
	//
	// @Override
	// public boolean upload(String localpath, String cloudpath,
	// OutputDataListener listener) throws ApiException {
	// try {
	// if (context.load()) {
	// final String BDUSS = HttpCookieContainer.getInstance()
	// .getCookie("BDUSS").getValue();
	// final LocalFile cloudFile = new LocalFile(cloudpath);
	// final String url = BDSYNCURL.getuploadfile(
	// URLEncoder.encode(cloudFile.getParentPath(), "UTF-8"),
	// URLEncoder.encode(cloudFile.getName(), "UTF-8"), BDUSS);
	//
	// final HttpFormOutput http = new HttpFormOutput(url, localpath,
	// listener);
	// if (http.http()) {
	// if (DEBUG)
	// Log.i(TAG, String.format("upload result:%s",
	// http.result()));
	// final String resultString = http.result();
	// final JSONObject object = new JSONObject(resultString);
	// if (object.has("md5")) {
	// return true;
	// }
	//
	// }
	//
	// }
	// } catch (Exception e) {
	// throw new ApiException("upload error", e);
	// }
	// return false;
	// }

	@Override
	public boolean secondUpload(String localpath, String cloudpath) throws ApiException {
		try {
			if (context.load()) {
				final String BDUSS = HttpCookieContainer.getInstance().getCookie("BDUSS").getValue();
				final String bdstoken = context.getProperty(BDSTOKEN, "");
				final File file = new File(localpath);
				final LocalFile cloudFile = new LocalFile(cloudpath);
				final String content_md5 = MD5.md5File(localpath);
				final String slice_md5 = MD5.md5File(localpath, RAPIDUPLOAD);
				final String url = BDSYNCURL.getsecondupload(URLEncoder.encode(cloudFile.getParentPath(), "UTF-8"),
						URLEncoder.encode(cloudFile.getName(), "UTF-8"), file.length(), content_md5, slice_md5,
						URLEncoder.encode(BDUSS, "UTF-8"), bdstoken);

				final Http http = new Http(url, Method.GET);
				if (http.http()) {
					if (DEBUG)
						Log.i(TAG, String.format("upload result:%s", http.result()));
					final String resultString = http.result();
					final JSONObject object = new JSONObject(resultString);
					if (object.has("md5")) {
						return true;
					}

				}

			}
		} catch (Exception e) {
			throw new ApiException("upload error", e);
		}
		return false;
	}

	@Override
	public MkDirResult mkdir(String dir) throws ApiException {
		try {
			if (context.load()) {
				final String bdstoken = context.getProperty(BDSTOKEN, "");
				final String url = BDSYNCURL.mkdir(bdstoken);

				final String formString = String.format("path=%s&isdir=1&size=&block_list=%s&method=post",
						URLEncoder.encode(dir, "UTF-8"), URLEncoder.encode("[]", "UTF-8"));
				if (DEBUG)
					Log.d(TAG, String.format("mkdir form string:%s", formString));
				final Http http = new Http(url, Method.POST, formString);
				if (http.http()) {
					if (DEBUG)
						Log.d(TAG, String.format("mkdir:%s", http.result()));
					final MkDirResult mkDirResult = new MkDirResult();
					mkDirResult.formJOSN(http.result());
					return mkDirResult;
				}

			}
		} catch (Exception e) {
			throw new ApiException("mkdir error", e);
		}
		return null;
	}

	// @Override
	// public CloudFile fileExists(String file) throws ApiException {
	//
	// try {
	// if (context.load()) {
	//
	// final File f = new File(file);
	// final long c_time = DateUtil.current_time_ss();
	// final String url = BDSYNCURL.getfileexists(c_time, context
	// .getProperty(BDSTOKEN), URLEncoder.encode(f.getParent()
	// .replaceAll("\\\\", "/"), "UTF-8"), URLEncoder.encode(
	// f.getName(), "UTF-8"));
	//
	// final Http http = new Http(url, Method.GET);
	// if (http.http()) {
	// // if (DEBUG)
	// Log.d(TAG, String.format("fileExists result:%s",
	// http.result()));
	// final String resultString = http.result();
	// final JSONObject object = new JSONObject(resultString);
	// if (object.has("errno") && object.getInt("errno") == 0
	// && object.has("list")) {
	// JSONArray jsonArray = object.getJSONArray("list");
	// if (jsonArray.length() > 0) {
	// JSONObject jsonObject = null;
	// CloudFile pcsFile = null;
	// // 返回的文件可能有很多
	// for (int i = 0; i < jsonArray.length(); i++) {
	// jsonObject = jsonArray.getJSONObject(0);
	// pcsFile = new CloudFile();
	// pcsFile.formJOSN(jsonObject.toString());
	// // 找到名字匹配的
	// if (pcsFile.getName().equals(f.getName())) {
	// return pcsFile;
	// }
	//
	// }
	//
	// }
	//
	// }
	//
	// } else {
	// throw new ApiException("fileExists http error");
	// }
	//
	// }
	// } catch (Exception e) {
	// throw new ApiException("fileExists error", e);
	// }
	//
	// return null;
	//
	// }

	@Override
	public CloudRmResult rm(String filenames) throws ApiException {
		return rm(new String[] { filenames });
	}

	@Override
	public CloudRmResult rm(String[] filenames) throws ApiException {
		try {
			if (context.load()) {
				final String bdstoken = context.getProperty(BDSTOKEN, "");
				final String url = BDSYNCURL.rm(bdstoken);

				final JSONArray files = new JSONArray();
				for (String s : filenames) {
					files.put(s);
				}
				final String formString = String.format("filelist=%s", URLEncoder.encode(files.toString(), "UTF-8"));
				if (DEBUG)
					Log.d(TAG, String.format("rm form string:%s", formString));
				final Http http = new Http(url, Method.POST, formString);
				if (http.http()) {
					if (DEBUG)
						Log.d(TAG, String.format("rm:%s", http.result()));
					final CloudRmResult rmResult = new CloudRmResult();
					if (rmResult.formJOSN(http.result()))
						return rmResult;
				}
			}
		} catch (Exception e) {
			throw new ApiException("rm error", e);
		}
		return null;

	}

	public boolean upload2(String localpath, String cloudpath) throws ApiException {
		return upload2(localpath, cloudpath, null);
	}

	@Override
	public boolean upload2(String localpath, String cloudpath, OutputDataListener listener) throws ApiException {
		try {
			if (context.load()) {
				final String BDUSS = HttpCookieContainer.getInstance().getCookie("BDUSS").getValue();
				final String url = BDSYNCURL.getuploadfile2(URLEncoder.encode(BDUSS, "UTF-8"));

				final HttpFormOutput http = new HttpFormOutput(url, localpath, listener, true);
				if (http.http()) {
					if (DEBUG)
						Log.i(TAG, String.format("upload2 result:%s", http.result()));
					final String resultString = http.result();
					final JSONObject object = new JSONObject(resultString);
					if (object.has("md5")) {
						if (object.getString("md5").equalsIgnoreCase(http.getFilemd5())) {
							final String[] block_list = new String[1];
							block_list[0] = object.getString("md5");
							if (block_list.length > 0) {
								// 调用 createFile
								for (int i = 0; i < 3; i++) {
									if (createFile(cloudpath, new File(localpath).length(), block_list)) {
										return true;
									}
								}
							}
						} else {
							Log.w(TAG, "cloud result md5 not same to local " + http.getFilemd5() + " "
									+ object.getString("md5"));
						}
					}

				}

			}
		} catch (Exception e) {
			throw new ApiException("upload error", e);
		}
		return false;
	}

	@Override
	public boolean createFile(String path, long size, String[] block_list) throws ApiException {

		try {
			if (context.load()) {
				final String bdstoken = context.getProperty(BDSTOKEN, "");
				final String url = BDSYNCURL.mkdir(bdstoken);

				final StringBuffer block_listString = new StringBuffer("[");
				for (int i = 0; i < block_list.length; i++) {
					if (i != 0) {
						block_listString.append(",");
					}
					block_listString.append("\"").append(block_list[i]).append("\"");
				}
				block_listString.append("]");

				final String formString = String.format("path=%s&isdir=0&size=%s&block_list=%s&method=post",
						URLEncoder.encode(path, "UTF-8"), size,
						URLEncoder.encode(block_listString.toString(), "UTF-8"));
				if (DEBUG)
					Log.d(TAG, String.format("mkdir form string:%s", formString));
				final Http http = new Http(url, Method.POST, formString);
				if (http.http()) {
					if (DEBUG)
						Log.d(TAG, String.format("mkdir:%s", http.result()));
					final MkDirResult mkDirResult = new MkDirResult();
					mkDirResult.formJOSN(http.result());
					return mkDirResult.getSize() == size;
				}

			}
		} catch (Exception e) {
			throw new ApiException("createFile error", e);
		}
		return false;
	}

	@Override
	public CloudPageFile list(String dir) throws ApiException {
		// int i = 1;
		// CloudPageFile file = new CloudPageFile();
		// file.setList(new ArrayList<CloudFile>());
		// CloudPageFile pageFile = null;
		// while ((pageFile = list(dir, i)) != null) {
		// file.setErrno(pageFile.getErrno());
		// file.getList().addAll(pageFile.getList());
		// if (pageFile.getErrno() != 0 || pageFile.getList().isEmpty()
		// || pageFile.getList().size() < PAGESIZE) {
		// break;
		// }
		// i++;
		// }
		// return file;
		return list(dir, 1, PAGESIZE);
	}

	// @Override
	// public CloudPageFile list(String dir, int page) throws ApiException {
	// return list(dir, page, PAGESIZE);
	// }

	@Override
	public CloudPageFile list(String dir, int page, int page_num) throws ApiException {
		try {
			if (context.load()) {
				final long c_time = System.currentTimeMillis();
				final String url = BDSYNCURL.list(page, page_num, dir, c_time, context.getProperty(BDSTOKEN));

				final Http http = new Http(url, Method.GET);
				if (http.http()) {
					final CloudPageFile pageFile = new CloudPageFile();
					if (pageFile.formJOSN(http.result())) {
						return pageFile;
					}
				}

			}

		} catch (Exception e) {
			throw new ApiException("list error", e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.api.FSApi#exists(java.lang.String)
	 */
	@Override
	public CloudFile exists(String file, boolean dir) throws ApiException {
		String dirpath = null;
		if (dir) {
			dirpath = file;
		} else {
			dirpath = new LocalFile(file).getParentPath();
		}
		dirpath = dirpath.replaceAll("\\\\", "/");
		CloudPageFile list = list(dirpath);
		if (list.getErrno() == 0) {
			if (dir) {
				final CloudFile cloudFile = new CloudFile();
				cloudFile.setDir(true);
				cloudFile.setPath(file);
				return cloudFile;
			} else {
				if (null != list.getList()) {
					// Log.d(TAG, "getList:" + list.getList().size());
					for (CloudFile f : list.getList()) {
						// 百度云windows不区别大小写的
						if (f.getPath().equalsIgnoreCase(file)) {
							// Log.d(TAG,
							// "found in getlist :" + f.getAbsolutePath());
							return f;
						}
					}
					Log.d(TAG, "not found in getlist");
				}
			}
		} else if (list.getErrno() == -9) {
			// 文件夹不存在
			return null;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.api.FSApi#getMeta(java.lang.String)
	 */
	@Override
	public CloudFile getMeta(String file) throws ApiException {
		String dirpath = null;
		dirpath = new LocalFile(file).getParentPath();
		dirpath = dirpath.replaceAll("\\\\", "/");
		final CloudPageFile list = list(dirpath);
		if (list.getErrno() == 0) {
			if (null != list.getList()) {
				for (CloudFile f : list.getList()) {
					// 百度云windows不区别大小写的
					if (f.getPath().equalsIgnoreCase(file)) {
						return f;
					}
				}
			}
		} else if (list.getErrno() == -9) {
			// 文件夹不存在
			return null;
		}
		return null;
	}

}
