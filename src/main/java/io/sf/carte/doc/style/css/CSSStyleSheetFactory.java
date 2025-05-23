/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

import java.io.IOException;
import java.io.Reader;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

/**
 * CSS style sheet factory.
 */
public interface CSSStyleSheetFactory {

	// Origins
	int ORIGIN_USER_IMPORTANT = 0x3ff;
	int ORIGIN_AUTHOR = 0x7fff;
	int ORIGIN_USER = 0xfffa;
	int ORIGIN_USER_AGENT = 0xfffd;

	// Flags

	/**
	 * Prefer single quotes in the serialization of strings.
	 */
	short FLAG_STRING_SINGLE_QUOTE = 1;

	/**
	 * Prefer double quotes in the serialization of strings.
	 */
	short FLAG_STRING_DOUBLE_QUOTE = 2;

	/**
	 * Creates a stand-alone author style sheet.
	 * <p>
	 *
	 * @param title
	 *            the advisory title.
	 * @param media
	 *            the target media for style.
	 * @return the style sheet.
	 */
	CSSStyleSheet<? extends CSSRule> createStyleSheet(String title, MediaQueryList media);

	/**
	 * Create a style declaration for an anonymous inline box, associated to a text/cdata
	 * node.
	 *
	 * @param node
	 *            the node that has the declaration associated to it.
	 * @return the anonymous style declaration.
	 */
	CSSStyleDeclaration createAnonymousStyleDeclaration(Node node);

	/**
	 * Gets the User Agent default CSS style sheet to be used by this factory.
	 *
	 * @param mode
	 *            the compliance mode.
	 * @return the default style sheet, or an empty sheet if no User Agent sheet was defined.
	 */
	DocumentCSSStyleSheet getUserAgentStyleSheet(CSSDocument.ComplianceMode mode);

	/**
	 * Sets a default HTML default style sheet as the user agent style sheet.
	 */
	void setDefaultHTMLUserAgentSheet();

	/**
	 * Sets the CSS style sheet defined by the end user.
	 * <p>
	 * The sheet in the supplied reader should contain user preferences, and will be
	 * appropriately merged with the other style sheets.
	 * </p>
	 *
	 * @param url the URL of the style sheet. If {@code null}, the call is
	 *            equivalent to {@link #setUserStyleSheet(Reader)}.
	 * @param re  the reader with the user style sheet. If {@code null}, a
	 *            connection to the URL shall be opened. If both arguments are
	 *            {@code null}, the user style sheet shall be cleared.
	 * @throws DOMException if a problem is found parsing the sheet.
	 * @throws IOException  if there is a problem opening the URL or reading the
	 *                      reader.
	 */
	void setUserStyleSheet(String url, Reader re) throws DOMException, IOException;

	/**
	 * Sets the CSS style sheet defined by the end user.
	 * <p>
	 * The sheet in the supplied reader should contain user preferences, and will be
	 * appropriately merged with the other style sheets.
	 * </p>
	 *
	 * @param re the reader with the user style sheet. If {@code null}, the user
	 *           style sheet shall be cleared.
	 * @throws DOMException if a problem is found parsing the sheet.
	 * @throws IOException  if there is a problem retrieving the reader.
	 */
	void setUserStyleSheet(Reader re) throws DOMException, IOException;

	/**
	 * Set a configuration flag. Do not confuse with NSAC flags, which must be set
	 * at this object's creation time (see the documentation for implementation's
	 * constructors).
	 * <p>
	 * The flags are implementation-dependent.
	 *
	 * @param flag
	 *             the flag to set.
	 */
	void setFactoryFlag(short flag);

