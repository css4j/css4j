/*

 Copyright (c) 2005-2024, Carlos Amengual.

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

	/**
	 * Starts a property declaration in a rule style (does not apply to inline
	 * styles).
	 * <p>
	 * It is called before writing the property name.
	 * </p>
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void startPropertyDeclaration(SimpleWriter wri) throws IOException;

	/**
	 * Ends a property declaration in a rule style (does not apply to inline
	 * styles).
	 * <p>
	 * It is called after {@link #writeSemiColon(SimpleWriter)}.
	 * </p>
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
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
	 * <p>
	 * Called after writing the property name.
	 * </p>
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
	 * <p>
	 * The default implementation calls {@link CSSValue#writeCssText(SimpleWriter)}.
	 * </p>
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	default void writeValue(SimpleWriter wri, String propertyName, CSSValue value)
			throws IOException {
		value.writeCssText(wri);
	}

	/**
	 * Write a minified css value to the given writer.
	 * <p>
	 * Note that in order to guarantee some consistency, shorthands may not use this
	 * method to minify values.
	 * </p>
	 * <p>
	 * The default implementation just writes
	 * {@link CSSValue#getMinifiedCssText(String)}.
	 * </p>
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	default void writeMinifiedValue(SimpleWriter wri, String propertyName, CSSValue value)
		throws IOException {
		wri.write(value.getMinifiedCssText(propertyName));
	}

}
