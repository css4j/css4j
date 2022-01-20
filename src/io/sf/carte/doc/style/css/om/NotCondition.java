/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.parser.BooleanConditionHelper;

/**
 * NOT condition.
 * 
 */
class NotCondition extends BooleanConditionImpl {

	private static final long serialVersionUID = 1L;

	BooleanCondition nestedCondition;

	NotCondition() {
		super();
	}

	@Override
	public void addCondition(BooleanCondition nestedCondition) {
		nestedCondition.setParentCondition(this);
		this.nestedCondition = nestedCondition;
	}

	@Override
	public BooleanCondition replaceLast(BooleanCondition newCondition) {
		BooleanCondition last = this.nestedCondition;
		addCondition(newCondition);
		return last;
	}

	@Override
	public Type getType() {
		return Type.NOT;
	}

	@Override
	public BooleanCondition getNestedCondition() {
		return nestedCondition;
	}

	@Override
	public void appendText(StringBuilder buf) {
		BooleanConditionHelper.appendNOTText(this, buf);
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		BooleanConditionHelper.appendNOTMinifiedText(this, buf);
	}

	@Override
	public int hashCode() {
		return ((nestedCondition == null) ? 0 : nestedCondition.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NotCondition other = (NotCondition) obj;
		if (nestedCondition == null) {
			if (other.nestedCondition != null) {
				return false;
			}
		} else if (!nestedCondition.equals(other.nestedCondition)) {
			return false;
		}
		return true;
	}

}
