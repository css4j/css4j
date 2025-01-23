/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2019-2025, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc;

/**
 * Null character exception for DOM.
 * <p>
 * Although NULL is a valid character in CSS (in the sense that it does not
 * cause the parsing to fail), it is often used as a hack to target specific
 * browsers. This exception is intended to help in the identification of such
 * cases.
 *
 */
public class DOMNullCharacterException extends DOMCharacterException {

	private static final long serialVersionUID = 2L;

	public DOMNullCharacterException(int index) {
		super("Found a null character, probably a browser hack", index);
	}

	public DOMNullCharacterException(String message, int index) {
		super(message, index);
	}

}
