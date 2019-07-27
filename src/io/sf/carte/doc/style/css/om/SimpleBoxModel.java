/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.NumberValue;

/**
 * Simple CSS Box Model.
 * <p>
 * Does not take into account pseudo-elements, pseudo-classes, floating content, and makes
 * other assumptions to simplify the computations of the CSS box model.
 */
abstract class SimpleBoxModel {

	SimpleBoxModel() {
		super();
	}

	static class MyBoxValues implements BoxValues {

		float marginTop = 0f;
		float marginRight = 0f;
		float marginBottom = 0f;
		float marginLeft = 0f;
		float paddingTop = 0f;
		float paddingRight = 0f;
		float paddingBottom = 0f;
		float paddingLeft = 0f;
		float borderTopWidth = 0f;
		float borderBottomWidth = 0f;
		float borderRightWidth = 0f;
		float borderLeftWidth = 0f;
		float width = Float.MIN_VALUE;

		@Override
		public float getMarginTop() {
			return marginTop;
		}

		@Override
		public float getMarginRight() {
			return marginRight;
		}

		@Override
		public float getMarginBottom() {
			return marginBottom;
		}

		@Override
		public float getMarginLeft() {
			return marginLeft;
		}

		@Override
		public float getPaddingTop() {
			return paddingTop;
		}

		@Override
		public float getPaddingRight() {
			return paddingRight;
		}

		@Override
		public float getPaddingBottom() {
			return paddingBottom;
		}

		@Override
		public float getPaddingLeft() {
			return paddingLeft;
		}

		@Override
		public float getBorderTopWidth() {
			return borderTopWidth;
		}

		@Override
		public float getBorderRightWidth() {
			return borderRightWidth;
		}

		@Override
		public float getBorderBottomWidth() {
			return borderBottomWidth;
		}

		@Override
		public float getBorderLeftWidth() {
			return borderLeftWidth;
		}

		@Override
		public float getWidth() {
			return width;
		}
	}

