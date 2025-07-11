/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Condition.ConditionType;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

abstract class ConditionalSelectorImpl extends AbstractSelector implements ConditionalSelector {

	private static final long serialVersionUID = 1L;

	SimpleSelector selector;
	AbstractCondition condition;

	ConditionalSelectorImpl(SimpleSelector selector, AbstractCondition condition) {
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
	public AbstractCondition getCondition() {
		return this.condition;
	}

	@Override
	ConditionalSelectorImpl withCondition(NSACSelectorFactory factory, AbstractCondition cond) {
		SimpleSelector simple;
		if (selector == null) {
			simple = NSACSelectorFactory.getUniversalSelector();
		} else {
			simple = selector;
		}
		CombinatorConditionImpl newCond = this.condition.appendCondition(cond);
		return factory.createConditionalSelector(simple, newCond);
	}

	@Override
	Selector replace(SelectorList base, MutableBoolean replaced) {
		AbstractCondition replCond;

		CombinatorConditionImpl comb;
		Selector base0;

		if (condition.getConditionType() == ConditionType.NESTING) {
			replaced.setTrueValue();
			if (base.getLength() == 1) {
				if (selector == null || selector.getSelectorType() == SelectorType.UNIVERSAL) {
					return base.item(0);
				}
				base0 = base.item(0);
				if (base0.getSelectorType() == SelectorType.CONDITIONAL
						&& ((ConditionalSelector) base0).getSimpleSelector()
								.getSelectorType() == SelectorType.UNIVERSAL) {
					ConditionalSelectorImpl bclon = ((ConditionalSelectorImpl) base0).clone();
					bclon.selector = selector;
					return bclon;
				}
			}
			SelectorArgumentConditionImpl is = new SelectorArgumentConditionImpl();
			is.arguments = base;
			is.setName("is");
			replCond = is;
		} else if (condition.getConditionType() == ConditionType.AND && base.getLength() == 1
				&& (selector == null || selector.getSelectorType() == SelectorType.UNIVERSAL)
				&& ((AbstractSelector) (base0 = base.item(0))).isSimpleSelector()) {
			comb = (CombinatorConditionImpl) condition;
			int len = comb.getLength();
			CombinatorConditionImpl newCombCond = new CombinatorConditionImpl(len);
			SimpleSelector simple;

			// Check whether base0 is just a condition
			if (base0.getSelectorType() == SelectorType.CONDITIONAL
					&& ((simple = ((ConditionalSelector) base0).getSimpleSelector()) == null
							|| simple.getSelectorType() == SelectorType.UNIVERSAL)) {
				AbstractCondition basecond = ((ConditionalSelectorImpl) base0).getCondition();
				for (int i = 1; i < len; i++) {
					AbstractCondition condItem = comb.getCondition(i);
					if (condItem.getConditionType() == ConditionType.NESTING) {
						newCombCond.setCondition(i, basecond);
						replaced.setTrueValue();
					} else {
						condItem = condItem.replace(base, replaced);
						newCombCond.setCondition(i, condItem);
					}
				}
			} else {
				for (int i = 1; i < len; i++) {
					AbstractCondition condItem = comb.getCondition(i);
					condItem = condItem.replace(base, replaced);
					newCombCond.setCondition(i, condItem);
				}
			}

			AbstractCondition cond1 = comb.getFirstCondition();
			if (cond1.getConditionType() == ConditionType.NESTING) {
				AbstractCondition newcond = newCombCond.removeFirstCondition();
				// base0 is a SimpleSelector, so we can do this
				ConditionalSelectorImpl newsel = ((AbstractSelector) base0)
						.withCondition(getSelectorFactory(), newcond);

				replaced.setTrueValue();
				return newsel;
			} else {
				cond1 = cond1.replace(base, replaced);
				newCombCond.setCondition(0, cond1);
			}

			ConditionalSelectorImpl newsel = getSelectorFactory().createConditionalSelector(
					NSACSelectorFactory.getUniversalSelector(), newCombCond);
			return newsel;
		} else {
			replCond = condition.replace(base, replaced);
		}

		ConditionalSelectorImpl clon = clone();

		clon.selector = (SimpleSelector) ((AbstractSelector) selector).replace(base, replaced);
		clon.condition = replCond;

		return clon;
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
			if (selector == null || selector.getSelectorType() != SelectorType.CONDITIONAL) {
				return false;
			}

			// Possible different decomposition of same selector
			if ((selector != null && selector.getSelectorType() == SelectorType.CONDITIONAL)
					|| other.selector != null
							&& other.selector.getSelectorType() == SelectorType.CONDITIONAL) {
				// check serializations
				return toString().equals(other.toString());
			}

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

	@Override
	public ConditionalSelectorImpl clone() {
		ConditionalSelectorImpl clon = (ConditionalSelectorImpl) super.clone();
		clon.condition = condition;
		clon.selector = selector;
		return clon;
	}

}
