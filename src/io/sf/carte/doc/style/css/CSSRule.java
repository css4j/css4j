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

import io.sf.carte.util.SimpleWriter;

/**
 * A CSS rule.
 *
 */
public interface CSSRule extends org.w3c.dom.css.CSSRule {

	/**
	 * Rule is a <code>CSSUnknownRule</code>.
	 */
	short UNKNOWN_RULE = org.w3c.dom.css.CSSRule.UNKNOWN_RULE;

	/**
	 * Rule is a {@link CSSStyleRule}.
	 */
	short STYLE_RULE = org.w3c.dom.css.CSSRule.STYLE_RULE;

	/**
	 * Rule is a <code>CSSImportRule</code>.
	 */
	short IMPORT_RULE = org.w3c.dom.css.CSSRule.IMPORT_RULE;

	/**
	 * Rule is a {@link CSSMediaRule}.
	 */
	short MEDIA_RULE = org.w3c.dom.css.CSSRule.MEDIA_RULE;

	/**
	 * Rule is a {@link CSSFontFaceRule}.
	 */
	short FONT_FACE_RULE = org.w3c.dom.css.CSSRule.FONT_FACE_RULE;

	/**
	 * Rule is a {@link CSSPageRule}.
	 */
	short PAGE_RULE = org.w3c.dom.css.CSSRule.PAGE_RULE;

	/**
	 * Rule is a {@link CSSKeyframesRule}.
	 */
	short KEYFRAMES_RULE = 7;

	/**
	 * Rule is a {@link CSSKeyframeRule}.
	 */
	short KEYFRAME_RULE = 8;

	/**
	 * Rule is a {@link CSSMarginRule}.
	 */
	short MARGIN_RULE = 9;

	/**
	 * Rule is a {@link CSSNamespaceRule}.
	 */
	short NAMESPACE_RULE = 10;

	/**
	 * Rule is a {@link CSSCounterStyleRule}.
	 */
	short COUNTER_STYLE_RULE = 11;

	/**
	 * Rule is a {@link CSSSupportsRule}.
	 */
	short SUPPORTS_RULE = 12;

	short DOCUMENT_RULE = 13;

	/**
	 * Rule is a {@link CSSFontFeatureValuesRule}.
	 */
	short FONT_FEATURE_VALUES_RULE = 14;

	/**
	 * Rule is a {@code @viewport} rule.
	 */
	short VIEWPORT_RULE = 15;

	short REGION_STYLE_RULE = 16;
	short CUSTOM_MEDIA_RULE = 17;

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
	@Override
	CSSRule getParentRule();

	/**
	 * Get the style sheet that contains this rule.
	 *
	 * @return the style sheet, or null if no sheet contains this rule.
	 */
	@Override
	CSSStyleSheet<? extends CSSRule> getParentStyleSheet();

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
