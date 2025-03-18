/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;
import java.util.LinkedList;
import java.util.List;

public class TestUserAgentErrorHandler implements UserAgentErrorHandler {

	private final List<String> errors = new LinkedList<>();

	private boolean wrongPropertyFound = false;

	/**
	 * @return true if a wrong property was found.
	 */
	public boolean isWrongPropertyFound() {
		return wrongPropertyFound;
	}

	@Override
	public void onWrongPropertyValue(String propertyName, String value) {
		wrongPropertyFound = true;
		errors.add("Wrong value for property " + propertyName + ": " + value);
	}

	@Override
	public void reset() {
		wrongPropertyFound = false;
		errors.clear();
	}

}
