/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Locale;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.Viewport;
import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.AttrValue;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.CustomPropertyValue;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.ExpressionValue;
import io.sf.carte.doc.style.css.property.FunctionValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.LinkedCSSValueList;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.doc.style.css.property.StringValue;
import io.sf.carte.doc.style.css.property.StyleValue;
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
 * 
 */
abstract public class ComputedCSSStyle extends BaseCSSStyleDeclaration implements CSSComputedProperties {

	private CSSElement node = null;

	/*
	 * Account custom property and attribute names, to prevent circular dependencies.
	 */
	private transient LinkedList<String> customPropertyStack = null;
	private transient LinkedList<String> attrValueStack = null;

	protected ComputedCSSStyle() {
		super();
	}

	protected ComputedCSSStyle(ComputedCSSStyle copiedObject) {
		super(copiedObject);
		setOwnerNode(copiedObject.getOwnerNode());
	}

	protected void setOwnerNode(CSSElement node) {
		this.node = node;
	}

	@Override
	public CSSElement getOwnerNode() {
		return node;
	}

	@Override
	public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
			return node.getOwnerDocument().getErrorHandler()
					.getInlineStyleErrorHandler(node);
		}
		return null;
	}

	@Override
	String getUnknownPropertyPriority(String propertyName) {
		return checkShorthandPriority(propertyName);
	}

	@Override
	public String getPropertyValue(String propertyName) {
		propertyName = getCanonicalPropertyName(propertyName);
		CSSValue value = getCSSValue(propertyName);
		if (value != null) {
			short type = value.getCssValueType();
			if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
				short ptype;
				if ((ptype = ((CSSPrimitiveValue) value).getPrimitiveType()) == CSSPrimitiveValue.CSS_STRING
						|| ptype == CSSPrimitiveValue.CSS_IDENT) {
					return ((CSSPrimitiveValue) value).getStringValue();
				}
			}
			return value.getCssText();
		} else if (ShorthandDatabase.getInstance().isShorthand(propertyName)) {
			return serializeShorthand(propertyName);
		}
		return "";
	}

	private String serializeShorthand(String propertyName) {
		ShorthandBuilder builder = createBuilder(propertyName);
		if (builder != null) {
			String[] longhands = builder.getLonghandProperties();
			for (String longhand : longhands) {
				builder.addAssignedProperty(longhand, false);
			}
			if ("font".equals(propertyName) || "font-variant".equals(propertyName)) {
				builder.addAssignedProperty("font-variant-caps", false);
				builder.addAssignedProperty("font-variant-ligatures", false);
				builder.addAssignedProperty("font-variant-position", false);
				builder.addAssignedProperty("font-variant-numeric", false);
				builder.addAssignedProperty("font-variant-alternates", false);
				builder.addAssignedProperty("font-variant-east-asian", false);
			}
			StringBuilder buf = new StringBuilder(64);
			builder.appendMinifiedCssText(buf);
			String declaration = buf.toString();
			/* 
			 * Reparse and check for errors and number of properties serialized.
			 * Shorthand builders often use multiple declarations for more
			 * efficiency in the full style serialization. Skip those cases.
			 */
			InputSource source = new InputSource(new StringReader(declaration));
			CSSParser parser = new CSSParser();
			PropertyCounterDocumentHandler handler = new PropertyCounterDocumentHandler();
			parser.setDocumentHandler(handler);
			parser.setErrorHandler(handler);
			try {
				parser.parseStyleDeclaration(source);
			} catch (CSSException | IOException e) {
				return "";
			}
			if (!handler.hasError() && handler.getPropertyCount() == 1) {
				int len = declaration.length();
				int lenm1 = len - 1;
				int firstc = declaration.indexOf(':');
				if (declaration.charAt(lenm1) == ';') {
					len = lenm1;
				}
				return declaration.substring(firstc + 1, len);
			}
		}
		return "";
	}

	@Override
	protected void setPropertyCSSValue(String propertyName, StyleValue value, String hrefcontext) {
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
	public StyleValue getCSSValue(String property) throws StyleDatabaseRequiredException {
		StyleValue value = super.getCSSValue(property);
		// Is the property inherited ?
		PropertyDatabase propertydb = PropertyDatabase.getInstance();
		boolean inherited = propertydb.isInherited(property) || property.startsWith("--");
		// Check for unset
		if (value != null) {
			if (value.getCssValueType() == CSSValue.CSS_CUSTOM) {
				return null;
			}
			if (isCSSIdentifier(value, "unset")) {
				/*
				 * The 'unset' keyword acts as either inherit or initial, depending on whether
				 * the property is inherited or not.
				 */
				value = null;
			}
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

	private static StyleValue inheritValue(ComputedCSSStyle ancStyle, String propertyName, StyleValue value,
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

	private static boolean isCSSIdentifier(CSSValue value, String ident) {
		CSSPrimitiveValue primi;
		return value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& (primi = (CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& ident.equalsIgnoreCase(primi.getStringValue());
	}

	private StyleValue computeValue(String property, StyleValue value, boolean inherited,
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
			// Convert to absolute units
			if (property.equals("font-size")) {
				value = absoluteFontSizeValue(value, false);
			} else {
				try {
					value = absoluteValue(property, value, false);
				} catch (DOMException e) {
					computedStyleError(property, value.getCssText(), null, e);
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
					value = colorValue(property, (PrimitiveValue) value);
				}
			}
		}
		return value;
	}

	private StyleValue absoluteValue(String property, StyleValue value, boolean useParentStyle) {
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			int lstlen = list.getLength();
			for (int i = 0; i < lstlen; i++) {
				list.set(i, absoluteValue(property, list.item(i), useParentStyle));
			}
		} else if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			PrimitiveValue primi = (PrimitiveValue) value;
			// Check for custom properties ('variables')
			if (primi.getPrimitiveType() == CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY) {
				value = evaluateCustomProperty(property, (CustomPropertyValue) primi, useParentStyle);
			} else {
				value = absolutePrimitiveValue(property, primi, useParentStyle);
			}
		}
		return value;
	}

	PrimitiveValue absolutePrimitiveValue(String propertyName, PrimitiveValue pri, boolean useParentStyle) {
		if (isRelativeUnit(pri)) {
			try {
				pri = absoluteNumberValue((NumberValue) pri, useParentStyle);
			} catch (DOMException | IllegalStateException e) {
			}
		} else {
			short type = pri.getPrimitiveType();
			if (type == CSSPrimitiveValue2.CSS_EXPRESSION) {
				pri = pri.clone();
				ExpressionValue exprval = (ExpressionValue) pri;
				Evaluator ev = new MyEvaluator(propertyName);
				try {
					pri = (PrimitiveValue) ev.evaluateExpression(exprval);
				} catch (DOMException e) {
					// Evaluation failed, convert expressions to absolute anyway.
					absoluteExpressionValue(propertyName, exprval.getExpression(), useParentStyle);
				}
			} else if (type == CSSPrimitiveValue2.CSS_FUNCTION) {
				FunctionValue function = (FunctionValue) pri;
				function = function.clone();
				Evaluator ev = new MyEvaluator(propertyName);
				try {
					pri = (PrimitiveValue) ev.evaluateFunction(function);
				} catch (DOMException e) {
					// Evaluation failed, convert arguments to absolute anyway.
					LinkedCSSValueList args = function.getArguments();
					int sz = args.size();
					for (int i = 0; i < sz; i++) {
						args.set(i, absoluteValue(propertyName, args.get(i), useParentStyle));
					}
				}
			} else if (type == CSSPrimitiveValue.CSS_ATTR) {
				pri = computeAttribute(propertyName, (AttrValue) pri, useParentStyle);
			// Check for custom properties ('variables')
			} else if (type == CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY) {
				StyleValue custom = evaluateCustomProperty(propertyName, (CustomPropertyValue) pri, useParentStyle);
				if (custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					pri = (PrimitiveValue) custom;
				}
			}
		}
		return pri;
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
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
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
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				fv *= root.getComputedStyle(null).getComputedLineHeight();
			} else {
				fv *= getInitialFontSize();
			}
		} else {
			CSSCanvas canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
		value = new NumberValue();
		value.setFloatValuePt(fv);
		value.setAbsolutizedUnit();
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
		if (force && (medium = getOwnerNode().getOwnerDocument().getTargetMedium()) != null) {
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
		if (force && (medium = getOwnerNode().getOwnerDocument().getTargetMedium()) != null) {
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

	private void absoluteExpressionValue(String propertyName, CSSExpression expr, boolean useParentStyle) {
		switch (expr.getPartType()) {
		case SUM:
		case PRODUCT:
			AlgebraicExpression ae = (AlgebraicExpression) expr;
			int len = ae.getLength();
			for (int i = 0; i < len; i++) {
				CSSExpression op = ae.item(i);
				absoluteExpressionValue(propertyName, op, useParentStyle);
			}
			break;
		case OPERAND:
			CSSOperandExpression operand = (CSSOperandExpression) expr;
			PrimitiveValue primi = (PrimitiveValue) operand.getOperand();
			operand.setOperand(absolutePrimitiveValue(propertyName, primi, useParentStyle));
		}
	}

	private PrimitiveValue computeAttribute(String propertyName, AttrValue attr, boolean useParentStyle) throws DOMException {
		String attrname = attr.getAttributeName();
		CSSElement owner = getOwnerNode();
		String attrvalue = owner.getAttribute(attrname);
		String attrtype = attr.getAttributeType();
		if (attrvalue.length() != 0) {
			if (isSafeAttrValue(propertyName, owner, attrname)) {
				if (attrtype == null || "string".equalsIgnoreCase(attrtype)) {
					// Do not reparse
					StringValue value = new StringValue(AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE);
					value.setStringValue(CSSPrimitiveValue.CSS_STRING, attrvalue);
					return value;
				}
				attrvalue = attrvalue.trim();
				if ("url".equalsIgnoreCase(attrtype)) {
					try {
						URL url = getOwnerNode().getOwnerDocument().getURL(attrvalue);
						URIValue uri = new URIValue(AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE);
						uri.setStringValue(CSSPrimitiveValue.CSS_URI, url.toExternalForm());
						return uri;
					} catch (MalformedURLException e) {
					}
				} else {
					ValueFactory factory = new ValueFactory();
					StyleValue value;
					try {
						value = factory.parseProperty(attrvalue);
					} catch (DOMException e) {
						DOMException ex = new DOMException(e.code,
								"Error parsing attribute '" + attrname + "', value: " + attrvalue);
						ex.initCause(e);
						throw ex;
					}
					if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
						PrimitiveValue pri;
						// Prevent circular dependencies.
						addAttrNameGuard(attrname);
						try {
							pri = absolutePrimitiveValue(propertyName, (PrimitiveValue) value, useParentStyle);
						} catch (Exception e) {
							clearAttrGuardStack();
							throw e;
						}
						removeAttrNameGuard(attrname);
						PrimitiveValue val = attrValueOfType(pri, attrtype);
						if (val != null) {
							val = absolutePrimitiveValue(propertyName, val, useParentStyle);
							return val;
						}
						computedStyleWarning(propertyName, attr,
								"Attribute value does not match type (" + attrtype + ").");
					} else {
						computedStyleWarning(propertyName, attr, "Invalid attribute value");
					}
				}
			} else {
				computedStyleWarning(propertyName, attr, "Unsafe attribute value");
			}
		}
		// Fallback
		StyleValue fallback = attr.getFallback();
		if (fallback == null) {
			if (attrValueStack != null && !attrValueStack.isEmpty()) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"No explicit fallback and we are in recursive attr(), forbidden by CSS.");
			}
			PrimitiveValue defval = AttrValue.defaultFallback(attrtype);
			if (defval == null) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Invalid attribute, no default fallback for type " + attrtype + '.');
			}
			if ("color".equalsIgnoreCase(attrtype)) {
				defval = colorValue("", defval);
			}
			return defval;
		}
		// Prevent circular dependencies.
		addAttrNameGuard(attrname);
		StyleValue value;
		try {
			/*
			 * We compute inherited value, if appropriate.
			 */
			fallback = inheritValue(ComputedCSSStyle.this, propertyName, fallback, false);
			// Still inheriting ?
			if (fallback == null || fallback.getCssValueType() == CSSValue.CSS_INHERIT) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Invalid fallback.");
			}
			value = absoluteValue(propertyName, fallback, useParentStyle);
		} catch (Exception e) {
			clearAttrGuardStack();
			throw e;
		}
		removeAttrNameGuard(attrname);
		if (value == null || value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Invalid fallback.");
		}
		PrimitiveValue pri = (PrimitiveValue) value;
		if (pri.getPrimitiveType() == CSSPrimitiveValue.CSS_STRING
				&& "url".equalsIgnoreCase(attrtype)) {
			try {
				URL url = getOwnerNode().getOwnerDocument().getURL(pri.getStringValue());
				URIValue uri = new URIValue(AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE);
				uri.setStringValue(CSSPrimitiveValue.CSS_URI, url.toExternalForm());
				return uri;
			} catch (MalformedURLException e) {
			}
		} else if ("color".equalsIgnoreCase(attrtype)) {
			pri = colorValue("", pri);
		}
		return pri;
	}

	/**
	 * Is the given {@code attr()} safe to use?
	 * <p>
	 * If {@code attr()} is used outside of the {@code content} property, then the
	 * {@code value} attribute in form fields, as well as any attribute containing
	 * certain strings are considered unsafe.
	 * </p>
	 * 
	 * @param propertyName the name of the property where the {@code attr()} is to
	 *                     be used.
	 * @param element      the element where the attribute resides.
	 * @param attrname     the name of the attribute.
	 * @return {@code true} if the value is considered safe.
	 */
	private boolean isSafeAttrValue(String propertyName, CSSElement element, String attrname) {
		String tagname;
		if (!"content".equals(propertyName)) {
			if (attrname.contains("nonce") || attrname.contains("pass") || attrname.contains("pwd")
					|| attrname.contains("user") || attrname.contains("uid") || attrname.contains("session")
					|| attrname.contains("secret")
					|| ("input".equalsIgnoreCase(tagname = element.getTagName()) && attrname.equalsIgnoreCase("value"))
					|| "meta".equals(tagname) || "link".equals(tagname)) {
				return false;
			}
		}
		return true;
	}

	private void addAttrNameGuard(String attrname) {
		if (attrValueStack == null) {
			attrValueStack = new LinkedList<String>();
		} else if (attrValueStack.contains(attrname)) {
			attrValueStack.clear();
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"Recursive use of attribute " + attrname + " in computed style.");
		}
		attrValueStack.add(attrname);
	}

	private void removeAttrNameGuard(String attrname) {
		attrValueStack.remove(attrname);
	}

	private void clearAttrGuardStack() {
		attrValueStack.clear();
	}

	private PrimitiveValue attrValueOfType(PrimitiveValue value, String type) throws DOMException {
		if ("color".equalsIgnoreCase(type)) {
			value = colorValue("", value);
			if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_RGBCOLOR) {
				return value;
			}
		} else {
			short ptype = value.getPrimitiveType();
			if ("number".equalsIgnoreCase(type)) {
				if (ptype == CSSPrimitiveValue.CSS_NUMBER) {
					return value;
				}
			} else if ("integer".equalsIgnoreCase(type)) {
				float fval;
				if (ptype == CSSPrimitiveValue.CSS_NUMBER
						&& Math.abs((fval = value.getFloatValue(ptype)) - Math.round(fval)) < 7e-6) {
					return value;
				}
			} else if ("%".equals(type)) {
				if (ptype == CSSPrimitiveValue.CSS_NUMBER) {
					float fval = value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
					value.setFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, fval);
					return value;
				} else if (ptype == CSSPrimitiveValue.CSS_PERCENTAGE) {
					return value;
				}
			} else if ("length".equalsIgnoreCase(type)) {
				if (NumberValue.isLengthUnitType(ptype)) {
					return value;
				}
			} else if ("angle".equalsIgnoreCase(type)) {
				if (NumberValue.isAngleUnitType(ptype)) {
					return value;
				}
			} else if ("time".equalsIgnoreCase(type)) {
				if (ptype == CSSPrimitiveValue.CSS_S || ptype == CSSPrimitiveValue.CSS_MS) {
					return value;
				}
			} else if ("frequency".equalsIgnoreCase(type)) {
				if (ptype == CSSPrimitiveValue.CSS_HZ || ptype == CSSPrimitiveValue.CSS_KHZ) {
					return value;
				}
			} else if (ptype == CSSPrimitiveValue.CSS_NUMBER) {
				String lctypeval = type.toLowerCase(Locale.ROOT).intern();
				short sacUnit = ParseHelper.unitFromString(lctypeval);
				if (sacUnit != LexicalUnit.SAC_DIMENSION) {
					short expectedType = ValueFactory.domPrimitiveType(sacUnit);
					if (expectedType != CSSPrimitiveValue.CSS_UNKNOWN) {
						if (NumberValue.isLengthUnitType(expectedType)) {
							return NumberValue.createCSSNumberValue(expectedType, value.getFloatValue(ptype));
						} else if (NumberValue.isAngleUnitType(expectedType)) {
							return NumberValue.createCSSNumberValue(expectedType, value.getFloatValue(ptype));
						} else if (expectedType == CSSPrimitiveValue.CSS_S
								|| expectedType == CSSPrimitiveValue.CSS_MS) {
							return NumberValue.createCSSNumberValue(expectedType, value.getFloatValue(ptype));
						} else if (expectedType == CSSPrimitiveValue.CSS_HZ
								|| expectedType == CSSPrimitiveValue.CSS_KHZ) {
							return NumberValue.createCSSNumberValue(expectedType, value.getFloatValue(ptype));
						}
						return null;
					}
					return value;
				}
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Unknown attribute type '" + type + "' found in computed style.");
			} else {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Invalid attr() value found in computed style.");
			}
		}
		return null;
	}

	private StyleValue evaluateCustomProperty(String property, CustomPropertyValue value, boolean useParentStyle) {
		String propertyName = getCanonicalPropertyName(value.getStringValue());
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<String>();
		} else if (customPropertyStack.contains(propertyName)) {
			StyleValue custom = value.getFallback();
			if (custom != null) {
				// Fallback is already checked for expecting integer.
				return absoluteValue(property, custom, useParentStyle);
			} else {
				customPropertyStack.clear();
				throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Dependency loop in " + propertyName);
			}
		}
		customPropertyStack.add(propertyName);
		StyleValue custom;
		try {
			custom = getCSSValue(propertyName);
			if (custom == null) {
				custom = value.getFallback();
				if (custom != null) {
					custom = absoluteValue(property, custom, useParentStyle);
				} else {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Unable to evaluate custom property " + propertyName);
				}
			} else {
				/*
				 * Tentative protection for Billion Laugh Attacks
				 */
				StyleValue item;
				int stackSize;
				if (custom.getCssValueType() == CSSValue.CSS_VALUE_LIST
						&& (stackSize = customPropertyStack.size()) > 5
						&& (item = ((ValueList) custom).item(0)).getCssValueType() == CSSValue.CSS_VALUE_LIST
						&& (item = ((ValueList) item).item(0)).getCssValueType() == CSSValue.CSS_VALUE_LIST
						&& ((ValueList) item).item(0).getCssValueType() == CSSValue.CSS_VALUE_LIST
						&& ((ValueList) custom).getLength() * stackSize > 32) {
					// Probable DoS attack
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Resource limit hit while replacing custom property " + propertyName);
				}
				custom = absoluteValue(property, custom, useParentStyle);
			}
		} catch (Exception e) {
			customPropertyStack.clear();
			throw e;
		}
		customPropertyStack.remove(propertyName);
		if (value.isExpectingInteger() && custom != null && custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			((PrimitiveValue) custom).setExpectInteger();
		} // 'custom' could be <inherit> or a list
		return custom;
	}

	private PrimitiveValue getFontSizeValue() {
		StyleValue value = super.getCSSValue("font-size");
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

	private PrimitiveValue absoluteFontSizeValue(StyleValue value, boolean force) {
		PrimitiveValue primi;
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			primi = absoluteFontSizePrimitive((PrimitiveValue) value, force);
			if (primi == null) {
				ComputedCSSStyle ancStyle = this;
				do {
					StyleValue inheritedValue = inheritValue(ancStyle, "font-size", primi, true);
					if (inheritedValue != null) {
						primi = absoluteFontSizeValue(inheritedValue, force);
						if (primi != null) {
							break;
						}
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

	private PrimitiveValue absoluteFontSizePrimitive(PrimitiveValue cssSize, boolean force) {
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
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
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
			root = getOwnerNode().getOwnerDocument().getDocumentElement();
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
			CSSCanvas canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			return getRelativeFontSize(cssSize, pcnt * 0.01f, true);
		case CSSPrimitiveValue2.CSS_VW:
			factor = cssSize.getFloatValue(CSSPrimitiveValue2.CSS_VW);
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
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
			ExpressionValue exprval = (ExpressionValue) cssSize;
			absoluteExpressionValue("font-size", exprval.getExpression(), true);
			Evaluator ev = new FontEvaluator();
			try {
				cssSize = (PrimitiveValue) ev.evaluateExpression(exprval);
			} catch (DOMException e) {
			}
			return cssSize;
		case CSSPrimitiveValue2.CSS_FUNCTION:
			FunctionValue function = (FunctionValue) cssSize;
			function = function.clone();
			LinkedCSSValueList args = function.getArguments();
			int siz = args.size();
			for (int i = 0; i < siz; i++) {
				args.set(i, absoluteFontSizeValue(args.get(i), true));
			}
			ev = new FontEvaluator();
			try {
				cssSize = (PrimitiveValue) ev.evaluateFunction(function);
			} catch (DOMException e) {
			}
			return cssSize;
		// Check for custom properties ('variables')
		case CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY:
			StyleValue custom = evaluateFontCustomProperty((CustomPropertyValue) cssSize);
			if (custom != null && custom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				cssSize = (PrimitiveValue) custom;
			} else {
				cssSize = null;
			}
			return cssSize;
		default:
			try {
				cssSize.getFloatValue(CSSPrimitiveValue.CSS_PT);
			} catch (DOMException e) {
				String cssText = cssSize.getCssText();
				computedStyleError("font-size", cssText, "Error converting to points.", e);
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

	private StyleValue evaluateFontCustomProperty(CustomPropertyValue cssSize) {
		String propertyName = getCanonicalPropertyName(cssSize.getStringValue());
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<String>();
		} else if (customPropertyStack.contains(propertyName)) {
			StyleValue custom = cssSize.getFallback();
			if (custom != null) {
				return absoluteFontSizeValue(custom, true);
			} else {
				reportFontSizeError(cssSize, "Dependency loop in " + propertyName);
				return null;
			}
		}
		customPropertyStack.add(propertyName);
		StyleValue custom;
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
	private PrimitiveValue getRelativeFontSize(PrimitiveValue cssSize, float factor,
			boolean force) {
		PrimitiveValue value;
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			if (force) {
				float sz = parentCss.getComputedFontSize() * factor;
				sz = Math.round(sz * 100f) * 0.01f;
				value = asNumericValuePt(sz);
			} else {
				// Convert to absolute units
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
		} else {
			float sz = getInitialFontSize() * factor;
			value = asNumericValuePt(sz);
		}
		return value;
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
		StyleValue value = super.getCSSValue("font-size");
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
				computedStyleError("font-size", sizeIdentifier, "Unknown identifier", e);
				sz = getInitialFontSize();
			}
			break;
		default:
			try {
				sz = cssSize.getFloatValue(CSSPrimitiveValue.CSS_PT);
			} catch (DOMException e) {
				String cssText = cssSize.getCssText();
				computedStyleError("font-size", cssText, null, e);
				sz = getInitialFontSize();
			}
		}
		return sz;
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

	private PrimitiveValue getLargerFontSize(String familyName) {
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

	private PrimitiveValue getSmallerFontSize(String familyName) {
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

	private void reportFontSizeError(CSSValue cssSize, String message) {
		String cssText = cssSize.getCssText();
		computedStyleError("font-size", cssText, message);
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

	private class MyEvaluator extends Evaluator {

		final String propertyName;

		MyEvaluator(String propertyName) {
			super();
			this.propertyName = propertyName;
		}

		@Override
		protected ExtendedCSSPrimitiveValue absoluteValue(ExtendedCSSPrimitiveValue partialValue) {
			return absolutePrimitiveValue(propertyName, (PrimitiveValue) partialValue, false);
		}

	}

	private class FontEvaluator extends MyEvaluator {

		FontEvaluator() {
			super("font-size");
		}

		@Override
		protected ExtendedCSSPrimitiveValue absoluteValue(ExtendedCSSPrimitiveValue partialValue) {
			return absolutePrimitiveValue(propertyName, (PrimitiveValue) partialValue, true);
		}

		@Override
		protected float percentage(ExtendedCSSPrimitiveValue value, short resultType) throws DOMException {
			float pcnt = value.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
			// Use parent element's size.
			return getParentElementFontSize() * pcnt * 0.01f;
		}

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

	private PrimitiveValue colorValue(String propertyName, PrimitiveValue primi) {
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
						primi = (PrimitiveValue) getValueFactory().parseProperty(spec);
					} catch (DOMException e) {
					}
				}
			}
		}
		return primi;
	}

	private static NumberValue asNumericValuePt(float f) {
		NumberValue number = new NumberValue();
		number.setFloatValuePt(f);
		return number;
	}

	private float getInitialFontSize() {
		String familyName = getUsedFontFamily();
		return getFontSizeFromIdentifier(familyName, "medium");
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

	private StyleValue applyDisplayConstrains(StyleValue value) {
		StyleValue computedValue = value;
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
						|| (node = getOwnerNode()).getParentNode() == node.getOwnerDocument()) {
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
	private StyleValue computeConstrainedDisplay(StyleValue value) {
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

	private StyleValue applyBorderWidthConstrains(String property, StyleValue value) {
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

	private StyleValue computeBackgroundRepeat(StyleValue value) {
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				// It is a list of layer values
				for (int i = 0; i < list.getLength(); i++) {
					StyleValue item = list.item(i);
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

	private StyleValue computeBackgroundRepeatPrimitive(StyleValue value) {
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
	protected PrimitiveValue getCurrentColor() {
		return getCSSColor();
	}

	@Override
	public PrimitiveValue getCSSColor() {
		return (PrimitiveValue) getCSSValue("color");
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
			StyleValue fontFamily = getCSSValue("font-family");
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

	private void computedStyleError(String propertyName, String propertyValue, String message) {
		computedStyleError(propertyName, propertyValue, message, null);
	}

	private void computedStyleError(String propertyName, String propertyValue, String message, Throwable cause) {
		if (message == null) {
			message = cause.getMessage();
		}
		CSSPropertyValueException ex = new CSSPropertyValueException(message, cause);
		ex.setValueText(propertyValue);
		getOwnerNode().getOwnerDocument().getErrorHandler().computedStyleError(getOwnerNode(), propertyName, ex);
	}

	private void computedStyleWarning(String propertyName, PrimitiveValue value, String message) {
		CSSPropertyValueException ex = new CSSPropertyValueException(message);
		ex.setValueText(value.getCssText());
		getOwnerNode().getOwnerDocument().getErrorHandler().computedStyleWarning(getOwnerNode(), propertyName, ex);
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
