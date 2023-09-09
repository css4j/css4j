/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Locale;

import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * AnB expression. @see <a href="https://www.w3.org/TR/css-syntax-3/#anb">The An+B microsyntax</a>.
 */
abstract public class AnBExpression implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private SelectorList selectorList = null;
	private int offset = 0;
	private int step = 0;
	private boolean haskeyword = false;

	protected AnBExpression() {
		super();
	}

	/**
	 * Get the step (i.e. &#39;A&#39;).
	 * 
	 * @return the step, zero if not specified.
	 */
	public int getStep() {
		return step;
	}

	/**
	 * Get the expression offset (i.e. &#39;B&#39;).
	 * 
	 * @return the expression offset.
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * If the expression contains a list of <code>of</code>-selectors, return it.
	 * 
	 * @return the list of <code>of</code>-selectors, or <code>null</code> if there was no
	 *         <code>of</code>-list
	 */
	public SelectorList getSelectorList() {
		return selectorList;
	}

	/**
	 * The expression is a keyword ?
	 * 
	 * @return <code>true</code> if the expression is a keyword like <code>odd</code>.
	 */
	public boolean isKeyword() {
		return haskeyword;
	}

	/**
	 * Parse a AnB expression into this object.
	 * <p>
	 * For efficiency, the internal fields are not reset by this method, it should
	 * be called on a newly created object.
	 * 
	 * @param expression the AnB expression.
	 */
	public void parse(String expression) {
		String s = expression;
		int selIdx = s.indexOf(" of ");
		if (selIdx != -1) {
			selectorList = parseSelector(s.substring(selIdx + 4).trim());
			s = s.substring(0, selIdx);
		}
		s = s.trim().toLowerCase(Locale.ROOT);
		offset = 0;
		if (s.equals("even")) {
			step = 2;
			haskeyword = true;
			return;
		} else if (s.equals("odd")) {
			step = 2;
			offset = 1;
			haskeyword = true;
			return;
		}
		int nidx = s.indexOf('n');
		int lm1 = s.length() - 1;
		if (lm1 == -1) {
			reportError(expression);
		}
		if (nidx != -1) {
			if (lm1 == nidx + 1) {
				// We have either no sign or no offset
				reportError(expression);
			} else {
				if (lm1 != nidx) {
					// '[+|-]*[A]*n[+|-]B'
					String offstr = s.substring(nidx + 1);
					offstr = removeSpacesBeforeFirstDigit(offstr);
					if (offstr == null) {
						reportError(expression);
					}
					try {
						offset = Integer.parseInt(offstr);
					} catch (NumberFormatException e) {
						reportError(expression);
					}
				}
				if (nidx == 0) {
					// 'n'
					step = 1;
				} else if (nidx == 1) {
					// [+|-|A]n'
					char c = s.charAt(0);
					if (c == '+') {
						step = 1;
					} else if (c == '-') {
						step = -1;
					} else if (isDigit(c)) {
						step = Integer.parseInt(s.substring(0, nidx));
					} else {
						reportError(expression);
					}
				} else {
					// We now want to parse left part of n '[+|-]*[A]*n'
					try {
						step = Integer.parseInt(s.substring(0, nidx));
					} catch (NumberFormatException e) {
						reportError(expression);
					}
				}
			}
		} else {
			// '[+|-]B'
			step = 0;
			try {
				offset = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				reportError(expression);
			}
		}
	}

	private static String removeSpacesBeforeFirstDigit(String numstr) {
		boolean negative = false;
		boolean foundSpace = false;
		boolean foundSign = false;
		int len = numstr.length();
		for (int i = 0; i < len; i++) {
			char c = numstr.charAt(i);
			if (c == '-') {
				if (foundSign) {
					break;
				}
				foundSign = true;
				negative = true;
			} else if (c == ' ') {
				foundSpace = true;
			} else if (Character.isDigit(c)) {
				if (!foundSpace) {
					return numstr;
				}
				if (negative) {
					return "-" + numstr.subSequence(i, len);
				} else {
					if (foundSign) {
						return numstr.substring(i, len);
					}
				}
			} else if (c == '+') {
				if (foundSign) {
					break;
				}
				foundSign = true;
			} else {
				break;
			}
		}
		return null;
	}

	private static boolean isDigit(char c) {
		return c >= 0x30 && c <= 0x39;
	}

	void reportError(String expression) {
		throw new IllegalArgumentException("Wrong expression " + expression);
	}

	abstract protected SelectorList parseSelector(String selText);

}
