/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.doc.style.css.CSSShapeValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Basic shape.
 * 
 */
abstract class ShapeValue extends TypedValue implements CSSShapeValue {

	private static final long serialVersionUID = 1L;

	ShapeValue(Type type) {
		super(type);
	}

	protected ShapeValue(ShapeValue copied) {
		super(copied);
	}

	@Override
	public String getCssText() {
		BufferSimpleWriter sw = new BufferSimpleWriter(32);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case basicShape:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	public abstract ShapeValue clone();

}
