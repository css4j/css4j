/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Collection;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet.Cascade;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Stores a list of CSS rules, implementing CSSRuleList.
 */
public class CSSRuleArrayList extends AbstractRuleList<AbstractCSSRule> {

	private static final long serialVersionUID = 3L;

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
	public int insertRule(CSSRule cssrule, int index) {
		if (index > size()) {
			index = size();
		} else if (index < 0) {
			index = 0;
		}
		add(index, (AbstractCSSRule) cssrule);
		return index;
	}

	void cascade(Cascade cascade, SelectorMatcher matcher, ComputedCSSStyle style,
			String targetMedium) {
		for (AbstractCSSRule rule : this) {
			rule.cascade(cascade, matcher, style, targetMedium);
		}
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
