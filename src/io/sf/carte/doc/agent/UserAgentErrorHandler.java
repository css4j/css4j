/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.net.URL;

/**
 * Handles errors that the user agent finds.
 * 
 * @author Carlos Amengual
 *
 */
public interface UserAgentErrorHandler {
	void onSuperCookie(URL offendingURL, String domain);

	void onUnknownProperty(String propertyName, String value);

	void onWrongPropertyValue(String propertyName, String value);
}