	static class MyTableItemBoxValues extends MyBoxValues {
		@Override
		public float getWidth() {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
					"Operation not supported by this box model. Please compute box for table and cast to TableBoxValues.");
		}
	}

	static class MyTableBoxValues extends MyBoxValues implements BoxValues.TableBoxValues {
		float[] colwidth;

		@Override
		public float[] getColumnsContentWidth() {
			return colwidth;
		}
	}

	abstract protected ComputedCSSStyle getComputedStyle();

	private StyleDatabase getStyleDatabase() {
		return getComputedStyle().getStyleDatabase();
	}

	private void computeSharedBoxValues(MyBoxValues box, short unitType) {
		ComputedCSSStyle styledecl = getComputedStyle();
		CSSValue cssval = styledecl.getCSSValue("margin-top");
		while (cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
			styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCSSValue("margin-top");
			}
		}
		if (styledecl == null) {
			box.marginTop = 0f;
		} else {
			if (cssval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				if (((CSSPrimitiveValue) cssval).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
						&& "auto".equals(((CSSPrimitiveValue) cssval).getStringValue())) {
					box.marginTop = 0f;
				} else {
					box.marginTop = computeMarginNumberValue(styledecl, "margin-top", (CSSPrimitiveValue) cssval,
							unitType);
				}
			} else {
				CSSPropertyValueException e = new CSSPropertyValueException(
						"Expected primitive value for margin-top, found " + cssval.getCssText());
				styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-top", e);
				box.marginTop = 0f;
			}
		}
		styledecl = getComputedStyle();
		cssval = styledecl.getCSSValue("margin-bottom");
		while (cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
			styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCSSValue("margin-bottom");
			}
		}
		if (styledecl == null) {
			box.marginBottom = 0f;
		} else {
			if (cssval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				if (((CSSPrimitiveValue) cssval).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
						&& "auto".equals(((CSSPrimitiveValue) cssval).getStringValue())) {
					box.marginBottom = 0f;
				} else {
					box.marginBottom = computeMarginNumberValue(styledecl, "margin-bottom", (CSSPrimitiveValue) cssval,
							unitType);
				}
			} else {
				CSSPropertyValueException e = new CSSPropertyValueException(
						"Expected primitive value for margin-bottom, found " + cssval.getCssText());
				styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-bottom", e);
				box.marginBottom = 0f;
			}
		}
		// Padding (no 'auto' applies to padding)
		box.paddingTop = computePaddingSubproperty(styledecl, "padding-top", unitType);
		box.paddingRight = computePaddingSubproperty(styledecl, "padding-right", unitType);
		box.paddingBottom = computePaddingSubproperty(styledecl, "padding-bottom", unitType);
		box.paddingLeft = computePaddingSubproperty(styledecl, "padding-left", unitType);
		box.borderTopWidth = findBorderWidthProperty(styledecl, "border-top-width",
				styledecl.getCSSValue("border-top-width"), unitType);
		box.borderBottomWidth = findBorderWidthProperty(styledecl, "border-bottom-width",
				styledecl.getCSSValue("border-bottom-width"), unitType);
		// Border left & right width
		// If the element is block-level, these values can be later modified when computing width
		box.borderLeftWidth = findBorderWidthProperty(styledecl, "border-left-width",
				styledecl.getCSSValue("border-left-width"), unitType);
		box.borderRightWidth = findBorderWidthProperty(styledecl, "border-right-width",
				styledecl.getCSSValue("border-right-width"), unitType);
	}

	/**
	 * Gets the computed values of the box model properties for a non-replaced inline box,
	 * expressed in the given unit.
	 * 
	 * @param unitType
	 *            the desired result unit type. If less than zero and a style database is
	 *            available, it will be attempted to use it to convert to the unit given by
	 *            the <code>StyleDatabase.getNaturalUnit()</code> method.
	 * @return the computed values of the box model properties.
	 * @throws StyleDatabaseRequiredException
	 *             when a computation that requires a style database is attempted, but no
	 *             style database has been set.
	 */
	void computeInlineBox(MyBoxValues box, short unitType) throws StyleDatabaseRequiredException {
		ComputedCSSStyle styledecl = getComputedStyle();
		CSSValue cssval = styledecl.getCSSValue("margin-right");
		while (cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
			styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCSSValue("margin-right");
			}
		}
		if (styledecl == null) {
			box.marginRight = 0f;
		} else {
			if (cssval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				if (((CSSPrimitiveValue) cssval).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT
						|| !"auto".equals(((CSSPrimitiveValue) cssval).getStringValue())) {
					box.marginRight = computeMarginNumberValue(styledecl, "margin-right", (CSSPrimitiveValue) cssval,
							unitType);
				}
			} else {
				CSSPropertyValueException e = new CSSPropertyValueException(
						"Expected primitive value for margin-right, found " + cssval.getCssText());
				styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-right", e);
				box.marginRight = 0f;
			}
		}
		styledecl = getComputedStyle();
		cssval = styledecl.getCSSValue("margin-left");
		while (cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
			styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCSSValue("margin-left");
			}
		}
		if (styledecl == null) {
			box.marginLeft = 0f;
		} else {
			if (cssval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				if (((CSSPrimitiveValue) cssval).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT
						|| !"auto".equals(((CSSPrimitiveValue) cssval).getStringValue())) {
					box.marginLeft = computeMarginNumberValue(styledecl, "margin-left", (CSSPrimitiveValue) cssval,
							unitType);
				}
			} else {
				CSSPropertyValueException e = new CSSPropertyValueException(
						"Expected primitive value for margin-left, found " + cssval.getCssText());
				styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-left", e);
				box.marginLeft = 0f;
			}
		}
		Node node = styledecl.getOwnerNode();
		if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
			CSSElement elm = (CSSElement) node;
			if ("img".equals(elm.getTagName()) && elm.hasAttribute("width")) {
				// Replaced element
				try {
					box.width = Float.parseFloat(elm.getAttribute("width"));
				} catch (NumberFormatException e) {
					elm.getOwnerDocument().getStyleSheet().getErrorHandler().computedStyleError(elm, "width",
							elm.getAttribute("width"), "Could not parse value of 'width' attribute for img element");
					;
				}
				if (unitType != CSSPrimitiveValue.CSS_PX) {
					box.width = NumberValue.floatValueConversion(box.width, CSSPrimitiveValue.CSS_PX, unitType);
				}
			}
		}
	}

	/**
	 * Gets the computed values of the box model properties, expressed in the given unit.
	 * 
	 * @param unitType
	 *            the desired result unit type. If less than zero and a style database is
	 *            available, it will be converted to the unit given by the
	 *            <code>StyleDatabase.getNaturalUnit()</code> method.
	 * @return the computed values of the box model properties.
	 * @throws DOMException
	 *             if the document contains features that are not supported by the simple
	 *             model.
	 * @throws StyleDatabaseRequiredException
	 *             when a computation that requires a style database is attempted, but no
	 *             style database has been set.
	 */
	public BoxValues getComputedBox(short unitType) throws DOMException, StyleDatabaseRequiredException {
		MyBoxValues box;
		String display = getComputedStyle().getDisplay();
		if ("block".equals(display) || "list-item".equals(display)) {
			box = new MyBoxValues();
			computeSharedBoxValues(box, unitType);
			computeBlockBox(box, unitType);
			return box;
		} else if ("table".equals(display)) {
			box = new MyTableBoxValues();
			computeSharedBoxValues(box, unitType);
			String tableLayout = getComputedStyle().getPropertyValue("table-layout");
			if ("auto".equals(tableLayout)) {
				computeTableBox((MyTableBoxValues) box, unitType);
				return box;
			} else {
				computeBlockBox(box, unitType);
				return box;
			}
		} else if ("table-cell".equals(display) || "table-row".equals(display)) {
			box = new MyTableItemBoxValues();
			computeTableCellBox(box, unitType);
			return box;
		}
		box = new MyBoxValues();
		computeSharedBoxValues(box, unitType);
		computeInlineBox(box, unitType);
		return box;
	}

	void computeBlockBox(MyBoxValues box, short unitType) throws StyleDatabaseRequiredException {
		boolean margin_right_auto = false;
		ComputedCSSStyle styledecl = getComputedStyle();
		CSSValue cssval = styledecl.getCSSValue("margin-right");
		while (cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
			styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCSSValue("margin-right");
			}
		}
		if (cssval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			margin_right_auto = ((CSSPrimitiveValue) cssval).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
					&& "auto".equals(((CSSPrimitiveValue) cssval).getStringValue());
			if (!margin_right_auto) {
				box.marginRight = computeMarginNumberValue(styledecl, "margin-right", (CSSPrimitiveValue) cssval,
						unitType);
			}
		} else {
			CSSPropertyValueException e = new CSSPropertyValueException(
					"Expected primitive value for margin-right, found " + cssval.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-right", e);
			box.marginRight = 0f;
		}
		styledecl = getComputedStyle();
		boolean margin_left_auto = false;
		cssval = styledecl.getCSSValue("margin-left");
		while (cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
			styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCSSValue("margin-left");
			}
		}
		if (cssval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			margin_left_auto = ((CSSPrimitiveValue) cssval).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
					&& "auto".equals(((CSSPrimitiveValue) cssval).getStringValue());
			if (!margin_left_auto) {
				box.marginLeft = computeMarginNumberValue(styledecl, "margin-left", (CSSPrimitiveValue) cssval,
						unitType);
			}
		} else {
			CSSPropertyValueException e = new CSSPropertyValueException(
					"Expected primitive value for margin-left, found " + cssval.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-left", e);
			box.marginLeft = 0f;
		}
		// The rest of values...
		styledecl = getComputedStyle();
		// Width
		cssval = styledecl.getCSSValue("width");
		if (cssval == null || "auto".equals(cssval.getCssText())) {
			// width: auto
			if (margin_right_auto) {
				box.marginRight = 0f;
			}
			if (margin_left_auto) {
				box.marginLeft = 0f;
			}
			float contBlockWidth;
			ComputedCSSStyle contblockStyledecl = findContainingBlockStyle(styledecl);
			if (contblockStyledecl == null) {
				contBlockWidth = deviceDocumentWidth("width is auto, and cannot find top block width.", "auto");
			} else {
				contBlockWidth = computeWidth(contblockStyledecl, unitType);
			}
			box.width = contBlockWidth - (box.marginLeft + box.marginRight + box.borderLeftWidth + box.borderRightWidth
					+ box.paddingLeft + box.paddingRight);
		} else {
			// Non-auto width
			box.width = computeNonAutoWidth(styledecl, cssval, unitType);
			float contBlockWidth;
			ComputedCSSStyle contblockStyledecl = findContainingBlockStyle(styledecl);
			if (contblockStyledecl == null) {
				contBlockWidth = deviceDocumentWidth("width is auto, and cannot find top block width.", "auto");
			} else {
				contBlockWidth = computeWidth(styledecl, unitType);
			}
			float remMargin = contBlockWidth - box.width;
			remMargin -= box.borderLeftWidth + box.borderRightWidth + box.paddingLeft + box.paddingRight;
			if (margin_right_auto) {
				if (remMargin < 0f) {
					box.marginRight = 0f;
					box.marginLeft = 0f;
				} else if (margin_left_auto) {
					// both margins are auto
					box.marginRight = remMargin / 2f;
					box.marginLeft = box.marginRight;
				} else {
					// margin-right is auto, margin-left isn't
					if (remMargin < box.marginLeft) {
						box.marginLeft = remMargin;
						box.marginRight = 0f;
					} else {
						box.marginRight = remMargin - box.marginLeft;
					}
				}
			} else if (margin_left_auto) {
				// margin-left is auto, margin-right isn't
				if (remMargin < 0f) {
					box.marginRight = 0f;
					box.marginLeft = 0f;
				} else if (remMargin < box.marginRight) {
					box.marginRight = remMargin;
					box.marginLeft = 0f;
				} else {
					box.marginLeft = remMargin;
				}
			} else {
				remMargin -= box.marginLeft + box.marginRight;
				if (remMargin < 0f) {
					// Check for over-constraining
					if ("ltr".equals(styledecl.getPropertyValue("direction"))) {
						// Ignore margin-right declared value
						remMargin += box.marginRight;
						if (remMargin > 0f) {
							box.marginRight = remMargin;
						} else {
							box.marginRight = 0f;
						}
					} else {
						// Ignore margin-left declared value
						remMargin += box.marginLeft;
						if (remMargin > 0f) {
							box.marginLeft = remMargin;
						} else {
							box.marginLeft = 0f;
						}
					}
				}
			}
		}
	}

	/*
	 * Document should be normalized for this to work
	 */
	private void computeTableBox(MyTableBoxValues box, short unitType) {
		computeBlockBox(box, unitType);
		// width
		// compute caption width minimum
		float capmin = computeCapmin(unitType);
		// compute minimum width required by all the columns plus cell spacing or borders
		ComputedCSSStyle style = getComputedStyle();
		CSSElement tbl = (CSSElement) style.getOwnerNode();
		NodeList nlist = tbl.getElementsByTagName("tbody");
		if (nlist.getLength() > 0) {
			// has tbody
			nlist = nlist.item(0).getChildNodes();
		} else {
			nlist = tbl.getChildNodes();
		}
		// Now nlist has the row nodes.
		// Count number of columns
		int ncol = 0;
		int nrows = nlist.getLength();
		for (int i = 0; i < nrows; i++) {
			Node node = nlist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				CSSElement elm = (CSSElement) node;
				if ("tr".equals(elm.getTagName().toLowerCase(Locale.US))) {
					int rowcols = elm.getChildNodes().getLength();
					if (rowcols > ncol) {
						ncol = rowcols;
					}
				}
			}
		}
		float[] minrcw = new float[ncol];
		float[] maxrcw = new float[ncol];
		float maxRowSpacing = 0;
		for (int i = 0; i < nrows; i++) {
			Node node = nlist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				CSSElement elm = (CSSElement) node;
				if ("tr".equals(elm.getTagName().toLowerCase(Locale.US))) {
					style = (ComputedCSSStyle) elm.getOwnerDocument().getStyleSheet().getComputedStyle(elm, null);
					BoxValues colbox = style.getBoxValues(unitType);
					float rowSpacing = colbox.getBorderLeftWidth() + colbox.getBorderRightWidth()
							+ colbox.getMarginLeft() + colbox.getMarginRight() + colbox.getPaddingLeft()
							+ colbox.getPaddingRight();
					if (maxRowSpacing < rowSpacing) {
						maxRowSpacing = rowSpacing;
					}
					NodeList columns = elm.getChildNodes();
					int nrowcols = columns.getLength();
					for (int j = 0, k = 0; j < nrowcols; j++) {
						Node colnode = columns.item(j);
						if (colnode.getNodeType() == Node.ELEMENT_NODE) {
							CSSElement col = (CSSElement) colnode;
							style = (ComputedCSSStyle) elm.getOwnerDocument().getStyleSheet().getComputedStyle(col,
									null);
							if ("table-cell".equals(style.getPropertyValue("display"))) {
								int colspan = 1;
								String colspanStr = col.getAttribute("colspan");
								if (colspanStr.length() != 0) {
									colspan = Integer.parseInt(colspanStr);
								}
								float[] contw = computeChildContentWidth(col, colspan, style, unitType);
								if (contw[0] > maxrcw[k]) {
									maxrcw[k] = contw[0];
								}
								if (contw[1] > minrcw[k]) {
									minrcw[k] = contw[1];
								}
								k += colspan;
							}
						}
					}
				}
			}
		}
		maxRowSpacing += box.getBorderLeftWidth() + box.getBorderRightWidth() + box.getMarginLeft()
				+ box.getMarginRight() + box.getPaddingLeft() + box.getPaddingRight();
		float mincw = maxRowSpacing;
		float maxcw = maxRowSpacing;
		for (int i = 0; i < ncol; i++) {
			mincw += minrcw[i];
			maxcw += maxrcw[i];
		}
		box.colwidth = new float[ncol];
		AbstractCSSValue cssval = style.getCSSValue("width");
		if (cssval == null || "auto".equals(cssval.getCssText())) {
			// width: auto
			/*
			 * If the 'table' or 'inline-table' element has 'width: auto', the used width is the
			 * greater of the table's containing block width, CAPMIN, and MIN. However, if either
			 * CAPMIN or the maximum width required by the columns plus cell spacing or borders (MAX)
			 * is less than that of the containing block, use max(MAX, CAPMIN).
			 */
			if (capmin < box.getWidth() && maxcw < box.getWidth()) {
				if (maxcw > capmin) {
					box.width = maxcw;
					for (int i = 0; i < ncol; i++) {
						box.colwidth[i] = maxrcw[i];
					}
				} else {
					box.width = capmin;
					float factor = capmin / maxcw;
					for (int i = 0; i < ncol; i++) {
						box.colwidth[i] = maxrcw[i] * factor;
					}
				}
			} else {
				boolean unset = true;
				if (mincw > box.width) {
					box.width = mincw;
					for (int i = 0; i < ncol; i++) {
						box.colwidth[i] = minrcw[i];
					}
					unset = false;
				}
				if (capmin > box.width) {
					float factor = capmin / box.width;
					box.width = capmin;
					for (int i = 0; i < ncol; i++) {
						box.colwidth[i] *= factor;
					}
				} else if (unset) {
					for (int i = 0; i < ncol; i++) {
						box.colwidth[i] = maxrcw[i];
					}
					/*
					 * This shrinking procedure does not account for the actual words contained in the table
					 * cells, perhaps a better shrinking process could be used.
					 */
					BoxModelHelper.shrinkTo(box, minrcw, mincw, maxcw, box.width);
					float width = 0;
					for (int i = 0; i < ncol; i++) {
						width += box.colwidth[i];
					}
					box.width = width;
				}
			}
		} else {
			// width: non-auto
			/*
			 * If the 'table' or 'inline-table' element's 'width' property has a computed value (W)
			 * other than 'auto', the used width is the greater of W, CAPMIN, and the minimum width
			 * required by all the columns plus cell spacing or borders (MIN). If the used width is
			 * greater than MIN, the extra width should be distributed over the columns.
			 */
			if (mincw > box.width) {
				box.width = mincw;
				for (int i = 0; i < ncol; i++) {
					box.colwidth[i] = minrcw[i];
				}
			} else {
				float factor = box.width / mincw;
				for (int i = 0; i < ncol; i++) {
					box.colwidth[i] = minrcw[i] * factor;
				}
			}
			if (capmin > box.width) {
				float factor = capmin / box.width;
				box.width = capmin;
				for (int i = 0; i < ncol; i++) {
					box.colwidth[i] *= factor;
				}
			}
		}
	}

	/*
	 * Document should be normalized for this to work
	 */
	private float computeCapmin(short unitType) {
		CSSCanvas canvas = ((CSSDocument) getComputedStyle().getOwnerNode().getOwnerDocument()).getCanvas();
		ComputedCSSStyle style = getComputedStyle();
		CSSElement tbl = (CSSElement) style.getOwnerNode();
		NodeList nlist = tbl.getElementsByTagName("caption");
		float capmin = 0f;
		for (int i = 0; i < nlist.getLength(); i++) {
			CSSElement elm = (CSSElement) nlist.item(i);
			NodeList chldlist = elm.getChildNodes();
			for (int j = 0; j < chldlist.getLength(); j++) {
				String text;
				Node chldnode = chldlist.item(j);
				if (chldnode.getNodeType() == Node.ELEMENT_NODE) {
					CSSElement col = (CSSElement) chldnode;
					style = (ComputedCSSStyle) col.getOwnerDocument().getStyleSheet().getComputedStyle(col, null);
					if (!"inline".equals(style.getDisplay())) {
						throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
								"Only inline elements are supported in caption");
					}
					text = chldnode.getTextContent();
				} else if (chldnode.getNodeType() == Node.TEXT_NODE
						|| chldnode.getNodeType() == Node.CDATA_SECTION_NODE) {
					text = chldnode.getTextContent();
				} else {
					text = null;
				}
				float minw = 0f;
				if (text != null) {
					text = BoxModelHelper.contractSpaces(text.trim());
					if (canvas != null) {
						minw = NumberValue.floatValueConversion(canvas.stringWidth(text, style),
								CSSPrimitiveValue.CSS_PT, unitType);
					} else {
						minw = BoxModelHelper.computeNodeMinimumWidth(text, style, unitType);
					}
				}
				if (minw > capmin) {
					capmin = minw;
				}
			}
		}
		return capmin;
	}

	private float[] computeChildContentWidth(CSSElement col, int colspan, ComputedCSSStyle style, short unitType) {
		float[] contw = { 0, 0 }; // contw[0] = max content width, contw[1] = min content width
		NodeList nlist = col.getChildNodes();
		int sz = nlist.getLength();
		for (int i = 0; i < sz; i++) {
			Node node = nlist.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				float[] nodew = computeTextWidth(node.getTextContent(), colspan, style, unitType);
				contw[0] += nodew[0];
				contw[1] += nodew[1];
			} else if (node.getNodeType() == Node.ELEMENT_NODE) {
				CSSElement elm = (CSSElement) node;
				style = (ComputedCSSStyle) elm.getOwnerDocument().getStyleSheet().getComputedStyle(elm, null);
				float width = computeWidth(style, unitType);
				contw[0] += width;
				contw[1] += width;
			}
		}
		return contw;
	}

	private float[] computeTextWidth(String text, int colspan, ComputedCSSStyle style, short unitType) {
		float[] contw = { 0, 0 }; // contw[0] = max content width, contw[1] = min content width
		// Get text content and remove contiguous space
		text = BoxModelHelper.contractSpaces(text.trim());
		CSSCanvas canvas = ((CSSDocument) getComputedStyle().getOwnerNode().getOwnerDocument()).getCanvas();
		if (canvas != null) {
			contw[0] = NumberValue.floatValueConversion(canvas.stringWidth(text, style), CSSPrimitiveValue.CSS_PT,
					unitType);
		} else {
			contw[0] = BoxModelHelper.computeTextWidth(text, style, unitType);
		}
		BoxValues colbox = style.getBoxValues(unitType);
		float spacing = colbox.getBorderLeftWidth() + colbox.getBorderRightWidth() + colbox.getMarginLeft()
				+ colbox.getMarginRight() + colbox.getPaddingLeft() + colbox.getPaddingRight();
		contw[0] += spacing;
		if (canvas != null) {
			contw[1] = BoxModelHelper.computeNodeMinimumWidth(text, style, canvas, unitType) / colspan + spacing;
		} else {
			contw[1] = BoxModelHelper.computeNodeMinimumWidth(text, style, unitType) / colspan + spacing;
		}
		return contw;
	}

	private void computeTableCellBox(MyBoxValues colbox, short unitType) {
		computeSharedBoxValues(colbox, unitType);
		computeInlineBox(colbox, unitType);
	}

	/*
	 * Obtain a width from the viewport or the style database, if available.
	 */
	private float deviceDocumentWidth(String failureReason, String value) throws StyleDatabaseRequiredException {
		CSSDocument doc = (CSSDocument) getComputedStyle().getOwnerNode().getOwnerDocument();
		if (doc != null) {
			CSSCanvas canvas = doc.getCanvas();
			if (canvas != null && canvas.getViewport() != null) {
				return canvas.getViewport().getViewportWidth();
			}
		}
		if (getStyleDatabase() == null) {
			StyleDatabaseRequiredException pve = new StyleDatabaseRequiredException(
					"No style database, " + failureReason);
			pve.setValueText(value);
			throw pve;
		}
		return getStyleDatabase().getDeviceWidth();
	}

	private float computeMarginNumberValue(ComputedCSSStyle styledecl, String propertyName, CSSPrimitiveValue cssval,
			short unitType) throws StyleDatabaseRequiredException {
		short declType = cssval.getPrimitiveType();
		float fv = cssval.getFloatValue(declType);
		if (fv == 0f) {
			return 0f;
		}
		if (declType == CSSPrimitiveValue.CSS_PERCENTAGE) {
			// Get the width of the containing block.
			styledecl = findContainingBlockStyle(styledecl);
			if (styledecl == null) {
				return deviceDocumentWidth("no enclosing block, and value is percentage.", cssval.getCssText())
						* cssval.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE) / 100f;
			}
			// We got the enclosing block: let's figure out the width
			return computeWidth(styledecl, unitType) * cssval.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE) / 100f;
		}
		float margin;
		if (unitType == declType) {
			margin = cssval.getFloatValue(unitType);
		} else if (cssval instanceof NumberValue) {
			if (unitType < 0) {
				if (getStyleDatabase() == null) {
					StyleDatabaseRequiredException sdex = new StyleDatabaseRequiredException(
							"Requested natural unit, but no style database was set.");
					sdex.setValueText(cssval.getCssText());
					throw sdex;
				}
				unitType = getStyleDatabase().getNaturalUnit();
			}
			margin = cssval.getFloatValue(declType);
			if (declType != unitType) {
				margin = NumberValue.floatValueConversion(margin, declType, unitType);
			}
		} else {
			CSSPropertyValueException e = new CSSPropertyValueException(
					"Expected number, found " + cssval.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue(propertyName, e);
			margin = 0;
		}
		return margin;
	}

	private ComputedCSSStyle findContainingBlockStyle(ComputedCSSStyle styledecl) {
		String position = styledecl.getPropertyValue("position");
		if ("fixed".equals(position)) {
			return null;
		} else if ("absolute".equals(position)) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "position: absolute is not supported");
		}
		// loop until display is block-level
		String display = styledecl.getPropertyValue("display");
		if ("table-cell".equals(display)) {
			do {
				styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
				if (styledecl == null) {
					break;
				} else {
					display = styledecl.getPropertyValue("display");
				}
			} while (!"table".equals(display));
		} else {
			do {
				styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
				if (styledecl == null) {
					break;
				} else {
					display = styledecl.getPropertyValue("display");
				}
			} while (!"block".equals(display) && !"list-item".equals(display) && !"table".equals(display)
					&& !display.startsWith("table-"));
		}
		return styledecl;
	}

	private float computeWidth(ComputedCSSStyle styledecl, short unitType) throws StyleDatabaseRequiredException {
		CSSValue cssval = styledecl.getCSSValue("width");
		short type = cssval.getCssValueType();
		if (cssval == null || "auto".equals(cssval.getCssText())
				|| (type != CSSValue.CSS_PRIMITIVE_VALUE && type != CSSValue.CSS_INHERIT)) {
			// width: auto
			CSSValue cssMarginLeft = styledecl.getCSSValue("margin-left");
			CSSValue cssBorderLeftWidth = styledecl.getCSSValue("border-left-width");
			CSSValue cssMarginRight = styledecl.getCSSValue("margin-right");
			CSSValue cssBorderRightWidth = styledecl.getCSSValue("border-right-width");
			float marginLeft = findWidthautoBoxProperty(styledecl, "margin-left", cssMarginLeft, unitType);
			float borderLeftWidth = findBorderWidthProperty(styledecl, "border-left-width", cssBorderLeftWidth,
					unitType);
			float paddingLeft = computePaddingSubproperty(styledecl, "padding-left", unitType);
			float paddingRight = computePaddingSubproperty(styledecl, "padding-right", unitType);
			float borderRightWidth = findBorderWidthProperty(styledecl, "border-right-width", cssBorderRightWidth,
					unitType);
			float marginRight = findWidthautoBoxProperty(styledecl, "margin-right", cssMarginRight, unitType);
			float contBlockWidth;
			ComputedCSSStyle contblockStyledecl = findContainingBlockStyle(styledecl);
			if (contblockStyledecl == null) {
				contBlockWidth = deviceDocumentWidth("width is auto, and cannot find top block width.", "auto");
			} else {
				String display = contblockStyledecl.getPropertyValue("display");
				if ("table".equals(display) || display.startsWith("table-")) {
					cssval = contblockStyledecl.getCSSValue("width");
					if (cssval == null || "auto".equals(cssval.getCssText())
							|| ((CSSPrimitiveValue) cssval).getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
						throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
								"Automatic tables not supported by this box model");
					}
					contBlockWidth = computeNonAutoWidth(contblockStyledecl, cssval, unitType);
				} else {
					contBlockWidth = computeWidth(contblockStyledecl, unitType);
				}
			}
			return contBlockWidth
					- (marginLeft + marginRight + borderLeftWidth + borderRightWidth + paddingLeft + paddingRight);
		}
		return computeNonAutoWidth(styledecl, cssval, unitType);
	}

	private float computeNonAutoWidth(ComputedCSSStyle styledecl, CSSValue cssval, short unitType)
			throws StyleDatabaseRequiredException {
		short type = cssval.getCssValueType();
		if (type == CSSValue.CSS_INHERIT) {
			// inherit: we probably won't find this here
			styledecl = findContainingBlockStyle(styledecl);
			if (styledecl == null) {
				return deviceDocumentWidth("and width value is inherit.", cssval.getCssText());
			} else {
				return computeWidth(styledecl, unitType);
			}
		}
		// width is not 'auto' nor 'inherit'.
		CSSPrimitiveValue cssWidth = (CSSPrimitiveValue) cssval;
		short declType = cssWidth.getPrimitiveType();
		float width = cssWidth.getFloatValue(type);
		if (declType == CSSPrimitiveValue.CSS_PERCENTAGE) {
			styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
			if (styledecl == null) {
				width = width
						* deviceDocumentWidth("width is a percentage, and enclosing block width could not be found.",
								cssval.getCssText())
						/ 100f;
				declType = getStyleDatabase().getNaturalUnit();
			} else {
				return computeWidth(styledecl, unitType) * cssWidth.getFloatValue(type) / 100f;
			}
		}
		// Unit conversions
		if (unitType < 0) {
			if (getStyleDatabase() == null) {
				StyleDatabaseRequiredException pve = new StyleDatabaseRequiredException(
						"No style database, and requested natural unit type for 'width'.");
				pve.setValueText(cssval.getCssText());
				throw pve;
			}
			unitType = getStyleDatabase().getNaturalUnit();
		}
		if (unitType != declType) {
			width = NumberValue.floatValueConversion(width, declType, unitType);
		}
		return width;
	}

	private float findWidthautoBoxProperty(ComputedCSSStyle styledecl, String propertyName, CSSValue cssval,
			short unitType) throws StyleDatabaseRequiredException, DOMException {
		if (cssval != null && !"auto".equals(cssval.getCssText())) {
			while (cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
				styledecl = findContainingBlockStyle(styledecl);
				if (styledecl != null) {
					cssval = styledecl.getCSSValue(propertyName);
				} else {
					cssval = null;
					break;
				}
			}
			CSSPrimitiveValue cssprim = (CSSPrimitiveValue) cssval;
			if (cssprim != null) {
				return findNumericBoxProperty(styledecl, propertyName, cssprim, unitType);
			}
		}
		return 0;
	}

	private float findBorderWidthProperty(ComputedCSSStyle styledecl, String propertyName, CSSValue cssval,
			short unitType) throws StyleDatabaseRequiredException, DOMException {
		if (cssval != null) {
			while (cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
				styledecl = findContainingBlockStyle(styledecl);
				if (styledecl != null) {
					cssval = styledecl.getCSSValue(propertyName);
				} else {
					cssval = null;
					break;
				}
			}
			CSSPrimitiveValue cssprim = (CSSPrimitiveValue) cssval;
			if (cssprim != null) {
				if (cssprim.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
					float fval;
					short declType;
					if (getStyleDatabase() == null) {
						if (getComplianceMode() != CSSDocument.ComplianceMode.QUIRKS) {
							StyleDatabaseRequiredException pve = new StyleDatabaseRequiredException(
									"No style database, and " + propertyName + " value is an identifier.");
							pve.setValueText(cssval.getCssText());
							throw pve;
						} else {
							float sz = getComputedStyle().getComputedFontSize();
							String sv = cssprim.getStringValue();
							if (sv.equals("thin")) {
								fval = (float) Math.floor(1d + sz * 0.05d);
							} else if (sv.equals("thick")) {
								fval = (float) Math.floor(4d + sz * 0.15d);
							} else {
								// Otherwise assume medium (default)
								fval = (float) Math.floor(2d + sz * 0.1d);
							}
							declType = CSSPrimitiveValue.CSS_PT;
						}
					} else {
						declType = getStyleDatabase().getNaturalUnit();
						fval = getStyleDatabase().getWidthSize(cssprim.getStringValue(),
								styledecl.getComputedFontSize());
					}
					if (declType == unitType) {
						return fval;
					} else {
						return NumberValue.floatValueConversion(fval, declType, unitType);
					}
				} else {
					return findNumericBoxProperty(styledecl, propertyName, cssprim, unitType);
				}
			}
		}
		return 0;
	}

	private CSSDocument.ComplianceMode getComplianceMode() {
		Node node = getComputedStyle().getOwnerNode();
		if (node != null) { // Probably unneeded check
			CSSDocument document = (CSSDocument) node.getOwnerDocument();
			if (document != null) {
				return document.getComplianceMode();
			}
		}
		return CSSDocument.ComplianceMode.STRICT;
	}

	private float findNumericBoxProperty(ComputedCSSStyle styledecl, String propertyName, CSSPrimitiveValue cssprim,
			short unitType) throws StyleDatabaseRequiredException {
		short declType = cssprim.getPrimitiveType();
		float fv = cssprim.getFloatValue(declType);
		if (fv == 0f) {
			return 0f;
		}
		if (declType == CSSPrimitiveValue.CSS_PERCENTAGE) {
			ComputedCSSStyle contblockStyledecl = findContainingBlockStyle(styledecl);
			return computeWidth(contblockStyledecl, unitType) * cssprim.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE)
					/ 100f;
		}
		if (unitType != declType) {
			if (unitType < 0) {
				if (getStyleDatabase() == null) {
					StyleDatabaseRequiredException pve = new StyleDatabaseRequiredException(
							"No style database, and requested natural unit type for " + propertyName + '.');
					pve.setValueText(cssprim.getCssText());
					throw pve;
				}
				unitType = getStyleDatabase().getNaturalUnit();
			}
			fv = NumberValue.floatValueConversion(fv, declType, unitType);
		}
		return fv;
	}

	private float computePaddingSubproperty(ComputedCSSStyle styledecl, String subpropertyName, short unitType)
			throws StyleDatabaseRequiredException {
		CSSValue cssval = styledecl.getCSSValue(subpropertyName);
		while (cssval != null && cssval.getCssValueType() == CSSValue.CSS_INHERIT) {
			styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
			if (styledecl != null) {
				cssval = styledecl.getCSSValue(subpropertyName);
			} else {
				getComputedStyle().getStyleDeclarationErrorHandler().noContainingBlock(getComputedStyle().getDisplay(),
						getComputedStyle().getOwnerNode());
				return 0;
			}
		}
		if (cssval == null) {
			return 0;
		}
		CSSPrimitiveValue csspri = (CSSPrimitiveValue) cssval;
		float padding;
		short declType = csspri.getPrimitiveType();
		float fv = csspri.getFloatValue(declType);
		if (fv == 0f) {
			return 0f;
		}
		if (declType == CSSPrimitiveValue.CSS_PERCENTAGE) {
			String display = styledecl.getPropertyValue("display");
			// check for table cell
			if ("table-cell".equals(display)) {
				// loop until display: table
				do {
					styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
					if (styledecl == null) {
						// Unable to find table ancestor for cell
						getComputedStyle().getStyleDeclarationErrorHandler().noContainingBlock("table-cell",
								getComputedStyle().getOwnerNode());
						padding = 0;
					} else {
						display = styledecl.getPropertyValue("display");
					}
				} while (!"table".equals(display));
			} else {
				// loop until display: block or list-item
				do {
					styledecl = (ComputedCSSStyle) styledecl.getParentComputedStyle();
					if (styledecl == null) {
						// Unable to find containing block.
						getComputedStyle().getStyleDeclarationErrorHandler().noContainingBlock(display,
								getComputedStyle().getOwnerNode());
						padding = 0;
					} else {
						display = styledecl.getPropertyValue("display");
					}
				} while (!"block".equals(display) && !"list-item".equals(display));
			}
			// Compute width for ancestor
			float parentWidth = computeWidth(styledecl, unitType);
			return parentWidth * csspri.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE) / 100f;
		} else if (csspri instanceof NumberValue) {
			padding = csspri.getFloatValue(declType);
		} else {
			CSSPropertyValueException e = new CSSPropertyValueException(
					"Expected number, found " + csspri.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue(subpropertyName, e);
			return 0;
		}
		if (padding != 0 && declType != unitType) {
			if (unitType < 0) {
				if (getStyleDatabase() == null) {
					StyleDatabaseRequiredException sdex = new StyleDatabaseRequiredException(
							"Requested natural unit, but no style database was set.");
					sdex.setValueText(csspri.getCssText());
					throw sdex;
				}
				unitType = getStyleDatabase().getNaturalUnit();
			}
			padding = NumberValue.floatValueConversion(padding, declType, unitType);
		}
		return padding;
	}

}
