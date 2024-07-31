/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.stylesheets.DocumentStyle;

/**
 * A CSS-enabled Document.
 *
 * @author Carlos
 *
 */
public interface CSSDocument extends Document, DocumentStyle, CSSNode {

	/**
	 * The style computations operate under a compliance mode. Two modes are supported:
	 * <code>STRICT</code> and <code>QUIRKS</code>.
	 * <p>
	 * <code>QUIRKS</code> and <code>STRICT</code> differ in the matching of class and ID
	 * selectors, and the default user agent sheet is slightly different.
	 * </p>
	 */
	enum ComplianceMode {
		QUIRKS, STRICT
	}

	/**
	 * Creates an element of the type specified.
	 * <p>
	 * The presence of <code>IMPLIED</code> attributes is implementation-dependent.
	 *
	 * @param tagName the tag name of the element to create.
	 * @return the new <code>CSSElement</code>.
	 * @throws DOMException INVALID_CHARACTER_ERR if the name is not an XML valid
	 *                      name.
	 */
	@Override CSSElement createElement(String tagName) throws DOMException;

	/**
	 * {@inheritDoc}
	 */
	@Override CSSElement createElementNS(String namespaceURI, String qualifiedName) throws DOMException;

	/**
	 * Get the compatibility mode ({@code compatMode}) attribute.
	 * 
	 * @return the string "BackCompat" if document’s mode is {@code QUIRKS},
	 *         otherwise "CSS1Compat".
	 */
	default String getCompatMode() {
		DocumentType doctype = getDoctype();
		if (doctype != null) {
			return "CSS1Compat";
		}
		return "BackCompat";
	}

	/**
	 * Get the compliance mode of this document.
	 * <p>
	 * The styling in this document operate under a compliance mode. Two modes are supported:
	 * <code>STRICT</code> and <code>QUIRKS</code>.
	 * </p>
	 * <p>
	 * <code>QUIRKS</code> and <code>STRICT</code> differ in the matching of class and ID
	 * selectors, and the default user agent sheet is slightly different.
	 * </p>
	 *
	 * @return the compliance mode.
	 */
	CSSDocument.ComplianceMode getComplianceMode();

	/**
	 * Get the child node which is the document element of this document.
	 *
	 * @return the document element.
	 */
	@Override CSSElement getDocumentElement();

	/**
	 * {@inheritDoc}
	 */
	@Override CSSElement getElementById(String elementId);

	/**
	 * A list containing all the style sheets explicitly linked into or embedded
	 * in a document. For HTML documents, this includes external style sheets,
	 * included via the HTML LINK element, and inline STYLE elements.
	 */
	@Override CSSStyleSheetList<? extends CSSRule> getStyleSheets();

	/**
	 * Gets the list of available style set titles.
	 *
	 * @return the list of available style set titles.
	 */
	DOMStringList getStyleSheetSets();

	/**
	 * Gets the title of the currently selected style sheet set.
	 *
	 * @return the title of the currently selected style sheet, or the empty
	 *         string if none is selected.
	 */
	String getSelectedStyleSheetSet();

	/**
	 * Selects a style sheet set, disabling the other non-persistent sheet sets.
	 * If the name is the empty string, all non-persistent sheets will be
	 * disabled. Otherwise, if the name does not match any of the sets, does
	 * nothing.
	 *
	 * @param name
	 *            the case-sensitive name of the set to select.
	 */
	void setSelectedStyleSheetSet(String name);

	/**
	 * Gets the style sheet set that was last selected.
	 *
	 * @return the last selected style sheet set, or <code>null</code> if none.
	 */
	String getLastStyleSheetSet();

	/**
	 * Enables a style sheet set. If the name does not match any of the sets,
	 * does nothing.
	 *
	 * @param name
	 *            the case-sensitive name of the set to enable.
	 */
	void enableStyleSheetsForSet(String name);

