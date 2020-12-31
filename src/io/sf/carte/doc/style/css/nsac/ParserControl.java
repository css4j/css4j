/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017-2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

/**
 * Allows convenient access to certain parser functionalities.
 */
public interface ParserControl {

	/**
	 * Register a new document event handler at the parser.
	 *
	 * @param handler the document handler.
	 */
	void setDocumentHandler(CSSHandler handler);

	/**
	 * Register a new error event handler at the parser.
	 *
	 * @param handler the error handler.
	 */
	void setErrorHandler(CSSErrorHandler handler);

	/**
	 * Get a reference to the error handler.
	 * 
	 * @return the error handler.
	 */
	CSSErrorHandler getErrorHandler();

	/**
	 * Create a locator for the current parsing location.
	 * 
	 * @return the locator.
	 */
	Locator createLocator();

}
