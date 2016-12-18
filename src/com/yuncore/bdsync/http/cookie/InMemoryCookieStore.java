/**
 * InMemoryCookieStore.java Created on 2016年11月22日
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
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;

import com.yuncore.bdsync.Environment;
import com.yuncore.bdsync.http.Http;
import com.yuncore.bdsync.http.log.HttpLog;
import com.yuncore.bdsync.http.log.HttpLogLoader;

/**
 * The class <code>InMemoryCookieStore</code>
 * 
 * @author Feng OuYang
 * @version 1.0
 */
public class InMemoryCookieStore implements CookieStore {
	// the in-memory representation of cookies
	private List<HttpCookie> cookieJar = null;

	// the cookies are indexed by its domain and associated uri (if present)
	// CAUTION: when a cookie removed from main data structure (i.e. cookieJar),
	// it won't be cleared in domainIndex & uriIndex. Double-check the
	// presence of cookie when retrieve one form index store.
	private Map<String, List<HttpCookie>> domainIndex = null;
	private Map<URI, List<HttpCookie>> uriIndex = null;

	// use ReentrantLock instead of syncronized for scalability
	private ReentrantLock lock = null;
	
	private HttpLog httpLog = HttpLogLoader.getInstance();

	/**
	 * The default ctor
	 */
	public InMemoryCookieStore() {
		cookieJar = new ArrayList<HttpCookie>();
		domainIndex = new HashMap<String, List<HttpCookie>>();
		uriIndex = new HashMap<URI, List<HttpCookie>>();

		restroe();
		lock = new ReentrantLock(false);
	}

	/**
	 * Add one cookie into cookie store.
	 */
	public void add(URI uri, HttpCookie cookie) {
		// pre-condition : argument can't be null
		if (cookie == null) {
			throw new NullPointerException("cookie is null");
		}
		
		if (Http.DEBUG) {
			httpLog.log(String.format("add uri:%s cookie:%s", uri, cookie));
		}

		lock.lock();
		try {
			// remove the ole cookie if there has had one
			cookieJar.remove(cookie);

			// add new cookie if it has a non-zero max-age
			if (cookie.getMaxAge() != 0) {
				cookieJar.add(cookie);
				// and add it to domain index
				if (cookie.getDomain() != null) {
					addIndex(domainIndex, cookie.getDomain(), cookie);
				}
				if (uri != null) {
					// add it to uri index, too
					addIndex(uriIndex, getEffectiveURI(uri), cookie);
				}
			}
		} finally {
			lock.unlock();
			save();
		}
	}

	/**
	 * Get all cookies, which: 1) given uri domain-matches with, or, associated
	 * with given uri when added to the cookie store. 3) not expired. See RFC
	 * 2965 sec. 3.3.4 for more detail.
	 */
	public List<HttpCookie> get(URI uri) {
		// argument can't be null
		if (uri == null) {
			throw new NullPointerException("uri is null");
		}
		
		if (Http.DEBUG) {
			httpLog.log(String.format("get uri:%s", uri));
		}

		List<HttpCookie> cookies = new ArrayList<HttpCookie>();
		boolean secureLink = "https".equalsIgnoreCase(uri.getScheme());
		lock.lock();
		try {
			// check domainIndex first
			getInternal1(cookies, domainIndex, uri.getHost(), secureLink);
			// check uriIndex then
			getInternal2(cookies, uriIndex, getEffectiveURI(uri), secureLink);
		} finally {
			lock.unlock();
		}


		if (Http.DEBUG) {
			for (HttpCookie e : cookies) {
				httpLog.log(String.format("get uri:%s e:%s", uri, e));
			}

		}
		return cookies;
	}

	/**
	 * Get all cookies in cookie store, except those have expired
	 */
	public List<HttpCookie> getCookies() {
		List<HttpCookie> rt;

		lock.lock();
		try {
			Iterator<HttpCookie> it = cookieJar.iterator();
			while (it.hasNext()) {
				if (it.next().hasExpired()) {
					it.remove();
				}
			}
		} finally {
			rt = Collections.unmodifiableList(cookieJar);
			lock.unlock();
			save();
		}

		if (Http.DEBUG) {
			httpLog.log(String.format("getCookies:%s", rt.size()));
		}
		return rt;
	}

