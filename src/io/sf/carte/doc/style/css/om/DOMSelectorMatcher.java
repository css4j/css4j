/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.SelectorMatcher;

/**
 * CSS Selector matcher for DOM.
 * 
 * @author Carlos Amengual
 * 
 */
public class DOMSelectorMatcher extends AbstractSelectorMatcher {

	CSSElement element;

	public DOMSelectorMatcher(CSSElement elm) {
		super();
		element = elm;
		String name = elm.getLocalName();
		if (name == null) {
			name = elm.getTagName();
		}
		if (name != null) {
			name = name.toLowerCase(Locale.US).intern();
		}
		setLocalName(name);
	}

	@Override
	protected AbstractSelectorMatcher getParentSelectorMatcher() {
		Node parent = element.getParentNode();
		if (parent.getNodeType() == Node.ELEMENT_NODE) {
			// This should be always true if it is a correct tree
			return (AbstractSelectorMatcher) ((CSSElement) parent).getSelectorMatcher();
		} else {
			return null;
		}
	}

	@Override
	protected AbstractSelectorMatcher getPreviousSiblingSelectorMatcher() {
		Node sibling = element.getPreviousSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				return (AbstractSelectorMatcher) ((CSSElement) sibling).getSelectorMatcher();
			}
			sibling = sibling.getPreviousSibling();
		}
		return null;
	}

	@Override
	protected int indexOf(SelectorList selectors) {
		NodeList list = element.getParentNode().getChildNodes();
		int sz = list.getLength();
		int idx = 0;
		for (int i=0; i<sz; i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && matchSelectors(selectors, (CSSElement)node)) {
				idx++;
				if (node == element) {
					break;
				}
			}
		}
		return idx == sz ? -1 : idx;
	}

	@Override
	protected int reverseIndexOf(SelectorList selectors) {
		NodeList list = element.getParentNode().getChildNodes();
		int sz = list.getLength();
		int idx = 0;
		for (int i=sz-1; i>=0; i--) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && matchSelectors(selectors, (CSSElement)node)) {
				idx++;
				if (node == element) {
					break;
				}
			}
		}
		return idx;
	}

	private boolean matchSelectors(SelectorList selectors, CSSElement element) {
		if (selectors == null) {
			return true;
		}
		DOMSelectorMatcher matcher = new DOMSelectorMatcher(element);
		int sz = selectors.getLength();
		for (int i=0; i<sz; i++) {
			if (matcher.matches(selectors.item(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean isActivePseudoClass(String pseudoclassName) {
		CSSDocument doc = element.getOwnerDocument();
		CSSCanvas canvas;
		if (doc != null && (canvas = doc.getCanvas()) != null) {
			return canvas.isActivePseudoClass(element, pseudoclassName);
		}
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
			if (sibling.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(sibling.getNodeName())) {
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
			if (sibling.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(sibling.getNodeName())) {
				return false;
			}
			sibling = sibling.getNextSibling();
		}
		return true;
	}

	@Override
	protected boolean isNthOfType(int step, int offset) {
		NodeList list = element.getParentNode().getChildNodes();
		int sz = list.getLength();
		int idx = 0;
		for (int i=0; i<sz; i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(node.getNodeName())) {
				idx++;
				if (node == element) {
					break;
				}
			}
		}
		idx -= offset;
		return step == 0 ? idx == 0 : Math.floorMod(idx, step) == 0;
	}

	@Override
	protected boolean isNthLastOfType(int step, int offset) {
		NodeList list = element.getParentNode().getChildNodes();
		int sz = list.getLength();
		int idx = 0;
		for (int i=sz-1; i>=0; i--) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(node.getNodeName())) {
				idx++;
				if (node == element) {
					break;
				}
			}
		}
		idx -= offset;
		return step == 0 ? idx == 0 : Math.floorMod(idx, step) == 0;
	}

	@Override
	protected boolean isNotVisitedLink() {
		String href = element.getAttribute("href");
		if (href.length() != 0) {
			return !element.getOwnerDocument().isVisitedURI(href);
		} else {
			return false;
		}
	}

	@Override
	protected boolean isVisitedLink() {
		String href = element.getAttribute("href");
		if (href.length() != 0) {
			return element.getOwnerDocument().isVisitedURI(href);
		} else {
			return false;
		}
	}

	@Override
	protected boolean isTarget() {
		String uri = element.getOwnerDocument().getDocumentURI();
		int idx;
		if (uri != null && (idx = uri.lastIndexOf('#')) != -1) {
			idx++;
			int len = uri.length();
			if (idx < len && getId().equals(uri.subSequence(idx, len))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean isRoot() {
		return element.getOwnerDocument().getDocumentElement().isSameNode(element);
	}

	@Override
	protected boolean isEmpty() {
		if (element.hasChildNodes()) {
			NodeList list = element.getChildNodes();
			int sz = list.getLength();
			for (int i=0; i<sz; i++) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					return false;
				} else if (type == Node.TEXT_NODE) {
					String value = node.getNodeValue();
					if (value.trim().length() != 0) {
						return false;
					}
				} else if (type == Node.ENTITY_REFERENCE_NODE && node.hasChildNodes()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected boolean isBlank() {
		if (element.hasChildNodes()) {
			NodeList list = element.getChildNodes();
			int sz = list.getLength();
			for (int i=0; i<sz; i++) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					return false;
				} else if (type == Node.TEXT_NODE) {
					String value = node.getNodeValue();
					if (value != null && !((Text) node).isElementContentWhitespace()) {
						return false;
					}
				} else if (type == Node.ENTITY_REFERENCE_NODE && node.hasChildNodes()) {
					return false;
				}
			}
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
			return ((CSSElement)parent).hasAttribute("disabled");
		}
		return false;
	}

	@Override
	protected boolean isDefaultButton() {
		// "A form element's default button is the first submit button
		//  in tree order whose form owner is that form element."
		Node parent = element.getParentNode();
		if (parent == null) {
			return false;
		}
		while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
			if ("form".equals(((CSSElement) parent).getTagName())) {
				break;
			}
			parent = parent.getParentNode();
		}
		String formid;
		if (parent == null) {
			formid = null;
		} else {
			formid = ((CSSElement) parent).getId();
		}
		Node sibling = element.getPreviousSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				CSSElement element = (CSSElement) sibling;
				if (!defaultButtonCheck(element, formid)) {
					return false;
				}
				if (element.hasChildNodes()) {
					NodeList list = element.getChildNodes();
					int sz = list.getLength();
					for (int i = 0; i < sz; i++) {
						Node node = list.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							if (!defaultButtonCheck((CSSElement)node, formid)) {
								return false;
							}
						}
					}
				}
				sibling = sibling.getPreviousSibling();
			}
		}
		return true;
	}

	private static boolean defaultButtonCheck(CSSElement element, String formid) {
		if (!element.hasAttribute("disabled")) {
			String form = element.getAttribute("form");
			if (form == null || form.equals(formid)) {
				String tagname = element.getTagName().toLowerCase(Locale.US);
				if (tagname.equals("input")) {
					String type = element.getAttribute("type");
					if ("submit".equals(type) || "image".equals(type)) {
						return false;
					}
				} else if (tagname.equals("button")) {
					if ("submit".equals(element.getAttribute("type"))) {
						return false;
					}
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
	protected CSSDocument.ComplianceMode getComplianceMode() {
		return element.getOwnerDocument().getComplianceMode();
	}

	@Override
	protected String getId() {
		return element.getId();
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
	protected boolean scopeMatchChild(DescendantSelector selector) {
		SimpleSelector desc = selector.getSimpleSelector();
		NodeList list = element.getChildNodes();
		int sz = list.getLength();
		for (int i=0; i<sz; i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				SelectorMatcher childSM = new DOMSelectorMatcher((CSSElement) node);
				if (childSM.matches(desc)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean scopeMatchDirectAdjacent(SiblingSelector selector) {
		SelectorMatcher siblingSM = null;
		Node sibling = element.getNextSibling();
		while (sibling != null) {
			if (sibling.getNodeType() == Node.ELEMENT_NODE) {
				siblingSM = new DOMSelectorMatcher((CSSElement) sibling);
				break;
			}
			sibling = sibling.getNextSibling();
		}
		if (siblingSM != null) {
			return siblingSM.matches(selector.getSiblingSelector());
		}
		return false;
	}

}
