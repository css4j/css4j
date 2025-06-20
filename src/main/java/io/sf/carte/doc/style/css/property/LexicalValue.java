/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSLexicalValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.impl.AttrUtil;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.om.CSSOMParser;
import io.sf.carte.util.SimpleWriter;

/**
 * A value that contains a {@code var} value as part of its declaration.
 */
public class LexicalValue extends ProxyValue implements CSSLexicalValue {

	private static final long serialVersionUID = 1L;

	private LexicalUnit lexicalUnit;

	public LexicalValue() {
		super(Type.LEXICAL);
	}

	protected LexicalValue(LexicalValue copied) {
		super(copied);
		lexicalUnit = copied.lexicalUnit.clone();
	}

	@Override
	public LexicalUnit getLexicalUnit() {
		return lexicalUnit;
	}

	public void setLexicalUnit(LexicalUnit lexicalUnit) {
		this.lexicalUnit = lexicalUnit;
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
			type = AttrUtil.finalAttrType(lexicalUnit);
			break;
		case STRING:
			type = Type.STRING;
			break;
		case URI:
			type = Type.URI;
			break;
		case SRC:
			type = Type.SRC;
			break;
		case DIMENSION:
		case PERCENTAGE:
		case REAL:
		case INTEGER:
			type = Type.NUMERIC;
			break;
		case RGBCOLOR:
		case HSLCOLOR:
		case LABCOLOR:
		case LCHCOLOR:
		case OKLABCOLOR:
		case OKLCHCOLOR:
		case HWBCOLOR:
		case COLOR_FUNCTION:
			type = Type.COLOR;
			break;
		case CALC:
			type = Type.EXPRESSION;
			break;
		case FUNCTION:
		case PREFIXED_FUNCTION:
			type = Type.FUNCTION;
			break;
		case GRADIENT:
			type = Type.GRADIENT;
			break;
		case MATH_FUNCTION:
			type = Type.MATH_FUNCTION;
			break;
		case COLOR_MIX:
			type = Type.COLOR_MIX;
			break;
		case CUBIC_BEZIER_FUNCTION:
			type = Type.CUBIC_BEZIER;
			break;
		case STEPS_FUNCTION:
			type = Type.STEPS;
			break;
		case RECT_FUNCTION:
			type = Type.RECT;
			break;
		case CIRCLE_FUNCTION:
			type = Type.CIRCLE;
			break;
		case ELLIPSE_FUNCTION:
			type = Type.ELLIPSE;
			break;
		case INSET_FUNCTION:
			type = Type.INSET;
			break;
		case PATH_FUNCTION:
			type = Type.PATH;
			break;
		case POLYGON_FUNCTION:
			type = Type.POLYGON;
			break;
		case SHAPE_FUNCTION:
			type = Type.SHAPE;
			break;
		case XYWH_FUNCTION:
			type = Type.XYWH;
			break;
		case COUNTER_FUNCTION:
			type = Type.COUNTER;
			break;
		case COUNTERS_FUNCTION:
			type = Type.COUNTERS;
			break;
		case ENV:
			type = Type.ENV;
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
		return lutype == LexicalType.INTEGER || lutype == LexicalType.REAL || lutype == LexicalType.CALC
				|| lutype == LexicalType.MATH_FUNCTION;
	}

