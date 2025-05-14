/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.impl.CSSUtil;

class EnvUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public EnvUnitImpl() {
		super(LexicalType.ENV);
	}

	@Override
	EnvUnitImpl instantiateLexicalUnit() {
		return new EnvUnitImpl();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		String name = parameters.getStringValue();
		LexicalUnitImpl fallback = getFallback();

		return CSSUtil.matchEnv(rootSyntax, syntax, name, fallback);
	}

	Dimension dimension() {
		String name = parameters.getStringValue();
		if (!CSSUtil.isEnvLengthName(name)) {
			LexicalUnitImpl fallback = getFallback();
			if (fallback != null) {
				try {
					return DimensionalAnalyzer.createDimension(fallback.getCssUnit());
				} catch (DOMException e) {
				}
			}
			return null;
		}
		Dimension dim = new Dimension();
		dim.category = Category.length;
		dim.exponent = 1;
		return dim;
	}

	private LexicalUnitImpl getFallback() {
		LexicalUnitImpl param = parameters;
		LexicalUnitImpl fallback;
		do {
			param = param.nextLexicalUnit;
			if (param == null) {
				fallback = null;
				break;
			}
			if (param.getLexicalUnitType() == LexicalType.OPERATOR_COMMA) {
				fallback = param.nextLexicalUnit;
				break;
			}
		} while (true);
		return fallback;
	}

}
