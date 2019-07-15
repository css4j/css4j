/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.io.IOException;
import java.io.Reader;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.agent.DeviceFactory;

/**
 * CSS style sheet factory.
 * 
 * @author Carlos Amengual
 * 
 */
public interface CSSStyleSheetFactory {

	public static final byte ORIGIN_USER_IMPORTANT = 3;
	public static final byte ORIGIN_AUTHOR = 8;
	public static final byte ORIGIN_USER = 9;
	public static final byte ORIGIN_USER_AGENT = 16;

	/**
	 * Creates a stand-alone author style sheet.
	 * <p>
	 * 
	 * @param title
	 *            the advisory title.
	 * @param media
	 *            the target media for style.
	 * @return the style sheet.
	 * @throws DOMException
	 *             if the media string could not be parsed.
	 */
	public ExtendedCSSStyleSheet<? extends ExtendedCSSRule> createStyleSheet(String title, String media) throws DOMException;

	/**
	 * Create a style declaration for an anonymous inline box, associated to a text/cdata
	 * node.
	 * 
	 * @param node
	 *            the node that has the declaration associated to it.
	 * @return the anonymous style declaration.
	 */
	public ExtendedCSSStyleDeclaration createAnonymousStyleDeclaration(Node node);

	/**
	 * Gets the User Agent default CSS style sheet to be used by this factory.
	 * 
	 * @param mode
	 *            the compliance mode.
	 * @return the default style sheet, or an empty sheet if no User Agent sheet was defined.
	 */
	public DocumentCSSStyleSheet getUserAgentStyleSheet(CSSDocument.ComplianceMode mode);

	/**
	 * Sets a default HTML default style sheet as the user agent style sheet.
	 */
	public void setDefaultHTMLUserAgentSheet();

	/**
	 * Sets the CSS style sheet defined by the end-user.
	 * <p>
	 * The sheet in the supplied reader should contain user preferences, and will be
	 * appropriately merged with the other style sheets.
	 * </p>
	 * 
	 * @param re
	 *            the reader with the user style sheet.
	 * @throws IOException
	 *             if there is a problem retrieving the reader.
	 */
	public void setUserStyleSheet(Reader re) throws IOException;

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
	public void setFactoryFlag(byte flag);

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
	public void setLenientSystemValues(boolean lenient);

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
	public ExtendedCSSPrimitiveValue getSystemDefaultValue(String propertyName);

	/**
	 * Create a new StyleDeclarationErrorHandler for the given style rule.
	 * 
	 * @param rule
	 *            the declaration rule the handler is for.
	 * @return the StyleDeclarationErrorHandler.
	 */
	public StyleDeclarationErrorHandler createStyleDeclarationErrorHandler(CSSDeclarationRule rule);

	/**
	 * Create a new StyleDeclarationErrorHandler for the given style rule.
	 * 
	 * @param owner
	 *            the element owner of the inline style.
	 * @return the StyleDeclarationErrorHandler.
	 */
	public StyleDeclarationErrorHandler createInlineStyleErrorHandler(CSSElement owner);

	/**
	 * Creates a style sheet error handler.
	 * 
	 * @param sheet
	 *            the style sheet whose errors are to be handled.
	 * @return the error handler.
	 */
	public SheetErrorHandler createSheetErrorHandler(ExtendedCSSStyleSheet<? extends ExtendedCSSRule> sheet);

	/**
	 * Get the style formatting factory to be used with this sheet factory.
	 * 
	 * @return the style formatting factory.
	 */
	public StyleFormattingFactory getStyleFormattingFactory();

	/**
	 * Set a style formatting factory to format style serializations made with sheets created
	 * by this sheet factory.
	 * 
	 * @param factory
	 *            the style formatting factory.
	 */
	public void setStyleFormattingFactory(StyleFormattingFactory factory);

	/**
	 * Gets the device factory.
	 * 
	 * @return the device factory.
	 */
	public DeviceFactory getDeviceFactory();

}
