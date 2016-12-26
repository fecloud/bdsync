package com.yuncore.bdsync.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.yuncore.bdsync.http.cookie.InMemoryCookieStore;
import com.yuncore.bdsync.http.log.HttpLog;
import com.yuncore.bdsync.http.log.HttpLogLoader;
import com.yuncore.bdsync.util.Gzip;
import com.yuncore.bdsync.util.TextUtil;

public class Http {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36";

	protected String url;

	protected Method method;

	private String formString;

	protected HttpURLConnection conn;

	protected static CookieManager manager = new CookieManager(new InMemoryCookieStore(),
			CookiePolicy.ACCEPT_ALL);

	private String result;

	private Hashtable<String, String> requestHeader = new Hashtable<String, String>();
	
	private static boolean setCookieManager = false;

	public static final  boolean DEBUG = false;

	private HttpLog httpLog = HttpLogLoader.getInstance();

	public enum Method {
		GET, POST
	}

	public Http() {
		super();
		if(!setCookieManager){
			CookieHandler.setDefault(manager);
			setCookieManager = true;
		}
		
	}

	public Http(String url, Method method) {
		super();
		this.url = url;
		this.method = method;
		if(!setCookieManager){
			CookieHandler.setDefault(manager);
			setCookieManager = true;
		}
	}

	public Http(String url, Method method, String formString) {
		super();
		this.url = url;
		this.method = method;
		this.formString = formString;
		if(!setCookieManager){
			CookieHandler.setDefault(manager);
			setCookieManager = true;
		}
	}

	public boolean http() throws MalformedURLException, IOException {
		if (DEBUG) {
			httpLog.log(String.format("url:%s", url));
			httpLog.log(String.format("method:%s", method == Method.GET ? "GET" : "POST"));
		}
		
		final String proxyString = /*"127.0.0.1:8888";*/System.getProperty("http_proxy");
		if (null != proxyString && proxyString.split(":").length == 2) {
			final String[] proxyArray = proxyString.split(":");
			final Proxy proxy = new Proxy(Type.HTTP,
					new InetSocketAddress(proxyArray[0], Integer.parseInt(proxyArray[1])));
			conn = (HttpURLConnection) new URL(url).openConnection(proxy);
		} else {
			conn = (HttpURLConnection) new URL(url).openConnection();
		}
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(true);// 不使用Cache
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod(method == Method.GET ? "GET" : "POST");
		conn.addRequestProperty("User-Agent", USER_AGENT);
		// conn.addRequestProperty("Connection", "close");
		addHost();
		conn.addRequestProperty("Accept", "*/*");
		addRequestProperty();
		addRequestHeader();
//		addCookie();
		if (DEBUG)
			printRequestHead();

		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);

		if(!addFormData()){
			return false;
		}

		conn.connect();
		if (DEBUG)
			printResponeHead();

//		setCookie();
		return execResult();
	}

	protected void addRequestProperty() {
		conn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
	}

	protected void addRequestHeader() {
		if (!requestHeader.isEmpty()) {
			final Enumeration<String> keys = requestHeader.keys();
			String key = null;
			while (keys.hasMoreElements()) {
				key = keys.nextElement();
				conn.addRequestProperty(key, requestHeader.get(key));
			}
		}
	}

	public void addRequestProperty(String key, String value) {
		if (null != key && value != null) {
			requestHeader.put(key, value);
		}
	}

	public int getResponseCode() throws IOException {
		return conn.getResponseCode();
	}

	protected boolean execResult() throws IOException {
		if (getResponseCode() == HttpURLConnection.HTTP_OK || getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
				|| getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
			return true;
		}
		return false;
	}

	public InputStream getInputStream() throws IOException {
		return conn.getInputStream();
	}

	public long getContentLength() {
		long clen = conn.getContentLength();
		if (clen == -1) {
			try {
				clen = Long.parseLong(conn.getHeaderField("Content-Length"));
			} catch (Exception e) {
			}
		}
		return clen;
	}

	public HttpURLConnection getConnet() {
		return conn;
	}

	public String result() throws IOException {
		if (execResult() && null == result) {
			final String contentEncoding = conn.getHeaderField("Content-Encoding");
			if (contentEncoding != null && contentEncoding.trim().equals("gzip")) {
				this.result = Gzip.readToStringByGzip(conn.getInputStream(), "UTF-8");
			} else {
				this.result = TextUtil.readToString(conn.getInputStream(), "UTF-8");
			}
		}
		if(DEBUG){
			
			if(result != null){
				if(result.length() > 30){
					httpLog.log(result.substring(0, 29));
				}else {
					httpLog.log(result);
				}
				
			}
		}
		
		return result;
	}
	
	public String getLocation(){
		return conn.getHeaderField("Location");
	}

	protected boolean addFormData() throws IOException {
		if (null != formString) {
			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.getOutputStream().write(formString.getBytes("UTF-8"));
		}
		return true;
	}

//	private boolean setCookie() {
//		final Map<String, List<String>> headerFields = conn.getHeaderFields();
//		final List<String> list = headerFields.get("Set-Cookie");
//
//		if (null != list) {
//			if (DEBUG)
//				httpLog.log("Set-Cookie list:" + list);
//			for (String c : list) {
//				if (DEBUG)
//					httpLog.log("Set-Cookie one:" + c);
//				if (null != c)
//					container.addCookieOrUpdate(c.trim());
//			}
//		}
//		return true;
//	}

	private void printRequestHead() {
		final Map<String, List<String>> headerFields = conn.getRequestProperties();
		httpLog.log("");
		httpLog.log("[===============request header ===================]");
		for (Entry<String, List<String>> entry : headerFields.entrySet()) {
			httpLog.log(String.format("[%s:%s]", entry.getKey(), entry.getValue()));
		}
		httpLog.log("");
	}

	private void printResponeHead() {
		final Map<String, List<String>> headerFields = conn.getHeaderFields();
		httpLog.log("");
		httpLog.log("[===============respone header ===================]");
		for (Entry<String, List<String>> entry : headerFields.entrySet()) {
			if (entry.getKey() != null) {
				final List<String> strings = entry.getValue();
				if(null != strings){
					final StringBuilder builder = new StringBuilder();
					builder.append(String.format("[%s:",entry.getKey()));
					for(String string:strings){
						builder.append(string);
						builder.append(";");
					}
					builder.append("]");
					httpLog.log(builder.toString());
				}
				
			} else {
				httpLog.log(String.format("[%s]", entry.getValue()));
			}
		}
		httpLog.log("");
	}

	private boolean addHost() {
		try {
			final URL connurl = new URL(this.url);
			conn.addRequestProperty("Host", connurl.getHost());
		} catch (MalformedURLException e) {
		}

		return true;
	}

//	private boolean addCookie() {
//		final List<Cookie> cookies = container.getCookieList(url);
//		if (cookies != null && !cookies.isEmpty()) {
//			final StringBuilder cookie_str = new StringBuilder();
//			for (int i = 0; i < cookies.size(); i++) {
//				cookie_str.append(cookies.get(i).getName()).append("=").append(cookies.get(i).getValue());
//				if (i != cookies.size() - 1) {
//					cookie_str.append(";");
//				}
//			}
//			conn.addRequestProperty("Cookie", cookie_str.toString());
//		}
//		return true;
//	}

//	public boolean clearCookie() {
//		container.clear();
//		container.save();
//		return true;
//
//	}
}
