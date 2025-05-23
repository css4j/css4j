/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import io.sf.carte.doc.DirectionalityHelper;
import io.sf.carte.doc.DirectionalityHelper.Directionality;
import io.sf.carte.doc.style.css.CSSDocument.ComplianceMode;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CombinatorSelector;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.nsac.SimpleSelector;

/**
 * Base CSS Selector matcher for DOM.
 */
abstract public class BaseSelectorMatcher<E extends Element> extends AbstractSelectorMatcher {

	private static final long serialVersionUID = 1L;

	private final E element;

	protected BaseSelectorMatcher(E elm) {
		super();
		element = elm;
		String name = localName(elm).intern();
		setLocalName(name);
	}

	protected E getElement() {
		return element;
	}

	protected String localName(Node elm) {
		String name = elm.getLocalName();
		if (name == null) {
			name = elm.getNodeName();
		}
		return name.toLowerCase(Locale.ROOT);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractSelectorMatcher getParentSelectorMatcher() {
		Node parent = element.getParentNode();
		if (parent.getNodeType() == Node.ELEMENT_NODE) {
			// This should be always true if it is a correct tree
			return (AbstractSelectorMatcher) obtainSelectorMatcher((E) parent);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractSelectorMatcher getPreviousSiblingSelectorMatcher() {
		Node sibling = element.getPreviousSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				return (AbstractSelectorMatcher) obtainSelectorMatcher((E) sibling);
			}
			sibling = sibling.getPreviousSibling();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected int indexOf(SelectorList selectors) {
		Node node = element.getParentNode().getFirstChild();
		int idx = 0;
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE && matchSelectors(selectors, (E) node)) {
				idx++;
				if (node == element) {
					return idx;
				}
			}
			node = node.getNextSibling();
		}

		return -1;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected int reverseIndexOf(SelectorList selectors) {
		Node node = element.getParentNode().getLastChild();
		int idx = 0;
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE
				&& matchSelectors(selectors, (E) node)) {
				idx++;
				if (node == element) {
					return idx;
				}
			}
			node = node.getPreviousSibling();
		}
		return -1;
	}

