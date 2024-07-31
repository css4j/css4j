/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

/**
 * User agent configuration for http cookies.
 *
 * @author Carlos Amengual
 *
 */
public interface CookieConfig {

	boolean acceptsAllCookies();

	/**
	 * Accept all cookies when opening HTTP connections to retrieve resources.
	 *
	 * @param acceptAllCookies
	 *            true to accept all cookies, or <code>false</code> to ignore cookies.
	 */
	void setAcceptAllCookies(boolean acceptAllCookies);

	/**
	 * Does the user agent accept session cookies.
	 *
	 * @return <code>true</code> if session cookies are accepted, <code>false</code> otherwise.
	 */
	boolean acceptsSessionCookies();

	/**
	 * Accept only session cookies when opening HTTP connections to retrieve
	 * resources.
	 *
	 * @param acceptSessionCookies
	 *            true to accept session cookies, or <code>false</code> to ignore all
	 *            cookies.
	 */
	void setAcceptSessionCookies(boolean acceptSessionCookies);

}
