/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.util.Visitor;

/**
 * A Visitor that counts style rules.
 */
public class StyleCountVisitor implements Visitor<CSSStyleRule> {

	private int count = 0;

	public StyleCountVisitor() {
		super();
	}

	/**
	 * The number of rules counted.
	 * 
	 * @return the number of rules counted.
	 */
	public int getCount() {
		return count;
	}

	@Override
	public void visit(CSSStyleRule rule) {
		count++;
	}

}
