/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSLexicalValue;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.om.CSSOMParser;
import io.sf.carte.util.SimpleWriter;

/**
 * A value that contains a {@code var} value as part of its declaration.
 */
public class LexicalValue extends ProxyValue implements CSSLexicalValue {

	private LexicalUnit lexicalUnit;

	public LexicalValue() {
		super(Type.LEXICAL);
	}

	protected LexicalValue(LexicalValue copied) {
		super(copied);
		lexicalUnit = copied.lexicalUnit;
	}

	@Override
	public LexicalUnit getLexicalUnit() {
		return lexicalUnit;
	}

	@Override
	public Type getFinalType() {
		// Check for ratio
		LexicalUnit nlu = lexicalUnit.getNextLexicalUnit();
		if (nlu != null) {
			if (nlu.getLexicalUnitType() == LexicalType.OPERATOR_SLASH
					&& isRatioCompUnit(lexicalUnit.getLexicalUnitType()) && (nlu = nlu.getNextLexicalUnit()) != null
					&& isRatioCompUnit(nlu.getLexicalUnitType())) {
				return Type.RATIO;
			}
			return Type.UNKNOWN;
		}
		Type type;
		switch (lexicalUnit.getLexicalUnitType()) {
		case IDENT:
			type = Type.IDENT;
			break;
		case ATTR:
			type = Type.ATTR;
			break;
		case STRING:
			type = Type.STRING;
			break;
		case URI:
			type = Type.URI;
			break;
		case DIMENSION:
		case PERCENTAGE:
		case REAL:
		case INTEGER:
			type = Type.NUMERIC;
			break;
		case RGBCOLOR:
		case HSLCOLOR:
			type = Type.COLOR;
			break;
		case FUNCTION:
			String func = lexicalUnit.getFunctionName().toLowerCase(Locale.ROOT);
			if ("hwb".equals(func) || "color".equals(func)) {
				type = Type.COLOR;
			} else if ("calc".equals(func)) {
				type = Type.EXPRESSION;
			} else if (func.endsWith("linear-gradient") || func.endsWith("radial-gradient")
					|| func.endsWith("conic-gradient")) {
				type = Type.GRADIENT;
			} else if ("env".equals(func)) {
				type = Type.ENV;
			} else {
				type = Type.FUNCTION;
			}
			break;
		case UNICODE_RANGE:
			type = Type.UNICODE_RANGE;
			break;
		case UNICODE_WILDCARD:
			type = Type.UNICODE_WILDCARD;
			break;
		case RECT_FUNCTION:
			type = Type.RECT;
			break;
		case COUNTER_FUNCTION:
			type = Type.COUNTER;
			break;
		case COUNTERS_FUNCTION:
			type = Type.COUNTERS;
			break;
		case ELEMENT_REFERENCE:
			type = Type.ELEMENT_REFERENCE;
			break;
		default:
			type = Type.UNKNOWN;
		}
		return type;
	}

	private boolean isRatioCompUnit(LexicalType lutype) {
		return lutype == LexicalType.INTEGER || lutype == LexicalType.REAL
				|| lutype == LexicalType.FUNCTION;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		CSSOMParser parser = new CSSOMParser();
		try {
			lexicalUnit = parser.parsePropertyValue(new StringReader(cssText));
		} catch (CSSException | IOException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	@Override
	public String getCssText() {
		return lexicalUnit.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		wri.write(getCssText());
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
		LexicalValue other = (LexicalValue) obj;
		return getCssText().equals(other.getCssText());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getCssText().hashCode();
		return result;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			lexicalUnit = lunit;
		}
	}

	@Override
	public LexicalValue clone() {
		return new LexicalValue(this);
	}

}
