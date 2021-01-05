/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.io.Serializable;

/**
 * Default Cookie implementation.
 * 
 * @author Carlos Amengual
 *
 */
public class DefaultCookie implements Cookie, Serializable, Cloneable {

	private static final long serialVersionUID = 2L;

	private long expiryTime = 0L;

	private String comment = null;

	private String domain = null;

	private int[] ports;

	private String path = null;

	private String name = null;

	private String value = null;

	private boolean secure = false;

	private boolean persistent = false;

	private boolean httpOnly = false;

	public DefaultCookie() {
		super();
		ports = new int[0];
	}

	public DefaultCookie(int port, String name, String value) {
		super();
		ports = new int[1];
		ports[0] = port;
		this.name = name;
		this.value = value;
	}

	private DefaultCookie(String domain, String path, int[] ports, boolean secure, String name, String value,
			boolean persistent, long expiryTime, String comment) {
		super();
		this.domain = domain;
		this.path = path;
		this.ports = ports;
		this.name = name;
		this.value = value;
		this.secure = secure;
		this.persistent = persistent;
		this.expiryTime = expiryTime;
		this.comment = comment;
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
	public int[] getPorts() {
		return ports;
	}

	@Override
	public void addPort(int port) {
		int pl = ports.length;
		int[] newports = new int[pl + 1];
		for (int i = 0; i < pl; i++) {
			newports[i] = ports[i];
		}
		newports[pl] = port;
		this.ports = newports;
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

	@Override
	public long getExpiryTime() {
		return expiryTime;
	}

	@Override
	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
		this.persistent = true;
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
		return persistent;
	}

	@Override
	public void setHttpOnly() {
		this.httpOnly = true;
	}

	@Override
	public boolean isHttpOnly() {
		return httpOnly;
	}

	@Override
	public DefaultCookie clone() {
		int[] clonedports = new int[ports.length];
		for (int i = 0; i < clonedports.length; i++) {
			clonedports[i] = ports[i];
		}
		return new DefaultCookie(domain, path, clonedports, secure, name, value, persistent, expiryTime, comment);
	}

}
