/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.impl;

import java.util.Locale;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.parser.SyntaxParser;

/**
 * Utility methods related to attribute values.
 */
public class AttrUtil {

	/**
	 * Determine whether the final type of an attr() could be only one, by comparing
	 * the attribute type to the fallback value, if any.
	 * 
	 * @param lunit
	 * @return
	 */
	public static boolean isProxyAttr(LexicalUnit lunit) {
		LexicalUnit nextParam = lunit.getParameters().getNextLexicalUnit();
		if (nextParam == null) {
			// No attribute type, no fallback
			return false;
		}
		String attrType;
		LexicalType type = nextParam.getLexicalUnitType();
		if (type == LexicalType.OPERATOR_COMMA) {
			attrType = "string";
			nextParam = nextParam.getNextLexicalUnit();
			if (nextParam == null) {
				// Possible syntax error
				return false;
			}
		} else {
			LexicalUnit postType = nextParam.getNextLexicalUnit();
			if (postType == null) {
				// No fallback
				return false;
			}
			// Obtain a string with the attribute type
			if (type == LexicalType.IDENT) {
				attrType = nextParam.getStringValue().toLowerCase(Locale.ROOT);
			} else if (type == LexicalType.OPERATOR_MOD) {
				attrType = "percentage";
			} else {
				// Probably error
				return false;
			}
			if (postType.getLexicalUnitType() != LexicalType.OPERATOR_COMMA
					|| (nextParam = postType.getNextLexicalUnit()) == null) {
				// Syntax error
				return false;
			}
		}

		// We got the fallback in nextParam
		return !unitFitsAttrType(nextParam, attrType);
	}

	/**
	 * Determine whether the lexical unit is of a type compatible with the lower
	 * case attrtype.
	 * 
	 * @param lunit    the lexical unit to test.
	 * @param attrtype the attribute type (in lower case letters).
	 * @return true if the lexical unit is of the same type or has a compatible
	 *         unit.
	 */
	public static boolean unitFitsAttrType(LexicalUnit lunit, String attrtype) {
		if ("ident".equalsIgnoreCase(attrtype)) {
			attrtype = "custom-ident";
		}
		CSSValueSyntax syn = SyntaxParser.createSimpleSyntax(attrtype);
		if (syn != null) {
			return lunit.matches(syn) == Match.TRUE
					|| (lunit.getLexicalUnitType() == LexicalType.STRING && attrtype.equals("url"));
		}

		// Could be an unit suffix, or an error
		attrtype = attrtype.intern();
		short declUnit = ParseHelper.unitFromString(attrtype);
		short fbUnit = lunit.getCssUnit();

		return declUnit != CSSUnit.CSS_OTHER && fbUnit != CSSUnit.CSS_INVALID;
	}

}
