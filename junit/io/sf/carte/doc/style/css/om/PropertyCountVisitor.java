/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.util.Visitor;

/**
 * A Visitor that counts properties.
 */
public class PropertyCountVisitor implements Visitor<CSSDeclarationRule> {

	private int count = 0;

	public PropertyCountVisitor() {
		super();
	}

	/**
	 * The number of properties counted.
	 * 
	 * @return the number of properties counted.
	 */
	public int getCount() {
		return count;
	}

	@Override
	public void visit(CSSDeclarationRule rule) {
		count += rule.getStyle().getLength();
	}

	/**
	 * Reset the counter.
	 */
	public void reset() {
		count = 0;
	}

}
