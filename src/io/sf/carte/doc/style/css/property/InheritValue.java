/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.util.SimpleWriter;

/**
 * An <code>inherit</code> value.
 * 
 */
public class InheritValue extends KeywordValue {

	private static final long serialVersionUID = 1L;

	private static InheritValue singleton = new InheritValue();

	/**
	 * Must access instance through static method.
	 *
	 */
	protected InheritValue() {
		super();
	}

	public static InheritValue getValue() {
		return singleton;
	}

	@Override
	public Type getPrimitiveType() {
		return Type.INHERIT;
	}

	@Override
	public String getCssText() {
		return "inherit";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("inherit");
	}

	@Override
	public KeywordValue getCSSValue() {
		return singleton;
	}

}
