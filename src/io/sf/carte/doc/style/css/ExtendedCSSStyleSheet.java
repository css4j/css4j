/*
 * This software extends interfaces defined by CSS Object Model draft
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2005-2018 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import java.io.IOException;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSPageRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.style.css.om.AbstractCSSRule;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.om.MediaRule;

/**
 * A style sheet.
 * <p>
 * This extension to the W3C interface adds utility as well as factory methods. The
 * factory methods create rules that have the same origin specificity (like 'author' or
 * 'user') as this style sheet.
 */
public interface ExtendedCSSStyleSheet<R extends ExtendedCSSRule> extends CSSStyleSheet {

	/**
	 * Inserts a rule in the current insertion point (generally after the last rule).
	 *
	 * @param cssrule
	 *            the rule to be inserted.
	 * @throws DOMException
	 *             NAMESPACE_ERR if the rule could not be added due to a namespace-related
	 *             error.
	 */
	void addRule(R cssrule) throws DOMException;

	/**
	 * Adds the rules contained by the supplied style sheet, if that sheet is not disabled.
	 * <p>
	 * If the provided sheet does not target all media, a media rule is created.
	 *
	 * @param sheet
	 *            the sheet whose rules are to be added.
	 */
	void addStyleSheet(AbstractCSSStyleSheet sheet);

	/**
	 * Gets the collection of all CSS rules contained within the style sheet.
	 *
	 * @return the list of all CSS rules contained within the style sheet.
	 */
	@Override ExtendedCSSRuleList<R> getCssRules();

	/**
	 * Get the destination media for this sheet.
	 *
	 * @return the media query list.
	 */
	@Override MediaQueryList getMedia();

	/**
	 * Clone this style sheet.
	 *
	 * @return the cloned style sheet.
	 */
	ExtendedCSSStyleSheet<R> clone();

	/**
	 * Create a CSSCounterStyleRule compatible with this implementation.
	 *
	 * @param name
	 *            the counter-style name.
	 * @return a CSSCounterStyleRule object.
	 */
	CSSCounterStyleRule createCounterStyleRule(String name);

	/**
	 * Create a CSS Font Face rule compatible with this implementation.
	 *
	 * @return a CSS Font Face rule object.
	 */
	CSSFontFaceRule createFontFaceRule();

	/**
	 * Create a CSSFontFeatureValuesRule compatible with this implementation.
	 *
	 * @param fontFamily
	 *            the font family.
	 * @return a CSSFontFeatureValuesRule object.
	 */
	CSSFontFeatureValuesRule createFontFeatureValuesRule(String[] fontFamily);

	/**
	 * Create a CSS import rule compatible with this implementation.
	 *
	 * @param mediaList
	 *            a list of media types for which the new import rule may be used.
	 * @param href
	 *            the URI from which to import the sheet.
	 * @return a CSS import rule.
	 */
	CSSImportRule createImportRule(MediaQueryList mediaList, String href);

	/**
	 * Create a CSSKeyframesRule compatible with this implementation.
	 *
	 * @param keyframesName
	 *            the name of the keyframes.
	 * @return a CSSKeyframesRule object.
	 */
	CSSKeyframesRule createKeyframesRule(String keyframesName);

	/**
	 * Create a CSS media rule.
	 *
	 * @param mediaList
	 *            a list of media types for the new rule.
	 * @return a CSS media rule.
	 */
	MediaRule createMediaRule(MediaQueryList mediaList);

	/**
	 * Create a CSS page rule compatible with this implementation.
	 *
	 * @return a CSS page rule.
	 */
	CSSPageRule createPageRule();

	/**
	 * Create a CSS namespace rule compatible with this implementation.
	 *
	 * @param prefix
	 *            the namespace prefix.
	 * @param namespaceUri
	 *            the namespace URI.
	 * @return a CSS namespace rule.
	 * @throws DOMException
	 *             INVALID_ACCESS_ERR: if the prefix or the URI are null.
	 */
	CSSNamespaceRule createNamespaceRule(String prefix, String namespaceUri);

	/**
	 * Create a CSS margin rule compatible with this implementation.
	 *
	 * @param name
	 *            the margin rule name.
	 * @return a CSS margin rule.
	 */
	CSSMarginRule createMarginRule(String name);

