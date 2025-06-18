/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.DOMHierarchyRequestException;

/**
 * This exception can be used to force a set of lexical units to be processed as
 * a lexical value.
 */
public class CSSLexicalProcessingException extends DOMHierarchyRequestException {

	private static final long serialVersionUID = 1L;

	public CSSLexicalProcessingException() {
		super("PROXY value found.");
	}

	public CSSLexicalProcessingException(String message) {
		super(message);
	}

}
