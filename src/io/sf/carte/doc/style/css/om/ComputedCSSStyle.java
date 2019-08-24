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
import java.util.LinkedList;
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
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.CustomPropertyValue;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.ExpressionValue;
import io.sf.carte.doc.style.css.property.FunctionValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
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

	private transient LinkedList<String> customPropertyStack = null;

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
	String getUnknownPropertyPriority(String propertyName) {
		return checkShorthandPriority(propertyName);
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
		boolean inherited = propertydb.isInherited(property) || property.startsWith("--");
		// Check for unset
		if (value != null && isCSSIdentifier(value, "unset")) {
			/*
			 * The 'unset' keyword acts as either inherit or initial, depending on whether the
			 * property is inherited or not.
			 */
			value = null;
		}
		/*
		 * We compute inherited value, if appropriate.
		 */
		value = inheritValue(this, property, value, inherited);
		// Still inheriting ?
		if (value != null && value.getCssValueType() == CSSValue.CSS_INHERIT) {
			value = null;
		}
		value = computeValue(property, value, inherited, propertydb);
		return value;
	}

	private boolean isCSSIdentifier(CSSValue value, String ident) {
		CSSPrimitiveValue primi;
		return value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& (primi = (CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& ident.equalsIgnoreCase(primi.getStringValue());
	}

	private AbstractCSSValue inheritValue(ComputedCSSStyle ancStyle, String propertyName, AbstractCSSValue value,
			boolean inherited) {
		while (value == null ? inherited : value.getCssValueType() == CSSValue.CSS_INHERIT) {
			ancStyle = ancStyle.getParentComputedStyle();
			if (ancStyle == null) {
				break;
			}
			if (ancStyle.isPropertySet(propertyName)) {
				value = ancStyle.getCSSValue(propertyName);
				if (value != null && isCSSIdentifier(value, "unset")) {
					value = null;
				}
			}
		}
		return value;
	}

	private AbstractCSSValue computeValue(String property, AbstractCSSValue value, boolean inherited,
			PropertyDatabase propertydb) {
		// Check for null, and apply initial values if appropriate
		if (value == null || isCSSIdentifier(value, "initial")) {
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
				return colorValue(property, (AbstractCSSPrimitiveValue) value);
			}
			if (property.equals("font-size")) {
				value = absoluteFontSizeValue(value, false);
			} else {
				// Convert to absolute units
				try {
					value = absoluteValue(value, false);
				} catch (DOMException e) {
					computedStyleError(property, value.getCssText(), e.getMessage());
					value = null;
				}
				if (value == null) {
					if (inherited) {
						ComputedCSSStyle ancStyle = this;
						do {
							value = inheritValue(ancStyle, property, value, true);
							value = ancStyle.computeValue(property, value, inherited, propertydb);
							if (value != null) {
								break;
							}
							ancStyle = ancStyle.getParentComputedStyle();
						} while (ancStyle != null);
					}
					if (value == null || value.getCssValueType() == CSSValue.CSS_INHERIT) {
						value = defaultPropertyValue(property, propertydb);
					}
				}
			}
		}
		return value;
	}

	private AbstractCSSPrimitiveValue colorValue(String propertyName, AbstractCSSPrimitiveValue primi) {
		if (primi.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
			String s = primi.getStringValue().toLowerCase(Locale.ROOT);
			if ("currentcolor".equals(s)) {
				if (!"color".equals(propertyName)) {
					primi = getCSSColor();
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
						primi = (AbstractCSSPrimitiveValue) getValueFactory().parseProperty(spec);
					} catch (DOMException e) {
					}
				}
			}
		}
		return primi;
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
				value = evaluateCustomProperty((CustomPropertyValue) primi, useParentStyle);
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
			} catch (DOMException | IllegalStateException e) {
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
				AbstractCSSValue custom = evaluateCustomProperty((CustomPropertyValue) pri, useParentStyle);
				if (custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					pri = (AbstractCSSPrimitiveValue) custom;
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
			CSSElement root = (CSSElement) getOwnerNode().getOwnerDocument().getDocumentElement();
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
			CSSElement root = (CSSElement) getOwnerNode().getOwnerDocument().getDocumentElement();
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
				fv *= getInitialContainingBlockWidthPt(canvas, true) * 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VH) {
				fv *= getInitialContainingBlockHeightPt(canvas, true) * 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VI) {
				String writingMode = getCSSValue("writing-mode").getCssText();
				if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
					fv *= getInitialContainingBlockWidthPt(canvas, true);
				} else {
					fv *= getInitialContainingBlockHeightPt(canvas, true);
				}
				fv *= 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VB) {
				String writingMode = getCSSValue("writing-mode").getCssText();
				if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
					fv *= getInitialContainingBlockHeightPt(canvas, true);
				} else {
					fv *= getInitialContainingBlockWidthPt(canvas, true);
				}
				fv *= 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VMIN) {
				float size = Math.min(getInitialContainingBlockWidthPt(canvas, true),
						getInitialContainingBlockHeightPt(canvas, true));
				fv *= size * 0.01f;
			} else if (unit == CSSPrimitiveValue2.CSS_VMAX) {
				float size = Math.max(getInitialContainingBlockWidthPt(canvas, true),
						getInitialContainingBlockHeightPt(canvas, true));
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

	private float getInitialContainingBlockWidthPt(CSSCanvas canvas, boolean force)
			throws StyleDatabaseRequiredException {
		float fv;
		if (canvas != null) {
			Viewport viewport = canvas.getViewport();
			if (viewport != null) {
				fv = viewport.getViewportWidth();
				return NumberValue.floatValueConversion(fv, getStyleDatabase().getNaturalUnit(),
						CSSPrimitiveValue.CSS_PT);
			}
		}
		StyleDatabase sdb = getStyleDatabase();
		if (sdb != null) {
			fv = sdb.getDeviceWidth();
			return NumberValue.floatValueConversion(fv, sdb.getNaturalUnit(), CSSPrimitiveValue.CSS_PT);
		}
		String medium;
		if (force && (medium = ((CSSDocument) getOwnerNode().getOwnerDocument()).getTargetMedium()) != null) {
			if ("print".equals(medium)) {
				return 595f; // A4
			} else if ("screen".equals(medium)) {
				return 1440f;
			} else if ("handheld".equals(medium)) {
				return 270f;
			}
		}
		throw new StyleDatabaseRequiredException("Unit conversion failed.");
	}

	private float getInitialContainingBlockHeightPt(CSSCanvas canvas, boolean force) throws StyleDatabaseRequiredException {
		float fv;
		if (canvas != null) {
			Viewport viewport = canvas.getViewport();
			if (viewport != null) {
				fv = viewport.getViewportHeight();
				return NumberValue.floatValueConversion(fv, getStyleDatabase().getNaturalUnit(),
						CSSPrimitiveValue.CSS_PT);
			}
		}
		StyleDatabase sdb = getStyleDatabase();
		if (sdb != null) {
			fv = sdb.getDeviceHeight();
			return NumberValue.floatValueConversion(fv, sdb.getNaturalUnit(), CSSPrimitiveValue.CSS_PT);
		}
		String medium;
		if (force && (medium = ((CSSDocument) getOwnerNode().getOwnerDocument()).getTargetMedium()) != null) {
			if ("print".equals(medium)) {
				return 842f; // A4
			} else if ("screen".equals(medium)) {
				return 810f;
			} else if ("handheld".equals(medium)) {
				return 480f;
			}
		}
		throw new StyleDatabaseRequiredException("Unit conversion failed.");
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

	private AbstractCSSValue evaluateCustomProperty(CustomPropertyValue value, boolean useParentStyle) {
		String propertyName = getCanonicalPropertyName(value.getStringValue());
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<String>();
		} else if (customPropertyStack.contains(propertyName)) {
			AbstractCSSValue custom = value.getFallback();
			if (custom != null) {
				return absoluteValue(custom, useParentStyle);
			} else {
				customPropertyStack.clear();
				throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Dependency loop in " + propertyName);
			}
		}
		customPropertyStack.add(propertyName);
		AbstractCSSValue custom;
		try {
			custom = getCSSValue(propertyName);
			if (custom == null) {
				custom = value.getFallback();
				if (custom != null) {
					custom = absoluteValue(custom, useParentStyle);
				} else {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Unable to evaluate custom property " + propertyName);
				}
			} else {
				custom = absoluteValue(custom, useParentStyle);
			}
		} catch (Exception e) {
			customPropertyStack.clear();
			throw e;
		}
		customPropertyStack.remove(propertyName);
		return custom;
	}

	private AbstractCSSPrimitiveValue getFontSizeValue() {
		AbstractCSSValue value = super.getCSSValue("font-size");
		// Check for unset
		if (value != null && isCSSIdentifier(value, "unset")) {
			/*
			 * The 'unset' keyword acts as either inherit or initial, depending on whether the
			 * property is inherited or not.
			 */
			value = null;
		}
		/*
		 * We compute inherited value, if appropriate.
		 */
		value = inheritValue(this, "font-size", value, true);
		// Still inheriting ?
		if (value != null && value.getCssValueType() == CSSValue.CSS_INHERIT) {
			value = null;
		}
		// Check for null, and apply initial values if appropriate
		if (value == null || isCSSIdentifier(value, "initial")) {
			return new IdentifierValue("medium");
		}
		return absoluteFontSizeValue(value, false);
	}

	private AbstractCSSPrimitiveValue absoluteFontSizeValue(AbstractCSSValue value, boolean force) {
		AbstractCSSPrimitiveValue primi;
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			primi = absoluteFontSizePrimitive((AbstractCSSPrimitiveValue) value, force);
			if (primi == null) {
				ComputedCSSStyle ancStyle = this;
				do {
					AbstractCSSValue inheritedValue = inheritValue(ancStyle, "font-size", primi, true);
					primi = absoluteFontSizeValue(inheritedValue, force);
					if (primi != null) {
						break;
					}
					ancStyle = ancStyle.getParentComputedStyle();
				} while (ancStyle != null);
				if (primi != null) {
					return primi;
				}
			} else {
				return primi;
			}
		}
		reportFontSizeError(value, "Could not compute font-size from " + value.getCssText());
		if (force) {
			float sz = getInitialFontSize();
			return asNumericValuePt(sz);
		}
		return new IdentifierValue("medium");
	}

	private AbstractCSSPrimitiveValue absoluteFontSizePrimitive(AbstractCSSPrimitiveValue cssSize, boolean force) {
		float sz;
		switch (cssSize.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_EMS:
			float factor = cssSize.getFloatValue(CSSPrimitiveValue.CSS_EMS);
			// Use parent element's size.
			return getRelativeFontSize(cssSize, factor, force);
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
				} else if (force) {
					factor = 0.5f * factor;
					return getRelativeFontSize(cssSize, factor, force);
				} else {
					return cssSize;
				}
			}
			break;
		case CSSPrimitiveValue2.CSS_REM:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_REM);
			CSSElement root = (CSSElement) getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				sz = root.getComputedStyle(null).getComputedFontSize();
			} else if (force) {
				sz = getInitialFontSize();
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSPrimitiveValue2.CSS_LH:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_LH);
			parentStyle = getParentComputedStyle();
			if (parentStyle != null) {
				sz = parentStyle.getComputedLineHeight();
			} else if (force) {
				sz = getInitialFontSize();
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSPrimitiveValue2.CSS_RLH:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_RLH);
			root = (CSSElement) getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				sz = root.getComputedStyle(null).getComputedLineHeight();
			} else if (force) {
				sz = getInitialFontSize();
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSPrimitiveValue2.CSS_CAP:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_CAP);
			CSSCanvas canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			if (canvas != null) {
				parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					sz = canvas.getCapHeight(parentStyle) * factor;
					break;
				}
			}
			if (force) {
				sz = getInitialFontSize() * factor;
			} else {
				return cssSize;
			}
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
			if (force) {
				sz = getParentElementFontSize() * 0.25f * factor;
			} else {
				return cssSize;
			}
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
			if (force) {
				sz = getParentElementFontSize() * factor;
			} else {
				return cssSize;
			}
			break;
		case CSSPrimitiveValue.CSS_IDENT:
			String sizeIdentifier = cssSize.getStringValue();
			// relative size: larger, smaller.
			String familyName = getUsedFontFamily();
			if ("larger".equalsIgnoreCase(sizeIdentifier)) {
				cssSize = getLargerFontSize(familyName);
			} else if ("smaller".equalsIgnoreCase(sizeIdentifier)) {
				cssSize = getSmallerFontSize(familyName);
			}
			return cssSize;
		case CSSPrimitiveValue.CSS_PERCENTAGE:
			float pcnt = cssSize.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
			// Use parent element's size.
			return getRelativeFontSize(cssSize, pcnt * 0.01f, force);
		case CSSPrimitiveValue2.CSS_VW:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VW);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			try {
				sz = getInitialContainingBlockWidthPt(canvas, force) * factor * 0.01f;
			} catch (StyleDatabaseRequiredException e) {
				if (force) {
					throw e;
				}
				return cssSize;
			}
			break;
		case CSSPrimitiveValue2.CSS_VH:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VH);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			try {
				sz = getInitialContainingBlockHeightPt(canvas, force) * factor * 0.01f;
			} catch (StyleDatabaseRequiredException e) {
				if (force) {
					throw e;
				}
				return cssSize;
			}
			break;
		case CSSPrimitiveValue2.CSS_VI:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VI);
			String writingMode = getCSSValue("writing-mode").getCssText();
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			try {
				if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
					sz = getInitialContainingBlockWidthPt(canvas, force);
				} else {
					sz = getInitialContainingBlockHeightPt(canvas, force);
				}
			} catch (StyleDatabaseRequiredException e) {
				if (force) {
					throw e;
				}
				return cssSize;
			}
			sz *= factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VB:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VB);
			writingMode = getCSSValue("writing-mode").getCssText();
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			try {
				if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
					sz = getInitialContainingBlockHeightPt(canvas, force);
				} else {
					sz = getInitialContainingBlockWidthPt(canvas, force);
				}
			} catch (StyleDatabaseRequiredException e) {
				if (force) {
					throw e;
				}
				return cssSize;
			}
			sz *= factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VMIN:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VMIN);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			try {
				sz = Math.min(getInitialContainingBlockWidthPt(canvas, force),
						getInitialContainingBlockHeightPt(canvas, force));
			} catch (StyleDatabaseRequiredException e) {
				if (force) {
					throw e;
				}
				return cssSize;
			}
			sz *= factor * 0.01f;
			break;
		case CSSPrimitiveValue2.CSS_VMAX:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VMAX);
			canvas = ((CSSDocument) getOwnerNode().getOwnerDocument()).getCanvas();
			try {
				sz = Math.max(getInitialContainingBlockWidthPt(canvas, force),
						getInitialContainingBlockHeightPt(canvas, force));
			} catch (StyleDatabaseRequiredException e) {
				if (force) {
					throw e;
				}
				return cssSize;
			}
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
			AbstractCSSValue custom = evaluateFontCustomProperty((CustomPropertyValue) cssSize);
			if (custom != null && custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				cssSize = (AbstractCSSPrimitiveValue) custom;
			} else {
				cssSize = null;
			}
			return cssSize;
		default:
			try {
				cssSize.getFloatValue(CSSPrimitiveValue.CSS_PT);
			} catch (DOMException e) {
				reportFontSizeError(cssSize, "Error converting to points");
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

	/**
	 * Get a <code>font-size</code> value based on multiplying the parent font size
	 * by a factor.
	 * 
	 * @param cssSize the specified value that is relative to the inherited
	 *                font-size.
	 * @param factor  the factor to multiply by.
	 * @param force   if <code>true</code>, appropriate defaults will be used if an
	 *                absolute identifier like <code>small</code> is found while
	 *                inheriting.
	 * @return the new <code>font-size</code> value, or the specified one if
	 *         <code>force</code> was <code>false</code> and an absolute identifier
	 *         was found while inheriting.
	 */
	private AbstractCSSPrimitiveValue getRelativeFontSize(AbstractCSSPrimitiveValue cssSize, float factor,
			boolean force) {
		AbstractCSSPrimitiveValue value;
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			if (force) {
				float sz = parentCss.getComputedFontSize() * factor;
				sz = Math.round(sz * 100f) * 0.01f;
				value = asNumericValuePt(sz);
			} else {
				value = parentCss.getFontSizeValue();
				if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
					value = cssSize;
				} else {
					try {
						float sz = value.getFloatValue(CSSPrimitiveValue.CSS_PT) * factor;
						sz = Math.round(sz * 100f) * 0.01f;
						value = asNumericValuePt(sz);
					} catch (DOMException e) {
						value = cssSize;
					}
				}
			}
		} else if (force) {
			float sz = getInitialFontSize() * factor;
			value = asNumericValuePt(sz);
		} else {
			value = cssSize;
		}
		return value;
	}

	private NumberValue asNumericValuePt(float f) {
		NumberValue number = new NumberValue();
		number.setFloatValuePt(f);
		return number;
	}

	private float getInitialFontSize() {
		String familyName = getUsedFontFamily();
		return getFontSizeFromIdentifier(familyName, "medium");
	}

	private AbstractCSSValue evaluateFontCustomProperty(CustomPropertyValue cssSize) {
		String propertyName = getCanonicalPropertyName(cssSize.getStringValue());
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<String>();
		} else if (customPropertyStack.contains(propertyName)) {
			AbstractCSSValue custom = cssSize.getFallback();
			if (custom != null) {
				return absoluteFontSizeValue(custom, true);
			} else {
				reportFontSizeError(cssSize, "Dependency loop in " + propertyName);
				return null;
			}
		}
		customPropertyStack.add(propertyName);
		AbstractCSSValue custom;
		try {
			custom = getCSSValue(propertyName);
			if (custom == null) {
				custom = cssSize.getFallback();
				if (custom != null) {
					custom = absoluteFontSizeValue(custom, true);
				} else {
					custom = null;
				}
			} else {
				custom = absoluteFontSizeValue(custom, true);
			}
		} catch (Exception e) {
			customPropertyStack.clear();
			throw e;
		}
		customPropertyStack.remove(propertyName);
		return custom;
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
		if (!"none".equalsIgnoreCase(strVal)) {
			String position = ((CSSPrimitiveValue) getCSSValue("position")).getStringValue();
			if ("absolute".equalsIgnoreCase(position) || "fixed".equalsIgnoreCase(position)) {
				computedValue = computeConstrainedDisplay(value);
			} else {
				String floatProp = ((CSSPrimitiveValue) getCSSValue("float")).getStringValue();
				Node node;
				/*
				 * If float is not 'none' or the owner node is the root element (here checked as
				 * "parent node is document"), then constrain 'display'
				 */
				if (!"none".equalsIgnoreCase(floatProp)
						|| ((node = getOwnerNode()).getParentNode() == node.getOwnerDocument())) {
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
		String display = ((CSSPrimitiveValue) value).getStringValue().toLowerCase(Locale.ROOT);
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
	 * <p>
	 * If no font family is set, a default value of <code>Serif</code> is used.
	 * 
	 * @return the value of the font-family property.
	 */
	@Override
	public String getUsedFontFamily() {
		StyleDatabase sdb = getStyleDatabase();
		if (sdb != null) {
			return sdb.getUsedFontFamily(this);
		} else {
			AbstractCSSValue fontFamily = getCSSValue("font-family");
			if (fontFamily != null) {
				if (fontFamily.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					CSSPrimitiveValue primi = (CSSPrimitiveValue) fontFamily;
					try {
						return primi.getStringValue();
					} catch (DOMException e) {
					}
				} else {
					ValueList list = (ValueList) fontFamily;
					fontFamily = list.item(0);
					if (fontFamily.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
						CSSPrimitiveValue primi = (CSSPrimitiveValue) fontFamily;
						try {
							return primi.getStringValue();
						} catch (DOMException e) {
						}
					}
				}
			}
		}
		return "Serif";
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
		AbstractCSSValue value = super.getCSSValue("font-size");
		// Check for unset
		if (value != null && isCSSIdentifier(value, "unset")) {
			/*
			 * The 'unset' keyword acts as either inherit or initial, depending on whether the
			 * property is inherited or not.
			 */
			value = null;
		}
		/*
		 * We compute inherited value, if appropriate.
		 */
		value = inheritValue(this, "font-size", value, true);
		// Still inheriting ?
		if (value != null && value.getCssValueType() == CSSValue.CSS_INHERIT) {
			value = null;
		}
		CSSPrimitiveValue cssSize;
		// Check for null, and apply initial values if appropriate
		if (value == null || isCSSIdentifier(value, "initial")) {
			return getInitialFontSize();
		} else {
			cssSize = absoluteFontSizeValue(value, true);
		}
		float sz;
		switch (cssSize.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_IDENT:
			String sizeIdentifier = cssSize.getStringValue().toLowerCase(Locale.ROOT);
			try {
				String familyName = getUsedFontFamily();
				sz = getFontSizeFromIdentifier(familyName, sizeIdentifier);
			} catch (DOMException e) {
				computedStyleError("font-size", sizeIdentifier, "Unknown identifier");
				sz = getInitialFontSize();
			}
			break;
		default:
			try {
				sz = cssSize.getFloatValue(CSSPrimitiveValue.CSS_PT);
			} catch (DOMException e) {
				reportFontSizeError(cssSize, e.getMessage());
				sz = getInitialFontSize();
			}
		}
		return sz;
	}

	private void computedStyleError(String propertyName, String propertyValue, String message) {
		CSSPropertyValueException ex = new CSSPropertyValueException(message);
		ex.setValueText(propertyValue);
		((CSSDocument) getOwnerNode().getOwnerDocument()).getErrorHandler()
				.computedStyleError((CSSElement) getOwnerNode(), propertyName, ex);
	}

	private void reportFontSizeError(CSSValue cssSize, String message) {
		String cssText = cssSize.getCssText();
		computedStyleError("font-size", cssText, message);
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
				sz = 18f;
			} else if (sizeIdentifier.equals("xx-large")) {
				sz = 24f;
			} else {
				sz = 12f; // default
			}
			return sz;
		}
	}

	private AbstractCSSPrimitiveValue getLargerFontSize(String familyName) {
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			CSSPrimitiveValue csssize = (CSSPrimitiveValue) parentCss.getCSSValue("font-size");
			if (csssize != null) {
				String larger;
				switch (csssize.getPrimitiveType()) {
				case CSSPrimitiveValue.CSS_IDENT:
					String baseFontSize = csssize.getStringValue();
					if (baseFontSize.equals("xx-small")) {
						larger = "x-small";
					} else if (baseFontSize.equals("x-small")) {
						larger = "small";
					} else if (baseFontSize.equals("small")) {
						larger = "medium";
					} else if (baseFontSize.equals("medium")) {
						larger = "large";
					} else if (baseFontSize.equals("large")) {
						larger = "x-large";
					} else if (baseFontSize.equals("x-large")) {
						larger = "xx-large";
					} else if (baseFontSize.equals("xx-large")) {
						return asNumericValuePt(2f * getFontSizeFromIdentifier(familyName, "xx-large")
								- getFontSizeFromIdentifier(familyName, "x-large"));
					} else {
						computedStyleError("font-size", baseFontSize, "Unknown identifier");
						return asNumericValuePt(getFontSizeFromIdentifier(familyName, "medium") * 1.2f);
					}
					break;
				default:
					return asNumericValuePt(parentCss.getComputedFontSize() * 1.2f);
				}
				return new IdentifierValue(larger);
			}
		}
		return asNumericValuePt(getFontSizeFromIdentifier(familyName, "medium") * 1.2f);
	}

	private AbstractCSSPrimitiveValue getSmallerFontSize(String familyName) {
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			CSSPrimitiveValue csssize = (CSSPrimitiveValue) parentCss.getCSSValue("font-size");
			if (csssize != null) {
				String smaller;
				switch (csssize.getPrimitiveType()) {
				case CSSPrimitiveValue.CSS_IDENT:
					String baseFontSize = csssize.getStringValue();
					if (baseFontSize.equals("xx-small")) {
						float sz = 2f * getFontSizeFromIdentifier(familyName, "xx-small")
								- getFontSizeFromIdentifier(familyName, "x-small");
						// Safety check
						if (sz < 7f) {
							sz = getFontSizeFromIdentifier(familyName, "xx-small");
						}
						return asNumericValuePt(sz);
					} else if (baseFontSize.equals("x-small")) {
						smaller = "xx-small";
					} else if (baseFontSize.equals("small")) {
						smaller = "x-small";
					} else if (baseFontSize.equals("medium")) {
						smaller = "small";
					} else if (baseFontSize.equals("large")) {
						smaller = "medium";
					} else if (baseFontSize.equals("x-large")) {
						smaller = "large";
					} else if (baseFontSize.equals("xx-large")) {
						smaller = "x-large";
					} else {
						computedStyleError("font-size", baseFontSize, "Unknown identifier");
						return asNumericValuePt(getFontSizeFromIdentifier(familyName, "medium") * 0.82f);
					}
					break;
				default:
					return asNumericValuePt(parentCss.getComputedFontSize() * 0.82f);
				}
				return new IdentifierValue(smaller);
			}
		}
		return asNumericValuePt(getFontSizeFromIdentifier(familyName, "medium") * 0.82f);
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
			if (!"normal".equalsIgnoreCase(cssval.getStringValue())) {
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
