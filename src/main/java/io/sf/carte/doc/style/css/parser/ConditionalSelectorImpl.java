/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

class ConditionalSelectorImpl extends AbstractSelector implements ConditionalSelector {

	private static final long serialVersionUID = 1L;

	SimpleSelector selector;
	Condition condition;

	ConditionalSelectorImpl(SimpleSelector selector, Condition condition) {
		super();
		this.selector = selector;
		this.condition = condition;
	}

	@Override
	public SelectorType getSelectorType() {
		return SelectorType.CONDITIONAL;
	}

	@Override
	public SimpleSelector getSimpleSelector() {
		return this.selector;
	}

	@Override
	public Condition getCondition() {
		return this.condition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((selector == null) ? 0 : selector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConditionalSelectorImpl other = (ConditionalSelectorImpl) obj;
		if (condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!condition.equals(other.condition)) {
			return false;
		}
		if (selector == null) {
			if (other.selector != null) {
				return false;
			}
		} else if (!selector.equals(other.selector)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		SelectorType simpletype = selector.getSelectorType();
		if (simpletype != Selector.SelectorType.UNIVERSAL
				|| ((ElementSelector) selector).getNamespaceURI() != null) {
			buf.append(selector.toString());
		}
		buf.append(condition.toString());
		return buf.toString();
	}
}
