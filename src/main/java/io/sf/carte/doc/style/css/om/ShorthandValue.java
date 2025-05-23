/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.util.SimpleWriter;

/**
 * Value for a shorthand property.
 *
 * @author Carlos Amengual
 *
 */
class ShorthandValue extends StyleValue implements io.sf.carte.doc.style.css.CSSShorthandValue {

	private static final long serialVersionUID = 1L;

	private String cssText = null;

	private final LexicalUnit lexicalUnit;

	private final boolean important;

	private final boolean priorityCompat;

	private boolean attrTainted;

	private final HashSet<String> longhands;

	private String miniCssText = "";

	ShorthandValue(LexicalUnit lexicalUnit, boolean important) {
		this(lexicalUnit, important, new String[0]);
	}

	private ShorthandValue(LexicalUnit lexicalUnit, boolean important, String[] longhands) {
		super();
		this.lexicalUnit = lexicalUnit;
		this.important = important;
		priorityCompat = lexicalUnit.getLexicalUnitType() == LexicalUnit.LexicalType.COMPAT_PRIO;
		this.longhands = new HashSet<>(longhands.length);
		Collections.addAll(this.longhands, longhands);
	}

	private ShorthandValue(ShorthandValue copied) {
		super();
		this.lexicalUnit = copied.lexicalUnit;
		this.important = copied.important;
		this.priorityCompat = copied.priorityCompat;
		this.cssText = copied.cssText;
		this.miniCssText = copied.miniCssText;
		this.longhands = new HashSet<>(copied.longhands.size());
		this.longhands.addAll(copied.longhands);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"This property can only be changed from CSSStyleDeclaration.setCssText()");
	}

	public void setShorthandText(String cssText, String miniCssText) throws DOMException {
		this.cssText = cssText;
		this.miniCssText = miniCssText;
	}

	public LexicalUnit getLexicalUnit() {
		return lexicalUnit;
	}

	@Override
	public HashSet<String> getLonghands() {
		return longhands;
	}

	public boolean isAttrTainted() {
		return attrTainted;
	}

	@Override
	public boolean isImportant() {
		return important && !priorityCompat;
	}

	public boolean isPriorityCompat() {
		return priorityCompat;
	}

	@Override
	public String getCssText() {
		return cssText;
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(getCssText());
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		return miniCssText;
	}

	boolean isSetSubproperty(String ptyname) {
		return longhands.contains(ptyname);
	}

	boolean overrideBy(ShorthandValue shorthand) {
		this.longhands.removeAll(shorthand.longhands);
		return this.longhands.isEmpty();
	}

	/**
	 * Override one of the subproperties of this shorthand by the given one.
	 * 
	 * @param longhandName
	 *            the longhand.
	 * @return <code>true</code> if this shorthand was totally overridden (no longhand
	 *         subproperties are left).
	 */
	boolean overrideByLonghand(String longhandName) {
		this.longhands.remove(longhandName);
		return this.longhands.isEmpty();
	}

	@Override
	public ShorthandValue clone() {
		return new ShorthandValue(this);
	}

	static ShorthandValue createCSSShorthandValue(ShorthandDatabase sdb, String shorthandName, LexicalUnit value,
			boolean important, boolean attrTainted) {
		ShorthandValue shval = new ShorthandValue(value, important, sdb.getLonghandProperties(shorthandName));
		if ("border".equals(shorthandName)) {
			shval.longhands.add("border-image-source");
			shval.longhands.add("border-image-slice");
			shval.longhands.add("border-image-width");
			shval.longhands.add("border-image-outset");
			shval.longhands.add("border-image-repeat");
		} else if ("font".equals(shorthandName)) {
			shval.longhands.add("font-variant-caps");
			shval.longhands.add("font-variant-ligatures");
			shval.longhands.add("font-variant-position");
			shval.longhands.add("font-variant-numeric");
			shval.longhands.add("font-variant-alternates");
			shval.longhands.add("font-variant-east-asian");
		}
		shval.attrTainted = attrTainted;
		return shval;
	}

}
