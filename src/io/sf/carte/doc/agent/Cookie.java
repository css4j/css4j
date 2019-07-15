/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.agent;

public interface Cookie {

	public String getDomain();

	public void setDomain(String domain);

	public int[] getPorts();

	public void addPort(int port);

	public String getPath();

	public void setPath(String path);

	public String getName();

	public void setName(String name);

	public String getValue();

	public void setValue(String value);

	public long getExpiryTime();

	public void setExpiryTime(long expiryTime);

	public String getComment();

	public void setComment(String comment);

	public boolean isSecure();

	public void setSecure();

	public boolean isPersistent();

	public void setHttpOnly();

	public boolean isHttpOnly();

}