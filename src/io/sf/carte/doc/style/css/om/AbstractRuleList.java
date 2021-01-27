/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.ExtendedCSSRuleList;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.SimpleWriter;

/**
 * Stores a list of CSS rules, implementing CSSRuleList.
 * 
 * @author Carlos Amengual
 * 
 */
class AbstractRuleList<T extends ExtendedCSSRule> extends ArrayList<T>
		implements ExtendedCSSRuleList<T> {

	private static final long serialVersionUID = 2L;

	/**
	 * Constructs an empty rule list with the specified initial capacity.
	 * 
	 * @param initialCapacity the initial capacity.
	 * @throws IllegalArgumentException if the specified initial capacity is
	 *                                  negative.
	 */
	public AbstractRuleList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructs an empty rule list with an initial capacity of 16.
	 */
	public AbstractRuleList() {
		super(16);
	}

	public AbstractRuleList(Collection<? extends T> c) {
		super(c);
	}

	/**
	 * Gives the number of rules in the list.
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
	public T item(int index) {
		if (index >= getLength() || index < 0) {
			return null;
		}
		return get(index);
	}

	/**
	 * Appends the rule to the end of the list
	 * 
	 * @param rule
	 *            the rule to append.
	 * @return <code>true</code> if the rule was appended successfully, <code>false</code> otherwise.
	 */
	public boolean append(T rule) {
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
			T rule = item(i);
			sb.append(rule.getCssText()).append('\n');
		}
		return sb.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		int sz = size();
		for (int i = 0; i < sz; i++) {
			T rule = item(i);
			rule.writeCssText(wri, context);
		}
	}

}
