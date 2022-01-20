/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.NegativeCondition;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;

import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.Condition2;
import io.sf.carte.doc.style.css.nsac.PositionalCondition2;
import io.sf.carte.doc.style.css.nsac.Selector2;

/**
 * The specificity of this rule.
 * <p>
 * See Cascading Style Sheets, level 2 revision 1 CSS 2.1 Specification,
 * paragraph 6.4.3.
 */
class Specificity {

	short id_count = 0;

	short attrib_classes_count = 0;

	short names_pseudoelements_count = 0;

	private final SelectorMatcher selectorMatcher;

	public Specificity(Selector selector, SelectorMatcher matcher) {
		super();
		this.selectorMatcher = matcher;
		specificity(selector);
	}

	private void add(Specificity specificity) {
		id_count += specificity.id_count;
		attrib_classes_count += specificity.attrib_classes_count;
		names_pseudoelements_count += specificity.names_pseudoelements_count;
	}

	private void specificity(Selector selector) {
		switch (selector.getSelectorType()) {
		case Selector.SAC_ELEMENT_NODE_SELECTOR:
			String elname = ((ElementSelector) selector).getLocalName();
			if (elname == null || elname.equals("*")) {
				// "ignore the universal selector"
				break;
			}
		case Selector.SAC_PSEUDO_ELEMENT_SELECTOR:
		case Selector.SAC_ROOT_NODE_SELECTOR:
			names_pseudoelements_count++;
			break;
		case Selector.SAC_CONDITIONAL_SELECTOR:
			ConditionalSelector condsel = (ConditionalSelector) selector;
			conditionSpecificity(condsel.getCondition(), condsel.getSimpleSelector(), this);
			break;
		case Selector.SAC_DESCENDANT_SELECTOR:
		case Selector.SAC_CHILD_SELECTOR:
			specificity(((DescendantSelector) selector).getSimpleSelector());
			specificity(((DescendantSelector) selector).getAncestorSelector());
			break;
		case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
		case Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR:
			specificity(((SiblingSelector) selector).getSiblingSelector());
			specificity(((SiblingSelector) selector).getSelector());
			break;
		case Selector.SAC_ANY_NODE_SELECTOR:
			if (selector instanceof SiblingSelector) {
				// XXX Steadystate parser hack
				specificity(((SiblingSelector) selector).getSiblingSelector());
				specificity(((SiblingSelector) selector).getSelector());
			}
			break;
		}
	}

	private void conditionSpecificity(Condition cond, SimpleSelector selector, Specificity sp) {
		switch (cond.getConditionType()) {
		case Condition.SAC_POSITIONAL_CONDITION:
			PositionalCondition2 pcond = (PositionalCondition2) cond;
			org.w3c.css.sac.SelectorList ofList = pcond.getOfList();
			if (ofList != null) {
				int selIdx = selectorMatcher.matches(ofList);
				if (selIdx == -1) {
					return;
				}
				Specificity argSpecificity = new Specificity(ofList.item(selIdx), selectorMatcher);
				add(argSpecificity);
			}
		case Condition.SAC_CLASS_CONDITION:
		case Condition.SAC_ATTRIBUTE_CONDITION:
		case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
		case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
		case Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION:
		case Condition2.SAC_ENDS_ATTRIBUTE_CONDITION:
		case Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
		case Condition.SAC_PSEUDO_CLASS_CONDITION:
		case Condition.SAC_LANG_CONDITION:
		case Condition.SAC_ONLY_CHILD_CONDITION:
		case Condition.SAC_ONLY_TYPE_CONDITION:
			sp.attrib_classes_count++;
			break;
		case Condition2.SAC_PSEUDO_ELEMENT_CONDITION:
			sp.names_pseudoelements_count++;
		break;
		case Condition.SAC_ID_CONDITION:
			sp.id_count++;
			break;
		case Condition.SAC_NEGATIVE_CONDITION:
			conditionSpecificity(((NegativeCondition) cond).getCondition(), selector, sp);
			return;
		case Condition.SAC_AND_CONDITION:
			CombinatorCondition comb = (CombinatorCondition) cond;
			Specificity firstsp = new Specificity(selector, selectorMatcher);
			conditionSpecificity(comb.getFirstCondition(), selector, firstsp);
			Specificity secondsp = new Specificity(selector, selectorMatcher);
			conditionSpecificity(comb.getSecondCondition(), selector, secondsp);
			if (firstsp.id_count > secondsp.id_count) {
				sp.id_count += firstsp.id_count;
				sp.attrib_classes_count += firstsp.attrib_classes_count;
				sp.names_pseudoelements_count += firstsp.names_pseudoelements_count;
			} else if (firstsp.id_count < secondsp.id_count) {
				sp.id_count += secondsp.id_count;
				sp.attrib_classes_count += secondsp.attrib_classes_count;
				sp.names_pseudoelements_count += secondsp.names_pseudoelements_count;
			} else {
				if (firstsp.attrib_classes_count > secondsp.attrib_classes_count) {
					sp.attrib_classes_count += firstsp.attrib_classes_count;
					sp.names_pseudoelements_count += firstsp.names_pseudoelements_count;
				} else if (firstsp.attrib_classes_count < secondsp.attrib_classes_count) {
					sp.attrib_classes_count += secondsp.attrib_classes_count;
					sp.names_pseudoelements_count += secondsp.names_pseudoelements_count;
				} else {
					if (firstsp.names_pseudoelements_count > secondsp.names_pseudoelements_count) {
						sp.names_pseudoelements_count += firstsp.names_pseudoelements_count;
					} else if (firstsp.names_pseudoelements_count < secondsp.names_pseudoelements_count) {
						sp.names_pseudoelements_count += secondsp.names_pseudoelements_count;
					}
				}
			}
			return;
		case Condition2.SAC_SELECTOR_ARGUMENT_CONDITION:
			ArgumentCondition acond = (ArgumentCondition) cond;
			String name = acond.getName();
			if ("where".equalsIgnoreCase(name)) {
				// "where" does not contribute to specificity
				break;
			}
			org.w3c.css.sac.SelectorList argList = acond.getSelectors();
			int selIdx = selectorMatcher.matches(argList);
			if (selIdx == -1) {
				return;
			}
			Specificity argSpecificity = new Specificity(argList.item(selIdx), selectorMatcher);
			add(argSpecificity);
			break;
		}
		sp.specificity(selector);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attrib_classes_count;
		result = prime * result + id_count;
		result = prime * result + names_pseudoelements_count;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Specificity))
			return false;
		Specificity other = (Specificity) obj;
		if (attrib_classes_count != other.attrib_classes_count)
			return false;
		if (id_count != other.id_count)
			return false;
		if (names_pseudoelements_count != other.names_pseudoelements_count)
			return false;
		return true;
	}

	static int selectorCompare(Specificity o1, Specificity o2) {
		return (o1.id_count - o2.id_count) * 16384 + (o1.attrib_classes_count - o2.attrib_classes_count) * 128
				+ (o1.names_pseudoelements_count - o2.names_pseudoelements_count);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(82);
		buf.append("id: ").append(id_count).append(", attributes and pseudo-classes: ").append(attrib_classes_count)
				.append(", element names and pseudo-elements: ").append(names_pseudoelements_count);
		return buf.toString();
	}
}
