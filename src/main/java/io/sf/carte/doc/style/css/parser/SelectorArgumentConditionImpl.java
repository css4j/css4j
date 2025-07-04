/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class SelectorArgumentConditionImpl extends AbstractNamedCondition implements ArgumentCondition {

	private static final long serialVersionUID = 1L;

	SelectorList arguments = null;

	SelectorArgumentConditionImpl() {
		super();
	}

	@Override
	public ConditionType getConditionType() {
		return Condition.ConditionType.SELECTOR_ARGUMENT;
	}

	@Override
	public SelectorList getSelectors() {
		return arguments;
	}

	@Override
	AbstractCondition replace(SelectorList base, MutableBoolean replaced) {
		if (arguments != null) {
			SelectorListImpl replArgs = ((SelectorListImpl) arguments).replaceNestedArgument(base,
					replaced);
			if (replaced.isTrue()) {
				SelectorArgumentConditionImpl clon = clone();
				clon.arguments = replArgs;
				return clon;
			}
		}
		return super.replace(base, replaced);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getConditionType().ordinal();
		result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SelectorArgumentConditionImpl other = (SelectorArgumentConditionImpl) obj;
		if (arguments == null) {
			if (other.arguments != null) {
				return false;
			}
		} else if (!ParseHelper.equalSelectorList(arguments, other.arguments)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	void serialize(StringBuilder buf) {
		buf.append(':').append(getName()).append('(');
		if (arguments != null) {
			buf.append(arguments.toString());
		}
		buf.append(')');
	}

	@Override
	public SelectorArgumentConditionImpl clone() {
		SelectorArgumentConditionImpl clon = (SelectorArgumentConditionImpl) super.clone();
		clon.arguments = arguments;
		return clon;
	}

}
