/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.PositionalCondition;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

/**
 * The specificity of this rule.
 * <p>
 * See Cascading Style Sheets, level 2 revision 1 CSS 2.1 Specification,
 * paragraph 6.4.3.
 */
public class Specificity implements java.io.Serializable {

	private static final long serialVersionUID = 2L;

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

	private void clear() {
		id_count = 0;
		attrib_classes_count = 0;
		names_pseudoelements_count = 0;
	}

	private void specificity(Selector selector) {
		switch (selector.getSelectorType()) {
		case ELEMENT:
			String elname = ((ElementSelector) selector).getLocalName();
			if (elname == null || elname.equals("*")) {
				// "ignore the universal selector"
				break;
			}
			names_pseudoelements_count++;
			break;
		case CONDITIONAL:
			ConditionalSelector condsel = (ConditionalSelector) selector;
			conditionSpecificity(condsel.getCondition(), condsel.getSimpleSelector(), this);
			break;
		case DESCENDANT:
		case CHILD:
		case DIRECT_ADJACENT:
		case SUBSEQUENT_SIBLING:
			specificity(((CombinatorSelector) selector).getSecondSelector());
			specificity(((CombinatorSelector) selector).getSelector());
			break;
		default:
		}
	}

	private void conditionSpecificity(Condition cond, SimpleSelector selector, Specificity sp) {
		switch (cond.getConditionType()) {
		case POSITIONAL:
			PositionalCondition pcond = (PositionalCondition) cond;
			SelectorList ofList = pcond.getOfList();
			if (ofList != null) {
				int selIdx = selectorMatcher.matches(ofList);
				if (selIdx == -1) {
					return;
				}
				Specificity argSpecificity = new Specificity(ofList.item(selIdx), selectorMatcher);
				add(argSpecificity);
			}
		case CLASS:
		case ATTRIBUTE:
		case ONE_OF_ATTRIBUTE:
		case BEGIN_HYPHEN_ATTRIBUTE:
		case BEGINS_ATTRIBUTE:
		case ENDS_ATTRIBUTE:
		case SUBSTRING_ATTRIBUTE:
		case PSEUDO_CLASS:
		case LANG:
		case ONLY_CHILD:
		case ONLY_TYPE:
			sp.attrib_classes_count++;
			break;
		case PSEUDO_ELEMENT:
			sp.names_pseudoelements_count++;
			break;
		case ID:
			sp.id_count++;
			break;
		case AND:
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
		case SELECTOR_ARGUMENT:
			ArgumentCondition acond = (ArgumentCondition) cond;
			String name = acond.getName();
			if ("where".equalsIgnoreCase(name)) {
				// "where" does not contribute to specificity
				break;
			}
			SelectorList argList = acond.getSelectors();
			int selIdx = selectorMatcher.matches(argList);
			if (selIdx == -1) {
				clear();
				return;
			}
			Specificity argSpecificity = new Specificity(argList.item(selIdx), selectorMatcher);
			add(argSpecificity);
			break;
		default:
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

	public static int selectorCompare(Specificity o1, Specificity o2) {
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
