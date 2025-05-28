/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

abstract class AbstractCondition implements Condition, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	AbstractCondition() {
		super();
	}

	AbstractCondition replace(SelectorList base, MutableBoolean replaced) {
		return this;
	}

	CombinatorConditionImpl appendCondition(AbstractCondition cond) {
		CombinatorConditionImpl comb;
		if (cond.getConditionType() == ConditionType.AND) {
			comb = (CombinatorConditionImpl) cond;
			comb.prependCondition(this);
		} else {
			comb = new CombinatorConditionImpl();
			comb.conditions[0] = this;
			comb.conditions[1] = cond;
		}
		return comb;
	}

	/**
	 * Perform a shallow cloning of this value.
	 * 
	 * @return the clone;
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		serialize(buf);
		return buf.toString();
	}

	/**
	 * Serialize this condition to a buffer.
	 * <p>
	 * Only this condition, not the next one(s).
	 * </p>
	 * 
	 * @param buf the buffer.
	 */
	abstract void serialize(StringBuilder buf);

}
