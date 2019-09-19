/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.LangCondition;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;

import io.sf.carte.doc.style.css.nsac.ArgumentCondition;
import io.sf.carte.doc.style.css.nsac.AttributeCondition2;
import io.sf.carte.doc.style.css.nsac.Condition2;
import io.sf.carte.doc.style.css.nsac.Parser2.NamespaceMap;
import io.sf.carte.doc.style.css.nsac.PositionalCondition2;
import io.sf.carte.doc.style.css.nsac.Selector2;
import io.sf.jclf.text.TokenParser;

/**
 * Based on SAC api.
 */
class NSACSelectorFactory implements NamespaceMap {

	private static final SelectorList universalSelectorList;
	private static final ElementSelector universalSelector;
	private HashMap<String, String> mapNsPrefix2Uri = null;

	static {
		universalSelectorList = new SingleSelectorList(new AnyNodeSelector());
		universalSelector = (ElementSelector) universalSelectorList.item(0);
	}

	NSACSelectorFactory() {
		super();
	}

	ElementSelector getUniversalSelector(String namespacePrefix) {
		if (namespacePrefix == null) {
			return universalSelector;
		}
		return createUniversalSelector(getNamespaceURI(namespacePrefix));
	}

	static ElementSelector getUniversalSelector() {
		return universalSelector;
	}

	static SelectorList getUniversalSelectorList() {
		return universalSelectorList;
	}

	ElementSelector createUniversalSelector(String namespaceUri) {
		return new UniversalSelector(namespaceUri);
	}

	ElementSelectorImpl createElementSelector() {
		return new ElementSelectorImpl();
	}

	DescendantSelectorImpl createDescendantSelector(short type, Selector ancestorSelector) {
		if (ancestorSelector == null) {
			ancestorSelector = getUniversalSelector();
		}
		return new DescendantSelectorImpl(type, ancestorSelector);
	}

	SiblingSelectorImpl createSiblingSelector(short type, Selector firstSelector) {
		if (firstSelector == null) {
			firstSelector = getUniversalSelector();
		}
		return new SiblingSelectorImpl(type, firstSelector);
	}

	/**
	 * Creates a conditional selector.
	 * 
	 * @param selector
	 *            a selector.
	 * @param condition
	 *            a condition
	 * @return the conditional selector.
	 * @exception CSSException
	 *                If this selector is not supported.
	 */
	ConditionalSelectorImpl createConditionalSelector(SimpleSelector selector, Condition condition)
			throws CSSException {
		if (selector == null) {
			selector = getUniversalSelector();
		}
		return new ConditionalSelectorImpl(selector, condition);
	}

	Selector createScopeSelector() {
		return new ScopeSelector();
	}

	void registerNamespacePrefix(String prefix, String uri) {
		if (mapNsPrefix2Uri == null) {
			mapNsPrefix2Uri = new HashMap<String, String>();
		}
		mapNsPrefix2Uri.put(prefix, uri);
	}

	@Override
	public String getNamespaceURI(String nsPrefix) {
		return mapNsPrefix2Uri != null ? mapNsPrefix2Uri.get(nsPrefix) : null;
	}

