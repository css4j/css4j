/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

/**
 * Thrown when a property does not seem to have a correct value.
 */
public class CSSPropertyValueException extends CSSPropertyException {

	private static final long serialVersionUID = -1271227099310234530L;

	private String stringValue = null;

	public CSSPropertyValueException() {
	}

	public CSSPropertyValueException(String message) {
		super(message);
	}

	public CSSPropertyValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public CSSPropertyValueException(Throwable cause) {
		super(cause);
	}

	/**
	 * Sets the text string of the value causing the exception.
	 * 
	 * @param text
	 *            the value text.
	 */
	public void setValueText(String text) {
		this.stringValue = text;
	}

	/**
	 * Gets the text string of the value causing the exception.
	 * 
	 * @return the value text.
	 */
	public String getValueText() {
		return stringValue;
	}
}
