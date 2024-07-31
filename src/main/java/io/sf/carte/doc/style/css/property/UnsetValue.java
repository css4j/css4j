/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import io.sf.carte.util.SimpleWriter;

/**
 * An <code>unset</code> value.
 * 
 */
public class UnsetValue extends KeywordValue {

	private static final long serialVersionUID = 1L;

	private static UnsetValue singleton = new UnsetValue();

	/**
	 * Must access instance through static method.
	 *
	 */
	protected UnsetValue() {
		super();
	}

	public static UnsetValue getValue() {
		return singleton;
	}

	@Override
	public Type getPrimitiveType() {
		return Type.UNSET;
	}

	@Override
	public String getCssText() {
		return "unset";
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("unset");
	}

	@Override
	public KeywordValue getCSSValue() {
		return singleton;
	}

}
