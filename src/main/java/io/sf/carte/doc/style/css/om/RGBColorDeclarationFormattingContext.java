/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSColorMixFunction;
import io.sf.carte.doc.style.css.CSSColorValue;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.util.SimpleWriter;

/**
 * DeclarationFormattingContext that serializes colors as RGB (sRGB).
 */
public class RGBColorDeclarationFormattingContext extends ColorDeclarationFormattingContext {

	private static final long serialVersionUID = 1L;

	/**
	 * Write a css {@code COLOR} to the given writer.
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	@Override
	protected void writeColor(SimpleWriter wri, String propertyName, CSSColorValue value)
			throws IOException {
		try {
			RGBAColor rgb = value.toRGBColor();
			wri.write(rgb.toString());
			return;
		} catch (DOMException e) {
		}
		super.writeColor(wri, propertyName, value);
	}

	/**
	 * Write a css {@code COLOR_MIX} to the given writer.
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	@Override
	protected void writeColorMix(SimpleWriter wri, String propertyName, CSSColorMixFunction value)
			throws IOException {
		try {
			RGBAColor rgb = value.toRGBColor();
			wri.write(rgb.toString());
			return;
		} catch (DOMException e) {
		}
		super.writeColorMix(wri, propertyName, value);
	}

	/**
	 * Write a minified css {@code COLOR} value to the given writer.
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	@Override
	protected void writeMinifiedColor(SimpleWriter wri, String propertyName, CSSColorValue value)
			throws IOException {
		try {
			RGBAColor rgb = value.toRGBColor();
			wri.write(rgb.toMinifiedString());
			return;
		} catch (DOMException e) {
		}
		super.writeMinifiedColor(wri, propertyName, value);
	}

	/**
	 * Write a minified css {@code COLOR_MIX} value to the given writer.
	 * 
	 * @param wri          the writer.
	 * @param propertyName the name of the property whose value is being printed.
	 * @param value        the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	@Override
	protected void writeMinifiedColorMix(SimpleWriter wri, String propertyName,
			CSSColorMixFunction value) throws IOException {
		try {
			RGBAColor rgb = value.toRGBColor();
			wri.write(rgb.toMinifiedString());
			return;
		} catch (DOMException e) {
		}
		super.writeMinifiedColorMix(wri, propertyName, value);
	}

}
