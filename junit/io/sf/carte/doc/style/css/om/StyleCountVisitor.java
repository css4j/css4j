/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.util.Visitor;

class StyleCountVisitor implements Visitor<CSSStyleRule> {

	private int count = 0;

	StyleCountVisitor() {
		super();
	}

	public int getCount() {
		return count;
	}

	@Override
	public void visit(CSSStyleRule rule) {
		count++;
	}

}
