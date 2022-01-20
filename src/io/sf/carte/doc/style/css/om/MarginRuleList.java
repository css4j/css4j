/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.SimpleWriter;

/**
 * Stores a list of CSS margin rules, implementing CSSRuleList.
 * 
 * @author Carlos Amengual
 * 
 */
public class MarginRuleList extends AbstractRuleList<MarginRule> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty rule list with the specified initial capacity.
	 * 
	 * @param initialCapacity the initial capacity.
	 * @throws IllegalArgumentException if the specified initial capacity is
	 *                                  negative.
	 */
	public MarginRuleList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructs an empty rule list with an initial capacity of 16.
	 */
	public MarginRuleList() {
		super(16);
	}

	public MarginRuleList(Collection<MarginRule> c) {
		super(c);
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
