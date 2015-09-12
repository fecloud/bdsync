package com.yuncore.bdsync.http.cookie;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AppCookieContainer implements CookieContainer {

	public static final String COOKIE_LOAD = "cookie_load";

	public Set<Cookie> cookies = new HashSet<Cookie>();

	public synchronized boolean addCookie(Cookie cookie) {
		boolean result = false;

		if (null != cookie && cookie.getDomain() != null) {
			Cookie findCookie = findCookie(cookie);

			if (null != findCookie) {
				result = findCookie.update(cookie);
			} else {
				result = cookies.add(cookie);
			}
			if (result)
				save();
		}

		return result;
	}

	public synchronized Cookie findCookie(Cookie cookie) {

		for (Cookie c : cookies) {
			if (cookie.getDomain() != null
					&& c.getDomain().equals(cookie.getDomain())
					&& c.getName().equals(cookie.getName())) {
				return c;
			}
		}
		return null;
	}

	public synchronized boolean removeCookie(Cookie cookie) {
		final Cookie c = findCookie(cookie);
		boolean result = false;
		if (null != c) {
			cookies.remove(c);
			result = save();
		}
		return result;
	}

	public synchronized String toJSON() {
		final JSONArray array = new JSONArray();
		JSONObject onec = null;
		for (Cookie c : cookies) {
			onec = new JSONObject();
			c.toJSON(onec);
			array.put(onec);
		}
		return array.toString();
	}

	public synchronized List<Cookie> getCookieList(String url) {
		if (read()) {
			final List<Cookie> results = new ArrayList<Cookie>();
			final String[] strings = urlPathAndDomain(url);
			if (null != strings && strings.length == 2) {
				final String host = strings[0];
				final String path = strings[1];
				for (Cookie c : cookies) {
					if (host.endsWith(c.getDomain())
							&& path.startsWith(c.getPath())) {
						results.add(c);
					}
				}

			}
			return results;
		}
		return new ArrayList<Cookie>();
	}

	public synchronized boolean addCookieOrUpdate(String string) {
		final Cookie cookie = new Cookie();
		if (cookie.parseCookie(string)) {
			addCookie(cookie);
		}
		return true;
	}

	public synchronized Cookie getCookie(String name) {
		for (Cookie c : cookies) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	public static String[] urlPathAndDomain(String string) {

		try {
			final URL url = new URL(string);
			String host = url.getHost();
			String path = url.getPath();
			if (path != null && path.equals("")) {
				path = "/";
			}
			return new String[] { host, path };
		} catch (Exception e) {

		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.bdsync.http.cookie.CookieContainer#clear()
	 */
	@Override
	public void clear() {
		cookies.clear();
	}

	@Override
	public boolean read() {
		final boolean load = Boolean.parseBoolean(System.getProperty(
				COOKIE_LOAD, "false"));
		if (load) {
			System.setProperty(COOKIE_LOAD, "true");
			return true;
		} else if (readForm()) {
			System.setProperty(COOKIE_LOAD, "true");
			return true;
		} else {
			System.setProperty(COOKIE_LOAD, "false");
		}
		return false;
	}

	@Override
	public boolean save() {
		return saveTo();
	}

	/**
	 * 从哪里读取
	 */
	protected abstract boolean readForm();

	/**
	 * 保存到哪里
	 */
	protected abstract boolean saveTo();

}