	String getNamespacePrefix(String namespaceUri) {
		Iterator<Entry<String, String>> it = mapNsPrefix2Uri.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> me = it.next();
			if (namespaceUri.equals(me.getValue())) {
				return me.getKey();
			}
		}
		return null;
	}

	static abstract class AbstractSelector implements Selector2 {

		@Override
		public int hashCode() {
			return Short.hashCode(getSelectorType());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Selector))
				return false;
			Selector other = (Selector) obj;
			return getSelectorType() == other.getSelectorType();
		}
	}

	/**
	 * Universal selector (for all namespaces).
	 */
	static class AnyNodeSelector extends AbstractSelector implements ElementSelector {

		@Override
		public short getSelectorType() {
			return Selector.SAC_ANY_NODE_SELECTOR;
		}

		@Override
		public String getLocalName() {
			return "*";
		}

		@Override
		public String getNamespaceURI() {
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + "*".hashCode();
			return result;
		}

		@Override
		public String toString() {
			return "*";
		}

	}

	/**
	 * Namespace-aware universal selector.
	 */
	class UniversalSelector extends ElementSelectorImpl {

		UniversalSelector(String namespaceUri) {
			super();
			this.namespaceUri = namespaceUri;
		}

		@Override
		public short getSelectorType() {
			return Selector.SAC_ANY_NODE_SELECTOR;
		}

		@Override
		public String getLocalName() {
			return "*";
		}

		@Override
		public String toString() {
			if (namespaceUri == null) {
				return "*";
			}
			return getNamespacePrefix(namespaceUri) + "|*";
		}

	}

	/**
	 * A selector list that only contains one selector.
	 */
	private static class SingleSelectorList implements SelectorList {
		private final Selector selector;

		SingleSelectorList(Selector selector) {
			super();
			this.selector = selector;
		}

		@Override
		public int getLength() {
			return 1;
		}

		@Override
		public Selector item(int index) {
			if (index != 0)
				return null;
			else
				return selector;
		}

		@Override
	    public int hashCode() {
	    	/*
	    	 * This should be compatible with the linked list hashCode implementation.
	    	 */
	        int hashCode = 31;
	        if (selector != null) {
	        	hashCode += selector.hashCode();
	        }
	        return hashCode;
	    }

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (!(obj instanceof SelectorList)) {
				return false;
			}
			SelectorList other = (SelectorList) obj;
			return other.getLength() == 1 && selector.equals(other.item(0));
		}

		@Override
		public String toString() {
			return selector.toString();
		}
	}

	class ElementSelectorImpl extends AbstractSelector implements ElementSelector {
		String namespaceUri = null;
		String localName = null;

		ElementSelectorImpl() {
			super();
		}

		void setNamespaceUri(String namespaceUri) {
			this.namespaceUri = namespaceUri;
		}
	
		void setLocalName(String localName) {
			this.localName = localName;
		}
	
		@Override
		public short getSelectorType() {
			return Selector.SAC_ELEMENT_NODE_SELECTOR;
		}

		@Override
		public String getNamespaceURI() {
			return namespaceUri;
		}

		@Override
		public String getLocalName() {
			return localName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((getLocalName() == null) ? 0 : getLocalName().hashCode());
			result = prime * result + ((namespaceUri == null) ? 0 : namespaceUri.hashCode());
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
			if (!(obj instanceof ElementSelector)) {
				return false;
			}
			ElementSelector other = (ElementSelector) obj;
			if (getLocalName() == null) {
				if (other.getLocalName() != null) {
					return false;
				}
			} else if (!getLocalName().equals(other.getLocalName())) {
				return false;
			}
			if (getNamespaceURI() == null) {
				if (other.getNamespaceURI() != null) {
					return false;
				}
			} else if (!getNamespaceURI().equals(other.getNamespaceURI())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			if (namespaceUri != null) {
				if (namespaceUri.length() != 0) {
					String nspre = getNamespacePrefix(namespaceUri);
					if (nspre != null && nspre.length() != 0) {
						return nspre + "|" + getLocalName();
					}
				} else {
					return "|" + getLocalName();
				}
			}
			return getLocalName();
		}

	}

	static class DescendantSelectorImpl extends AbstractSelector implements DescendantSelector {

		private short type;

		Selector ancestorSelector;
		SimpleSelector simpleSelector = null;

		DescendantSelectorImpl(short type, Selector ancestorSelector) {
			super();
			this.type = type;
			this.ancestorSelector = ancestorSelector;
		}

		void setSelectorType(short newType) {
			this.type = newType;
		}

		@Override
		public short getSelectorType() {
			return type;
		}

		@Override
		public Selector getAncestorSelector() {
			return ancestorSelector;
		}

		@Override
		public SimpleSelector getSimpleSelector() {
			return simpleSelector;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((ancestorSelector == null) ? 0 : ancestorSelector.hashCode());
			result = prime * result + ((simpleSelector == null) ? 0 : simpleSelector.hashCode());
			result = prime * result + type;
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
			DescendantSelectorImpl other = (DescendantSelectorImpl) obj;
			if (ancestorSelector == null) {
				if (other.ancestorSelector != null) {
					return false;
				}
			} else if (!ancestorSelector.equals(other.ancestorSelector)) {
				return false;
			}
			if (simpleSelector == null) {
				if (other.simpleSelector != null) {
					return false;
				}
			} else if (!simpleSelector.equals(other.simpleSelector)) {
				return false;
			}
			if (type != other.type) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(ancestorSelector.toString());
			switch (this.type) {
			case Selector.SAC_CHILD_SELECTOR:
				buf.append('>');
			break;
			case Selector.SAC_DESCENDANT_SELECTOR:
				if (ancestorSelector.getSelectorType() != Selector2.SAC_SCOPE_SELECTOR) {
					buf.append(' ');
				} else {
					buf.append(">>");
				}
			break;
			case Selector2.SAC_COLUMN_COMBINATOR_SELECTOR:
				buf.append("||");
			break;
			default:
				throw new IllegalStateException("Unknown type: " + type);
			}
			if (simpleSelector != null) {
				buf.append(simpleSelector.toString());
			} else {
				buf.append('?');
			}
			return buf.toString();
		}

	}

	static class SiblingSelectorImpl extends AbstractSelector implements SiblingSelector {

		private final short type;

		SimpleSelector siblingSelector = null;
		Selector selector;

		SiblingSelectorImpl(short type, Selector selector) {
			super();
			this.type = type;
			this.selector = selector;
		}

		@Override
		public short getSelectorType() {
			return type;
		}

		@Override
		public short getNodeType() {
			return SiblingSelector.ANY_NODE;
		}

		@Override
		public Selector getSelector() {
			return selector;
		}

		@Override
		public SimpleSelector getSiblingSelector() {
			return siblingSelector;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((selector == null) ? 0 : selector.hashCode());
			result = prime * result + ((siblingSelector == null) ? 0 : siblingSelector.hashCode());
			result = prime * result + type;
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
			SiblingSelectorImpl other = (SiblingSelectorImpl) obj;
			if (selector == null) {
				if (other.selector != null) {
					return false;
				}
			} else if (!selector.equals(other.selector)) {
				return false;
			}
			if (siblingSelector == null) {
				if (other.siblingSelector != null) {
					return false;
				}
			} else if (!siblingSelector.equals(other.siblingSelector)) {
				return false;
			}
			if (type != other.type) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(selector.toString());
			switch (this.type) {
			case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
				buf.append('+');
			break;
			case Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR:
				buf.append('~');
				break;
			default:
				throw new IllegalStateException("Unknown type: " + type);
			}
			if (siblingSelector != null) {
				buf.append(siblingSelector.toString());
			} else {
				buf.append('?');
			}
			return buf.toString();
		}

	}

	Condition createPositionalCondition() {
		return new PositionalConditionImpl(false);
	}

	Condition createPositionalCondition(boolean needsArgument) {
		PositionalConditionImpl cond = new PositionalConditionImpl(needsArgument);
		return cond;
	}

	Condition createCondition(short type) {
		switch (type) {
		case Condition.SAC_CLASS_CONDITION:
			AttributeConditionImpl cond = new AttributeConditionImpl(type);
			return cond;
		case Condition.SAC_ATTRIBUTE_CONDITION:
		case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
		case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
		case Condition2.SAC_ENDS_ATTRIBUTE_CONDITION:
		case Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
		case Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION:
		case Condition.SAC_ID_CONDITION:
		case Condition.SAC_PSEUDO_CLASS_CONDITION:
		case Condition.SAC_ONLY_CHILD_CONDITION:
		case Condition.SAC_ONLY_TYPE_CONDITION:
		case Condition2.SAC_PSEUDO_ELEMENT_CONDITION:
			return new AttributeConditionImpl(type);
		case Condition.SAC_LANG_CONDITION:
			return new LangConditionImpl();
		case Condition.SAC_AND_CONDITION:
			return new CombinatorConditionImpl();
		case Condition2.SAC_SELECTOR_ARGUMENT_CONDITION:
			return new SelectorArgumentConditionImpl();
		case Condition.SAC_POSITIONAL_CONDITION:
			return createPositionalCondition();
		}
		return null;
	}

	static class ConditionalSelectorImpl extends AbstractSelector implements ConditionalSelector {
		SimpleSelector selector;
		Condition condition;

		ConditionalSelectorImpl(SimpleSelector selector, Condition condition) {
			super();
			this.selector = selector;
			this.condition = condition;
		}

		@Override
		public short getSelectorType() {
			return Selector.SAC_CONDITIONAL_SELECTOR;
		}

		@Override
		public SimpleSelector getSimpleSelector() {
			return this.selector;
		}

		@Override
		public Condition getCondition() {
			return this.condition;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((condition == null) ? 0 : condition.hashCode());
			result = prime * result + ((selector == null) ? 0 : selector.hashCode());
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
			ConditionalSelectorImpl other = (ConditionalSelectorImpl) obj;
			if (condition == null) {
				if (other.condition != null) {
					return false;
				}
			} else if (!condition.equals(other.condition)) {
				return false;
			}
			if (selector == null) {
				if (other.selector != null) {
					return false;
				}
			} else if (!selector.equals(other.selector)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			short simpletype = selector.getSelectorType();
			if (simpletype != Selector.SAC_ANY_NODE_SELECTOR
					|| ((ElementSelector) selector).getNamespaceURI() != null) {
				buf.append(selector.toString());
			}
			buf.append(condition.toString());
			return buf.toString();
		}
	}

	class AttributeConditionImpl implements AttributeCondition2 {

		short type;
		String namespaceURI = null;
		String localName = null;
		String value = null;
		private Flag flag = null;

		AttributeConditionImpl(short type) {
			super();
			this.type = type;
		}

		@Override
		public short getConditionType() {
			return type;
		}

		@Override
		public String getNamespaceURI() {
			return namespaceURI;
		}

		@Override
		public String getLocalName() {
			return localName;
		}

		@Override
		public boolean getSpecified() {
			return value != null;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public boolean hasFlag(Flag flag) {
			return this.flag == flag;
		}

		void setFlag(Flag flag) {
			this.flag = flag;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((flag == null) ? 0 : flag.hashCode());
			result = prime * result + ((localName == null) ? 0 : localName.hashCode());
			result = prime * result + ((namespaceURI == null) ? 0 : namespaceURI.hashCode());
			result = prime * result + type;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			AttributeConditionImpl other = (AttributeConditionImpl) obj;
			if (flag != other.flag) {
				return false;
			}
			if (localName == null) {
				if (other.localName != null) {
					return false;
				}
			} else if (!localName.equals(other.localName)) {
				return false;
			}
			if (namespaceURI == null) {
				if (other.namespaceURI != null) {
					return false;
				}
			} else if (!namespaceURI.equals(other.namespaceURI)) {
				return false;
			}
			if (type != other.type) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			short condtype = getConditionType();
			switch (condtype) {
			case Condition.SAC_ATTRIBUTE_CONDITION:
				buf.append('[');
				appendEscapedQName(buf);
				if (getSpecified()) {
					buf.append('=').append('"')
						.append(getControlEscapedValue()).append('"');
					if (flag == Flag.CASE_I) {
						buf.append(' ').append('i');
					} else if (flag == Flag.CASE_S) {
						buf.append(' ').append('s');
					}
				}
				buf.append(']');
			break;
			case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('|').append('=').append('"')
						.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('~').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('^').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case Condition2.SAC_ENDS_ATTRIBUTE_CONDITION:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('$').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('*').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case Condition.SAC_CLASS_CONDITION:
				buf.append('.').append(getEscapedValue());
			break;
			case Condition.SAC_ID_CONDITION:
				buf.append('#').append(getEscapedValue());
			break;
			case Condition.SAC_PSEUDO_CLASS_CONDITION:
				buf.append(':').append(getEscapedLocalName());
				if (value != null) {
					buf.append('(').append(getEscapedValue()).append(')');
				}
			break;
			case Condition.SAC_LANG_CONDITION:
				buf.append(":lang(").append(getValue())
					.append(')');
			break;
			case Condition.SAC_ONLY_CHILD_CONDITION:
				buf.append(":only-child");
			break;
			case Condition.SAC_ONLY_TYPE_CONDITION:
				buf.append(":only-of-type");
			break;
			case Condition2.SAC_PSEUDO_ELEMENT_CONDITION:
				buf.append(':').append(':').append(getEscapedLocalName());
			break;
			default:
				throw new IllegalStateException("Unknown type: " + condtype);
			}
			return buf.toString();
		}

		private String getEscapedLocalName() {
			return localName != null ? ParseHelper.escape(localName, false, false) : "";
		}

		private void appendEscapedQName(StringBuilder buf) {
			if (namespaceURI != null) {
				if (namespaceURI.length() != 0) {
					String nspre = getNamespacePrefix(namespaceURI);
					if (nspre != null && nspre.length() != 0) {
						buf.append(nspre).append("|");
					}
				} else {
					buf.append("|");
				}
			}
			if (localName != null) {
				buf.append(ParseHelper.escape(localName, false, false));
			}
		}

		private String getEscapedValue() {
			return value != null ? ParseHelper.escape(value) : "";
		}

		private String getControlEscapedValue() {
			return value != null ? ParseHelper.escapeControl(ParseHelper.escapeBackslash(value)) : "";
		}

	}

	static class LangConditionImpl implements LangCondition {

		String lang = null;

		@Override
		public short getConditionType() {
			return Condition.SAC_LANG_CONDITION;
		}

		@Override
		public String getLang() {
			return lang;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((lang == null) ? 0 : lang.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			LangConditionImpl other = (LangConditionImpl) obj;
			if (lang == null) {
				if (other.lang != null) {
					return false;
				}
			} else if (!lang.equals(other.lang)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(":lang(");
			String lang = getLang();
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
				char quote = noDQ ? '\'' : '"';
				s = ParseHelper.quote(s, quote);
			} else if ((escaped = ParseHelper.escapeCssCharsAndFirstChar(s)) != s) {
				boolean noSQ = nextSQIdx == -1 || nextSQIdx > nextCommaIdx;
				if (escaped.length() < s.length() + 2 && noDQ && noSQ) {
					s = escaped.toString();
				} else {
					char quote = noDQ ? '\'' : '"';
					s = ParseHelper.quote(s, quote);
				}
			}
			return s;
		}

	}

	static class PositionalConditionImpl implements PositionalCondition2 {

		int offset = 1; // By default, set to :first-child or :first-of-type
		int slope = 0;
		boolean forwardCondition = true;
		boolean oftype = false;
		private final boolean hasArgument;
		boolean hasKeyword = false;
		SelectorList ofList = null;

		PositionalConditionImpl(boolean needsArgument) {
			super();
			this.hasArgument = needsArgument;
		}

		@Override
		public short getConditionType() {
			return Condition.SAC_POSITIONAL_CONDITION;
		}

		@Override
		public boolean isForwardCondition() {
			return forwardCondition;
		}

		@Override
		public boolean isOfType() {
			return oftype;
		}

		@Override
		public int getPosition() {
			if (slope != 0) {
				return 0;
			} else if (!forwardCondition) {
				return -offset;
			} else {
				return offset;
			}
		}

		@Override
		public int getFactor() {
			return slope;
		}

		@Override
		public int getOffset() {
			return offset;
		}

		@Override
		public boolean getTypeNode() {
			return true;
		}

		@Override
		public boolean getType() {
			return oftype;
		}

		@Override
		public boolean hasArgument() {
			return hasArgument;
		}

		/**
		 * The AnB expression is a keyword ?
		 * 
		 * @return <code>true</code> if the AnB expression is a keyword like
		 *         <code>odd</code>.
		 */
		@Override
		public boolean hasKeyword() {
			return hasKeyword;
		}

		@Override
		public SelectorList getOfList() {
			return ofList;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (forwardCondition ? 1231 : 1237);
			result = prime * result + (hasArgument ? 1231 : 1237);
			result = prime * result + (hasKeyword ? 1231 : 1237);
			result = prime * result + ((ofList == null) ? 0 : ofList.hashCode());
			result = prime * result + offset;
			result = prime * result + (oftype ? 1231 : 1237);
			result = prime * result + slope;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			PositionalConditionImpl other = (PositionalConditionImpl) obj;
			if (forwardCondition != other.forwardCondition) {
				return false;
			}
			if (hasArgument != other.hasArgument) {
				return false;
			}
			if (hasKeyword != other.hasKeyword) {
				return false;
			}
			if (ofList == null) {
				if (other.ofList != null) {
					return false;
				}
			} else if (!ParseHelper.equalSelectorList(ofList, other.ofList)) {
				return false;
			}
			if (offset != other.offset) {
				return false;
			}
			if (oftype != other.oftype) {
				return false;
			}
			if (slope != other.slope) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(':');
			if (oftype) {
				ofTypeSerialization(buf);
			} else {
				normalSerialization(buf);
			}
			return buf.toString();
		}

		private void normalSerialization(StringBuilder buf) {
			if (slope == 0) {
				if (offset == 1 && ofList == null && !hasArgument) {
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
					if (!isUniversalOfList()) {
						buf.append(" of ").append(ofList.toString());
					}
					buf.append(')');
				}
			} else {
				if (forwardCondition) {
					buf.append("nth-child(");
				} else {
					buf.append("nth-last-child(");
				}
				appendAnB(buf);
				if (!isUniversalOfList()) {
					buf.append(" of ").append(ofList.toString());
				}
				buf.append(')');
			}
		}

		private void ofTypeSerialization(StringBuilder buf) {
			if (slope == 0) {
				if (offset == 1 && ofList == null && !hasArgument) {
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
				appendAnB(buf);
				buf.append(')');
			}
		}

		private void appendAnB(StringBuilder buf) {
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

		private boolean isUniversalOfList() {
			if (ofList == null) {
				return true;
			}
			for (int i = 0; i < ofList.getLength(); i++) {
				Selector sel = ofList.item(i);
				if (sel.getSelectorType() == Selector.SAC_ANY_NODE_SELECTOR
						&& ((ElementSelector) sel).getNamespaceURI() == null) {
					return true;
				}
			}
			return false;
		}

	}

	static class CombinatorConditionImpl implements CombinatorCondition {

		Condition first = null;
		Condition second = null;

		CombinatorConditionImpl() {
			super();
		}

		@Override
		public short getConditionType() {
			return Condition.SAC_AND_CONDITION;
		}

		@Override
		public Condition getFirstCondition() {
			return first;
		}

		@Override
		public Condition getSecondCondition() {
			return second;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((first == null) ? 0 : first.hashCode());
			result = prime * result + ((second == null) ? 0 : second.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CombinatorConditionImpl other = (CombinatorConditionImpl) obj;
			if (first == null) {
				if (other.first != null) {
					return false;
				}
			} else if (!first.equals(other.first)) {
				return false;
			}
			if (second == null) {
				if (other.second != null) {
					return false;
				}
			} else if (!second.equals(other.second)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(getFirstCondition().toString());
			if (second != null) {
				buf.append(second.toString());
			} else  {
				buf.append('?');
			}
			return buf.toString();
		}

	}

	static class SelectorArgumentConditionImpl implements ArgumentCondition {

		String name = null;
		SelectorList arguments = null;

		SelectorArgumentConditionImpl() {
			super();
		}

		@Override
		public short getConditionType() {
			return Condition2.SAC_SELECTOR_ARGUMENT_CONDITION;
		}

		@Override
		public String getName() {
			return name;
		}

		void setName(String name) {
			this.name = name;
		}

		@Override
		public SelectorList getSelectors() {
			return arguments;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SelectorArgumentConditionImpl other = (SelectorArgumentConditionImpl) obj;
			if (arguments == null) {
				if (other.arguments != null) {
					return false;
				}
			} else if (!ParseHelper.equalSelectorList(arguments, other.arguments)) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(':').append(getName()).append('(');
			if (arguments != null) {
				buf.append(arguments.toString());
			}
			buf.append(')');
			return buf.toString();
		}

	}

	/**
	 * Universal selector (for all namespaces).
	 */
	static class ScopeSelector extends AbstractSelector {

		@Override
		public short getSelectorType() {
			return Selector2.SAC_SCOPE_SELECTOR;
		}

		@Override
		public int hashCode() {
			return 31 * super.hashCode();
		}

		@Override
		public String toString() {
			return "";
		}

	}

}
