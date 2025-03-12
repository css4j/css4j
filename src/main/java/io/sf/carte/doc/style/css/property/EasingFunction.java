/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;

/**
 * Easing function.
 * 
 */
class EasingFunction extends FunctionValue {

	private static final long serialVersionUID = 1L;

	EasingFunction(Type type) {
		super(type);
	}

	protected EasingFunction(EasingFunction copied) {
		super(copied);
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		switch (syntax.getCategory()) {
		case easingFunction:
		case universal:
			return Match.TRUE;
		default:
			return Match.FALSE;
		}
	}

	@Override
	public EasingFunction clone() {
		return new EasingFunction(this);
	}

}
