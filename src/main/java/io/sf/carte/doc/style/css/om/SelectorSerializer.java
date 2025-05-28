/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorCondition;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ConditionalSelector;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.LangCondition;
import io.sf.carte.doc.style.css.nsac.PositionalCondition;
import io.sf.carte.doc.style.css.nsac.PseudoCondition;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SheetContext;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.jclf.text.TokenParser;

/**
 * Selector serializer.
 */
class SelectorSerializer {

	private final SheetContext parentSheet;

	/**
	 * Construct a serializer.
	 */
	public SelectorSerializer(SheetContext parentSheet) {
		super();
		this.parentSheet = parentSheet;
	}

	private SheetContext getSheetContext() {
		return parentSheet;
	}

	void selectorText(StringBuilder buf, Selector sel, boolean omitUniversal) {
		selectorText(buf, sel, omitUniversal, false);
	}

	private void selectorText(final StringBuilder buf, Selector sel, boolean omitUniversal,
			boolean scoped) {
		switch (sel.getSelectorType()) {
		case UNIVERSAL:
			if (!omitUniversal) {
				buf.append('*');
			}
			break;
		case ELEMENT:
			ElementSelector esel = (ElementSelector) sel;
			String lname = esel.getLocalName();
			String nsuri = esel.getNamespaceURI();
			if (lname != null) {
				lname = ParseHelper.escape(lname, false, false);
			}
			if (nsuri != null) {
				if (!nsuri.isEmpty()) {
					String nsprefix = getSheetContext().getNamespacePrefix(esel.getNamespaceURI());
					if (nsprefix == null) {
						throw new IllegalStateException(
								"Unknown ns prefix for URI " + esel.getNamespaceURI());
					}
					// Append prefix if not empty, otherwise is Default namespace
					if (!nsprefix.isEmpty()) {
						buf.append(nsprefix).append('|');
					}
				} else {
					buf.append('|');
				}
				buf.append(lname);
			} else {
				SheetContext psheet = getSheetContext();
				if (psheet != null && psheet.hasDefaultNamespace()) {
					buf.append("*|");
					if (lname != null) {
						buf.append(lname);
					} else {
						buf.append('*');
					}
				} else if (lname != null) {
					buf.append(lname);
				} else if (!omitUniversal) {
					buf.append('*');
				}
			}
			break;
		case CHILD:
			CombinatorSelector dsel = (CombinatorSelector) sel;
			Selector ancsel = dsel.getSelector();
			if (!scoped || ancsel.getSelectorType() != Selector.SelectorType.UNIVERSAL) {
				selectorText(buf, ancsel, false, scoped);
			}
			buf.append('>');
			selectorText(buf, dsel.getSecondSelector(), false, scoped);
			break;
		case CONDITIONAL:
			ConditionalSelector csel = (ConditionalSelector) sel;
			SimpleSelector simpleSelector = csel.getSimpleSelector();
			if (simpleSelector != null) {
				appendSimpleSelector(simpleSelector, buf);
			}
			conditionalSelectorText(csel.getCondition(), buf);
			break;
		case DESCENDANT:
			dsel = (CombinatorSelector) sel;
			Selector ancestor = dsel.getSelector();
			selectorText(buf, ancestor, false, scoped);
			buf.append(' ');
			selectorText(buf, dsel.getSecondSelector(), false, scoped);
			break;
		case DIRECT_ADJACENT:
			CombinatorSelector asel = (CombinatorSelector) sel;
			selectorText(buf, asel.getSelector(), omitUniversal, scoped);
			buf.append('+');
			selectorText(buf, asel.getSecondSelector(), false, scoped);
			break;
		case SUBSEQUENT_SIBLING:
			asel = (CombinatorSelector) sel;
			selectorText(buf, asel.getSelector(), omitUniversal, scoped);
			buf.append('~');
			selectorText(buf, asel.getSecondSelector(), false, scoped);
			break;
		case COLUMN_COMBINATOR:
			dsel = (CombinatorSelector) sel;
			selectorText(buf, dsel.getSelector(), omitUniversal, scoped);
			buf.append("||");
			selectorText(buf, dsel.getSecondSelector(), false, scoped);
			break;
		case SCOPE_MARKER:
			break;
		default:
			throw new IllegalStateException("Unknown selector: " + sel.toString());
		}
	}

