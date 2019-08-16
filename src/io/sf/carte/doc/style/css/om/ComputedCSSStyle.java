/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.Viewport;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.property.AbstractCSSExpression;
import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.CustomPropertyValue;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.ExpressionValue;
import io.sf.carte.doc.style.css.property.FunctionValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.LinkedCSSValueList;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.OperandExpression;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
import io.sf.carte.doc.style.css.property.URIValue;
import io.sf.carte.doc.style.css.property.URIValueWrapper;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.doc.style.css.property.WrappedValue;
import io.sf.carte.util.SimpleWriter;

/**
 * Style declaration that computes CSS properties.
 * <p>
 * See section 6.1 of the Document Object Model CSS spec.
 * </p>
 * <p>
 * Some of the methods require that a style database is set, for example to
 * verify the availability of font families.
 * 
 */
abstract public class ComputedCSSStyle extends BaseCSSStyleDeclaration implements CSSComputedProperties {

	private Node node = null;

	protected ComputedCSSStyle() {
		super();
	}

	protected ComputedCSSStyle(ComputedCSSStyle copiedObject) {
		super(copiedObject);
		setOwnerNode(copiedObject.getOwnerNode());
	}

	protected void setOwnerNode(Node node) {
		this.node = node;
	}

	@Override
	public Node getOwnerNode() {
		return node;
	}

