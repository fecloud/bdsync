package com.yuncore.bdsync.http.log;

public class HttpLogLoader implements HttpLog {

	@Override
	public void log(String message) {
		if (null != imple)
			imple.log(message);
	}

	public static final String httplog = "httplog";

	private static HttpLogLoader instance;

	private HttpLog imple;

	private HttpLogLoader() {
		inStanceHttpLog();
	}

	private void inStanceHttpLog() {
		try {
			System.setProperty(httplog, ConsoleHttpLog.class.getName());
			imple = (HttpLog) Class.forName(System.getProperty(httplog))
					.newInstance();
		} catch (Exception e) {
		}
	}

	public static synchronized HttpLogLoader getInstance() {
		if (instance == null) {
			instance = new HttpLogLoader();
		}
		return instance;
	}
}
