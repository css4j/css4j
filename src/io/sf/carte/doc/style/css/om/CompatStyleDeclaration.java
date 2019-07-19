/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.LinkedList;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.util.SimpleWriter;

/**
 * A CSS style declaration that serializes non-conformant values together with the values
 * that they override, for compatibility with non-compliant browsers.
 */
class CompatStyleDeclaration extends BaseCSSStyleDeclaration {

	private final CompatDeclarationSet compatSet;

	protected CompatStyleDeclaration(BaseCSSDeclarationRule parentRule) {
		super(parentRule);
		compatSet = new CompatDeclarationSet();
	}

	protected CompatStyleDeclaration(CompatStyleDeclaration copiedObject) {
		super(copiedObject);
		compatSet = copiedObject.compatSet.clone();
	}

	@Override
	protected void setLonghandProperty(String propertyName, LexicalUnit value, String priority) throws DOMException {
		if (value.getLexicalUnitType() != LexicalUnit2.SAC_COMPAT_PRIO) {
			super.setLonghandProperty(propertyName, value, priority);
		} else {
			ValueFactory factory = getValueFactory();
			AbstractCSSValue cssvalue;
			try {
				cssvalue = factory.createCSSValue(value, this);
			} catch (DOMException e) {
				// Report error
				StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					CSSPropertyValueException ex = new CSSPropertyValueException("Wrong value for " + propertyName, e);
					ex.setValueText(lexicalUnitToString(value));
					errHandler.wrongValue(propertyName, ex);
				}
				throw e;
			}
			compatLonghand(propertyName, cssvalue, true, isPropertySet(propertyName));
		}
	}

	@Override
	protected boolean addOverrideProperty(String propertyName, AbstractCSSValue cssValue, String priority) {
		return addCompatProperty(propertyName, cssValue, priority);
	}

	@Override
	protected void compatLonghand(String propertyName, AbstractCSSValue value, boolean priorityImportant,
			boolean isOverride) {
		compatSet.setCompatLonghand(propertyName, value, priorityImportant, isOverride);
	}

	@Override
	protected void shorthandError(String propertyName, LexicalUnit value, boolean important,
			LinkedList<String> shadowedShorthands, DOMException e) {
		if (compatSet.setCompatShorthand(propertyName, value, important, shadowedShorthands)) {
			compatWarning(propertyName, value, important);
			return;
		}
		super.shorthandError(propertyName, value, important, shadowedShorthands, e);
	}

	@Override
	protected void appendShorthandMinifiedCssText(StringBuilder sb, String shorthandName, ShorthandValue shval) {
		boolean isCompatOv = compatSet.isCompatShorthand(shorthandName);
		ShorthandValue compatvalue;
		if (!isCompatOv && !shval.isImportant()
				&& (compatvalue = compatSet.getNonOvCompatShorthand(shorthandName)) != null) {
			CompatDeclarationSet.appendIEPrioCharShorthandMinifiedCssText(sb, shorthandName, compatvalue);
		}
		super.appendShorthandMinifiedCssText(sb, shorthandName, shval);
		if (isCompatOv) {
			compatvalue = compatSet.getCompatShorthand(shorthandName);
			if (!compatvalue.isPriorityCompat()) {
				super.appendShorthandMinifiedCssText(sb, shorthandName, compatvalue);
			} else {
				CompatDeclarationSet.appendIEPrioCharShorthandMinifiedCssText(sb, shorthandName, compatvalue);
			}
		}
	}

	@Override
	protected void appendLonghandMinifiedCssText(StringBuilder sb, String ptyname, AbstractCSSValue cssVal,
			boolean important) {
		boolean isCompatOv = compatSet.isCompatLonghand(ptyname);
		AbstractCSSValue compatvalue;
		if (!isCompatOv && !important && (compatvalue = compatSet.getNonOvCompatLonghand(ptyname)) != null) {
			super.appendLonghandMinifiedCssText(sb, ptyname, compatvalue, true);
			sb.append('!').append(';');
		}
		super.appendLonghandMinifiedCssText(sb, ptyname, cssVal, important);
		if (isCompatOv) {
			sb.append(';');
			compatvalue = compatSet.getCompatLonghand(ptyname);
			super.appendLonghandMinifiedCssText(sb, ptyname, compatvalue, compatSet.isImportantCompatLonghand(ptyname));
			if (CompatDeclarationSet.isPriorityCompat(compatvalue)) {
				sb.append('!');
			}
		}
	}

	@Override
	protected void writeShorthandCssText(SimpleWriter wri, StyleFormattingContext context, String shorthandName,
			ShorthandValue shval) throws IOException {
		boolean isCompatOv = compatSet.isCompatShorthand(shorthandName);
		ShorthandValue compatvalue;
		if (!isCompatOv && !shval.isImportant()
				&& (compatvalue = compatSet.getNonOvCompatShorthand(shorthandName)) != null) {
			CompatDeclarationSet.writeIEPrioCharShorthandCssText(wri, context, shorthandName, compatvalue);
		}
		super.writeShorthandCssText(wri, context, shorthandName, shval);
		if (isCompatOv) {
			compatvalue = compatSet.getCompatShorthand(shorthandName);
			if (!compatvalue.isPriorityCompat()) {
				super.writeShorthandCssText(wri, context, shorthandName, compatvalue);
			} else {
				CompatDeclarationSet.writeIEPrioCharShorthandCssText(wri, context, shorthandName, compatvalue);
			}
		}
	}

	@Override
	protected void writeLonghandCssText(SimpleWriter wri, StyleFormattingContext context, String ptyname,
			AbstractCSSValue ptyvalue, boolean important) throws IOException {
		boolean isCompatOv = compatSet.isCompatLonghand(ptyname);
		AbstractCSSValue compatvalue;
		if (!isCompatOv && !important && (compatvalue = compatSet.getNonOvCompatLonghand(ptyname)) != null) {
			CompatDeclarationSet.writeIEPrioCharLonghandCssText(wri, context, ptyname, compatvalue);
		}
		super.writeLonghandCssText(wri, context, ptyname, ptyvalue, important);
		if (isCompatOv) {
			compatvalue = compatSet.getCompatLonghand(ptyname);
			if (!CompatDeclarationSet.isPriorityCompat(compatvalue)) {
				super.writeLonghandCssText(wri, context, ptyname, compatvalue,
						compatSet.isImportantCompatLonghand(ptyname));
			} else {
				CompatDeclarationSet.writeIEPrioCharLonghandCssText(wri, context, ptyname, compatvalue);
			}
		}
	}

	@Override
	void clear() {
		super.clear();
		compatSet.clear();
	}

	@Override
	public CompatStyleDeclaration clone() {
		return new CompatStyleDeclaration(this);
	}

}
