/*
 * This software extends interfaces defined by CSS Object Model draft
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 *
 * Copyright © 2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc.style.css;

/**
 * A CSS {@literal @}media rule.
 *
 */
public interface CSSMediaRule extends CSSGroupingRule, org.w3c.dom.css.CSSMediaRule {

	/**
	 * A list of media queries that apply to this rule.
	 */
	@Override
	MediaQueryList getMedia();

}
