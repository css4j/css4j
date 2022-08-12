/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.RGBAColor;
import io.sf.carte.util.SimpleWriter;

/**
 * DeclarationFormattingContext that serializes colors as RGB.
 */
public class RGBColorDeclarationFormattingContext extends DefaultDeclarationFormattingContext {

	private static final long serialVersionUID = 1L;

	@Override
	public void writeValue(SimpleWriter wri, String propertyName, CSSValue value) throws IOException {
		if (value.getPrimitiveType() == Type.COLOR) {
			try {
				RGBAColor rgb = ((CSSTypedValue) value).toRGBColor();
				wri.write(rgb.toString());
				return;
			} catch (DOMException e) {
			}
		}
		super.writeValue(wri, propertyName, value);
	}

}
