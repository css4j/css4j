/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Comparator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

/**
 * CSS style rule.
 * 
 * @author Carlos Amengual
 * 
 */
public class StyleRule extends CSSStyleDeclarationRule implements CSSStyleRule {

	public StyleRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.STYLE_RULE, origin);
	}

	@Override
	public void setSelectorText(String selectorText) throws DOMException {
		Reader re = new StringReader(selectorText);
		Parser parser = createSACParser();
		SelectorList selist;
		try {
			selist = parser.parseSelectors(re);
		} catch (CSSException | IOException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		super.setSelectorList(selist);
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
				specifity(((CombinatorSelector) selector).getSecondSelector());
				specifity(((CombinatorSelector) selector).getSelector());
				break;
			default:
			}
		}

		private static void conditionSpecificity(Condition cond, SimpleSelector selector, Specifity sp) {
			switch (cond.getConditionType()) {
			case CLASS:
			case ATTRIBUTE:
			case ONE_OF_ATTRIBUTE:
			case BEGIN_HYPHEN_ATTRIBUTE:
			case PSEUDO_CLASS:
			case LANG:
			case ONLY_CHILD:
			case ONLY_TYPE:
			case POSITIONAL:
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
			default:
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
