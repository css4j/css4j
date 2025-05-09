/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class CombinatorConditionImpl extends AbstractCondition implements CombinatorCondition {

	private static final long serialVersionUID = 1L;

	Condition first = null;
	Condition second = null;

	CombinatorConditionImpl() {
		super();
	}

	@Override
	public ConditionType getConditionType() {
		return Condition.ConditionType.AND;
	}

	@Override
	public Condition getFirstCondition() {
		return first;
	}

	@Override
	public Condition getSecondCondition() {
		return second;
	}

	@Override
	Condition replace(SelectorList base, MutableBoolean replaced) {
		CombinatorConditionImpl clon = clone();
		if (first != null) {
			clon.first = ((AbstractCondition) clon.first).replace(base, replaced);
		}
		if (second != null) {
			clon.second = ((AbstractCondition) clon.second).replace(base, replaced);
		}
		return clon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
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
		CombinatorConditionImpl other = (CombinatorConditionImpl) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(getFirstCondition().toString());
		if (second != null) {
			buf.append(second.toString());
		} else {
			buf.append('?');
		}
		return buf.toString();
	}

	@Override
	public CombinatorConditionImpl clone() {
		CombinatorConditionImpl clon = (CombinatorConditionImpl) super.clone();
		clon.first = first;
		clon.second = second;
		return clon;
	}

}
