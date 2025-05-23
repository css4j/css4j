/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.parser.BooleanConditionHelper;

/**
 * AND condition.
 * 
 */
class AndCondition extends BooleanConditionImpl.GroupCondition {

	private static final long serialVersionUID = 1L;

	AndCondition() {
		super();
	}

	@Override
	public Type getType() {
		return Type.AND;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + 13;
	}

	@Override
	public void appendText(StringBuilder buf) {
		BooleanConditionHelper.appendANDText(this, buf);
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		BooleanConditionHelper.appendANDMinifiedText(this, buf);
	}

}
