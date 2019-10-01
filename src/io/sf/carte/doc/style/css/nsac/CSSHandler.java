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

import java.util.List;

/**
 * A CSS event handler for low-level parsing.
 * <p>
 * Based on SAC's {@code DocumentHandler} interface by Philippe Le Hegaret.
 * </p>
 */
public interface CSSHandler {

	/**
	 * Receive notification of the beginning of a style sheet.
	 *
	 * The CSS parser will invoke this method only once, before any other methods in
	 * this interface, but only if a sheet is being processed. It is not called when
	 * using, for example, {@code parseRule}.
	 */
	void startDocument();

	/**
	 * Receive notification of the end of a document.
	 *
	 * The CSS parser will invoke this method only once, and it will be the last
	 * method invoked during the parse. The parser shall not invoke this method
	 * unless it has reached the end of input.
	 */
	void endDocument();

	/**
	 * Receive notification of a comment. If the comment appears in a declaration
	 * (e.g. color: /* comment * / blue;), the parser notifies the comment before
	 * the declaration.
	 *
	 * @param text The comment.
	 */
	void comment(String text);

	/**
	 * Receive notification of an unknown {@literal @}-rule not supported by this parser.
	 *
	 * @param atRule The complete {@literal @}-rule.
	 */
	void ignorableAtRule(String atRule);

	/**
	 * Receive notification of a namespace declaration.
	 *
	 * @param prefix <code>null</code> if this is the default namespace
	 * @param uri    The URI for this namespace.
	 */
	void namespaceDeclaration(String prefix, String uri);

	/**
	 * Receive notification of a import statement in the style sheet.
	 *
	 * @param uri                The URI of the imported style sheet.
	 * @param media              The intended destination media for style
	 *                           information.
	 * @param defaultNamepaceURI The default namespace URI for the imported style
	 *                           sheet.
	 */
	void importStyle(String uri, List<String> media, String defaultNamespaceURI);

	/**
	 * Receive notification of the beginning of a media statement.
	 *
	 * The Parser will invoke this method at the beginning of every media statement
	 * in the style sheet. There will be a corresponding endMedia() event for every
	 * startElement() event.
	 *
	 * @param media The intended destination media for style information.
	 */
	void startMedia(List<String> media);

	/**
	 * Receive notification of the end of a media statement.
	 *
	 * @param media The intended destination media for style information.
	 */
	void endMedia(List<String> media);

	/**
	 * Receive notification of the beginning of a page statement.
	 *
	 * The Parser will invoke this method at the beginning of every page statement
	 * in the style sheet. There will be a corresponding endPage() event for every
	 * startPage() event.
	 *
	 * @param name        the name of the page (if any, null otherwise)
	 * @param pseudo_page the pseudo page (if any, null otherwise)
	 */
	void startPage(String name, String pseudo_page);

	/**
	 * Receive notification of the end of a media statement.
	 *
	 * @param media       The intended destination medium for style information.
	 * @param pseudo_page the pseudo page (if any, null otherwise)
	 */
	void endPage(String name, String pseudo_page);

	/**
	 * Receive notification of the beginning of a font face statement.
	 *
	 * The Parser will invoke this method at the beginning of every font face
	 * statement in the style sheet. There will be a corresponding endFontFace()
	 * event for every startFontFace() event.
	 */
	void startFontFace();

	/**
	 * Receive notification of the end of a font face statement.
	 */
	void endFontFace();

	/**
	 * Receive notification of the beginning of a rule statement.
	 *
	 * @param selectors the intended selectors for next declarations.
	 */
	void startSelector(SelectorList selectors);

	/**
	 * Receive notification of the end of a rule statement.
	 *
	 * @param selectors the intended selectors for the previous declarations.
	 */
	void endSelector(SelectorList selectors);

	/**
	 * Receive notification of a declaration.
	 *
	 * @param name      the name of the property.
	 * @param value     the value of the property.
	 * @param important is this property important ?
	 */
	void property(String name, LexicalUnit value, boolean important);

}
