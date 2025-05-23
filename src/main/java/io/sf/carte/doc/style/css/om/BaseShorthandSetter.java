/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.property.ShorthandDatabase;

abstract class BaseShorthandSetter implements SubpropertySetter {

	final BaseCSSStyleDeclaration styleDeclaration;

	private final String shorthandName;

	private final ShorthandDatabase shorthandDb = ShorthandDatabase.getInstance();

	BaseShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super();
		this.styleDeclaration = style;
		this.shorthandName = shorthandName;
	}

	String getShorthandName() {
		return shorthandName;
	}

	final ShorthandDatabase getShorthandDatabase() {
		return shorthandDb;
	}

}
