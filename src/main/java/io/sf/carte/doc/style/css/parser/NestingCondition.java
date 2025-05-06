/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class NestingCondition extends AbstractCondition {

	private static final long serialVersionUID = 1L;

	NestingCondition() {
		super();
	}

	@Override
	Condition replace(SelectorList base) {
		NSACSelectorFactory factory = ((AbstractSelector) base.item(0)).getSelectorFactory();
		SelectorArgumentConditionImpl is = (SelectorArgumentConditionImpl) factory
				.createCondition(ConditionType.SELECTOR_ARGUMENT);
		is.arguments = base;
		is.setName("is");
		return is;
	}

	@Override
	public ConditionType getConditionType() {
		return ConditionType.NESTING;
	}

	@Override
	public String toString() {
		return "&";
	}

	@Override
	public NestingCondition clone() {
		return this;
	}

}
