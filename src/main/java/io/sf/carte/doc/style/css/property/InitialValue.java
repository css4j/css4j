/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.util.SimpleWriter;

/**
 * An <code>initial</code> value.
 * 
 */
public class InitialValue extends KeywordValue {

	private static final long serialVersionUID = 1L;

	private static InitialValue singleton = new InitialValue();

	/**
	 * Must access instance through static method.
	 *
	 */
	private InitialValue() {
		super();
	}

	public static InitialValue getValue() {
		return singleton;
	}

	@Override
	public Type getPrimitiveType() {
		return Type.INITIAL;
	}

	@Override
	public String getCssText() {
		return "initial";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("initial");
	}

	@Override
	public KeywordValue getCSSValue() {
		return singleton;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj)
				|| (obj instanceof StyleValue && ((StyleValue) obj).isSystemDefault());
	}

}
