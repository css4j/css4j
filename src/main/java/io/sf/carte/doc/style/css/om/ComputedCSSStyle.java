/*

 Copyright (c) 2005-2024, Carlos Amengual.

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
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.AttrValue;
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
import io.sf.carte.doc.style.css.property.StringValue;
import io.sf.carte.doc.style.css.property.StyleValue;
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

	private static boolean isCustomPropertyName(String propertyName) {
		return propertyName.startsWith("--");
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
	 * @return the property's lexical value.
	 */
	private LexicalValue getCustomPropertyValue(String property, boolean inherited, CounterRef counter) {
		// All custom properties come through lexicalProperty() and are handled as a LexicalValue
		LexicalValue value = (LexicalValue) super.getCSSValue(property);
		//
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
		//
		if (value != null) {
			if (ancStyle.customPropertyStack == null) {
				ancStyle.customPropertyStack = new LinkedList<>();
			}
			LexicalUnit lunit = value.getLexicalUnit().clone();
			LexicalUnit replUnit;
			try {
				replUnit = ancStyle.replaceLexicalProxy(property, lunit, counter);
			} catch (CSSResourceLimitException e) {
				throw e;
			} catch (Exception e) {
				ancStyle.computedStyleError(property, value.getCssText(), null, e);
				replUnit = null;
			}
			//
			if (replUnit != null) {
				value = new LexicalValue();
				value.setLexicalUnit(replUnit);
			} else {
				value = null;
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
			CounterRef counter = new CounterRef();
			LexicalValue lexval;
			try {
				lexval = getCustomPropertyValue(property, inherited, counter);
			} catch (DOMException e) {
				computedStyleError(property, null, null, e);
				return null;
			}
			if (lexval == null) {
				if (definition != null) {
					LexicalValue initial = (LexicalValue) definition.getInitialValue();
					comp = customPropertyFallback(property, initial, initial.getLexicalUnit());
				} else {
					comp = null;
				}
			} else {
				comp = evaluateLexicalValue(property, lexval, counter);
				if (comp != null && comp.getCssValueType() == CssType.TYPED) {
					try {
						comp = absoluteValue(property, comp, false);
					} catch (DOMException e) {
						computedStyleError(property, comp.getCssText(), null, e);
						comp = null;
					}
				}
			}
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
		if (pritype == Type.VAR) {
			VarValue varValue = (VarValue) pri;
			value = evaluateVarValue(propertyName, varValue);
			if (value != null && (value.getPrimitiveType() == Type.INITIAL || (value.getPrimitiveType() == Type.UNSET
					&& !PropertyDatabase.getInstance().isInherited(propertyName)))) {
				value = defaultPropertyValue(propertyName);
				if (value != null) {
					// Enforce integer, if expected
					value = enforceExpectIntegerFromProxy(propertyName, varValue, value);
				}
			}
		} else if (pritype == Type.LEXICAL) {
			try {
				value = evaluateLexicalValue(propertyName, (LexicalValue) pri, new CounterRef());
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
		} else if (pritype == Type.ATTR) {
			value = computeAttribute(propertyName, (AttrValue) pri);
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
			customPropertyStack = new LinkedList<>();
		}
		lunit = replaceLexicalProxy(longhand, lunit, new CounterRef());
		return lunit != null ? setShorthandLonghands(shorthand, lunit, prioImportant, null) : false;
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
				} catch (DOMException e) {
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
				} catch (DOMException e) {
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
							throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
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
						typed2.setComponent(i, (StyleValue) mathFunction);
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
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Unexpected value in expression: " + value.getCssText());
			}
			operand.setOperand(primi);
		}
	}

	private StyleValue computeAttribute(String propertyName, AttrValue attr) throws DOMException {
		String attrname = attr.getAttributeName();
		CSSElement owner = getOwnerNode();
		String attrvalue = owner.getAttribute(attrname);
		String attrtype = attr.getAttributeType();
		if (attrvalue.length() != 0) {
			if (isSafeAttrName(propertyName, owner, attrname)) {
				if (attrtype == null || "string".equalsIgnoreCase(attrtype)) {
					// Do not reparse
					StringValue value = new StringValue(
							CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
					value.setStringValue(Type.STRING, attrvalue);
					return value;
				}
				attrvalue = attrvalue.trim();
				if ("url".equalsIgnoreCase(attrtype)) {
					try {
						URL url = getOwnerNode().getOwnerDocument().getURL(attrvalue);
						URIValue uri = new URIValue(
								CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
						uri.setStringValue(Type.URI, url.toExternalForm());
						return uri;
					} catch (MalformedURLException e) {
						computedStyleError(propertyName, attr.getCssText(),
								"Error building URL from attribute '" + attrname + "', value: "
										+ attrvalue,
								e);
						// Leaving for fallback
					}
				} else {
					ValueFactory factory = getValueFactory();
					StyleValue value;
					try {
						value = factory.parseProperty(attrvalue);
					} catch (DOMException e) {
						computedStyleError(propertyName, attr.getCssText(),
								"Error parsing attribute '" + attrname + "', value: " + attrvalue,
								e);
						return computeAttrFallback(propertyName, attr);
					}
					// Verify whether we got another PROXY value (or a LIST of them)
					// Prevent circular dependencies.
					addAttrNameGuard(attrname);
					try {
						value = replaceProxyValues(propertyName, value);
					} catch (Exception e) {
						computedStyleError(propertyName, attr.getCssText(), "Circularity: "
								+ attr.getCssText() + " references " + value.getCssText(), e);
						StyleValue fallback = computeAttrFallback(propertyName, attr);
						removeAttrNameGuard(attrname);
						return fallback;
					}
					removeAttrNameGuard(attrname);
					// Attribute value must now be typed
					if (value != null) {
						if (value.getCssValueType() == CssType.TYPED) {
							TypedValue typed = (TypedValue) value;
							// Absolutize
							typed = absoluteTypedValue(propertyName, typed, false);
							if (typed != null) {
								try {
									value = attrValueOfType(typed, attrtype);
									if (value != null) {
										return value;
									}
									computedStyleError(propertyName, attr.getCssText(),
											"Attribute value does not match type (" + attrtype
													+ ").");
								} catch (DOMException e) {
									computedStyleError(propertyName, attr.getCssText(),
											"Attribute value does not match type (" + attrtype
													+ ").",
											e);
								}
							}
						} else {
							computedStyleWarning(propertyName, attr, "Invalid attribute value.");
						}
					}
				}
			} else {
				computedStyleError(propertyName, attr.getCssText(), "Unsafe attribute name");
			}
		}
		return computeAttrFallback(propertyName, attr);
	}

	private StyleValue computeAttrFallback(String propertyName, AttrValue attr) throws DOMException {
		String attrtype = attr.getAttributeType();
		// Fallback
		StyleValue fallback = attr.getFallback();
		if (fallback != null && fallback.getCssValueType() != CssType.TYPED) {
			String attrname = attr.getAttributeName();
			addAttrNameGuard(attrname);
			fallback = replaceProxyValues(propertyName, fallback);
			if (fallback != null && fallback.getCssValueType() == CssType.PROXY) {
				fallback = replaceProxyValues(propertyName, fallback);
			}
			removeAttrNameGuard(attrname);
		}
		if (fallback == null) {
			if (attrValueStack != null && !attrValueStack.isEmpty()) {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"No explicit fallback and we are in recursive attr(), forbidden by CSS.");
			}
			TypedValue defval = AttrValue.defaultFallback(attrtype);
			if (defval != null && "color".equalsIgnoreCase(attrtype)) {
				defval = colorValue("", defval);
			}
			return defval;
		}
		if (fallback.getCssValueType() != CssType.TYPED) {
			return fallback;
		}
		TypedValue pri = (TypedValue) fallback;
		if (pri.getPrimitiveType() == Type.STRING && "url".equalsIgnoreCase(attrtype)) {
			try {
				URL url = getOwnerNode().getOwnerDocument().getURL(pri.getStringValue());
				URIValue uri = new URIValue(CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE);
				uri.setStringValue(Type.URI, url.toExternalForm());
				return uri;
			} catch (MalformedURLException e) {
				computedStyleError(propertyName, attr.getCssText(),
						"Error building URL from attr() fallback: " + pri.getCssText(), e);
				pri = null;
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
	private boolean isSafeAttrName(String propertyName, CSSElement element, String attrname) {
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
			attrValueStack = new LinkedList<>();
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

	private TypedValue attrValueOfType(TypedValue value, String type) throws DOMException {
		// Neither 'string' nor 'url' are expected here
		TypedValue result = null;
		if ("color".equalsIgnoreCase(type)) {
			value = colorValue("", value);
			if (value.getPrimitiveType() == Type.COLOR) {
				return value;
			}
		} else {
			short ptype = value.getUnitType();
			if ("number".equalsIgnoreCase(type)) {
				if (ptype == CSSUnit.CSS_NUMBER) {
					result = value;
				}
			} else if ("integer".equalsIgnoreCase(type)) {
				float fval;
				if (ptype == CSSUnit.CSS_NUMBER
						&& Math.abs((fval = value.getFloatValue(ptype)) - Math.round(fval)) < 7e-6) {
					result = value;
				}
			} else if ("percentage".equals(type)) {
				if (ptype == CSSUnit.CSS_NUMBER) {
					float fval = value.getFloatValue(CSSUnit.CSS_NUMBER);
					value.setFloatValue(CSSUnit.CSS_PERCENTAGE, fval);
					result = value;
				} else if (ptype == CSSUnit.CSS_PERCENTAGE) {
					result = value;
				}
			} else if ("ident".equalsIgnoreCase(type)) {
				if (value.getPrimitiveType() == Type.IDENT) {
					result = value;
				}
			} else if ("length".equalsIgnoreCase(type)) {
				if (CSSUnit.isLengthUnitType(ptype)) {
					result = value;
				}
			} else if ("angle".equalsIgnoreCase(type)) {
				if (CSSUnit.isAngleUnitType(ptype)) {
					result = value;
				}
			} else if ("time".equalsIgnoreCase(type)) {
				if (ptype == CSSUnit.CSS_S || ptype == CSSUnit.CSS_MS) {
					result = value;
				}
			} else if ("frequency".equalsIgnoreCase(type)) {
				if (ptype == CSSUnit.CSS_HZ || ptype == CSSUnit.CSS_KHZ) {
					result = value;
				}
			} else if (ptype == CSSUnit.CSS_NUMBER) {
				float numVal = value.getFloatValue(ptype);
				String lctypeval = type.toLowerCase(Locale.ROOT);
				short expectedType = UnitStringToId.unitFromString(lctypeval);
				if (expectedType != CSSUnit.CSS_OTHER) {
					result = NumberValue.createCSSNumberValue(expectedType, numVal);
				} else {
					throw new DOMException(DOMException.INVALID_ACCESS_ERR,
							"Unknown attribute type '" + type + "' found in computed style.");
				}
			} else {
				throw new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Unknown attribute type '" + type + "' found in computed style.");
			}
		}
		return result;
	}

	/**
	 * Evaluates a top-level CSSOM {@code var()} value.
	 * 
	 * @param property
	 * @param value
	 * @return the evaluated CSSOM value.
	 */
	private StyleValue evaluateVarValue(String property, VarValue value) {
		String propertyName = value.getName();
		if (!isCustomPropertyName(propertyName)) {
			computedStyleError(property, value.getCssText(), "var() references non-custom property: " + propertyName);
			return null;
		}

		// Circularity checks
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<>();
		} else if (customPropertyStack.contains(propertyName)) {
			// Circularity found, report error
			computedStyleError(property, value.getCssText(), "var() dependency loop in " + propertyName);
			// Attempt to use the fallback
			LexicalUnit fallback = value.getFallback();
			if (fallback != null) {
				StyleValue custom;
				try {
					custom = customPropertyFallback(property, value, fallback);
				} catch (Exception e) {
					custom = null;
				}
				return custom;
			} else {
				return null;
			}
		}
		customPropertyStack.add(propertyName);

		//
		CSSPropertyDefinition definition = getOwnerSheet().getPropertyDefinition(propertyName);
		boolean inherited = definition == null || definition.inherits();
		CounterRef counter = new CounterRef();
		LexicalValue cpLexical;
		try {
			cpLexical = getCustomPropertyValue(propertyName, inherited, counter);
		} catch (Exception e) {
			computedStyleError(property, value.getCssText(), null, e);
			cpLexical = null;
		}
		StyleValue custom;
		try {
			if (cpLexical == null) {
				LexicalUnit fallback = value.getFallback();
				if (fallback == null) {
					if (definition == null) {
						// Unable to evaluate custom property
						customPropertyStack.remove(propertyName);
						return null;
					} else {
						fallback = definition.getInitialValue().getLexicalUnit();
					}
				}
				custom = customPropertyFallback(property, value, fallback);
			} else {
				custom = evaluateLexicalValue(property, cpLexical, counter);
				if (custom != null && custom.getCssValueType() == CssType.TYPED) {
					try {
						custom = absoluteValue(property, custom, false);
					} catch (DOMException e) {
						computedStyleError(property, custom.getCssText(), null, e);
						custom = null;
					}
				}
			}
		} catch (Exception e) {
			computedStyleError(property, value.getCssText(), null, e);
			custom = null;
		}
		customPropertyStack.remove(propertyName);
		//
		custom = enforceExpectIntegerFromProxy(property, value, custom);
		return custom;
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
	 * Evaluates the fallback of a top-level {@code var()} function.
	 * 
	 * @param property
	 * @param varValue
	 * @param fallback
	 * @return the fallback as a {@code CSSValue}.
	 * @throws DOMException
	 */
	private StyleValue customPropertyFallback(String property, ProxyValue varValue, LexicalUnit fallback)
			throws DOMException {
		StyleValue custom = getValueFactory().createCSSValue(fallback, this);
		if (custom != null) {
			// Check fallback for expecting integer.
			custom = enforceExpectIntegerFromProxy(property, varValue, custom);
		}
		return custom;
	}

	/**
	 * Create a CSSOM value from a lexical value, substituting any {@code VAR} and
	 * {@code ATTR} lexical units as necessary.
	 * 
	 * @param property
	 * @param lexval
	 * @param counter
	 * @return
	 */
	private StyleValue evaluateLexicalValue(String property, LexicalValue lexval, CounterRef counter) {
		if (customPropertyStack == null) {
			customPropertyStack = new LinkedList<>();
		}
		LexicalUnit lunit = lexval.getLexicalUnit().clone();
		LexicalUnit replUnit;
		try {
			replUnit = replaceLexicalProxy(property, lunit, counter);
		} catch (CSSResourceLimitException e) {
			throw e;
		} catch (DOMException e) {
			computedStyleError(property, lunit.toString(), "Problem evaluating lexical value.", e);
			return null;
		}
		//
		StyleValue result = null;
		if (replUnit != null) {
			try {
				result = getValueFactory().createCSSValue(replUnit, this);
			} catch (DOMException e) {
				LexicalValue repLexval = new LexicalValue();
				repLexval.setLexicalUnit(replUnit);
				result = repLexval;
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
	 * @param property
	 * @param lexval
	 * @param counter
	 * @return
	 * @throws DOMException
	 * @throws CSSResourceLimitException
	 */
	private LexicalUnit replaceLexicalProxy(String property, LexicalUnit lexval, CounterRef counter)
			throws DOMException {
		final int REPLACE_COUNT_LIMIT = 0x45000;

		LexicalUnit lu = lexval;
		do {
			if (lu.getLexicalUnitType() == LexicalType.VAR) {
				LexicalUnit newlu;
				LexicalUnit param = lu.getParameters();
				String propertyName = param.getStringValue(); // Property name
				param = param.getNextLexicalUnit(); // Comma?
				if (param != null) {
					param = param.getNextLexicalUnit(); // Fallback
				}

				// Circularity check
				if (customPropertyStack.contains(propertyName)) {
					// Attempt to use the fallback
					if (param != null) {
						computedStyleError(property, lexval.toString(),
								"Circularity evaluating lexical value.");
						// Replace param, just in case
						// But first, check for resource exhaustion...
						if (!counter.increment()) {
							throw new CSSResourceLimitException(
									"Resource limit hit while replacing custom property: "
											+ propertyName);
						}
						newlu = replaceLexicalProxy(property, param.clone(), counter);
					} else {
						throw new DOMException(DOMException.INVALID_ACCESS_ERR,
								"Circularity evaluating custom property " + propertyName);
					}
				} else {
					newlu = getCustomPropertyValueOrFallback(property, propertyName, param,
							counter);
					while (newlu != null && newlu.getLexicalUnitType() == LexicalType.VAR) {
						LexicalUnit newParam = newlu.getParameters();
						String replacedPropertyName = newParam.getStringValue();
						newParam = newParam.getNextLexicalUnit(); // Comma?
						if (newParam != null) {
							newParam = newParam.getNextLexicalUnit(); // Fallback
						}
						if (!counter.increment()) {
							throw new CSSResourceLimitException(
									"Resource limit hit while replacing custom property: "
											+ property);
						}
						newlu = getCustomPropertyValueOrFallback(property, replacedPropertyName,
								newParam, counter);
					}
				}

				boolean isLexval = lu == lexval;
				if (newlu == null) {
					lu = lu.remove();
					if (isLexval) {
						lexval = lu;
					}
					continue;
				}
				if (newlu.getLexicalUnitType() != LexicalType.EMPTY) {
					try {
						counter.replaceCounter += lu.countReplaceBy(newlu);
					} catch (CSSBudgetException e) {
						DOMException ex = new CSSResourceLimitException(
								"Resource limit hit while replacing custom property "
										+ propertyName);
						ex.initCause(e);
						throw ex;
					}
					lu = newlu;
					if (isLexval) {
						lexval = newlu;
					}
					if (counter.replaceCounter >= REPLACE_COUNT_LIMIT) {
						throw new CSSResourceLimitException(
								"Resource limit hit while replacing custom property "
										+ propertyName);
					}
				} else {
					lu = lu.remove();
					if (isLexval) {
						lexval = lu;
					}
				}
				continue;
			} else if (lu.getLexicalUnitType() == LexicalType.ATTR) {
				boolean isLexval = lu == lexval;
				LexicalUnit newlu = replacementAttrUnit(property, lu, counter);
				try {
					counter.replaceCounter += lu.countReplaceBy(newlu);
				} catch (CSSBudgetException e) {
					DOMException ex = new CSSResourceLimitException(
							"Resource limit hit while replacing attr() property " + property);
					ex.initCause(e);
					throw ex;
				}
				if (counter.replaceCounter >= REPLACE_COUNT_LIMIT) {
					throw new CSSResourceLimitException(
							"Resource limit hit while replacing attr() property " + property);
				}
				if (isLexval) {
					lexval = newlu;
				}
				lu = newlu;
				continue;
			} else {
				LexicalUnit param = lu.getParameters();
				if (param != null) {
					// Ignore return value (it is a parameter)
					replaceLexicalProxy(property, param, counter);
				} else if ((param = lu.getSubValues()) != null) {
					// Ignore return value (it is a sub-value)
					replaceLexicalProxy(property, param, counter);
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
	 * @param property
	 * @param customProperty
	 * @param fallbackLU
	 * @param counter
	 * @return
	 * @throws DOMException
	 */
	private LexicalUnit getCustomPropertyValueOrFallback(String property, String customProperty, LexicalUnit fallbackLU,
			CounterRef counter) throws DOMException {
		CSSPropertyDefinition definition = getOwnerSheet().getPropertyDefinition(customProperty);
		boolean inherited = definition == null || definition.inherits();
		//
		Exception exception = null;
		customPropertyStack.add(customProperty);
		try {
			LexicalValue custom = getCustomPropertyValue(customProperty, inherited, counter);
			if (custom != null) {
				LexicalUnit lu = custom.getLexicalUnit();
				customPropertyStack.remove(customProperty);
				if (counter.increment()) {
					return lu;
				} else {
					exception = new CSSResourceLimitException(
							"Resource limit hit while replacing custom property: " + customProperty);
				}
			}
		} catch (Exception e) {
			exception = e;
		}
		// Fallback
		if (fallbackLU != null) {
			// Replace param, just in case
			try {
				fallbackLU = replaceLexicalProxy(property, fallbackLU.clone(), counter);
				if (counter.increment()) {
					if (exception != null) {
						computedStyleError(property, null,
								"Circularity error evaluating custom property " + customProperty);
					}
					return fallbackLU;
				} else {
					throw new CSSResourceLimitException(
							"Resource limit hit while replacing custom property: " + customProperty);
				}
			} catch (CSSResourceLimitException e) {
				throw e;
			} catch (DOMException e) {
				exception = e;
			} finally {
				customPropertyStack.remove(customProperty);
			}
		}
		//
		customPropertyStack.remove(customProperty);
		//
		if (exception != null) {
			if (exception.getClass() == CSSResourceLimitException.class) {
				throw (CSSResourceLimitException) exception;
			} else {
				DOMException ex = new DOMException(DOMException.INVALID_ACCESS_ERR,
						"Unable to evaluate custom property " + customProperty);
				ex.initCause(exception);
				throw ex;
			}
		} else if (definition != null) {
			return definition.getInitialValue().getLexicalUnit();
		}
		return null;
	}

	private LexicalUnit replacementAttrUnit(String propertyName, LexicalUnit attr,
			CounterRef counter) throws DOMException {
		// Obtain attribute name and type (if set)
		LexicalUnit lu = attr.getParameters();
		if (lu.getLexicalUnitType() != LexicalType.IDENT) {
			computedStyleError(propertyName, attr.getCssText(),
					"Unexpected attribute name: " + lu.getCssText());
			return null;
		}
		String attrname = lu.getStringValue();
		String attrtype;
		lu = lu.getNextLexicalUnit();
		if (lu != null) {
			if (lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
				switch (lu.getLexicalUnitType()) {
				case IDENT:
					attrtype = lu.getStringValue().toLowerCase(Locale.ROOT);
					break;
				case OPERATOR_MOD:
					attrtype = "%";
					break;
				default:
					computedStyleError(propertyName, attr.getCssText(),
							"Unexpected attribute type: " + lu.getCssText());
					return null;
				}
				lu = lu.getNextLexicalUnit();
				if (lu != null) {
					if (lu.getLexicalUnitType() != LexicalType.OPERATOR_COMMA) {
						computedStyleError(propertyName, attr.getCssText(),
								"Expected comma, found: " + lu.getCssText());
						return null;
					}
					lu = lu.getNextLexicalUnit();
				}
			} else {
				lu = lu.getNextLexicalUnit();
				if (lu == null) {
					// Ending with comma is wrong syntax
					computedStyleError(propertyName, attr.getCssText(),
							"Unexpected end after comma.");
					return null;
				}
				attrtype = null;
			}
		} else {
			attrtype = null;
		}

		CSSElement owner = getOwnerNode();
		String attrvalue = owner.getAttribute(attrname);
		if (attrvalue.length() != 0) {
			if (isSafeAttrName(propertyName, owner, attrname)) {
				Parser parser = getStyleSheetFactory().createSACParser();
				if (attrtype == null || "string".equals(attrtype)) {
					String s = ParseHelper.quote(attrvalue, '"');
					LexicalUnit substValue;
					try {
						substValue = parser.parsePropertyValue(new StringReader(s));
					} catch (IOException e) {
						// This won't happen
						substValue = null;
					} catch (CSSParseException e) {
						// Possibly a budget error
						computedStyleError(propertyName, attr.getCssText(),
								"Unexpected error parsing: "
										+ s.substring(0, Math.min(s.length(), 255)),
								e);
						// Process fallback
						substValue = lu;
					}
					// No further processing required
					return safeReplaceLexicalAttr(propertyName, attrname, substValue, counter);
				}
				attrvalue = attrvalue.trim();
				if ("url".equals(attrtype)) {
					String s = "url(" + ParseHelper.quote(attrvalue, '"') + ')';
					LexicalUnit substValue;
					try {
						substValue = parser.parsePropertyValue(new StringReader(s));
					} catch (IOException e) {
						// This won't happen
						substValue = null;
					} catch (CSSParseException e) {
						// Possibly a budget error
						computedStyleError(propertyName, attr.getCssText(),
								"Unexpected error parsing: "
										+ s.substring(0, Math.min(s.length(), 255)),
								e);
						// Return fallback but preventing circular dependencies.
						substValue = safeReplaceLexicalAttr(propertyName, attrname, lu, counter);
					}
					return substValue;
				} else {
					// Let's see if the type is an actual type or an unit suffix
					if (attrtype.length() <= 2 || UnitStringToId
							.unitFromString(attrtype) != CSSUnit.CSS_OTHER) {
						attrvalue += attrtype;
					}
					LexicalUnit substValue;
					try {
						substValue = parser.parsePropertyValue(new StringReader(attrvalue));
					} catch (IOException e) {
						// This won't happen
						substValue = null;
					} catch (CSSParseException e) {
						computedStyleError(propertyName, attr.getCssText(),
								"Error parsing attribute '" + attrname + "', value: " + attrvalue,
								e);
						// Return fallback but preventing circular dependencies.
						return safeReplaceLexicalAttr(propertyName, attrname, lu, counter);
					}
					// Verify whether we got another proxy value.
					// Prevent circular dependencies.
					addAttrNameGuard(attrname);
					try {
						substValue = replaceLexicalProxy(propertyName, substValue, counter);
					} catch (Exception e) {
						computedStyleError(propertyName, attr.getCssText(), "Circularity: "
								+ attr.getCssText() + " references " + substValue.getCssText(), e);
						// Return fallback
						substValue = null;
					}
					if (substValue != null) {
						substValue = replaceLexicalProxy(propertyName, substValue, counter);
						removeAttrNameGuard(attrname);
						// Now check that the value is of the correct type.
						//
						// If the attribute type length is 1 or 2, type can only be a unit suffix
						// and there is no need to check.
						if (attrtype.length() > 2 && !unitMatchesAttrType(substValue, attrtype)) {
							substValue = null;
							computedStyleError(propertyName, attr.getCssText(),
									"Attribute value does not match type (" + attrtype + ").");
						} else {
							return substValue;
						}
					} else {
						// Fallback
						if (lu != null) {
							substValue = replaceLexicalProxy(propertyName, lu.clone(), counter);
						}
						removeAttrNameGuard(attrname);
						return substValue;
					}
				}
			} else {
				computedStyleError(propertyName, attr.getCssText(),
						"Unsafe attribute name: " + attrname);
			}
		}

		// Return fallback but preventing circular dependencies.
		return safeReplaceLexicalAttr(propertyName, attrname, lu, counter);
	}

	private LexicalUnit safeReplaceLexicalAttr(String propertyName, String attrname, LexicalUnit lu,
			CounterRef counter) {
		// Prevent circular dependencies.
		addAttrNameGuard(attrname);
		if (lu != null) {
			lu = replaceLexicalProxy(propertyName, lu.clone(), counter);
		}
		removeAttrNameGuard(attrname);
		return lu;
	}

	/**
	 * Determine whether the lexical unit is of the type given by the lower case
	 * attrtype.
	 * 
	 * @param lunit    the lexical unit to test.
	 * @param attrtype the attribute type (in lower case letters).
	 * @return true if the lexical unit is of the same type.
	 */
	private static boolean unitMatchesAttrType(LexicalUnit lunit, String attrtype) {
		int len = attrtype.length();
		if (len == 1) {
			return "%".equals(attrtype) && lunit.getCssUnit() == CSSUnit.CSS_PERCENTAGE;
		} else if (len == 2) {
			return attrtype.equalsIgnoreCase(lunit.getDimensionUnitText());
		}
		if ("ident".equalsIgnoreCase(attrtype)) {
			attrtype = "custom-ident";
		}
		CSSValueSyntax syn = SyntaxParser.createSimpleSyntax(attrtype);
		if (syn == null) {
			// Could be a 3-4 letter unit suffix, or an error
			return attrtype.equalsIgnoreCase(lunit.getDimensionUnitText());
		}
		return lunit.matches(syn) == Match.TRUE
				|| (lunit.getLexicalUnitType() == LexicalType.STRING && attrtype.equals("url"));
	}

	private StyleValue computeEnv(String propertyName, EnvVariableValue env) {
		if (getStyleDatabase() != null) {
			StyleValue envValue = (StyleValue) getStyleDatabase().getEnvValue(env.getName());
			if (envValue != null) {
				return envValue;
			}
		}
		StyleValue fallback = env.getFallback();
		if (fallback == null) {
			computedStyleError(propertyName, env.getCssText(), "Unable to evaluate env() value for: " + env.getName());
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
				reportFontSizeWarning(cssSize, "Inaccurate use of 'rem'.");
				sz = getInitialFontSize();
			} else {
				return cssSize;
			}
			sz *= factor;
			break;
		case CSSUnit.CSS_REX:
			factor = cssSize.getFloatValue(CSSUnit.CSS_REX);
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
			factor = cssSize.getFloatValue(CSSUnit.CSS_LH);
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
			factor = cssSize.getFloatValue(CSSUnit.CSS_RLH);
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
			factor = cssSize.getFloatValue(CSSUnit.CSS_CAP);
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
			factor = cssSize.getFloatValue(CSSUnit.CSS_CH);
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
			factor = cssSize.getFloatValue(CSSUnit.CSS_RCH);
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
			factor = cssSize.getFloatValue(CSSUnit.CSS_IC);
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
			factor = cssSize.getFloatValue(CSSUnit.CSS_RIC);
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
		protected CSSTypedValue absoluteTypedValue(CSSTypedValue partialValue) {
			return ComputedCSSStyle.this.absoluteTypedValue(propertyName, (TypedValue) partialValue, false);
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
		protected CSSTypedValue absoluteTypedValue(CSSTypedValue partialValue) {
			return ComputedCSSStyle.this.absoluteTypedValue(propertyName, (TypedValue) partialValue, true);
		}

		@Override
		protected StyleValue absoluteProxyValue(CSSPrimitiveValue partialValue) {
			return ComputedCSSStyle.this.replaceProxyValue(propertyName, partialValue);
		}

		@Override
		protected float percentage(CSSTypedValue value, short resultType) throws DOMException {
			float pcnt = value.getFloatValue(CSSUnit.CSS_PERCENTAGE);
			// Use parent element's size.
			return getParentElementFontSize() * pcnt * 0.01f;
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
