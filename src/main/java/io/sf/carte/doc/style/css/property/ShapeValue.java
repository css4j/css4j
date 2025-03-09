/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSShapeValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
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

	DOMException createUnexpectedArgumentTypeException(LexicalUnit lu) {
		return createTypeMismatchException("Unexpected argument in path(): " + lu.getCssText());
	}

	private DOMException createTypeMismatchException(String message) {
		return new DOMException(DOMException.TYPE_MISMATCH_ERR, message);
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
