/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.io.IOException;

import io.sf.carte.util.SimpleWriter;

/**
 * Define methods for formatting properties or descriptors in a declaration,
 * generally for a computed style.
 * 
 * @see StyleFormattingFactory
 * @see CSSStyleSheetFactory#setStyleFormattingFactory(StyleFormattingFactory)
 */
public interface DeclarationFormattingContext {

	void startPropertyDeclaration(SimpleWriter wri) throws IOException;

	void endPropertyDeclaration(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a colon to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeColon(SimpleWriter wri) throws IOException;

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
	 * @param wri  the writer.
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

}
