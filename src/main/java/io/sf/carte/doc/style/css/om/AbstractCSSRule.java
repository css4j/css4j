/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.SimpleWriter;

/**
 * Abstract class to be inherited by all CSS rules.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class AbstractCSSRule implements CSSRule, java.io.Serializable {

	private static final long serialVersionUID = 1L;

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
	 * If this rule does not contain a preceding comment list, create one.
	 * <p>
	 * If this rule already has a preceding comment list, does nothing.
	 */
	abstract public void enablePrecedingComments();

	abstract void setPrecedingComments(StringList precedingComments);

	/**
	 * If this rule does not contain a trailing comment list, create one.
	 * <p>
	 * If this rule already has a trailing comment list, does nothing.
	 */
	abstract public void enableTrailingComments();

	abstract void setTrailingComments(StringList trailingComments);

	/**
	 * Obtain a clone of this rule whose parent sheet is <code>parentSheet</code>.
	 * 
	 * @param parentSheet the parent sheet for the new rule.
	 * @return a clone of this rule with the given parent sheet.
	 */
	abstract public AbstractCSSRule clone(AbstractCSSStyleSheet parentSheet);

	/**
	 * Set the style sheet that is parent of this rule.
	 * 
	 * @param parentSheet
	 *            the parent style sheet.
	 */
	abstract void setParentStyleSheet(AbstractCSSStyleSheet parentSheet);

}
