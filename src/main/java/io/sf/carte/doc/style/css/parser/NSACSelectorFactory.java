/*

 Copyright (c) 2005-2023, Carlos Amengual.

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
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

/**
 * Based on SAC api.
 */
class NSACSelectorFactory implements NamespaceMap, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private static final ElementSelector universalSelector;
	private HashMap<String, String> mapNsPrefix2Uri = null;

	static {
		universalSelector = new AnyNodeSelector();
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

	ElementSelector createUniversalSelector(String namespaceUri) {
		return new UniversalSelector(namespaceUri);
	}

	ElementSelectorImpl createElementSelector() {
		return new ElementSelectorImpl();
	}

	CombinatorSelectorImpl createCombinatorSelector(Selector.SelectorType type, Selector firstSelector) {
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
			mapNsPrefix2Uri = new HashMap<>();
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

		private static final long serialVersionUID = 1L;

		@Override
		public SelectorType getSelectorType() {
			return SelectorType.UNIVERSAL;
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

		private static final long serialVersionUID = 1L;

		UniversalSelector(String namespaceUri) {
			super();
			this.namespaceUri = namespaceUri;
		}

		@Override
		public SelectorType getSelectorType() {
			return SelectorType.UNIVERSAL;
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

	class ElementSelectorImpl extends AbstractSelector implements ElementSelector {

		private static final long serialVersionUID = 1L;

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
		public SelectorType getSelectorType() {
			return SelectorType.ELEMENT;
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
			String lName = getLocalName();
			if (lName != null) {
				lName = escapeName(lName);
			}
			if (namespaceUri != null) {
				if (namespaceUri.length() != 0) {
					String nspre = getNamespacePrefix(namespaceUri);
					if (nspre != null && nspre.length() != 0) {
						return nspre + "|" + lName;
					}
				} else {
					return "|" + lName;
				}
			}
			return lName;
		}

	}

	static class CombinatorSelectorImpl extends AbstractSelector implements CombinatorSelector {

		private static final long serialVersionUID = 1L;

		private SelectorType type;

		SimpleSelector simpleSelector = null;

		Selector selector;

		CombinatorSelectorImpl(SelectorType type, Selector selector) {
			super();
			this.type = type;
			this.selector = selector;
		}

		@Override
		public SelectorType getSelectorType() {
			return type;
		}

		void setSelectorType(SelectorType newType) {
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
			result = prime * result + type.hashCode();
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
			case DIRECT_ADJACENT:
				buf.append('+');
			break;
			case SUBSEQUENT_SIBLING:
				buf.append('~');
				break;
			case CHILD:
				buf.append('>');
			break;
			case DESCENDANT:
				if (selector.getSelectorType() != SelectorType.SCOPE_MARKER) {
					buf.append(' ');
				} else {
					buf.append(">>");
				}
			break;
			case COLUMN_COMBINATOR:
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

	Condition createCondition(Condition.ConditionType type) {
		switch (type) {
		case CLASS:
			AttributeConditionImpl cond = new AttributeConditionImpl(type);
			return cond;
		case ATTRIBUTE:
		case BEGIN_HYPHEN_ATTRIBUTE:
		case ONE_OF_ATTRIBUTE:
		case ENDS_ATTRIBUTE:
		case SUBSTRING_ATTRIBUTE:
		case BEGINS_ATTRIBUTE:
		case ID:
		case ONLY_CHILD:
		case ONLY_TYPE:
			return new AttributeConditionImpl(type);
		case PSEUDO_CLASS:
		case PSEUDO_ELEMENT:
			return new PseudoConditionImpl(type);
		case LANG:
			return new LangConditionImpl();
		case AND:
			return new CombinatorConditionImpl();
		case SELECTOR_ARGUMENT:
			return new SelectorArgumentConditionImpl();
		case POSITIONAL:
			return createPositionalCondition();
		}
		return null;
	}

	class AttributeConditionImpl implements AttributeCondition, java.io.Serializable {

		private static final long serialVersionUID = 1L;

		ConditionType type;
		private String namespaceURI = null;
		private String localName = null;
		private String value = null;
		private Flag flag = null;

		AttributeConditionImpl(ConditionType type) {
			super();
			this.type = type;
		}

		@Override
		public ConditionType getConditionType() {
			return type;
		}

		@Override
		public String getNamespaceURI() {
			return namespaceURI;
		}

		void setNamespaceURI(String namespaceURI) {
			this.namespaceURI = namespaceURI;
		}

		@Override
		public String getLocalName() {
			return localName;
		}

		void setLocalName(String localName) {
			this.localName = localName;
		}

		@Override
		public String getValue() {
			return value;
		}

		void setValue(String value) {
			this.value = value;
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
			result = prime * result + type.hashCode();
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
			ConditionType condtype = getConditionType();
			switch (condtype) {
			case ATTRIBUTE:
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
			case BEGIN_HYPHEN_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('|').append('=').append('"')
						.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case ONE_OF_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('~').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case BEGINS_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('^').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case ENDS_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('$').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case SUBSTRING_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('*').append('=').append('"')
					.append(getControlEscapedValue()).append('"');
				buf.append(']');
			break;
			case CLASS:
				buf.append('.').append(getEscapedValue());
			break;
			case ID:
				buf.append('#').append(getEscapedValue());
			break;
			case LANG:
				buf.append(":lang(").append(getEscapedValue()).append(')');
			break;
			case ONLY_CHILD:
				buf.append(":only-child");
			break;
			case ONLY_TYPE:
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
			return value != null ? ParseHelper.escape(value, false, false) : "";
		}

		private String getControlEscapedValue() {
			return value != null ? ParseHelper.escapeControl(ParseHelper.escapeBackslash(value)) : "";
		}

	}

	static String escapeName(String name) {
		return name != null ? ParseHelper.escape(name, false, false) : "";
	}

}
