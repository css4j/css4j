/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2019-2025, Carlos Amengual.
 */
/*
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc;

import org.w3c.dom.DOMException;

/**
 * Invalid DOM character exception.
 * <p>
 * Provides a method to recover the index at which the problem occurred.
 */
public class DOMCharacterException extends DOMException {

	private static final long serialVersionUID = 1L;

	private final int index;

	public DOMCharacterException(int index) {
		this(autoMessage(index), index);
	}

	public DOMCharacterException(String message, int index) {
		super(DOMException.INVALID_CHARACTER_ERR, message);
		this.index = index;
	}

	private static String autoMessage(int index) {
		String message;
		if (index >= 0) {
			message = "Found an invalid character at index " + index;
		} else {
			message = "Found an invalid character";
		}
		return message;
	}

	/**
	 * Get the index at which the problem was found.
	 * 
	 * @return the index, starting at 0, or -1 if no index could be determined.
	 */
	public int getIndex() {
		return index;
	}

}
