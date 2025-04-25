/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class SelectorArgumentConditionImpl extends AbstractCondition implements ArgumentCondition {

	private static final long serialVersionUID = 1L;

	String name = null;
	SelectorList arguments = null;

	SelectorArgumentConditionImpl() {
		super();
	}

	@Override
	public ConditionType getConditionType() {
		return Condition.ConditionType.SELECTOR_ARGUMENT;
	}

	@Override
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	@Override
	public SelectorList getSelectors() {
		return arguments;
	}

	@Override
	Condition replace(SelectorList base) {
		SelectorArgumentConditionImpl clon = clone();
		if (arguments != null) {
			clon.arguments = ((SelectorListImpl) arguments).replaceNested(base);
		}
		return clon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(':').append(getName()).append('(');
		if (arguments != null) {
			buf.append(arguments.toString());
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public SelectorArgumentConditionImpl clone() {
		SelectorArgumentConditionImpl clon = (SelectorArgumentConditionImpl) super.clone();
		clon.name = name;
		clon.arguments = arguments;
		return clon;
	}

}
