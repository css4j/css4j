/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSAttrValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Attr value.
 * 
 */
public class AttrValue extends ProxyValue implements CSSAttrValue {

	private String attrname = null;
	private String typeval = null;
	private StyleValue fallback = null;

	private final byte flags;

	public AttrValue(byte flags) {
		super(Type.ATTR);
		this.flags = flags;
	}

	protected AttrValue(AttrValue copied) {
		super(copied);
		attrname = copied.attrname;
		typeval = copied.typeval;
		fallback = copied.fallback;
		this.flags = copied.flags;
	}

	@Override
	public String getAttributeName() {
		return attrname;
	}

	@Override
	public String getAttributeType() {
		return typeval;
	}

	@Override
	public void setExpectInteger() {
		super.setExpectInteger();
		if (fallback != null && fallback.isPrimitiveValue()) {
			((PrimitiveValue) fallback).setExpectInteger();
		}
	}

	@Override
	public StyleValue getFallback() {
		return fallback;
	}

	/**
	 * Sets the fallback for this <code>attr()</code> value.
	 * 
	 * @param fallback the fallback value.
	 */
	public void setFallback(StyleValue fallback) {
		this.fallback = fallback;
	}

	/**
	 * Creates a default value for the given <code>attr()</code> value type.
	 * 
	 * @param valueType the value type. If <code>null</code>, a <code>string</code>
	 *                  will be assumed.
	 * 
	 * @return the default value, or <code>null</code> if no suitable default was
	 *         found.
	 */
	public static TypedValue defaultFallback(String valueType) {
		// Defaults
		TypedValue defaultFallback = null;
		if (valueType == null || "string".equalsIgnoreCase(valueType)) {
			defaultFallback = new StringValue();
			defaultFallback.setStringValue(Type.STRING, "");
		} else if ("color".equalsIgnoreCase(valueType)) {
			defaultFallback = new IdentifierValue("currentColor");
		} else if ("integer".equalsIgnoreCase(valueType) || "number".equalsIgnoreCase(valueType)
				|| "length".equalsIgnoreCase(valueType)) {
			defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0);
		} else if ("angle".equalsIgnoreCase(valueType)) {
			defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 0);
		} else if ("time".equalsIgnoreCase(valueType)) {
			defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_S, 0);
		} else if ("frequency".equalsIgnoreCase(valueType)) {
			defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_HZ, 0);
		} else if ("%".equalsIgnoreCase(valueType)) {
			defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_PERCENTAGE, 0);
		} else {
			String lctypeval = valueType.toLowerCase(Locale.ROOT).intern();
			short cssUnit = ParseHelper.unitFromString(lctypeval);
			if (cssUnit != CSSUnit.CSS_OTHER) {
				if (CSSUnit.isLengthUnitType(cssUnit)) {
					defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0);
				} else if (CSSUnit.isAngleUnitType(cssUnit)) {
					defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_DEG, 0);
				} else if (cssUnit == CSSUnit.CSS_S || cssUnit == CSSUnit.CSS_MS) {
					defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_S, 0);
				} else if (cssUnit == CSSUnit.CSS_HZ || cssUnit == CSSUnit.CSS_KHZ) {
					defaultFallback = NumberValue.createCSSNumberValue(CSSUnit.CSS_HZ, 0);
				}
			}
		}
		return defaultFallback;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			String strval = lunit.getStringValue();
			parseAttrValues(strval);
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getCssText() {
		BufferSimpleWriter sw = new BufferSimpleWriter(32);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("attr(");
		wri.write(attrname);
		if (typeval != null) {
			wri.write(' ');
			wri.write(typeval);
		}
		if (fallback != null) {
			wri.write(", ");
			fallback.writeCssText(wri);
		}
		wri.write(')');
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		StringBuilder buf = new StringBuilder(32);
		buf.append("attr(");
		buf.append(attrname);
		if (typeval != null) {
			buf.append(' ');
			buf.append(typeval);
		}
		if (fallback != null) {
			buf.append(',');
			buf.append(fallback.getMinifiedCssText(propertyName));
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		ValueFactory factory = new ValueFactory(flags);
		StyleValue cssval = factory.parseProperty(cssText);
		if (cssval == null || cssval.getPrimitiveType() != Type.ATTR) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not an attr value.");
		}
		AttrValue attr = (AttrValue) cssval;
		this.attrname = attr.attrname;
		this.typeval = attr.typeval;
		this.fallback = attr.fallback;
	}

	private void parseAttrValues(String attr) throws DOMException {
		int len = attr.length();
		int idx = attr.indexOf(',');
		int idxp1 = idx + 1;
		if (idxp1 == len) {
			badSyntax(attr);
		}
		if (idxp1 != 0) {
			String s = attr.substring(idxp1, len).trim();
			ValueFactory factory = new ValueFactory(flags);
			StyleValue value = factory.parseProperty(s);
			if (TypedValue.isOrContainsType(value, Type.ATTR)) {
				badSyntax(attr);
			}
			fallback = value;
		}
		if (idx == -1) {
			idx = len;
		}
		StringTokenizer st = new StringTokenizer(attr.substring(0, idx));
		if (!st.hasMoreTokens()) {
			badSyntax(attr);
		}
		attrname = st.nextToken();
		attrname = ParseHelper.parseIdent(attrname);
		if (st.hasMoreTokens()) {
			typeval = st.nextToken();
			if (st.hasMoreTokens()) {
				badSyntax(attr);
			}
		}
	}

	private void badSyntax(String attr) throws DOMException {
		throw new DOMException(DOMException.SYNTAX_ERR, "Bad attr(): " + attr);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((attrname == null) ? 0 : attrname.hashCode());
		result = prime * result + ((fallback == null) ? 0 : fallback.hashCode());
		result = prime * result + ((typeval == null) ? 0 : typeval.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AttrValue other = (AttrValue) obj;
		if (attrname == null) {
			if (other.attrname != null) {
				return false;
			}
		} else if (!attrname.equals(other.attrname)) {
			return false;
		}
		if (fallback == null) {
			if (other.fallback != null) {
				return false;
			}
		} else if (!fallback.equals(other.fallback)) {
			return false;
		}
		if (typeval == null) {
			return other.typeval == null;
		} else {
			return typeval.equals(other.typeval);
		}
	}

	@Override
	public AttrValue clone() {
		return new AttrValue(this);
	}

}
