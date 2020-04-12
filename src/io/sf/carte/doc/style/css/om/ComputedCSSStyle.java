/*

 Copyright (c) 2005-2020, Carlos Amengual.

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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.agent.Viewport;
import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.AttrValue;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.EnvVariableValue;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.ExpressionValue;
import io.sf.carte.doc.style.css.property.FunctionValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.LinkedCSSValueList;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.PercentageEvaluator;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.doc.style.css.property.StringValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.URIValue;
import io.sf.carte.doc.style.css.property.URIValueWrapper;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.doc.style.css.property.VarValue;
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

	private final BaseDocumentCSSStyleSheet ownerSheet;

	private CSSElement node = null;

	/*
	 * Account custom property and attribute names, to prevent circular dependencies.
	 */
	private transient LinkedList<String> customPropertyStack = null;
	private transient LinkedList<String> attrValueStack = null;

	protected ComputedCSSStyle(BaseDocumentCSSStyleSheet docSheet) {
		super();
		this.ownerSheet = docSheet;
	}

	protected ComputedCSSStyle(ComputedCSSStyle copiedObject) {
		super(copiedObject);
		this.ownerSheet = copiedObject.ownerSheet;
		setOwnerNode(copiedObject.getOwnerNode());
	}

	protected void setOwnerNode(CSSElement node) {
		this.node = node;
	}

	@Override
	public CSSElement getOwnerNode() {
		return node;
	}

	protected BaseDocumentCSSStyleSheet getOwnerSheet() {
		return ownerSheet;
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
			CssType type = value.getCssValueType();
			if (type == CssType.TYPED) {
				CSSTypedValue.Type ptype;
				if ((ptype = value
						.getPrimitiveType()) == Type.STRING
						|| ptype == Type.IDENT) {
					return ((CSSTypedValue) value).getStringValue();
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
			StringReader re = new StringReader(declaration);
			CSSParser parser = new CSSParser();
			PropertyCounterHandler handler = new PropertyCounterHandler();
			parser.setDocumentHandler(handler);
			parser.setErrorHandler(handler);
			try {
				parser.parseStyleDeclaration(re);
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
			if (value.getCssValueType() == CssType.LIST) {
				if (hrefcontext != null) {
					value = ((ValueList) value).wrap(hrefcontext, getOwnerNode().getOwnerDocument().getBaseURI());
				}
			} else if (value.getPrimitiveType() == Type.URI) {
				if (hrefcontext != null) {
					value = new URIValueWrapper((URIValue) value, hrefcontext,
							getOwnerNode().getOwnerDocument().getBaseURI());
				}
			}
		}
		super.setPropertyCSSValue(propertyName, value, hrefcontext);
	}

	public StyleValue getCascadedValue(String property) throws StyleDatabaseRequiredException {
		// Is the property inherited ?
		PropertyDatabase propertydb = PropertyDatabase.getInstance();
		boolean inherited = propertydb.isInherited(property) || property.startsWith("--");
		StyleValue value = super.getCSSValue(property);
		if (value != null) {
			if (value.getPrimitiveType() == CSSValue.Type.INTERNAL) {
				// Pending substitution values.
				PendingValue pending = (PendingValue) value;
				value = getSubstitutedValue(property, pending.getShorthandName(), pending.getLexicalUnit().clone(),
						isPropertyImportant(property));
			} else if (value.getPrimitiveType() == CSSValue.Type.UNSET) {
				/*
				 * The 'unset' keyword acts as either inherit or initial, depending on whether
				 * the property is inherited or not.
				 */
				value = null;
			} else if (value.getCssValueType() == CssType.SHORTHAND) {
				return null;
			} else {
				try {
					value = absoluteValue(property, value, false);
				} catch (DOMException e) {
					computedStyleError(property, value.getCssText(), null, e);
					value = null;
				}
			}
		}
		if (value == null) {
			if (inherited) {
				value = InheritValue.getValue();
			} else {
				value = defaultPropertyValue(property, propertydb);
			}
		}
		return value;
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
		// Is the property inherited ?
		PropertyDatabase propertydb = PropertyDatabase.getInstance();
		boolean inherited = propertydb.isInherited(property) || property.startsWith("--");
		StyleValue value = super.getCSSValue(property);
		// Check for unset
		if (value != null) {
			if (value.getPrimitiveType() == CSSValue.Type.INTERNAL) {
				// Pending substitution values.
				PendingValue pending = (PendingValue) value;
				value = getSubstitutedValue(property, pending.getShorthandName(), pending.getLexicalUnit().clone(),
						isPropertyImportant(property));
			} else if (value.getPrimitiveType() == CSSValue.Type.UNSET) {
				/*
				 * The 'unset' keyword acts as either inherit or initial, depending on whether
				 * the property is inherited or not.
				 */
				value = null;
			} else if (value.getCssValueType() == CssType.SHORTHAND) {
				return null;
			}
		}
		/*
		 * We compute inherited value, if appropriate.
		 */
		value = inheritValue(this, property, value, inherited);
		// Still inheriting ?
		if (value != null && value.getPrimitiveType() == CSSValue.Type.INHERIT) {
			value = null;
		}
		value = computeValue(property, value, inherited, propertydb);
		return value;
	}

	private static StyleValue inheritValue(ComputedCSSStyle ancStyle, String propertyName, StyleValue value,
			boolean inherited) {
		while (value == null ? inherited : value.getPrimitiveType() == CSSValue.Type.INHERIT) {
			ancStyle = ancStyle.getParentComputedStyle();
			if (ancStyle == null) {
				break;
			}
			if (ancStyle.isPropertySet(propertyName)) {
				value = ancStyle.getCSSValue(propertyName);
				if (value != null && isCSSKeyword(CSSValue.Type.UNSET, value)) {
					value = null;
				}
			}
		}
		return value;
	}

	private static boolean isCSSKeyword(CSSValue.Type keyword, CSSValue value) {
		return value.getPrimitiveType() == keyword;
	}

	private StyleValue getSubstitutedValue(String property, String shorthandName, LexicalUnit lunit,
			boolean propertyImportant) {
		try {
			if (substituteShorthand(property, shorthandName, lunit, propertyImportant)) {
				return getCSSValue(property);
			}
		} catch (DOMException e) {
			computedStyleError(property, lunit.toString(), "Problem substituting lexical value in shorthand.", e);
		}
		return null;
	}

	private boolean substituteShorthand(String longhand, String shorthand, LexicalUnit lunit, boolean prioImportant) {
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<String>();
		}
		lunit = replaceLexicalVar(longhand, lunit, new CSSOMParser());
		return setShorthandLonghands(shorthand, lunit, prioImportant, null);
	}

	private StyleValue computeValue(String property, StyleValue value, boolean inherited,
			PropertyDatabase propertydb) {
		// Check for null, and apply initial values if appropriate
		if (value == null || isCSSKeyword(CSSValue.Type.INITIAL, value)
				|| (!inherited && isCSSKeyword(CSSValue.Type.UNSET, value))) {
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
					if (value == null || value.getPrimitiveType() == CSSValue.Type.INHERIT) {
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
				} else if (property.endsWith("color") && value.getCssValueType() == CssType.TYPED) {
					// If color is an identifier, try to give a computed color value
					value = colorValue(property, (TypedValue) value);
				}
			}
		}
		return value;
	}

	private StyleValue absoluteValue(String property, StyleValue value, boolean useParentStyle) {
		CssType type = value.getCssValueType();
		if (type == CssType.LIST) {
			ValueList list = (ValueList) value;
			int lstlen = list.getLength();
			for (int i = 0; i < lstlen; i++) {
				list.set(i, absoluteValue(property, list.item(i), useParentStyle));
			}
		} else if (type == CssType.TYPED) {
			value = absoluteTypedValue(property, (TypedValue) value, useParentStyle);
		} else if (type == CssType.PROXY) {
			value = absoluteProxyValue(property, value, useParentStyle);
		}
		return value;
	}

	StyleValue absoluteProxyValue(String propertyName, CSSValue pri, boolean useParentStyle) {
		StyleValue value;
		Type pritype = pri.getPrimitiveType();
		// Check for custom properties ('variables')
		if (pritype == Type.VAR) {
			value = evaluateCustomProperty(propertyName, (VarValue) pri, useParentStyle);
			value = absoluteValue(propertyName, value, useParentStyle);
		} else if (pritype == Type.LEXICAL) {
			value = evaluateLexicalValue(propertyName, (LexicalValue) pri, useParentStyle);
			if (value != null) {
				value = absoluteValue(propertyName, value, useParentStyle);
			}
		} else if (pritype == Type.ATTR) {
			value = computeAttribute(propertyName, (AttrValue) pri, useParentStyle);
		} else if (pritype == Type.ENV) {
			value = computeEnv(propertyName, (EnvVariableValue) pri, useParentStyle);
		} else {
			value = null;
		}
		return value;
	}

	TypedValue absoluteTypedValue(String propertyName, TypedValue pri, boolean useParentStyle) {
		if (isRelativeUnit(pri)) {
			try {
				pri = absoluteNumberValue((NumberValue) pri, useParentStyle);
			} catch (DOMException | IllegalStateException e) {
				computedStyleError(propertyName, pri.getCssText(), "Could not absolutize property value.", e);
			}
		} else {
			Type type = pri.getPrimitiveType();
			if (type == Type.EXPRESSION) {
				pri = pri.clone();
				ExpressionValue exprval = (ExpressionValue) pri;
				Evaluator ev = new MyEvaluator(propertyName);
				try {
					pri = ev.evaluateExpression(exprval);
				} catch (DOMException e) {
					computedStyleWarning(propertyName, pri, "Could not evaluate expression value.", e);
					// Evaluation failed, convert expressions to absolute anyway.
					absoluteExpressionValue(propertyName, exprval.getExpression(), useParentStyle);
				}
			} else if (type == Type.FUNCTION) {
				FunctionValue function = (FunctionValue) pri;
				function = function.clone();
				Evaluator ev = new MyEvaluator(propertyName);
				try {
					pri = (TypedValue) ev.evaluateFunction(function);
				} catch (DOMException e) {
					computedStyleWarning(propertyName, pri, "Could not evaluate function value.", e);
					// Evaluation failed, convert arguments to absolute anyway.
					LinkedCSSValueList args = function.getArguments();
					int sz = args.size();
					for (int i = 0; i < sz; i++) {
						args.set(i, absoluteValue(propertyName, args.get(i), useParentStyle));
					}
				}
			} else if (type == Type.COLOR) {
				pri = computeColor(propertyName, pri);
			} else {
				// Handle rect() and ratio()
				pri = relativizeComponents(propertyName, pri);
			}
		}
		return pri;
	}

	private NumberValue absoluteNumberValue(NumberValue value, boolean useParentStyle) {
		short unit = value.getUnitType();
		float fv = value.getFloatValue(unit);
		if (unit == CSSUnit.CSS_EM) {
			if (useParentStyle) {
				fv *= getParentComputedStyle().getComputedFontSize();
			} else {
				fv *= getComputedFontSize();
			}
		} else if (unit == CSSUnit.CSS_EX) {
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
		} else if (unit == CSSUnit.CSS_REM) {
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				fv *= root.getComputedStyle(null).getComputedFontSize();
			} else {
				fv *= getInitialFontSize();
			}
		} else if (unit == CSSUnit.CSS_LH) {
			if (useParentStyle) {
				fv *= getParentComputedStyle().getComputedLineHeight();
			} else {
				fv *= getComputedLineHeight();
			}
		} else if (unit == CSSUnit.CSS_RLH) {
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				fv *= root.getComputedStyle(null).getComputedLineHeight();
			} else {
				fv *= getInitialFontSize();
			}
		} else {
			CSSCanvas canvas = getOwnerNode().getOwnerDocument().getCanvas();
			if (unit == CSSUnit.CSS_CAP) {
				if (canvas != null) {
					fv *= canvas.getCapHeight(this);
				} else {
					throw new IllegalStateException("cap unit requires canvas");
				}
			} else if (unit == CSSUnit.CSS_CH) {
				if (canvas != null) {
					fv *= canvas.stringWidth("0", this);
				} else {
					fv *= getComputedFontSize() * 0.25f;
				}
			} else if (unit == CSSUnit.CSS_IC) {
				if (canvas != null) {
					fv *= canvas.stringWidth("\u6C34", this);
				} else {
					fv *= getComputedFontSize();
				}
			} else if (unit == CSSUnit.CSS_VW) {
				fv *= getInitialContainingBlockWidthPt(canvas, true) * 0.01f;
			} else if (unit == CSSUnit.CSS_VH) {
				fv *= getInitialContainingBlockHeightPt(canvas, true) * 0.01f;
			} else if (unit == CSSUnit.CSS_VI) {
				String writingMode = getCSSValue("writing-mode").getCssText();
				if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
					fv *= getInitialContainingBlockWidthPt(canvas, true);
				} else {
					fv *= getInitialContainingBlockHeightPt(canvas, true);
				}
				fv *= 0.01f;
			} else if (unit == CSSUnit.CSS_VB) {
				String writingMode = getCSSValue("writing-mode").getCssText();
				if ("horizontal-tb".equalsIgnoreCase(writingMode)) {
					fv *= getInitialContainingBlockHeightPt(canvas, true);
				} else {
					fv *= getInitialContainingBlockWidthPt(canvas, true);
				}
				fv *= 0.01f;
			} else if (unit == CSSUnit.CSS_VMIN) {
				float size = Math.min(getInitialContainingBlockWidthPt(canvas, true),
						getInitialContainingBlockHeightPt(canvas, true));
				fv *= size * 0.01f;
			} else if (unit == CSSUnit.CSS_VMAX) {
				float size = Math.max(getInitialContainingBlockWidthPt(canvas, true),
						getInitialContainingBlockHeightPt(canvas, true));
				fv *= size * 0.01f;
			} else {
				fv = NumberValue.floatValueConversion(fv, unit, CSSUnit.CSS_PT);
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
						CSSUnit.CSS_PT);
			}
		}
		StyleDatabase sdb = getStyleDatabase();
		if (sdb != null) {
			fv = sdb.getDeviceWidth();
			return NumberValue.floatValueConversion(fv, sdb.getNaturalUnit(), CSSUnit.CSS_PT);
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
						CSSUnit.CSS_PT);
			}
		}
		StyleDatabase sdb = getStyleDatabase();
		if (sdb != null) {
			fv = sdb.getDeviceHeight();
			return NumberValue.floatValueConversion(fv, sdb.getNaturalUnit(), CSSUnit.CSS_PT);
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

	private TypedValue computeColor(String propertyName, TypedValue color) {
		TypedValue color2 = null;
		int i = 0;
		while (true) {
			PrimitiveValue comp = (PrimitiveValue) color.getComponent(i);
			if (comp == null) {
				break;
			}
			if (comp.getPrimitiveType() == Type.EXPRESSION) {
				if (color2 == null) {
					color2 = color.clone();
				}
				ExpressionValue exprval = (ExpressionValue) comp;
				Evaluator ev = new PercentageEvaluator();
				try {
					comp = ev.evaluateExpression(exprval);
				} catch (DOMException e) {
					computedStyleError(propertyName, color.getCssText(), "Could not evaluate expression value.",
							e);
					return color;
				}
				color2.setComponent(i, comp);
			}
			i++;
		}
		return color2 == null ? color : color2;
	}

	private TypedValue relativizeComponents(String propertyName, TypedValue pri) {
		TypedValue pri2 = null;
		int i = 0;
		while (true) {
			StyleValue comp = pri.getComponent(i);
			if (comp == null) {
				break;
			}
			if (comp.getPrimitiveType() == Type.EXPRESSION) {
				if (pri2 == null) {
					pri2 = pri.clone();
				}
				ExpressionValue exprval = (ExpressionValue) comp;
				Evaluator ev = new MyEvaluator(propertyName);
				try {
					comp = ev.evaluateExpression(exprval);
				} catch (DOMException e) {
					computedStyleError(propertyName, pri.getCssText(), "Could not evaluate expression value.",
							e);
					return pri;
				}
				pri2.setComponent(i, comp);
			}
			i++;
		}
		return pri2 == null ? pri : pri2;
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
			CSSPrimitiveValue primi = operand.getOperand();
			if (primi.getCssValueType() == CssType.TYPED) {
				primi = absoluteTypedValue(propertyName, (TypedValue) primi, useParentStyle);
			} else {
				StyleValue value = absoluteProxyValue(propertyName, primi, useParentStyle);
				if (value.getCssValueType() == CssType.TYPED) {
					primi = (CSSPrimitiveValue) value;
				} else {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Unexpected value in expression: " + value.getCssText());
				}
			}
			operand.setOperand(primi);
		}
	}

	private StyleValue computeAttribute(String propertyName, AttrValue attr, boolean useParentStyle) throws DOMException {
		String attrname = attr.getAttributeName();
		String attrvalue = getOwnerNode().getAttribute(attrname);
		String attrtype = attr.getAttributeType();
		if (attrvalue.length() != 0) {
			if (attrtype == null || "string".equalsIgnoreCase(attrtype)) {
				// Do not reparse
				StringValue value = new StringValue(AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE);
				value.setStringValue(Type.STRING, attrvalue);
				return value;
			}
			attrvalue = attrvalue.trim();
			if ("url".equalsIgnoreCase(attrtype)) {
				try {
					URL url = getOwnerNode().getOwnerDocument().getURL(attrvalue);
					URIValue uri = new URIValue(AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE);
					uri.setStringValue(Type.URI, url.toExternalForm());
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
				if (value.getCssValueType() == CssType.PROXY) {
					// Prevent circular dependencies.
					addAttrNameGuard(attrname);
					try {
						value = absoluteValue(propertyName, value, useParentStyle);
					} catch (Exception e) {
						clearAttrGuardStack();
						throw e;
					}
					removeAttrNameGuard(attrname);
				}
				// Attribute value must now be typed
				if (value.getCssValueType() == CssType.TYPED) {
					TypedValue pri;
					// Prevent circular dependencies.
					addAttrNameGuard(attrname);
					try {
						pri = absoluteTypedValue(propertyName, (TypedValue) value, useParentStyle);
					} catch (Exception e) {
						clearAttrGuardStack();
						throw e;
					}
					removeAttrNameGuard(attrname);
					TypedValue val = attrValueOfType(pri, attrtype);
					if (val != null) {
						val = absoluteTypedValue(propertyName, val, useParentStyle);
						return val;
					}
					computedStyleWarning(propertyName, attr, "Attribute value does not match type (" + attrtype + ").");
				} else {
					computedStyleWarning(propertyName, attr, "Invalid attribute value");
				}
			}
		}
		// Fallback
		StyleValue fallback = attr.getFallback();
		if (fallback == null) {
			if (attrValueStack != null && !attrValueStack.isEmpty()) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"No explicit fallback and we are in recursive attr(), forbidden by CSS.");
			}
			TypedValue defval = AttrValue.defaultFallback(attrtype);
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
			if (fallback == null || fallback.getPrimitiveType() == CSSValue.Type.INHERIT) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR, "Invalid fallback.");
			}
			value = absoluteValue(propertyName, fallback, useParentStyle);
		} catch (Exception e) {
			clearAttrGuardStack();
			throw e;
		}
		removeAttrNameGuard(attrname);
		if (value.getCssValueType() != CssType.TYPED) {
			return value;
		}
		TypedValue pri = (TypedValue) value;
		if (pri.getPrimitiveType() == Type.STRING && "url".equalsIgnoreCase(attrtype)) {
			try {
				URL url = getOwnerNode().getOwnerDocument().getURL(pri.getStringValue());
				URIValue uri = new URIValue(AbstractCSSStyleSheetFactory.STRING_DOUBLE_QUOTE);
				uri.setStringValue(Type.URI, url.toExternalForm());
				return uri;
			} catch (MalformedURLException e) {
			}
		} else if ("color".equalsIgnoreCase(attrtype)) {
			pri = colorValue("", pri);
		}
		return pri;
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

	private TypedValue attrValueOfType(TypedValue value, String type) throws DOMException {
		if ("color".equalsIgnoreCase(type)) {
			value = colorValue("", value);
			if (value.getPrimitiveType() == Type.COLOR) {
				return value;
			}
		} else {
			short ptype = value.getUnitType();
			if ("number".equalsIgnoreCase(type)) {
				if (ptype == CSSUnit.CSS_NUMBER) {
					return value;
				}
			} else if ("integer".equalsIgnoreCase(type)) {
				float fval;
				if (ptype == CSSUnit.CSS_NUMBER
						&& Math.abs((fval = value.getFloatValue(ptype)) - Math.round(fval)) < 7e-6) {
					return value;
				}
			} else if ("%".equals(type)) {
				if (ptype == CSSUnit.CSS_NUMBER) {
					float fval = value.getFloatValue(CSSUnit.CSS_NUMBER);
					value.setFloatValue(CSSUnit.CSS_PERCENTAGE, fval);
					return value;
				} else if (ptype == CSSUnit.CSS_PERCENTAGE) {
					return value;
				}
			} else if ("length".equalsIgnoreCase(type)) {
				if (CSSUnit.isLengthUnitType(ptype)) {
					return value;
				}
			} else if ("angle".equalsIgnoreCase(type)) {
				if (CSSUnit.isAngleUnitType(ptype)) {
					return value;
				}
			} else if ("time".equalsIgnoreCase(type)) {
				if (ptype == CSSUnit.CSS_S || ptype == CSSUnit.CSS_MS) {
					return value;
				}
			} else if ("frequency".equalsIgnoreCase(type)) {
				if (ptype == CSSUnit.CSS_HZ || ptype == CSSUnit.CSS_KHZ) {
					return value;
				}
			} else if (ptype == CSSUnit.CSS_NUMBER) {
				String lctypeval = type.toLowerCase(Locale.ROOT).intern();
				short expectedType = ParseHelper.unitFromString(lctypeval);
				if (expectedType != CSSUnit.CSS_OTHER) {
					if (CSSUnit.isLengthUnitType(expectedType)) {
						return NumberValue.createCSSNumberValue(expectedType, value.getFloatValue(ptype));
					} else if (CSSUnit.isAngleUnitType(expectedType)) {
						return NumberValue.createCSSNumberValue(expectedType, value.getFloatValue(ptype));
					} else if (expectedType == CSSUnit.CSS_S
							|| expectedType == CSSUnit.CSS_MS) {
						return NumberValue.createCSSNumberValue(expectedType, value.getFloatValue(ptype));
					} else if (expectedType == CSSUnit.CSS_HZ
							|| expectedType == CSSUnit.CSS_KHZ) {
						return NumberValue.createCSSNumberValue(expectedType, value.getFloatValue(ptype));
					}
					return null;
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

	private StyleValue evaluateCustomProperty(String property, VarValue value, boolean useParentStyle) {
		String propertyName = getCanonicalPropertyName(value.getName());
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<String>();
		} else if (customPropertyStack.contains(propertyName)) {
			LexicalUnit fallback = value.getFallback();
			if (fallback != null) {
				StyleValue custom = new ValueFactory().createCSSValue(fallback, this);
				// Check fallback for expecting integer.
				if (value.isExpectingInteger() && custom.isPrimitiveValue()) {
					((CSSPrimitiveValue) custom).setExpectInteger();
				}
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
				LexicalUnit fallback = value.getFallback();
				if (fallback != null) {
					custom = new ValueFactory().createCSSValue(fallback, this);
					custom = absoluteValue(property, custom, useParentStyle);
				} else {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Unable to evaluate custom property " + propertyName);
				}
			} else {
				custom = absoluteValue(property, custom, useParentStyle);
			}
		} catch (Exception e) {
			customPropertyStack.clear();
			throw e;
		}
		customPropertyStack.remove(propertyName);
		if (value.isExpectingInteger() && (value.getCssValueType() == CssType.TYPED
				|| value.getCssValueType() == CssType.PROXY)) {
			((CSSPrimitiveValue) custom).setExpectInteger();
		} // 'custom' could be <inherit>
		return custom;
	}

	private StyleValue evaluateLexicalValue(String property, LexicalValue lexval, boolean useParentStyle) {
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<String>();
		}
		LexicalUnit lunit = lexval.getLexicalUnit().clone();
		try {
			LexicalUnit replUnit = replaceLexicalVar(property, lunit, new CSSOMParser());
			return getValueFactory().createCSSValue(replUnit, this);
		} catch (DOMException e) {
			computedStyleError(property, lunit.toString(), "Problem evaluating lexical value.", e);
			return null;
		}
	}

	private LexicalUnit replaceLexicalVar(String property, LexicalUnit lexval, Parser parser) throws DOMException {
		LexicalUnit lu = lexval;
		do {
			if (lu.getLexicalUnitType() == LexicalUnit.LexicalType.VAR) {
				LexicalUnit newlu;
				LexicalUnit param = lu.getParameters();
				String propertyName = param.getStringValue(); // Property name
				param = param.getNextLexicalUnit(); // Comma?
				if (param != null) {
					param = param.getNextLexicalUnit(); // Fallback
				}
				propertyName = getCanonicalPropertyName(propertyName);
				if (customPropertyStack.contains(propertyName)) {
					// Fallback
					if (param != null) {
						// Replace param, just in case
						replaceLexicalVar(property, param, parser);
						newlu = param;
					} else {
						throw new DOMException(DOMException.INVALID_ACCESS_ERR,
								"Unable to evaluate custom property " + propertyName);
					}
				} else {
					customPropertyStack.add(propertyName);
					newlu = evaluateCustomPropertyValue(property, propertyName, param, parser);
					customPropertyStack.remove(propertyName);
				}
				lu.replaceBy(newlu);
				if (lu == lexval) {
					lexval = newlu;
				}
			} else {
				LexicalUnit param = lu.getParameters();
				if (param != null) {
					replaceLexicalVar(property, param, parser);
				} else if (lu.getSubValues() != null) {
					replaceLexicalVar(property, lu.getSubValues(), parser);
				}
			}
			lu = lu.getNextLexicalUnit();
		} while (lu != null);
		return lexval;
	}

	private LexicalUnit evaluateCustomPropertyValue(String property, String customProperty, LexicalUnit param,
			Parser parser) throws DOMException {
		Exception exception = null;
		try {
			StyleValue custom = getCSSValue(customProperty);
			if (custom != null) {
				String cssText = custom.getCssText();
				return parser.parsePropertyValue(new StringReader(cssText));
			}
		} catch (Exception e) {
			exception = e;
		}
		// Fallback
		if (param != null) {
			// Replace param, just in case
			try {
				param = replaceLexicalVar(property, param, parser);
				return param;
			} catch (DOMException e) {
				exception = e;
			}
		}
		customPropertyStack.clear();
		DOMException ex = new DOMException(DOMException.INVALID_ACCESS_ERR, "Unable to evaluate custom property " + customProperty);
		if (exception != null) {
			ex.initCause(exception);
		}
		throw ex;
	}

	private StyleValue computeEnv(String propertyName, EnvVariableValue env, boolean useParentStyle) {
		if (getStyleDatabase() != null) {
			StyleValue envValue = (StyleValue) getStyleDatabase().getEnvValue(env.getName());
			if (envValue != null) {
				envValue = absoluteValue(propertyName, envValue, useParentStyle);
				return envValue;
			}
		}
		StyleValue fallback = env.getFallback();
		if (fallback == null) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"Unable to evaluate env() value: " + env.getName());
		}
		fallback = absoluteValue(propertyName, fallback, useParentStyle);
		return fallback;
	}

	private TypedValue getFontSizeValue() {
		StyleValue value = super.getCSSValue("font-size");
		// Check for unset
		if (value != null && isCSSKeyword(CSSValue.Type.UNSET, value)) {
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
		if (value != null && value.getPrimitiveType() == CSSValue.Type.INHERIT) {
			value = null;
		}
		// Check for null, and apply initial values if appropriate
		if (value == null || isCSSKeyword(CSSValue.Type.INITIAL, value)) {
			return new IdentifierValue("medium");
		}
		return absoluteFontSizeValue(value, false);
	}

	private TypedValue absoluteFontSizeValue(StyleValue value, boolean force) {
		CssType type = value.getCssValueType();
		if (type == CssType.TYPED) {
			TypedValue primi = absoluteFontSizePrimitive((TypedValue) value, force);
			if (primi != null) {
				return primi;
			}
		} else if (type == CssType.PROXY) {
			StyleValue proxy;
			switch (value.getPrimitiveType()) {
			case ATTR:
				proxy = computeAttribute("font-size", (AttrValue) value, true);
				break;
			// Check for custom properties ('variables')
			case VAR:
				proxy = evaluateFontCustomProperty((VarValue) value);
				break;
			case LEXICAL:
				proxy = evaluateLexicalValue("font-size", (LexicalValue) value, true);
				break;
			// env() variables
			case ENV:
				proxy = computeFontSizeEnv((EnvVariableValue) value);
				break;
			default:
				proxy = null;
			}
			if (proxy != null) {
				return absoluteFontSizeValue(proxy, force);
			}
		}
		// Keyword
		if (value.getPrimitiveType() != Type.INITIAL) {
			TypedValue primi = null;
			ComputedCSSStyle ancStyle = this;
			do {
				StyleValue inheritedValue = inheritValue(ancStyle, "font-size", primi, true);
				primi = absoluteFontSizeValue(inheritedValue, force);
				if (primi != null) {
					break;
				}
				ancStyle = ancStyle.getParentComputedStyle();
			} while (ancStyle != null);
			if (primi != null) {
				return primi;
			}
			reportFontSizeError(value, "Could not compute font-size from " + value.getCssText());
		}
		if (force) {
			float sz = getInitialFontSize();
			return asNumericValuePt(sz);
		}
		return new IdentifierValue("medium");
	}

	private TypedValue absoluteFontSizePrimitive(TypedValue cssSize, boolean force) {
		switch (cssSize.getPrimitiveType()) {
		case IDENT:
			String sizeIdentifier = cssSize.getStringValue();
			// relative size: larger, smaller.
			String familyName = getUsedFontFamily();
			if ("larger".equalsIgnoreCase(sizeIdentifier)) {
				cssSize = getLargerFontSize(familyName);
			} else if ("smaller".equalsIgnoreCase(sizeIdentifier)) {
				cssSize = getSmallerFontSize(familyName);
			}
			return cssSize;
		case EXPRESSION:
			cssSize = cssSize.clone();
			ExpressionValue exprval = (ExpressionValue) cssSize;
			absoluteExpressionValue("font-size", exprval.getExpression(), true);
			Evaluator ev = new FontEvaluator();
			try {
				cssSize = ev.evaluateExpression(exprval);
			} catch (DOMException e) {
				computedStyleError("font-size", exprval.getCssText(), "Could not evaluate expression value.", e);
			}
			return cssSize;
		case FUNCTION:
			FunctionValue function = (FunctionValue) cssSize;
			function = function.clone();
			LinkedCSSValueList args = function.getArguments();
			int siz = args.size();
			for (int i = 0; i < siz; i++) {
				args.set(i, absoluteFontSizeValue(args.get(i), true));
			}
			ev = new FontEvaluator();
			try {
				cssSize = (TypedValue) ev.evaluateFunction(function);
			} catch (DOMException e) {
				computedStyleError("font-size", function.getCssText(), "Could not evaluate function value.", e);
			}
			return cssSize;
		case NUMERIC:
			return absoluteFontSizeNumeric(cssSize, force);
		default:
			String cssText = cssSize.getCssText();
			computedStyleError("font-size", cssText, "Unable to convert to pt.");
			float sz = getInitialFontSize();
			sz = Math.round(sz * 100f) * 0.01f;
			NumberValue number = new NumberValue();
			number.setFloatValuePt(sz);
			number.setSubproperty(cssSize.isSubproperty());
			return number;
		}
	}

	private TypedValue absoluteFontSizeNumeric(TypedValue cssSize, boolean force) {
		float sz;
		switch (cssSize.getUnitType()) {
		case CSSUnit.CSS_EM:
			float factor = cssSize.getFloatValue(CSSUnit.CSS_EM);
			// Use parent element's size.
			return getRelativeFontSize(cssSize, factor, force);
		case CSSUnit.CSS_EX:
			factor = cssSize.getFloatValue(CSSUnit.CSS_EX);
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
		case CSSUnit.CSS_REM:
			factor = cssSize.getFloatValue(CSSUnit.CSS_REM);
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				sz = root.getComputedStyle(null).getComputedFontSize();
			} else if (force) {
				reportFontSizeError(cssSize, "Inaccurate conversion from 'rem'.");
				sz = getInitialFontSize();
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSUnit.CSS_LH:
			factor = cssSize.getFloatValue(CSSUnit.CSS_LH);
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
		case CSSUnit.CSS_RLH:
			factor = cssSize.getFloatValue(CSSUnit.CSS_RLH);
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
		case CSSUnit.CSS_CAP:
			factor = cssSize.getFloatValue(CSSUnit.CSS_CAP);
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
				reportFontSizeError(cssSize, "Inaccurate conversion from 'cap'.");
			} else {
				return cssSize;
			}
			break;
		case CSSUnit.CSS_CH:
			factor = cssSize.getFloatValue(CSSUnit.CSS_CH);
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
				reportFontSizeWarning(cssSize, "Inaccurate conversion from 'ch'.");
			} else {
				return cssSize;
			}
			break;
		case CSSUnit.CSS_IC:
			factor = cssSize.getFloatValue(CSSUnit.CSS_IC);
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
				reportFontSizeWarning(cssSize, "Inaccurate conversion from 'ic'.");
			} else {
				return cssSize;
			}
			break;
		case CSSUnit.CSS_PERCENTAGE:
			float pcnt = cssSize.getFloatValue(CSSUnit.CSS_PERCENTAGE);
			// Use parent element's size.
			return getRelativeFontSize(cssSize, pcnt * 0.01f, true);
		case CSSUnit.CSS_VW:
			factor = cssSize.getFloatValue(CSSUnit.CSS_VW);
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
		case CSSUnit.CSS_VH:
			factor = cssSize.getFloatValue(CSSUnit.CSS_VH);
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
		case CSSUnit.CSS_VI:
			factor = cssSize.getFloatValue(CSSUnit.CSS_VI);
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
		case CSSUnit.CSS_VB:
			factor = cssSize.getFloatValue(CSSUnit.CSS_VB);
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
		case CSSUnit.CSS_VMIN:
			factor = cssSize.getFloatValue(CSSUnit.CSS_VMIN);
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
		case CSSUnit.CSS_VMAX:
			factor = cssSize.getFloatValue(CSSUnit.CSS_VMAX);
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
		default:
			try {
				cssSize.getFloatValue(CSSUnit.CSS_PT);
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

	private StyleValue evaluateFontCustomProperty(VarValue cssSize) {
		String propertyName = getCanonicalPropertyName(cssSize.getName());
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<String>();
		} else if (customPropertyStack.contains(propertyName)) {
			LexicalUnit fallback = cssSize.getFallback();
			if (fallback != null) {
				StyleValue custom = new ValueFactory().createCSSValue(fallback, this);
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
				LexicalUnit fallback = cssSize.getFallback();
				if (fallback != null) {
					custom = new ValueFactory().createCSSValue(fallback, this);
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

	private StyleValue computeFontSizeEnv(EnvVariableValue env) {
		if (getStyleDatabase() != null) {
			StyleValue envValue = (StyleValue) getStyleDatabase().getEnvValue(env.getName());
			if (envValue != null) {
				envValue = absoluteFontSizeValue(envValue, true);
				return envValue;
			}
		}
		StyleValue fallback = env.getFallback();
		if (fallback == null) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR,
					"Unable to evaluate env() value: " + env.getName());
		}
		fallback = absoluteFontSizeValue(fallback, true);
		return fallback;
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
	private TypedValue getRelativeFontSize(TypedValue cssSize, float factor,
			boolean force) {
		TypedValue value;
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			if (force) {
				float sz = parentCss.getComputedFontSize() * factor;
				sz = Math.round(sz * 100f) * 0.01f;
				value = asNumericValuePt(sz);
			} else {
				// Convert to absolute units
				value = parentCss.getFontSizeValue();
				if (value.getPrimitiveType() == Type.IDENT) {
					value = cssSize;
				} else {
					try {
						float sz = value.getFloatValue(CSSUnit.CSS_PT) * factor;
						sz = Math.round(sz * 100f) * 0.01f;
						value = asNumericValuePt(sz);
					} catch (DOMException e) {
						// Could not compute relative font-size.
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
		if (value != null) {
			if (value.getPrimitiveType() == CSSValue.Type.INTERNAL) {
				// Pending substitution values.
				PendingValue pending = (PendingValue) value;
				value = getSubstitutedValue("font-size", "font", pending.getLexicalUnit().clone(),
						isPropertyImportant("font-size"));
				// Check for unset
			} else if (isCSSKeyword(CSSValue.Type.UNSET, value)) {
				/*
				 * The 'unset' keyword acts as either inherit or initial, depending on whether the
				 * property is inherited or not.
				 */
				value = null;
			}
		}
		/*
		 * We compute inherited value, if appropriate.
		 */
		value = inheritValue(this, "font-size", value, true);
		// Still inheriting ?
		if (value != null && value.getPrimitiveType() == CSSValue.Type.INHERIT) {
			value = null;
		}
		CSSTypedValue cssSize;
		// Check for null, and apply initial values if appropriate
		if (value == null || isCSSKeyword(CSSValue.Type.INITIAL, value)) {
			return getInitialFontSize();
		} else {
			cssSize = absoluteFontSizeValue(value, true);
		}
		float sz;
		switch (cssSize.getPrimitiveType()) {
		case IDENT:
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
				sz = cssSize.getFloatValue(CSSUnit.CSS_PT);
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

	private TypedValue getLargerFontSize(String familyName) {
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			CSSTypedValue csssize = (CSSTypedValue) parentCss.getCSSValue("font-size");
			if (csssize != null) {
				String larger;
				switch (csssize.getPrimitiveType()) {
				case IDENT:
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

	private TypedValue getSmallerFontSize(String familyName) {
		ComputedCSSStyle parentCss = getParentComputedStyle();
		if (parentCss != null) {
			CSSTypedValue csssize = (CSSTypedValue) parentCss.getCSSValue("font-size");
			if (csssize != null) {
				String smaller;
				switch (csssize.getPrimitiveType()) {
				case IDENT:
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

	private void reportFontSizeWarning(PrimitiveValue cssSize, String message) {
		computedStyleWarning("font-size", cssSize, message);
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
		protected TypedValue absoluteTypedValue(TypedValue partialValue) {
			return ComputedCSSStyle.this.absoluteTypedValue(propertyName, partialValue, false);
		}

		@Override
		protected StyleValue absoluteProxyValue(CSSPrimitiveValue partialValue) {
			return ComputedCSSStyle.this.absoluteProxyValue(propertyName, partialValue, false);
		}

	}

	private class FontEvaluator extends MyEvaluator {

		FontEvaluator() {
			super("font-size");
		}

		@Override
		protected TypedValue absoluteTypedValue(TypedValue partialValue) {
			return ComputedCSSStyle.this.absoluteTypedValue(propertyName, partialValue, true);
		}

		@Override
		protected StyleValue absoluteProxyValue(CSSPrimitiveValue partialValue) {
			return ComputedCSSStyle.this.absoluteProxyValue(propertyName, partialValue, true);
		}

		@Override
		protected float percentage(CSSTypedValue value, short resultType) throws DOMException {
			float pcnt = value.getFloatValue(CSSUnit.CSS_PERCENTAGE);
			// Use parent element's size.
			return getParentElementFontSize() * pcnt * 0.01f;
		}

	}

	private boolean isRelativeUnit(CSSTypedValue pri) {
		if (pri.getPrimitiveType() == Type.NUMERIC) {
			switch (pri.getUnitType()) {
			case CSSUnit.CSS_EM:
			case CSSUnit.CSS_EX:
			case CSSUnit.CSS_CAP:
			case CSSUnit.CSS_CH:
			case CSSUnit.CSS_IC:
			case CSSUnit.CSS_LH:
			case CSSUnit.CSS_REM:
			case CSSUnit.CSS_RLH:
			case CSSUnit.CSS_VW:
			case CSSUnit.CSS_VH:
			case CSSUnit.CSS_VI:
			case CSSUnit.CSS_VB:
			case CSSUnit.CSS_VMIN:
			case CSSUnit.CSS_VMAX:
				return true;
			}
		}
		return false;
	}

	private TypedValue colorValue(String propertyName, TypedValue primi) {
		if (primi.getPrimitiveType() == Type.IDENT) {
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
						primi = (TypedValue) getValueFactory().parseProperty(spec);
					} catch (DOMException e) {
						// This won't happen
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
		CSSValue value = getCSSValue("line-height");
		if (value == null || value.getCssValueType() != CssType.TYPED) {
			// No 'line-height' found, applying default
			return defval * getComputedFontSize();
		}
		CSSTypedValue cssval = (CSSTypedValue) value;
		Type declType = cssval.getPrimitiveType();
		if (declType == Type.NUMERIC) {
			short unit = cssval.getUnitType();
			if (unit == CSSUnit.CSS_PERCENTAGE) {
				height = getComputedFontSize() * cssval.getFloatValue(CSSUnit.CSS_PERCENTAGE) / 100f;
			} else {
				height = cssval.getFloatValue(unit);
				if (unit != CSSUnit.CSS_PT) {
					height = NumberValue.floatValueConversion(height, unit, CSSUnit.CSS_PT);
				}
			}
		} else if (declType == Type.IDENT) {
			// expect "normal"
			if (!"normal".equalsIgnoreCase(cssval.getStringValue())) {
				computedStyleError("line-height", cssval.getStringValue(), "Wrong value: expected 'normal'");
			}
			height = defval * getComputedFontSize();
		} else {
			computedStyleError("line-height", cssval.getStringValue(), "Wrong value: expected number or identifier");
			height = defval * getComputedFontSize();
		}
		return height;
	}

	private StyleValue applyDisplayConstrains(StyleValue value) {
		StyleValue computedValue = value;
		if (value.getCssValueType() != CssType.TYPED) {
			return value;
		}
		// CSS spec, sect. 9.7
		String strVal = ((CSSTypedValue) value).getStringValue();
		if (!"none".equalsIgnoreCase(strVal)) {
			String position = getTypedValueOrInitial("position").getStringValue();
			if ("absolute".equalsIgnoreCase(position) || "fixed".equalsIgnoreCase(position)) {
				computedValue = computeConstrainedDisplay(value);
			} else {
				String floatProp = getTypedValueOrInitial("float").getStringValue();
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

	private TypedValue getTypedValueOrInitial(String propertyName) {
		TypedValue typed;
		StyleValue value = getCSSValue(propertyName);
		if (value.getCssValueType() != CssType.TYPED) {
			typed = (TypedValue) defaultPropertyValue(propertyName, PropertyDatabase.getInstance());
		} else {
			typed = (TypedValue) value;
		}
		return typed;
	}

	private TypedValue getTypedValueOrInherit(String propertyName) {
		ComputedCSSStyle ancStyle = this;
		StyleValue value = getCSSValue(propertyName);
		if (value.getCssValueType() != CssType.TYPED) {
			do {
				value = inheritValue(ancStyle, propertyName, value, true);
				if (value != null && value.getCssValueType() == CssType.TYPED) {
					break;
				}
				value = null;
				ancStyle = ancStyle.getParentComputedStyle();
			} while (ancStyle != null);
			if (value == null) {
				value = defaultPropertyValue(propertyName, PropertyDatabase.getInstance());
			}
		}
		return (TypedValue) value;
	}

	/**
	 * Table of computed values of 'display' property, per CSS spec, sect. 9.7.
	 *
	 * @param value
	 *            the value to constrain.
	 * @return the constrained value.
	 */
	private StyleValue computeConstrainedDisplay(StyleValue value) {
		String display = ((CSSTypedValue) value).getStringValue().toLowerCase(Locale.ROOT);
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
		if (value.getCssValueType() == CssType.LIST) {
			ValueList list = (ValueList) value;
			if (list.isCommaSeparated()) {
				// It is a list of layer values
				for (int i = 0; i < list.getLength(); i++) {
					StyleValue item = list.item(i);
					if (item.getCssValueType() != CssType.LIST) {
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
	protected TypedValue getCurrentColor() {
		return getCSSColor();
	}

	@Override
	public TypedValue getCSSColor() {
		return getTypedValueOrInherit("color");
	}

	@Override
	public TypedValue getCSSBackgroundColor() {
		return getTypedValueOrInitial("background-color");
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
		if (cssVal.getCssValueType() == CssType.TYPED) {
			return new String[] { getHref((CSSTypedValue) cssVal, baseHref) };
		} else {
			ValueList list = (ValueList) cssVal;
			int len = list.getLength();
			String[] sa = new String[len];
			for (int i = 0; i < len; i++) {
				sa[i] = getHref((CSSTypedValue) list.item(i), baseHref);
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
	protected String getHref(CSSTypedValue cssVal, String baseHref) {
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
				if (fontFamily.getCssValueType() == CssType.TYPED) {
					CSSTypedValue primi = (CSSTypedValue) fontFamily;
					try {
						return primi.getStringValue();
					} catch (DOMException e) {
						computedStyleError("font-family", primi.getCssText(), "Bad font-family.");
					}
				} else {
					ValueList list = (ValueList) fontFamily;
					fontFamily = list.item(0);
					if (fontFamily.getCssValueType() == CssType.TYPED) {
						CSSTypedValue primi = (CSSTypedValue) fontFamily;
						try {
							return primi.getStringValue();
						} catch (DOMException e) {
							computedStyleError("font-family", primi.getCssText(), "Bad font-family.");
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
		CSSPropertyValueException ex;
		if (cause == null) {
			ex = new CSSPropertyValueException(message);
		} else {
			if (message == null) {
				ex = new CSSPropertyValueException(cause);
			} else {
				ex = new CSSPropertyValueException(message, cause);
			}
		}
		ex.setValueText(propertyValue);
		getOwnerNode().getOwnerDocument().getErrorHandler().computedStyleError(getOwnerNode(), propertyName, ex);
	}

	private void computedStyleWarning(String propertyName, PrimitiveValue value, String message) {
		computedStyleWarning(propertyName, value, message, null);
	}

	private void computedStyleWarning(String propertyName, PrimitiveValue value, String message, Throwable cause) {
		CSSPropertyValueException ex;
		if (cause == null) {
			ex = new CSSPropertyValueException(message);
		} else {
			if (message == null) {
				ex = new CSSPropertyValueException(cause);
			} else {
				ex = new CSSPropertyValueException(message, cause);
			}
		}
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
		return getComputedPlainCssText();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		writeComputedCssText(wri, context);
	}

	@Override
	public String getMinifiedCssText() {
		return getOptimizedCssText();
	}

	public ComputedCSSStyle getRevertStyle(Condition pseudoElt) {
		ComputedCSSStyle style = ownerSheet.createComputedCSSStyle();
		CSSElement elt = ComputedCSSStyle.this.getOwnerNode();
		style.setOwnerNode(elt);
		return ownerSheet.computeRevertedStyle(style, elt.getSelectorMatcher(), pseudoElt,
				(BaseCSSStyleDeclaration) elt.getStyle(), CSSStyleSheetFactory.ORIGIN_AUTHOR);
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

		@Override
		protected CSSComputedProperties getRevertStyle(Condition pseudoElt) {
			return ComputedCSSStyle.this.getRevertStyle(pseudoElt);
		}
	}

}
