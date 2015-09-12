/**
 * 
 */
package com.yuncore.bdsync.http.cookie;

import java.util.List;

/**
 * @author ouyangfeng
 * 
 */
public interface CookieContainer {

	boolean addCookie(Cookie cookie);

	Cookie findCookie(Cookie cookie);

	boolean removeCookie(Cookie cookie);

	String toJSON();

	List<Cookie> getCookieList(String url);

	boolean addCookieOrUpdate(String string);

	Cookie getCookie(String name);

	void clear();
	
	boolean save();
	
	boolean read();
}