	private void conditionalSelectorText(Condition condition, final StringBuilder buf) {
		switch (condition.getConditionType()) {
		case CLASS:
			classText((AttributeCondition) condition, buf);
			break;
		case ID:
			String id = ((AttributeCondition) condition).getValue();
			buf.append('#').append(ParseHelper.escape(id, false, false));
			break;
		case ATTRIBUTE:
			attributeText((AttributeCondition) condition, buf);
			break;
		case BEGINS_ATTRIBUTE:
			attributeBeginsText((AttributeCondition) condition, buf);
			break;
		case BEGIN_HYPHEN_ATTRIBUTE:
			attributeBeginHyphenText((AttributeCondition) condition, buf);
			break;
		case ENDS_ATTRIBUTE:
			attributeEndsText((AttributeCondition) condition, buf);
			break;
		case SUBSTRING_ATTRIBUTE:
			attributeSubstringText((AttributeCondition) condition, buf);
			break;
		case LANG:
			langText((LangCondition) condition, buf);
			break;
		case ONE_OF_ATTRIBUTE:
			attributeOneOfText((AttributeCondition) condition, buf);
			break;
		case ONLY_CHILD:
			buf.append(":only-child");
			break;
		case ONLY_TYPE:
			buf.append(":only-of-type");
			break;
		case POSITIONAL:
			PositionalCondition pcond = (PositionalCondition) condition;
			buf.append(':');
			if (pcond.isOfType()) {
				appendPositionalOfType(pcond, buf);
			} else {
				appendPositional(pcond, buf);
			}
			break;
		case AND:
			CombinatorCondition ccond = (CombinatorCondition) condition;
			int len = ccond.getLength();
			for (int i = 0; i < len; i++) {
				conditionalSelectorText(ccond.getCondition(i), buf);
			}
			break;
		case PSEUDO_CLASS:
			pseudoClassText((PseudoCondition) condition, buf);
			break;
		case PSEUDO_ELEMENT:
			pseudoElementText((PseudoCondition) condition, buf);
			break;
		case SELECTOR_ARGUMENT:
			selectorArgumentText((ArgumentCondition) condition, buf);
			break;
		case NESTING:
			buf.append('&');
			break;
		default:
			// throw exception to ease the identification of unhandled cases.
			throw new IllegalStateException("Unknown condition: " + condition.toString());
		}
	}

	private void appendSimpleSelector(SimpleSelector simpleSelector, StringBuilder buf) {
		selectorText(buf, simpleSelector, true);
	}

	private void classText(AttributeCondition acond, StringBuilder buf) {
		buf.append(".").append(ParseHelper.escape(acond.getValue(), false, false));
	}

	private void pseudoClassText(PseudoCondition acond, StringBuilder buf) {
		buf.append(':');
		String name = acond.getName();
		String value = acond.getArgument();
		if (name == null) {
			buf.append(value);
		} else {
			buf.append(name);
			if (value != null) {
				buf.append('(');
				buf.append(value);
				buf.append(')');
			}
		}
	}

	private String attributeText(AttributeCondition acond, StringBuilder buf) {
		String value = acond.getValue();
		if (value != null) {
			buf.append('[');
			serializeAttributeQName(acond, buf);
			buf.append('=');
			quoteAttributeValue(acond.getValue(), buf);
			attributeSelectorEnd(acond, buf);
		} else {
			buf.append('[');
			serializeAttributeQName(acond, buf);
			buf.append(']');
		}
		return buf.toString();
	}

