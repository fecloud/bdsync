package com.yuncore.bdsync.http.cookie;

import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import org.json.JSONObject;

import com.yuncore.bdsync.entity.EntityJSON;

public class Cookie implements EntityJSON {

	private String name;

	private String value;

	private String domain;

	private String path;

	private long max_age;

	private long expires;

	private long version;

	private boolean httponly = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getMax_age() {
		return max_age;
	}

	public void setMax_age(long max_age) {
		this.max_age = max_age;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

	public boolean isHttponly() {
		return httponly;
	}

	public void setHttponly(boolean httponly) {
		this.httponly = httponly;
	}

	public Cookie() {
	}

	public Cookie(String name, String value, String domain, String path) {
		super();
		this.name = name;
		this.value = value;
		this.domain = domain;
		this.path = path;
	}

	@Override
	public String toJSON() {
		final JSONObject object = new JSONObject();
		toJSON(object);
		return object.toString();
	}

	@Override
	public boolean formJOSN(JSONObject object) {

		if (object != null) {
			name = object.getString("name");
			value = object.getString("value");
			domain = object.getString("domain");
			path = object.getString("path");
			max_age = object.getLong("max_age");
			expires = object.getLong("expires");
			version = object.getLong("version");
			httponly = object.getBoolean("httponly");
			return true;
		}

		return false;
	}

	public boolean parseCookie(String string) {
		final String[] strings = string.split(";");
		final StringBuilder builder = new StringBuilder();
		if (null != strings) {
			for (String s : strings) {
				builder.append(s.trim()).append("\n");
			}

			final Properties properties = new Properties();
			final Reader reader = new StringReader(builder.toString());
			try {
				properties.load(reader);
				domain = properties.getProperty("domain");
				path = properties.getProperty("path");

				final String max_age_string = properties.getProperty("max-age");
				if (null != max_age_string) {
					try {
						final long max_age_temp = Long.parseLong(max_age_string);
						this.max_age = max_age_temp;
					} catch (Exception e) {
					}
				}

				final String name_key = getCookieName(properties);
				if (null != name_key) {
					name = name_key;
					value = properties.getProperty(name_key);
				}

				final String expires = properties.getProperty("expires");
				if (null != expires && expires.length() > 0) {
					final SimpleDateFormat Gmt = new SimpleDateFormat("EEE, d-MMM-yyyy HH:mm:ss z", Locale.US);
					this.expires = Gmt.parse(expires).getTime();
				}

				httponly = properties.containsKey("httponly");
				version = Integer.parseInt(properties.getProperty("version", "0"));

				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	private final String getCookieName(Properties properties) {
		for (Object o : properties.keySet()) {
			if (null != o) {
				if (!o.toString().toLowerCase().equals("domain") && !o.toString().toLowerCase().equals("path")
						&& !o.toString().toLowerCase().equals("domain") && !o.toString().toLowerCase().equals("expires")
						&& !o.toString().toLowerCase().equals("max-age")
						&& !o.toString().toLowerCase().equals("version")
						&& !o.toString().toLowerCase().equals("httponly")) {
					return o.toString();
				}
			}
		}
		return null;
	}

	public boolean update(Cookie cookie) {
		if (cookie != null && !cookie.toString().equals(this.toString())) {
			this.name = cookie.getName();
			this.domain = cookie.getDomain();
			this.value = cookie.getValue();
			this.path = cookie.getPath();
			this.max_age = cookie.getMax_age();
			this.expires = cookie.getExpires();
			this.httponly = cookie.isHttponly();
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		final Cookie cookie = (Cookie) obj;
		final String str = this.name + this.domain;
		final String str2 = cookie.name + cookie.domain;
		return str.equals(str2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final String str = this.name + this.domain;
		return str.hashCode();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", value=" + value + ", domain=" + domain + ", path=" + path + ", max_age=" + max_age
				+ ", expires=" + expires + ", version=" + version + ", httponly=" + httponly + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yuncore.dbpcs.entity.EntityJSONObject#formJOSN(java.lang.String)
	 */
	@Override
	public boolean formJOSN(String json) {
		return formJOSN(new JSONObject(json));
	}

	@Override
	public void toJSON(JSONObject object) {
		object.put("name", name);
		object.put("value", value);
		object.put("domain", domain);
		object.put("path", path);
		object.put("max_age", max_age);
		object.put("expires", expires);
		object.put("version", version);
		object.put("httponly", httponly);
	}

}
