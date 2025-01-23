/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.impl;

import java.util.Locale;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.UnitStringToId;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

/**
 * Utility methods related to attribute values.
 */
public class AttrUtil {

	/**
	 * Determine whether the final type of an attr() could be only one, by comparing
	 * the attribute type to the fallback value, if any.
	 * 
	 * @param lunit the attr() value.
	 * @return {@code true} if the final value type cannot be determined now.
	 */
	public static boolean isProxyAttr(LexicalUnit lunit) {
		return finalAttrType(lunit) == Type.UNKNOWN;
	}

	/**
	 * Determine the CSSOM type of the given {@code attr()} lexical unit.
	 * 
	 * @param lunit the lexical unit to test.
	 * @return the CSSOM type.
	 */
	public static Type finalAttrType(LexicalUnit lunit) {
		LexicalUnit nextParam = lunit.getParameters();
		if (nextParam == null || nextParam.getLexicalUnitType() != LexicalType.IDENT) {
			// Error
			return Type.UNKNOWN;
		}
		nextParam = nextParam.getNextLexicalUnit();
		if (nextParam == null) {
			// No attribute type, no fallback
			return Type.STRING;
		}

		CSSValueSyntax attrSyntax = null;
		short unit = CSSUnit.CSS_OTHER;
		LexicalType type = nextParam.getLexicalUnitType();
		if (type != LexicalType.OPERATOR_COMMA) {
			// Obtain the attribute type
			if (type == LexicalType.IDENT) {
				String attrUnit = nextParam.getStringValue().toLowerCase(Locale.ROOT);
				if (!"string".equals(attrUnit)) {
					unit = UnitStringToId.unitFromString(attrUnit);
				} else {
					attrSyntax = SyntaxParser.createSimpleSyntax("string");
				}
			} else if (type == LexicalType.OPERATOR_MOD) {
				unit = CSSUnit.CSS_PERCENTAGE;
			} else if (type == LexicalType.TYPE_FUNCTION) {
				attrSyntax = nextParam.getParameters().getSyntax();
			} else {
				// Probably error
				return Type.UNKNOWN;
			}

			nextParam = nextParam.getNextLexicalUnit();
			if (nextParam == null) {
				// No fallback
				return attrTypeToCSSOMType(unit, attrSyntax);
			}
			if (nextParam.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
				// Syntax error
				return Type.UNKNOWN;
			}
		} else {
			attrSyntax = SyntaxParser.createSimpleSyntax("string");
		}

		LexicalUnit fallback = nextParam.getNextLexicalUnit();
		if (fallback == null) {
			return attrTypeToCSSOMType(unit, attrSyntax);
		}

		// We got a fallback
		if (!valueFitsAttrType(fallback, unit, attrSyntax)) {
			return Type.UNKNOWN;
		}

		return attrTypeToCSSOMType(unit, attrSyntax);
	}

	private static Type attrTypeToCSSOMType(short unit, CSSValueSyntax attrSyntax) {
		if (attrSyntax == null) {
			return unit == CSSUnit.CSS_OTHER ? Type.UNKNOWN : Type.NUMERIC;
		}

		Type finalType;
		switch (attrSyntax.getCategory()) {
		case string:
			finalType = Type.STRING;
			break;
		case url:
			finalType = Type.URI;
			break;
		case customIdent:
			finalType = Type.IDENT;
			break;
		case color:
			finalType = Type.COLOR;
			break;
		case number:
		case percentage:
		case length:
		case angle:
		case time:
		case frequency:
		case flex:
			finalType = Type.NUMERIC;
			break;
		default:
			finalType = Type.UNKNOWN;
		}

		return finalType;
	}

	private static boolean valueFitsAttrType(LexicalUnit value, short unit,
			CSSValueSyntax attrSyntax) {
		LexicalType luType = value.getLexicalUnitType();
		if (attrSyntax == null) {
			return luType == LexicalType.DIMENSION || luType == LexicalType.REAL
					|| luType == LexicalType.PERCENTAGE || luType == LexicalType.INTEGER;
		}

		return value.matches(attrSyntax) == Match.TRUE;
	}

}