	/**
	 * Get all URIs, which are associated with at least one cookie of this
	 * cookie store.
	 */
	public List<URI> getURIs() {
		List<URI> uris = new ArrayList<URI>();

		lock.lock();
		try {
			Iterator<URI> it = uriIndex.keySet().iterator();
			while (it.hasNext()) {
				URI uri = it.next();
				List<HttpCookie> cookies = uriIndex.get(uri);
				if (cookies == null || cookies.size() == 0) {
					// no cookies list or an empty list associated with
					// this uri entry, delete it
					it.remove();
				}
			}
		} finally {
			uris.addAll(uriIndex.keySet());
			save();
			lock.unlock();
		}

		if (Http.DEBUG) {
			httpLog.log(String.format("getURIs:%s", uris.size()));
		}
		return uris;
	}

	/**
	 * Remove a cookie from store
	 */
	public boolean remove(URI uri, HttpCookie ck) {
		// argument can't be null
		if (ck == null) {
			throw new NullPointerException("cookie is null");
		}

		boolean modified = false;
		lock.lock();
		try {
			modified = cookieJar.remove(ck);
		} finally {
			save();
			lock.unlock();
		}

		if (Http.DEBUG) {
			httpLog.log(String.format("remove uri:%s ck:%s", uri, ck));
		}
		return modified;
	}

	/**
	 * Remove all cookies in this cookie store.
	 */
	public boolean removeAll() {
		lock.lock();
		try {
			cookieJar.clear();
			domainIndex.clear();
			uriIndex.clear();
		} finally {
			save();
			lock.unlock();
		}

		if (Http.DEBUG) {
			httpLog.log(String.format("removeAll"));
		}
		return true;
	}

	/* ---------------- Private operations -------------- */

	/*
	 * This is almost the same as HttpCookie.domainMatches except for one
	 * difference: It won't reject cookies when the 'H' part of the domain
	 * contains a dot ('.'). I.E.: RFC 2965 section 3.3.2 says that if host is
	 * x.y.domain.com and the cookie domain is .domain.com, then it should be
	 * rejected. However that's not how the real world works. Browsers don't
	 * reject and some sites, like yahoo.com do actually expect these cookies to
	 * be passed along. And should be used for 'old' style cookies (aka Netscape
	 * type of cookies)
	 */
	private boolean netscapeDomainMatches(String domain, String host) {
		if (domain == null || host == null) {
			return false;
		}

		// if there's no embedded dot in domain and domain is not .local
		boolean isLocalDomain = ".local".equalsIgnoreCase(domain);
		int embeddedDotInDomain = domain.indexOf('.');
		if (embeddedDotInDomain == 0) {
			embeddedDotInDomain = domain.indexOf('.', 1);
		}
		if (!isLocalDomain
				&& (embeddedDotInDomain == -1 || embeddedDotInDomain == domain
						.length() - 1)) {
			return false;
		}

		// if the host name contains no dot and the domain name is .local
		int firstDotInHost = host.indexOf('.');
		if (firstDotInHost == -1 && isLocalDomain) {
			return true;
		}

		int domainLength = domain.length();
		int lengthDiff = host.length() - domainLength;
		if (lengthDiff == 0) {
			// if the host name and the domain name are just string-compare
			// euqal
			return host.equalsIgnoreCase(domain);
		} else if (lengthDiff > 0) {
			// need to check H & D component
			@SuppressWarnings("unused")
			String H = host.substring(0, lengthDiff);
			String D = host.substring(lengthDiff);

			return (D.equalsIgnoreCase(domain));
		} else if (lengthDiff == -1) {
			// if domain is actually .host
			return (domain.charAt(0) == '.' && host.equalsIgnoreCase(domain
					.substring(1)));
		}

		return false;
	}

