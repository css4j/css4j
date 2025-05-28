/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.Arrays;

import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class CombinatorConditionImpl extends AbstractCondition implements CombinatorCondition {

	private static final long serialVersionUID = 2L;

	AbstractCondition[] conditions;

	CombinatorConditionImpl() {
		this(2);
	}

	CombinatorConditionImpl(int size) {
		super();
		conditions = new AbstractCondition[size];
	}

	@Override
	public ConditionType getConditionType() {
		return Condition.ConditionType.AND;
	}

	@Override
	public AbstractCondition getFirstCondition() {
		return conditions[0];
	}

	@Override
	public AbstractCondition getSecondCondition() {
		return conditions[1];
	}

	public AbstractCondition getLastCondition() {
		return conditions[conditions.length - 1];
	}

	@Override
	public AbstractCondition getCondition(int index) {
		return conditions[index];
	}

	void setCondition(int idx, AbstractCondition newcond) {
		conditions[idx] = newcond;
	}

	@Override
	public int getLength() {
		return conditions.length;
	}

	CombinatorConditionImpl prependCondition(AbstractCondition cond) {
		int curlen = conditions.length;
		if (cond.getConditionType() == ConditionType.AND) {
			CombinatorConditionImpl comb = (CombinatorConditionImpl) cond;
			int otherlen = comb.conditions.length;
			AbstractCondition[] conds = new AbstractCondition[curlen + otherlen];
			System.arraycopy(comb.conditions, 0, conds, 0, otherlen);
			System.arraycopy(conditions, 0, conds, otherlen, curlen);
		} else {
			AbstractCondition[] conds = new AbstractCondition[curlen + 1];
			System.arraycopy(conditions, 0, conds, 1, curlen);
			conds[0] = cond;
			conditions = conds;
		}
		return this;
	}

	@Override
	CombinatorConditionImpl appendCondition(AbstractCondition cond) {
		int idx = conditions.length;
		if (cond.getConditionType() == ConditionType.AND) {
			CombinatorConditionImpl comb = (CombinatorConditionImpl) cond;
			int otherlen = comb.conditions.length;
			AbstractCondition[] conds = new AbstractCondition[idx + otherlen];
			System.arraycopy(conditions, 0, conds, 0, idx);
			System.arraycopy(comb.conditions, 0, conds, idx, otherlen);
		} else {
			AbstractCondition[] conds = new AbstractCondition[idx + 1];
			System.arraycopy(conditions, 0, conds, 0, idx);
			conds[idx] = cond;
			conditions = conds;
		}
		return this;
	}

	AbstractCondition removeFirstCondition() {
		int curlenm1 = conditions.length - 1;
		if (curlenm1 == 1) {
			return conditions[1];
		}
		AbstractCondition[] conds = new AbstractCondition[curlenm1];
		System.arraycopy(conditions, 1, conds, 0, curlenm1);
		conditions = conds;
		return this;
	}

	@Override
	AbstractCondition replace(SelectorList base, MutableBoolean replaced) {
		CombinatorConditionImpl clon = clone();
		for (int i = 0; i < conditions.length; i++) {
			clon.conditions[i] = conditions[i].replace(base, replaced);
		}
		return clon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = Condition.ConditionType.AND.hashCode();
		result = prime * result + Arrays.hashCode(conditions);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CombinatorConditionImpl)) {
			return false;
		}
		CombinatorConditionImpl other = (CombinatorConditionImpl) obj;
		return Arrays.equals(conditions, other.conditions);
	}

	@Override
	void serialize(StringBuilder buf) {
		for (AbstractCondition cond : conditions) {
			if (cond != null) {
				cond.serialize(buf);
			}
		}
	}

	@Override
	public CombinatorConditionImpl clone() {
		CombinatorConditionImpl clon = (CombinatorConditionImpl) super.clone();
		clon.conditions = conditions.clone();
		return clon;
	}

}
