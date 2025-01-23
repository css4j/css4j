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
 * A PROXY value, like <code>attr()</code>.
 * 
 */
abstract public class ProxyValue extends PrimitiveValue {

	private static final long serialVersionUID = 1L;

	private boolean expectInteger = false;

	protected ProxyValue(Type unitType) {
		super(unitType);
	}

	protected ProxyValue(ProxyValue copied) {
		super(copied);
		this.expectInteger = copied.expectInteger;
	}

	@Override
	public CssType getCssValueType() {
		return CssType.PROXY;
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		return Match.PENDING;
	}

	public boolean isExpectingInteger() {
		return expectInteger;
	}

	@Override
	public void setExpectInteger() {
		expectInteger = true;
	}

}