	private void getInternal1(List<HttpCookie> cookies,
			Map<String, List<HttpCookie>> cookieIndex, String host,
			boolean secureLink) {
		// Use a separate list to handle cookies that need to be removed so
		// that there is no conflict with iterators.
		ArrayList<HttpCookie> toRemove = new ArrayList<HttpCookie>();
		for (Map.Entry<String, List<HttpCookie>> entry : cookieIndex.entrySet()) {
			String domain = entry.getKey();
			List<HttpCookie> lst = entry.getValue();
			for (HttpCookie c : lst) {
				if ((c.getVersion() == 0 && netscapeDomainMatches(domain, host))
						|| (c.getVersion() == 1 && HttpCookie.domainMatches(
								domain, host))) {
					if ((cookieJar.indexOf(c) != -1)) {
						// the cookie still in main cookie store
						if (!c.hasExpired()) {
							// don't add twice and make sure it's the proper
							// security level
							if ((secureLink || !c.getSecure())
									&& !cookies.contains(c)) {
								cookies.add(c);
							}
						} else {
							toRemove.add(c);
						}
					} else {
						// the cookie has beed removed from main store,
						// so also remove it from domain indexed store
						toRemove.add(c);
					}
				}
			}
			// Clear up the cookies that need to be removed
			for (HttpCookie c : toRemove) {
				lst.remove(c);
				cookieJar.remove(c);

			}
			toRemove.clear();
			save();
		}
	}

	// @param cookies [OUT] contains the found cookies
	// @param cookieIndex the index
	// @param comparator the prediction to decide whether or not
	// a cookie in index should be returned
	private <T> void getInternal2(List<HttpCookie> cookies,
			Map<T, List<HttpCookie>> cookieIndex, Comparable<T> comparator,
			boolean secureLink) {
		for (T index : cookieIndex.keySet()) {
			if (comparator.compareTo(index) == 0) {
				List<HttpCookie> indexedCookies = cookieIndex.get(index);
				// check the list of cookies associated with this domain
				if (indexedCookies != null) {
					Iterator<HttpCookie> it = indexedCookies.iterator();
					while (it.hasNext()) {
						HttpCookie ck = it.next();
						if (cookieJar.indexOf(ck) != -1) {
							// the cookie still in main cookie store
							if (!ck.hasExpired()) {
								// don't add twice
								if ((secureLink || !ck.getSecure())
										&& !cookies.contains(ck))
									cookies.add(ck);
							} else {
								it.remove();
								cookieJar.remove(ck);
							}
						} else {
							// the cookie has beed removed from main store,
							// so also remove it from domain indexed store
							it.remove();
						}
					}
				} // end of indexedCookies != null
			} // end of comparator.compareTo(index) == 0
		} // end of cookieIndex iteration
		save();
	}

	// add 'cookie' indexed by 'index' into 'indexStore'
	private <T> void addIndex(Map<T, List<HttpCookie>> indexStore, T index,
			HttpCookie cookie) {
		if (index != null) {
			List<HttpCookie> cookies = indexStore.get(index);
			if (cookies != null) {
				// there may already have the same cookie, so remove it first
				cookies.remove(cookie);

				cookies.add(cookie);
			} else {
				cookies = new ArrayList<HttpCookie>();
				cookies.add(cookie);
				indexStore.put(index, cookies);
			}
		}
		save();
	}

	//
	// for cookie purpose, the effective uri should only be http://host
	// the path will be taken into account when path-match algorithm applied
	//
	private URI getEffectiveURI(URI uri) {
		URI effectiveURI = null;
		try {
			effectiveURI = new URI("http", uri.getHost(), null, // path
																// component
					null, // query component
					null // fragment component
			);
		} catch (URISyntaxException ignored) {
			effectiveURI = uri;
		}

		return effectiveURI;
	}

	public JSONObject httpCookieToJSON(HttpCookie c) {
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
		return jsonObject;
	}

