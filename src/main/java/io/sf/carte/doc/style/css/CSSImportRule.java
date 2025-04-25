/*
 * This software extends interfaces defined by CSS Object Model draft
 *  (https://www.w3.org/TR/cssom-1/).
 * Copyright © 2016 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 */

// SPDX-License-Identifier: W3C-20150513

package io.sf.carte.doc.style.css;

/**
 * Import rule. @see
 * <a href="https://drafts.csswg.org/cssom-1/#the-cssimportrule-interface">The
 * <code>CSSImportRule</code> Interface</a>.
 */
public interface CSSImportRule extends CSSRule, org.w3c.dom.css.CSSImportRule {

	/**
	 * The layer name declared in the at-rule itself, or an empty string if the
	 * layer is anonymous, or {@code null} if the at-rule does not declare a layer.
	 * 
	 * @return the layer name.
	 */
	String getLayerName();

	/**
	 * The {@code <supports-condition>} declared in the at-rule, or {@code null} if
	 * the at-rule does not declare a supports condition.
	 * 
	 * @return the supports condition.
	 */
	BooleanCondition getSupportsCondition();

}
