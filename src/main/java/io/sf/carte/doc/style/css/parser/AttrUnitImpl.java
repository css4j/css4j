/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Locale;

import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.UnitStringToId;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class AttrUnitImpl extends FunctionUnitImpl {

	private static final long serialVersionUID = 1L;

	public AttrUnitImpl() {
		super(LexicalType.ATTR);
	}

	@Override
	AttrUnitImpl instantiateLexicalUnit() {
		return new AttrUnitImpl();
	}

	@Override
	Match typeMatch(CSSValueSyntax rootSyntax, CSSValueSyntax syntax) {
		Match result = Match.FALSE;
		LexicalUnit param = getParameters();
		if (param != null) {
			LexicalUnit fallback = null;
			param = param.getNextLexicalUnit();
			if (param == null) {
				// Implicit "string"
				return syntax.getCategory() == Category.string ? Match.TRUE : Match.FALSE;
			}

			short unit = CSSUnit.CSS_OTHER;
			CSSValueSyntax attrSyntax = null;
			LexicalType luType = param.getLexicalUnitType();
			if (luType == LexicalType.OPERATOR_COMMA) {
				attrSyntax = SyntaxParser.createSimpleSyntax("string");
				fallback = param.getNextLexicalUnit();
			} else {
				switch (luType) {
				case OPERATOR_COMMA:
					break;
				case TYPE_FUNCTION:
					LexicalUnit typeParam = param.getParameters();
					try {
						attrSyntax = typeParam.getSyntax();
					} catch (IllegalStateException e) {
						return Match.FALSE; // Error
					}
					break;
				case IDENT:
					// We got a data type spec
					String s = param.getStringValue().toLowerCase(Locale.ROOT);
					if ("string".equals(s)) {
						attrSyntax = SyntaxParser.createSimpleSyntax("string");
					} else {
						unit = UnitStringToId.unitFromString(s);
					}
					break;
				case OPERATOR_MOD:
					unit = CSSUnit.CSS_PERCENTAGE;
					break;
				case VAR:
					return Match.PENDING;
				default:
					return Match.FALSE; // Should never happen
				}
				param = param.getNextLexicalUnit();
				if (param != null) {
					if (param.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
						return Match.FALSE; // Should never happen
					}
					fallback = param.getNextLexicalUnit();
				}
			}

			// Now, let's try matching the attr datatype and the fallback (if available).
			CSSValueSyntax typeMatch = null;
			CSSValueSyntax comp = rootSyntax;
			topLoop: do {
				Match attrTypeMatch;
				if (attrSyntax == null) {
					if (unitMatchesCategory(unit, comp.getCategory())) {
						attrTypeMatch = Match.TRUE;
						result = Match.PENDING;
						typeMatch = comp;
					} else {
						attrTypeMatch = Match.FALSE;
					}
				} else {
					attrTypeMatch = matchAttrTypeSyntax(isParameter(), attrSyntax, comp.getCategory());
					if (attrTypeMatch != Match.FALSE) {
						result = Match.PENDING;
						typeMatch = comp;
					}
				}
				//
				if (fallback != null) {
					// We got a fallback
					CSSValueSyntax fallbackComp = rootSyntax;
					do {
						Match match = fallback.matches(fallbackComp);
						if (match == Match.FALSE) {
							// url is a special case
							if (attrTypeMatch != Match.FALSE && attrSyntax != null
									&& attrSyntax.getCategory() == Category.url
									&& fallback.getLexicalUnitType() == LexicalType.STRING
									&& hasNoSiblings(this)
									&& fallback.getNextLexicalUnit() == null) {
								result = Match.TRUE;
							} else {
								continue;
							}
						} else if (match == Match.PENDING) {
							result = Match.PENDING;
							if (attrTypeMatch != Match.FALSE) {
								continue;
							}
						} else { // TRUE
							if (attrTypeMatch != Match.TRUE) {
								result = Match.PENDING;
								// Perhaps we'll have better luck matching attr datatype with next syntax
								continue topLoop;
							} else if (hasNoSiblings(this) || (typeMatch == fallbackComp)) {
								result = Match.TRUE;
							} // Here, attrTypeMatch is true and 'result' should be PENDING
						}
						return result;
					} while ((fallbackComp = fallbackComp.getNext()) != null);
				} else if (attrTypeMatch == Match.TRUE) {
					return Match.TRUE;
				}
			} while ((comp = comp.getNext()) != null);
		}
		return result;
	}

	/**
	 * Match the type syntax of an {@code attr()} value.
	 * 
	 * @param calcContext {@code true} if we are in {@code calc()} context.
	 * @param typeSyntax  the syntax of the type being checked.
	 * @param cat         the grammar data type category to check.
	 * @return true if the type category matches the syntax category.
	 */
	private static Match matchAttrTypeSyntax(boolean calcContext, CSSValueSyntax typeSyntax,
			Category cat) {
		Match expected = Match.FALSE;
		do {
			Category typeCategory = typeSyntax.getCategory();
			Match match = categoryMatch(calcContext, false, typeCategory, cat);
			if (match != Match.FALSE) {
				if (expected != Match.PENDING) {
					expected = Match.TRUE;
					break;
				}
			} else if (expected == Match.TRUE) {
				expected = Match.PENDING;
			}
			typeSyntax = typeSyntax.getNext();
		} while (typeSyntax != null);
		return expected;
	}

	private static boolean hasNoSiblings(LexicalUnit lexicalUnit) {
		return lexicalUnit.getNextLexicalUnit() == null
				&& lexicalUnit.getPreviousLexicalUnit() == null;
	}

}
