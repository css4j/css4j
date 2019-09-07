/*
 * This software extends interfaces defined by CSS Object Model draft
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2018 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import java.io.IOException;

import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.util.SimpleWriter;

/**
 * An extended CSS rule.
 *
 */
public interface ExtendedCSSRule extends CSSRule {

	short KEYFRAMES_RULE = 7;
	short KEYFRAME_RULE = 8;
	short MARGIN_RULE = 9;
	short NAMESPACE_RULE = 10;
	short COUNTER_STYLE_RULE = 11;
	short SUPPORTS_RULE = 12;
	short DOCUMENT_RULE = 13;
	short FONT_FEATURE_VALUES_RULE = 14;
	short VIEWPORT_RULE = 15;
	short REGION_STYLE_RULE = 16;
	short CUSTOM_MEDIA_RULE = 17;

	/**
	 * Obtain a clone of this rule whose parent sheet is <code>parentSheet</code>.
	 *
	 * @param parentSheet the parent sheet for the new rule.
	 * @return a clone of this rule with the given parent sheet.
	 */
	ExtendedCSSRule clone(AbstractCSSStyleSheet parentSheet);

	/**
	 * A minified parsable textual representation of the rule. This reflects the current state
	 * of the rule and not its initial value.
	 *
	 * @return the minified textual representation of the rule.
	 */
	String getMinifiedCssText();

	/**
	 * If this rule is contained inside another rule, return that rule. If it is not nested
	 * inside any other rules, return <code>null</code>.
	 *
	 * @return the containing rule, if any, otherwise <code>null</code>.
	 */
	@Override ExtendedCSSRule getParentRule();

	/**
	 * Get the style sheet that contains this rule.
	 *
	 * @return the style sheet, or null if no sheet contains this rule.
	 */
	@Override ExtendedCSSStyleSheet<? extends ExtendedCSSRule> getParentStyleSheet();

	/**
	 * Write a serialization of this rule to the given simple writer, according to the given
	 * context.
	 *
	 * @param wri
	 *            the simple writer object.
	 * @param context
	 *            the formatting context.
	 * @throws IOException
	 *             if an error happened while writing.
	 */
	void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException;

}
