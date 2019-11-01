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

import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.Parser.NamespaceMap;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;
import io.sf.carte.util.SingleElementIterator;

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

	CombinatorSelectorImpl createCombinatorSelector(short type, Selector firstSelector) {
		if (firstSelector == null) {
			firstSelector = getUniversalSelector();
		}
		return new CombinatorSelectorImpl(type, firstSelector);
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

	/**
	 * Universal selector (for all namespaces).
	 */
	static class AnyNodeSelector extends AbstractSelector implements ElementSelector {

		@Override
		public short getSelectorType() {
			return Selector.SAC_UNIVERSAL_SELECTOR;
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
			return Selector.SAC_UNIVERSAL_SELECTOR;
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
		public Iterator<Selector> iterator() {
			return new SingleElementIterator<Selector>(selector);
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

	static class CombinatorSelectorImpl extends AbstractSelector implements CombinatorSelector {

		private short type;

		SimpleSelector simpleSelector = null;

		Selector selector;

		CombinatorSelectorImpl(short type, Selector selector) {
			super();
			this.type = type;
			this.selector = selector;
		}

		@Override
		public short getSelectorType() {
			return type;
		}

		void setSelectorType(short newType) {
			this.type = newType;
		}

		@Override
		public Selector getSelector() {
			return selector;
		}

		@Override
		public SimpleSelector getSecondSelector() {
			return simpleSelector;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((selector == null) ? 0 : selector.hashCode());
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
			CombinatorSelectorImpl other = (CombinatorSelectorImpl) obj;
			if (selector == null) {
				if (other.selector != null) {
					return false;
				}
			} else if (!selector.equals(other.selector)) {
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
			buf.append(selector.toString());
			switch (this.type) {
			case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
				buf.append('+');
			break;
			case Selector.SAC_SUBSEQUENT_SIBLING_SELECTOR:
				buf.append('~');
				break;
			case Selector.SAC_CHILD_SELECTOR:
				buf.append('>');
			break;
			case Selector.SAC_DESCENDANT_SELECTOR:
				if (selector.getSelectorType() != Selector.SAC_SCOPE_SELECTOR) {
					buf.append(' ');
				} else {
					buf.append(">>");
				}
			break;
			case Selector.SAC_COLUMN_COMBINATOR_SELECTOR:
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

	PositionalConditionImpl createPositionalCondition() {
		return new PositionalConditionImpl(false);
	}

	PositionalConditionImpl createPositionalCondition(boolean needsArgument) {
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
		case Condition.SAC_ENDS_ATTRIBUTE_CONDITION:
		case Condition.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
		case Condition.SAC_BEGINS_ATTRIBUTE_CONDITION:
		case Condition.SAC_ID_CONDITION:
		case Condition.SAC_ONLY_CHILD_CONDITION:
		case Condition.SAC_ONLY_TYPE_CONDITION:
			return new AttributeConditionImpl(type);
		case Condition.SAC_PSEUDO_CLASS_CONDITION:
		case Condition.SAC_PSEUDO_ELEMENT_CONDITION:
			return new PseudoConditionImpl(type);
		case Condition.SAC_LANG_CONDITION:
			return new LangConditionImpl();
		case Condition.SAC_AND_CONDITION:
			return new CombinatorConditionImpl();
		case Condition.SAC_SELECTOR_ARGUMENT_CONDITION:
			return new SelectorArgumentConditionImpl();
		case Condition.SAC_POSITIONAL_CONDITION:
			return createPositionalCondition();
		}
		return null;
	}

	class AttributeConditionImpl implements AttributeCondition {

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
				if (value != null) {
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
			case Condition.SAC_BEGINS_ATTRIBUTE_CONDITION:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('^').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case Condition.SAC_ENDS_ATTRIBUTE_CONDITION:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('$').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case Condition.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
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
			default:
				throw new IllegalStateException("Unknown type: " + condtype);
			}
			return buf.toString();
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
				buf.append(escapeName(localName));
			}
		}

		private String getEscapedValue() {
			return value != null ? ParseHelper.escape(value) : "";
		}

		private String getControlEscapedValue() {
			return value != null ? ParseHelper.escapeControl(ParseHelper.escapeBackslash(value)) : "";
		}

	}

	static String escapeName(String name) {
		return name != null ? ParseHelper.escape(name, false, false) : "";
	}

}