	@Override
	public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
			return ((CSSDocument) node.getOwnerDocument()).getErrorHandler()
					.getInlineStyleErrorHandler((CSSElement) node);
		}
		return null;
	}

	@Override
	protected void setPropertyCSSValue(String propertyName, AbstractCSSValue value, String hrefcontext) {
		if ("background-image".equals(propertyName) || "border-image-source".equals(propertyName)) {
			// Add parent stylesheet info
			if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				if (hrefcontext != null) {
					value = ((ValueList) value).wrap(hrefcontext, getOwnerNode().getOwnerDocument().getBaseURI());
				}
			} else if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
					&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
				if (hrefcontext != null) {
					value = new URIValueWrapper((URIValue) value, hrefcontext,
							getOwnerNode().getOwnerDocument().getBaseURI());
				}
			}
		}
		super.setPropertyCSSValue(propertyName, value, hrefcontext);
	}

	/**
	 * Gets the absolute, primitive "computed" value for the given property.
	 * <p>
	 * The rendering context is not taken into account for this method.
	 * <p>
	 * See paragraph 6.1.2 of the Document Object Model CSS specification for the definition
	 * of "computed" values.
	 * 
	 * @param property
	 *            the property that we want to evaluate.
	 * @return the primitive value of the property, a CSSShorthandValue if the property is a
	 *         shorthand, or null if the property is not known.
	 * @throws StyleDatabaseRequiredException
	 *             when a computation that requires a style database is attempted, but no
	 *             style database has been set.
	 */
	@Override
	public AbstractCSSValue getCSSValue(String property) throws StyleDatabaseRequiredException {
		AbstractCSSValue value = super.getCSSValue(property);
		// Is the property inherited ?
		PropertyDatabase propertydb = PropertyDatabase.getInstance();
		boolean inherited = propertydb.isInherited(property);
		// Check for unset
		if (value != null && isCSSIdentifier(value, "unset")) {
			/*
			 * The 'unset' keyword acts as either inherit or initial, depending on whether the
			 * property is inherited or not.
			 */
			if (inherited) {
				value = InheritValue.getValue();
			} else {
				value = defaultPropertyValue(property, propertydb);
			}
		}
		ComputedCSSStyle ancStyle = this;
		/*
		 * We compute inherited value, if appropriate.
		 */
		while (value == null ? inherited : value.getCssValueType() == CSSValue.CSS_INHERIT) {
			ancStyle = ancStyle.getParentComputedStyle();
			if (ancStyle == null) {
				break;
			}
			if (ancStyle.isPropertySet(property)) {
				value = ancStyle.getCSSValue(property);
			}
		}
		// Still inheriting ?
		if (value != null && value.getCssValueType() == CSSValue.CSS_INHERIT) {
			value = null;
		}
		// Check for null, and apply initial values if appropriate
		if (value == null || (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& value.getCssText().equalsIgnoreCase("initial"))) {
			value = defaultPropertyValue(property, propertydb);
		}
		// If value is null now, we have no idea about this property's value
		if (value != null) {
			if (value.isSystemDefault() && value instanceof SystemDefaultValue) {
				return value;
			}
			if ("display".equals(property)) {
				// Computed values of some properties are constrained by others
				value = applyDisplayConstrains(value);
			} else if (property.endsWith("-width")) {
				// Computed values of border-width properties are constrained by
				// border-style
				value = applyBorderWidthConstrains(property, value);
			} else if ("background-repeat".equals(property)) {
				value = computeBackgroundRepeat(value);
			} else if (property.endsWith("color") && value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				// If color is an identifier, try to give a computed color value
				return colorValue(property, value);
			}
			if (property.equals("font-size")) {
				value = absoluteFontSizeValue(value);
			} else {
				// Convert to absolute units
				value = absoluteValue(value, false);
			}
		}
		return value;
	}

	private boolean isCSSIdentifier(CSSValue value, String ident) {
		return value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& ident.equalsIgnoreCase(value.getCssText());
	}

	private AbstractCSSValue colorValue(String propertyName, AbstractCSSValue value) {
		CSSPrimitiveValue primi = (CSSPrimitiveValue) value;
		if (primi.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
			String s = primi.getStringValue().toLowerCase(Locale.US);
			if ("currentcolor".equals(s)) {
				if(!"color".equals(propertyName)) {
					value = getCSSColor();
				}
			} else {
				String spec;
				if ("transparent".equals(s)) {
					spec = "rgba(0 0 0/0)";
				} else {
					spec = ColorIdentifiers.getInstance().getColor(s);
				}
				if (spec != null) {
					try {
						value = getValueFactory().parseProperty(spec);
					} catch (DOMException e) {
					}
				}
			}
		}
		return value;
	}

	private AbstractCSSValue absoluteValue(AbstractCSSValue value, boolean useParentStyle) {
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			int lstlen = list.getLength();
			for (int i = 0; i < lstlen; i++) {
				list.set(i, absoluteValue(list.item(i), useParentStyle));
			}
		} else if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			AbstractCSSPrimitiveValue primi = (AbstractCSSPrimitiveValue) value;
			// Check for custom properties ('variables')
			if (primi.getPrimitiveType() == CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY) {
				AbstractCSSValue custom = getCSSValue(primi.getStringValue());
				if (custom == null) {
					custom = ((CustomPropertyValue) value).getFallback();
					if (custom != null) {
						value = absoluteValue(custom, useParentStyle);
					}
				} else {
					value = absoluteValue(custom, useParentStyle);
				}
			} else {
				value = absolutePrimitiveValue(primi, useParentStyle);
			}
		}
		return value;
	}

	AbstractCSSPrimitiveValue absolutePrimitiveValue(AbstractCSSPrimitiveValue pri, boolean useParentStyle) {
		if (isRelativeUnit(pri)) {
			try {
				pri = absoluteNumberValue((NumberValue) pri, useParentStyle);
			} catch (DOMException e) {
			}
		} else {
			short type = pri.getPrimitiveType();
			if (type == CSSPrimitiveValue2.CSS_EXPRESSION) {
				pri = pri.clone();
				AbstractCSSExpression expr = ((ExpressionValue) pri).getExpression();
				Evaluator ev = new MyEvaluator();
				try {
					pri = (AbstractCSSPrimitiveValue) ev.evaluateExpression(expr);
				} catch (DOMException e) {
					// Evaluation failed, convert expressions to absolute anyway.
					absoluteExpressionValue(expr, useParentStyle);
				}
			} else if (type == CSSPrimitiveValue2.CSS_FUNCTION) {
				FunctionValue function = (FunctionValue) pri;
				function = function.clone();
				Evaluator ev = new MyEvaluator();
				try {
					pri = (AbstractCSSPrimitiveValue) ev.evaluateFunction(function);
				} catch (DOMException e) {
					// Evaluation failed, convert arguments to absolute anyway.
					LinkedCSSValueList args = function.getArguments();
					int sz = args.size();
					for (int i = 0; i < sz; i++) {
						args.set(i, absoluteValue(args.get(i), useParentStyle));
					}
				}
				// Check for custom properties ('variables')
			} else if (type == CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY) {
				AbstractCSSValue custom = getCSSValue(pri.getStringValue());
				if (custom == null) {
					custom = ((CustomPropertyValue) pri).getFallback();
					if (custom != null && custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
						pri = absolutePrimitiveValue((AbstractCSSPrimitiveValue) custom, useParentStyle);
					}
				} else if (custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					pri = absolutePrimitiveValue((AbstractCSSPrimitiveValue) custom, useParentStyle);
				}
			}
		}
		return pri;
	}

	private boolean isRelativeUnit(CSSPrimitiveValue pri) {
		switch (pri.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_EMS:
		case CSSPrimitiveValue.CSS_EXS:
		case CSSPrimitiveValue2.CSS_CAP:
		case CSSPrimitiveValue2.CSS_CH:
		case CSSPrimitiveValue2.CSS_IC:
		case CSSPrimitiveValue2.CSS_LH:
		case CSSPrimitiveValue2.CSS_REM:
		case CSSPrimitiveValue2.CSS_RLH:
		case CSSPrimitiveValue2.CSS_VW:
		case CSSPrimitiveValue2.CSS_VH:
		case CSSPrimitiveValue2.CSS_VI:
		case CSSPrimitiveValue2.CSS_VB:
		case CSSPrimitiveValue2.CSS_VMIN:
		case CSSPrimitiveValue2.CSS_VMAX:
			return true;
		}
		return false;
	}

	private NumberValue absoluteNumberValue(NumberValue value, boolean useParentStyle) {
		short unit = value.getPrimitiveType();
		float fv = value.getFloatValue(unit);
		if (unit == CSSPrimitiveValue.CSS_EMS) {
			if (useParentStyle) {
				fv *= getParentComputedStyle().getComputedFontSize();
			} else {
				fv *= getComputedFontSize();
			}
		} else if (unit == CSSPrimitiveValue.CSS_EXS) {
			if (getStyleDatabase() != null) {
				fv *= getStyleDatabase().getExSizeInPt(getUsedFontFamily(), getComputedFontSize());
			} else {
				if (useParentStyle) {
					fv *= getParentComputedStyle().getComputedFontSize();
				} else {
					fv *= getComputedFontSize();
				}
				fv *= 0.5f;
			}
		} else if (unit == CSSPrimitiveValue2.CSS_REM) {
			CSSElement root = getRootElement();
			if (root != getOwnerNode()) {
				fv *= root.getComputedStyle(null).getComputedFontSize();
			} else {
				fv *= getInitialFontSize();
			}
		} else if (unit == CSSPrimitiveValue2.CSS_LH) {
			if (useParentStyle) {
				fv *= getParentComputedStyle().getComputedLineHeight();
			} else {
				fv *= getComputedLineHeight();
			}
		} else if (unit == CSSPrimitiveValue2.CSS_RLH) {
			CSSElement root = getRootElement();
			if (root != getOwnerNode()) {
				fv *= root.getComputedStyle(null).getComputedLineHeight();
			} else {
				fv *= getInitialFontSize();
			}
		} else {
			CSSCanvas canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			if (unit == CSSPrimitiveValue2.CSS_CAP) {
				if (canvas != null) {
					fv *= canvas.getCapHeight(this);
				} else {
					throw new IllegalStateException("cap unit requires canvas");
				}
			} else if (unit == CSSPrimitiveValue2.CSS_CH) {
				if (canvas != null) {
					fv *= canvas.stringWidth("0", this);
				} else {
					fv *= getComputedFontSize() * 0.25f;
				}
			} else if (unit == CSSPrimitiveValue2.CSS_IC) {
				if (canvas != null) {
					fv *= canvas.stringWidth("\u6C34", this);
				} else {
					fv *= getComputedFontSize();
				}
			} else if (unit == CSSPrimitiveValue2.CSS_VW) {
				fv *= getInitialContainingBlockWidthPt(canvas) * 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VH) {
				fv *= getInitialContainingBlockHeightPt(canvas) * 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VI) {
				String writingMode = getCSSValue("writing-mode").getCssText();
				if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
					fv *= getInitialContainingBlockWidthPt(canvas);
				} else {
					fv *= getInitialContainingBlockHeightPt(canvas);
				}
				fv *= 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VB) {
				String writingMode = getCSSValue("writing-mode").getCssText();
				if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
					fv *= getInitialContainingBlockHeightPt(canvas);
				} else {
					fv *= getInitialContainingBlockWidthPt(canvas);
				}
				fv *= 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VMIN) {
				float size = Math.min(getInitialContainingBlockWidthPt(canvas),
						getInitialContainingBlockHeightPt(canvas));
				fv *= size * 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VMAX) {
				float size = Math.max(getInitialContainingBlockWidthPt(canvas),
						getInitialContainingBlockHeightPt(canvas));
				fv *= size * 0.01f;
			} else {
				fv = NumberValue.floatValueConversion(fv, unit, CSSPrimitiveValue.CSS_PT);
			}
		}
		fv = Math.round(fv * 100f) * 0.01f;
		value = new NumberValue();
		value.setFloatValuePt(fv);
		return value;
	}

	private CSSElement getRootElement() {
		ComputedCSSStyle style = this;
		CSSElement root = null;
		do {
			Node node = style.getOwnerNode();
			if (node.getNodeType() == Node.ELEMENT_NODE && !"none".equalsIgnoreCase(style.getDisplay())) {
				root = (CSSElement) node;
			}
			style = style.getParentComputedStyle();
		} while (style != null);
		return root;
	}

	private float getInitialContainingBlockWidthPt(CSSCanvas canvas) throws StyleDatabaseRequiredException {
		float fv;
		if (canvas != null) {
			Viewport viewport = canvas.getViewport();
			if (viewport != null) {
				fv = viewport.getViewportWidth();
				return NumberValue.floatValueConversion(fv, getStyleDatabase().getNaturalUnit(),
						CSSPrimitiveValue.CSS_PT);
			} else {
				StyleDatabase sdb = getStyleDatabase();
				if (sdb != null) {
					fv = sdb.getDeviceWidth();
					return NumberValue.floatValueConversion(fv, sdb.getNaturalUnit(), CSSPrimitiveValue.CSS_PT);
				}
			}
		}
		ComputedCSSStyle style = getInitialContainingBlockStyle();
		if (style != null) {
			BoxValues box;
			try {
				box = style.getBoxValues(CSSPrimitiveValue.CSS_PT);
			} catch (DOMException e) {
				throw new StyleDatabaseRequiredException(e);
			}
			return box.getWidth();
		}
		throw new StyleDatabaseRequiredException();
	}

	private float getInitialContainingBlockHeightPt(CSSCanvas canvas) throws StyleDatabaseRequiredException {
		float fv;
		if (canvas != null) {
			Viewport viewport = canvas.getViewport();
			if (viewport != null) {
				fv = viewport.getViewportHeight();
				return NumberValue.floatValueConversion(fv, getStyleDatabase().getNaturalUnit(),
						CSSPrimitiveValue.CSS_PT);
			} else {
				StyleDatabase sdb = getStyleDatabase();
				if (sdb != null) {
					fv = sdb.getDeviceHeight();
					return NumberValue.floatValueConversion(fv, sdb.getNaturalUnit(), CSSPrimitiveValue.CSS_PT);
				}
			}
		}
		ComputedCSSStyle style = getInitialContainingBlockStyle();
		if (style != null) {
			AbstractCSSValue height = style.getPropertyCSSValue("height");
			if (height != null && height.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				try {
					return ((CSSPrimitiveValue) height).getFloatValue(CSSPrimitiveValue.CSS_PT);
				} catch (DOMException e) {
					throw new StyleDatabaseRequiredException(e);
				}
			}
		}
		throw new StyleDatabaseRequiredException();
	}

	private ComputedCSSStyle getInitialContainingBlockStyle() {
		ComputedCSSStyle styledecl = this;
		String position = styledecl.getPropertyValue("position");
		if ("fixed".equalsIgnoreCase(position) || "absolute".equalsIgnoreCase(position)) {
			return null;
		}
		// loop until display is block-level
		String display = styledecl.getPropertyValue("display");
		if ("table-cell".equals(display)) {
			do {
				styledecl = styledecl.getParentComputedStyle();
				if (styledecl == null) {
					break;
				} else {
					display = styledecl.getPropertyValue("display");
				}
			} while (!"table".equals(display));
		} else {
			do {
				styledecl = styledecl.getParentComputedStyle();
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

	private void absoluteExpressionValue(CSSExpression expr, boolean useParentStyle) {
		switch (expr.getPartType()) {
		case SUM:
		case PRODUCT:
			List<? extends CSSExpression> operands = ((CSSExpression.AlgebraicExpression) expr).getOperands();
			Iterator<? extends CSSExpression> it = operands.iterator();
			while (it.hasNext()) {
				absoluteExpressionValue(it.next(), useParentStyle);
			}
			break;
		case OPERAND:
			OperandExpression operand = (OperandExpression) expr;
			AbstractCSSPrimitiveValue primi = (AbstractCSSPrimitiveValue) operand.getOperand();
			operand.setOperand(absolutePrimitiveValue(primi, useParentStyle));
		}
	}

	private AbstractCSSPrimitiveValue absoluteFontSizeValue(AbstractCSSValue value) {
		AbstractCSSPrimitiveValue primi;
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			primi = absoluteFontSizePrimitive((AbstractCSSPrimitiveValue) value);
		} else {
			computedStyleError("font-size", value.getCssText(), "font-size is not a primitive value");
			float sz = getInitialFontSize();
			NumberValue number = new NumberValue();
			number.setFloatValuePt(sz);
			number.setSubproperty(value.isSubproperty());
			primi = number;
		}
		return primi;
	}

	private AbstractCSSPrimitiveValue absoluteFontSizePrimitive(AbstractCSSPrimitiveValue cssSize) {
		float sz;
		switch (cssSize.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_EMS:
			float factor = cssSize.getFloatValue(CSSPrimitiveValue.CSS_EMS);
			// Use parent element's size.
			sz = getParentElementFontSize() * factor;
			break;
		case CSSPrimitiveValue.CSS_EXS:
			factor = cssSize.getFloatValue(CSSPrimitiveValue.CSS_EXS);
			// Use parent element's size.
			CSSComputedProperties parentStyle = getParentComputedStyle();
			if (parentStyle == null) {
				sz = getInitialFontSize() * 0.5f * factor;
			} else {
				if (getStyleDatabase() != null) {
					sz = getStyleDatabase().getExSizeInPt(parentStyle.getUsedFontFamily(),
							parentStyle.getComputedFontSize()) * factor;
				} else {
					sz = parentStyle.getComputedFontSize() * 0.5f * factor;
				}
			}
			break;
		case CSSPrimitiveValue2.CSS_REM:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_REM);
			CSSElement root = getRootElement();
			if (root != getOwnerNode()) {
				sz = root.getComputedStyle(null).getComputedFontSize() * factor;
			} else {
				sz = getInitialFontSize();
			}
			break;
		case CSSPrimitiveValue2.CSS_LH:
			parentStyle = getParentComputedStyle();
			if (parentStyle != null) {
				factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_LH);
				sz = parentStyle.getComputedLineHeight() * factor;
			} else {
				sz = getInitialFontSize();
			}
			break;
		case CSSPrimitiveValue2.CSS_RLH:
			root = getRootElement();
			if (root != getOwnerNode()) {
				factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_RLH);
				sz = root.getComputedStyle(null).getComputedLineHeight() * factor;
			} else {
				sz = getInitialFontSize();
			}
			break;
		case CSSPrimitiveValue2.CSS_CAP:
			CSSCanvas canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			if (canvas != null) {
				parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_CAP);
					sz = canvas.getCapHeight(parentStyle) * factor;
					break;
				}
			}
			sz = getInitialFontSize();
			break;
		case CSSPrimitiveValue2.CSS_CH:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_CH);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			if (canvas != null) {
				parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					sz = canvas.stringWidth("0", parentStyle) * factor;
					break;
				}
			}
			sz = getParentElementFontSize() * 0.25f * factor;
			break;
		case CSSPrimitiveValue2.CSS_IC:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_IC);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			if (canvas != null) {
				parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					sz = canvas.stringWidth("\u6C34", parentStyle) * factor;
					break;
				}
			}
			sz = getParentElementFontSize() * factor;
			break;
		case CSSPrimitiveValue.CSS_IDENT:
			// Conversion to lower case is probably not needed
			String sizeIdentifier = cssSize.getStringValue().toLowerCase(Locale.US);
			try {
				// relative size: larger, smaller.
				String familyName = getUsedFontFamily();
				if ("larger".equals(sizeIdentifier)) {
					sz = getLargerFontSize(familyName);
				} else if ("smaller".equals(sizeIdentifier)) {
					sz = getSmallerFontSize(familyName);
				} else {
					sz = getFontSizeFromIdentifier(familyName, sizeIdentifier);
				}
			} catch (DOMException e) {
				computedStyleError("font-size", sizeIdentifier, "Unknown identifier");
				sz = getInitialFontSize();
			}
			break;
		case CSSPrimitiveValue.CSS_PERCENTAGE:
			float pcnt = cssSize.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
			// Use parent element's size.
			sz = getParentElementFontSize() * pcnt * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VW:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VW);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			sz = getInitialContainingBlockWidthPt(canvas) * factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VH:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VH);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			sz = getInitialContainingBlockHeightPt(canvas) * factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VI:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VI);
			String writingMode = getCSSValue("writing-mode").getCssText();
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
				sz = getInitialContainingBlockWidthPt(canvas);
			} else {
				sz = getInitialContainingBlockHeightPt(canvas);
			}
			sz *= factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VB:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VB);
			writingMode = getCSSValue("writing-mode").getCssText();
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
				sz = getInitialContainingBlockHeightPt(canvas);
			} else {
				sz = getInitialContainingBlockWidthPt(canvas);
			}
			sz *= factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VMIN:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VMIN);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			sz = Math.min(getInitialContainingBlockWidthPt(canvas), getInitialContainingBlockHeightPt(canvas));
			sz *= factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VMAX:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VMAX);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			sz = Math.max(getInitialContainingBlockWidthPt(canvas), getInitialContainingBlockHeightPt(canvas));
			sz *= factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_EXPRESSION:
			cssSize = cssSize.clone();
			AbstractCSSExpression expr = ((ExpressionValue) cssSize).getExpression();
			absoluteExpressionValue(expr, true);
			Evaluator ev = new FontEvaluator();
			try {
				cssSize = (AbstractCSSPrimitiveValue) ev.evaluateExpression(expr);
			} catch (DOMException e) {
			}
			return cssSize;
		case CSSPrimitiveValue2.CSS_FUNCTION:
			FunctionValue function = (FunctionValue) cssSize;
			function = function.clone();
			LinkedCSSValueList args = function.getArguments();
			int siz = args.size();
			for (int i = 0; i < siz; i++) {
				args.set(i, absoluteValue(args.get(i), true));
			}
			ev = new FontEvaluator();
			try {
				cssSize = (AbstractCSSPrimitiveValue) ev.evaluateFunction(function);
			} catch (DOMException e) {
			}
			return cssSize;
		// Check for custom properties ('variables')
		case CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY:
			AbstractCSSValue custom = getCSSValue(cssSize.getStringValue());
			if (custom == null) {
				custom = ((CustomPropertyValue) cssSize).getFallback();
				if (custom != null && custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					cssSize = absoluteFontSizePrimitive((AbstractCSSPrimitiveValue) custom);
				}
			} else if (custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				cssSize = absoluteFontSizePrimitive((AbstractCSSPrimitiveValue) custom);
			}
			return cssSize;
		default:
			try {
				cssSize.getFloatValue(CSSPrimitiveValue.CSS_PT);
			} catch (DOMException e) {
				reportFontSizeError(cssSize, e);
				sz = getInitialFontSize();
				break;
			}
			return cssSize;
		}
		sz = Math.round(sz * 100f) * 0.01f;
		NumberValue number = new NumberValue();
		number.setFloatValuePt(sz);
		number.setSubproperty(cssSize.isSubproperty());
		return number;
	}

	private float getInitialFontSize() {
		String familyName = getUsedFontFamily();
		return getFontSizeFromIdentifier(familyName, "medium");
	}

	private class MyEvaluator extends Evaluator {

		@Override
		protected ExtendedCSSPrimitiveValue absoluteValue(ExtendedCSSPrimitiveValue partialValue) {
			return absolutePrimitiveValue((AbstractCSSPrimitiveValue) partialValue, false);
		}

	}

	private class FontEvaluator extends MyEvaluator {

		@Override
		protected ExtendedCSSPrimitiveValue absoluteValue(ExtendedCSSPrimitiveValue partialValue) {
			return absolutePrimitiveValue((AbstractCSSPrimitiveValue) partialValue, true);
		}

		@Override
		protected float percentage(ExtendedCSSPrimitiveValue value, short resultType) throws DOMException {
			float pcnt = value.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
			// Use parent element's size.
			return getParentElementFontSize() * pcnt * 0.01f;
		}

	}

	private AbstractCSSValue applyDisplayConstrains(AbstractCSSValue value) {
		AbstractCSSValue computedValue = value;
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return value;
		}
		// CSS spec, sect. 9.7
		String strVal = ((CSSPrimitiveValue) value).getStringValue();
		if (!"none".equals(strVal)) {
			String position = ((CSSPrimitiveValue) getCSSValue("position")).getStringValue();
			if ("absolute".equals(position) || "fixed".equals(position)) {
				computedValue = computeConstrainedDisplay(value);
			} else {
				String floatProp = ((CSSPrimitiveValue) getCSSValue("float")).getStringValue();
				Node node;
				/*
				 * If float is 'none' or the owner node is the root element (here checked as
				 * "parent node is null"), then constrain 'display'
				 */
				if (!"none".equals(floatProp) || ((node = getOwnerNode()) != null && node.getParentNode() == null)) {
					computedValue = computeConstrainedDisplay(value);
				}
			}

		}
		return computedValue;
	}

	/**
	 * Table of computed values of 'display' property, per CSS spec, sect. 9.7.
	 * 
	 * @param value
	 *            the value to constrain.
	 * @return the constrained value.
	 */
	private AbstractCSSValue computeConstrainedDisplay(AbstractCSSValue value) {
		String display = ((CSSPrimitiveValue) value).getStringValue();
		if ("inline-table".equals(display)) {
			return new IdentifierValue("table");
		} else if ("inline".equals(display) || "run-in".equals(display) || "table-row-group".equals(display)
				|| "table-column".equals(display) || "table-column-group".equals(display)
				|| "table-header-group".equals(display) || "table-footer-group".equals(display)
				|| "table-row".equals(display) || "table-cell".equals(display) || "table-caption".equals(display)
				|| "inline-block".equals(display)) {
			return new IdentifierValue("block");
		}
		return value;
	}

	private AbstractCSSValue applyBorderWidthConstrains(String property, AbstractCSSValue value) {
		String style = null;
		if (property.equals("border-top-width")) {
			style = getCSSValue("border-top-style").getCssText();
		} else if (property.equals("border-right-width")) {
			style = getCSSValue("border-top-style").getCssText();
		} else if (property.equals("border-bottom-width")) {
			style = getCSSValue("border-top-style").getCssText();
		} else if (property.equals("border-left-width")) {
			style = getCSSValue("border-top-style").getCssText();
		}
		if (style != null && (style.equals("none") || style.equals("hidden"))) {
			value = new ValueFactory().parseProperty("0");
		}
		return value;
	}

	private AbstractCSSValue computeBackgroundRepeat(AbstractCSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				// It is a list of layer values
				for (int i = 0; i < list.getLength(); i++) {
					AbstractCSSValue item = list.item(i);
					if (item.getCssValueType() != CSSValue.CSS_VALUE_LIST) {
						list.set(i, computeBackgroundRepeatPrimitive(item));
					}
				}
			}
		} else {
			value = computeBackgroundRepeatPrimitive(value);
		}
		return value;
	}

	private AbstractCSSValue computeBackgroundRepeatPrimitive(AbstractCSSValue value) {
		ValueList list = ValueList.createWSValueList();
		String s = value.getCssText();
		if (s.equals("repeat-y")) {
			list.add(new IdentifierValue("no-repeat"));
			list.add(new IdentifierValue("repeat"));
		} else if (s.equals("repeat-x")) {
			list.add(new IdentifierValue("repeat"));
			list.add(new IdentifierValue("no-repeat"));
		} else if (s.equals("repeat")) {
			list.add(new IdentifierValue("repeat"));
			list.add(new IdentifierValue("repeat"));
		} else if (s.equals("no-repeat")) {
			list.add(new IdentifierValue("no-repeat"));
			list.add(new IdentifierValue("no-repeat"));
		} else if (s.equals("space")) {
			list.add(new IdentifierValue("space"));
			list.add(new IdentifierValue("space"));
		} else if (s.equals("round")) {
			list.add(new IdentifierValue("round"));
			list.add(new IdentifierValue("round"));
		} else {
			return value;
		}
		return list;
	}

	@Override
	protected AbstractCSSPrimitiveValue getCurrentColor() {
		return getCSSColor();
	}

	@Override
	public AbstractCSSPrimitiveValue getCSSColor() {
		return (AbstractCSSPrimitiveValue) getCSSValue("color");
	}

	@Override
	public ExtendedCSSPrimitiveValue getCSSBackgroundColor() {
		return (ExtendedCSSPrimitiveValue) getCSSValue("background-color");
	}

	@Override
	public String[] getBackgroundImages() {
		CSSValue cssVal = getCSSValue("background-image");
		if (cssVal == null) {
			return null;
		}
		String baseHref;
		if (cssVal instanceof WrappedValue) {
			baseHref = ((WrappedValue) cssVal).getParentSheetHref();
		} else {
			CSSDeclarationRule pRule = getParentRule();
			if (pRule != null) {
				baseHref = pRule.getParentStyleSheet().getHref();
			} else {
				baseHref = null;
			}
		}
		if (cssVal.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			return new String[] { getHref((CSSPrimitiveValue) cssVal, baseHref) };
		} else {
			CSSValueList list = (CSSValueList) cssVal;
			int len = list.getLength();
			String[] sa = new String[len];
			for (int i = 0; i < len; i++) {
				sa[i] = getHref((CSSPrimitiveValue) list.item(i), baseHref);
			}
			return sa;
		}
	}

	/**
	 * Attempts to get the absolute Href from the string value of a (uri)
	 * property.
	 * 
	 * @param cssVal
	 *            the uri value.
	 * @param baseHref
	 *            the base href context.
	 * @return the base URL, or null if could not be determined.
	 */
	protected String getHref(CSSPrimitiveValue cssVal, String baseHref) {
		String href = cssVal.getStringValue();
		if (!href.contains("://")) {
			// Relative URL
			URL baseUrl = null;
			if (baseHref == null) {
				String documentURI = getOwnerNode().getBaseURI();
				if (documentURI != null) {
					try {
						baseUrl = new URL(documentURI);
					} catch (MalformedURLException e) {
						// This will never happen
					}
				}
			} else if (!baseHref.contains("://")) {
				try {
					baseUrl = new URL(new URL(getOwnerNode().getBaseURI()), baseHref);
				} catch (MalformedURLException e) {
					getStyleDeclarationErrorHandler().malformedURIValue(baseHref);
				}
			} else {
				try {
					baseUrl = new URL(baseHref);
				} catch (MalformedURLException e) {
					getStyleDeclarationErrorHandler().malformedURIValue(baseHref);
				}
			}
			if (baseUrl != null) {
				try {
					URL url = new URL(baseUrl, href);
					href = url.toExternalForm();
				} catch (MalformedURLException e) {
					getStyleDeclarationErrorHandler().malformedURIValue(href);
				}
			}
		}
		return href;
	}

	/**
	 * Gets the used value of the font-family property.
	 * <p>
	 * If the computed value is a list, it returns the first item. If a style
	 * database is available, it returns the 'used' value.
	 * 
	 * @return the value of the font-family property.
	 */
	@Override
	public String getUsedFontFamily() {
		String requestedFamily = scanFontFamilyValue(getCSSValue("font-family"), this);
		if (requestedFamily == null) {
			requestedFamily = getStyleDatabase().getDefaultGenericFontFamily();
		}
		return requestedFamily;
	}

	private String scanFontFamilyValue(AbstractCSSValue value, CSSComputedProperties style) {
		String requestedFamily = null;
		if (value != null) {
			if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				ValueList fontList = (ValueList) value;
				Iterator<AbstractCSSValue> it = fontList.iterator();
				while (it.hasNext()) {
					AbstractCSSValue item = it.next();
					requestedFamily = stringValueOrNull(item);
					if (requestedFamily != null && isFontFamilyAvailable(requestedFamily)) {
						return requestedFamily;
					}
				}
			} else {
				requestedFamily = stringValueOrNull(value);
				if (requestedFamily != null && isFontFamilyAvailable(requestedFamily)) {
					return requestedFamily;
				}
			}
		}
		CSSComputedProperties ancStyle = style.getParentComputedStyle();
		if (ancStyle != null) {
			value = ((BaseCSSStyleDeclaration) ancStyle).getDeclaredCSSValue("font-family");
			requestedFamily = scanFontFamilyValue(value, ancStyle);
		}
		return requestedFamily;
	}

	private String stringValueOrNull(AbstractCSSValue value) {
		CSSPrimitiveValue primi;
		short ptype;
		String s;
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((ptype = (primi = (CSSPrimitiveValue) value).getPrimitiveType()) == CSSPrimitiveValue.CSS_STRING
						|| ptype == CSSPrimitiveValue.CSS_IDENT)) {
			s = primi.getStringValue();
		} else {
			s = null;
		}
		return s;
	}

	/**
	 * Gets the computed value of the font-family property.
	 * 
	 * @return the value of the font-family property.
	 */
	@Override
	public String getFontFamily() {
		return getCSSValue("font-family").getCssText();
	}

	/**
	 * Gets the computed value of the font-size property.
	 * <p>
	 * May require a style database to give accurate results.
	 * </p>
	 * 
	 * @return the value of the font-size property, in typographic points.
	 */
	@Override
	public float getComputedFontSize() {
		// font-size is guaranteed to be a numeric primitive here
		CSSPrimitiveValue cssSize = (CSSPrimitiveValue) getCSSValue("font-size");
		float sz;
		try {
			sz = cssSize.getFloatValue(CSSPrimitiveValue.CSS_PT);
		} catch (DOMException e) {
			computedStyleError("font-size", cssSize.getCssText(), e.getMessage());
			sz = getInitialFontSize();
		}
		return sz;
	}

	private void computedStyleError(String propertyName, String propertyValue, String message) {
		((CSSDocument) getOwnerNode().getOwnerDocument()).getStyleSheet().getErrorHandler()
				.computedStyleError(getOwnerNode(), propertyName, propertyValue, message);
	}

	private void reportFontSizeError(CSSPrimitiveValue cssSize, DOMException e) {
		String cssText = cssSize.getCssText();
		computedStyleError("font-size", cssText, "Error converting to points");
	}

	private float getFontSizeFromIdentifier(String familyName, String sizeIdentifier) {
		if (getStyleDatabase() != null) {
			return getStyleDatabase().getFontSizeFromIdentifier(familyName, sizeIdentifier);
		} else {
			float sz;
			if (sizeIdentifier.equals("medium")) {
				sz = 12f;
			} else if (sizeIdentifier.equals("x-small")) {
				sz = 9f;
			} else if (sizeIdentifier.equals("small")) {
				sz = 10f;
			} else if (sizeIdentifier.equals("xx-small")) {
				sz = 8f;
			} else if (sizeIdentifier.equals("large")) {
				sz = 14f;
			} else if (sizeIdentifier.equals("x-large")) {
				sz = 16f;
			} else if (sizeIdentifier.equals("xx-large")) {
				sz = 18f;
			} else {
				sz = 12f; // default
			}
			return sz;
		}
	}

	private float getLargerFontSize(String familyName) {
		float sz;
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			CSSPrimitiveValue csssize = (CSSPrimitiveValue) parentCss.getCSSValue("font-size");
			if (csssize != null) {
				switch (csssize.getPrimitiveType()) {
				case CSSPrimitiveValue.CSS_IDENT:
					String baseFontSize = csssize.getStringValue();
					if (baseFontSize.equals("xx-small")) {
						sz = getFontSizeFromIdentifier(familyName, "x-small");
					} else if (baseFontSize.equals("x-small")) {
						sz = getFontSizeFromIdentifier(familyName, "small");
					} else if (baseFontSize.equals("small")) {
						sz = getFontSizeFromIdentifier(familyName, "medium");
					} else if (baseFontSize.equals("medium")) {
						sz = getFontSizeFromIdentifier(familyName, "large");
					} else if (baseFontSize.equals("large")) {
						sz = getFontSizeFromIdentifier(familyName, "x-large");
					} else if (baseFontSize.equals("x-large")) {
						sz = getFontSizeFromIdentifier(familyName, "xx-large");
					} else if (baseFontSize.equals("xx-large")) {
						sz = 2f * getFontSizeFromIdentifier(familyName, "xx-large")
								- getFontSizeFromIdentifier(familyName, "x-large");
					} else {
						computedStyleError("font-size", baseFontSize, "Unknown identifier");
						sz = getFontSizeFromIdentifier(familyName, "medium") * 1.2f;
					}
					break;
				default:
					sz = parentCss.getComputedFontSize() * 1.2f;
				}
				return sz;
			}
		}
		return getFontSizeFromIdentifier(familyName, "medium") * 1.2f;
	}

	private float getSmallerFontSize(String familyName) {
		float sz;
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			CSSPrimitiveValue csssize = (CSSPrimitiveValue) parentCss.getCSSValue("font-size");
			if (csssize != null) {
				switch (csssize.getPrimitiveType()) {
				case CSSPrimitiveValue.CSS_IDENT:
					String baseFontSize = csssize.getStringValue();
					if (baseFontSize.equals("xx-small")) {
						sz = 2f * getFontSizeFromIdentifier(familyName, "xx-small")
								- getFontSizeFromIdentifier(familyName, "x-small");
						// Safety check
						if (sz < 0.1f) {
							sz = getFontSizeFromIdentifier(familyName, "xx-small");
						}
					} else if (baseFontSize.equals("x-small")) {
						sz = getFontSizeFromIdentifier(familyName, "xx-small");
					} else if (baseFontSize.equals("small")) {
						sz = getFontSizeFromIdentifier(familyName, "x-small");
					} else if (baseFontSize.equals("medium")) {
						sz = getFontSizeFromIdentifier(familyName, "small");
					} else if (baseFontSize.equals("large")) {
						sz = getFontSizeFromIdentifier(familyName, "medium");
					} else if (baseFontSize.equals("x-large")) {
						sz = getFontSizeFromIdentifier(familyName, "large");
					} else if (baseFontSize.equals("xx-large")) {
						sz = getFontSizeFromIdentifier(familyName, "x-large");
					} else {
						computedStyleError("font-size", baseFontSize, "Unknown identifier");
						sz = getFontSizeFromIdentifier(familyName, "medium") * 0.82f;
					}
					break;
				default:
					sz = parentCss.getComputedFontSize() * 0.82f;
				}
				return sz;
			}
		}
		return getFontSizeFromIdentifier(familyName, "medium") * 0.82f;
	}

	private float getParentElementFontSize() {
		float sz;
		CSSComputedProperties parentCss = getParentComputedStyle();
		if (parentCss != null) {
			sz = parentCss.getComputedFontSize();
		} else {
			sz = getInitialFontSize();
		}
		return sz;
	}

	private boolean isFontFamilyAvailable(String requestedFamily) {
		StyleDatabase sdb = getStyleDatabase();
		if (sdb == null || sdb.isFontFamilyAvailable(requestedFamily)) {
			return true;
		}
		CSSCanvas canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
		if (canvas != null) {
			return canvas.isFontFaceName(requestedFamily);
		}
		return false;
	}

	/**
	 * Gets the computed line height with the default 'normal' value of 1.16em.
	 * 
	 * @return the default computed line height, in typographic points.
	 */
	@Override
	public float getComputedLineHeight() {
		return getComputedLineHeight(1.16f);
	}

	/**
	 * Gets the computed line height, in typographic points.
	 * 
	 * @param defval the default value in EMs.
	 * @return the computed line height, or the default value if the computed value
	 *         could not be found.
	 */
	public float getComputedLineHeight(float defval) {
		float height;
		CSSPrimitiveValue cssval = (CSSPrimitiveValue) getCSSValue("line-height");
		if (cssval == null) {
			// No 'line-height' found, applying default
			return defval * getComputedFontSize();
		}
		short declType = cssval.getPrimitiveType();
		if (declType == CSSPrimitiveValue.CSS_PERCENTAGE) {
			height = getComputedFontSize() * cssval.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE) / 100f;
		} else if (declType == CSSPrimitiveValue.CSS_IDENT) {
			// expect "normal"
			if (!"normal".equals(cssval.getStringValue())) {
				computedStyleError("line-height", cssval.getStringValue(), "Wrong value: expected 'normal'");
			}
			height = defval * getComputedFontSize();
		} else if (cssval instanceof NumberValue) {
			height = cssval.getFloatValue(declType);
			if (declType != CSSPrimitiveValue.CSS_PT) {
				height = NumberValue.floatValueConversion(height, declType, CSSPrimitiveValue.CSS_PT);
			}
		} else {
			computedStyleError("line-height", cssval.getStringValue(), "Wrong value: expected number or identifier");
			height = defval * getComputedFontSize();
		}
		return height;
	}

	/**
	 * Get the box values from a simple box model.
	 * 
	 * @param unitType
	 *            the desired unit type.
	 * @return the box values, in the specified unit.
	 * @throws DOMException
	 *             if the document contains features that are not supported by
	 *             the simple model.
	 * @throws StyleDatabaseRequiredException
	 *             when a computation that requires a style database is
	 *             attempted, but no style database has been set.
	 */
	@Override
	public BoxValues getBoxValues(short unitType) throws DOMException, StyleDatabaseRequiredException {
		return new MyDefaultBoxModel().getComputedBox(unitType);
	}

	@Override
	public String getCssText() {
		return getPlainCssText();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		writeComputedCssText(wri, context);
	}

	@Override
	public String getMinifiedCssText() {
		return getOptimizedCssText();
	}

	/**
	 * Gets the computed style for the parent element.
	 * 
	 * @return the computed style for the parent element, or null if there is no
	 *         parent element, or has no style associated.
	 */
	@Override
	abstract public ComputedCSSStyle getParentComputedStyle();

	/**
	 * Gets the (whitespace-trimmed) text content of the node associated to this
	 * style.
	 * 
	 * @return the text content, or the empty string if the box has no text.
	 */
	public String getText() {
		return BoxModelHelper.contractSpaces(getOwnerNode().getTextContent()).trim();
	}

	@Override
	abstract public ComputedCSSStyle clone();

	private class MyDefaultBoxModel extends SimpleBoxModel {
		MyDefaultBoxModel() {
			super();
		}

		@Override
		protected ComputedCSSStyle getComputedStyle() {
			return ComputedCSSStyle.this;
		}
	}
}
