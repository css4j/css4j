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
 * A <code>revert</code> value.
 * 
 */
public class RevertValue extends KeywordValue {

	private static final long serialVersionUID = 1L;

	private static RevertValue singleton = new RevertValue();

	/**
	 * Must access instance through static method.
	 *
	 */
	private RevertValue() {
		super();
	}

	public static RevertValue getValue() {
		return singleton;
	}

	@Override
	public Type getPrimitiveType() {
		return Type.REVERT;
	}

	@Override
	public String getCssText() {
		return "revert";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("revert");
	}

	@Override
	public KeywordValue getCSSValue() {
		return singleton;
	}

}
