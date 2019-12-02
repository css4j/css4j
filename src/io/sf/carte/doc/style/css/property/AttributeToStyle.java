/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSStyleDeclaration;

/**
 * Transforms HTML attributes into style declarations.
 * 
 * @author Carlos Amengual
 *
 */
public final class AttributeToStyle {

	public static void align(String attribute, CSSStyleDeclaration style) {
		if (attribute.equalsIgnoreCase("center") || attribute.equalsIgnoreCase("middle")) {
			style.setProperty("text-align", "center", null);
		} else if (attribute.equalsIgnoreCase("justify")) {
			style.setProperty("text-align", "justify", null);
		} else if (attribute.equalsIgnoreCase("left")) {
			style.setProperty("text-align", "left", null);
		} else if (attribute.equalsIgnoreCase("right")) {
			style.setProperty("text-align", "right", null);
		}
	}

	public static void bgcolor(String bgcolorAttr, CSSStyleDeclaration style) {
		bgcolorAttr = bgcolorAttr.trim();
		if (bgcolorAttr.length() != 0) {
			style.setProperty("background-color", bgcolorAttr, null);
		}
	}

	public static void width(String widthAttr, CSSStyleDeclaration style) {
		int attlen = widthAttr.length();
		if (attlen != 0) {
			char c = widthAttr.charAt(attlen - 1);
			if (Character.isDigit(c)) {
				widthAttr += "px";
			}
			style.setProperty("width", widthAttr, null);
		}
	}

	public static void height(String heightAttr, CSSStyleDeclaration style) {
		int attlen = heightAttr.length();
		if (attlen != 0) {
			char c = heightAttr.charAt(attlen - 1);
			if (Character.isDigit(c)) {
				heightAttr += "px";
			}
			style.setProperty("height", heightAttr, null);
		}
	}

	public static void face(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		if (attribute.length() != 0) {
			style.setProperty("font-family", attribute, null);
		}
	}

	public static void size(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		if (attribute.length() != 0) {
			style.setProperty("font-size", translateFontSize(attribute), null);
		}
	}

	private static String translateFontSize(String size) {
		// It is assumed was previously checked that size.length() 
		// is greater than zero.
		if(size.charAt(0) == '+') {
			return "larger";
		} else if(size.charAt(0) == '-') {
			return "smaller";
		} else {
			return size;
		}
	}

	public static void color(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		if (attribute.length() != 0) {
			style.setProperty("color", attribute, null);
		}
	}

	public static boolean border(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			char c = attribute.charAt(attlen - 1);
			if (Character.isDigit(c)) {
				attribute += "px";
			}
			style.setProperty("border-top-width", attribute, null);
			style.setProperty("border-right-width", attribute, null);
			style.setProperty("border-bottom-width", attribute, null);
			style.setProperty("border-left-width", attribute, null);
			return true;
		}
		return false;
	}

	public static void borderColor(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			style.setProperty("border-top-color", attribute, null);
			style.setProperty("border-right-color", attribute, null);
			style.setProperty("border-bottom-color", attribute, null);
			style.setProperty("border-left-color", attribute, null);
		}
	}

	public static void cellSpacing(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			char c = attribute.charAt(attlen - 1);
			if (Character.isDigit(c)) {
				attribute += "px";
			}
			style.setProperty("border-spacing", attribute, null);
		}
	}

	public static void background(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		if (attribute.length() != 0) {
			style.setProperty("background-image", attribute, null);
		}
	}

	public static void hspace(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			style.setProperty("margin-right", attribute, null);
			style.setProperty("margin-left", attribute, null);
		}
	}

	public static void vspace(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			style.setProperty("margin-top", attribute, null);
			style.setProperty("margin-bottom", attribute, null);
		}
	}

}