	/**
	 * Create a CSSSupportsRule compatible with this implementation.
	 *
	 * @return a CSSSupportsRule object.
	 */
	CSSSupportsRule createSupportsRule();

	/**
	 * Create a CSSViewportRule compatible with this implementation.
	 *
	 * @return a CSSViewportRule object.
	 */
	CSSDeclarationRule createViewportRule();

	/**
	 * Create a CSS style declaration compatible with this implementation.
	 *
	 * @return a CSS style declaration.
	 */
	CSSStyleDeclaration createStyleDeclaration();

	/**
	 * Create a CSS style rule.
	 *
	 * @return a CSS style rule.
	 */
	CSSStyleDeclarationRule createStyleRule();

	/**
	 * Gets the error handler for this style sheet.
	 *
	 * @return the error handler.
	 */
	SheetErrorHandler getErrorHandler();

	/**
	 * Check whether this sheet contains rules that have errors or warnings reported
	 * to their handlers.
	 *
	 * @return <code>true</code> if this sheet contains rules that have errors or
	 *         warnings.
	 */
	boolean hasRuleErrorsOrWarnings();

	/**
	 * Returns a list of rules that apply to a style where the given longhand property
	 * is set (either explicitly or through a shorthand).
	 * <p>
	 * Grouping rules are scanned too, regardless of the medium or condition.
	 *
	 * @param longhandPropertyName
	 *            the longhand property name.
	 * @return the list of rules, or <code>null</code> if no rules declare that property,
	 *         or the property is a shorthand.
	 */
	ExtendedCSSRuleList<? extends ExtendedCSSRule> getRulesForProperty(String longhandPropertyName);

	/**
	 * Returns an array of selectors that apply to a style where the given longhand property
	 * is set (either explicitly or through a shorthand).
	 * <p>
	 * Grouping rules are scanned too, regardless of the medium or condition.
	 *
	 * @param longhandPropertyName
	 *            the longhand property name.
	 * @return the array of selectors, or <code>null</code> if no rules declare that property,
	 *         or the property is a shorthand.
	 */
	Selector[] getSelectorsForProperty(String longhandPropertyName);

	/**
	 * Get the style sheet factory used to produce this sheet.
	 *
	 * @return the style sheet factory.
	 */
	CSSStyleSheetFactory getStyleSheetFactory();

	/**
	 * Parses a source into this style sheet.
	 * <p>
	 * If this style sheet is not empty, the rules from the parsed source will be added at the
	 * end of the rule list.
	 * <p>
	 * The comments preceding a rule will be available through
	 * {@link AbstractCSSRule#getPrecedingComments()}.
	 *
	 * @param source
	 *            the SAC input source.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, <code>false</code> otherwise.
	 * @throws DOMException
	 *             if a DOM problem is found parsing the sheet.
	 * @throws CSSException
	 *             if a non-DOM problem is found parsing the sheet.
	 * @throws IOException
	 *             if a problem is found reading the sheet.
	 */
	boolean parseStyleSheet(InputSource source) throws DOMException, IOException;

	/**
	 * Parses a style sheet.
	 * <p>
	 * If the style sheet is not empty, the rules from the parsed source will be
	 * added at the end of the rule list, with the same origin as the rule with a
	 * highest precedence origin.
	 * <p>
	 * If <code>ignoreComments</code> is false, the comments preceding a rule will
	 * be available through {@link AbstractCSSRule#getPrecedingComments()}.
	 * <p>
	 * To create a sheet, see
	 * {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#createStyleSheet(String title, io.sf.carte.doc.style.css.MediaQueryList media)
	 * CSSStyleSheetFactory.createStyleSheet(String,MediaQueryList)}
	 *
	 * @param source         the SAC input source.
	 * @param ignoreComments true if comments have to be ignored.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal
	 *         errors, <code>false</code> otherwise.
	 * @throws DOMException if a problem is found parsing the sheet.
	 * @throws IOException  if a problem is found reading the sheet.
	 */
	boolean parseStyleSheet(InputSource source, boolean ignoreComments) throws DOMException, IOException;

}
