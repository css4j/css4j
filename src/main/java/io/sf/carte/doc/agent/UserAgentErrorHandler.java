/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.net.URL;

/**
 * Handles errors that the user agent finds.
 *
 */
public interface UserAgentErrorHandler {

	@Deprecated
	default void onSuperCookie(URL offendingURL, String domain) {
	}

	@Deprecated
	default void onUnknownProperty(String propertyName, String value) {
	}

	default void onWrongPropertyValue(String propertyName, String value) {
	}

	/**
	 * Resets the error handler.
	 * <p>
	 * Depending on the error handler, this call may not have any effect.
	 * </p>
	 */
	default void reset() {
	}

}
