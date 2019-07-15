/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.ExtendedCSSRuleList;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.SimpleWriter;

/**
 * Stores a list of CSS rules, implementing CSSRuleList.
 * 
 * @author Carlos Amengual
 * 
 */
public class CSSRuleArrayList extends ArrayList<AbstractCSSRule>
		implements ExtendedCSSRuleList<AbstractCSSRule>, RuleStore {

	private static final long serialVersionUID = 2L;

	public CSSRuleArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public CSSRuleArrayList() {
		super();
	}

	public CSSRuleArrayList(Collection<? extends AbstractCSSRule> c) {
		super(c);
	}

	/**
	 * Gives the number of <code>CSSRules</code> in the list.
	 * 
	 * @return the number of <code>CSSRules</code> in the list. The range of
	 *         valid child rule indices is <code>0</code> to
	 *         <code>length-1</code> inclusive.
	 */
	@Override
	public int getLength() {
		return size();
	}

	/**
	 * Used to retrieve a CSS rule by ordinal index. The order in this
	 * collection represents the order of the rules in the CSS style sheet. If
	 * index is greater than or equal to the number of rules in the list, this
	 * returns <code>null</code>.
	 * 
	 * @param index
	 *            Index into the collection.
	 * @return The style rule at the <code>index</code> position in the
	 *         <code>CSSRuleList</code>, or <code>null</code> if that is not a
	 *         valid index.
	 */
	@Override
	public AbstractCSSRule item(int index) {
		if (index >= getLength() || index < 0) {
			return null;
		}
		return get(index);
	}

	/**
	 * Insert the given CSS rule at the given index.
	 * 
	 * @param cssrule
	 *            the rule.
	 * @param index
	 *            the index at which to insert the rule.
	 * @return the index at which the rule was finally inserted.
	 */
	@Override
	public int insertRule(CSSRule cssrule, int index) {
		if (index > size()) {
			index = size();
		} else if (index < 0) {
			index = 0;
		}
		add(index, (AbstractCSSRule) cssrule);
		return index;
	}

	/**
	 * Appends the rule to the end of the list
	 * 
	 * @param rule
	 *            the rule to append.
	 * @return <code>true</code> if the rule was appended successfully, <code>false</code> otherwise.
	 */
	public boolean append(AbstractCSSRule rule) {
		return super.add(rule);
	}

	@Override
	public String toMinifiedString() {
		int sz = size();
		StringBuilder sb = new StringBuilder(sz * 20);
		for (int i = 0; i < sz; i++) {
			sb.append(item(i).getMinifiedCssText());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		int sz = size();
		StringBuilder sb = new StringBuilder(sz * 20);
		for (int i = 0; i < sz; i++) {
			AbstractCSSRule rule = item(i);
			List<String> comments = rule.getPrecedingComments();
			if (comments != null) {
				int nc = comments.size();
				for (int j = 0; j < nc; j++) {
					sb.append("/*").append(comments.get(j)).append("*/\n");
				}
			}
			sb.append(rule.getCssText()).append('\n');
		}
		return sb.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		int sz = size();
		for (int i = 0; i < sz; i++) {
			AbstractCSSRule rule = item(i);
			List<String> comments = rule.getPrecedingComments();
			if (comments != null) {
				int nc = comments.size();
				for (int j = 0; j < nc; j++) {
					context.writeComment(wri, comments.get(j));
				}
			}
			rule.writeCssText(wri, context);
		}
	}

}
