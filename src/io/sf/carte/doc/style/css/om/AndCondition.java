/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;

import io.sf.carte.doc.style.css.parser.BooleanCondition;

/**
 * AND condition.
 * 
 */
class AndCondition extends BooleanConditionImpl.AndCondition {

	AndCondition() {
		super();
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		boolean hasparent = getParentCondition() != null;
		if (hasparent) {
			buf.append('(');
		}
		Iterator<BooleanCondition> it = nestedConditions.iterator();
		it.next().appendMinifiedText(buf);
		while (it.hasNext()) {
			buf.append(" and ");
			it.next().appendMinifiedText(buf);
		}
		if (hasparent) {
			buf.append(')');
		}
	}

	@Override
	public void appendText(StringBuilder buf) {
		boolean hasparent = getParentCondition() != null;
		if (hasparent) {
			buf.append('(');
		}
		Iterator<BooleanCondition> it = nestedConditions.iterator();
		it.next().appendText(buf);
		while (it.hasNext()) {
			buf.append(" and ");
			it.next().appendText(buf);
		}
		if (hasparent) {
			buf.append(')');
		}
	}

}
