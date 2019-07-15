/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.ExtendedCSSStyleDeclaration;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.util.SimpleWriter;

/**
 * Abstract class to be inherited by all CSS style declarations.
 * 
 * @author Carlos Amengual
 *
 */
abstract public class AbstractCSSStyleDeclaration implements LexicalPropertyListener, ExtendedCSSStyleDeclaration {

	abstract void addProperty(String propertyName, AbstractCSSValue cssValue, String priority);

	/**
	 * Clear the internal property lists.
	 */
	abstract void clear();

	/**
	 * Retrieves a minified textual representation of the declaration block (excluding the
	 * surrounding curly braces).
	 * 
	 * @return the minified representation of this style declaration.
	 */
	@Override
	abstract public String getMinifiedCssText();

	/**
	 * Writes a textual representation of the declaration block (excluding the surrounding
	 * curly braces) to a <code>SimpleWriter</code>, and according to a
	 * <code>StyleFormattingContext</code>.
	 * 
	 * @param wri
	 *            the simple writer to write to.
	 * @param context
	 *            the style formatting context.
	 * @throws IOException
	 *             if a problem occurs while writing the text.
	 */
	@Override
	abstract public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException;

	@Override
	abstract public AbstractCSSValue getPropertyCSSValue(String propertyName);

	/**
	 * Get the error handler for this style declaration.
	 * 
	 * @return the error handler, or <code>null</code> if this is an anonymous style
	 *         declaration.
	 */
	abstract public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler();

	abstract protected CSSStyleSheetFactory getStyleSheetFactory();

	/**
	 * Is this style declaration empty?
	 * 
	 * @return <code>true</code> if the declaration is empty, <code>false</code> otherwise.
	 */
	abstract public boolean isEmpty();

	/**
	 * Splits this style declaration in two: one for important properties only, and the other
	 * with normal properties.
	 * 
	 * @param importantDecl
	 *            the style declaration for important properties.
	 * @param normalDecl
	 *            the style declaration for normal properties.
	 */
	abstract protected void prioritySplit(AbstractCSSStyleDeclaration importantDecl,
			AbstractCSSStyleDeclaration normalDecl);
}
