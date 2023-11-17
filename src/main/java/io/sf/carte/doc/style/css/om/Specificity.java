/*

 Copyright (c) 2005-2023, Carlos Amengual.

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
 * The specificity of a selector (in the context given by a
 * {@code SelectorMatcher}).
 * <p>
 * See Cascading Style Sheets, level 2 revision 1 CSS 2.1 Specification, § 6.4.3
 * and also Selectors Level 4 § 16,
 * <a href="https://www.w3.org/TR/selectors-4/#specificity-rules">Calculating a
 * selector’s specificity</a>.
 */
public class Specificity implements java.io.Serializable {

	private static final long serialVersionUID = 2L;

	short id_count = 0;

	short attrib_classes_count = 0;

	short names_pseudoelements_count = 0;

	private final SelectorMatcher selectorMatcher;

	/**
	 * Construct a specificity for a selector and a {@code SelectorMatcher} context.
	 * 
	 * @param selector the selector.
	 * @param matcher  the selector matcher.
	 */
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
			SimpleSelector simple = condsel.getSimpleSelector();
			specificity(simple);
			conditionSpecificity(condsel.getCondition());
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

	private void conditionSpecificity(Condition cond) {
		switch (cond.getConditionType()) {
		case POSITIONAL:
			PositionalCondition pcond = (PositionalCondition) cond;
			SelectorList ofList = pcond.getOfList();
			if (ofList != null) {
				mostSpecific(ofList);
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
			attrib_classes_count++;
			break;
		case PSEUDO_ELEMENT:
			names_pseudoelements_count++;
			break;
		case ID:
			id_count++;
			break;
		case AND:
			CombinatorCondition comb = (CombinatorCondition) cond;
			conditionSpecificity(comb.getFirstCondition());
			conditionSpecificity(comb.getSecondCondition());
			break;
		case SELECTOR_ARGUMENT:
			ArgumentCondition acond = (ArgumentCondition) cond;
			String name = acond.getName();
			if ("where".equalsIgnoreCase(name)) {
				// "where" does not contribute to specificity
				break;
			}
			SelectorList argList = acond.getSelectors();
			// Assume it is not(), is() or has()
			// Just compute the most specific in the list
			mostSpecific(argList);
			break;
		default:
		}
	}

	/**
	 * Add the specificity of the most specific selector in the list.
	 * 
	 * @param selectorList the selector list.
	 */
	private void mostSpecific(SelectorList selectorList) {
		// Find the most specific selector
		int sz = selectorList.getLength();
		Selector selector = selectorList.item(0);
		Specificity spMost = new Specificity(selector, selectorMatcher);
		for (int i = 1; i < sz; i++) {
			Selector sel = selectorList.item(i);
			Specificity sp = new Specificity(sel, selectorMatcher);
			if (selectorCompare(spMost, sp) < 0) {
				spMost = sp;
			}
		}
		add(spMost);
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

	/**
	 * Compare two specificities according to its selectors.
	 * 
	 * @param s1 the first specificity being compared.
	 * @param s2 the other specificity (to compare against the first).
	 * @return a negative integer, zero, or a positive integer as the first argument
	 *         is less than, equal to, or greater than the second.
	 */
	public static int selectorCompare(Specificity s1, Specificity s2) {
		return (s1.id_count - s2.id_count) * 16384 + (s1.attrib_classes_count - s2.attrib_classes_count) * 128
				+ (s1.names_pseudoelements_count - s2.names_pseudoelements_count);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(82);
		buf.append("id: ").append(id_count).append(", attributes and pseudo-classes: ").append(attrib_classes_count)
				.append(", element names and pseudo-elements: ").append(names_pseudoelements_count);
		return buf.toString();
	}

}
