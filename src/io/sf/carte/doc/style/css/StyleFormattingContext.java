/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.io.IOException;

import org.w3c.dom.css.CSSRule;

import io.sf.carte.util.SimpleWriter;

/**
 * Define methods for style text formatting.
 * <p>
 * Different implementations of this interface can provide different style text
 * formattings.
 * 
 * @see StyleFormattingFactory
 * @see CSSStyleSheetFactory#setStyleFormattingFactory(StyleFormattingFactory)
 */
public interface StyleFormattingContext {

	void deepenCurrentContext();

	void endCurrentContext(CSSRule rule);

	void endPropertyDeclaration(SimpleWriter wri) throws IOException;

	void endInlinePropertyDeclaration(SimpleWriter wri) throws IOException;

	void endRule(SimpleWriter wri) throws IOException;

	void endRuleList(SimpleWriter wri) throws IOException;

	void endStyleDeclaration(SimpleWriter wri) throws IOException;

	void newLine(SimpleWriter wri) throws IOException;

	void setParentContext(CSSRule rule);

	void startPropertyDeclaration(SimpleWriter wri) throws IOException;

	void startRule(SimpleWriter wri) throws IOException;

	void startStyleDeclaration(SimpleWriter wri) throws IOException;

	void updateContext(CSSRule rule);

	void writeColon(SimpleWriter wri) throws IOException;

	void writeComment(SimpleWriter wri, String comment) throws IOException;

	void writeComma(SimpleWriter wri) throws IOException;

	void writeFullIndent(SimpleWriter wri) throws IOException;

	void writeImportantPriority(SimpleWriter wri) throws IOException;

	void writeLeftCurlyBracket(SimpleWriter wri) throws IOException;

	void writeLevelIndent(SimpleWriter wri) throws IOException;

	void writeRightCurlyBracket(SimpleWriter wri) throws IOException;

	void writeSemiColon(SimpleWriter wri) throws IOException;

	void writeURL(SimpleWriter wri, String href) throws IOException;

	/**
	 * Write a css value to the given writer.
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	void writeValue(SimpleWriter wri, String propertyName, ExtendedCSSValue value) throws IOException;

	/**
	 * Serialize a css shorthand value to the given writer.
	 * <p>
	 * Although the <code>CSSShorthandValue</code> type contains priority
	 * information, this method only writes the value.
	 * 
	 * @param wri           the writer.
	 * @param shorthandName the name of the shorthand property whose value is being
	 *                      printed.
	 * @param value         the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	void writeShorthandValue(SimpleWriter wri, String shorthandName, CSSShorthandValue value) throws IOException;

}
