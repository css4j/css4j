/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

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
import io.sf.carte.doc.style.css.nsac.SimpleSelector;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.jclf.text.TokenParser;

/**
 * Selector serializer.
 * 
 * @author Carlos Amengual
 */
class SelectorSerializer {

	private final AbstractCSSStyleSheet parentSheet;

	/**
	 * Construct a serializer.
	 */
	public SelectorSerializer(AbstractCSSStyleSheet parentSheet) {
		super();
		this.parentSheet = parentSheet;
	}

	private AbstractCSSStyleSheet getStyleSheet() {
		return parentSheet;
	}

	String selectorText(Selector sel, boolean omitUniversal) {
		return selectorText(sel, omitUniversal, false);
	}

	private String selectorText(Selector sel, boolean omitUniversal, boolean scoped) {
		switch (sel.getSelectorType()) {
		case UNIVERSAL:
			return omitUniversal ? "" : "*";
		case ELEMENT:
			ElementSelector esel = (ElementSelector) sel;
			String lname = esel.getLocalName();
			String nsuri = esel.getNamespaceURI();
			if (lname != null) {
				lname = ParseHelper.escape(lname, false, false);
			}
			if (nsuri != null) {
				if (nsuri.length() != 0) {
					String nsprefix = getStyleSheet()
							.getNamespacePrefix(esel.getNamespaceURI());
					if (nsprefix == null) {
						throw new IllegalStateException(
								"Unknown ns prefix for URI " + esel.getNamespaceURI());
					}
					if (nsprefix.length() != 0) {
						return nsprefix + "|" + lname;
					} else {
						// Default namespace
						return lname;
					}
				} else {
					return "|" + lname;
				}
			} else {
				AbstractCSSStyleSheet psheet = getStyleSheet();
				if (psheet != null && psheet.hasDefaultNamespace()) {
					return "*|" + lname;
				}
				return lname != null ? lname : (omitUniversal ? "" : "*");
			}
		case CHILD:
			CombinatorSelector dsel = (CombinatorSelector) sel;
			Selector ancsel = dsel.getSelector();
			String anctext;
			if (!scoped || ancsel.getSelectorType() != Selector.SelectorType.UNIVERSAL) {
				anctext = selectorText(ancsel, false, scoped);
			} else {
				anctext = "";
			}
			String desctext = selectorText(dsel.getSecondSelector(), false, scoped);
			StringBuilder buf = new StringBuilder(anctext.length() + desctext.length() + 3);
			buf.append(anctext);
			buf.append('>');
			buf.append(desctext);
			return buf.toString();
		case CONDITIONAL:
			ConditionalSelector csel = (ConditionalSelector) sel;
			return conditionalSelectorText(csel.getCondition(), csel.getSimpleSelector());
		case DESCENDANT:
			dsel = (CombinatorSelector) sel;
			Selector ancestor = dsel.getSelector();
			anctext = selectorText(ancestor, false, scoped);
			desctext = selectorText(dsel.getSecondSelector(), false, scoped);
			buf = new StringBuilder(anctext.length() + desctext.length() + 1);
			buf.append(anctext);
			buf.append(' ');
			buf.append(desctext);
			return buf.toString();
		case DIRECT_ADJACENT:
			CombinatorSelector asel = (CombinatorSelector) sel;
			return selectorText(asel.getSelector(), omitUniversal, scoped) + '+'
					+ selectorText(asel.getSecondSelector(), false, scoped);
		case SUBSEQUENT_SIBLING:
			asel = (CombinatorSelector) sel;
			return selectorText(asel.getSelector(), omitUniversal, scoped) + "~"
					+ selectorText(asel.getSecondSelector(), false, scoped);
		case COLUMN_COMBINATOR:
			dsel = (CombinatorSelector) sel;
			return selectorText(dsel.getSelector(), omitUniversal, scoped) + "||"
					+ selectorText(dsel.getSecondSelector(), false, scoped);
		case SCOPE_MARKER:
			return "";
		default:
			return null;
		}
	}