	public HttpCookie jsonToHttpCookie(JSONObject jsonObject) {
		final HttpCookie cookie = new HttpCookie(jsonObject.getString("name"),
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
		return cookie;
	}

	private void save() {
		try {
			final JSONObject root = new JSONObject();
			if (cookieJar != null && !cookieJar.isEmpty()) {
				final JSONArray cookieJarJsonArray = new JSONArray();
				for (HttpCookie c : cookieJar) {
					final JSONObject jsonObject = httpCookieToJSON(c);
					cookieJarJsonArray.put(jsonObject);
				}
				root.put("cookieJar", cookieJarJsonArray);
			}

			if (domainIndex != null && !domainIndex.isEmpty()) {
				JSONObject domainIndexJsonObject = new JSONObject();
				for (String key : domainIndex.keySet()) {
					final List<HttpCookie> list = domainIndex.get(key);
					if (null != list && !list.isEmpty()) {
						final JSONArray cookieJarJsonArray = new JSONArray();
						for (HttpCookie c : list) {
							final JSONObject jsonObject = httpCookieToJSON(c);
							cookieJarJsonArray.put(jsonObject);
						}
						domainIndexJsonObject.put(key, cookieJarJsonArray);
					}

				}
				root.put("domainIndex", domainIndexJsonObject);
			}

			if (uriIndex != null && !uriIndex.isEmpty()) {
				JSONObject uriIndexJsonObject = new JSONObject();
				for (URI key : uriIndex.keySet()) {
					final List<HttpCookie> list = uriIndex.get(key);
					if (null != list && !list.isEmpty()) {
						final JSONArray cookieJarJsonArray = new JSONArray();
						for (HttpCookie c : list) {
							final JSONObject jsonObject = httpCookieToJSON(c);
							cookieJarJsonArray.put(jsonObject);
						}
						uriIndexJsonObject.put(key.toString(),
								cookieJarJsonArray);
					}

				}
				root.put("uriIndex", uriIndexJsonObject);
			}

			FileOutputStream openFileOutput = new FileOutputStream(
					Environment.getCookieFile());
			openFileOutput.write(root.toString().getBytes());
			openFileOutput.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void restroe() {
		try {
			final InputStream in = new FileInputStream(
					Environment.getCookieFile());
			byte[] buffer = new byte[1024];
			int len = -1;
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (-1 != (len = in.read(buffer))) {
				out.write(buffer, 0, len);
			}
			in.close();
			final String json = new String(out.toByteArray());
			final JSONObject root = new JSONObject(json);
			if (root.has("cookieJar")) {
				final JSONArray cookieJarJsonArray = root
						.getJSONArray("cookieJar");
				cookieJar = new ArrayList<HttpCookie>();
				for (int i = 0; i < cookieJarJsonArray.length(); i++) {
					cookieJar.add(jsonToHttpCookie(cookieJarJsonArray
							.getJSONObject(i)));
				}
			}
			if (root.has("domainIndex")) {
				JSONObject domainIndexJsonObject = root
						.getJSONObject("domainIndex");
				Set<String> keySet = domainIndexJsonObject.keySet();
				domainIndex = new HashMap<String, List<HttpCookie>>();
				for (String key : keySet) {
					final JSONArray jsonArray = domainIndexJsonObject
							.getJSONArray(key);
					final List<HttpCookie> list = new ArrayList<HttpCookie>();
					for (int i = 0; i < jsonArray.length(); i++) {
						final HttpCookie cookie = jsonToHttpCookie(jsonArray
								.getJSONObject(i));
						list.add(cookie);
					}
					domainIndex.put(key, list);
				}
			}

			if (root.has("uriIndex")) {
				JSONObject uriIndexJsonObject = root.getJSONObject("uriIndex");
				Set<String> keySet = uriIndexJsonObject.keySet();
				uriIndex = new HashMap<URI, List<HttpCookie>>();
				for (String key : keySet) {
					final JSONArray jsonArray = uriIndexJsonObject
							.getJSONArray(key);
					final List<HttpCookie> list = new ArrayList<HttpCookie>();
					for (int i = 0; i < jsonArray.length(); i++) {
						final HttpCookie cookie = jsonToHttpCookie(jsonArray
								.getJSONObject(i));
						list.add(cookie);
					}
					uriIndex.put(new URI(key), list);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