	/**
	 * Registers the definition of a custom property.
	 * 
	 * @param definition the definition.
	 * @see CSSStyleSheetFactory#createPropertyDefinition(String, CSSValueSyntax,
	 *      boolean, CSSLexicalValue)
	 */
	void registerProperty(CSSPropertyDefinition definition);

	/**
	 * Gets the merged style sheet that applies to this document, resulting from
	 * the merge of the document's default style sheet, the document linked or
	 * embedded style sheets, and the non-important part of the user style
	 * sheet. Does not include overriden styles nor the 'important' part of the
	 * user-defined style sheet.
	 *
	 * @return the merged style sheet that applies to this document.
	 */
	DocumentCSSStyleSheet getStyleSheet();

	/**
	 * Gets the style database currently used to apply specific styles to this
	 * document.
	 *
	 * @return the style database.
	 */
	StyleDatabase getStyleDatabase();

	/**
	 * Set the medium that will be used to compute the styles of this document.
	 *
	 * @param medium
	 *            the target medium.
	 * @throws CSSMediaException
	 *             if the document is unable to target the given medium.
	 */
	void setTargetMedium(String medium) throws CSSMediaException;

	/**
	 * This document's current target medium name (e.g. 'screen').
	 *
	 * @return the target medium name of this document.
	 */
	String getTargetMedium();

	/**
	 * Gets the document's canvas for the current target medium.
	 *
	 * @return the canvas, or null if the DeviceFactory does not support canvas
	 *         for the current medium.
	 */
	CSSCanvas getCanvas();

	/**
	 * Forces the cascade to be rebuilt the next time that a computed style is
	 * obtained.
	 * <p>
	 * This method should be called after you modify the styles in a way that is not
	 * detected by the library (like modifying a value).
	 * </p>
	 */
	void rebuildCascade();

	/**
	 * Get the referrer policy obtained through the 'Referrer-Policy' header or a meta
	 * element.
	 *
	 * @return the referrer policy, or the empty string if none was specified.
	 */
	String getReferrerPolicy();

	/**
	 * Is the provided URL a safe origin to load certain external resources?
	 *
	 * @param linkedURL
	 *            the URL of the external resource.
	 *
	 * @return <code>true</code> if is a safe origin, <code>false</code> otherwise.
	 */
	boolean isSafeOrigin(URL linkedURL);

	/**
	 * Determine whether the retrieval of the given URL is authorized.
	 * <p>
	 * This check is less restrictive than {@link #isSafeOrigin(URL)}.
	 * </p>
	 * 
	 * @param url the URL to check.
	 * @return {@code true} if allowed.
	 */
	boolean isAuthorizedOrigin(URL url);

	/**
	 * Gets the Base URL of this Document.
	 *
	 * @return the base URL, or null if no base URL could be determined.
	 */
	URL getBaseURL();

	/**
	 * Gets an URL for the given URI, taking into account the Base URL if
	 * appropriate.
	 *
	 * @param uri
	 *            the uri.
	 * @return the absolute URL.
	 * @throws MalformedURLException
	 *             if the uri was wrong.
	 */
	URL getURL(String uri) throws MalformedURLException;

	/**
	 * Opens a connection for the given URL.
	 *
	 * @param url
	 *            the URL to open a connection to.
	 * @return the URL connection.
	 * @throws IOException
	 *             if the connection could not be opened.
	 */
	URLConnection openConnection(URL url) throws IOException;

	/**
	 * Has this URI been visited by the agent ?
	 *
	 * @param href
	 *            the URI.
	 * @return <code>true</code> if visited, <code>false</code> if not visited or the agent does not
	 *         support history.
	 */
	boolean isVisitedURI(String href);

	/**
	 * Gets the document-level error handler.
	 *
	 * @return the error handler.
	 */
	ErrorHandler getErrorHandler();

	/**
	 * Has any of the linked or embedded style sheets any error or warning ?
	 *
	 * @return <code>true</code> if any of the linked or embedded style sheets has any NSAC or rule error
	 *         or warning, <code>false</code> otherwise.
	 */
	boolean hasStyleIssues();

}
