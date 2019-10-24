/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.io.IOException;
import java.util.List;

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

	void endRule(SimpleWriter wri, List<String> trailingComments) throws IOException;

	void endRuleList(SimpleWriter wri) throws IOException;

	void endStyleDeclaration(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a newline to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void newLine(SimpleWriter wri) throws IOException;

	void setParentContext(CSSRule rule);

	void startPropertyDeclaration(SimpleWriter wri) throws IOException;

	void startRule(SimpleWriter wri, List<String> precedingComments) throws IOException;

	void startStyleDeclaration(SimpleWriter wri) throws IOException;

	void updateContext(CSSRule rule);

	/**
	 * Serialize a colon to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeColon(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a comment that precedes a rule.
	 * 
	 * @param wri     the writer to write to.
	 * @param comment the comment to serialize.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeComment(SimpleWriter wri, String comment) throws IOException;

	/**
	 * Serialize a comma to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeComma(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a full paragraph indent to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeFullIndent(SimpleWriter wri) throws IOException;

	/**
	 * Serialize an {@code important} priority declaration to a
	 * {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeImportantPriority(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a left curly bracket to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeLeftCurlyBracket(SimpleWriter wri) throws IOException;

	/**
	 * Serialize one level of indentation to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeLevelIndent(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a right curly bracket to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeRightCurlyBracket(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a semicolon to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeSemiColon(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a URL/URI to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @param href the URI.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeURL(SimpleWriter wri, String href) throws IOException;

	/**
	 * Write a css value to the given writer.
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	void writeValue(SimpleWriter wri, String propertyName, CSSValue value) throws IOException;

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
