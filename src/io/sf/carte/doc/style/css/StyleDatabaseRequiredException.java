/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * Thrown when a requested operation requires a {@link StyleDatabase} to
 * complete.
 */
public class StyleDatabaseRequiredException extends IllegalStateException {

	private static final long serialVersionUID = 1L;
	private String cssText = null;

	public StyleDatabaseRequiredException() {
	}

	public StyleDatabaseRequiredException(String s) {
		super(s);
	}

	public StyleDatabaseRequiredException(Throwable cause) {
		super(cause);
	}

	public StyleDatabaseRequiredException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getMessage() {
		if (cssText != null) {
			return super.getMessage() + " (" + cssText + ")";
		}
		return super.getMessage();
	}

	/**
	 * Sets the text string of the value causing the exception.
	 * 
	 * @param text
	 *            the value text.
	 */
	public void setValueText(String text) {
		this.cssText = text;
	}

	/**
	 * Gets the text string of the value causing the exception.
	 * 
	 * @return the value text.
	 */
	public String getValueText() {
		return cssText;
	}

}