	private boolean matchSelectors(SelectorList selectors, E element) {
		if (selectors == null) {
			return true;
		}
		SelectorMatcher matcher = obtainSelectorMatcher(element);
		int sz = selectors.getLength();
		for (int i = 0; i < sz; i++) {
			if (matcher.matches(selectors.item(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean isActivePseudoClass(String pseudoclassName) {
		return false;
	}

	@Override
	protected boolean isFirstChild() {
		Node sibling = element.getPreviousSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				return false;
			}
			sibling = sibling.getPreviousSibling();
		}
		return true;
	}

	@Override
	protected boolean isLastChild() {
		Node sibling = element.getNextSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				return false;
			}
			sibling = sibling.getNextSibling();
		}
		return true;
	}

	@Override
	protected boolean isFirstOfType() {
		Node sibling = element.getPreviousSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(localName(sibling))) {
				return false;
			}
			sibling = sibling.getPreviousSibling();
		}
		return true;
	}

	@Override
	protected boolean isLastOfType() {
		Node sibling = element.getNextSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(localName(sibling))) {
				return false;
			}
			sibling = sibling.getNextSibling();
		}
		return true;
	}

	@Override
	protected boolean isNthOfType(int step, int offset) {
		Node node = element.getParentNode().getFirstChild();
		int idx = 0;
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& getLocalName().equals(localName(node))) {
				idx++;
				if (node == element) {
					break;
				}
			}
			node = node.getNextSibling();
		}

		idx -= offset;
		return step == 0 ? idx == 0 : Math.floorMod(idx, step) == 0;
	}

	@Override
	protected boolean isNthLastOfType(int step, int offset) {
		Node node = element.getParentNode().getLastChild();
		int idx = 0;
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& getLocalName().equals(localName(node))) {
				idx++;
				if (node == element) {
					break;
				}
			}
			node = node.getPreviousSibling();
		}

		idx -= offset;
		return step == 0 ? idx == 0 : Math.floorMod(idx, step) == 0;
	}

	@Override
	protected boolean isTarget() {
		String uri = element.getOwnerDocument().getDocumentURI();
		int idx;
		if (uri != null && (idx = uri.lastIndexOf('#')) != -1) {
			idx++;
			int len = uri.length();
			return idx < len && getId().equals(uri.subSequence(idx, len));
		}
		return false;
	}

	@Override
	protected boolean isRoot() {
		return element.getOwnerDocument().getDocumentElement().isSameNode(element);
	}

	@Override
	protected boolean isEmpty() {
		Node node = element.getFirstChild();
		while (node != null) {
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				return false;
			} else if (type == Node.TEXT_NODE) {
				String value = node.getNodeValue();
				if (value.trim().length() != 0) {
					return false;
				}
			} else if (type == Node.ENTITY_REFERENCE_NODE) {
				return false;
			}
			node = node.getNextSibling();
		}

		return true;
	}

	@Override
	protected boolean isBlank() {
		Node node = element.getFirstChild();
		while (node != null) {
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				return false;
			} else if (type == Node.TEXT_NODE) {
				String value = node.getNodeValue();
				if (value != null && !((Text) node).isElementContentWhitespace()) {
					return false;
				}
			} else if (type == Node.ENTITY_REFERENCE_NODE) {
				return false;
			}
			node = node.getNextSibling();
		}

		return true;
	}

	@Override
	protected boolean isDisabled() {
		/*
		 * A form control is disabled if its disabled attribute is set, or if it is a descendant
		 * of a fieldset element whose disabled attribute is set and is not a descendant of that
		 * fieldset element's first legend element child, if any.
		 */
		if (element.hasAttribute("disabled")) {
			return true;
		}
		Node parent = element.getParentNode();
		if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE &&
				"fieldset".equals(parent.getNodeName()) && !"legend".equals(getLocalName())) {
			return ((Element)parent).hasAttribute("disabled");
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean isDefaultButton() {
		// "A form element's default button is the first submit button
		//  in tree order whose form owner is that form element."

		// First, determine the parent form and its form ID
		Node parent = element.getParentNode();
		if (parent == null) {
			return false;
		}
		while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
			if ("form".equalsIgnoreCase(((Element) parent).getTagName())) {
				break;
			}
			parent = parent.getParentNode();
		}
		String formid;
		if (parent == null) {
			formid = null;
		} else {
			formid = getElementId((E) parent);
		}

		// Now check whether it is the default button
		Node sibling = element.getPreviousSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) sibling;
				if (!defaultButtonCheck(element, formid)) {
					return false;
				}
				Node node = element.getFirstChild();
				while (node != null) {
					if (node.getNodeType() == Node.ELEMENT_NODE
							&& !defaultButtonCheck((Element) node, formid)) {
						return false;
					}
					node = node.getNextSibling();
				}
				sibling = sibling.getPreviousSibling();
			}
		}

		return true;
	}

	private static boolean defaultButtonCheck(Element element, String formid) {
		if (!element.hasAttribute("disabled")) {
			String form = element.getAttribute("form");
			if (form == null || form.equals(formid)) {
				String tagname = element.getTagName().toLowerCase(Locale.ROOT);
				if (tagname.equals("input")) {
					String type = element.getAttribute("type");
					return !"submit".equalsIgnoreCase(type) && !"image".equalsIgnoreCase(type);
				} else if (tagname.equals("button")) {
					return !"submit".equalsIgnoreCase(element.getAttribute("type"));
				}
			}
		}
		return true;
	}

	@Override
	protected String getNamespaceURI() {
		return element.getNamespaceURI();
	}

	@Override
	protected String getAttributeValue(String attrName) {
		return element.getAttribute(attrName);
	}

	@Override
	protected boolean hasAttribute(String attrName) {
		return element.hasAttribute(attrName);
	}

	@Override
	protected ComplianceMode getComplianceMode() {
		Document doc = getOwnerDocument();
		return doc.getDoctype() != null ? ComplianceMode.STRICT : ComplianceMode.QUIRKS;
	}

	protected Document getOwnerDocument() {
		return element.getOwnerDocument();
	}

	@Override
	protected String getId() {
		return getElementId(element);
	}

	protected String getElementId(E element) {
		String idAttr = element.getAttribute("id");
		if (idAttr.length() == 0 && getComplianceMode() == ComplianceMode.QUIRKS) {
			idAttr = getAttributeValue("ID");
			if (idAttr.length() == 0) {
				idAttr = getAttributeValue("Id");
			}
		}
		return idAttr;
	}

	@Override
	protected String getLanguage() {
		/*
		 * In (X)HTML, the lang attribute contains the language, but that may
		 * not be true for other XML.
		 */
		String lang = element.getAttribute("lang");
		Node parent = element;
		while (lang.length() == 0) {
			parent = parent.getParentNode();
			if (parent == null) {
				break;
			}
			if (!(parent instanceof org.w3c.dom.Element)) {
				continue;
			} else {
				lang = ((org.w3c.dom.Element) parent).getAttribute("lang");
			}
		}
		return lang;
	}

	@Override
	protected Directionality getDirectionality() {
		return DirectionalityHelper.getDirectionality(element);
	}

	@Override
	protected boolean scopeMatchChild(CombinatorSelector selector) {
		SimpleSelector desc = selector.getSecondSelector();
		Node node = element.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				@SuppressWarnings("unchecked")
				SelectorMatcher childSM = obtainSelectorMatcher((E) node);
				if (childSM.matches(desc)) {
					return true;
				}
			}
			node = node.getNextSibling();
		}
		return false;
	}

	@Override
	protected boolean scopeMatchDescendant(CombinatorSelector selector) {
		SimpleSelector desc = selector.getSecondSelector();
		Node first = element.getFirstChild();
		return scopeMatchRecursive(first, desc);
	}

	@Override
	public boolean matchesRelational(Selector selector) {
		Node first = element.getFirstChild();
		return scopeMatchRecursive(first, selector);
	}

	private boolean scopeMatchRecursive(Node node, Selector desc) {
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				@SuppressWarnings("unchecked")
				SelectorMatcher childSM = obtainSelectorMatcher((E) node);
				if (childSM.matches(desc) || scopeMatchRecursive(node.getFirstChild(), desc)) {
					return true;
				}
			}
			node = node.getNextSibling();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean scopeMatchDirectAdjacent(CombinatorSelector selector) {
		SelectorMatcher siblingSM = null;
		Node sibling = element.getNextSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				siblingSM = obtainSelectorMatcher((E) sibling);
				break;
			}
			sibling = sibling.getNextSibling();
		}
		if (siblingSM != null) {
			return siblingSM.matches(selector.getSecondSelector());
		}
		return false;
	}

	abstract protected SelectorMatcher obtainSelectorMatcher(E element);

}
