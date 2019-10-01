/*
 * This software extends interfaces defined by CSS Object Model
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2019 Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.css.CSSStyleRule;

import io.sf.carte.doc.style.css.nsac.SelectorList;

/**
 * An extended CSS style rule.
 * 
 */
public interface ExtendedCSSStyleRule extends CSSDeclarationRule, CSSStyleRule {

	/**
	 * The selectors of this style rule.
	 * 
	 * @return the selector list.
	 */
	SelectorList getSelectorList();

}
