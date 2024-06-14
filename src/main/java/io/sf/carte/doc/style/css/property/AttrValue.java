/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSAttrValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Attr value.
 * 
 */
public class AttrValue extends ProxyValue implements CSSAttrValue {

	private static final long serialVersionUID = 1L;

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
		if (fallback != null && (TypedValue.isOrContainsType(fallback, Type.ATTR)
				|| fallback.getCssValueType() == CssType.KEYWORD)) {
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
					"Invalid fallback: " + fallback.toString());
		}
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
		} else if ("percentage".equalsIgnoreCase(valueType)) {
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
	public Match matches(CSSValueSyntax syntax) {
		if (syntax.getCategory() == Category.universal) {
			return Match.TRUE;
		}
		//
		String dataType;
		if (typeval != null) {
			dataType = typeval.toLowerCase(Locale.ROOT);
		} else { // Implicit "string"
			dataType = "string";
		}
		//
		Match result = Match.FALSE;
		CSSValueSyntax comp = syntax;
		topLoop: do {
			boolean attrTypeMatch = ParseHelper.matchAttrType(dataType, comp.getCategory());
			if (attrTypeMatch) {
				result = Match.PENDING;
			}
			// Now check the fallback
			if (fallback != null) {
				CSSValueSyntax fallbackComp = syntax;
				do {
					Match match = fallback.matches(fallbackComp);
					if (match == Match.FALSE) {
						continue;
					} else if (match == Match.PENDING) {
						result = Match.PENDING;
						if (attrTypeMatch) {
							continue;
						}
					} else { // TRUE
						if (!attrTypeMatch) {
							result = Match.PENDING;
							// Perhaps we'll have better luck matching attr datatype with next syntax
							continue topLoop;
						} else {
							result = Match.TRUE;
						} // Here, attrTypeMatch is true and 'result' should be PENDING
					}
					return result;
				} while ((fallbackComp = fallbackComp.getNext()) != null);
			} else if (attrTypeMatch) {
				return Match.TRUE;
			}
		} while ((comp = comp.getNext()) != null);
		return result;
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		/*
		 * This method is not called from matches(), but is called from lists.
		 */
		if (syntax.getCategory() == Category.universal) {
			return Match.TRUE;
		}
		//
		String dataType;
		if (typeval != null) {
			dataType = typeval.toLowerCase(Locale.ROOT);
		} else { // Implicit "string"
			dataType = "string";
		}
		//
		Match result = Match.FALSE;
		boolean attrTypeMatch = ParseHelper.matchAttrType(dataType, syntax.getCategory());
		if (attrTypeMatch) {
			result = Match.PENDING;
		}
		// Now check the fallback
		if (fallback != null) {
			Match match = fallback.matchesComponent(syntax);
			if (match == Match.TRUE) {
				if (!attrTypeMatch) {
					result = Match.PENDING;
				} else {
					result = Match.TRUE;
				}
			} else if (match == Match.PENDING) {
				result = Match.PENDING;
			}
		} else if (attrTypeMatch) {
			result = Match.TRUE;
		}
		return result;
	}

	@Override
	LexicalSetter newLexicalSetter() {
		return new MyLexicalSetter();
	}

	class MyLexicalSetter extends LexicalSetter {

		@Override
		void setLexicalUnit(LexicalUnit lunit) {
			String attrname;
			String typeval = null;
			StyleValue fallback = null;

			LexicalUnit lu = lunit.getParameters();

			if (lu == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Invalid attr() name.");
			}

			LexicalType type = lu.getLexicalUnitType();
			if (type == LexicalType.IDENT) {
				attrname = lu.getStringValue();
			} else if (type == LexicalType.VAR) {
				// Cannot handle a var() before the comma
				throw new CSSLexicalProcessingException("var() inside attr() found.");
			} else {
				throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "Invalid attr() name.");
			}

			lu = lu.getNextLexicalUnit();
			if (lu != null) {
				type = lu.getLexicalUnitType();
				if (type != LexicalType.OPERATOR_COMMA) {
					// attr(name type
					if (type == LexicalType.VAR) {
						// Cannot handle a var() before the comma
						throw new CSSLexicalProcessingException("var() inside attr() found.");
					} else if (type == LexicalType.ATTR) {
						throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
								"Invalid attr() nested in attr().");
					}
					if (type == LexicalType.IDENT) {
						typeval = lu.getStringValue();
					} else if (type == LexicalType.OPERATOR_MOD) {
						typeval = "%";
					} else {
						throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
								"Invalid attr() name.");
					}
					lu = lu.getNextLexicalUnit();
					if (lu == null) {
						// attr(name type) and that's all
						AttrValue.this.attrname = attrname;
						AttrValue.this.typeval = typeval;
						AttrValue.this.fallback = null;
						nextLexicalUnit = lunit.getNextLexicalUnit();
						return;
					}
					type = lu.getLexicalUnitType();
					if (type != LexicalType.OPERATOR_COMMA) {
						wrongSyntax(lunit);
					}
				}
				// Move past comma
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					// Check the fallback
					type = lu.getLexicalUnitType();
					ValueFactory factory = new ValueFactory(flags);
					try {
						fallback = factory.createCSSValue(lu.clone(), null, false);
					} catch (RuntimeException e) {
						DOMException ex = new DOMException(DOMException.TYPE_MISMATCH_ERR,
								"Invalid fallback: " + fallback.toString());
						ex.initCause(e);
						throw ex;
					}
				}
			}

			// Everything is fine, so assign the fields starting
			// with the fallback which may trigger an error.
			setFallback(fallback);
			AttrValue.this.attrname = attrname;
			AttrValue.this.typeval = typeval;

			nextLexicalUnit = lunit.getNextLexicalUnit();
		}
	}

	private static void wrongSyntax(LexicalUnit lunit) {
		throw new DOMException(DOMException.SYNTAX_ERR, "Wrong attr() syntax: " + lunit.toString());
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
		CSSParser parser = new CSSParser();
		LexicalUnit lunit;
		try {
			lunit = parser.parsePropertyValue(new StringReader(cssText));
		} catch (CSSParseException e) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Not an attr value.");
		} catch (IOException e) {
			return;
		}
		newLexicalSetter().setLexicalUnit(lunit);
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
