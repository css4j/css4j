/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.NegativeCondition;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.Condition2;
import io.sf.carte.doc.style.css.nsac.PositionalCondition2;
import io.sf.carte.doc.style.css.nsac.Selector2;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;
import io.sf.jclf.text.TokenParser;

/**
 * Abstract class to be inherited by CSS rules which have both selectors and
 * a CSSStyleDeclaration.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class CSSStyleDeclarationRule extends BaseCSSDeclarationRule {

	private SelectorList selectorList = null;

	private String selectorText = "";

	protected CSSStyleDeclarationRule(AbstractCSSStyleSheet parentSheet, short type, byte origin) {
		super(parentSheet, type, origin);
	}

	/**
	 * Constructor used for stand-alone style rules.
	 */
	CSSStyleDeclarationRule() {
		super();
	}

	public String getSelectorText() {
		return selectorText;
	}

	public void setSelectorText(String selectorText) throws DOMException {
		this.selectorText = selectorText;
	}

	void setSelectorList(SelectorList selectorList) {
		this.selectorList = selectorList;
		updateSelectorText();
	}

	void updateSelectorText() {
		int sz = selectorList.getLength();
		StringBuilder sb = new StringBuilder(sz * 7 + 5);
		if (sz > 0) {
			selectorListText(sb, selectorList, false, false);
		}
		setSelectorText(sb.toString());
	}

	SelectorList getSelectorList() {
		return selectorList;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		clear();
		selectorList = null;
		selectorText = "";
		Parser parser = createSACParser();
		InputSource source = new InputSource();
		Reader re = new StringReader(cssText);
		source.setCharacterStream(re);
		PropertyDocumentHandler handler = createDocumentHandler();
		handler.setLexicalPropertyListener(getLexicalPropertyListener());
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(handler);
		try {
			parser.parseRule(source);
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(50 + getStyle().getLength() * 24);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		String seltext = getSelectorText();
		if (seltext.length() != 0) {
			context.startRule(wri);
			wri.write(seltext);
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			context.startStyleDeclaration(wri);
			getStyle().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endStyleDeclaration(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri);
		}
	}

	@Override
	public String getMinifiedCssText() {
		String seltext = getSelectorText();
		if (seltext.length() != 0) {
			return seltext + '{' + getStyle().getMinifiedCssText() + '}';
		}
		return "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = super.hashCode();
		result = prime * result + ((selectorList == null) ? 0 : selectorList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CSSStyleDeclarationRule))
			return false;
		CSSStyleDeclarationRule other = (CSSStyleDeclarationRule) obj;
		if (!super.equals(other))
			return false;
		if (selectorList == null) {
			if (other.selectorList != null)
				return false;
		} else if (!selectorList.equals(other.selectorList))
			return false;
		return true;
	}

	@Override
	public CSSStyleDeclarationRule clone(AbstractCSSStyleSheet parentSheet) throws IllegalArgumentException {
		Class<?>[] parameterTypes = new Class<?>[2];
		parameterTypes[0] = AbstractCSSStyleSheet.class;
		parameterTypes[1] = Byte.TYPE;
		Constructor<? extends CSSStyleDeclarationRule> ctor;
		try {
			ctor = getClass().getConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
		Object[] initargs = new Object[2];
		initargs[0] = parentSheet;
		initargs[1] = getOrigin();
		CSSStyleDeclarationRule rule;
		try {
			rule = ctor.newInstance(initargs);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
		String oldHrefContext = getParentStyleSheet().getHref();
		rule.setWrappedStyle((BaseCSSStyleDeclaration) getStyle(), oldHrefContext);
		rule.selectorList = getSelectorList();
		rule.selectorText = getSelectorText();
		if (hasErrorsOrWarnings()) {
			rule.setStyleDeclarationErrorHandler(getStyleDeclarationErrorHandler());
		}
		return rule;
	}

	protected PropertyDocumentHandler createDocumentHandler() {
		return new RuleDocumentHandler();
	}

	class RuleDocumentHandler extends DeclarationRuleDocumentHandler {
		RuleDocumentHandler() {
			super();
		}

		@Override
		public void startSelector(SelectorList selectors) throws DOMException {
			if (selectorList != null) {
				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
						"Rule already set, stream contains more than one rule");
			}
			setSelectorList(selectors);
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) throws CSSException {
			try {
				super.property(name, value, important);
			} catch (DOMException e) {
				CSSPropertyValueException ex = new CSSPropertyValueException(e);
				ex.setValueText(value.toString());
				getStyleDeclarationErrorHandler().wrongValue(name, ex);
			}
		}

		@Override
		public void warning(CSSParseException exception) throws CSSException {
			if (selectorList != null) {
				super.warning(exception);
			} else {
				AbstractCSSStyleSheet sheet = getParentStyleSheet();
				if (sheet != null) {
					sheet.getErrorHandler().ruleParseWarning(CSSStyleDeclarationRule.this, exception);
				}
			}
		}

		@Override
		public void error(CSSParseException exception) throws CSSException {
			if (selectorList != null) {
				super.error(exception);
			} else {
				AbstractCSSStyleSheet sheet = getParentStyleSheet();
				if (sheet != null) {
					sheet.getErrorHandler().ruleParseError(CSSStyleDeclarationRule.this, exception);
				}
			}
		}

		@Override
		public void fatalError(CSSParseException exception) throws CSSException {
			if (selectorList != null) {
				super.fatalError(exception);
			} else {
				AbstractCSSStyleSheet sheet = getParentStyleSheet();
				if (sheet != null) {
					sheet.getErrorHandler().ruleParseError(CSSStyleDeclarationRule.this, exception);
				}
			}
		}

	}

	String selectorText(Selector sel, boolean omitUniversal) {
		return selectorText(sel, omitUniversal, false);
	}

	private String selectorText(Selector sel, boolean omitUniversal, boolean scoped) {
		switch (sel.getSelectorType()) {
		case Selector.SAC_ANY_NODE_SELECTOR:
			return omitUniversal ? "" : "*";
		case Selector.SAC_ELEMENT_NODE_SELECTOR:
			ElementSelector esel = (ElementSelector) sel;
			String lname = esel.getLocalName();
			String nsuri = esel.getNamespaceURI();
			if (nsuri != null) {
				if (nsuri.length() != 0) {
					String nsprefix = getParentStyleSheet().getNamespacePrefix(esel.getNamespaceURI());
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
				AbstractCSSStyleSheet psheet = getParentStyleSheet();
				if (psheet != null && psheet.hasDefaultNamespace()) {
					return "*|" + lname;
				}
				return lname != null ? lname : (omitUniversal ? "" : "*");
			}
		case Selector.SAC_CHILD_SELECTOR:
			DescendantSelector dsel = (DescendantSelector) sel;
			Selector ancsel = dsel.getAncestorSelector();
			String anctext;
			if (!scoped || ancsel.getSelectorType() != Selector.SAC_ANY_NODE_SELECTOR) {
				anctext = selectorText(ancsel, false, scoped);
			} else {
				anctext = "";
			}
			String desctext = selectorText(dsel.getSimpleSelector(), false, scoped);
			StringBuilder buf = new StringBuilder(anctext.length() + desctext.length() + 3);
			buf.append(anctext);
			short stype = dsel.getSimpleSelector().getSelectorType();
			// First condition is SS parser hack
			if (stype != Selector.SAC_PSEUDO_ELEMENT_SELECTOR && stype != Selector.SAC_NEGATIVE_SELECTOR) {
				buf.append(">");
			}
			buf.append(desctext);
			return buf.toString();
		case Selector.SAC_CONDITIONAL_SELECTOR:
			ConditionalSelector csel = (ConditionalSelector) sel;
			return conditionalSelectorText(csel.getCondition(), csel.getSimpleSelector());
		case Selector.SAC_DESCENDANT_SELECTOR:
			dsel = (DescendantSelector) sel;
			anctext = selectorText(dsel.getAncestorSelector(), false, scoped);
			desctext = selectorText(dsel.getSimpleSelector(), false, scoped);
			buf = new StringBuilder(anctext.length() + desctext.length() + 1);
			buf.append(anctext);
			// Check for cssparser's handling of pseudo-elements
			if (dsel.getSimpleSelector().getSelectorType() != Selector.SAC_PSEUDO_ELEMENT_SELECTOR) {
				buf.append(' ');
			}
			buf.append(desctext);
			return buf.toString();
		case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
			SiblingSelector asel = (SiblingSelector) sel;
			return selectorText(asel.getSelector(), omitUniversal, scoped) + " + "
					+ selectorText(asel.getSiblingSelector(), false, scoped);
		case Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR:
			asel = (SiblingSelector) sel;
			return selectorText(asel.getSelector(), omitUniversal, scoped) + "~"
					+ selectorText(asel.getSiblingSelector(), false, scoped);
		case Selector2.SAC_COLUMN_COMBINATOR_SELECTOR:
			dsel = (DescendantSelector) sel;
			return selectorText(dsel.getAncestorSelector(), omitUniversal, scoped) + "||"
					+ selectorText(dsel.getSimpleSelector(), false, scoped);
		case Selector.SAC_ROOT_NODE_SELECTOR:
			return ":root";
		case Selector2.SAC_SCOPE_SELECTOR:
			return "";
		default:
			return null;
		}
	}

	private String conditionalSelectorText(Condition condition, SimpleSelector simpleSelector) {
		switch (condition.getConditionType()) {
		case Condition.SAC_CLASS_CONDITION:
			return classText((AttributeCondition) condition, simpleSelector);
		case Condition.SAC_ID_CONDITION:
			return "#" + ParseHelper.escape(((AttributeCondition) condition).getValue(), false, false);
		case Condition.SAC_ATTRIBUTE_CONDITION:
			return attributeText((AttributeCondition) condition, simpleSelector);
		case Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION:
			return attributeBeginsText((AttributeCondition) condition, simpleSelector);
		case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
			return attributeBeginHyphenText((AttributeCondition) condition, simpleSelector);
		case Condition2.SAC_ENDS_ATTRIBUTE_CONDITION:
			return attributeEndsText((AttributeCondition) condition, simpleSelector);
		case Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
			return attributeSubstringText((AttributeCondition) condition, simpleSelector);
		case Condition.SAC_LANG_CONDITION:
			return langText((LangCondition) condition, simpleSelector);
		case Condition.SAC_NEGATIVE_CONDITION: // Nobody implements this
			return ":not(" + conditionalSelectorText(((NegativeCondition) condition).getCondition(), simpleSelector)
					+ ")";
		case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
			return attributeOneOfText((AttributeCondition) condition, simpleSelector);
		case Condition.SAC_ONLY_CHILD_CONDITION:
			StringBuilder buf = new StringBuilder(16);
			if (simpleSelector != null) {
				appendSimpleSelector(simpleSelector, buf);
			}
			return buf.append(":only-child").toString();
		case Condition.SAC_ONLY_TYPE_CONDITION:
			buf = new StringBuilder(16);
			if (simpleSelector != null) {
				appendSimpleSelector(simpleSelector, buf);
			}
			return buf.append(":only-of-type").toString();
		case Condition.SAC_POSITIONAL_CONDITION:
			buf = new StringBuilder(50);
			if (simpleSelector != null) {
				appendSimpleSelector(simpleSelector, buf);
			}
			// Nobody else implements PositionalCondition, so we just cast
			PositionalCondition2 pcond = (PositionalCondition2) condition;
			buf.append(':');
			if (pcond.getType()) {
				appendPositionalOfType(pcond, buf);
			} else {
				appendPositional(pcond, buf);
			}
			return buf.toString();
		case Condition.SAC_PSEUDO_CLASS_CONDITION:
			return pseudoClassText((AttributeCondition) condition, simpleSelector);
		case Condition2.SAC_PSEUDO_ELEMENT_CONDITION:
			return pseudoElementText((AttributeCondition) condition, simpleSelector);
		case Condition.SAC_OR_CONDITION: // Nobody implements this
			return conditionOrDeprecated(simpleSelector, (CombinatorCondition) condition);
		case Condition.SAC_AND_CONDITION:
			CombinatorCondition ccond = (CombinatorCondition) condition;
			return conditionalSelectorText(ccond.getFirstCondition(), simpleSelector)
					+ conditionalSelectorText(ccond.getSecondCondition(), null);
		case Condition2.SAC_SELECTOR_ARGUMENT_CONDITION:
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

	private String pseudoClassText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(24);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append(':');
		String name = acond.getLocalName();
		String value = acond.getValue();
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
			buf.append('[').append(acond.getLocalName()).append('=');
			quoteAttributeValue(acond.getValue(), buf);
			buf.append(']');
		} else {
			buf.append('[').append(acond.getLocalName()).append(']');
		}
		return buf.toString();
	}

	private String attributeBeginsText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[').append(acond.getLocalName()).append("^=");
		quoteAttributeValue(acond.getValue(), buf);
		buf.append(']');
		return buf.toString();
	}

	private String attributeBeginHyphenText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[').append(acond.getLocalName()).append("|=");
		quoteAttributeValue(acond.getValue(), buf);
		buf.append(']');
		return buf.toString();
	}

	private String attributeEndsText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[').append(acond.getLocalName()).append("$=");
		quoteAttributeValue(acond.getValue(), buf);
		buf.append(']');
		return buf.toString();
	}

	private String attributeSubstringText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[').append(acond.getLocalName()).append("*=");
		quoteAttributeValue(acond.getValue(), buf);
		buf.append(']');
		return buf.toString();
	}

	private String attributeOneOfText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(48);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append('[').append(acond.getLocalName()).append("~=");
		quoteAttributeValue(acond.getValue(), buf);
		buf.append(']');
		return buf.toString();
	}

	private void quoteAttributeValue(String value, StringBuilder buf) {
		char quote = quoteChar(true);
		buf.append(ParseHelper.quote(value, quote));
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
		AbstractCSSStyleSheet sheet = getParentStyleSheet();
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

	private String pseudoElementText(AttributeCondition acond, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(16);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		return buf.append(':').append(':').append(acond.getLocalName()).toString();
	}

	private String selectorArgumentText(ArgumentCondition condition, SimpleSelector simpleSelector) {
		StringBuilder buf = new StringBuilder(96);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append(':').append(condition.getName()).append("(");
		selectorListText(buf, condition.getSelectors(), true, true);
		return buf.append(')').toString();
	}

	private void appendPositional(PositionalCondition2 pcond, StringBuilder buf) {
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

	private void appendPositionalOfType(PositionalCondition2 pcond, StringBuilder buf) {
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

	private void selectorListText(StringBuilder buf, SelectorList selist, boolean omitUniversal, boolean scoped) {
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
			if (selist.item(i).getSelectorType() == Selector.SAC_ANY_NODE_SELECTOR) {
				return true;
			}
		}
		return false;
	}

	private String conditionOrDeprecated(SimpleSelector simpleSelector, CombinatorCondition ccond) {
		StringBuilder buf = new StringBuilder(96);
		if (simpleSelector != null) {
			appendSimpleSelector(simpleSelector, buf);
		}
		buf.append(":matches(").append(conditionalSelectorText(ccond.getFirstCondition(), null)).append(',')
				.append(conditionalSelectorText(ccond.getSecondCondition(), null)).append(')');
		return buf.toString();
	}

}
