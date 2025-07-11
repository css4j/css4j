/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.HashMap;
import java.util.Map.Entry;

import io.sf.carte.doc.style.css.impl.CSSUtil;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.NamespacePrefixMap;
import io.sf.carte.doc.style.css.nsac.Parser.NamespaceMap;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

/**
 * Based on SAC api.
 */
class NSACSelectorFactory implements NamespaceMap, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private static final ElementSelector universalSelector;

	private NamespacePrefixMap nsPrefixMap = null;

	static {
		universalSelector = new AnyNodeSelector();
	}

	NSACSelectorFactory() {
		super();
	}

	NSACSelectorFactory(NamespacePrefixMap nsPrefixMap) {
		super();
		this.nsPrefixMap = nsPrefixMap;
	}

	NamespacePrefixMap getNamespacePrefixMap() {
		if (nsPrefixMap == null) {
			nsPrefixMap = createNamespacePrefixMap();
		}
		return nsPrefixMap;
	}

	private NSACNamespacePrefixMap createNamespacePrefixMap() {
		return new NSACNamespacePrefixMap();
	}

	private static class NSACNamespacePrefixMap implements NamespacePrefixMap {

		private HashMap<String, String> mapNsPrefix2Uri = new HashMap<>();

		/*
		 * NamespaceMap
		 */

		@Override
		public String getNamespaceURI(String nsPrefix) {
			return mapNsPrefix2Uri.get(nsPrefix);
		}

		/*
		 * NamespacePrefixMap
		 */

		@Override
		public String getNamespacePrefix(String namespaceUri) {
			for (Entry<String, String> me : mapNsPrefix2Uri.entrySet()) {
				if (namespaceUri.equals(me.getValue())) {
					return me.getKey();
				}
			}
			return null;
		}

		@Override
		public boolean hasDefaultNamespace() {
			return mapNsPrefix2Uri.containsKey("");
		}

		@Override
		public void registerNamespacePrefix(String prefix, String uri) {
			mapNsPrefix2Uri.put(prefix, uri);
		}

	}

	@Override
	public String getNamespaceURI(String nsPrefix) {
		return nsPrefixMap != null ? nsPrefixMap.getNamespaceURI(nsPrefix) : null;
	}

	String getNamespacePrefix(String namespaceUri) {
		return nsPrefixMap != null ? nsPrefixMap.getNamespacePrefix(namespaceUri) : null;
	}

	void registerNamespacePrefix(String prefix, String uri) {
		getNamespacePrefixMap().registerNamespacePrefix(prefix, uri);
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

	CombinatorSelectorImpl createCombinatorSelector(Selector.SelectorType type,
			Selector firstSelector) {
		if (firstSelector == null) {
			firstSelector = getUniversalSelector();
		}
		return new CombinatorSelectorImpl(type, firstSelector);
	}

	/**
	 * Creates a conditional selector.
	 * 
	 * @param selector  a selector.
	 * @param condition a condition
	 * @return the conditional selector.
	 * @exception CSSException If this selector is not supported.
	 */
	ConditionalSelectorImpl createConditionalSelector(SimpleSelector selector,
			AbstractCondition condition) throws CSSException {
		if (selector == null) {
			selector = getUniversalSelector();
		}

		return new ConditionalSelectorImpl(selector, condition) {

			private static final long serialVersionUID = 1L;

			@Override
			NSACSelectorFactory getSelectorFactory() throws IllegalStateException {
				return NSACSelectorFactory.this;
			}

		};
	}

	AbstractSelector createScopeSelector() {
		return new ScopeSelector();
	}

	/**
	 * Universal selector (for all namespaces).
	 */
	private static class AnyNodeSelector extends AbstractSelector implements ElementSelector {

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

		@Override
		public AbstractSelector clone() {
			return this;
		}

		@Override
		NSACSelectorFactory getSelectorFactory() {
			throw new IllegalStateException();
		}

	}

	/**
	 * Namespace-aware universal selector.
	 */
	private class UniversalSelector extends ElementSelectorImpl {

		private static final long serialVersionUID = 1L;

		private UniversalSelector(String namespaceUri) {
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

			String pre;
			if (nsPrefixMap == null) {
				pre = "";
			} else {
				pre = nsPrefixMap.getNamespacePrefix(namespaceUri);
			}
			return pre + "|*";
		}

	}

	class ElementSelectorImpl extends NamespaceAwareSelector implements ElementSelector {

		private static final long serialVersionUID = 1L;

		String namespaceUri = null;
		String localName = null;

		private ElementSelectorImpl() {
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
					if (nspre != null && !nspre.isEmpty()) {
						return nspre + "|" + lName;
					}
				} else {
					return "|" + lName;
				}
			}
			return lName;
		}

		@Override
		public ElementSelectorImpl clone() {
			ElementSelectorImpl clon = (ElementSelectorImpl) super.clone();
			clon.localName = localName;
			clon.namespaceUri = namespaceUri;
			return clon;
		}

	}

	/**
	 * Namespace-aware selector.
	 */
	abstract class NamespaceAwareSelector extends AbstractSelector {

		private static final long serialVersionUID = 1L;

		@Override
		boolean setNamespacePrefixMap(NamespacePrefixMap map) {
			NSACSelectorFactory.this.nsPrefixMap = map;
			return true;
		}

		@Override
		NSACSelectorFactory getSelectorFactory() {
			return NSACSelectorFactory.this;
		}

	}

	class CombinatorSelectorImpl extends NamespaceAwareSelector implements CombinatorSelector {

		private static final long serialVersionUID = 1L;

		private SelectorType type;

		SimpleSelector simpleSelector = null;

		Selector selector;

		private CombinatorSelectorImpl(SelectorType type, Selector selector) {
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
		boolean isSimpleSelector() {
			return false;
		}

		@Override
		ConditionalSelectorImpl withCondition(NSACSelectorFactory factory,
				AbstractCondition condition) {
			throw new IllegalStateException("Not a simple selector.");
		}

		@Override
		AbstractSelector descendant(SelectorList base) {
			CombinatorSelectorImpl clon = clone();
			clon.selector = ((AbstractSelector) selector).descendant(base);
			return clon;
		}

		@Override
		Selector replace(SelectorList base, MutableBoolean replaced) {
			CombinatorSelectorImpl clon = clone();
			clon.selector = ((AbstractSelector) clon.selector).replace(base, replaced);
			AbstractSelector replSel = (AbstractSelector) ((AbstractSelector) clon.simpleSelector)
					.replace(base, replaced);

			if (replSel.isSimpleSelector()) {
				clon.simpleSelector = (SimpleSelector) replSel;
				return clon;
			} else {
				CombinatorSelectorImpl replComb = (CombinatorSelectorImpl) replSel;
				CombinatorSelectorImpl comb = getSelectorFactory()
						.createCombinatorSelector(replSel.getSelectorType(), clon);
				if (((AbstractSelector) replComb.selector).isSimpleSelector()) {
					clon.simpleSelector = (SimpleSelector) replComb.selector;
				} else {
					NSACSelectorFactory factory = getSelectorFactory();
					SelectorArgumentConditionImpl is = new SelectorArgumentConditionImpl();
					SelectorListImpl selist = new SelectorListImpl();
					selist.add(replComb.selector);
					is.arguments = selist;
					is.setName("is");
					clon.simpleSelector = factory.createConditionalSelector(
							NSACSelectorFactory.getUniversalSelector(), is);
				}
				comb.simpleSelector = replComb.simpleSelector;
				return comb;
			}
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

		@Override
		public CombinatorSelectorImpl clone() {
			CombinatorSelectorImpl clon = (CombinatorSelectorImpl) super.clone();
			clon.simpleSelector = simpleSelector;
			clon.selector = selector;
			clon.type = type;
			return clon;
		}

	}

	AttributeConditionImpl createAttributeCondition(Condition.ConditionType type) {
		return new AttributeConditionImpl(type);
	}

	class AttributeConditionImpl extends AbstractCondition implements AttributeCondition {

		private static final long serialVersionUID = 1L;

		ConditionType type;
		private String namespaceURI = null;
		private String localName = null;
		private String value = null;
		private Flag flag = null;

		private AttributeConditionImpl(ConditionType type) {
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

		@Override
		public boolean hasFlag() {
			return this.flag != null;
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
		void serialize(StringBuilder buf) {
			ConditionType condtype = getConditionType();
			switch (condtype) {
			case ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				if (value != null) {
					buf.append('=');
					appendAttributeValue(buf);
				}
				buf.append(']');
				break;
			case BEGIN_HYPHEN_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('|').append('=');
				appendAttributeValue(buf);
				buf.append(']');
				break;
			case ONE_OF_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('~').append('=');
				appendAttributeValue(buf);
				buf.append(']');
				break;
			case BEGINS_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('^').append('=');
				appendAttributeValue(buf);
				buf.append(']');
				break;
			case ENDS_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('$').append('=');
				appendAttributeValue(buf);
				buf.append(']');
				break;
			case SUBSTRING_ATTRIBUTE:
				buf.append('[');
				appendEscapedQName(buf);
				buf.append('*').append('=');
				appendAttributeValue(buf);
				buf.append(']');
				break;
			case CLASS:
				buf.append('.').append(getEscapedValue());
				break;
			case ID:
				buf.append('#').append(getEscapedValue());
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
		}

		private void appendAttributeValue(StringBuilder buf) {
			if (flag == null && !value.isEmpty() && CSSUtil.isValidIdentifier(value)) {
				buf.append(value);
			} else {
				buf.append('"').append(getControlEscapedValue()).append('"');
				if (flag == Flag.CASE_I) {
					buf.append(' ').append('i');
				} else if (flag == Flag.CASE_S) {
					buf.append(' ').append('s');
				}
			}
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
			return value != null ? ParseHelper.escapeControl(ParseHelper.escapeBackslash(value))
					: "";
		}

		@Override
		public AttributeConditionImpl clone() {
			AttributeConditionImpl clon = (AttributeConditionImpl) super.clone();
			clon.flag = flag;
			clon.type = type;
			clon.localName = localName;
			clon.namespaceURI = namespaceURI;
			clon.value = value;
			return clon;
		}

	}

	static String escapeName(String name) {
		return name != null ? ParseHelper.escape(name, false, false) : "";
	}

}
