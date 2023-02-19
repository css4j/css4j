/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

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
	protected InitialValue() {
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

}
