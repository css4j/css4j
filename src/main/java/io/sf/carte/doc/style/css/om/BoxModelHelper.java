/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.om.SimpleBoxModel.MyTableBoxValues;
import io.sf.carte.doc.style.css.property.NumberValue;

/**
 * Box model helper class.
 */
public class BoxModelHelper {

	/**
	 * Contracts all the spaces of a given String, leaving only one space
	 * between characters
	 * 
	 * @param t
	 *            the string to compact.
	 * @return the compacted string.
	 */
	public static String contractSpaces(String t) {
		int tl = t.length();
		StringBuilder sb = new StringBuilder(tl);
		int ini = 0;
		int fin = 0;
		while ((ini = t.indexOf("  ", fin)) != -1) {
			sb.append(t, fin, ini);
			fin = ini;
			while (++fin < tl && t.charAt(fin) == ' ')
				;
			fin--;
		}
		sb.append(t.substring(fin));
		return sb.toString();
	}

	/**
	 * Compute the minimum width of a node that contains the given text.
	 * 
	 * @param text
	 *            the text content of he node.
	 * @param style
	 *            the style that applies to the node.
	 * @param canvas
	 *            the canvas that is going to draw the node.
	 * @param unitType
	 *            the desired unit type. If negative, uses the style's natural unit.
	 * @return the minimum width of the node.
	 */
	public static float computeNodeMinimumWidth(String text, ComputedCSSStyle style, CSSCanvas canvas, short unitType) {
		int tlen = text.length();
		int maxsz = 0;
		int j = 0;
		int i = 0;
		while (i < tlen) {
			if (isSeparator(text.charAt(i))) {
				int k = canvas.stringWidth(text.substring(j, i), style);
				if (k > maxsz) {
					maxsz = k;
				}
				j = i;
				j++;
				while (j < tlen && isSeparator(text.charAt(j))) {
					j++;
				}
				i = j;
			} else {
				i++;
			}
		}
		if (j > 0) {
			int k = canvas.stringWidth(text.substring(j, tlen), style);
			if (k > maxsz) {
				maxsz = k;
			}
		} else {
			maxsz = canvas.stringWidth(text, style);
		}
		return NumberValue.floatValueConversion(maxsz, CSSUnit.CSS_PT, unitType);
	}

	/**
	 * Compute the approximate minimum width of a node that contains the given text.
	 * 
	 * @param text
	 *            the text content of he node.
	 * @param styledecl
	 *            the style that applies to the node.
	 * @param unitType
	 *            the desired unit type. If negative, uses the style's natural unit.
	 * @return the approximate minimum width of the node.
	 */
	public static float computeNodeMinimumWidth(String text, ComputedCSSStyle styledecl, short unitType) {
		float maxsz = computeMinimumCharsWidth(text);
		if (maxsz == 0f) {
			return 0f;
		}
		if (styledecl.getStyleDatabase() == null) {
			if (unitType < 0) {
				throw new StyleDatabaseRequiredException();
			}
			maxsz *= 0.5f * styledecl.getComputedFontSize();
		} else {
			if (unitType < 0) {
				unitType = styledecl.getStyleDatabase().getNaturalUnit();
			}
			maxsz *= styledecl.getStyleDatabase().getExSizeInPt(styledecl.getUsedFontFamily(),
					styledecl.getComputedFontSize());
		}
		return NumberValue.floatValueConversion(maxsz, CSSUnit.CSS_PT, unitType);
	}

	/**
	 * Find the number of characters of the longest word in the supplied text.
	 * 
	 * @param text
	 *            the text to evaluate.
	 * @return the character length of the longest word in text.
	 */
	public static int computeMinimumCharsWidth(String text) {
		// find size of the longest word in text
		int tlen = text.length();
		int maxsz = 0;
		int j = 0;
		int i = 0;
		while (i < tlen) {
			if (isSeparator(text.charAt(i))) {
				int k = i - j;
				if (k > maxsz) {
					maxsz = k;
				}
				j = i;
				j++;
				while (j < tlen && isSeparator(text.charAt(j))) {
					j++;
				}
				i = j;
			} else {
				i++;
			}
		}
		if (j > 0) {
			if (tlen - j > maxsz) {
				maxsz = tlen - j;
			}
		} else {
			maxsz = tlen;
		}
		return maxsz;
	}

	private static boolean isSeparator(char c) {
		return Character.isWhitespace(c);
	}

	/**
	 * Compute the approximate width of the given text, in the given unit type.
	 * 
	 * @param text
	 *            the text to evaluate.
	 * @param styledecl
	 *            the style that applies to the text.
	 * @param unitType
	 *            the desired unit type. If negative, uses the style's natural unit.
	 * @return the approximate width of the given text.
	 */
	public static float computeTextWidth(String text, ComputedCSSStyle styledecl, short unitType) {
		float maxsz = text.length();
		if (maxsz == 0f) {
			return 0f;
		}
		if (styledecl.getStyleDatabase() == null) {
			if (unitType < 0) {
				throw new StyleDatabaseRequiredException();
			}
			maxsz *= 0.5f * styledecl.getComputedFontSize();
		} else {
			if (unitType < 0) {
				unitType = styledecl.getStyleDatabase().getNaturalUnit();
			}
			maxsz *= styledecl.getStyleDatabase().getExSizeInPt(styledecl.getUsedFontFamily(),
					styledecl.getComputedFontSize());
		}
		return NumberValue.floatValueConversion(maxsz, CSSUnit.CSS_PT, unitType);
	}

	/**
	 * Shrinks the widths of the columns of a table box to a desired value.
	 * 
	 * @param box
	 *            the table box.
	 * @param minrcw
	 *            the array with the minimally acceptable column widths.
	 * @param minwidth
	 *            the sum of the widths in minrcw.
	 * @param curwidth
	 *            the current sum of the column widths in <code>box</code>.
	 * @param width
	 *            the desired with (to shrink to).
	 */
	static void shrinkTo(MyTableBoxValues box, float[] minrcw, float minwidth, float curwidth, float width) {
		int shrinkableCount = 0;
		for (int i = 0; i < minrcw.length; i++) {
			if (box.colwidth[i] > minrcw[i]) {
				shrinkableCount++;
			}
		}
		if (shrinkableCount == 0) {
			return;
		}
		float delta = curwidth - width;
		float coldelta = delta / shrinkableCount;
		float shrink = 0f;
		curwidth = 0f;
		for (int i = 0; i < minrcw.length; i++) {
			float colwidth = box.colwidth[i];
			if (colwidth > minrcw[i]) {
				colwidth -= coldelta;
				if (colwidth < minrcw[i]) {
					colwidth = minrcw[i];
				}
				shrink += box.colwidth[i] - colwidth;
				box.colwidth[i] = colwidth;
			}
			curwidth += colwidth;
		}
		if (Math.abs(delta - shrink) > 0.1f) {
			shrinkTo(box, minrcw, minwidth, curwidth, width);
		}
	}

}
