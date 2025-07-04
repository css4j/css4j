/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.DOMInvalidAccessException;
import io.sf.carte.doc.DOMNotSupportedException;
import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSColor;
import io.sf.carte.doc.style.css.CSSColorMixFunction;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSMathFunctionValue;
import io.sf.carte.doc.style.css.CSSNumberValue;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue;
import io.sf.carte.doc.style.css.CSSPropertyDefinition;
import io.sf.carte.doc.style.css.CSSResourceLimitException;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.UnitStringToId;
import io.sf.carte.doc.style.css.Viewport;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.ColorValue;
import io.sf.carte.doc.style.css.property.EnvVariableValue;
import io.sf.carte.doc.style.css.property.Evaluator;
import io.sf.carte.doc.style.css.property.ExpressionValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.LinkedCSSValueList;
import io.sf.carte.doc.style.css.property.MathFunctionValue;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.PercentageEvaluator;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.ProxyValue;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.TypedValue;
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

	private static final long serialVersionUID = 1L;

	/*
	 * Default widths and heights per media, in typographic points.
	 * To use when no style database is available, but a medium i set.
	 * Only applies to the determination of the initial containing block,
	 * and not to media queries.
	 */
	static final float SCREEN_WIDTH = 1440f;
	static final float SCREEN_HEIGHT = 810f;
	static final float HANDHELD_WIDTH = 270f;
	static final float HANDHELD_HEIGHT = 480f;
	static final float PRINT_WIDTH = 595f; // A4
	static final float PRINT_HEIGHT = 842f; // A4

	private static Set<String> attrForbiddenProperties = new HashSet<>(7);

	static {
		attrForbiddenProperties.add("background-image");
		attrForbiddenProperties.add("cue-after");
		attrForbiddenProperties.add("cue-before");
		attrForbiddenProperties.add("cursor");
		attrForbiddenProperties.add("list-style-image");
		attrForbiddenProperties.add("play-during");
	}

	private final BaseDocumentCSSStyleSheet ownerSheet;

	private CSSElement node = null;

	private transient Set<String> attrTaintedProperties = null;

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

	private static boolean isCustomPropertyName(String propertyName) {
		return propertyName.startsWith("--");
	}

	@Override
	protected void setPropertyCSSValue(String propertyName, StyleValue value, String hrefcontext) {
		if ("background-image".equals(propertyName) || "border-image-source".equals(propertyName)) {
			// Add parent stylesheet info
			if (value.getCssValueType() == CssType.LIST) {
				if (hrefcontext != null) {
					value = ((ValueList) value).wrap(hrefcontext,
							getOwnerNode().getOwnerDocument().getBaseURI());
				}
			} else if (value.getPrimitiveType() == Type.URI && hrefcontext != null) {
				value = new URIValueWrapper((URIValue) value, hrefcontext,
						getOwnerNode().getOwnerDocument().getBaseURI());
			}
		}
		super.setPropertyCSSValue(propertyName, value, hrefcontext);
	}

	/**
	 * Get the (lexical) value of a custom property, replacing lexical {@code VAR} units if needed.
	 * 
	 * @param property
	 * @param inherited
	 * @param counter
	 * @param customPtySet
	 * @return the property's lexical value.
	 */
	private LexicalValue getCustomPropertyValue(String property, boolean inherited, CounterRef counter,
			Set<String> customPtySet) {
		// All custom properties come through lexicalProperty() and are handled as a LexicalValue
		LexicalValue value = (LexicalValue) super.getCSSValue(property);

		ComputedCSSStyle ancStyle = this;
		if (inherited) {
			// Inherit until declared value is not null
			while (value == null) {
				ancStyle = ancStyle.getParentComputedStyle();
				if (ancStyle == null) {
					value = null;
					break;
				}
				if (ancStyle.isPropertySet(property)) {
					value = (LexicalValue) ancStyle.getDeclaredCSSValue(property);
				}
			}
		}

		return value;
	}

	public StyleValue getCascadedValue(String property) throws StyleDatabaseRequiredException {
		StyleValue value = super.getCSSValue(property);
		if (value != null) {
			CssType category = value.getCssValueType();
			if (category == CssType.PROXY) {
				value = replaceProxyValues(property, value);
			} else if (value.getCssValueType() == CssType.SHORTHAND) {
				return null;
			}
			if (value != null) {
				if (value.getPrimitiveType() == Type.UNSET) {
					/*
					 * The 'unset' keyword acts as either inherit or initial, depending on whether
					 * the property is inherited or not.
					 */
					value = null;
				} else {
					try {
						value = absoluteValue(property, value, false);
					} catch (DOMException e) {
						computedStyleError(property, value.getCssText(), null, e);
						value = null;
					}
				}
			}
		}
		if (value == null) {
			// Is the property inherited ?
			CSSPropertyDefinition definition = null;
			boolean inherited = PropertyDatabase.getInstance().isInherited(property) || (isCustomPropertyName(property)
					&& ((definition = getOwnerSheet().getPropertyDefinition(property)) == null || definition.inherits()));
			if (inherited) {
				value = InheritValue.getValue();
			} else if (definition != null) {
				value = (StyleValue) definition.getInitialValue();
			} else {
				value = defaultPropertyValue(property);
			}
		}
		return value;
	}

	/**
	 * Gets the absolute, primitive "computed" value for the given property.
	 * <p>
	 * The rendering context is not taken into account for this method.
	 * <p>
	 * See paragraph 6.1.2 of the Document Object Model CSS specification for the
	 * definition of "computed" values.
	 * 
	 * @param property the property that we want to evaluate.
	 * @return the primitive value of the property, a CSSShorthandValue if the
	 *         property is a shorthand, or null if the property is not known.
	 * @throws StyleDatabaseRequiredException when a computation that requires a
	 *                                        style database is attempted, but no
	 *                                        style database has been set.
	 */
	@Override
	public StyleValue getCSSValue(String property) throws StyleDatabaseRequiredException {
		// Is a regular inherited property ?
		boolean inherited = PropertyDatabase.getInstance().isInherited(property);
		// Compute value
		StyleValue comp;
		// Is it a custom property ?
		if (!inherited && isCustomPropertyName(property)) {
			CSSPropertyDefinition definition = getOwnerSheet().getPropertyDefinition(property);
			inherited = definition == null || definition.inherits();
			LexicalValue lexval;
			try {
				lexval = getCustomPropertyValue(property, inherited, new CounterRef(), new HashSet<>(1));
			} catch (DOMException e) {
				computedStyleError(property, null, null, e);
				return null;
			}
			if (lexval == null) {
				LexicalUnit initial;
				if (definition != null && (initial = definition.getInitialValue()) != null) {
					lexval = new LexicalValue();
					lexval.setLexicalUnit(initial);
				}
			}
			comp = lexval;
		} else {
			comp = getCSSValue(property, inherited);
		}
		return comp;
	}

	StyleValue getCSSValue(String property, boolean inherited) throws StyleDatabaseRequiredException {
		StyleValue value = getDeclaredCSSValue(property);
		if (value != null) {
			CssType category = value.getCssValueType();
			if (category == CssType.PROXY || category == CssType.LIST) {
				value = replaceProxyValues(property, value);
			} else if (category == CssType.SHORTHAND) {
				return null;
			}
			if (value != null) {
				// Check for unset
				if (value.getPrimitiveType() == Type.UNSET) {
					/*
					 * The 'unset' keyword acts as either inherit or initial, depending on whether
					 * the property is inherited or not.
					 */
					value = null;
				} else if (value.getPrimitiveType() == Type.LEXICAL) {
					/*
					 * LEXICAL values at this point means something went wrong.
					 */
					computedStyleError(property, value.getCssText(), "Unable to replace LEXICAL value.");
					value = null;
				}
			}
		}
		/*
		 * We compute inherited value, if appropriate.
		 */
		value = inheritValue(this, property, value, inherited);
		// Still inheriting ?
		if (value != null && value.getPrimitiveType() == Type.INHERIT) {
			value = null;
		}
		value = computeValue(property, value, inherited);
		return value;
	}

	private StyleValue replaceProxyValues(String property, StyleValue value) {
		CssType type = value.getCssValueType();
		if (type == CssType.LIST) {
			ValueList list = (ValueList) value;
			int lstlen = list.getLength();
			for (int i = 0; i < lstlen; i++) {
				StyleValue item = list.item(i);
				item = replaceProxyValues(property, item);
				while (item != null && item.getCssValueType() == CssType.PROXY) {
					item = replaceProxyValue(property, item);
				}
				if (item == null) {
					value = null;
					break;
				} else {
					list.set(i, item);
				}
			}
		} else if (type == CssType.PROXY) {
			do {
				value = replaceProxyValue(property, value);
			} while (value != null && value.getCssValueType() == CssType.PROXY
					&& value.getPrimitiveType() != Type.LEXICAL);
		}
		return value;
	}

	/**
	 * Convert a Primitive to a Typed value.
	 * <p>
	 * Use this method when getting a LIST would be an error.
	 * </p>
	 * 
	 * @param propertyName the property name.
	 * @param value        the value.
	 * @return the Typed value.
	 */
	private TypedValue primitiveToTypedValue(String propertyName, PrimitiveValue value) {
		TypedValue typed;

		if (value.getCssValueType() == CssType.TYPED) {
			typed = (TypedValue) value;
		} else {
			StyleValue proxy = value;
			do {
				proxy = replaceProxyValue(propertyName, proxy);
			} while (proxy != null && proxy.getCssValueType() == CssType.PROXY
					&& proxy.getPrimitiveType() != Type.LEXICAL);
			if (proxy == null || proxy.getCssValueType() != CssType.TYPED) {
				computedStyleError(propertyName, value.getCssText(),
						"Could not evaluate proxy value");
				return null;
			}
			typed = (TypedValue) proxy;
		}

		return typed;
	}

	StyleValue replaceProxyValue(String propertyName, CSSValue pri) {
		StyleValue value;
		Type pritype = pri.getPrimitiveType();
		// Check for top-level var()
		if (pritype == Type.LEXICAL) {
			try {
				value = evaluateLexicalValue(propertyName, (LexicalValue) pri);
			} catch (CSSResourceLimitException e) {
				computedStyleError(propertyName, pri.getCssText(), null, e);
				return null;
			}
			if (value != null && value.getPrimitiveType() == Type.LEXICAL) {
				if (!isCustomPropertyName(propertyName)) {
					// Not a custom property
					computedStyleError(propertyName, value.getCssText(),
							"Invalid value for non-custom property.");
					return null;
				}
				return value;
			}
		} else if (pritype == Type.ENV) {
			value = computeEnv(propertyName, (EnvVariableValue) pri);
		} else if (pritype == Type.INTERNAL) {
			// Pending substitution values.
			PendingValue pending = (PendingValue) pri;
			value = getSubstitutedValue(propertyName, pending.getShorthandName(),
					pending.getLexicalUnit().clone(), isPropertyImportant(propertyName));
		} else {
			return null;
		}
		return value;
	}

	private static StyleValue inheritValue(ComputedCSSStyle ancStyle, String propertyName,
			StyleValue value, boolean inherited) {
		while (value == null ? inherited : value.getPrimitiveType() == Type.INHERIT) {
			ancStyle = ancStyle.getParentComputedStyle();
			if (ancStyle == null) {
				break;
			}
			if (ancStyle.isPropertySet(propertyName)) {
				value = ancStyle.getCSSValue(propertyName);
				if (value != null && value.getPrimitiveType() == Type.UNSET) {
					value = null;
				}
			}
		}
		return value;
	}

	private StyleValue getSubstitutedValue(String longhand, String shorthandName, LexicalUnit lunit,
			boolean propertyImportant) {
		try {
			lunit = replaceLexicalProxy(longhand, lunit, new CounterRef(), new HashSet<>(1));
			if (lunit != null && setShorthandLonghands(shorthandName, lunit, propertyImportant,
					null,
					attrTaintedProperties != null && attrTaintedProperties.contains(longhand))) {
				return getCSSValue(longhand);
			}
		} catch (DOMException e) {
			computedStyleError(longhand, lunit.toString(),
					"Problem substituting lexical value in shorthand.", e);
		}
		return null;
	}

	private StyleValue computeValue(String property, StyleValue value, boolean inherited) {
		// Check for null, and apply initial values if appropriate
		if (value == null || value.getPrimitiveType() == Type.INITIAL
				|| (!inherited && value.getPrimitiveType() == Type.UNSET)) {
			value = defaultPropertyValue(property);
		}
		// If value is null now, we have no idea about this property's value
		if (value != null) {
			if (value.isSystemDefault()) {
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
							if (value != null) {
								value = ancStyle.replaceProxyValues(property, value);
								if (value != null) {
									value = ancStyle.computeValue(property, value, inherited);
								}
							}
							if (value != null) {
								break;
							}
							ancStyle = ancStyle.getParentComputedStyle();
						} while (ancStyle != null);
					}
					if (value == null || value.getPrimitiveType() == Type.INHERIT) {
						value = defaultPropertyValue(property);
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
		}
		return value;
	}

	TypedValue absoluteTypedValue(String propertyName, TypedValue typed, boolean useParentStyle) {
		if (isRelativeUnit(typed)) {
			try {
				typed = absoluteNumberValue((CSSNumberValue) typed, useParentStyle);
			} catch (DOMException | IllegalStateException e) {
				computedStyleError(propertyName, typed.getCssText(),
						"Could not absolutize property value.", e);
			}
		} else {
			Type type = typed.getPrimitiveType();
			if (type == Type.EXPRESSION) {
				typed = typed.clone();
				ExpressionValue exprval = (ExpressionValue) typed;
				Evaluator ev = new MyEvaluator(propertyName);
				try {
					typed = (TypedValue) ev.evaluateExpression(exprval);
				} catch (DOMNotSupportedException e) {
					computedStyleWarning(propertyName, typed,
							"Could not evaluate expression value.", e);
					// Evaluation failed, convert expressions to absolute anyway.
					absoluteExpressionValue(propertyName, exprval.getExpression(), useParentStyle);
				}
			} else if (type == Type.MATH_FUNCTION) {
				MathFunctionValue function = (MathFunctionValue) typed;
				Evaluator ev = new MyEvaluator(propertyName);
				try {
					typed = (TypedValue) ev.evaluateFunction(function);
				} catch (DOMNotSupportedException e) {
					computedStyleWarning(propertyName, typed, "Could not evaluate function value.",
							e);
					// Evaluation failed, convert arguments to absolute anyway.
					function = (MathFunctionValue) typed.clone();
					LinkedCSSValueList args = function.getArguments();
					int sz = args.size();
					for (int i = 0; i < sz; i++) {
						args.set(i, absoluteValue(propertyName, args.get(i), useParentStyle));
					}
				}
			} else if (type == Type.COLOR) {
				// We could handle colors as a normal componentized value,
				// but we would miss the conversion of null components to 0
				typed = computeColor(propertyName, (ColorValue) typed);
			} else if (type == Type.COLOR_MIX) {
				typed = computeColorMix(propertyName, typed, useParentStyle);
			} else if (typed.getComponentCount() != 0) {
				// Handle rect(), ratio() and arbitrary functions
				typed = absolutizeComponents(propertyName, typed, useParentStyle);
			}
		}
		return typed;
	}

	private NumberValue absoluteNumberValue(CSSNumberValue value, boolean useParentStyle) {
		short unit = value.getUnitType();
		float fv = value.getFloatValue(unit);
		if (unit == CSSUnit.CSS_EM) {
			if (useParentStyle) {
				ComputedCSSStyle parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					fv *= parentStyle.getComputedFontSize();
				} else {
					fv *= getInitialFontSize();
				}
			} else {
				fv *= getComputedFontSize();
			}
		} else if (unit == CSSUnit.CSS_EX) {
			CSSComputedProperties style;
			float fontSize;
			if (useParentStyle) {
				style = getParentComputedStyle();
				if (style != null) {
					fontSize = style.getComputedFontSize();
				} else {
					style = this;
					fontSize = getInitialFontSize();
				}
			} else {
				style = this;
				fontSize = getComputedFontSize();
			}
			if (getStyleDatabase() != null) {
				fv *= getStyleDatabase().getExSizeInPt(style.getUsedFontFamily(), fontSize);
			} else {
				fv *= fontSize * 0.5f;
			}
		} else if (unit == CSSUnit.CSS_REM) {
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				fv *= root.getComputedStyle(null).getComputedFontSize();
			} else if (!useParentStyle) {
				fv *= getComputedFontSize();
			} else {
				fv *= getInitialFontSize();
			}
		} else if (unit == CSSUnit.CSS_REX) {
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
			CSSComputedProperties style = this;
			float fontSize;
			if (root != getOwnerNode()) {
				style = root.getComputedStyle(null);
				fontSize = style.getComputedFontSize();
			} else if (!useParentStyle) {
				fontSize = getComputedFontSize();
			} else {
				fontSize = getInitialFontSize();
			}
			if (getStyleDatabase() != null) {
				fv *= getStyleDatabase().getExSizeInPt(style.getUsedFontFamily(),
						fontSize);
			} else {
				fv *= fontSize * 0.5f;
			}
		} else if (unit == CSSUnit.CSS_LH) {
			if (useParentStyle) {
				ComputedCSSStyle parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					fv *= parentStyle.getComputedLineHeight();
				} else {
					fv *= getInitialFontSize() * 1.2f;
				}
			} else {
				fv *= getComputedLineHeight();
			}
		} else if (unit == CSSUnit.CSS_RLH) {
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				fv *= root.getComputedStyle(null).getComputedLineHeight();
			} else if (!useParentStyle) {
				fv *= getComputedLineHeight();
			} else {
				fv *= getInitialFontSize() * 1.2f;
			}
		} else {
			CSSCanvas canvas = getOwnerNode().getOwnerDocument().getCanvas();
			if (unit == CSSUnit.CSS_CAP) {
				if (canvas != null) {
					ComputedCSSStyle style;
					if (useParentStyle) {
						style = getParentComputedStyle();
						if (style == null) {
							throw new DOMNotSupportedException(
									"Cannot use parent style at root element.");
						}
					} else {
						style = this;
					}
					fv *= canvas.getCapHeight(style);
				} else {
					throw new IllegalStateException("cap unit requires canvas");
				}
			} else if (unit == CSSUnit.CSS_CH) {
				ComputedCSSStyle style;
				if (useParentStyle) {
					style = getParentComputedStyle();
					if (style == null) {
						fv *= getInitialFontSize() * 0.25f;
						return asNumericValuePt(fv);
					}
				} else {
					style = this;
				}
				if (canvas != null) {
					fv *= canvas.stringWidth("0", style);
				} else {
					// rough approximation
					fv *= style.getComputedFontSize() * 0.25f;
				}
			} else if (unit == CSSUnit.CSS_RCH) {
				CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
				CSSComputedProperties style;
				if (root != getOwnerNode()) {
					style = root.getComputedStyle(null);
				} else if (!useParentStyle) {
					style = this;
				} else {
					// rough approximation
					fv *= getInitialFontSize() * 0.25f;
					return asNumericValuePt(fv);
				}
				if (canvas != null) {
					fv *= canvas.stringWidth("0", style);
				} else {
					// rough approximation
					fv *= style.getComputedFontSize() * 0.25f;
				}
			} else if (unit == CSSUnit.CSS_IC) {
				ComputedCSSStyle style;
				if (useParentStyle) {
					style = getParentComputedStyle();
					if (style == null) {
						fv *= getInitialFontSize();
						return asNumericValuePt(fv);
					}
				} else {
					style = this;
				}
				if (canvas != null) {
					fv *= canvas.stringWidth("\u6C34", style);
				} else {
					// rough approximation
					fv *= getComputedFontSize();
				}
			} else if (unit == CSSUnit.CSS_RIC) {
				CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
				CSSComputedProperties style;
				if (root != getOwnerNode()) {
					style = root.getComputedStyle(null);
				} else if (!useParentStyle) {
					style = this;
				} else {
					fv *= getInitialFontSize();
					return asNumericValuePt(fv);
				}
				if (canvas != null) {
					fv *= canvas.stringWidth("\u6C34", style);
				} else {
					// rough approximation
					fv *= style.getComputedFontSize();
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
		return asNumericValuePt(fv);
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
				return PRINT_WIDTH;
			} else if ("screen".equals(medium)) {
				return SCREEN_WIDTH;
			} else if ("handheld".equals(medium)) {
				return HANDHELD_WIDTH;
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
				return PRINT_HEIGHT;
			} else if ("screen".equals(medium)) {
				return SCREEN_HEIGHT;
			} else if ("handheld".equals(medium)) {
				return HANDHELD_HEIGHT;
			}
		}
		throw new StyleDatabaseRequiredException("Unit conversion failed.");
	}

	private TypedValue computeColor(String propertyName, ColorValue color) {
		TypedValue color2 = null;

		int len = color.getColor().getLength();
		for (int i = 0; i < len; i++) {
			PrimitiveValue comp = color.getComponent(i);
			if (comp != null) {
				TypedValue typed = primitiveToTypedValue(propertyName, comp);
				if (comp != typed) {
					if (typed == null) {
						return null;
					}
					if (color2 == null) {
						color2 = color.clone();
					}
					color2.setComponent(i, typed);
				}
				if (typed.getPrimitiveType() == Type.EXPRESSION) {
					if (color2 == null) {
						color2 = color.clone();
					}
					Evaluator ev = new PercentageEvaluator();
					try {
						typed = (TypedValue) ev.evaluateExpression((ExpressionValue) typed);
					} catch (DOMException e) {
						computedStyleError(propertyName, color.getCssText(),
								"Could not evaluate expression value in color.", e);
						return null;
					}
					color2.setComponent(i, typed);
				} else if (typed.getPrimitiveType() == Type.MATH_FUNCTION) {
					if (color2 == null) {
						color2 = color.clone();
					}
					PercentageEvaluator eval = new PercentageEvaluator();
					try {
						typed = (TypedValue) eval.evaluateFunction((CSSMathFunctionValue) typed);
					} catch (DOMException e) {
						computedStyleError(propertyName, color.getCssText(),
								"Could not evaluate math function in color.", e);
						return null;
					}
					color2.setComponent(i, typed);
				}
			} else {
				// Omitted components are zero
				comp = NumberValue.createCSSNumberValue(CSSUnit.CSS_NUMBER, 0f);
				if (color2 == null) {
					color2 = color.clone();
				}
				color2.setComponent(i, comp);
			}
		}

		return color2 == null ? color : color2;
	}

	private TypedValue computeColorMix(String propertyName, TypedValue typed,
			boolean useParentStyle) {
		TypedValue typed2 = null;

		typed = absolutizeComponents(propertyName, typed, useParentStyle);

		if (typed == null) {
			// Components could not be made absolute
			return null;
		}

		CSSColorMixFunction colorMix = (CSSColorMixFunction) typed;
		PrimitiveValue colorVal1 = (PrimitiveValue) colorMix.getColorValue1();
		PrimitiveValue colorVal2 = (PrimitiveValue) colorMix.getColorValue2();
		TypedValue color1 = primitiveToTypedValue(propertyName, colorVal1);
		TypedValue color2 = primitiveToTypedValue(propertyName, colorVal2);

		if (color1 == null || color2 == null) {
			return null;
		}

		color1 = absolutizeComponents(propertyName, color1, useParentStyle);
		color2 = absolutizeComponents(propertyName, color2, useParentStyle);

		if (color1 != colorVal1) {
			typed2 = typed.clone();
			typed2.setComponent(0, color1);
		}
		if (color2 != colorVal2) {
			if (typed2 == null) {
				typed2 = typed.clone();
			}
			typed2.setComponent(2, color2);
		}

		PrimitiveValue primi = (PrimitiveValue) colorMix.getPercentage1();
		if (primi != null) {
			TypedValue pcnt1 = primitiveToTypedValue(propertyName, primi);
			pcnt1 = absolutizeComponents(propertyName, pcnt1, useParentStyle);
			if (pcnt1 == null) {
				return null;
			}
			if (pcnt1 != primi) {
				if (typed2 == null) {
					typed2 = typed.clone();
				}
				typed2.setComponent(1, pcnt1);
			}
		}

		primi = (PrimitiveValue) colorMix.getPercentage2();
		if (primi != null) {
			TypedValue pcnt2 = primitiveToTypedValue(propertyName, primi);
			if (pcnt2 == null) {
				return null;
			}
			pcnt2 = absolutizeComponents(propertyName, pcnt2, useParentStyle);
			if (pcnt2 != primi) {
				if (typed2 == null) {
					typed2 = typed.clone();
				}
				typed2.setComponent(3, pcnt2);
			}
		}

		// Interpolation method ?
		PrimitiveValue interpMethod = (PrimitiveValue) typed.getComponent(4);
		if (interpMethod != null) {
			if (typed2 == null) {
				typed2 = typed.clone();
			}
			TypedValue ident = primitiveToTypedValue(propertyName, interpMethod);
			if (ident == null || ident.getPrimitiveType() != Type.IDENT) {
				return null;
			}
			typed2.setComponent(4, ident);
		}

		colorMix = (CSSColorMixFunction) (typed2 == null ? typed : typed2);

		CSSColor color = colorMix.getColor();
		if (color != null) {
			typed = (TypedValue) color.packInValue();
		} else {
			typed = null;
		}

		return typed;
	}

	private TypedValue absolutizeComponents(String propertyName, TypedValue typed,
			boolean useParentStyle) {
		TypedValue typed2 = null;

		int len = typed.getComponentCount();
		for (int i = 0; i < len; i++) {
			StyleValue comp = typed.getComponent(i);
			if (comp == null) {
				continue;
			}

			StyleValue value = replaceProxyValues(propertyName, comp);
			if (value == null) {
				// Proxy values could not be replaced
				return null;
			}
			value = absoluteValue(propertyName, value, useParentStyle);

			if (value != comp) {
				if (value == null) {
					return null;
				}
				if (typed2 == null) {
					typed2 = typed.clone();
				}
				typed2.setComponent(i, value);
			}

			if (value.getPrimitiveType() == Type.EXPRESSION) {
				if (typed2 == null) {
					typed2 = typed.clone();
				}

				ExpressionValue exprval = (ExpressionValue) value;
				Evaluator ev = new MyEvaluator(propertyName);
				try {
					comp = (TypedValue) ev.evaluateExpression(exprval);
					typed2.setComponent(i, comp);
				} catch (RuntimeException e) {
					computedStyleWarning(propertyName, exprval,
							"Could not evaluate expression value.", e);
					// Evaluation failed, convert expressions to absolute anyway.
					exprval = exprval.clone();
					try {
						absoluteExpressionValue(propertyName, exprval.getExpression(),
								useParentStyle);
						typed2.setComponent(i, exprval);
					} catch (RuntimeException e1) {
						// Probably the problem already reported, ignore
					}
				}
			} else if (value.getPrimitiveType() == Type.MATH_FUNCTION) {
				if (typed2 == null) {
					typed2 = typed.clone();
				}
				MathFunctionValue mathFunction = (MathFunctionValue) value;
				PercentageEvaluator eval = new PercentageEvaluator();
				try {
					comp = (StyleValue) eval.evaluateFunction(mathFunction);
					typed2.setComponent(i, comp);
				} catch (RuntimeException e) {
					computedStyleWarning(propertyName, mathFunction,
							"Could not evaluate math function.", e);
					// Evaluation failed, convert arguments to absolute anyway.
					mathFunction = mathFunction.clone();
					LinkedCSSValueList args = mathFunction.getArguments();
					int sz = args.size();
					for (int j = 0; i < sz; i++) {
						try {
							args.set(j, absoluteValue(propertyName, args.get(j), useParentStyle));
						} catch (RuntimeException e1) {
							// Probably the problem already reported, ignore
							continue;
						}
					}
					try {
						typed2.setComponent(i, mathFunction);
					} catch (RuntimeException e1) {
						// Probably the problem already reported, ignore
					}
				}
			}
		}

		return typed2 == null ? typed : typed2;
	}

	private void absoluteExpressionValue(String propertyName, CSSExpression expr,
			boolean useParentStyle) throws DOMException {
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
			StyleValue value = (StyleValue) operand.getOperand();
			if (value.getCssValueType() == CssType.PROXY) {
				value = replaceProxyValue(propertyName, value);
			}
			CSSPrimitiveValue primi;
			if (value.getCssValueType() == CssType.TYPED) {
				primi = absoluteTypedValue(propertyName, (TypedValue) value, useParentStyle);
			} else {
				throw new DOMInvalidAccessException(
						"Unexpected value in expression: " + value.getCssText());
			}
			operand.setOperand(primi);
		}
	}

	/**
	 * Enforces the 'expect integer' flag from a {@code PROXY} value on a resolved
	 * value (obtained from the proxy).
	 * 
	 * @param property
	 * @param proxy
	 * @param resolved
	 * @return the resolved value if the enforcement succeeded at this point, {@code null} otherwise.
	 */
	private StyleValue enforceExpectIntegerFromProxy(String property, ProxyValue proxy, StyleValue resolved) {
		if (proxy.isExpectingInteger()) {
			if (resolved.isPrimitiveValue()) {
				try {
					((CSSPrimitiveValue) resolved).setExpectInteger();
				} catch (DOMException e) {
					computedStyleError(property, resolved.getCssText(), null, e);
					resolved = null;
				}
			} else if (resolved.getCssValueType() == CSSValue.CssType.LIST) {
				computedStyleError(property, resolved.getCssText(), "Expected an integer, found a LIST.");
				resolved = null;
			} // 'custom' could be <inherit>
		}
		return resolved;
	}

	/**
	 * Create a CSSOM value from a lexical value, substituting any {@code VAR} and
	 * {@code ATTR} lexical units as necessary.
	 * 
	 * @param property
	 * @param lexval
	 * @return
	 * @throws CSSResourceLimitException
	 */
	private StyleValue evaluateLexicalValue(String property, LexicalValue lexval)
			throws CSSResourceLimitException {
		HashSet<String> customPropertySet = new HashSet<>(1);
		LexicalUnit lunit = lexval.getLexicalUnit().clone();
		LexicalUnit replUnit;
		try {
			replUnit = replaceLexicalProxy(property, lunit, new CounterRef(), customPropertySet);
		} catch (CSSResourceLimitException e) {
			throw e;
		} catch (DOMException e) {
			computedStyleError(property, lunit.toString(), "Problem evaluating lexical value.", e);
			return null;
		}

		StyleValue result = null;
		if (replUnit != null) {
			try {
				result = getValueFactory().createCSSValue(replUnit, this);
			} catch (DOMException e) {
				computedStyleError(property, replUnit.toString(), "Invalid replaced value.", e);
				return null;
			}
		}

		result = enforceExpectIntegerFromProxy(property, lexval, result);

		return result;
	}

	/**
	 * Given a lexical value, replace all occurrences of the {@code VAR} and
	 * {@code ATTR} lexical types with the values of the corresponding custom
	 * properties or attributes.
	 * 
	 * @param property     the CSS property name.
	 * @param lexval       the lexical value.
	 * @param counter      the substitution and recursion counter.
	 * @param customPtySet the set of custom property names, to prevent circular
	 *                     dependencies.
	 * @return
	 * @throws DOMException
	 * @throws CSSResourceLimitException
	 */
	private LexicalUnit replaceLexicalProxy(String property, LexicalUnit lexval, CounterRef counter,
			Set<String> customPtySet) throws DOMException {
		final int REPLACE_COUNT_LIMIT = 0x20000;

		/*
		 * Replace the PROXY (var(), attr()) values in the lexical chain
		 */
		LexicalUnit lu = lexval;
		topLoop: do {
			if (lu.getLexicalUnitType() == LexicalType.VAR) {
				LexicalUnit newlu;
				LexicalUnit param = lu.getParameters();
				String customPtyName = param.getStringValue(); // Custom property name
				param = param.getNextLexicalUnit(); // Comma?
				if (param != null) {
					param = param.getNextLexicalUnit(); // Fallback
				}

				if (!counter.increment()) {
					throw new CSSResourceLimitException(
							"Resource limit hit while replacing custom property: " + customPtyName);
				}

				newlu = getCustomPropertyValueOrFallback(property, customPtyName, param, counter,
						customPtySet);

				boolean isLexval = lu == lexval;
				if (newlu == null) {
					// The current lexical unit can be removed
					lu = lu.remove();
					if (isLexval) {
						// We are processing the first in the lexical chain, re-assign
						lexval = lu;
					}
					continue;
				}

				if (newlu.getLexicalUnitType() != LexicalType.EMPTY) {
					try {
						counter.replaceCounter += lu.countReplaceBy(newlu);
					} catch (CSSBudgetException e) {
						throw new CSSResourceLimitException(
								"Resource limit hit while replacing custom property "
										+ customPtyName, e);
					}
					if (counter.replaceCounter >= REPLACE_COUNT_LIMIT) {
						throw new CSSResourceLimitException(
								"Resource limit hit while replacing custom property: "
										+ property);
					}
					lu = newlu;
					if (isLexval) {
						// We are processing the first in the lexical chain, re-assign
						lexval = newlu;
					}
				} else {
					// The current lexical unit can be removed
					LexicalUnit nextlu = lu.remove();
					if (nextlu != null) {
						lu = nextlu;
					} else {
						lu = newlu;
					}
					if (isLexval) {
						// We are processing the first in the lexical chain, re-assign
						lexval = lu;
					}
				}
				continue;
			} else if (lu.getLexicalUnitType() == LexicalType.ATTR) {
				boolean isLexval = lu == lexval;

				/*
				 * Prepare a working set of traversed custom properties and attr()
				 */
				Set<String> ptySet = new HashSet<>(customPtySet.size() + 1);
				ptySet.addAll(customPtySet);

				LexicalUnit newlu = replacementAttrUnit(property, lu, counter, customPtySet, ptySet);

				if (newlu != null) {
					// Verify whether we got another proxy value.
					newlu = replaceLexicalProxy(property, newlu, counter, ptySet);
				}

				if (newlu == null) {
					// The current lexical unit can be removed
					lu = lu.remove();
					if (isLexval) {
						// We are processing the first in the lexical chain, re-assign
						lexval = lu;
					}
					continue;
				}

				// attr() security
				if (attrForbiddenProperties.contains(property)) {
					computedStyleWarning(property, lu.getCssText(),
							property + " value is attr()-tainted.", null);
					/*
					 * We do not want to use the fallback, in case this is a
					 * shorthand substitution.
					 */
					return null;
				}

				if (attrTaintedProperties == null) {
					attrTaintedProperties = new HashSet<>();
				}
				attrTaintedProperties.add(property);

				// Handle EMPTY values
				while (newlu.getLexicalUnitType() == LexicalType.EMPTY) {
					LexicalUnit nextlu = newlu.remove();
					if (nextlu == null) {
						// The current lexical unit can be removed
						lu = lu.remove();
						if (lu == null) {
							lu = newlu;
						}
						if (isLexval) {
							// We are processing the first in the lexical chain, re-assign
							lexval = lu;
						}
						continue topLoop;
					} else {
						newlu = nextlu;
					}
				}

				try {
					counter.replaceCounter += lu.countReplaceBy(newlu);
				} catch (CSSBudgetException e) {
					throw new CSSResourceLimitException(
							"Resource limit hit while replacing attr() property " + property, e);
				}
				if (counter.replaceCounter >= REPLACE_COUNT_LIMIT) {
					throw new CSSResourceLimitException(
							"Resource limit hit while replacing attr() property " + property);
				}
				lu = newlu;
				if (isLexval) {
					// We are processing the first in the lexical chain, re-assign
					lexval = newlu;
				}
				continue;
			} else {
				LexicalUnit param = lu.getParameters();
				if (param != null || (param = lu.getSubValues()) != null) {
					// Ignore return value (it is a parameter or a sub-value)
					replaceLexicalProxy(property, param, counter, customPtySet);
				}
			}
			lu = lu.getNextLexicalUnit();
		} while (lu != null);

		return lexval;
	}

	/**
	 * Obtain the (lexical) value of a custom property and replace any {@code VAR}
	 * unit in it, applying the fallback if necessary.
	 * 
	 * @param property       the name of the CSS standard property being looked for.
	 * @param customProperty the custom property name.
	 * @param fallbackLU     the custom property fallback.
	 * @param parent         the parent element, or {@code null} if no parent.
	 * @param counter        the counter.
	 * @param checkPtySet    check that the custom property name is not a member of
	 *                       this set of custom property names, to prevent circular
	 *                       dependencies.
	 * @param putSet         if a custom property value is found, add the custom
	 *                       property name to this set, to prevent circular
	 *                       dependencies.
	 * @return the value of {@code customProperty} or the fallback if there is no
	 *         value.
	 * @throws DOMException
	 */
	private LexicalUnit getCustomPropertyValueOrFallback(String property, String customProperty,
			LexicalUnit fallbackLU, CounterRef counter, Set<String> checkPtySet)
			throws DOMException {
		CSSPropertyDefinition definition = getOwnerSheet().getPropertyDefinition(customProperty);
		boolean inherited = definition == null || definition.inherits();

		/*
		 * Registered initial value takes precedence over fallback
		 */

		LexicalUnit lu;

		if (checkPtySet.contains(customProperty)) {
			computedStyleError(property, null, "Circularity evaluating custom property "
					+ customProperty + ": " + checkPtySet.toString());
			if (definition == null || (lu = definition.getInitialValue()) == null) {
				return null;
			}
		} else {
			LexicalValue custom = getCustomPropertyValue(customProperty, inherited, counter,
					checkPtySet);

			if (custom != null) {
				lu = custom.getLexicalUnit();
				// Replace proxies before matching syntax
				// We do not want to mess with a declared value, so clone it
				lu = lu.clone();
				// Verify whether we got another proxy value.
				Set<String> ptySet = new HashSet<>(checkPtySet.size() + 1);
				ptySet.add(customProperty);
				lu = replaceLexicalProxy(property, lu, counter, ptySet);
				// Check syntax
				if (definition != null) {
					if (lu == null || lu.matches(definition.getSyntax()) == Match.FALSE) {
						lu = definition.getInitialValue();
						if (lu != null) {
							lu = replaceLexicalProxy(property, lu.clone(), counter, ptySet);
						}
					}
				}
				return lu;
			} else {
				if (definition != null) {
					lu = definition.getInitialValue();
					if (lu == null) {
						lu = fallbackLU;
						if (lu == null) {
							return null;
						}
					}
				} else {
					lu = fallbackLU;
					if (lu == null) {
						return null;
					}
				}
			}
		}

		// Verify whether we got another proxy value.
		Set<String> ptySet = new HashSet<>(checkPtySet.size() + 1);
		ptySet.add(customProperty);
		// lu is not null here
		return replaceLexicalProxy(property, lu.clone(), counter, ptySet);
	}

	private LexicalUnit replacementAttrUnit(String propertyName, LexicalUnit attr,
			CounterRef counter, Set<String> checkSet, Set<String> putSet) throws DOMException {
		// Obtain attribute name and type (if set)
		LexicalUnit lu = attr.getParameters();
		if (lu.getLexicalUnitType() != LexicalType.IDENT) {
			computedStyleError(propertyName, attr.getCssText(),
					"Unexpected attribute name: " + lu.getCssText());
			return null;
		}
		String attrname = lu.getStringValue();

		if (checkSet.contains(attrname)) {
			throw new DOMInvalidAccessException(
					"Circularity evaluating attr() '" + attrname + "': " + checkSet.toString());
		}

		putSet.add(attrname);

		CSSValueSyntax syn = null;
		short unitConv = -1;
		lu = lu.getNextLexicalUnit();
		if (lu != null) {
			if (lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
				switch (lu.getLexicalUnitType()) {
				case IDENT:
					String attrtype = lu.getStringValue().toLowerCase(Locale.ROOT);
					if (!"raw-string".equals(attrtype)) {
						unitConv = UnitStringToId.unitFromString(attrtype);
						if (unitConv == CSSUnit.CSS_OTHER) {
							computedStyleError(propertyName, attr.getCssText(),
									"Unexpected attribute type: " + attrtype);
							return null;
						}
					}
					break;
				case OPERATOR_MOD:
					// %
					unitConv = CSSUnit.CSS_PERCENTAGE;
					break;
				case TYPE_FUNCTION:
					LexicalUnit param = lu.getParameters();
					if (param.getLexicalUnitType() == LexicalType.SYNTAX) {
						syn = param.getSyntax();
						// Set some unit so it is not processed as string
						unitConv = CSSUnit.CSS_NUMBER;
						break;
					}
				default:
					computedStyleError(propertyName, attr.getCssText(),
							"Unexpected attribute type: " + lu.getCssText());
					return lu == null ? null : lu.clone();
				}

				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					if (lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
						computedStyleError(propertyName, attr.getCssText(),
								"Expected comma, found: " + lu.getCssText());
						return null;
					}
					lu = lu.getNextLexicalUnit();
					// Now lu contains the fallback
				}
			} else {
				lu = lu.getNextLexicalUnit();
				if (lu == null) {
					// Ending with comma is wrong syntax
					computedStyleError(propertyName, attr.getCssText(),
							"Unexpected end after comma.");
					return null;
				}
			}
		}

		// Obtain the attribute value
		CSSElement owner = getOwnerNode();
		org.w3c.dom.Attr attrNode = owner.getAttributeNode(attrname);
		String attrvalue = attrNode != null ? attrNode.getValue() : "";

		Parser parser = getStyleSheetFactory().createSACParser();

		/*
		 * string type is a special case
		 */
		if (unitConv == -1) { // string
			LexicalUnit substValue;
			if (attrNode != null || lu == null) {
				String s = ParseHelper.quote(attrvalue, '"');
				try {
					substValue = parser.parsePropertyValue(new StringReader(s));
				} catch (IOException e) {
					// This won't happen
					substValue = null;
				} catch (CSSParseException e) {
					// Possibly a budget error
					computedStyleError(propertyName, attr.getCssText(),
							"Unexpected error parsing: "
									+ s.substring(0, Math.min(s.length(), 255)), e);
					// Process fallback
					if (lu != null) {
						substValue = lu.clone();
					} else {
						try {
							substValue = parser.parsePropertyValue(new StringReader("\"\""));
						} catch (CSSParseException | IOException e1) {
							substValue = null; // cannot happen
						}
					}
				}
			} else {
				// fallback cannot be null here
				substValue = lu.clone();
			}
			// No further processing required
			return substValue;
		}

		attrvalue = attrvalue.trim();

		if (!attrvalue.isEmpty()) {
			/*
			 * Non-string types
			 */

			LexicalUnit substValue;
			try {
				substValue = parser.parsePropertyValue(new StringReader(attrvalue));
			} catch (IOException e) {
				// This won't happen
				throw new IllegalStateException(e);
			} catch (CSSParseException e) {
				computedStyleError(propertyName, attr.getCssText(),
						"Error parsing attribute '" + attrname + "', value: " + attrvalue, e);
				// Return fallback
				return lu == null ? null : lu.clone();
			}

			// Substitute proxy values before checking type.
			substValue = replaceLexicalProxy(propertyName, substValue, counter, putSet);

			if (substValue != null) {
				// Now check that the value is of the correct type.
				//
				if (syn != null) {
					if (substValue.matches(syn) == Match.TRUE) {
						return substValue;
					} else {
						String message = "Attribute " + attrname + " with value '" + substValue
								+ "' does not match type '" + syn.toString() + "'.";
						if (lu == null) {
							// Throw an exception to break the chain replacement
							throw new DOMException(DOMException.TYPE_MISMATCH_ERR, message);
						}
						computedStyleError(propertyName, attr.getCssText(), message);
					}
				} else {
					// Unit-based types
					float f;
					LexicalType luType = substValue.getLexicalUnitType();
					if (luType == LexicalType.INTEGER) {
						f = substValue.getIntegerValue();
					} else if (luType == LexicalType.REAL) {
						f = substValue.getFloatValue();
					} else {
						String message = "Attribute unit is not a <number>, instead is: "
								+ CSSUnit.dimensionUnitString(substValue.getCssUnit());
						// Check for fallback
						if (lu != null) {
							computedStyleError(propertyName, attr.getCssText(), message);
							return lu.clone();
						}
						// Guaranteed-invalid
						throw new DOMException(DOMException.TYPE_MISMATCH_ERR, message);
					}
					// Must convert to unit if necessary
					switch (unitConv) {
					case CSSUnit.CSS_NUMBER:
						break;
					case CSSUnit.CSS_PERCENTAGE:
						substValue = ParseHelper.createPercentageLexicalUnit(f);
						break;
					default:
						substValue = ParseHelper.createDimensionLexicalUnit(unitConv, f);
					}
					return substValue;
				}
			}
		}

		// Return fallback
		return lu == null ? null : lu.clone();
	}

	private StyleValue computeEnv(String propertyName, EnvVariableValue env) {
		/*
		 * In web browsers, env() is substituted at parse time. Given the multiplicity
		 * of use cases for this library, the substitution is done at computed-value
		 * time.
		 */
		if (getStyleDatabase() != null) {
			StyleValue envValue = (StyleValue) getStyleDatabase().getEnvValue(env.getName());
			if (envValue != null) {
				return envValue;
			}
		}

		StyleValue fallback = null;
		LexicalUnit lu = env.getFallback();
		if (lu == null) {
			computedStyleError(propertyName, env.getCssText(),
					"Unable to evaluate env() value for: " + env.getName());
		} else {
			ValueFactory factory = new ValueFactory();
			try {
				fallback = factory.createCSSValue(lu, this);
			} catch (DOMException e) {
				computedStyleError(propertyName, lu.toString(),
						"Unable to evaluate env() fallback: " + env.getCssText());
			}
		}
		return fallback;
	}

	private TypedValue getFontSizeValue() {
		StyleValue value = super.getCSSValue("font-size");
		// Check for unset
		if (value != null && value.getPrimitiveType() == Type.UNSET) {
			/*
			 * The 'unset' keyword acts as either inherit or initial, depending on whether the
			 * property is inherited (and 'font-size' is) or not.
			 */
			value = null;
		}
		/*
		 * We compute inherited value, if appropriate.
		 */
		value = inheritValue(this, "font-size", value, true);
		// Still inheriting ?
		if (value != null && value.getPrimitiveType() == Type.INHERIT) {
			value = null;
		}
		// Check for null, and apply initial values if appropriate
		if (value == null || value.getPrimitiveType() == Type.INITIAL) {
			return new IdentifierValue("medium");
		}
		return absoluteFontSizeValue(value, false);
	}

	private TypedValue absoluteFontSizeValue(StyleValue value, boolean force) {
		CssType type = value.getCssValueType();
		if (type == CssType.TYPED) {
			TypedValue primi = absoluteFontSizeTyped((TypedValue) value, force);
			if (primi != null) {
				return primi;
			}
		}
		// Keyword
		if (value.getPrimitiveType() != Type.INITIAL && value.getPrimitiveType() != Type.INTERNAL) {
			TypedValue primi = null;
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
			reportFontSizeError(value, "Could not compute font-size from " + value.getCssText());
		}
		if (force) {
			float sz = getInitialFontSize();
			return asNumericValuePt(sz);
		}
		return new IdentifierValue("medium");
	}

	private TypedValue absoluteFontSizeTyped(TypedValue cssSize, boolean force) {
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
			CSSExpressionValue exprval = (CSSExpressionValue) cssSize;
			absoluteExpressionValue("font-size", exprval.getExpression(), true);
			Evaluator ev = new FontEvaluator();
			try {
				cssSize = (TypedValue) ev.evaluateExpression(exprval);
			} catch (DOMException e) {
				computedStyleError("font-size", exprval.getCssText(),
						"Could not evaluate expression value.", e);
				break;
			}
			return cssSize;
		case MATH_FUNCTION:
			MathFunctionValue function = (MathFunctionValue) cssSize;
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
				computedStyleError("font-size", function.getCssText(),
						"Could not evaluate function value.", e);
				break;
			}
			return cssSize;
		case NUMERIC:
			return absoluteFontSizeNumeric(cssSize, force);
		default:
			String cssText = cssSize.getCssText();
			computedStyleError("font-size", cssText, "Unable to convert to absolute length.");
		}
		float sz = getInitialFontSize();
		sz = Math.round(sz * 100f) * 0.01f;
		NumberValue number = new NumberValue();
		number.setFloatValuePt(sz);
		number.setSubproperty(cssSize.isSubproperty());
		number.setAbsolutizedUnit();
		return number;
	}

	/*
	 * This is a bit redundant with absoluteNumberValue() called
	 * with useParentStyle=true, but allows to keep original values
	 * in some cases (where the absolute value is not computed from
	 * a calc() value).
	 */
	private TypedValue absoluteFontSizeNumeric(TypedValue cssSize, boolean force) {
		float sz;
		switch (cssSize.getUnitType()) {
		case CSSUnit.CSS_EM:
			float factor = cssSize.getFloatValue();
			// Use parent element's size.
			return getRelativeFontSize(cssSize, factor, force);
		case CSSUnit.CSS_EX:
			factor = cssSize.getFloatValue();
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
			factor = cssSize.getFloatValue();
			CSSElement root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				sz = root.getComputedStyle(null).getComputedFontSize();
			} else if (force) {
				reportFontSizeWarning(cssSize, "Inaccurate use of 'rem'.");
				sz = getInitialFontSize();
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSUnit.CSS_REX:
			factor = cssSize.getFloatValue();
			root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				sz = root.getComputedStyle(null).getComputedFontSize() * 0.5f;
			} else if (force) {
				reportFontSizeWarning(cssSize, "Inaccurate use of 'rex'.");
				sz = getInitialFontSize() * 0.5f;
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSUnit.CSS_LH:
			factor = cssSize.getFloatValue();
			parentStyle = getParentComputedStyle();
			if (parentStyle != null) {
				sz = parentStyle.getComputedLineHeight();
			} else if (force) {
				sz = getInitialFontSize() * 1.2f;
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSUnit.CSS_RLH:
			factor = cssSize.getFloatValue();
			root = getOwnerNode().getOwnerDocument().getDocumentElement();
			if (root != getOwnerNode()) {
				sz = root.getComputedStyle(null).getComputedLineHeight();
			} else if (force) {
				sz = getInitialFontSize() * 1.2f;
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSUnit.CSS_CAP:
			factor = cssSize.getFloatValue();
			CSSCanvas canvas = getOwnerNode().getOwnerDocument().getCanvas();
			if (canvas != null) {
				parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					sz = canvas.getCapHeight(parentStyle) * factor;
				} else {
					// Approximation
					sz = getInitialFontSize() * factor;
					reportFontSizeWarning(cssSize, "Inaccurate use of 'cap'.");
				}
				break;
			}
			if (force) {
				sz = getInitialFontSize() * factor;
				reportFontSizeWarning(cssSize, "Inaccurate conversion from 'cap'.");
			} else {
				return cssSize;
			}
			break;
		case CSSUnit.CSS_CH:
			factor = cssSize.getFloatValue();
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
			if (canvas != null) {
				parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					sz = canvas.stringWidth("0", parentStyle) * factor;
				} else {
					// Approximation
					sz = getInitialFontSize() * 0.25f * factor;
					reportFontSizeWarning(cssSize, "Inaccurate use of 'ch'.");
				}
				break;
			}
			if (force) {
				sz = getParentElementFontSize() * 0.25f * factor;
				reportFontSizeWarning(cssSize, "Inaccurate conversion from 'ch'.");
			} else {
				return cssSize;
			}
			break;
		case CSSUnit.CSS_RCH:
			factor = cssSize.getFloatValue();
			CSSDocument doc = getOwnerNode().getOwnerDocument();
			canvas = doc.getCanvas();
			root = doc.getDocumentElement();
			if (canvas != null) {
				if (root != getOwnerNode()) {
					sz = canvas.stringWidth("0", root.getComputedStyle(null)) * factor;
				} else {
					// We are at root
					sz = getInitialFontSize() * 0.25f * factor;
					reportFontSizeWarning(cssSize, "Inaccurate use of 'rch'.");
				}
				break;
			}
			if (force) {
				if (root != getOwnerNode()) {
					sz = root.getComputedStyle(null).getComputedFontSize() * 0.25f;
				} else {
					// We are at root
					sz = getInitialFontSize() * 0.25f;
				}
				reportFontSizeWarning(cssSize, "Inaccurate conversion from 'rch'.");
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSUnit.CSS_IC:
			factor = cssSize.getFloatValue();
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
			if (canvas != null) {
				parentStyle = getParentComputedStyle();
				if (parentStyle != null) {
					sz = canvas.stringWidth("\u6C34", parentStyle) * factor;
				} else {
					// We are at root
					sz = getInitialFontSize() * factor;
					reportFontSizeWarning(cssSize, "Inaccurate use of 'ic'.");
				}
				break;
			}
			if (force) {
				sz = getParentElementFontSize() * factor;
				reportFontSizeWarning(cssSize, "Inaccurate conversion from 'ic'.");
			} else {
				return cssSize;
			}
			break;
		case CSSUnit.CSS_RIC:
			factor = cssSize.getFloatValue();
			doc = getOwnerNode().getOwnerDocument();
			canvas = doc.getCanvas();
			root = doc.getDocumentElement();
			if (canvas != null) {
				if (root != getOwnerNode()) {
					sz = canvas.stringWidth("\u6C34", root.getComputedStyle(null)) * factor;
				} else {
					// We are at root
					sz = getInitialFontSize() * factor;
					reportFontSizeWarning(cssSize, "Inaccurate use of 'ric'.");
				}
				break;
			}
			if (force) {
				if (root != getOwnerNode()) {
					sz = root.getComputedStyle(null).getComputedFontSize();
				} else {
					// We are at root
					sz = getInitialFontSize();
				}
				reportFontSizeWarning(cssSize, "Inaccurate conversion from 'ric'.");
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSUnit.CSS_PERCENTAGE:
			float pcnt = cssSize.getFloatValue();
			// Use parent element's size.
			return getRelativeFontSize(cssSize, pcnt / 100f, true);
		case CSSUnit.CSS_VW:
			factor = cssSize.getFloatValue();
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
			try {
				sz = getInitialContainingBlockWidthPt(canvas, force) * factor / 100f;
			} catch (StyleDatabaseRequiredException e) {
				if (force) {
					throw e;
				}
				return cssSize;
			}
			break;
		case CSSUnit.CSS_VH:
			factor = cssSize.getFloatValue();
			canvas = getOwnerNode().getOwnerDocument().getCanvas();
			try {
				sz = getInitialContainingBlockHeightPt(canvas, force) * factor / 100f;
			} catch (StyleDatabaseRequiredException e) {
				if (force) {
					throw e;
				}
				return cssSize;
			}
			break;
		case CSSUnit.CSS_VI:
			factor = cssSize.getFloatValue();
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
			sz *= factor / 100f;
			break;
		case CSSUnit.CSS_VB:
			factor = cssSize.getFloatValue();
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
			sz *= factor / 100f;
			break;
		case CSSUnit.CSS_VMIN:
			factor = cssSize.getFloatValue();
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
			sz *= factor / 100f;
			break;
		case CSSUnit.CSS_VMAX:
			factor = cssSize.getFloatValue();
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
			sz *= factor / 100f;
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
		sz = Math.round(sz * 100f) / 100f;
		NumberValue number = new NumberValue();
		number.setFloatValuePt(sz);
		number.setSubproperty(cssSize.isSubproperty());
		number.setAbsolutizedUnit();
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
		if (value != null) {
			if (value.getPrimitiveType() == Type.INTERNAL) {
				// Pending substitution values.
				PendingValue pending = (PendingValue) value;
				value = getSubstitutedValue("font-size", "font", pending.getLexicalUnit().clone(),
						isPropertyImportant("font-size"));
				// Check for unset
			} else if (value.getPrimitiveType() == Type.UNSET) {
				/*
				 * The 'unset' keyword acts as either inherit or initial, depending on whether the
				 * property is inherited or not.
				 */
				value = null;
			}
			if (value != null) {
				CssType category = value.getCssValueType();
				if (category == CssType.PROXY) {
					try {
						value = replaceProxyValues("font-size", value);
					} catch (DOMException e) {
						computedStyleError("font-size", value.getCssText(), "Unable to replace PROXY value.", e);
						value = null;
					}
					if (value != null && value.getPrimitiveType() == Type.LEXICAL) {
						/*
						 * LEXICAL values at this point means something went wrong.
						 */
						computedStyleError("font-size", value.getCssText(), "Unable to replace LEXICAL value.");
						value = null;
					}
				}
			}
		}
		/*
		 * We compute inherited value, if appropriate.
		 */
		value = inheritValue(this, "font-size", value, true);
		// Still inheriting ?
		if (value != null && value.getPrimitiveType() == Type.INHERIT) {
			value = null;
		}
		CSSTypedValue cssSize;
		// Check for null, and apply initial values if appropriate
		if (value == null || value.getPrimitiveType() == Type.INITIAL) {
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
		protected CSSNumberValue absoluteTypedValue(CSSTypedValue partialValue) {
			TypedValue typed = ComputedCSSStyle.this.absoluteTypedValue(propertyName,
					(TypedValue) partialValue, false);
			return super.absoluteTypedValue(typed);
		}

		@Override
		protected StyleValue absoluteProxyValue(CSSPrimitiveValue partialValue) {
			return ComputedCSSStyle.this.replaceProxyValue(propertyName, partialValue);
		}

	}

	private class FontEvaluator extends MyEvaluator {

		FontEvaluator() {
			super("font-size");
		}

		@Override
		protected CSSNumberValue absoluteTypedValue(CSSTypedValue partialValue) {
			TypedValue typed = ComputedCSSStyle.this.absoluteTypedValue(propertyName,
					(TypedValue) partialValue, true);
			return super.absoluteTypedValue(typed);
		}

		@Override
		protected StyleValue absoluteProxyValue(CSSPrimitiveValue partialValue) {
			return ComputedCSSStyle.this.replaceProxyValue(propertyName, partialValue);
		}

		@Override
		protected float percentage(CSSNumberValue value, short resultType) throws DOMException {
			float pcnt = value.getFloatValue(CSSUnit.CSS_PERCENTAGE);
			// Use parent element's size.
			return getParentElementFontSize() * pcnt / 100f;
		}

	}

	private boolean isRelativeUnit(CSSTypedValue pri) {
		return pri.getPrimitiveType() == Type.NUMERIC
				&& CSSUnit.isRelativeLengthUnitType(pri.getUnitType());
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
					spec = "#0000";
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
		number.setAbsolutizedUnit();
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
				height = getComputedFontSize() * cssval.getFloatValue() / 100f;
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
			typed = (TypedValue) defaultPropertyValue(propertyName);
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
				value = defaultPropertyValue(propertyName);
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
			value = getValueFactory().parseProperty("0");
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
	 * Attempts to get the absolute Href from the string value of a (uri) property.
	 * 
	 * @param cssVal   the uri value.
	 * @param baseHref the base href context. Cannot be null.
	 * @return the base URL, or null if could not be determined.
	 */
	protected String getHref(CSSTypedValue cssVal, String baseHref) {
		String href = cssVal.getStringValue();
		URI uri;
		try {
			uri = new URI(href);
		} catch (Exception e) {
			getStyleDeclarationErrorHandler().malformedURIValue(baseHref);
			return null;
		}

		if (!uri.isAbsolute()) {
			// Relative URL
			URI baseUri = null;
			if (baseHref == null) {
				baseUri = documentURI();
			} else {
				try {
					baseUri = new URI(baseHref);
					if (!baseUri.isAbsolute()) {
						String ownerUri = getOwnerNode().getBaseURI();
						if (ownerUri != null) {
							URI ownerBase = new URI(ownerUri);
							baseUri = ownerBase.resolve(baseUri);
						}
					}
				} catch (Exception e) {
					getStyleDeclarationErrorHandler().malformedURIValue(baseHref);
					baseUri = documentURI();
				}
			}
			if (baseUri != null) {
				uri = baseUri.resolve(uri);
			}
		}

		return uri.toASCIIString();
	}

	private URI documentURI() {
		URI uri = null;
		String documentURI = getOwnerNode().getBaseURI();
		if (documentURI != null) {
			try {
				uri = new URI(documentURI);
			} catch (URISyntaxException e) {
				// This should never happen
			}
		}
		return uri;
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
				if (fontFamily.getCssValueType() == CssType.LIST) {
					ValueList list = (ValueList) fontFamily;
					fontFamily = list.item(0);
				}
				if (fontFamily.getPrimitiveType() != Type.UNKNOWN && fontFamily.getCssValueType() == CssType.TYPED) {
					CSSTypedValue primi = (CSSTypedValue) fontFamily;
					try {
						return primi.getStringValue();
					} catch (DOMException e) {
						computedStyleError("font-family", primi.getCssText(), "Bad font-family.");
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

	private void computedStyleWarning(String propertyName, CSSPrimitiveValue value,
			String message) {
		computedStyleWarning(propertyName, value, message, null);
	}

	private void computedStyleWarning(String propertyName, CSSPrimitiveValue value, String message,
			Throwable cause) {
		computedStyleWarning(propertyName, value.getCssText(), message, cause);
	}

	private void computedStyleWarning(String propertyName, String propertyValue, String message,
			Throwable cause) {
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
		getOwnerNode().getOwnerDocument().getErrorHandler().computedStyleWarning(getOwnerNode(),
				propertyName, ex);
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
