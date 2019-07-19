/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Comparator;

import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.NegativeCondition;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.ExtendedCSSStyleRule;

/**
 * CSS style rule.
 * 
 * @author Carlos Amengual
 * 
 */
public class StyleRule extends CSSStyleDeclarationRule implements ExtendedCSSStyleRule {

	public StyleRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.STYLE_RULE, origin);
	}

	/**
	 * Constructor used for stand-alone style rules.
	 * <p>
	 * Useful for testing.
	 * </p>
	 */
	public StyleRule() {
		super();
	}

	@Override
	public SelectorList getSelectorList() {
		return super.getSelectorList();
	}

	/**
	 * Returns the specificity of this rule.
	 * <p>
	 * See Cascading Style Sheets, level 2 revision 1 CSS 2.1 Specification,
	 * paragraph 6.4.3.
	 * 
	 * @return the specificity.
	 */
	RuleSpecifity getSpecifity(int index) {
		return new RuleSpecifity(getSelectorList().item(index));
	}

	/**
	 * The specificity of this rule.
	 * <p>
	 * See Cascading Style Sheets, level 2 revision 1 CSS 2.1 Specification,
	 * paragraph 6.4.3.
	 */
	static class Specifity {

		short id_count = 0;

		short attrib_classes_count = 0;

		short names_pseudoelements_count = 0;

		public Specifity(Selector selector) {
			super();
			specifity(selector);
		}

		private void specifity(Selector selector) {
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
				specifity(((DescendantSelector) selector).getSimpleSelector());
				specifity(((DescendantSelector) selector).getAncestorSelector());
				break;
			case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
				specifity(((SiblingSelector) selector).getSiblingSelector());
				specifity(((SiblingSelector) selector).getSelector());
				break;
			case Selector.SAC_ANY_NODE_SELECTOR:
				if (selector instanceof SiblingSelector) {
					// XXX Steadystate parser hack
					specifity(((SiblingSelector) selector).getSiblingSelector());
					specifity(((SiblingSelector) selector).getSelector());
				}
				break;
			}
		}

		private static void conditionSpecificity(Condition cond, SimpleSelector selector, Specifity sp) {
			switch (cond.getConditionType()) {
			case Condition.SAC_CLASS_CONDITION:
			case Condition.SAC_ATTRIBUTE_CONDITION:
			case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
			case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
			case Condition.SAC_PSEUDO_CLASS_CONDITION:
			case Condition.SAC_LANG_CONDITION:
			case Condition.SAC_ONLY_CHILD_CONDITION:
			case Condition.SAC_ONLY_TYPE_CONDITION:
			case Condition.SAC_POSITIONAL_CONDITION:
				sp.attrib_classes_count++;
				break;
			case Condition.SAC_ID_CONDITION:
				sp.id_count++;
				break;
			case Condition.SAC_NEGATIVE_CONDITION:
				conditionSpecificity(((NegativeCondition) cond).getCondition(), selector, sp);
				return;
			case Condition.SAC_AND_CONDITION:
				CombinatorCondition comb = (CombinatorCondition) cond;
				Specifity firstsp = new Specifity(selector);
				conditionSpecificity(comb.getFirstCondition(), selector, firstsp);
				Specifity secondsp = new Specifity(selector);
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
			}
			sp.specifity(selector);
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
			if (!(obj instanceof Specifity))
				return false;
			Specifity other = (Specifity) obj;
			if (attrib_classes_count != other.attrib_classes_count)
				return false;
			if (id_count != other.id_count)
				return false;
			if (names_pseudoelements_count != other.names_pseudoelements_count)
				return false;
			return true;
		}

		static int selectorCompare(Specifity o1, Specifity o2) {
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

	class RuleSpecifity extends Specifity {

		public RuleSpecifity(Selector selector) {
			super(selector);
		}

		@Override
		public int hashCode() {
			return 31 * super.hashCode() + getCSSStyleRule().getOrigin();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			if (getCSSStyleRule().getOrigin() != ((RuleSpecifity) obj).getCSSStyleRule().getOrigin())
				return false;
			return true;
		}

		/**
		 * Gets the style rule to which this specificity applies.
		 * 
		 * @return the style rule.
		 */
		public StyleRule getCSSStyleRule() {
			return StyleRule.this;
		}
	}

	/*
	 * This comparator ignores rule insertion index, so any users of this class
	 * have to deal with that elsewhere.
	 */
	static class SpecificityComparator implements Comparator<RuleSpecifity> {

		/*
		 * Compares the two arguments for order. <p> Returns a negative integer,
		 * zero, or a positive integer as the first argument is less than, equal
		 * to, or greater than the second.
		 */
		@Override
		public int compare(RuleSpecifity o1, RuleSpecifity o2) {
			return (o2.getCSSStyleRule().getOrigin() - o1.getCSSStyleRule().getOrigin()) * 131071
					+ Specifity.selectorCompare(o1, o2);
		}
	}

}
