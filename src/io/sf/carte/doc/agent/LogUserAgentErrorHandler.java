/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUserAgentErrorHandler implements UserAgentErrorHandler {
	static Logger log = LoggerFactory.getLogger(LogUserAgentErrorHandler.class.getName());

	@Override
	public void onSuperCookie(URL offendingURL, String domain) {
		log.warn("URL " + offendingURL.toExternalForm() + " attempted to set a supercookie for top-level-suffix "
				+ domain + ".");
	}

	@Override
	public void onUnknownProperty(String propertyName, String value) {
		log.warn("Unknown property was set: " + propertyName);
	}

	@Override
	public void onWrongPropertyValue(String propertyName, String value) {
		log.error("Wrong value for property " + propertyName + ": " + value);
	}

}
