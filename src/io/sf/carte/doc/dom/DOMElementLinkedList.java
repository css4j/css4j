package io.sf.carte.doc.dom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;

import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSElement;

class DOMElementLinkedList extends LinkedList<DOMElement> implements ElementList {
	private static final long serialVersionUID = 1L;

	DOMElementLinkedList() {
		super();
	}

	@Override
	public boolean contains(Node node) {
		return super.contains(node);
	}

	@Override
	public DOMElement item(int index) {
		if (index < 0 || index >= size()) {
			return null;
		}
		return get(index);
	}

	@Override
	public int getLength() {
		return size();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(size() * 32 + 40);
		Iterator<DOMElement> it = iterator();
		while (it.hasNext()) {
			buf.append(it.next().toString());
		}
		return buf.toString();
	}

	void fillQuerySelectorList(SelectorList selist, Node firstChild) {
		Node node = firstChild;
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				DOMElement element = (DOMElement) node;
				if (element.matches(selist, null)) {
					add(element);
				}
				fillQuerySelectorList(selist, element.getFirstChild());
			}
			node = node.getNextSibling();
		}
	}

	void fillByTagList(String localName, AbstractDOMNode contextNode, String namespaceURI, boolean matchAll) {
		synchronized (contextNode) {
			Node node = contextNode.getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					DOMElement element = (DOMElement) node;
					if (matchAll || element.getLocalName().equals(localName)) {
						if (isSameNamespace(element, namespaceURI)) {
							add(element);
						}
					}
					fillByTagList(localName, element, namespaceURI, matchAll);
				}
				node = node.getNextSibling();
			}
		}
	}

	private static boolean isSameNamespace(DOMElement element, String ns2) {
		String ns1 = element.getNamespaceURI();
		if (ns1 == null) {
			return ns2 == null || element.isDefaultNamespace(ns2);
		}
		return ns1.equals(ns2) || (ns2 == null && element.isDefaultNamespace(ns1));
	}

	void fillByClassList(SortedSet<String> sorted, AbstractDOMNode contextNode) {
		synchronized (contextNode) {
			Node node = contextNode.getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					DOMElement element = (DOMElement) node;
					if (element.hasAttribute("class") && element.getClassList().containsAll(sorted)) {
						add(element);
					}
					fillByClassList(sorted, element);
				}
				node = node.getNextSibling();
			}
		}
	}

	void updateOnInsert(DOMElement newChild) {
		int sz = size();
		int i = 0;
		for (; i < sz; i++) {
			CSSElement elm = get(i);
			if ((elm.compareDocumentPosition(newChild)
					& Node.DOCUMENT_POSITION_FOLLOWING) == Node.DOCUMENT_POSITION_FOLLOWING) {
				break;
			}
		}
		add(i, newChild);
	}

	void updateOnRemove(DOMElement oldChild) {
		remove(oldChild);
	}
}

