/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.StringTokenizer;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSAttrValue;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Attr primitive value.
 * 
 * @author Carlos Amengual
 *
 */
public class AttrValue extends AbstractCSSPrimitiveValue implements CSSAttrValue {

	private String attrname = null;
	private String typeval = null;
	private AbstractCSSValue fallback = null;

	private final byte flags;

	public AttrValue(byte flags) {
		super();
		this.flags = flags;
		setCSSUnitType(CSS_ATTR);
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
	public AbstractCSSValue getFallback() {
		return fallback;
	}

	@Override
	public String getStringValue() {
		if (typeval == null && fallback == null) {
			return attrname;
		}
		StringBuilder buf = new StringBuilder(attrname.length() + 32);
		buf.append(attrname);
		if (typeval != null) {
			buf.append(' ');
			buf.append(typeval);
		}
		if (fallback != null) {
			buf.append(", ");
			buf.append(fallback.getCssText());
		}
		return buf.toString();
	}

	@Override
	public void setStringValue(short stringType, String stringValue) throws DOMException {
		if (isSubproperty()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was set with a shorthand. Must modify at the style-declaration level.");
		}
		if (stringType != CSSPrimitiveValue.CSS_ATTR) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"This value is an attribute. To have a new type, set it at the style-declaration level.");
		}
		typeval = null;
		fallback = null;
		parseAttrValues(stringValue);
		setPlainCssText("attr(" + stringValue + ')');
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			super.setLexicalUnit(lunit);
			String strval = lunit.getStringValue();
			setPlainCssText("attr(" + strval + ')');
			parseAttrValues(strval);
			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	@Override
	public String getCssText() {
		BufferSimpleWriter sw = new BufferSimpleWriter(attrname.length() + 6);
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
	public void setCssText(String cssText) throws DOMException {
		if (isSubproperty()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"This property was set with a shorthand. Must modify at the style-declaration level.");
		}
		ValueFactory factory = new ValueFactory(flags);
		AbstractCSSValue cssval = factory.parseProperty(cssText);
		if (cssval == null || cssval.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE ||
				((CSSPrimitiveValue)cssval).getPrimitiveType() != CSSPrimitiveValue.CSS_ATTR) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Not an attr value.");
		}
		AttrValue attr = (AttrValue) cssval;
		this.attrname = attr.attrname;
		this.typeval = attr.typeval;
		this.fallback = attr.fallback;
		setPlainCssText(cssval.getCssText());
	}

	private void parseAttrValues(String attr) throws DOMException {
		int len = attr.length();
		int idx = attr.indexOf(',');
		int idxp1 = idx + 1;
		if (idxp1 == len) {
			badSyntax(attr);
		}
		if (idxp1 != 0) {
			ValueFactory factory = new ValueFactory(flags);
			fallback = factory.parseProperty(attr.substring(idxp1, len));
			if (fallback == null) {
				badSyntax(attr);
			}
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
			if (other.typeval != null) {
				return false;
			}
		} else if (!typeval.equals(other.typeval)) {
			return false;
		}
		return true;
	}

	@Override
	public AttrValue clone() {
		return new AttrValue(this);
	}

}