	private String conditionalSelectorText(Condition condition, SimpleSelector simpleSelector) {
		switch (condition.getConditionType()) {
		case CLASS:
			return classText((AttributeCondition) condition, simpleSelector);
		case ID:
			String id = ((AttributeCondition) condition).getValue();
			StringBuilder buf = new StringBuilder(id.length() + 1);
			if (simpleSelector != null) {
				appendSimpleSelector(simpleSelector, buf);
			}
			return buf.append('#').append(ParseHelper.escape(id, false, false)).toString();
		case ATTRIBUTE:
			return attributeText((AttributeCondition) condition, simpleSelector);
		case BEGINS_ATTRIBUTE:
			return attributeBeginsText((AttributeCondition) condition, simpleSelector);
		case BEGIN_HYPHEN_ATTRIBUTE:
			return attributeBeginHyphenText((AttributeCondition) condition, simpleSelector);
		case ENDS_ATTRIBUTE:
			return attributeEndsText((AttributeCondition) condition, simpleSelector);
		case SUBSTRING_ATTRIBUTE:
			return attributeSubstringText((AttributeCondition) condition, simpleSelector);
		case LANG:
			return langText((LangCondition) condition, simpleSelector);
		case ONE_OF_ATTRIBUTE:
			return attributeOneOfText((AttributeCondition) condition, simpleSelector);
		case ONLY_CHILD:
			buf = new StringBuilder(16);
			if (simpleSelector != null) {
				appendSimpleSelector(simpleSelector, buf);
			}
			return buf.append(":only-child").toString();
		case ONLY_TYPE:
			buf = new StringBuilder(16);
			if (simpleSelector != null) {
				appendSimpleSelector(simpleSelector, buf);
			}
			return buf.append(":only-of-type").toString();
		case POSITIONAL:
			buf = new StringBuilder(50);
			if (simpleSelector != null) {
				appendSimpleSelector(simpleSelector, buf);
			}
			// Nobody else implements PositionalCondition, so we just cast
			PositionalCondition pcond = (PositionalCondition) condition;
			buf.append(':');
			if (pcond.isOfType()) {
				appendPositionalOfType(pcond, buf);
			} else {
				appendPositional(pcond, buf);
			}
			return buf.toString();
		case PSEUDO_CLASS:
			return pseudoClassText((PseudoCondition) condition, simpleSelector);
		case PSEUDO_ELEMENT:
			return pseudoElementText((PseudoCondition) condition, simpleSelector);
		case AND:
			CombinatorCondition ccond = (CombinatorCondition) condition;
			return conditionalSelectorText(ccond.getFirstCondition(), simpleSelector)
					+ conditionalSelectorText(ccond.getSecondCondition(), null);
		case SELECTOR_ARGUMENT:
			return selectorArgumentText((ArgumentCondition) condition, simpleSelector);
		default:
			// return null to ease the identification of unhandled cases.
			return null;
		}
	}

	private void appendSimpleSelector(SimpleSelector simpleSelector, StringBuilder buf) {
		buf.append(selectorText(simpleSelector, true));
	}

	private String classText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(16);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append(".").append(ParseHelper.escape(acond.getValue(), false, false));
		return buf.toString();
	}

	private String pseudoClassText(PseudoCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(24);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
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
		return buf.toString();
	}

	private String attributeText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(32);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
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

	private String attributeBeginsText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("^=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
		return buf.toString();
	}

	private String attributeBeginHyphenText(AttributeCondition acond,
			SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("|=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
		return buf.toString();
	}

	private String attributeEndsText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("$=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
		return buf.toString();
	}

	private String attributeSubstringText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("*=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
		return buf.toString();
	}

	private String attributeOneOfText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[');
		serializeAttributeQName(acond, buf);
		buf.append("~=");
		quoteAttributeValue(acond.getValue(), buf);
		attributeSelectorEnd(acond, buf);
		return buf.toString();
	}

	private void serializeAttributeQName(AttributeCondition acond, StringBuilder buf) {
		String nsuri = acond.getNamespaceURI();
		if (nsuri != null) {
			if (nsuri.length() != 0) {
				String nsprefix = getStyleSheet().getNamespacePrefix(nsuri);
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

	private String langText(LangCondition condition, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(32);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
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
		return buf.toString();
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
		AbstractCSSStyleSheet sheet = getStyleSheet();
		if (sheet != null) {
			ValueFactory factory = sheet.getStyleSheetFactory().getValueFactory();
			if (factory.hasFactoryFlag(AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE)) {
				quote = '"';
			} else if (factory.hasFactoryFlag(AbstractCSSStyleSheetFactory.STRING_SINGLE_QUOTE)) {
				quote = '\'';
			} else {
				quote = noDQ ? '\'' : '"';
			}
		} else {
			quote = noDQ ? '\'' : '"';
		}
		return quote;
	}

	private String pseudoElementText(PseudoCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(16);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		return buf.append(':').append(':').append(acond.getName()).toString();
	}

	private String selectorArgumentText(ArgumentCondition condition,
			SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(96);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append(':').append(condition.getName()).append("(");
		selectorListText(buf, condition.getSelectors(), false, true);
		return buf.append(')').toString();
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
		buf.append(selectorText(selist.item(0), omitUniversal, scoped));
		for (int i = 1; i < selist.getLength(); i++) {
			buf.append(',').append(selectorText(selist.item(i), omitUniversal, scoped));
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
