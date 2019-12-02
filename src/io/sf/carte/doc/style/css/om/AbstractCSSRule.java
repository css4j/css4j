/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.SimpleWriter;

/**
 * Abstract class to be inherited by all CSS rules.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class AbstractCSSRule implements CSSRule {

	@Override
	abstract public short getType();

	@Override
	abstract public String getCssText();

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"Cannot set text for this rule");
	}

	/**
	 * A minified parsable textual representation of the rule. This reflects
	 * the current state of the rule and not its initial value.
	 * 
	 * @return the minified textual representation of the rule.
	 */
	@Override
	abstract public String getMinifiedCssText();

	@Override
	public String toString() {
		return getCssText();
	}

	@Override
	abstract public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException;

	abstract boolean hasErrorsOrWarnings();

	@Override
	abstract public AbstractCSSStyleSheet getParentStyleSheet();

	@Override
	abstract public AbstractCSSRule getParentRule();

	abstract public void setParentRule(AbstractCSSRule parent);

	/**
	 * Get the origin of this rule (user agent sheet, author, user).
	 * 
	 * @return the origin of this rule.
	 */
	abstract public byte getOrigin();

	/**
	 * If this rule does not contain a preceding comment list, create one with the
	 * given initial capacity.
	 * <p>
	 * If this rule already has a comment list, does nothing.
	 * 
	 * @param initialSize the initial capacity.
	 */
	abstract public void enablePrecedingComments(int initialSize);

	/**
	 * Get a list of the comments that preceded this rule, if any.
	 * 
	 * @return the list of comments, or <code>null</code> if there were no preceding
	 *         comments or the parsing was specified to ignore comments.
	 * @see AbstractCSSStyleSheet#parseStyleSheet(java.io.Reader, short)
	 */
	abstract public List<String> getPrecedingComments();

	abstract void setPrecedingComments(List<String> precedingComments);

	/**
	 * Get a list of the comments that immediately follow this rule, if any.
	 * <p>
	 * If the parsing mode was {@code COMMENTS_PRECEDING}, or was
	 * {@code COMMENTS_AUTO} and the next comment happens after a newline character,
	 * it shall be assigned to the next rule as a preceding comment.
	 * 
	 * @return the list of comments, or <code>null</code> if there were no trailing
	 *         comments or the parsing was specified to ignore comments.
	 * @see AbstractCSSStyleSheet#parseStyleSheet(java.io.Reader, short)
	 */
	abstract public List<String> getTrailingComments();

	/**
	 * Obtain a clone of this rule whose parent sheet is <code>parentSheet</code>.
	 * 
	 * @param parentSheet the parent sheet for the new rule.
	 * @return a clone of this rule with the given parent sheet.
	 */
	@Override
	abstract public AbstractCSSRule clone(AbstractCSSStyleSheet parentSheet);

	/**
	 * Set the style sheet that is parent of this rule.
	 * 
	 * @param parentSheet
	 *            the parent style sheet.
	 */
	abstract void setParentStyleSheet(AbstractCSSStyleSheet parentSheet);

}
