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
 * Abstract CSSTypedValue that prefers to have <code>cssText</code> as a field.
 * 
 */
abstract class AbstractTextValue extends TypedValue {

	private static final long serialVersionUID = 1L;

	private String cssText = null;

	AbstractTextValue(Type primitiveType) {
		super(primitiveType);
	}

	AbstractTextValue(AbstractTextValue copied) {
		super(copied);
		this.cssText = copied.cssText;
	}

	@Override
	public String getCssText() {
		return cssText;
	}

	void setPlainCssText(String cssText) {
		this.cssText = cssText;
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(getCssText());
	}

}
