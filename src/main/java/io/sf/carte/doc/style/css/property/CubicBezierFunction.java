/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.util.Iterator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSNumberValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

/**
 * Easing function.
 * 
 */
class CubicBezierFunction extends EasingFunction {

	private static final long serialVersionUID = 1L;

	CubicBezierFunction() {
		super(Type.CUBIC_BEZIER);
	}

	protected CubicBezierFunction(CubicBezierFunction copied) {
		super(copied);
	}

	private void validate() throws DOMException {
		LinkedCSSValueList args = getArguments();
		int len = args.size();
		if (len != 4) {
			throw new DOMException(DOMException.SYNTAX_ERR,
					"cubic-bezier() requires four arguments: " + getCssText());
		}

		CSSValueSyntax syn = SyntaxParser.createSimpleSyntax("number");
		Iterator<StyleValue> it = args.iterator();
		StyleValue arg = it.next();
		checkX(arg, syn);
		arg = it.next();
		checkY(arg, syn);
		arg = it.next();
		checkX(arg, syn);
		arg = it.next();
		checkY(arg, syn);
	}

	private void checkX(StyleValue arg, CSSValueSyntax syn) {
		if (arg.getPrimitiveType() == Type.NUMERIC) {
			CSSNumberValue number = (CSSNumberValue) arg;
			if (number.getUnitType() == CSSUnit.CSS_NUMBER && !number.isNegativeNumber()) {
				return;
			}
		} else if (arg.matches(syn) == Match.TRUE) {
			return;
		}
		throw createUnexpectedArgumentTypeException(arg);
	}

	private void checkY(StyleValue arg, CSSValueSyntax syn) {
		if (arg.matches(syn) == Match.FALSE) {
			throw createUnexpectedArgumentTypeException(arg);
		}
	}

	private DOMException createUnexpectedArgumentTypeException(StyleValue arg) {
		return new DOMException(DOMException.TYPE_MISMATCH_ERR,
				"Unexpected argument in cubic-bezier(): " + arg.getCssText());
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new BezierLexicalSetter();
	}

	class BezierLexicalSetter extends FunctionLexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) throws DOMException {
			super.setLexicalUnit(lunit);
			validate();
		}

	}

	@Override
	public CubicBezierFunction clone() {
		return new CubicBezierFunction(this);
	}

}
