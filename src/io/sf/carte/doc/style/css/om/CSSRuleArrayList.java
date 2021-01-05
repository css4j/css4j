/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Collection;

import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Stores a list of CSS rules, implementing CSSRuleList.
 * 
 * @author Carlos Amengual
 * 
 */
public class CSSRuleArrayList extends AbstractRuleList<AbstractCSSRule>
		implements RuleStore {

	private static final long serialVersionUID = 2L;

	/**
	 * Constructs an empty rule list with the specified initial capacity.
	 * 
	 * @param initialCapacity the initial capacity.
	 * @throws IllegalArgumentException if the specified initial capacity is
	 *                                  negative.
	 */
	public CSSRuleArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructs an empty rule list with an initial capacity of 16.
	 */
	public CSSRuleArrayList() {
		super(16);
	}

	public CSSRuleArrayList(Collection<? extends AbstractCSSRule> c) {
		super(c);
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

	@Override
	public String toString() {
		int sz = size();
		StyleFormattingContext context = new DefaultStyleFormattingContext();
		BufferSimpleWriter wri = new BufferSimpleWriter(sz * 128);
		try {
			writeCssText(wri, context);
		} catch (IOException e) {
		}
		return wri.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		int sz = size();
		for (int i = 0; i < sz; i++) {
			AbstractCSSRule rule = item(i);
			rule.writeCssText(wri, context);
		}
	}

}
