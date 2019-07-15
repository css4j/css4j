/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;

/**
 * Handle CSS errors at the style sheet level.
 */
public interface SheetErrorHandler extends SACErrorHandler {

	/**
	 * A fatal problem was found parsing an at-rule.
	 * 
	 * @param e
	 *            the exception explaining the problem.
	 * @param atRule
	 *            the text containing the at-rule.
	 */
	void badAtRule(DOMException e, String atRule);

	/**
	 * Found a media-related rule with a wrong media list.
	 * 
	 * @param media
	 *            the media list.
	 */
	void badMediaList(SACMediaList media);

	/**
	 * Error found in computed style declaration.
	 * <p>
	 * This means that an error could only be found when processing the values for computing
	 * the style.
	 * 
	 * @param node
	 *            the node for which the style was computed.
	 * @param propertyName
	 *            the name of the property involved in error.
	 * @param propertyValue
	 *            the value of the property involved in the error.
	 * @param message
	 *            description of the error.
	 */
	void computedStyleError(Node node, String propertyName, String propertyValue, String message);

	/**
	 * While computing a style, an error was found when processing the presentational hints of
	 * an element.
	 * 
	 * @param elm
	 *            the element.
	 * @param e
	 *            the exception describing the error found.
	 */
	void presentationalHintError(CSSElement elm, DOMException e);

	/**
	 * Notify this handler that an empty style rule (a style rule without property value
	 * declarations) was found.
	 * 
	 * @param selector
	 *            the selector for the empty rule.
	 */
	void emptyStyleRule(String selector);

	/**
	 * Notify this handler that an import rule was ignored for the given uri.
	 * <p>
	 * Imports can be ignored if they happen at the wrong place.
	 * 
	 * @param uri
	 *            the uri for the ignored rule.
	 */
	void ignoredImport(String uri);

	/**
	 * Report an error processing an inline style.
	 * <p>
	 * In HTML, it means that the style declration within the <code>style</code> attribute
	 * could not be parsed.
	 * 
	 * @param e
	 *            the exception found.
	 * @param elm
	 *            the element whose inline style was parsed.
	 * @param attr
	 *            the contents of the attribute containing the inline style (generally
	 *            <code>style</code>).
	 */
	void inlineStyleError(DOMException e, Element elm, String attr);

	/**
	 * An error was found when parsing a rule.
	 * 
	 * @param rule
	 *            the rule.
	 * @param ex
	 *            the exception.
	 */
	void ruleParseError(CSSRule rule, CSSParseException ex);

	/**
	 * A warning was produced when parsing a rule.
	 * 
	 * @param rule
	 *            the rule.
	 * @param ex
	 *            the exception.
	 */
	void ruleParseWarning(CSSRule rule, CSSParseException ex);

	/**
	 * Notify this handler that a rule of unknown type was found.
	 * 
	 * @param rule
	 *            the unknown rule.
	 */
	void unknownRule(String rule);

	/**
	 * the SAC parser is malfunctioning. Implementations may just ignore this method.
	 * <p>
	 * It may be called if a property was received for being processed outside of a rule.
	 * </p>
	 * 
	 * @param message
	 *            the message.
	 */
	void sacMalfunction(String message);

	/**
	 * Check whether this object has handled higher-level object model errors.
	 * 
	 * @return <code>true</code> if this object has handled higher-level object model errors.
	 */
	boolean hasOMErrors();

	/**
	 * Merge the error state from the error handler of another sheet.
	 * <p>
	 * Implementations are only required to merge boolean state of SAC errors and warnings.
	 * Merging other state is optional.
	 * 
	 * @param other
	 *            the other style sheet error handler.
	 */
	void mergeState(SheetErrorHandler other);

	/**
	 * Reset the error handler, getting it ready for a new run.
	 */
	void reset();

	/**
	 * Reset the error state about computed styles and presentational hints.
	 */
	void resetComputedStyleErrors();

}
