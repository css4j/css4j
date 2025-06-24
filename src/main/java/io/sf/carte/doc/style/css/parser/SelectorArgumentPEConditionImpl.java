/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Condition;

class SelectorArgumentPEConditionImpl extends SelectorArgumentConditionImpl {

	private static final long serialVersionUID = 1L;

	SelectorArgumentPEConditionImpl() {
		super();
	}

	@Override
	public ConditionType getConditionType() {
		return Condition.ConditionType.SELECTOR_ARGUMENT_PSEUDO_ELEMENT;
	}

	@Override
	void serialize(StringBuilder buf) {
		buf.append(':');
		super.serialize(buf);
	}

}
