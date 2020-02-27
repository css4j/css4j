/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.LinkedList;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.util.SimpleWriter;

/**
 * Compat-aware CSS inline style declaration.
 *
 */
abstract public class CompatInlineStyle extends InlineStyle {

	private final CompatDeclarationSet compatSet;

	protected CompatInlineStyle() {
		super();
		compatSet = new CompatDeclarationSet();
	}

	protected CompatInlineStyle(CompatInlineStyle copiedObject) {
		super(copiedObject);
		compatSet = copiedObject.compatSet.clone();
	}

	/*
	 * Compat boilerplate code: the following methods must be the same as their equivalents in
	 * CompatStyleDeclaration.
	 */
	@Override
	protected void setLonghandProperty(String propertyName, LexicalUnit value, boolean important) throws DOMException {
		if (value.getLexicalUnitType() != LexicalUnit.LexicalType.COMPAT_PRIO) {
			super.setLonghandProperty(propertyName, value, important);
		} else {
			ValueFactory factory = getValueFactory();
			StyleValue cssvalue;
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
	protected boolean addOverrideProperty(String propertyName, StyleValue cssValue, String priority) {
		return addCompatProperty(propertyName, cssValue, priority);
	}

	@Override
	protected void compatLonghand(String propertyName, StyleValue value, boolean priorityImportant,
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
	protected void appendLonghandMinifiedCssText(StringBuilder sb, String ptyname, StyleValue cssVal,
			boolean important) {
		boolean isCompatOv = compatSet.isCompatLonghand(ptyname);
		StyleValue compatvalue;
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
			context.endInlinePropertyDeclaration(wri);
		}
		super.writeShorthandCssText(wri, context, shorthandName, shval);
		if (isCompatOv) {
			compatvalue = compatSet.getCompatShorthand(shorthandName);
			if (!compatvalue.isPriorityCompat()) {
				super.writeShorthandCssText(wri, context, shorthandName, compatvalue);
			} else {
				CompatDeclarationSet.writeIEPrioCharShorthandCssText(wri, context, shorthandName, compatvalue);
				context.endInlinePropertyDeclaration(wri);
			}
		}
	}

	@Override
	protected void writeLonghandCssText(SimpleWriter wri, StyleFormattingContext context, String ptyname,
			StyleValue ptyvalue, boolean important) throws IOException {
		boolean isCompatOv = compatSet.isCompatLonghand(ptyname);
		StyleValue compatvalue;
		if (!isCompatOv && !important && (compatvalue = compatSet.getNonOvCompatLonghand(ptyname)) != null) {
			CompatDeclarationSet.writeIEPrioCharLonghandCssText(wri, context, ptyname, compatvalue);
			context.endInlinePropertyDeclaration(wri);
		}
		super.writeLonghandCssText(wri, context, ptyname, ptyvalue, important);
		if (isCompatOv) {
			compatvalue = compatSet.getCompatLonghand(ptyname);
			if (!CompatDeclarationSet.isPriorityCompat(compatvalue)) {
				super.writeLonghandCssText(wri, context, ptyname, compatvalue,
						compatSet.isImportantCompatLonghand(ptyname));
			} else {
				CompatDeclarationSet.writeIEPrioCharLonghandCssText(wri, context, ptyname, compatvalue);
				context.endInlinePropertyDeclaration(wri);
			}
		}
	}

	@Override
	void clear() {
		super.clear();
		compatSet.clear();
	}

}
