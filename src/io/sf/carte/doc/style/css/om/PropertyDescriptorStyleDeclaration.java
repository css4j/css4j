/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Declaration for {@literal @}property rule descriptors.
 */
class PropertyDescriptorStyleDeclaration extends BaseCSSStyleDeclaration {

	private static final long serialVersionUID = 1L;

	/*
	 * Booleans to keep track of the validity of the rule.
	 */
	private boolean hasSyntax, isUniversalSyntax, hasInherits;

	public PropertyDescriptorStyleDeclaration(BaseCSSDeclarationRule parentRule) {
		super(parentRule);
	}

	public PropertyDescriptorStyleDeclaration() {
		super();
	}

	public PropertyDescriptorStyleDeclaration(PropertyDescriptorStyleDeclaration copiedObject) {
		super(copiedObject);
	}

	@Override
	public void addStyle(BaseCSSStyleDeclaration style) {
		super.addStyle(style);
		updateValidity();
	}

	@Override
	public void setProperty(String propertyName, LexicalUnit lunit, boolean important) throws DOMException {
		propertyName = getCanonicalPropertyName(propertyName);
		if ("syntax".equals(propertyName)) {
			if (lunit.getLexicalUnitType() != LexicalType.STRING) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"'syntax' descriptor in @property rule must be a string.");
			}
			String def = lunit.getStringValue();
			CSSValueSyntax syn;
			try {
				syn = new SyntaxParser().parseSyntax(def);
			} catch (CSSException e) {
				DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
				ex.initCause(e);
				throw ex;
			}
			isUniversalSyntax = syn.getName().equals("*");
			hasSyntax = true;
		} else if ("inherits".equals(propertyName)) {
			String s;
			if (lunit.getLexicalUnitType() != LexicalType.IDENT
					|| (!"true".equalsIgnoreCase(s = lunit.getStringValue().toLowerCase(Locale.ROOT))
							&& !"false".equalsIgnoreCase(s))) {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"'inherits' descriptor in @property rule must be either 'true' or 'false'.");
			}
			hasInherits = true;
		} else if ("initial-value".equals(propertyName)) {
			// Verify that it is a typed value
			ValueFactory factory = getValueFactory();
			StyleValue cssvalue;
			try {
				cssvalue = factory.createCSSValue(lunit, this);
			} catch (DOMException e) {
				// Report error
				StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					CSSPropertyValueException ex = new CSSPropertyValueException("Wrong value for " + propertyName, e);
					ex.setValueText(lexicalUnitToString(lunit));
					errHandler.wrongValue(propertyName, ex);
				}
				throw e;
			}
			//
			if (cssvalue.getCssValueType() != CssType.TYPED) {
				// Invalid type error
				CSSPropertyValueException ex = null;
				StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
				if (errHandler != null) {
					ex = new CSSPropertyValueException("Wrong type for 'initial-value'." + propertyName);
					ex.setValueText(lexicalUnitToString(lunit));
					errHandler.wrongValue(propertyName, ex);
				}
				DOMException exception = new DOMException(DOMException.TYPE_MISMATCH_ERR,
						"Wrong type for 'initial-value'.");
				if (ex != null) {
					exception.initCause(ex);
				}
				throw exception;
			}
			//
			LexicalValue lexicalValue = new LexicalValue();
			lexicalValue.setLexicalUnit(lunit);
			setProperty(propertyName, lexicalValue, important);
			return;
		}
		super.setProperty(propertyName, lunit, important);
	}

	@Override
	public String removeProperty(String propertyName) {
		propertyName = getCanonicalPropertyName(propertyName);
		if (!"syntax".equals(propertyName) && !"inherits".equals(propertyName)
				&& (isUniversalSyntax || !"initial-value".equals(propertyName))) {
			return super.removeProperty(propertyName);
		}
		return "";
	}

	void updateValidity() {
		hasSyntax = false;
		isUniversalSyntax = false;
		hasInherits = false;
		for (int i = 0; i < getLength(); i++) {
			String property = item(i);
			StyleValue value = getPropertyCSSValue(property);
			if ("syntax".equalsIgnoreCase(property)) {
				if (value.getPrimitiveType() == Type.STRING) {
					hasSyntax = true;
					isUniversalSyntax = ((CSSTypedValue) value).getStringValue().trim().equals("*");
				}
			} else if ("inherits".equalsIgnoreCase(property)) {
				if (value.getPrimitiveType() == Type.IDENT) {
					String s = ((CSSTypedValue) value).getStringValue().trim();
					hasInherits = "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
				}
			}
		}
	}

	boolean isValidDeclaration() {
		return hasSyntax && hasInherits && (isUniversalSyntax || hasInitial());
	}

	private boolean hasInitial() {
		PropertyRule rule = (PropertyRule) getParentRule();
		CSSValueSyntax syntax = rule.getSyntax();
		StyleValue value = getPropertyCSSValue("initial-value");
		return value != null && value.matches(syntax) == Match.TRUE;
	}

}
