/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import org.w3c.dom.DOMException;

/**
 * This exception can be used to force a set of lexical units to be processed as
 * a lexical value.
 */
public class CSSLexicalProcessingException extends DOMException {

	private static final long serialVersionUID = 1L;

	public CSSLexicalProcessingException(String message) {
		super(DOMException.HIERARCHY_REQUEST_ERR, message);
	}

}
