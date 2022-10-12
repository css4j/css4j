/*
 * This software implements or extends interfaces defined by CSS Object Model
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import io.sf.carte.util.Visitor;

/**
 * List of extended style sheets.
 * 
 * See <a href="https://www.w3.org/TR/cssom-1/#the-stylesheetlist-interface">The
 *      <code>StyleSheetList</code> Interface</a>.
 */
public interface CSSStyleSheetList<T extends CSSRule> extends org.w3c.dom.stylesheets.StyleSheetList {

	/**
	 * retrieve a <code>CSSStyleSheet</code> by ordinal index.
	 * 
	 * @param index the index in this list.
	 * @return the sheet at <code>index</code>, or <code>null</code> if
	 *         <code>index</code> is less than zero, or greater or equal to the list
	 *         length.
	 */
	@Override
	CSSStyleSheet<T> item(int index);

	/**
	 * Accept a style rule visitor.
	 * <p>
	 * This method scans all the sheets in this list for style rules and visits
	 * them.
	 * </p>
	 * 
	 * @param visitor the visitor.
	 */
	void acceptStyleRuleVisitor(Visitor<CSSStyleRule> visitor);

	/**
	 * Accept a declaration rule visitor.
	 * <p>
	 * This method scans all the sheets in this list for declaration rules and
	 * visits them.
	 * </p>
	 * 
	 * @param visitor the visitor.
	 */
	void acceptDeclarationRuleVisitor(Visitor<CSSDeclarationRule> visitor);

	/**
	 * Accept a descriptor rule visitor.
	 * <p>
	 * This method scans all the sheets in this list for declaration rules that
	 * declare descriptors, and visits them.
	 * </p>
	 * 
	 * @param visitor the visitor.
	 */
	void acceptDescriptorRuleVisitor(Visitor<CSSDeclarationRule> visitor);

}
