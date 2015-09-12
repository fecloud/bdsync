/**
 * 
 */
package com.yuncore.bdsync.http.cookie;

import java.util.List;

import com.yuncore.bdsync.Environment;

/**
 * @author ouyangfeng
 * 
 */
public class HttpCookieContainer implements CookieContainer {

	private static HttpCookieContainer instance;

	private CookieContainer imple;

	private HttpCookieContainer() {
		inStanceCookieContainer();
	}

	private void inStanceCookieContainer() {
		try {
			imple = (CookieContainer) Class.forName(
					Environment.getCookiecontainerClassName()).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("not set "
					+ Environment.COOKIECONTAINER);
		}
	}

	public static synchronized HttpCookieContainer getInstance() {
		if (instance == null) {
			instance = new HttpCookieContainer();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.http.cookie.CookieContainer#addCookie(com.yuncore.bdsync
	 * .http.cookie.Cookie)
	 */
	@Override
	public boolean addCookie(Cookie cookie) {
		return imple.addCookie(cookie);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.http.cookie.CookieContainer#findCookie(com.yuncore.bdsync
	 * .http.cookie.Cookie)
	 */
	@Override
	public Cookie findCookie(Cookie cookie) {
		return imple.findCookie(cookie);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.http.cookie.CookieContainer#removeCookie(com.yuncore
	 * .bdsync.http.cookie.Cookie)
	 */
	@Override
	public boolean removeCookie(Cookie cookie) {
		return imple.removeCookie(cookie);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.cookie.CookieContainer#toJSON()
	 */
	@Override
	public String toJSON() {
		return imple.toJSON();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdbdsyncfs.http.cookie.CookieContainer#getCookieList(java.lang.
	 * String)
	 */
	@Override
	public List<Cookie> getCookieList(String url) {
		return imple.getCookieList(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.http.cookie.CookieContainer#addCookieOrUpdate(java.lang
	 * .String)
	 */
	@Override
	public boolean addCookieOrUpdate(String string) {
		return imple.addCookieOrUpdate(string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yuncore.bdsync.http.cookie.CookieContainer#getCookie(java.lang.String)
	 */
	@Override
	public Cookie getCookie(String name) {
		return imple.getCookie(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.cookie.CookieContainer#clear()
	 */
	@Override
	public void clear() {
		imple.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.cookie.CookieContainer#save()
	 */
	@Override
	public boolean save() {
		return imple.save();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.cookie.CookieContainer#read()
	 */
	@Override
	public boolean read() {
		return imple.read();
	}

}
