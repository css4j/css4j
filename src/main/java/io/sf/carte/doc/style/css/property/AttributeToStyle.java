/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import io.sf.carte.doc.style.css.CSSStyleDeclaration;

/**
 * Transforms legacy HTML attributes into style declarations.
 *
 */
public final class AttributeToStyle {

	/**
	 * Set a style according to the {@code align} attribute of table elements
	 * interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
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

	/**
	 * Set a style according to the {@code bgcolor} attribute of table elements
	 * interpreted as a presentational hint.
	 * 
	 * @param bgcolorAttr the attribute value.
	 * @param style       the style to set.
	 */
	public static void bgcolor(String bgcolorAttr, CSSStyleDeclaration style) {
		bgcolorAttr = bgcolorAttr.trim();
		if (bgcolorAttr.length() != 0) {
			style.setProperty("background-color", bgcolorAttr, null);
		}
	}

	/**
	 * Set a style according to the {@code height} attribute of legacy HTML 4.01
	 * elements interpreted as a presentational hint.
	 * 
	 * @param widthAttr the attribute value.
	 * @param style     the style to set.
	 */
	public static void width(String widthAttr, CSSStyleDeclaration style) {
		int attlen = widthAttr.length();
		if (attlen != 0) {
			char c = widthAttr.charAt(attlen - 1);
			if (isDigit(c)) {
				widthAttr += "px";
			}
			style.setProperty("width", widthAttr, null);
		}
	}

	private static boolean isDigit(char c) {
		return c >= 0x30 && c <= 0x39;
	}

	/**
	 * Set a style according to the {@code height} attribute of legacy HTML 4.01
	 * elements interpreted as a presentational hint.
	 * 
	 * @param heightAttr the attribute value.
	 * @param style      the style to set.
	 */
	public static void height(String heightAttr, CSSStyleDeclaration style) {
		int attlen = heightAttr.length();
		if (attlen != 0) {
			char c = heightAttr.charAt(attlen - 1);
			if (isDigit(c)) {
				heightAttr += "px";
			}
			style.setProperty("height", heightAttr, null);
		}
	}

	/**
	 * Set a style according to the {@code size} attribute of {@code FONT} elements
	 * interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
	public static void face(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		if (attribute.length() != 0) {
			style.setProperty("font-family", attribute, null);
		}
	}

	/**
	 * Set a style according to the {@code size} attribute of {@code FONT} elements
	 * interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
	public static void size(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		if (attribute.length() != 0) {
			String sz = translateFontSize(attribute);
			if (!sz.isEmpty()) {
				style.setProperty("font-size", sz, null);
			}
		}
	}

	private static String translateFontSize(String size) {
		// It is assumed was previously checked that size.length()
		// is greater than zero.
		if (size.charAt(0) == '+') {
			return "larger";
		} else if (size.charAt(0) == '-') {
			return "smaller";
		} else {
			int sz;
			try {
				sz = Integer.parseInt(size);
			} catch (NumberFormatException e) {
				return "";
			}
			switch (sz) {
			case 1:
				size = "8px";
				break;
			case 2:
				size = "10px";
				break;
			case 3:
				size = "12px";
				break;
			case 4:
				size = "14px";
				break;
			case 5:
				size = "18px";
				break;
			case 6:
				size = "24px";
				break;
			case 7:
				size = "28px";
				break;
			default:
				size = "";
			}
			return size;
		}
	}

	/**
	 * Set a style according to the {@code color} attribute of {@code FONT} elements
	 * interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
	public static void color(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		if (attribute.length() != 0) {
			style.setProperty("color", attribute, null);
		}
	}

	/**
	 * Set a style according to the {@code border} attribute of table elements
	 * interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
	public static boolean border(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			char c = attribute.charAt(attlen - 1);
			if (isDigit(c)) {
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

	/**
	 * Set a style according to the {@code bordercolor} attribute of table elements
	 * interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
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

	/**
	 * Set a style according to the {@code cell-spacing} attribute of table elements
	 * interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
	public static void cellSpacing(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			char c = attribute.charAt(attlen - 1);
			if (isDigit(c)) {
				attribute += "px";
			}
			style.setProperty("border-spacing", attribute, null);
		}
	}

	/**
	 * Set a style according to the {@code background} attribute of table elements
	 * interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
	public static void background(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		if (attribute.length() != 0) {
			style.setProperty("background-image", attribute, null);
		}
	}

	/**
	 * Set a style according to the {@code hspace} attribute of the {@code IMG}
	 * element interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
	public static void hspace(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			char c = attribute.charAt(attlen - 1);
			if (isDigit(c)) {
				attribute += "px";
			}
			style.setProperty("margin-right", attribute, null);
			style.setProperty("margin-left", attribute, null);
		}
	}

	/**
	 * Set a style according to the {@code vspace} attribute of the {@code IMG}
	 * element interpreted as a presentational hint.
	 * 
	 * @param attribute the attribute value.
	 * @param style     the style to set.
	 */
	public static void vspace(String attribute, CSSStyleDeclaration style) {
		attribute = attribute.trim();
		int attlen = attribute.length();
		if (attlen != 0) {
			char c = attribute.charAt(attlen - 1);
			if (isDigit(c)) {
				attribute += "px";
			}
			style.setProperty("margin-top", attribute, null);
			style.setProperty("margin-bottom", attribute, null);
		}
	}

}
