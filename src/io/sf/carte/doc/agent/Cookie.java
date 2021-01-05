/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

public interface Cookie {

	String getDomain();

	void setDomain(String domain);

	int[] getPorts();

	void addPort(int port);

	String getPath();

	void setPath(String path);

	String getName();

	void setName(String name);

	String getValue();

	void setValue(String value);

	long getExpiryTime();

	void setExpiryTime(long expiryTime);

	String getComment();

	void setComment(String comment);

	boolean isSecure();

	void setSecure();

	boolean isPersistent();

	void setHttpOnly();

	boolean isHttpOnly();

}
