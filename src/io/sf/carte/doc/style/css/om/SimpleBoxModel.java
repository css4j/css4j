/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Locale;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.Viewport;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.ExpressionValue;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;

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

	private enum PADDING {
		TOP,
		RIGHT,
		BOTTOM,
		LEFT
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

	abstract protected CSSComputedProperties getRevertStyle(Condition pseudoElt);

	private StyleDatabase getStyleDatabase() {
		return getComputedStyle().getStyleDatabase();
	}

	private void computeSharedBoxValues(MyBoxValues box, short unitType) {
		box.marginTop = computeMarginTop(unitType);
		box.marginBottom = computeMarginBottom(unitType);
		ComputedCSSStyle styledecl = getComputedStyle();
		// Padding (no 'auto' applies to padding)
		box.paddingTop = computePaddingSubproperty(styledecl, PADDING.TOP, unitType);
		box.paddingRight = computePaddingSubproperty(styledecl, PADDING.RIGHT, unitType);
		box.paddingBottom = computePaddingSubproperty(styledecl, PADDING.BOTTOM, unitType);
		box.paddingLeft = computePaddingSubproperty(styledecl, PADDING.LEFT, unitType);
		box.borderTopWidth = findBorderWidthProperty(styledecl, "border-top-width", unitType, "border-top-style");
		box.borderBottomWidth = findBorderWidthProperty(styledecl, "border-bottom-width", unitType,
				"border-bottom-style");
		// Border left & right width
		// If the element is block-level, these values can be later modified when
		// computing width
		box.borderLeftWidth = findBorderWidthProperty(styledecl, "border-left-width", unitType, "border-left-style");
		box.borderRightWidth = findBorderWidthProperty(styledecl, "border-right-width", unitType, "border-right-style");
	}

	private float computeMarginTop(short unitType) {
		ComputedCSSStyle styledecl = getComputedStyle();
		CSSValue cssval = styledecl.getCascadedValue("margin-top");
		if (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
			styledecl = styledecl.getParentComputedStyle();
			if (styledecl != null) {
				return styledecl.getBoxValues(unitType).getMarginTop();
			}
		}
		if (styledecl != null && cssval.getCssValueType() == CssType.TYPED) {
			CSSTypedValue typed = (CSSTypedValue) cssval;
			if (!isTypedAutoOrInvalidLength(typed)) {
				return computeMarginNumberValue(styledecl, "margin-top", typed, unitType);
			}
		} else {
			CSSPropertyValueException e = new CSSPropertyValueException(
					"Expected primitive value for margin-top, found " + cssval.getCssText());
			if (styledecl == null) {
				styledecl = getComputedStyle();
			}
			styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-top", e);
		}
		return 0f;
	}

	private float computeMarginBottom(short unitType) {
		ComputedCSSStyle styledecl = getComputedStyle();
		StyleValue cssval = styledecl.getCascadedValue("margin-bottom");
		if (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
			styledecl = styledecl.getParentComputedStyle();
			if (styledecl != null) {
				return styledecl.getBoxValues(unitType).getMarginBottom();
			}
		}
		if (styledecl != null && cssval.getCssValueType() == CssType.TYPED) {
			CSSTypedValue typed = (CSSTypedValue) cssval;
			if (!isTypedAutoOrInvalidLength(typed)) {
				return computeMarginNumberValue(styledecl, "margin-bottom", typed, unitType);
			}
		} else {
			CSSPropertyValueException e = new CSSPropertyValueException(
					"Expected primitive value for margin-bottom, found " + cssval.getCssText());
			if (styledecl == null) {
				styledecl = getComputedStyle();
			}
			styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-bottom", e);
		}
		return 0f;
	}

	/**
	 * Determine whether the given value is either the <code>auto</code> identifier
	 * or an invalid length (same effect).
	 * 
	 * @param value the value to test.
	 * @return <code>true</code> if the value is <code>auto</code> or invalid
	 *         length.
	 */
	private static boolean isTypedAutoOrInvalidLength(CSSTypedValue value) {
		return (value.getPrimitiveType() == Type.IDENT
				&& "auto".equalsIgnoreCase(value.getStringValue()))
				|| (value.isNegativeNumber() && !value.isCalculatedNumber());
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
		CSSValue cssval = styledecl.getCascadedValue("margin-right");
		while (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
			styledecl = styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCascadedValue("margin-right");
			}
		}
		if (styledecl == null) {
			box.marginRight = 0f;
		} else {
			if (cssval.getCssValueType() == CssType.TYPED) {
				CSSTypedValue typed = (CSSTypedValue) cssval;
				if (!isTypedAutoOrInvalidLength(typed)) {
					box.marginRight = computeMarginNumberValue(styledecl, "margin-right", typed, unitType);
				}
			} else {
				CSSPropertyValueException e = new CSSPropertyValueException(
						"Expected primitive value for margin-right, found " + cssval.getCssText());
				styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-right", e);
				box.marginRight = 0f;
			}
		}
		styledecl = getComputedStyle();
		cssval = styledecl.getCascadedValue("margin-left");
		while (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
			styledecl = styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCascadedValue("margin-left");
			}
		}
		if (styledecl == null) {
			box.marginLeft = 0f;
		} else {
			if (cssval.getCssValueType() == CssType.TYPED) {
				CSSTypedValue typed = (CSSTypedValue) cssval;
				if (!isTypedAutoOrInvalidLength(typed)) {
					box.marginLeft = computeMarginNumberValue(styledecl, "margin-left", typed, unitType);
				}
			} else {
				CSSPropertyValueException e = new CSSPropertyValueException(
						"Expected primitive value for margin-left, found " + cssval.getCssText());
				styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-left", e);
				box.marginLeft = 0f;
			}
		}
		Node node = getComputedStyle().getOwnerNode();
		if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
			CSSElement elm = (CSSElement) node;
			if ("img".equals(elm.getTagName()) && elm.hasAttribute("width")) {
				String strWidth = elm.getAttribute("width");
				// Replaced element
				try {
					box.width = Float.parseFloat(strWidth);
				} catch (NumberFormatException e) {
					String message = "Could not parse value of 'width' attribute for img element";
					CSSPropertyValueException ex = new CSSPropertyValueException(message, e);
					ex.setValueText(strWidth);
					elm.getOwnerDocument().getErrorHandler().computedStyleError(elm, "width", ex);
				}
				if (unitType != CSSUnit.CSS_PX) {
					box.width = NumberValue.floatValueConversion(box.width, CSSUnit.CSS_PX, unitType);
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
		if ("block".equalsIgnoreCase(display) || "list-item".equalsIgnoreCase(display)) {
			box = new MyBoxValues();
			computeSharedBoxValues(box, unitType);
			computeBlockBox(box, unitType);
			return box;
		} else if ("table".equalsIgnoreCase(display)) {
			box = new MyTableBoxValues();
			computeSharedBoxValues(box, unitType);
			String tableLayout = getComputedStyle().getPropertyValue("table-layout");
			if ("auto".equalsIgnoreCase(tableLayout)) {
				computeTableBox((MyTableBoxValues) box, unitType);
				return box;
			} else {
				computeBlockBox(box, unitType);
				return box;
			}
		} else if ("table-cell".equalsIgnoreCase(display) || "table-row".equalsIgnoreCase(display)) {
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
		CSSValue cssval = styledecl.getCascadedValue("margin-right");
		while (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
			styledecl = styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCascadedValue("margin-right");
			}
		}
		if (cssval.getCssValueType() == CssType.TYPED) {
			CSSTypedValue typed = (CSSTypedValue) cssval;
			margin_right_auto = isTypedAutoOrInvalidLength(typed);
			if (!margin_right_auto) {
				box.marginRight = computeMarginNumberValue(styledecl, "margin-right", typed, unitType);
			}
		} else {
			CSSPropertyValueException e = new CSSPropertyValueException(
					"Expected primitive value for margin-right, found " + cssval.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue("margin-right", e);
			box.marginRight = 0f;
		}
		styledecl = getComputedStyle();
		boolean margin_left_auto = false;
		cssval = styledecl.getCascadedValue("margin-left");
		while (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
			styledecl = styledecl.getParentComputedStyle();
			if (styledecl == null) {
				break;
			} else {
				cssval = styledecl.getCascadedValue("margin-left");
			}
		}
		if (cssval.getCssValueType() == CssType.TYPED) {
			CSSTypedValue typed = (CSSTypedValue) cssval;
			margin_left_auto = isTypedAutoOrInvalidLength(typed);
			if (!margin_left_auto) {
				box.marginLeft = computeMarginNumberValue(styledecl, "margin-left", typed, unitType);
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
		cssval = styledecl.getCascadedValue("width");
		CSSTypedValue typed;
		if (cssval == null || cssval.getCssValueType() != CssType.TYPED
				|| isTypedAutoOrInvalidLength(typed = (CSSTypedValue) cssval)) {
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
			box.width = computeNonAutoWidth(styledecl, typed, unitType);
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
					if ("ltr".equalsIgnoreCase(styledecl.getPropertyValue("direction"))) {
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
		// compute minimum width required by all the columns plus cell spacing or
		// borders
		ComputedCSSStyle style = getComputedStyle();
		CSSElement tbl = style.getOwnerNode();
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
				if ("tr".equalsIgnoreCase(elm.getTagName())) {
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
				if ("tr".equalsIgnoreCase(elm.getTagName())) {
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
							if ("table-cell".equalsIgnoreCase(style.getPropertyValue("display"))) {
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
		StyleValue cssval = style.getCascadedValue("width");
		if (cssval == null || cssval.getCssValueType() != CssType.TYPED
				|| isTypedAutoOrInvalidLength((CSSTypedValue) cssval)) {
			// width: auto
			/*
			 * If the 'table' or 'inline-table' element has 'width: auto', the used width is
			 * the greater of the table's containing block width, CAPMIN, and MIN. However,
			 * if either CAPMIN or the maximum width required by the columns plus cell
			 * spacing or borders (MAX) is less than that of the containing block, use
			 * max(MAX, CAPMIN).
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
					 * This shrinking procedure does not account for the actual words contained in
					 * the table cells, perhaps a better shrinking process could be used.
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
			 * If the 'table' or 'inline-table' element's 'width' property has a computed
			 * value (W) other than 'auto', the used width is the greater of W, CAPMIN, and
			 * the minimum width required by all the columns plus cell spacing or borders
			 * (MIN). If the used width is greater than MIN, the extra width should be
			 * distributed over the columns.
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
		CSSCanvas canvas = getComputedStyle().getOwnerNode().getOwnerDocument().getCanvas();
		ComputedCSSStyle style = getComputedStyle();
		CSSElement tbl = style.getOwnerNode();
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
					if (!"inline".equalsIgnoreCase(style.getDisplay())) {
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
								CSSUnit.CSS_PT, unitType);
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
		CSSCanvas canvas = getComputedStyle().getOwnerNode().getOwnerDocument().getCanvas();
		if (canvas != null) {
			contw[0] = NumberValue.floatValueConversion(canvas.stringWidth(text, style), CSSUnit.CSS_PT,
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
		StyleDatabase sdb = getStyleDatabase();
		CSSDocument doc = getComputedStyle().getOwnerNode().getOwnerDocument();
		if (doc != null) {
			CSSCanvas canvas = doc.getCanvas();
			Viewport viewport;
			if (canvas != null && (viewport = canvas.getViewport()) != null) {
				float fv = viewport.getViewportWidth();
				return NumberValue.floatValueConversion(fv, sdb.getNaturalUnit(),
						CSSUnit.CSS_PT);
			}
		}
		if (sdb == null) {
			String medium;
			if (doc != null && (medium = doc.getTargetMedium()) != null) {
				if ("print".equals(medium)) {
					return ComputedCSSStyle.PRINT_WIDTH;
				} else if ("screen".equals(medium)) {
					return ComputedCSSStyle.SCREEN_WIDTH;
				} else if ("handheld".equals(medium)) {
					return ComputedCSSStyle.HANDHELD_WIDTH;
				}
			}
			StyleDatabaseRequiredException pve = new StyleDatabaseRequiredException(
					"No style database, " + failureReason);
			pve.setValueText(value);
			throw pve;
		}
		return NumberValue.floatValueConversion(sdb.getDeviceWidth(), sdb.getNaturalUnit(), CSSUnit.CSS_PT);
	}

	private float computeMarginNumberValue(ComputedCSSStyle styledecl, String propertyName, CSSTypedValue cssval,
			short unitType) throws StyleDatabaseRequiredException {
		float fv;
		try {
			fv = floatValue(styledecl, propertyName, cssval, unitType, true);
			if (fv < 0f) {
				fv = 0f;
			}
		} catch (DOMException e) {
			CSSPropertyValueException ex = new CSSPropertyValueException(
					"Expected number, found " + cssval.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue(propertyName, ex);
			fv = 0;
		}
		return fv;
	}

	private float floatValue(ComputedCSSStyle styledecl, String propertyName, CSSTypedValue cssval,
			short unitType, boolean useDeviceDocumentWidth) throws DOMException {
		if (unitType < 0) {
			if (getStyleDatabase() == null) {
				StyleDatabaseRequiredException sdex = new StyleDatabaseRequiredException(
						"Requested natural unit, but no style database was set.");
				sdex.setValueText(cssval.getCssText());
				throw sdex;
			}
			unitType = getStyleDatabase().getNaturalUnit();
		}
		Type declType = cssval.getPrimitiveType();
		float fv;
		if (declType == Type.EXPRESSION) {
			fv = calcValue(styledecl, propertyName, (ExpressionValue) cssval, unitType, useDeviceDocumentWidth);
		} else if (declType == Type.FUNCTION) {
			fv = functionValue(styledecl, propertyName, (CSSFunctionValue) cssval, unitType, useDeviceDocumentWidth);
		} else if (declType == Type.NUMERIC) {
			if (cssval.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
				fv = percentageValue(styledecl, cssval, unitType, useDeviceDocumentWidth);
			} else {
				fv = cssval.getFloatValue(unitType);
			}
		} else {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Unable to evaluate value of type: " + declType);
		}
		return fv;
	}

	private float percentageValue(ComputedCSSStyle styledecl, CSSTypedValue cssval, short unitType,
			boolean useDeviceDocumentWidth) {
		// Get the width of the containing block.
		styledecl = findContainingBlockStyle(styledecl);
		if (styledecl == null) {
			if (useDeviceDocumentWidth) {
				return deviceDocumentWidth("no enclosing block, and value is percentage.", cssval.getCssText())
						* cssval.getFloatValue(CSSUnit.CSS_PERCENTAGE) / 100f;
			} else {
				getComputedStyle().getStyleDeclarationErrorHandler().noContainingBlock(getComputedStyle().getDisplay(),
						getComputedStyle().getOwnerNode());
				return 0f;
			}
		}
		// We got the enclosing block: let's figure out the width
		return computeWidth(styledecl, unitType) * cssval.getFloatValue(CSSUnit.CSS_PERCENTAGE) / 100f;
	}

	private static ComputedCSSStyle findContainingBlockStyle(ComputedCSSStyle styledecl) {
		String position = styledecl.getPropertyValue("position");
		if ("fixed".equalsIgnoreCase(position)) {
			return null;
		} else if ("absolute".equalsIgnoreCase(position)) {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "position: absolute is not supported");
		}
		// loop until display is block-level
		String display = styledecl.getPropertyValue("display");
		if ("table-cell".equalsIgnoreCase(display)) {
			do {
				styledecl = styledecl.getParentComputedStyle();
				if (styledecl == null) {
					break;
				} else {
					display = styledecl.getPropertyValue("display");
				}
			} while (!"table".equalsIgnoreCase(display));
		} else {
			do {
				styledecl = styledecl.getParentComputedStyle();
				if (styledecl == null) {
					break;
				} else {
					display = styledecl.getPropertyValue("display");
				}
			} while (!"block".equals(display = display.toLowerCase(Locale.ROOT)) && !"list-item".equals(display)
					&& !"table".equals(display) && !display.startsWith("table-"));
		}
		return styledecl;
	}

	private float calcValue(ComputedCSSStyle styledecl, String propertyName, ExpressionValue cssCalc, short unitType,
			boolean useDeviceDocumentWidth) {
		BoxEvaluator ev = new BoxEvaluator(styledecl, propertyName, useDeviceDocumentWidth);
		CSSTypedValue result = ev.evaluateExpression(cssCalc);
		if (result.isNegativeNumber()) {
			// A negative calculated length is taken as zero.
			return 0f;
		}
		return result.getFloatValue(unitType);
	}

	private float functionValue(ComputedCSSStyle styledecl, String propertyName, CSSFunctionValue function,
			short unitType, boolean useDeviceDocumentWidth) {
		BoxEvaluator ev = new BoxEvaluator(styledecl, propertyName, useDeviceDocumentWidth);
		return ev.evaluateFunction(function).getFloatValue(unitType);
	}

	private float computeWidth(ComputedCSSStyle styledecl, short unitType) throws StyleDatabaseRequiredException {
		CSSValue cssval = styledecl.getCascadedValue("width");
		CSSTypedValue typed;
		if (cssval == null || cssval.getCssValueType() != CssType.TYPED
				|| isTypedAutoOrInvalidLength(typed = (CSSTypedValue) cssval)) {
			// width: auto
			CSSValue cssMarginLeft = styledecl.getCascadedValue("margin-left");
			CSSValue cssMarginRight = styledecl.getCascadedValue("margin-right");
			float marginLeft = findWidthautoBoxProperty(styledecl, "margin-left", cssMarginLeft, unitType);
			float borderLeftWidth = findBorderWidthProperty(styledecl, "border-left-width", unitType,
					"border-left-style");
			float paddingLeft = computePaddingSubproperty(styledecl, PADDING.LEFT, unitType);
			float paddingRight = computePaddingSubproperty(styledecl, PADDING.RIGHT, unitType);
			float borderRightWidth = findBorderWidthProperty(styledecl, "border-right-width", unitType,
					"border-right-style");
			float marginRight = findWidthautoBoxProperty(styledecl, "margin-right", cssMarginRight, unitType);
			float contBlockWidth;
			ComputedCSSStyle contblockStyledecl = findContainingBlockStyle(styledecl);
			if (contblockStyledecl == null) {
				contBlockWidth = deviceDocumentWidth("width is auto, and cannot find top block width.", "auto");
			} else {
				String display = contblockStyledecl.getPropertyValue("display").toLowerCase(Locale.ROOT);
				if ("table".equals(display) || display.startsWith("table-")) {
					cssval = contblockStyledecl.getCascadedValue("width");
					if (cssval == null || cssval.getCssValueType() != CssType.TYPED
							|| isTypedAutoOrInvalidLength(typed = (CSSTypedValue) cssval)
							|| typed.getUnitType() == CSSUnit.CSS_PERCENTAGE) {
						throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
								"Automatic tables not supported by this box model");
					}
					contBlockWidth = computeNonAutoWidth(contblockStyledecl, typed, unitType);
				} else {
					contBlockWidth = computeWidth(contblockStyledecl, unitType);
				}
			}
			return contBlockWidth
					- (marginLeft + marginRight + borderLeftWidth + borderRightWidth + paddingLeft + paddingRight);
		}
		return computeNonAutoWidth(styledecl, typed, unitType);
	}

	private float computeNonAutoWidth(ComputedCSSStyle styledecl, CSSTypedValue cssWidth, short unitType)
			throws DOMException, StyleDatabaseRequiredException {
		// width is not 'auto' nor 'inherit'.
		float fv;
		try {
			fv = floatValue(styledecl, "width", cssWidth, unitType, true);
		} catch (DOMException e) {
			CSSPropertyValueException ex = new CSSPropertyValueException(
					"Expected number, found " + cssWidth.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue("width", ex);
			fv = 0;
		}
		return fv;
	}

	private float findWidthautoBoxProperty(ComputedCSSStyle styledecl, String propertyName, CSSValue cssval,
			short unitType) throws StyleDatabaseRequiredException, DOMException {
		if (cssval != null && !isAutoOrInvalidLength(cssval)) {
			while (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
				styledecl = findContainingBlockStyle(styledecl);
				if (styledecl != null) {
					cssval = styledecl.getCascadedValue(propertyName);
				} else {
					cssval = null;
					break;
				}
			}
			if (cssval != null) {
				if (cssval.getCssValueType() != CssType.TYPED) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected width value: " + cssval.getCssText());
				}
				CSSTypedValue cssprim = (CSSTypedValue) cssval;
				return findNumericBoxProperty(styledecl, propertyName, cssprim, unitType);
			}
		}
		return 0;
	}

	private static boolean isAutoOrInvalidLength(CSSValue cssval) {
		return cssval.getCssValueType() == CssType.TYPED && isTypedAutoOrInvalidLength((CSSTypedValue) cssval);
	}

	private float findBorderWidthProperty(ComputedCSSStyle styledecl, String propertyName, short unitType,
			String stylePropertyName)
			throws StyleDatabaseRequiredException, DOMException {
		CSSValue borderStyle = styledecl.getCascadedValue(stylePropertyName);
		if (borderStyle == null || borderStyle.getPrimitiveType() != CSSValue.Type.IDENT
				|| "none".equalsIgnoreCase(((CSSTypedValue) borderStyle).getStringValue())) {
			return 0f;
		}
		CSSValue cssval = styledecl.getCascadedValue(propertyName);
		if (cssval != null) {
			while (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
				styledecl = findContainingBlockStyle(styledecl);
				if (styledecl != null) {
					cssval = styledecl.getCascadedValue(propertyName);
				} else {
					cssval = null;
					break;
				}
			}
			if (cssval != null) {
				if (cssval.getCssValueType() != CssType.TYPED) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected border-width value: " + cssval.getCssText());
				}
				float fval;
				CSSTypedValue cssprim = (CSSTypedValue) cssval;
				if (cssprim.getPrimitiveType() == Type.IDENT) {
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
							if (sv.equalsIgnoreCase("thin")) {
								fval = (float) Math.floor(1d + sz * 0.05d);
							} else if (sv.equalsIgnoreCase("thick")) {
								fval = (float) Math.floor(4d + sz * 0.15d);
							} else {
								// Otherwise assume medium (default)
								fval = (float) Math.floor(2d + sz * 0.1d);
							}
							declType = CSSUnit.CSS_PT;
						}
					} else {
						declType = getStyleDatabase().getNaturalUnit();
						fval = getStyleDatabase().getWidthSize(cssprim.getStringValue(),
								styledecl.getComputedFontSize());
					}
					if (unitType != declType) {
						fval = NumberValue.floatValueConversion(fval, declType, unitType);
					}
				} else {
					fval = findNumericBoxProperty(styledecl, propertyName, cssprim, unitType);
				}
				return fval;
			}
		}
		return 0f;
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

	private float findNumericBoxProperty(ComputedCSSStyle styledecl, String propertyName, CSSTypedValue cssprim,
			short unitType) throws StyleDatabaseRequiredException {
		float fv;
		try {
			fv = floatValue(styledecl, propertyName, cssprim, unitType, true);
		} catch (DOMException e) {
			CSSPropertyValueException ex = new CSSPropertyValueException(
					"Expected number, found " + cssprim.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue(propertyName, ex);
			fv = 0;
		}
		return fv;
	}

	private float computePaddingSubproperty(ComputedCSSStyle styledecl, PADDING paddingProperty, short unitType)
			throws StyleDatabaseRequiredException {
		String propertyName;
		CSSValue cssval;
		switch (paddingProperty) {
		case TOP:
			propertyName = "padding-top";
			break;
		case RIGHT:
			propertyName = "padding-right";
			break;
		case BOTTOM:
			propertyName = "padding-bottom";
			break;
		default:
			propertyName = "padding-left";
		}
		cssval = styledecl.getCascadedValue(propertyName);
		if (cssval.getPrimitiveType() == CSSValue.Type.INHERIT) {
			styledecl = styledecl.getParentComputedStyle();
			if (styledecl != null) {
				BoxValues ctbox = styledecl.getBoxValues(unitType);
				float fv;
				switch (paddingProperty) {
				case TOP:
					fv = ctbox.getPaddingTop();
					break;
				case RIGHT:
					fv = ctbox.getPaddingRight();
					break;
				case BOTTOM:
					fv = ctbox.getPaddingBottom();
					break;
				default:
					fv = ctbox.getPaddingLeft();
				}
				return fv;
			} else {
				getComputedStyle().getStyleDeclarationErrorHandler().noContainingBlock(getComputedStyle().getDisplay(),
						getComputedStyle().getOwnerNode());
				return 0f;
			}
		}
		if (cssval.getCssValueType() != CssType.TYPED) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected padding value: " + cssval.getCssText());
		}
		CSSTypedValue csspri = (CSSTypedValue) cssval;
		float fv;
		try {
			fv = floatValue(styledecl, propertyName, csspri, unitType, false);
			if (fv < 0f) {
				fv = 0f;
			}
		} catch (DOMException e) {
			CSSPropertyValueException ex = new CSSPropertyValueException(
					"Expected number, found " + csspri.getCssText());
			styledecl.getStyleDeclarationErrorHandler().wrongValue(propertyName, ex);
			fv = 0;
		}
		return fv;
	}

	private class BoxEvaluator extends Evaluator {

		private final ComputedCSSStyle styledecl;
		private final String propertyName;
		private final boolean useDeviceDocumentWidth;

		private BoxEvaluator(ComputedCSSStyle styledecl, String propertyName, boolean useDeviceDocumentWidth) {
			super();
			this.styledecl = styledecl;
			this.propertyName = propertyName;
			this.useDeviceDocumentWidth = useDeviceDocumentWidth;
		}

		@Override
		protected TypedValue absoluteTypedValue(TypedValue partialValue) {
			return styledecl.absoluteTypedValue(propertyName, partialValue, false);
		}

		@Override
		protected StyleValue absoluteProxyValue(CSSPrimitiveValue partialValue) {
			return styledecl.absoluteProxyValue(propertyName, partialValue, false);
		}

		@Override
		protected float percentage(CSSTypedValue value, short unitType) throws DOMException {
			return percentageValue(styledecl, value, unitType, useDeviceDocumentWidth);
		}

	}

}
