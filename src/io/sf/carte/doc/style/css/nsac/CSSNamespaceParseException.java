/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 * 
 * Copyright © 2017,2018 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-19980720
 * 
 */
package io.sf.carte.doc.style.css.nsac;

import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.Locator;

/**
 * Namespace-related parse exception.
 */
public class CSSNamespaceParseException extends CSSParseException {

	private static final long serialVersionUID = 1L;

	public CSSNamespaceParseException(String message, Locator locator, Exception e) {
		super(message, locator, e);
	}

	public CSSNamespaceParseException(String message, Locator locator) {
		super(message, locator);
	}

	public CSSNamespaceParseException(String message, String uri, int lineNumber, int columnNumber, Exception e) {
		super(message, uri, lineNumber, columnNumber, e);
	}

	public CSSNamespaceParseException(String message, String uri, int lineNumber, int columnNumber) {
		super(message, uri, lineNumber, columnNumber);
	}

}
