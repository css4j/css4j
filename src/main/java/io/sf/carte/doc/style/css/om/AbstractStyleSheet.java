/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.w3c.dom.DOMException;
import org.w3c.dom.stylesheets.StyleSheet;

import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.MediaQueryList;

/**
 * Abstract class to be inherited by all CSS style sheets.
 *
 */
abstract public class AbstractStyleSheet implements StyleSheet, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String advisoryTitle;

	/*
	 * The title is in the constructor because it being intern is part of the
	 * AbstractStyleSheet contract.
	 */
	protected AbstractStyleSheet(String title) {
		super();
		// All titles must be intern
		if (title != null) {
			title = title.intern();
		}
		advisoryTitle = title;
	}

	/**
	 * Gets the advisory title.
	 * 
	 * @return the title.
	 */
	@Override
	public String getTitle() {
		return advisoryTitle;
	}

	protected void setTitle(String title) {
		advisoryTitle = title;
	}

	/**
	 * Clone this style sheet.
	 * 
	 * @return the cloned style sheet.
	 */
	@Override
	abstract public StyleSheet clone();

	abstract protected ErrorHandler getDocumentErrorHandler();

	/**
	 * Open a non-interactive connection to the given URL.
	 * <p>
	 * If this sheet was obtained through a network connection, the returned connection should
	 * be opened by the same user agent (and appropriate credentials) that retrieved this
	 * sheet.
	 * 
	 * @param url
	 *            the URL to connect to.
	 * @param referrerPolicy
	 *            the content of the <code>referrerpolicy</code> content attribute, if any, or
	 *            the empty string.
	 * @return the network connection.
	 * @throws IOException
	 *             if an I/O problem occurs opening the connection.
	 */
	abstract public URLConnection openConnection(URL url, String referrerPolicy) throws IOException;

	/**
	 * Set the destination media for this sheet.
	 * 
	 * @param media the destination media.
	 * 
	 * @throws DOMException if the <code>media</code> is invalid.
	 */
	abstract protected void setMedia(MediaQueryList media) throws DOMException;

	/**
	 * Returns a minified parsable representation of the rule list of this sheet.
	 * 
	 * @return a minified parsable representation of the rule list of this sheet.
	 */
	abstract public String toMinifiedString();

	/**
	 * Returns a serialization of this style sheet in the form of a
	 * <code>STYLE</code> element with its attributes and content.
	 * 
	 * @return an HTML <code>STYLE</code> element representing this style sheet.
	 */
	abstract public String toStyleString();

}
