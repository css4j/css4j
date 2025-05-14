/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Objects;

import io.sf.carte.doc.style.css.BooleanCondition;

/**
 * A condition that never matches.
 */
class FalseConditionImpl extends BooleanConditionImpl {

	private static final long serialVersionUID = 1L;

	private final String condition;

	/**
	 * Constructs a new false condition.
	 * 
	 * @param condition the condition text.
	 */
	public FalseConditionImpl(String condition) {
		super();
		this.condition = condition;
	}

	@Override
	public Type getType() {
		return Type.OTHER;
	}

	@Override
	public void addCondition(BooleanCondition nestedCondition) {
	}

	@Override
	public BooleanCondition replaceLast(BooleanCondition newCondition) {
		return this;
	}

	@Override
	public void appendText(StringBuilder buf) {
		buf.append(condition);
	}

	@Override
	public int hashCode() {
		return Objects.hash(condition);
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
		FalseConditionImpl other = (FalseConditionImpl) obj;
		return Objects.equals(condition, other.condition);
	}

}
