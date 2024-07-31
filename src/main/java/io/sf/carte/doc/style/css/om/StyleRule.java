/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Comparator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Parser.NamespaceMap;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * CSS style rule.
 * 
 * @author Carlos Amengual
 * 
 */
public class StyleRule extends CSSStyleDeclarationRule implements CSSStyleRule {

	private static final long serialVersionUID = 1L;

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
	public void setSelectorText(String selectorText) throws DOMException {
		Parser parser = createSACParser();
		SelectorList selist;
		try {
			selist = parser.parseSelectors(selectorText, new SheetNamespaceMap());
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		super.setSelectorList(selist);
	}

	private class SheetNamespaceMap implements NamespaceMap {

		@Override
		public String getNamespaceURI(String nsPrefix) {
			return ((BaseCSSStyleSheet) getParentStyleSheet()).getNamespaceURI(nsPrefix);
		}

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
	 * </p>
	 * 
	 * @param index the index of the selector in the selector list.
	 * @param matcher the selector matcher to apply in specificity computations.
	 * @return the specificity.
	 */
	RuleSpecificity getSpecificity(int index, SelectorMatcher matcher) {
		return new RuleSpecificity(getSelectorList().item(index), matcher);
	}

	class RuleSpecificity extends Specificity {

		private static final long serialVersionUID = 1L;

		public RuleSpecificity(Selector selector, SelectorMatcher matcher) {
			super(selector, matcher);
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
			if (getCSSStyleRule().getOrigin() != ((RuleSpecificity) obj).getCSSStyleRule().getOrigin())
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
	static class SpecificityComparator implements Comparator<RuleSpecificity> {

		/*
		 * Compares the two arguments for order. <p> Returns a negative integer,
		 * zero, or a positive integer as the first argument is less than, equal
		 * to, or greater than the second.
		 */
		@Override
		public int compare(RuleSpecificity o1, RuleSpecificity o2) {
			return (o2.getCSSStyleRule().getOrigin() - o1.getCSSStyleRule().getOrigin()) * 131071
					+ Specificity.selectorCompare(o1, o2);
		}
	}

}