	/**
	 * Set the value of the <code>lenientSystemValues</code> flag. Do not confuse this flag
	 * with the NSAC flags.
	 * <p>
	 * The style sheet factories provide system default values, and its behaviour depends on a
	 * <code>lenientSystemValues</code> flag. The lenient flag allows to compute values by
	 * giving reasonable defaults to properties with system-dependent initial values:
	 * '#000000' for color and 'serif' for font-family.
	 * <p>
	 * If you work with style databases you do not need <code>lenientSystemValues</code>.
	 * <p>
	 * Default value is <code>true</code>.
	 *
	 * @param lenient
	 *            <code>true</code> or <code>false</code> to enable/disable the
	 *            <code>lenientSystemValues</code> flag.
	 */
	void setLenientSystemValues(boolean lenient);

	/**
	 * Get a system default value for the given property.
	 * <p>
	 * If the <code>lenientSystemValues</code> flag is <code>true</code>, returns a reasonable
	 * default for the property. Otherwise, it returns a system default meta-value.
	 *
	 * @param propertyName
	 *            the property name.
	 * @return the system default css primitive value.
	 */
	CSSTypedValue getSystemDefaultValue(String propertyName);

	/**
	 * Create a new StyleDeclarationErrorHandler for the given style rule.
	 *
	 * @param rule
	 *            the declaration rule the handler is for.
	 * @return the StyleDeclarationErrorHandler.
	 */
	StyleDeclarationErrorHandler createStyleDeclarationErrorHandler(CSSDeclarationRule rule);

	/**
	 * Create a new StyleDeclarationErrorHandler for the given style rule.
	 *
	 * @param owner
	 *            the element owner of the inline style.
	 * @return the StyleDeclarationErrorHandler.
	 */
	StyleDeclarationErrorHandler createInlineStyleErrorHandler(CSSElement owner);

	/**
	 * Creates a style sheet error handler.
	 *
	 * @param sheet
	 *            the style sheet whose errors are to be handled.
	 * @return the error handler.
	 */
	SheetErrorHandler createSheetErrorHandler(CSSStyleSheet<? extends CSSRule> sheet);

	/**
	 * Parses <code>mediaQueryString</code> and creates a new media query list.
	 * 
	 * @param mediaQueryString
	 *            the media query string.
	 * @param owner
	 *            the node that would handle errors, if any.
	 * @return a new media list for <code>mediaQueryString</code>.
	 */
	MediaQueryList createMediaQueryList(String mediaQueryString, Node owner);

	/**
	 * Parses and creates an immutable media query list for the given media.
	 * 
	 * @param media
	 *            the comma-separated list of media. If <code>null</code>, the
	 *            media list will be for all media.
	 * @param owner
	 *            the node that would handle errors, if any.
	 * @return the immutable media list.
	 */
	MediaQueryList createImmutableMediaQueryList(String media, Node owner);

	/**
	 * Create a property definition.
	 * <p>
	 * To parse the syntax, use a parser like
	 * {@link io.sf.carte.doc.style.css.parser.SyntaxParser SyntaxParser}.
	 * </p>
	 * 
	 * @param name         the property name (unescaped).
	 * @param syntax       the syntax.
	 * @param inherited    whether the property is inherited or not.
	 * @param initialValue the initial value.
	 * @return the property definition.
	 * @throws DOMException INVALID_ACCESS_ERR if the initial value does not match
	 *                      the syntax.
	 */
	CSSPropertyDefinition createPropertyDefinition(String name, CSSValueSyntax syntax, boolean inherited,
			LexicalUnit initialValue) throws DOMException;

	/**
	 * Get the style formatting factory to be used with this sheet factory.
	 *
	 * @return the style formatting factory.
	 */
	StyleFormattingFactory getStyleFormattingFactory();

	/**
	 * Set a style formatting factory to format style serializations made with sheets created
	 * by this sheet factory.
	 *
	 * @param factory
	 *            the style formatting factory.
	 */
	void setStyleFormattingFactory(StyleFormattingFactory factory);

	/**
	 * Gets the device factory.
	 *
	 * @return the device factory.
	 */
	DeviceFactory getDeviceFactory();

}
