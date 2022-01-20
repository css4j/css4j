/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSCustomPropertyValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.util.SimpleWriter;

/**
 * Custom property (<code>var</code>) CSSPrimitiveValue.
 * 
 * @author Carlos Amengual
 *
 */
public class CustomPropertyValue extends PrimitiveValue implements CSSCustomPropertyValue {

	private String name = null;

	private StyleValue fallback = null;

	private boolean expectInteger = false;

	CustomPropertyValue() {
		super(CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY);
		this.fallback = null;
	}

	CustomPropertyValue(StyleValue fallback) {
		super(CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY);
		this.fallback = fallback;
	}

	protected CustomPropertyValue(CustomPropertyValue copied) {
		super(copied);
		this.name = copied.name;
		this.fallback = copied.fallback;
	}

	@Override
	public StyleValue getFallback() {
		return fallback;
	}

	public boolean isExpectingInteger() {
		return expectInteger;
	}

	@Override
	public void setExpectInteger() {
		expectInteger = true;
		if (fallback != null) {
			if (fallback.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
				super.setExpectInteger();
			} else {
				((PrimitiveValue) fallback).setExpectInteger();
			}
		}
	}

	@Override
	public String getCssText() {
		String ftext;
		int sz = name.length();
		if (fallback == null) {
			sz += 2;
			ftext = null;
		} else {
			ftext = fallback.getCssText();
			sz += 4 + ftext.length();
		}
		StringBuilder buf = new StringBuilder(sz);
		buf.append("var(");
		buf.append(name);
		if (ftext != null) {
			buf.append(", ");
			buf.append(ftext);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write("var(");
		wri.write(name);
		if (fallback != null) {
			wri.write(", ");
			fallback.writeCssText(wri);
		}
		wri.write(')');
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		String ftext;
		int sz = name.length();
		if (fallback == null) {
			sz += 2;
			ftext = null;
		} else {
			ftext = fallback.getMinifiedCssText(propertyName);
			sz += 3 + ftext.length();
		}
		StringBuilder buf = new StringBuilder(sz);
		buf.append("var(");
		buf.append(name);
		if (ftext != null) {
			buf.append(',');
			buf.append(ftext);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		ValueFactory factory = new ValueFactory();
		StyleValue cssval = factory.parseProperty(cssText);
		if (cssval == null || cssval.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE ||
				((CSSPrimitiveValue)cssval).getPrimitiveType() != CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not a custom property value.");
		}
		CustomPropertyValue customp = (CustomPropertyValue) cssval;
		this.name = customp.getStringValue();
		this.fallback = customp.fallback;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((fallback == null) ? 0 : fallback.hashCode());
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
		CustomPropertyValue other = (CustomPropertyValue) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (fallback == null) {
			if (other.fallback != null) {
				return false;
			}
		} else if (!fallback.equals(other.fallback)) {
			return false;
		}
		return true;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			/*
			 * We do not set the fallback here
			 */
			LexicalUnit lu = lunit.getParameters();
			name = lu.getStringValue();
			this.nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getStringValue() {
		return name;
	}

	@Override
	public CustomPropertyValue clone() {
		return new CustomPropertyValue(this);
	}

}
