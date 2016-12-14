/**
 * PersistentCookieStore.java Created on 2016年11月22日
 */
package com.yuncore.bdsync.http.cookie;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.yuncore.bdsync.Environment;

/**
 * The class <code>PersistentCookieStore</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class PersistentCookieStore implements CookieStore {
	/** this map may have null keys! */

	private Map<URI, List<HttpCookie>> map = new HashMap<URI, List<HttpCookie>>();

	public PersistentCookieStore() {
		Map<URI, List<HttpCookie>> restroe = restroe();
		if (null != restroe) {
			map = restroe;
		}
	}

	public synchronized void add(URI uri, HttpCookie cookie) {
		if (cookie == null) {
			throw new NullPointerException("cookie == null");
		}

		uri = cookiesUri(uri);
		List<HttpCookie> cookies = map.get(uri);
		if (cookies == null) {
			cookies = new ArrayList<HttpCookie>();
			map.put(uri, cookies);
		} else {
			cookies.remove(cookie);
		}
		cookies.add(cookie);
		save(map);
	}

	private URI cookiesUri(URI uri) {
		if (uri == null) {
			return null;
		}
		try {
			return new URI("http", uri.getHost(), null, null);
		} catch (URISyntaxException e) {
			return uri; // probably a URI with no host
		}
	}

	public synchronized List<HttpCookie> get(URI uri) {
		if (uri == null) {
			throw new NullPointerException("uri == null");
		}

		List<HttpCookie> result = new ArrayList<HttpCookie>();

		// get cookies associated with given URI. If none, returns an empty list
		List<HttpCookie> cookiesForUri = map.get(uri.getHost());
		if (cookiesForUri != null) {
			for (Iterator<HttpCookie> i = cookiesForUri.iterator(); i.hasNext();) {
				HttpCookie cookie = i.next();
				if (cookie.hasExpired()) {
					i.remove(); // remove expired cookies
				} else {
					result.add(cookie);
				}
			}
		}

		// get all cookies that domain matches the URI
		for (Map.Entry<URI, List<HttpCookie>> entry : map.entrySet()) {
			if (uri.equals(entry.getKey())) {
				continue; // skip the given URI; we've already handled it
			}

			List<HttpCookie> entryCookies = entry.getValue();
			for (Iterator<HttpCookie> i = entryCookies.iterator(); i.hasNext();) {
				HttpCookie cookie = i.next();
				if (!HttpCookie
						.domainMatches(cookie.getDomain(), uri.getHost())) {
					continue;
				}
				if (cookie.hasExpired()) {
					i.remove(); // remove expired cookies
				} else if (!result.contains(cookie)) {
					result.add(cookie);
				}
			}
		}

		save(map);
		return Collections.unmodifiableList(result);
	}

	public synchronized List<HttpCookie> getCookies() {
		List<HttpCookie> result = new ArrayList<HttpCookie>();
		for (List<HttpCookie> list : map.values()) {
			for (Iterator<HttpCookie> i = list.iterator(); i.hasNext();) {
				HttpCookie cookie = i.next();
				if (cookie.hasExpired()) {
					i.remove(); // remove expired cookies
				} else if (!result.contains(cookie)) {
					result.add(cookie);
				}
			}
		}
		save(map);
		return Collections.unmodifiableList(result);
	}

	public synchronized List<URI> getURIs() {
		List<URI> result = new ArrayList<URI>(map.keySet());
		result.remove(null); // sigh
		save(map);
		return Collections.unmodifiableList(result);
	}

	public synchronized boolean remove(URI uri, HttpCookie cookie) {
		if (cookie == null) {
			throw new NullPointerException("cookie == null");
		}

		List<HttpCookie> cookies = map.get(cookiesUri(uri));
		if (cookies != null) {
			boolean con = cookies.remove(cookie);
			save(map);
			return con;
		} else {
			return false;
		}
	}

	public synchronized boolean removeAll() {
		boolean result = !map.isEmpty();
		map.clear();
		save(map);
		return result;
	}

	private static void save(Map<URI, List<HttpCookie>> map) {
		try {
			if (map.isEmpty()) {
				return;
			}
			final JSONObject object = new JSONObject();
			Set<URI> keySet = map.keySet();
			for (URI uri : keySet) {
				List<HttpCookie> value = map.get(uri);
				if (null != value) {
					final JSONArray array = new JSONArray();
					for (HttpCookie c : value) {
						final JSONObject jsonObject = new JSONObject();

						jsonObject.put("discard", c.getDiscard());
						jsonObject.put("maxAge", c.getMaxAge());
						jsonObject.put("secure", c.getSecure());
						jsonObject.put("version", c.getVersion());
						jsonObject.put("domain", c.getDomain());
						jsonObject.put("comment", c.getComment());
						jsonObject.put("commentURL", c.getCommentURL());
						jsonObject.put("name", c.getName());
						jsonObject.put("path", c.getPath());
						jsonObject.put("portList", c.getPortlist());
						jsonObject.put("value", c.getValue());
						array.put(jsonObject);
						
						if (c.getName().equalsIgnoreCase("BDUSS")) {
							System.setProperty("BDUSS", c.getValue());
						}
					}
					object.put(uri.toString(), array);
				}

			}
			FileOutputStream openFileOutput = new FileOutputStream(Environment.getCookieFile());
			openFileOutput.write(object.toString().getBytes());
			openFileOutput.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Map<URI, List<HttpCookie>> restroe() {
		try {
			final InputStream in = new FileInputStream(Environment.getCookieFile());
			byte[] buffer = new byte[1024];
			int len = -1;
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (-1 != (len = in.read(buffer))) {
				out.write(buffer, 0, len);
			}
			in.close();
			final String json = new String(out.toByteArray());
			final JSONObject object = new JSONObject(json);
			@SuppressWarnings("rawtypes")
			Iterator keys = object.keys();
			Map<URI, List<HttpCookie>> map = new HashMap<URI, List<HttpCookie>>();
			for (; keys.hasNext();) {
				String next = (String) keys.next();
				final List<HttpCookie> cookies = new ArrayList<HttpCookie>();
				final JSONArray array = object.getJSONArray(next);
				for (int i = 0; i < array.length(); i++) {
					final JSONObject jsonObject = array.getJSONObject(i);
					final HttpCookie cookie = new HttpCookie(
							jsonObject.getString("name"),
							jsonObject.getString("value"));
					if (cookie.getName().equalsIgnoreCase("BDUSS")) {
						System.setProperty("BDUSS", cookie.getValue());
					}
					if (jsonObject.has("discard"))
						cookie.setDiscard(jsonObject.getBoolean("discard"));
					if (jsonObject.has("maxAge"))
						cookie.setMaxAge(jsonObject.getLong("maxAge"));
					if (jsonObject.has("secure"))
						cookie.setSecure(jsonObject.getBoolean("secure"));
					if (jsonObject.has("version"))
						cookie.setVersion(jsonObject.getInt("version"));
					if (jsonObject.has("domain"))
						cookie.setDomain(jsonObject.getString("domain"));
					if (jsonObject.has("comment"))
						cookie.setComment(jsonObject.getString("comment"));
					if (jsonObject.has("commentURL"))
						cookie.setCommentURL(jsonObject.getString("commentURL"));
					if (jsonObject.has("path"))
						cookie.setPath(jsonObject.getString("path"));
					if (jsonObject.has("portList"))
						cookie.setPortlist(jsonObject.getString("portList"));
					cookies.add(cookie);
				}
				map.put(new URI(next.toString()), cookies);
				return map;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
