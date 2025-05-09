/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

class MutableBoolean {

	private boolean b;

	public MutableBoolean() {
		super();
	}

	public void setValue(boolean b) {
		this.b = b;
	}

	public void setTrueValue() {
		this.b = true;
	}

	public boolean isTrue() {
		return b;
	}

}