	private void attributeBeginsText(AttributeCondition acond, StringBuilder buf) {
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("^=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
	}

	private void attributeBeginHyphenText(AttributeCondition acond, StringBuilder buf) {
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("|=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
	}

	private void attributeEndsText(AttributeCondition acond, StringBuilder buf) {
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("$=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
	}

	private void attributeSubstringText(AttributeCondition acond, StringBuilder buf) {
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("*=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
	}

	private void attributeOneOfText(AttributeCondition acond, StringBuilder buf) {
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("~=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
	}

	private void serializeAttributeQName(AttributeCondition acond, StringBuilder buf) {
		String nsuri = acond.getNamespaceURI();
		if (nsuri != null) {
			if (nsuri.length() != 0) {
				String nsprefix = getSheetContext().getNamespacePrefix(nsuri);
				if (nsprefix == null) {
					throw new IllegalStateException("Unknown ns prefix for URI " + nsuri);
				}
				if (nsprefix.length() != 0) {
					buf.append(nsprefix).append('|');
				}
			} else {
				buf.append('|');
			}
		}
		String escLName = ParseHelper.escape(acond.getLocalName(), false, false);
		buf.append(escLName);
	}

	private void quoteAttributeValue(String value, StringBuilder buf) {
		char quote = quoteChar(true);
		buf.append(ParseHelper.quote(value, quote));
	}

	private void attributeSelectorEnd(AttributeCondition acond, StringBuilder buf) {
		if (acond.hasFlag(AttributeCondition.Flag.CASE_I)) {
			buf.append(" i");
		} else if (acond.hasFlag(AttributeCondition.Flag.CASE_S)) {
			buf.append(" s");
		}
		buf.append(']');
	}

	private void langText(LangCondition condition, StringBuilder buf) {
		buf.append(":lang(");
		String lang = condition.getLang();
		TokenParser parser = new TokenParser(lang, ", ", "\"'");
		String s = parser.next();
		int commaIdx = lang.indexOf(',') + 1;
		buf.append(escapeLang(s, lang, commaIdx));
		while (parser.hasNext()) {
			s = parser.next();
			commaIdx = lang.indexOf(',', commaIdx) + 1;
			buf.append(',').append(escapeLang(s, lang, commaIdx));
		}
		buf.append(')');
	}

	private String escapeLang(String s, String lang, int commaIdx) {
		int nextCommaIdx = lang.indexOf(',', commaIdx) + 1;
		int nextDQIdx = lang.indexOf('"', commaIdx);
		int nextSQIdx = lang.indexOf('\'', commaIdx);
		boolean noDQ = nextDQIdx == -1 || nextDQIdx > nextCommaIdx;
		CharSequence escaped;
		if (s.indexOf(' ') != -1) {
			char quote = quoteChar(noDQ);
			s = ParseHelper.quote(s, quote);
		} else if ((escaped = ParseHelper.escapeCssCharsAndFirstChar(s)) != s) {
			boolean noSQ = nextSQIdx == -1 || nextSQIdx > nextCommaIdx;
			if (escaped.length() < s.length() + 2 && noDQ && noSQ) {
				s = escaped.toString();
			} else {
				char quote = quoteChar(noDQ);
				s = ParseHelper.quote(s, quote);
			}
		}
		return s;
	}

	private char quoteChar(boolean noDQ) {
		char quote;
		SheetContext sheet = getSheetContext();
		if (sheet != null) {
			if (sheet.hasFactoryFlag(CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE)) {
				quote = '"';
			} else if (sheet.hasFactoryFlag(CSSStyleSheetFactory.FLAG_STRING_SINGLE_QUOTE)) {
				quote = '\'';
			} else {
				quote = noDQ ? '\'' : '"';
			}
		} else {
			quote = noDQ ? '\'' : '"';
		}
		return quote;
	}

	private void pseudoElementText(PseudoCondition acond, StringBuilder buf) {
		buf.append(':').append(':').append(acond.getName());
	}

	private void selectorArgumentText(ArgumentCondition condition, StringBuilder buf) {
		buf.append(':').append(condition.getName()).append("(");
		selectorListText(buf, condition.getSelectors(), false, true);
		buf.append(')');
	}

	private void appendPositional(PositionalCondition pcond, StringBuilder buf) {
		int slope = pcond.getFactor();
		int offset = pcond.getOffset();
		SelectorList ofList = pcond.getOfList();
		boolean forwardCondition = pcond.isForwardCondition();
		if (slope == 0) {
			if (offset == 1 && ofList == null && !pcond.hasArgument()) {
				if (forwardCondition) {
					buf.append("first-child");
				} else {
					buf.append("last-child");
				}
			} else {
				if (forwardCondition) {
					buf.append("nth-child(");
				} else {
					buf.append("nth-last-child(");
				}
				buf.append(offset);
				if (!isUniversalSelectorList(ofList)) {
					buf.append(" of ");
					selectorListText(buf, ofList, true, false);
				}
				buf.append(')');
			}
		} else {
			if (forwardCondition) {
				buf.append("nth-child(");
			} else {
				buf.append("nth-last-child(");
			}
			appendAnB(slope, offset, pcond.hasKeyword(), buf);
			if (!isUniversalSelectorList(ofList)) {
				buf.append(" of ").append(ofList.toString());
			}
			buf.append(')');
		}
	}

	private void appendPositionalOfType(PositionalCondition pcond, StringBuilder buf) {
		int slope = pcond.getFactor();
		int offset = pcond.getOffset();
		SelectorList ofList = pcond.getOfList();
		boolean forwardCondition = pcond.isForwardCondition();
		if (slope == 0) {
			if (offset == 1 && ofList == null && !pcond.hasArgument()) {
				if (forwardCondition) {
					buf.append("first-of-type");
				} else {
					buf.append("last-of-type");
				}
			} else {
				if (forwardCondition) {
					buf.append("nth-of-type(");
				} else {
					buf.append("nth-last-of-type(");
				}
				buf.append(offset).append(')');
			}
		} else {
			if (forwardCondition) {
				buf.append("nth-of-type(");
			} else {
				buf.append("nth-last-of-type(");
			}
			appendAnB(slope, offset, pcond.hasKeyword(), buf);
			buf.append(')');
		}
	}

	private void appendAnB(int slope, int offset, boolean hasKeyword, StringBuilder buf) {
		if (hasKeyword && slope == 2) {
			if (offset == 0) {
				buf.append("even");
			} else {
				buf.append("odd");
			}
			return;
		}
		if (slope == -1) {
			buf.append('-');
		} else if (slope != 1) {
			buf.append(slope);
		}
		buf.append('n');
		if (offset > 0) {
			buf.append('+');
			buf.append(offset);
		} else if (offset != 0) {
			buf.append(offset);
		}
	}

	void selectorListText(StringBuilder buf, SelectorList selist, boolean omitUniversal,
			boolean scoped) {
		selectorText(buf, selist.item(0), omitUniversal, scoped);
		for (int i = 1; i < selist.getLength(); i++) {
			buf.append(',');
			selectorText(buf, selist.item(i), omitUniversal, scoped);
		}
	}

	private static boolean isUniversalSelectorList(SelectorList selist) {
		if (selist == null) {
			return true;
		}
		for (int i = 0; i < selist.getLength(); i++) {
			if (selist.item(i).getSelectorType() == Selector.SelectorType.UNIVERSAL) {
				return true;
			}
		}
		return false;
	}

}
