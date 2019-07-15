/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

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
import io.sf.carte.doc.style.css.BoxValues;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleDatabaseRequiredException;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.property.AbstractCSSExpression;
import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.CSSIdentifierValue;
import io.sf.carte.doc.style.css.property.CSSInheritValue;
import io.sf.carte.doc.style.css.property.CSSNumberValue;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.CSSURIValue;
import io.sf.carte.doc.style.css.property.CSSURIValueWrapper;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.ExpressionContainerValue;
import io.sf.carte.doc.style.css.property.LinkedCSSValueList;
import io.sf.carte.doc.style.css.property.FunctionValue;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.doc.style.css.property.OperandExpression;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
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
					value = new CSSURIValueWrapper((CSSURIValue) value, hrefcontext,
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
				value = CSSInheritValue.getValue();
			} else {
				value = defaultPropertyValue(property, propertydb);
			}
		}
		CSSComputedProperties ancStyle = this;
		/*
		 * We compute inherited value, if appropriate.
		 */
		while (value == null ? inherited : value.getCssValueType() == CSSValue.CSS_INHERIT) {
			ancStyle = ancStyle.getParentComputedStyle();
			if (ancStyle == null) {
				break;
			}
			value = (AbstractCSSValue) ancStyle.getPropertyCSSValue(property);
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
				CSSPrimitiveValue prival = (CSSPrimitiveValue) value;
				if (prival.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
					String s = prival.getStringValue().toLowerCase(Locale.US);
					if ("currentcolor".equals(s)) {
						if(!"color".equals(property)) {
							value = getCSSColor();
						}
					} else {
						String spec;
						if ("transparent".equals(s)) {
							spec = "rgba(0,0,0/0)";
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
			// Check for custom properties ('variables')
			if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue2.CSS_CUSTOM_PROPERTY) {
				value = getCSSValue(((CSSPrimitiveValue) value).getStringValue());
			} else if (!property.equals("font-size")) {
				// Convert to absolute units
				value = absoluteValue(value);
			}
		}
		return value;
	}

	private boolean isCSSIdentifier(CSSValue value, String ident) {
		return value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
				&& ident.equalsIgnoreCase(value.getCssText());
	}

	private AbstractCSSValue absoluteValue(AbstractCSSValue value) {
		short type = value.getCssValueType();
		if (type == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			int lstlen = list.getLength();
			for (int i = 0; i < lstlen; i++) {
				list.set(i, absoluteValue(list.item(i)));
			}
		} else if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
			value = absolutePrimitiveValue((AbstractCSSPrimitiveValue) value);
		}
		return value;
	}

	private AbstractCSSPrimitiveValue absolutePrimitiveValue(AbstractCSSPrimitiveValue pri) {
		if (isRelativeUnit(pri)) {
			pri = absoluteNumberValue((CSSNumberValue) pri);
		} else {
			short type = pri.getPrimitiveType();
			if (type == CSSPrimitiveValue2.CSS_EXPRESSION) {
				pri = pri.clone();
				AbstractCSSExpression expr = ((ExpressionContainerValue) pri).getExpression();
				absoluteExpressionValue(expr);
			} else if (type == CSSPrimitiveValue2.CSS_FUNCTION) {
				pri = pri.clone();
				LinkedCSSValueList args = ((FunctionValue) pri).getArguments();
				int sz = args.size();
				for (int i = 0; i < sz; i++) {
					args.set(i, absoluteValue(args.get(i)));
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
			return pri instanceof CSSNumberValue;
		}
		return false;
	}

	private CSSNumberValue absoluteNumberValue(CSSNumberValue value) {
		short unit = value.getPrimitiveType();
		float fv = value.getFloatValue(unit);
		if (unit == CSSPrimitiveValue.CSS_EMS) {
			value = new CSSNumberValue();
			value.setFloatValuePt(fv * getComputedFontSize());
		} else if (unit == CSSPrimitiveValue.CSS_EXS) {
			if (getStyleDatabase() != null) {
				fv *= getStyleDatabase().getExSizeInPt(getUsedFontFamily(), getComputedFontSize());
			} else {
				fv *= getComputedFontSize() * 0.5f;
			}
			value = new CSSNumberValue();
			value.setFloatValuePt(fv);
		} else if (unit == CSSPrimitiveValue2.CSS_REM) {
			CSSDocument doc = (CSSDocument) getOwnerNode().getOwnerDocument();
			fv *= doc.getStyleSheet().getComputedStyle(doc.getDocumentElement(), null).getComputedFontSize();
			value = new CSSNumberValue();
			value.setFloatValuePt(fv);
		} else if (unit == CSSPrimitiveValue2.CSS_LH) {
			fv *= getComputedLineHeight();
			value = new CSSNumberValue();
			value.setFloatValuePt(fv);
		} else if (unit == CSSPrimitiveValue2.CSS_RLH) {
			CSSDocument doc = (CSSDocument) getOwnerNode().getOwnerDocument();
			fv *= doc.getStyleSheet().getComputedStyle(doc.getDocumentElement(), null).getComputedLineHeight();
			value = new CSSNumberValue();
			value.setFloatValuePt(fv);
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
			}
			value = new CSSNumberValue();
			value.setFloatValuePt(fv);
		}
		return value;
	}

	private void absoluteExpressionValue(CSSExpression expr) {
		switch (expr.getPartType()) {
		case SUM:
		case PRODUCT:
			List<? extends CSSExpression> operands = ((CSSExpression.AlgebraicExpression) expr).getOperands();
			Iterator<? extends CSSExpression> it = operands.iterator();
			while (it.hasNext()) {
				absoluteExpressionValue(it.next());
			}
			break;
		case OPERAND:
			OperandExpression operand = (OperandExpression) expr;
			AbstractCSSPrimitiveValue primi = (AbstractCSSPrimitiveValue) operand.getOperand();
			operand.setOperand(absolutePrimitiveValue(primi));
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
			return new CSSIdentifierValue("table");
		} else if ("inline".equals(display) || "run-in".equals(display) || "table-row-group".equals(display)
				|| "table-column".equals(display) || "table-column-group".equals(display)
				|| "table-header-group".equals(display) || "table-footer-group".equals(display)
				|| "table-row".equals(display) || "table-cell".equals(display) || "table-caption".equals(display)
				|| "inline-block".equals(display)) {
			return new CSSIdentifierValue("block");
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
			list.add(new CSSIdentifierValue("no-repeat"));
			list.add(new CSSIdentifierValue("repeat"));
		} else if (s.equals("repeat-x")) {
			list.add(new CSSIdentifierValue("repeat"));
			list.add(new CSSIdentifierValue("no-repeat"));
		} else if (s.equals("repeat")) {
			list.add(new CSSIdentifierValue("repeat"));
			list.add(new CSSIdentifierValue("repeat"));
		} else if (s.equals("no-repeat")) {
			list.add(new CSSIdentifierValue("no-repeat"));
			list.add(new CSSIdentifierValue("no-repeat"));
		} else if (s.equals("space")) {
			list.add(new CSSIdentifierValue("space"));
			list.add(new CSSIdentifierValue("space"));
		} else if (s.equals("round")) {
			list.add(new CSSIdentifierValue("round"));
			list.add(new CSSIdentifierValue("round"));
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
					getParentRule().getStyleDeclarationErrorHandler().malformedURIValue(baseHref);
				}
			} else {
				try {
					baseUrl = new URL(baseHref);
				} catch (MalformedURLException e) {
					getParentRule().getStyleDeclarationErrorHandler().malformedURIValue(baseHref);
				}
			}
			if (baseUrl != null) {
				try {
					URL url = new URL(baseUrl, href);
					href = url.toExternalForm();
				} catch (MalformedURLException e) {
					getParentRule().getStyleDeclarationErrorHandler().malformedURIValue(href);
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
	 * May require a style database to work.
	 * </p>
	 * 
	 * @return the value of the font-size property, in typographic points.
	 */
	@Override
	public int getComputedFontSize() {
		CSSPrimitiveValue cssSize = (CSSPrimitiveValue) getCSSValue("font-size");
		int sz;
		if (getStyleDatabase() != null) {
			sz = getFontSizeFromIdentifier(null, "medium");
		} else {
			sz = 3;
		}
		if (cssSize == null) {
			return sz;
		}
		switch (cssSize.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_EMS:
			float factor = cssSize.getFloatValue(CSSPrimitiveValue.CSS_EMS);
			// Use parent element's size.
			sz = Math.round(getParentElementFontSize() * factor);
			break;
		case CSSPrimitiveValue.CSS_EXS:
			factor = cssSize.getFloatValue(CSSPrimitiveValue.CSS_EXS);
			// Use parent element's size.
			CSSComputedProperties parentStyle = getParentComputedStyle();
			if (parentStyle == null) {
				sz = Math.round(getFontSizeFromIdentifier(null, "medium") * 0.5f * factor);
			} else {
				if (getStyleDatabase() != null) {
					sz = Math.round(getStyleDatabase().getExSizeInPt(parentStyle.getUsedFontFamily(),
							parentStyle.getComputedFontSize()) * factor);
				} else {
					sz = Math.round(parentStyle.getComputedFontSize() * 0.5f * factor);
				}
			}
			break;
		case CSSPrimitiveValue.CSS_IDENT:
			// Conversion to lower case is probably not needed
			String sizeIdentifier = cssSize.getStringValue().toLowerCase(Locale.US);
			try {
				// relative size: larger, smaller.
				if ("larger".equals(sizeIdentifier)) {
					sz = getLargerFontSize(sz);
				} else if ("smaller".equals(sizeIdentifier)) {
					sz = getSmallerFontSize(sz);
				} else {
					sz = getFontSizeFromIdentifier(null, sizeIdentifier);
				}
			} catch (DOMException e) {
				if (getParentRule() != null) {
					getParentRule().getStyleDeclarationErrorHandler().unknownIdentifier("font-size", sizeIdentifier);
				} else {
					((CSSDocument) getOwnerNode().getOwnerDocument()).getStyleSheet().getErrorHandler()
							.computedStyleError(getOwnerNode(), "font-size", sizeIdentifier, "Unknown identifier");
				}
			}
			break;
		case CSSPrimitiveValue.CSS_PERCENTAGE:
			float pcnt = cssSize.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE);
			// Use parent element's size.
			sz = Math.round(getParentElementFontSize() * pcnt / 100f);
			break;
		case CSSPrimitiveValue.CSS_PT:
			sz = (int) cssSize.getFloatValue(CSSPrimitiveValue.CSS_PT);
			break;
		}
		return sz;
	}

	private int getFontSizeFromIdentifier(String familyName, String sizeIdentifier) {
		if (getStyleDatabase() != null) {
			return getStyleDatabase().getFontSizeFromIdentifier(familyName, sizeIdentifier);
		} else {
			int sz;
			if ("medium".equals(sizeIdentifier)) {
				sz = 3;
			} else if ("large".equals(sizeIdentifier)) {
				sz = 4;
			} else if ("small".equals(sizeIdentifier)) {
				sz = 2;
			} else if ("x-large".equals(sizeIdentifier)) {
				sz = 5;
			} else if ("x-small".equals(sizeIdentifier)) {
				sz = 2;
			} else if ("xx-small".equals(sizeIdentifier)) {
				sz = 1;
			} else if ("xx-large".equals(sizeIdentifier)) {
				sz = 6;
			} else {
				sz = 3; // default
			}
			return sz;
		}
	}

	protected int getLargerFontSize(int defaultSize) {
		float sz = defaultSize * 1.2f;
		ComputedCSSStyle parentCss = (ComputedCSSStyle) getParentComputedStyle();
		if (parentCss != null) {
			CSSPrimitiveValue csssize = (CSSPrimitiveValue) parentCss.getCSSValue("font-size");
			if (csssize != null) {
				switch (csssize.getPrimitiveType()) {
				case CSSPrimitiveValue.CSS_IDENT:
					String baseFontSize = csssize.getStringValue();
					if (baseFontSize.equals("xx-small")) {
						sz = getFontSizeFromIdentifier(null, "x-small");
					} else if (baseFontSize.equals("x-small")) {
						sz = getFontSizeFromIdentifier(null, "small");
					} else if (baseFontSize.equals("small")) {
						sz = getFontSizeFromIdentifier(null, "medium");
					} else if (baseFontSize.equals("medium")) {
						sz = getFontSizeFromIdentifier(null, "large");
					} else if (baseFontSize.equals("large")) {
						sz = getFontSizeFromIdentifier(null, "x-large");
					} else if (baseFontSize.equals("x-large")) {
						sz = getFontSizeFromIdentifier(null, "xx-large");
					} else if (baseFontSize.equals("xx-large")) {
						sz = 2f * getFontSizeFromIdentifier(null, "xx-large")
								- getFontSizeFromIdentifier(null, "x-large");
					} else {
						if (getParentRule() != null) {
							getParentRule().getStyleDeclarationErrorHandler().unknownIdentifier("font-size",
									baseFontSize);
						} else {
							((CSSDocument) getOwnerNode().getOwnerDocument()).getStyleSheet().getErrorHandler()
									.computedStyleError(getOwnerNode(), "font-size", baseFontSize, "Unknown identifier");
						}
					}
					break;
				default:
					sz = parentCss.getComputedFontSize() * 1.2f;
				}
			}
		}
		return Math.round(sz);
	}

	protected int getSmallerFontSize(int defaultSize) {
		float sz = defaultSize * 0.82f;
		ComputedCSSStyle parentCss = (ComputedCSSStyle) getParentComputedStyle();
		if (parentCss != null) {
			CSSPrimitiveValue csssize = (CSSPrimitiveValue) parentCss.getCSSValue("font-size");
			if (csssize != null) {
				switch (csssize.getPrimitiveType()) {
				case CSSPrimitiveValue.CSS_IDENT:
					String baseFontSize = csssize.getStringValue();
					if (baseFontSize.equals("xx-small")) {
						sz = 2f * getFontSizeFromIdentifier(null, "xx-small")
								- getFontSizeFromIdentifier(null, "x-small");
						// Safety check
						if (sz < 0.1f) {
							sz = getFontSizeFromIdentifier(null, "xx-small");
						}
					} else if (baseFontSize.equals("x-small")) {
						sz = getFontSizeFromIdentifier(null, "xx-small");
					} else if (baseFontSize.equals("small")) {
						sz = getFontSizeFromIdentifier(null, "x-small");
					} else if (baseFontSize.equals("medium")) {
						sz = getFontSizeFromIdentifier(null, "small");
					} else if (baseFontSize.equals("large")) {
						sz = getFontSizeFromIdentifier(null, "medium");
					} else if (baseFontSize.equals("x-large")) {
						sz = getFontSizeFromIdentifier(null, "large");
					} else if (baseFontSize.equals("xx-large")) {
						sz = getFontSizeFromIdentifier(null, "x-large");
					} else {
						if (getParentRule() != null) {
							getParentRule().getStyleDeclarationErrorHandler().unknownIdentifier("font-size",
									baseFontSize);
						} else {
							((CSSDocument) getOwnerNode().getOwnerDocument()).getStyleSheet().getErrorHandler()
									.computedStyleError(getOwnerNode(), "font-size", baseFontSize, "Unknown identifier");
						}
					}
					break;
				default:
					sz = parentCss.getComputedFontSize() * 0.82f;
				}
			}
		}
		return Math.round(sz);
	}

	private int getParentElementFontSize() {
		int sz;
		CSSComputedProperties parentCss = getParentComputedStyle();
		if (parentCss != null) {
			sz = parentCss.getComputedFontSize();
		} else {
			sz = getFontSizeFromIdentifier(null, "medium");
		}
		return sz;
	}

	protected boolean isFontFamilyAvailable(String requestedFamily) {
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
	 * @param defval
	 *            the default value in EMs.
	 * @return the computed line height, or the default value if the computed
	 *         value could not be found.
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
				if (getParentRule() != null) {
					CSSPropertyValueException e = new CSSPropertyValueException(
							"Expected 'normal', found " + cssval.getStringValue());
					getParentRule().getStyleDeclarationErrorHandler().wrongValue("line-height", e);
				} else {
					((CSSDocument) getOwnerNode().getOwnerDocument()).getStyleSheet().getErrorHandler()
							.computedStyleError(getOwnerNode(), "line-height", cssval.getStringValue(),
									"Wrong value: expected 'normal'");
				}
			}
			height = defval * getComputedFontSize();
		} else if (cssval instanceof CSSNumberValue) {
			height = cssval.getFloatValue(declType);
			if (declType != CSSPrimitiveValue.CSS_PT) {
				height = CSSNumberValue.floatValueConversion(height, declType, CSSPrimitiveValue.CSS_PT);
			}
		} else {
			if (getParentRule() != null) {
				CSSPropertyValueException e = new CSSPropertyValueException(
						"Expected number or identifier, found " + cssval.getCssText());
				getParentRule().getStyleDeclarationErrorHandler().wrongValue("line-height", e);
			} else {
				((CSSDocument) getOwnerNode().getOwnerDocument()).getStyleSheet().getErrorHandler().computedStyleError(
						getOwnerNode(), "line-height", cssval.getStringValue(), "Wrong value: expected number or identifier");
			}
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
	abstract public CSSComputedProperties getParentComputedStyle();

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
