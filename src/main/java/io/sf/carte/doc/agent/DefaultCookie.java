/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Default Cookie implementation.
 *
 */
@Deprecated(forRemoval = true)
public class DefaultCookie implements Cookie, Serializable, Cloneable {

	private static final long serialVersionUID = 3L;

	private long creationTime;

	private long expiryTime = 0L;

	private String name = null;

	private String value = null;

	private String domain = null;

	private String path = null;

	private boolean secure = false;

	private boolean httpOnly = false;

	private String comment = null;

	public DefaultCookie() {
		this(null, null);
	}

	public DefaultCookie(String name, String value) {
		this(name, value, System.currentTimeMillis());
	}

	public DefaultCookie(String name, String value, long creationTime) {
		super();
		this.name = name;
		this.value = value;
		this.creationTime = creationTime;
	}

	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the creation time
	 */
	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public void setCreationTime(long time) {
		this.creationTime = time;
	}

	@Override
	public long getExpiryTime() {
		return expiryTime;
	}

	@Override
	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public void setSecure() {
		this.secure = true;
	}

	@Override
	public boolean isPersistent() {
		return expiryTime != 0L;
	}

	@Override
	public void setHttpOnly() {
		this.httpOnly = true;
	}

	@Override
	public boolean isHttpOnly() {
		return httpOnly;
	}

	static class CookieComparator implements Comparator<Cookie> {

		@Override
		public int compare(Cookie c1, Cookie c2) {
			return c2.getPath().length() - c1.getPath().length();
		}

	}

	@Override
	public int hashCode() {
		return Objects.hash(domain, name, path);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DefaultCookie other = (DefaultCookie) obj;
		return Objects.equals(domain, other.domain) && Objects.equals(name, other.name)
				&& Objects.equals(path, other.path);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(32);
		buf.append(name);
		buf.append('=');
		buf.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
		buf.append(";Domain=");
		buf.append(domain);
		buf.append(";Path=");
		buf.append(path);
		if (isPersistent()) {
			SimpleDateFormat cookieDateFormat = new SimpleDateFormat(
					"EEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.ROOT);
			Date date = new Date(expiryTime);
			buf.append(";Expires=");
			buf.append(cookieDateFormat.format(date));
		}
		if (secure) {
			buf.append(";Secure");
		}
		if (httpOnly) {
			buf.append(";HttpOnly");
		}
		return buf.toString();
	}

	@Override
	public DefaultCookie clone() {
		DefaultCookie clon;
		try {
			clon = (DefaultCookie) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}

		clon.domain = domain;
		clon.path = path;
		clon.name = name;
		clon.value = value;
		clon.secure = secure;
		clon.httpOnly = httpOnly;
		clon.creationTime = creationTime;
		clon.expiryTime = expiryTime;
		clon.comment = comment;
		return clon;
	}

}
