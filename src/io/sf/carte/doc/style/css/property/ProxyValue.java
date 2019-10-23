/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

/**
 * A PROXY value, like <code>attr()</code>.
 * 
 */
abstract public class ProxyValue extends PrimitiveValue {

	private boolean expectInteger = false;

	protected ProxyValue(Type unitType) {
		super(unitType);
	}

	protected ProxyValue(ProxyValue copied) {
		super(copied);
	}

	@Override
	public CssType getCssValueType() {
		return CssType.PROXY;
	}

	public boolean isExpectingInteger() {
		return expectInteger;
	}

	@Override
	public void setExpectInteger() {
		expectInteger = true;
	}

}
