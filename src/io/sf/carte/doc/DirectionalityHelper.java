/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class to determine the directionality of an element.
 */
public class DirectionalityHelper {

	public enum Directionality {
		LTR,
		RTL
	}

	/**
	 * Determine the directionality of the given element.
	 * 
	 * @param element the element.
	 * @return the directionality.
	 */
	public static Directionality getDirectionality(Element element) {
		Node node;
		do {
			String dirAttr = element.getAttribute("dir");
			if (dirAttr.length() != 0) {
				if ("rtl".equalsIgnoreCase(dirAttr)) {
					return Directionality.RTL;
				} else if ("ltr".equalsIgnoreCase(dirAttr)) {
					break;
				} else if ("auto".equalsIgnoreCase(dirAttr)) {
					return autoDirection(element);
				} else if ("bdi".equals(element.getLocalName())) {
					return bdiDirection(element);
				} else if ("input".equals(element.getLocalName())
						&& "telephone".equalsIgnoreCase(element.getAttribute("type"))) {
					break;
				}
			} else if ("bdi".equals(element.getLocalName())) {
				return bdiDirection(element);
			}
			node = element.getParentNode();
			if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
				break;
			}
			element = (Element) node;
		} while (true);
		return Directionality.LTR;
	}

	private static Directionality autoDirection(Element element) {
		String name = element.getLocalName();
		if ("textarea".equals(name)) {
			String value = element.getTextContent().trim();
			return valueDirection(element, value);
		} else if ("input".equals(name)) {
			String value = element.getAttribute("value");
			return valueDirection(element, value);
		} else {
			byte ret = descendantTextDirection(element);
			switch (ret) {
			case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
				return Directionality.LTR;
			case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
				return Directionality.RTL;
			}
		}
		// Inconclusive
		return parentDirection(element);
	}

	private static Directionality bdiDirection(Element element) {
		byte ret = descendantTextDirection(element);
		switch (ret) {
		case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
			return Directionality.LTR;
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
			return Directionality.RTL;
		}
		// Inconclusive
		return parentDirection(element);
	}

	private static byte descendantTextDirection(Element element) {
		if (element.hasChildNodes()) {
			NodeList list = element.getChildNodes();
			if (list instanceof Iterable) {
				@SuppressWarnings("unchecked")
				Iterator<? extends Node> it = ((Iterable<? extends Node>) list).iterator();
				while (it.hasNext()) {
					Node node = it.next();
					switch (node.getNodeType()) {
					case Node.ELEMENT_NODE:
						element = (Element) node;
						String name = element.getLocalName();
						if ("bdi".equals(name) || "script".equals(name) || "style".equals(name)
								|| "textarea".equals(name) || hasLtrOrRtlDir(element)) {
							continue;
						}
						byte ret = descendantTextDirection(element);
						if (ret != -1) {
							return ret;
						}
						break;
					case Node.TEXT_NODE:
					case Node.CDATA_SECTION_NODE:
						String data = node.getNodeValue();
						int len = data.length();
						for (int i = 0; i < len; i = data.offsetByCodePoints(i, 1)) {
							int cp = data.codePointAt(i);
							byte cpDir = Character.getDirectionality(cp);
							switch (cpDir) {
							case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
								return Character.DIRECTIONALITY_LEFT_TO_RIGHT;
							case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
							case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
								return Character.DIRECTIONALITY_RIGHT_TO_LEFT;
							}
						}
					}
				}
			} else {
				for (int idx = 0; idx < list.getLength(); idx++) {
					Node node = list.item(idx);
					switch (node.getNodeType()) {
					case Node.ELEMENT_NODE:
						element = (Element) node;
						String name = element.getLocalName();
						if ("bdi".equals(name) || "script".equals(name) || "style".equals(name)
								|| "textarea".equals(name) || hasLtrOrRtlDir(element)) {
							continue;
						}
						byte ret = descendantTextDirection(element);
						if (ret != -1) {
							return ret;
						}
						break;
					case Node.TEXT_NODE:
					case Node.CDATA_SECTION_NODE:
						String data = node.getNodeValue();
						int len = data.length();
						for (int i = 0; i < len; i = data.offsetByCodePoints(i, 1)) {
							int cp = data.codePointAt(i);
							byte cpDir = Character.getDirectionality(cp);
							switch (cpDir) {
							case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
								return Character.DIRECTIONALITY_LEFT_TO_RIGHT;
							case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
							case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
								return Character.DIRECTIONALITY_RIGHT_TO_LEFT;
							}
						}
					}
				}
			}
		}
		return -1;
	}

	private static boolean hasLtrOrRtlDir(Element element) {
		String dir = element.getAttribute("dir");
		return "ltr".equalsIgnoreCase(dir) || "rtl".equalsIgnoreCase(dir);
	}

	private static Directionality parentDirection(Element element) {
		Node node = element.getParentNode();
		if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
			return getDirectionality((Element) node);
		}
		return Directionality.LTR;
	}

	private static Directionality valueDirection(Element element, String value) {
		/*
		 * If the element's value contains a character of bidirectional character type
		 * AL or R, and there is no character of bidirectional character type L anywhere
		 * before it in the element's value, then the directionality of the element is
		 * 'rtl'.
		 */
		int len = value.length();
		for (int i = 0; i < len; i = value.offsetByCodePoints(i, 1)) {
			int cp = value.codePointAt(i);
			byte cpDir = Character.getDirectionality(cp);
			switch (cpDir) {
			case Character.DIRECTIONALITY_LEFT_TO_RIGHT:
				return Directionality.LTR;
			case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
			case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
				return Directionality.RTL;
			}
		}
		// Inconclusive
		return parentDirection(element);
	}

}
