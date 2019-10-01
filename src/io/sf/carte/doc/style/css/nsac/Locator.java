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
 * Locate a place in a document.
 * <p>
 * Based on SAC's {@code Locator} interface by Philippe Le Hegaret.
 * </p>
 */
public interface Locator {

	/**
	 * Return the line number where this locator points at.
	 * <p>
	 * The first line number is 1.
	 * </p>
	 * 
	 * @return the line number, or -1 if not available.
	 * @see #getColumnNumber
	 */
	int getLineNumber();

	/**
	 * Return the column number where this locator points at.
	 * <p>
	 * The first column in a line is position 1.
	 * </p>
	 * 
	 * @return the column number, or -1 if not available.
	 * @see #getLineNumber
	 */
	int getColumnNumber();

}