	@Override
	public CSSValueSyntax.Match matches(CSSValueSyntax syntax) {
		return lexicalUnit.matches(syntax);
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		CSSValueSyntax synComp = syntax.shallowClone();
		return lexicalUnit.matches(synComp);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		checkModifiableProperty();
		CSSOMParser parser = new CSSOMParser();
		try {
			lexicalUnit = parser.parsePropertyValue(new StringReader(cssText));
		} catch (CSSException | IOException e) {
			throw new DOMSyntaxException(e);
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
	public String getMinifiedCssText(String propertyName) {
		return serializeMinifiedSequence(lexicalUnit, propertyName);
	}

	public static String serializeMinifiedSequence(LexicalUnit lexicalUnit) {
		return serializeMinifiedSequence(lexicalUnit, null);
	}

	public static String serializeMinifiedSequence(LexicalUnit lexicalUnit, String propertyName) {
		if (lexicalUnit.getNextLexicalUnit() == null) {
			// Save a buffer creation
			return serializeMinified(lexicalUnit, propertyName).toString();
		}
		StringBuilder buf = new StringBuilder(32);
		LexicalUnit lu = lexicalUnit;
		boolean needSpaces = false;
		while (lu != null) {
			switch(lu.getLexicalUnitType()) {
			case OPERATOR_EXP:
			case OPERATOR_GE:
			case OPERATOR_GT:
			case OPERATOR_LE:
			case OPERATOR_LT:
			case OPERATOR_MULTIPLY:
			case OPERATOR_SLASH:
			case OPERATOR_TILDE:
			case LEFT_BRACKET:
			case OPERATOR_COMMA:
			case OPERATOR_SEMICOLON:
			case OPERATOR_COLON:
				needSpaces = false;
				break;
			case RIGHT_BRACKET:
				needSpaces = true;
				break;
			default:
				if (needSpaces) {
					buf.append(' ');
				} else {
					needSpaces = true;
				}
			}
			buf.append(serializeMinified(lu, propertyName));
			lu = lu.getNextLexicalUnit();
		}
		return buf.toString();
	}

	private static CharSequence serializeMinified(LexicalUnit lexicalUnit, String propertyName) {
		if (lexicalUnit.getParameters() != null) {
			StringBuilder buf;
			switch (lexicalUnit.getLexicalUnitType()) {
			case RGBCOLOR:
				String cssText = lexicalUnit.getCssText();
				if (cssText.length() < 10) {
					// Hex notation
					cssText = ColorMinifier.getInstance().minifyHex(cssText);
					return cssText;
				}
			default:
				buf = new StringBuilder();
				buf.append(lexicalUnit.getFunctionName()).append('(');
				LexicalUnit lu = lexicalUnit.getParameters();
				if (lu != null) {
					buf.append(serializeMinifiedSequence(lu));
				}
				buf.append(')');
				return buf;
			case SUB_EXPRESSION:
				buf = new StringBuilder();
				buf.append('(');
				lu = lexicalUnit.getSubValues();
				if (lu != null) {
					buf.append(serializeMinifiedSequence(lu));
				}
				buf.append(')');
				return buf;
			case EMPTY:
				/*
				 * It is encouraged to serialize comments of empty values, to avoid re-parsing
				 * issues.
				 */
				StringList pre = lexicalUnit.getPrecedingComments();
				if (pre != null) {
					buf = new StringBuilder();
					writeComments(pre, buf);
					return buf;
				}
				StringList after = lexicalUnit.getTrailingComments();
				if (after != null) {
					buf = new StringBuilder();
					writeComments(after, buf);
					return buf;
				}
				return "";
			case ELEMENT_REFERENCE:
				break;
			}
		} else {
			switch (lexicalUnit.getLexicalUnitType()) {
			case URI:
				String s = lexicalUnit.getStringValue();
				if (urlCanBeUnquoted(s)) {
					return "url(" + s + ')';
				}
				break;
			case DIMENSION:
				float f = lexicalUnit.getFloatValue();
				if (f == 0f) {
					if (!lexicalUnit.isParameter()
							&& CSSUnit.isLengthUnitType(lexicalUnit.getCssUnit())) {
						return "0";
					}
				} else if (Float.isInfinite(f)) {
					return serializeInfinite(f, lexicalUnit.getDimensionUnitText());
				}
				return serializeNumber(f, lexicalUnit.getDimensionUnitText());
			case REAL:
			case PERCENTAGE:
				f = lexicalUnit.getFloatValue();
				if (Float.isInfinite(f)) {
					return serializeInfinite(f, lexicalUnit.getDimensionUnitText());
				}
				return serializeNumber(f, lexicalUnit.getDimensionUnitText());
			case IDENT:
				s = lexicalUnit.getStringValue();
				String color = ColorIdentifiers.getInstance().getColor(s);
				if (color != null && color.length() < s.length()) {
					s = color;
				}
				return s;
			case INITIAL:
				if (propertyName != null && !propertyName.isEmpty() && !lexicalUnit.isParameter()) {
					StyleValue ini = PropertyDatabase.getInstance().getInitialValue(propertyName);
					if (ini != null) {
						String mini = ini.getMinifiedCssText();
						String text = lexicalUnit.getCssText();
						if (mini.length() < text.length()) {
							return mini;
						}
						return text;
					}
				}
				break;
			default:
				break;
			}
		}
		return lexicalUnit.getCssText();
	}

	static boolean urlCanBeUnquoted(String s) {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c <= 0x20 || Character.isISOControl(c)) {
				return false;
			}
			switch (c) {
			case 0x22: // "
			case 0x27: // '
			case 0x28: // (
			case 0x29: // )
			case 0x5b: // [
			case 0x5d: // ]
			case 0x7b: // {
			case 0x7d: // }
				return false;
			default:
			}
		}
		return true;
	}

	private static String serializeInfinite(float realvalue, String unitText) {
		if (realvalue > 0f) {
			return "calc(1" + unitText + "/0)";
		} else {
			return "calc(-1" + unitText + "/0)";
		}
	}

	private static CharSequence serializeNumber(float f, String unitText) {
		String s;
		float rintValue = (float) Math.rint(f);
		if (f == rintValue) {
			s = Integer.toString((int) rintValue);
		} else {
			s = Float.toString(f);
		}
		int len = s.length();
		StringBuilder buf = new StringBuilder(len + unitText.length());
		char c = s.charAt(0);
		if (c == '-' && s.charAt(1) == '0') {
			buf.append('-');
			buf.append(s.subSequence(2, len));
		} else if (c == '0' && len > 2) {
			buf.append(s.subSequence(1, len));
		} else {
			buf.append(s);
		}
		buf.append(unitText);
		return buf;
	}

	private static void writeComments(StringList pre, StringBuilder buf) {
		for (String c : pre) {
			buf.append("/*");
			buf.append(c);
			buf.append("*/");
		}
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
