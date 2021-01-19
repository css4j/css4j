/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSS2Properties;

import io.sf.carte.doc.style.css.AlgebraicExpression;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSExpression;
import io.sf.carte.doc.style.css.CSSExpressionValue;
import io.sf.carte.doc.style.css.CSSFunctionValue;
import io.sf.carte.doc.style.css.CSSOperandExpression;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.CSSValueList;
import io.sf.carte.doc.style.css.NodeStyleDeclaration;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ColorIdentifiers;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.doc.style.css.property.StringValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.SystemDefaultValue;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.Diff;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS Style Declaration.
 *
 */
public class BaseCSSStyleDeclaration extends AbstractCSSStyleDeclaration implements CSS2Properties, Cloneable {

	private static final long serialVersionUID = 1L;

	/**
	 * The rule that contains this declaration block, if any.
	 */
	private final BaseCSSDeclarationRule parentRule;

	private HashMap<String, StyleValue> propValue;

	private ArrayList<String> propertyList;

	private ArrayList<String> priorities;

	private LinkedList<String> shorthandSet;

	/**
	 * Constructor with parent CSS rule argument.
	 * 
	 * @param parentRule
	 *            the parent CSS rule.
	 */
	protected BaseCSSStyleDeclaration(BaseCSSDeclarationRule parentRule) {
		super();
		this.parentRule = parentRule;
		propValue = new HashMap<String, StyleValue>();
		propertyList = new ArrayList<String>();
		priorities = new ArrayList<String>();
		shorthandSet = new LinkedList<String>();
	}

	public BaseCSSStyleDeclaration() {
		super();
		this.parentRule = null;
		propValue = new HashMap<String, StyleValue>();
		propertyList = new ArrayList<String>();
		priorities = new ArrayList<String>();
		shorthandSet = new LinkedList<String>();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param copiedObject
	 *            the BaseCSSStyleDeclaration to be copied.
	 */
	protected BaseCSSStyleDeclaration(BaseCSSStyleDeclaration copiedObject) {
		super();
		this.parentRule = copiedObject.getParentRule();
		setProperties(copiedObject);
	}

	@SuppressWarnings("unchecked")
	void setProperties(BaseCSSStyleDeclaration other) {
		priorities = (ArrayList<String>) other.priorities.clone();
		propertyList = (ArrayList<String>) other.propertyList.clone();
		propValue = deepClone(other.propValue);
		shorthandSet = (LinkedList<String>) other.shorthandSet.clone();
	}

	private HashMap<String, StyleValue> deepClone(HashMap<String, StyleValue> cloneFrom) {
		HashMap<String, StyleValue> propValue = new HashMap<String, StyleValue>(cloneFrom.size());
		Iterator<Entry<String, StyleValue>> it = cloneFrom.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, StyleValue> entry = it.next();
			StyleValue value = entry.getValue();
			if (value.getCssValueType() == CssType.SHORTHAND) {
				value = value.clone();
			}
			propValue.put(entry.getKey(), value);
		}
		return propValue;
	}

	@Override
	public String getMinifiedCssText() {
		LinkedList<String> unusedShorthands = new LinkedList<String>(shorthandSet);
		int sz = propertyList.size();
		StringBuilder sb = new StringBuilder(50 + sz * 10);
		for (int i = 0; i < sz; i++) {
			String ptyname = propertyList.get(i);
			String prio = priorities.get(i);
			boolean important = prio != null && "important".equals(prio);
			StyleValue cssVal = propValue.get(ptyname);
			CssType type = cssVal.getCssValueType();
			// Verify if the property is a subproperty of a previously set
			// shorthand
			if (type != CssType.SHORTHAND) {
				if (cssVal.isSubproperty()) {
					Iterator<String> it = unusedShorthands.iterator();
					while (it.hasNext()) {
						String sh = it.next();
						ShorthandValue shval = (ShorthandValue) propValue.get(sh);
						if (shval.isSetSubproperty(ptyname)) {
							if (important == shval.isImportant()) {
								it.remove();
								appendShorthandMinifiedCssText(sb, sh, shval);
							}
						}
					}
					continue;
				}
			}
			// No subproperty of already printed shorthand, print it.
			cssVal = getCSSValue(ptyname);
			appendLonghandMinifiedCssText(sb, ptyname, cssVal, important);
			if (i != sz - 1) {
				sb.append(';');
			}
		}
		return sb.toString();
	}

	protected void appendShorthandMinifiedCssText(StringBuilder sb, String shorthandName, ShorthandValue shval) {
		sb.append(shorthandName).append(':').append(shval.getMinifiedCssText(shorthandName));
		if (shval.isImportant()) {
			sb.append("!important");
		}
		sb.append(';');
	}

	protected void appendLonghandMinifiedCssText(StringBuilder sb, String ptyname, StyleValue cssVal,
			boolean important) {
		sb.append(ptyname).append(':').append(cssVal.getMinifiedCssText(ptyname));
		if (important) {
			sb.append("!important");
		}
	}

	/**
	 * Retrieves the parsable textual representation of the declaration block (excluding the
	 * surrounding curly braces).
	 * 
	 * @return the textual representation of the declaration block.
	 * @exception DOMException
	 *                SYNTAX_ERR: Raised if the specified CSS string value has a syntax error
	 *                and is unparsable. <br>
	 */
	@Override
	public String getCssText() {
		StyleFormattingContext context = null;
		BaseCSSDeclarationRule prule = getParentRule();
		if (prule != null) {
			AbstractCSSStyleSheet sheet = prule.getParentStyleSheet();
			if (sheet != null) {
				context = sheet.getStyleSheetFactory().getStyleFormattingFactory().createStyleFormattingContext();
			}
		}
		if (context == null) {
			context = new DefaultStyleFormattingContext();
		}
		BufferSimpleWriter sw = new BufferSimpleWriter(50 + propertyList.size() * 16);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		LinkedList<String> unusedShorthands = new LinkedList<String>(shorthandSet);
		int sz = propertyList.size();
		for (int i = 0; i < sz; i++) {
			String ptyname = propertyList.get(i);
			String prio = priorities.get(i);
			boolean important = prio != null && "important".equals(prio);
			CSSValue cssVal = propValue.get(ptyname);
			CssType type = cssVal.getCssValueType();
			// Verify if the property is a subproperty of a previously set
			// shorthand
			if (type != CssType.SHORTHAND) {
				if (((StyleValue) cssVal).isSubproperty()) {
					Iterator<String> it = unusedShorthands.iterator();
					while (it.hasNext()) {
						String sh = it.next();
						ShorthandValue shval = (ShorthandValue) propValue.get(sh);
						if (shval.isSetSubproperty(ptyname)) {
							if (important == shval.isImportant()) {
								it.remove();
								writeShorthandCssText(wri, context, sh, shval);
							}
						}
					}
					continue;
				}
			}
			// No subproperty of already printed shorthand, print it.
			StyleValue ptyvalue = getCSSValue(ptyname);
			writeLonghandCssText(wri, context, ptyname, ptyvalue, important);
		}
	}

	protected void writeShorthandCssText(SimpleWriter wri, StyleFormattingContext context, String shorthandName,
			ShorthandValue shval) throws IOException {
		context.startPropertyDeclaration(wri);
		wri.write(shorthandName);
		context.writeColon(wri);
		context.writeShorthandValue(wri, shorthandName, shval);
		if (shval.isImportant()) {
			context.writeImportantPriority(wri);
		}
		context.writeSemiColon(wri);
		context.endPropertyDeclaration(wri);
	}

	protected void writeLonghandCssText(SimpleWriter wri, StyleFormattingContext context, String ptyname,
			StyleValue ptyvalue, boolean important) throws IOException {
		context.startPropertyDeclaration(wri);
		wri.write(ptyname);
		context.writeColon(wri);
		context.writeValue(wri, ptyname, ptyvalue);
		if (important) {
			context.writeImportantPriority(wri);
		}
		context.writeSemiColon(wri);
		context.endPropertyDeclaration(wri);
	}

	String getComputedPlainCssText() {
		int sz = propertyList.size();
		StringBuilder sb = new StringBuilder(50 + sz * 18);
		for (int i = 0; i < sz; i++) {
			String ptyname = propertyList.get(i);
			String prio = priorities.get(i);
			StyleValue value = getCSSValue(ptyname);
			sb.append(ptyname).append(':').append(' ');
			appendCssText(sb, value);
			if (prio != null && "important".equals(prio)) {
				sb.append(" ! important");
			}
			sb.append(';').append(' ');
		}
		return sb.toString();
	}

	void writeComputedCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		int sz = propertyList.size();
		for (int i = 0; i < sz; i++) {
			String ptyname = propertyList.get(i);
			String prio = priorities.get(i);
			wri.write(ptyname);
			wri.write(':');
			wri.write(' ');
			context.writeValue(wri, ptyname, getCSSValue(ptyname));
			if (prio != null && "important".equals(prio)) {
				context.writeImportantPriority(wri);
			}
			wri.write(';');
			wri.write(' ');
		}
	}

	String getOptimizedCssText() {
		int sz = propertyList.size();
		ArrayList<String> ptyList = new ArrayList<String>(sz);
		HashSet<String> prioSet = new HashSet<String>(sz);
		HashMap<String, ShorthandBuilder> builders = new HashMap<String, ShorthandBuilder>();
		ShorthandDatabase sdb = ShorthandDatabase.getInstance();
		StringBuilder sb = new StringBuilder(50 + sz * 24);
		for (int i = 0; i < sz; i++) {
			String ptyname = propertyList.get(i);
			String shorthand = sdb.getShorthand(ptyname);
			String prio = priorities.get(i);
			boolean isimportant = prio != null && "important".equals(prio);
			if (shorthand != null) {
				// Is a shorthand subproperty
				// Get topmost shorthand
				String topsh = sdb.getShorthand(shorthand);
				if (topsh != null) {
					shorthand = topsh;
				}
				String prefShorthand = '&' + shorthand;
				ShorthandBuilder builder = builders.get(prefShorthand);
				if (builder == null) {
					builder = createBuilder(shorthand);
					if (builder == null) {
						ptyList.add(ptyname);
						if (isimportant) {
							prioSet.add(ptyname);
						}
						continue;
					}
					builders.put(prefShorthand, builder);
					ptyList.add(prefShorthand);
				}
				builder.addAssignedProperty(ptyname, isimportant);
			} else {
				ptyList.add(ptyname);
				if (isimportant) {
					prioSet.add(ptyname);
				}
			}
		}
		// 'border-image' is already handled by 'border'
		if (ptyList.contains("&border-image") && ptyList.contains("&border")) {
			ptyList.remove("&border-image");
			builders.remove("&border-image");
		}
		// 'font-variant' is already handled by 'font'
		if (ptyList.contains("&font-variant") && ptyList.contains("&font")) {
			ptyList.remove("&font-variant");
			builders.remove("&font-variant");
		}
		// if 'grid-row' and 'grid-column' are present, can be handled by 'grid-area'
		if (ptyList.contains("&grid-row") && ptyList.contains("&grid-column")) {
			ptyList.remove("&grid-row");
			ptyList.remove("&grid-column");
			if (!ptyList.contains("&grid-area")) {
				ptyList.add("&grid-area");
				builders.put("&grid-area", createBuilder("grid-area"));
			}
			builders.remove("&grid-row");
			builders.remove("&grid-column");
			// 'grid-row' is handled by 'grid-area'
		} else if (ptyList.contains("&grid-row") && ptyList.contains("&grid-area")) {
			ptyList.remove("&grid-row");
			builders.remove("&grid-row");
			// 'grid-column' is handled by 'grid-area'
		} else if (ptyList.contains("&grid-column") && ptyList.contains("&grid-area")) {
			ptyList.remove("&grid-column");
			builders.remove("&grid-column");
		}
		// Iterate for all the properties
		sz = ptyList.size();
		for (int i = 0; i < sz; i++) {
			String ptyname = ptyList.get(i);
			if (ptyname.charAt(0) == '&') {
				builders.get(ptyname).appendMinifiedCssText(sb);
			} else {
				StyleValue value = getCSSValue(ptyname);
				sb.append(ptyname).append(':');
				appendMinifiedCssText(sb, value, ptyname);
				if (prioSet.contains(ptyname)) {
					sb.append("!important");
				}
				sb.append(';');
			}
		}
		return sb.toString();
	}

	static void appendCssText(StringBuilder buf, StyleValue value) {
		String text;
		if (!value.isSystemDefault()) {
			text = value.getCssText();
		} else {
			text = "initial";
		}
		buf.append(text);
	}

	static void appendMinifiedCssText(StringBuilder buf, StyleValue value, String ptyname) {
		String text;
		if (!value.isSystemDefault()) {
			text = value.getMinifiedCssText(ptyname);
		} else {
			text = "initial";
		}
		buf.append(text);
	}

	ShorthandBuilder createBuilder(String shorthand) {
		if ("border".equals(shorthand)) {
			return new BorderBuilder(this);
		} else if ("background".equals(shorthand)) {
			return new BackgroundBuilder(this);
		} else if ("border-image".equals(shorthand)) {
			return new BorderImageBuilder(this);
		} else if ("margin".equals(shorthand)) {
			return new MarginBuilder(this);
		} else if ("padding".equals(shorthand)) {
			return new PaddingBuilder(this);
		} else if ("font".equals(shorthand)) {
			return new FontBuilder(this);
		} else if ("font-variant".equals(shorthand)) {
			return new FontVariantBuilder(this);
		} else if ("border-radius".equals(shorthand)) {
			return new BorderRadiusBuilder(this);
		} else if ("list-style".equals(shorthand)) {
			return new ListStyleShorthandBuilder(this);
		} else if ("text-decoration".equals(shorthand) || "outline".equals(shorthand)
				|| "text-emphasis".equals(shorthand) || "column-rule".equals(shorthand)) {
			return new GenericShorthandBuilder(shorthand, this, "none");
		} else if ("flex".equals(shorthand)) {
			return new FlexShorthandBuilder(this);
		} else if ("flex-flow".equals(shorthand)) {
			return new GenericShorthandBuilder(shorthand, this, "row");
		} else if ("columns".equals(shorthand)) {
			return new GenericShorthandBuilder(shorthand, this, "auto");
		} else if ("grid-column".equals(shorthand) || "grid-row".equals(shorthand)) {
			return new GridPlacementShorthandBuilder(shorthand, this);
		} else if ("grid-area".equals(shorthand)) {
			return new GridAreaShorthandBuilder(this);
		} else if ("grid".equals(shorthand)) {
			return new GridShorthandBuilder(this);
		} else if ("animation".equals(shorthand)) {
			return new AnimationShorthandBuilder(this);
		} else if ("transition".equals(shorthand)) {
			return new TransitionShorthandBuilder(this);
		} else if ("cue".equals(shorthand) || "pause".equals(shorthand) || "rest".equals(shorthand)) {
			return new SequenceShorthandBuilder(shorthand, this);
		} else if ("place-content".equals(shorthand) || "place-items".equals(shorthand)
				|| "place-self".equals(shorthand) || "gap".equals(shorthand)) {
			return new OrderedTwoValueShorthandBuilder(shorthand, this, "normal");
		}
		return null;
	}

	@Override
	public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		CSSDeclarationRule prule = getParentRule();
		if (prule != null) {
			return prule.getStyleDeclarationErrorHandler();
		}
		return null;
	}

	/**
	 * Parses the given value and resets all the properties in the declaration block,
	 * including the removal or addition of properties.
	 * 
	 * @param cssText
	 *            the text with the style declaration.
	 * @throws DOMException
	 *             NOT_SUPPORTED_ERR: if the system was unable to instantiate parser.
	 */
	@Override
	public void setCssText(String cssText) throws DOMException {
		// The following may cause a DOMException.NOT_SUPPORTED_ERR
		// (documented above) but the W3C API does not allow for that.
		Parser parser = createSACParser();
		StyleDeclarationHandler handler = new StyleDeclarationHandler();
		parser.setErrorHandler(handler);
		Reader re = new StringReader(cssText);
		clear();
		handler.setLexicalPropertyListener(this);
		parser.setDocumentHandler(handler);
		try {
			parser.parseStyleDeclaration(re);
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
	}

	private Parser createSACParser() throws DOMException {
		Parser parser;
		AbstractCSSStyleSheetFactory factory = getStyleSheetFactory();
		if (factory != null) {
			parser = factory.createSACParser();
		} else {
			parser = new CSSOMParser();
		}
		return parser;
	}

	@Override
	protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
		BaseCSSDeclarationRule prule = getParentRule();
		if (prule != null) {
			AbstractCSSStyleSheet sheet = prule.getParentStyleSheet();
			if (sheet != null) {
				return sheet.getStyleSheetFactory();
			}
		}
		return null;
	}

	protected ValueFactory getValueFactory() {
		AbstractCSSStyleSheetFactory factory = getStyleSheetFactory();
		return factory != null ? factory.getValueFactory() : new ValueFactory();
	}

	@Override
	public String getPropertyValue(String propertyName) {
		propertyName = getCanonicalPropertyName(propertyName);
		StyleValue value = getCSSValue(propertyName);
		if (value != null) {
			CssType type = value.getCssValueType();
			if (type == CssType.TYPED) {
				Type ptype = value.getPrimitiveType();
				if (ptype == Type.STRING || ptype == Type.IDENT) {
					return ((CSSTypedValue) value).getStringValue();
				}
			} else if (type == CssType.SHORTHAND
					&& ((ShorthandValue) value).getLonghands().size() < getLonghandPropertyCount(propertyName)) {
				return "";
			}
			return value.getCssText();
		}
		return "";
	}

	private int getLonghandPropertyCount(String propertyName) {
		int count;
		if ("font".equals(propertyName)) {
			count = 17;
		} else {
			String[] longhands = ShorthandDatabase.getInstance().getLonghandProperties(propertyName);
			if (longhands != null) {
				count = longhands.length;
			} else {
				count = Integer.MAX_VALUE;
			}
		}
		return count;
	}

	@Override
	public StyleValue getPropertyCSSValue(String propertyName) {
		propertyName = getCanonicalPropertyName(propertyName);
		if (ShorthandDatabase.getInstance().isShorthand(propertyName)) {
			return null;
		} else {
			return getCSSValue(propertyName);
		}
	}

	/*
	 * If it is not a custom property, convert to ASCII lower case.
	 */
	String getCanonicalPropertyName(String propertyName) {
		if (propertyName.length() > 2 && (propertyName.charAt(0) != '-' || propertyName.charAt(1) != '-')) {
			propertyName = propertyName.toLowerCase(Locale.ROOT);
		}
		return propertyName;
	}

	protected StyleValue getCSSValue(String propertyName) {
		return getDeclaredCSSValue(propertyName);
	}

	protected StyleValue getDeclaredCSSValue(String propertyName) {
		return propValue.get(propertyName);
	}

	/**
	 * The used value of some properties is bound by a 'master' property, so if that master
	 * property has a list value with <code>n</code> items, those properties have also a list
	 * value of <code>n</code> items. If more values were specified, they are truncated, and
	 * if less, the specified values are repeated until reaching <code>n</code>.
	 * 
	 * @param masterProperty
	 *            the name of the master property (e.g. 'background-image').
	 * @param propertyName
	 *            the name of the bound property.
	 * @param value
	 *            the specified value for that property.
	 * @return the used value for the given parameters.
	 */
	public StyleValue computeBoundProperty(String masterProperty, String propertyName, StyleValue value) {
		StyleValue bimg = getCSSValue(masterProperty);
		if (bimg == null) {
			return null;
		}
		// find the number of layers (list items in master property)
		int layers = 1;
		if (bimg.getCssValueType() == CssType.LIST) {
			ValueList list = (ValueList) bimg;
			if (list.isCommaSeparated()) {
				layers = list.getLength();
			} // else: error
		}
		if (layers == 1) {
			if (value == null) {
				value = PropertyDatabase.getInstance().getInitialValue(propertyName);
			}
		} else {
			// Adjust the number of list items to the count of layers.
			value = computeSubpropertyList(value, layers);
		}
		return value;
	}

	/*
	 * Adjust the number of list items to the count of layers.
	 */
	ValueList computeSubpropertyList(StyleValue value, int layers) {
		int items;
		ValueList list;
		if (value.getCssValueType() == CssType.LIST && ((ValueList) value).isCommaSeparated()) {
			list = (ValueList) value.clone();
			items = list.getLength();
		} else {
			list = ValueList.createCSValueList();
			list.add(value);
			items = 1;
		}
		if (layers != items) {
			if (layers < items) {
				while (items > layers) {
					list.remove(--items);
				}
			} else {
				int j = 0;
				while (items++ < layers) {
					list.add(list.item(j++));
				}
			}
		}
		return list;
	}

	/**
	 * Used to remove a CSS property if it has been explicitly set within this declaration
	 * block.
	 * 
	 * @param propertyName
	 *            name of the property to remove.
	 * @return Returns the value of the property if it has been explicitly set for this
	 *         declaration block. Returns the empty string if the property has not been set or
	 *         the property name does not correspond to a known CSS property.
	 */
	@Override
	public String removeProperty(String propertyName) {
		ShorthandDatabase sdb;
		propertyName = getCanonicalPropertyName(propertyName);
		int idx = propertyList.indexOf(propertyName);
		if (idx >= 0 && !propValue.get(propertyName).isSubproperty()) {
			String oldcsstext = propValue.remove(propertyName).getCssText();
			// Is an explicitly-set longhand property.
			propertyList.remove(idx);
			priorities.remove(idx);
			// Check whether there is a previous shorthand that has to have
			// effect again
			if (!shorthandSet.isEmpty() && (sdb = ShorthandDatabase.getInstance()).isShorthandSubproperty(propertyName)) {
				resetFromShorthand(propertyName, sdb);
			}
			return oldcsstext;
		} else if (shorthandSet.contains(propertyName)) {
			ShorthandValue shval = (ShorthandValue) propValue.get(propertyName);
			propValue.remove(propertyName);
			shorthandSet.remove(propertyName);
			String text = shval.getCssText();
			sdb = ShorthandDatabase.getInstance();
			// Remove all sub-properties
			removeSubproperties(shval, sdb);
			return text;
		} else {
			return "";
		}
	}

	private boolean resetFromShorthand(String propertyName, ShorthandDatabase sdb) {
		// Check whether there is a previous shorthand that has to have
		// effect again
		String shorthand = propertyName;
		int shidx = -1;
		Iterator<String> it = shorthandSet.iterator();
		while (it.hasNext()) {
			String shname = it.next();
			if (sdb.isShorthandSubpropertyOf(shname, propertyName)) {
				ShorthandValue shval = (ShorthandValue) propValue.get(shname);
				String pty = shval.getLonghands().iterator().next();
				int i = propertyList.indexOf(pty);
				if (i > shidx) {
					shidx = i;
					shorthand = shname;
				}
			}
		}
		if (shidx != -1) {
			ShorthandValue shval = (ShorthandValue) propValue.get(shorthand);
			// Reset sub-property
			shval.getLonghands().add(propertyName);
			BaseCSSStyleDeclaration copy = new BaseCSSStyleDeclaration();
			copy.setSubproperties(shorthand, shval.getLexicalUnit(), shval.isImportant());
			propValue.put(propertyName, copy.propValue.get(propertyName));
			priorities.add(shidx, shval.isImportant() ? "important" : null);
			propertyList.add(shidx, propertyName);
			return true;
		}
		return false;
	}

	private void removeSubproperties(ShorthandValue shval, ShorthandDatabase sdb) {
		HashSet<String> longhands = shval.getLonghands();
		Iterator<String> it = longhands.iterator();
		while (it.hasNext()) {
			String property = it.next();
			int idx = propertyList.indexOf(property);
			propertyList.remove(idx);
			priorities.remove(idx);
			propValue.remove(property);
			if (!shorthandSet.isEmpty()) {
				resetFromShorthand(property, sdb);
			}
		}
	}

	/**
	 * Used to retrieve the priority of a CSS property (e.g. the
	 * <code>"important"</code> qualifier) if the property has been explicitly set
	 * in this declaration block.
	 * 
	 * @param propertyName The name of the CSS property.
	 * @return A string representing the priority (e.g. <code>"important"</code>) if
	 *         the property has been explicitly set in this declaration block and
	 *         has a priority specified. The empty string otherwise.
	 */
	@Override
	public String getPropertyPriority(String propertyName) {
		int idx = propertyList.indexOf(propertyName);
		if (idx == -1) {
			return getUnknownPropertyPriority(propertyName);
		}
		String prio = priorities.get(idx);
		if (prio != null) {
			return prio;
		} else {
			return "";
		}
	}

	String getUnknownPropertyPriority(String propertyName) {
		/*
		 * If an active shorthand property is important, subproperties remain
		 * important
		 */
		if (shorthandSet.contains(propertyName)
				&& ((ShorthandValue) propValue.get(propertyName)).isImportant()) {
			return "important";
		}
		/*
		 * No active shorthand, but perhaps all longhands are available
		 */
		return checkShorthandPriority(propertyName);
	}

	String checkShorthandPriority(String propertyName) {
		ShorthandDatabase sdb = ShorthandDatabase.getInstance();
		if (sdb.isShorthand(propertyName)) {
			String[] longhands = sdb.getLonghandProperties(propertyName);
			for (int i = 0; i < longhands.length; i++) {
				if (!isPropertyImportant(longhands[i])) {
					return "";
				}
			}
			return "important";
		}
		return "";
	}

	@Override
	public void setProperty(String propertyName, LexicalUnit value, boolean important) throws DOMException {
		propertyName = getCanonicalPropertyName(propertyName);
		// Check for shorthand properties
		ShorthandDatabase sdb = ShorthandDatabase.getInstance();
		if (sdb.isShorthand(propertyName)) {
			// Shorthand case.
			setShorthandProperty(sdb, propertyName, value, important);
		} else {
			setLonghandProperty(propertyName, value, important);
		}
	}

	private void setShorthandProperty(ShorthandDatabase sdb, String propertyName, LexicalUnit value, boolean important)
			throws DOMException {
		// Check whether an ordinary shorthand tries to replace an
		// important one.
		ShorthandValue overriddenVal = (ShorthandValue) propValue.get(propertyName);
		if (overriddenVal == null || important || !overriddenVal.isImportant()) {
			LinkedList<String> shadowedShorthands = null;
			if (!shorthandSet.isEmpty()) {
				if (shorthandSet.contains(propertyName)) {
					shadowedShorthands = new LinkedList<String>();
					shadowedShorthands.add(propertyName);
				}
				// Check whether propertyName, while being a shorthand, is
				// also a subproperty of a previously set shorthand, or if a previously set
				// shorthand happens to be a subproperty of propertyName.
				Iterator<String> it = shorthandSet.iterator();
				while (it.hasNext()) {
					String sh = it.next();
					if (sdb.isShorthandSubpropertyOf(propertyName, sh)) {
						// Found a set shorthand that is a subproperty of propertyName
						if (important || !((ShorthandValue) propValue.get(sh)).isImportant()) {
							// It is replaceable, add to shadowedShorthands
							if (shadowedShorthands == null) {
								shadowedShorthands = new LinkedList<String>();
							}
							shadowedShorthands.add(sh);
						}
					} else if (sdb.isShorthandSubpropertyOf(sh, propertyName) && !important
							&& ((ShorthandValue) propValue.get(sh)).isImportant()) {
						return; // ignore non-important subproperty of
								// previously set important shorthand
					}
				}
			}
			setShorthandLonghands(propertyName, value, important, shadowedShorthands);
		}
	}

	boolean setShorthandLonghands(String propertyName, LexicalUnit value, boolean important,
			LinkedList<String> shadowedShorthands) {
		try {
			SubpropertySetter shorthandSetter = setSubproperties(propertyName, value, important);
			String shorthandText = shorthandSetter.getCssText();
			if (shorthandText.length() != 0) {
				ShorthandValue shVal = shorthandSetter.createCSSShorthandValue(value);
				shVal.setShorthandText(shorthandText, shorthandSetter.getMinifiedCssText());
				if (shadowedShorthands != null) {
					Iterator<String> it = shadowedShorthands.iterator();
					while (it.hasNext()) {
						String shadowed = it.next();
						shorthandSet.remove(shadowed);
						propValue.remove(shadowed);
					}
				}
				propValue.put(propertyName, shVal);
				shorthandSet.add(propertyName);
			} else {
				// Report warning
				shorthandWarning(propertyName, value, important);
			}
			return true;
		} catch (DOMException e) {
			// Report error
			shorthandError(propertyName, value, important, shadowedShorthands, e);
			return false;
		}
	}

	void shorthandWarning(String propertyName, LexicalUnit value, boolean important) {
		StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
		if (errHandler != null) {
			errHandler.shorthandWarning(propertyName, lexicalUnitToString(value));
		}
	}

	void compatWarning(String propertyName, LexicalUnit value, boolean important) {
		StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
		if (errHandler != null) {
			String s = lexicalUnitToString(value);
			StringBuilder sb = new StringBuilder(s.length());
			sb.append(s);
			if (important) {
				sb.append("!important");
			}
			errHandler.compatWarning(propertyName, sb.toString());
		}
	}

	protected void shorthandError(String propertyName, LexicalUnit value, boolean important,
			LinkedList<String> shadowedShorthands, DOMException e) {
		StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
		if (errHandler != null) {
			CSSPropertyValueException ex = new CSSPropertyValueException("Wrong value for " + propertyName);
			ex.setValueText(lexicalUnitToString(value));
			errHandler.wrongValue(propertyName, ex);
		}
	}

	protected void setLonghandProperty(String propertyName, LexicalUnit value, boolean important) throws DOMException {
		ValueFactory factory = getValueFactory();
		StyleValue cssvalue;
		try {
			cssvalue = factory.createCSSValue(value, this);
		} catch (DOMException e) {
			// Report error
			StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
			if (errHandler != null) {
				CSSPropertyValueException ex = new CSSPropertyValueException("Wrong value for " + propertyName, e);
				ex.setValueText(lexicalUnitToString(value));
				errHandler.wrongValue(propertyName, ex);
			}
			throw e;
		}
		// Handle special cases
		if (propertyName.equals("font-family") || propertyName.equals("content")) {
			// Special case (e.g. unquoted "Times New Roman")
			if (cssvalue.getCssValueType() == CssType.LIST) {
				ValueList list = (ValueList) cssvalue;
				if (list.isCommaSeparated()) {
					int sz = list.getLength();
					for (int i = 0; i < sz; i++) {
						if (list.item(i).getCssValueType() == CssType.LIST) {
							list.set(i, listToString((ValueList) list.item(i)));
						}
					}
				} else {
					cssvalue = listToString(list);
				}
			}
		} else if (propertyName.equals("background-position")) {
			// Check property
			if (cssvalue.getCssValueType() == CssType.LIST) {
				ValueList list = (ValueList) cssvalue;
				if (list.isCommaSeparated()) {
					int sz = list.getLength();
					for (int i = 0; i < sz; i++) {
						StyleValue item = list.item(i);
						if (item.getCssValueType() == CssType.LIST) {
							if (!checkBackgroundPosition((ValueList) item)) {
								list.remove(i--);
								// Report error
								wrongBackgroundPositionError(item.getCssText());
							}
						}
					}
					if (list.getLength() == 0) {
						return; // ignore
					}
				} else if (!checkBackgroundPosition(list)) {
					// Report error
					wrongBackgroundPositionError(list.getCssText());
					return; // ignore
				}
			}
		}
		setProperty(propertyName, cssvalue, important);
	}

	private void wrongBackgroundPositionError(String cssText) {
		StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
		if (errHandler != null) {
			CSSPropertyValueException ex = new CSSPropertyValueException("Wrong value for background-position");
			ex.setValueText(cssText);
			errHandler.wrongValue("background-position", ex);
		}
	}

	private StyleValue listToString(ValueList list) {
		int len = list.getLength();
		boolean allItemsAreIdent = true;
		for (int i = 1; i < len; i++) {
			StyleValue cssval = list.item(i);
			if (cssval.getCssValueType() != CssType.TYPED
					|| cssval.getPrimitiveType() != Type.IDENT) {
				allItemsAreIdent = false;
				break;
			}
		}
		if (allItemsAreIdent) {
			StringBuilder buf = new StringBuilder(len * 7 + 8);
			buf.append(list.item(0).getCssText());
			for (int i = 1; i < len; i++) {
				buf.append(' ').append(list.item(i).getCssText());
			}
			StringValue csstr = new StringValue();
			csstr.setStringValue(Type.STRING, buf.toString());
			return csstr;
		} else {
			return list;
		}
	}

	/*
	 * "If three or four values are given, then each <percentage> or <length> represents an
	 * offset and must be preceded by a keyword"
	 */
	private static boolean checkBackgroundPosition(ValueList list) {
		int count = list.getLength();
		if (count < 3) {
			return true;
		} else if (count == 4) {
			return list.item(0).getPrimitiveType() == Type.IDENT
					&& list.item(1).getPrimitiveType() != Type.IDENT
					&& list.item(2).getPrimitiveType() == Type.IDENT
					&& list.item(3).getPrimitiveType() != Type.IDENT;
		} else { // 3
			if (list.item(0).getPrimitiveType() != Type.IDENT) {
				return false;
			}
			if (list.item(1).getPrimitiveType() == Type.IDENT
					&& list.item(2).getPrimitiveType() != Type.IDENT) {
				return true;
			}
			return list.item(1).getPrimitiveType() != Type.IDENT
					&& list.item(2).getPrimitiveType() == Type.IDENT;
		}
	}

	/**
	 * Used to set a property value and priority within this declaration block.
	 * 
	 * @param propertyName
	 *            The name of the CSS property. See the CSS property index.
	 * @param value
	 *            The new value of the property.
	 * @param priority
	 *            The new priority of the property (e.g. <code>"important"</code>).
	 * @throws DOMException
	 *             SYNTAX_ERR: Raised if the specified value has a syntax error and is
	 *             unparsable. <br>
	 *             NO_MODIFICATION_ALLOWED_ERR: Raised if this declaration is readonly or the
	 *             property is readonly.
	 */
	@Override
	public void setProperty(String propertyName, String value, String priority) throws DOMException {
		if (value.length() == 0) {
			removeProperty(propertyName);
			return;
		}
		Parser parser;
		try {
			parser = createSACParser();
		} catch (DOMException e) {
			// Could not create parser.
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, e.getMessage());
		}
		Reader re = new StringReader(value);
		LexicalUnit lunit;
		try {
			lunit = parser.parsePropertyValue(re);
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		boolean important = "important".equalsIgnoreCase(priority);
		setProperty(propertyName, lunit, important);
	}

	/**
	 * Set the given property.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param cssValue
	 *            the property value.
	 * @param priority
	 *            <code>true</code> if the property priority is important.
	 * @return <code>true</code> if the property was set and the length of the style
	 *         declaration varied.
	 */
	boolean setProperty(String propertyName, StyleValue cssValue, boolean important) {
		propertyName = propertyName.intern();
		String priority = important ? "important" : null;
		if (!propertyList.contains(propertyName)) {
			addProperty(propertyName, cssValue, priority);
			return true;
		} else {
			// Replace property value if priority permits
			return replaceProperty(propertyName, cssValue, priority);
		}
	}

	/**
	 * Replace property value if priority permits
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param cssValue
	 *            the property value.
	 * @param priority
	 *            the property priority.
	 * @return <code>true</code> if the property was set and the length of the style
	 *         declaration varied.
	 */
	boolean replaceProperty(String propertyName, StyleValue cssValue, String priority) {
		int idx = propertyList.indexOf(propertyName);
		boolean overriddenImportant = "important".equals(priorities.get(idx));
		if (!overriddenImportant || "important".equals(priority)) {
			if (addOverrideProperty(propertyName, cssValue, priority)) {
				propertyList.remove(idx);
				priorities.remove(idx);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void addProperty(String propertyName, StyleValue cssValue, String priority) {
		if (cssValue.getCssValueType() == CssType.SHORTHAND) {
			// We got a CSSShorthandValue
			addShorthandName(propertyName);
		} else {
			propertyList.add(propertyName);
			priorities.add(priority);
			StyleValue ovValue = propValue.get(propertyName);
			if (ovValue != null && ovValue.isSubproperty()) {
				overrideShorthands(propertyName, priority);
			}
		}
		propValue.put(propertyName, cssValue);
	}

	protected boolean addOverrideProperty(String propertyName, StyleValue cssValue, String priority) {
		addProperty(propertyName, cssValue, priority);
		return true;
	}

	private void addShorthandName(String shorthandName) {
		if (shorthandSet.contains(shorthandName)) {
			shorthandSet.remove(shorthandName);
		}
		shorthandSet.add(shorthandName);
	}

	/**
	 * Partial override of a shorthand by a longhand.
	 * <p>
	 * The longhand property is replacing a value set by a shorthand. Remove the longhand from
	 * the list of subproperties that the shorthand is responsible for, and check whether that
	 * shorthand is still responsible for at least one longhand. If it is not, remove that
	 * shorthand.
	 * 
	 * @param longhandName
	 *            the longhand name.
	 * @param priority
	 *            the longhand priority.
	 */
	private void overrideShorthands(String longhandName, String priority) {
		Iterator<String> it = shorthandSet.iterator();
		while (it.hasNext()) {
			String shName = it.next();
			ShorthandValue shval = (ShorthandValue) propValue.get(shName);
			if (!shval.isImportant() || "important".equals(priority)) {
				if (shval.overrideByLonghand(longhandName)) {
					it.remove();
					propValue.remove(shName);
					return;
				}
			}
		}
	}

	boolean addCompatProperty(String propertyName, StyleValue cssValue, String priority) {
		if (cssValue.getCssValueType() == CssType.SHORTHAND) {
			// We got a CSSShorthandValue
			addShorthandName(propertyName);
		} else {
			StyleValue overridden = propValue.get(propertyName);
			if (hasUnknown(cssValue)) {
				compatLonghand(propertyName, cssValue, "important".equals(priority), overridden != null);
				return false;
			}
			propertyList.add(propertyName);
			priorities.add(priority);
			StyleValue ovValue = propValue.get(propertyName);
			if (ovValue != null && ovValue.isSubproperty()) {
				overrideShorthands(propertyName, priority);
			}
		}
		propValue.put(propertyName, cssValue);
		return true;
	}

	private static boolean hasUnknown(CSSValue cssValue) {
		CssType type = cssValue.getCssValueType();
		if (type == CssType.TYPED) {
			CSSTypedValue.Type ptype = cssValue.getPrimitiveType();
			if (ptype == Type.UNKNOWN) {
				return true;
			} else if (ptype == Type.FUNCTION) {
				CSSFunctionValue function = (CSSFunctionValue) cssValue;
				CSSValueList<? extends CSSValue> list = function.getArguments();
				for (CSSValue value : list) {
					if (hasUnknown(value)) {
						return true;
					}
				}
			} else if (ptype == Type.EXPRESSION) {
				CSSExpressionValue calc = (CSSExpressionValue) cssValue;
				return hasUnknown(calc.getExpression());
			}
		} else if (type == CssType.LIST) {
			ValueList list = (ValueList) cssValue;
			for (CSSValue value : list) {
				if (hasUnknown(value)) {
					return true;
				}
			}
		} // assume that PROXY values have no hacks.
		return false;
	}

	private static boolean hasUnknown(CSSExpression expression) {
		if (expression.getPartType() == CSSExpression.AlgebraicPart.OPERAND) {
			return hasUnknown(((CSSOperandExpression) expression).getOperand());
		}
		AlgebraicExpression ae = (AlgebraicExpression) expression;
		int len = ae.getLength();
		for (int i = 0; i < len; i++) {
			CSSExpression expr = ae.item(i);
			if (hasUnknown(expr)) {
				return true;
			}
		}
		return false;
	}

	protected void compatLonghand(String propertyName, StyleValue overridden, boolean priorityImportant,
			boolean isOverridden) {
	}

	boolean isPropertySet(String propertyName) {
		return propertyList.contains(propertyName);
	}

	/**
	 * Is the given property set as of important priority ?
	 * 
	 * @param propertyName
	 *            the name of the property to test.
	 * @return <code>true</code> if the property is set, and has important priority,
	 *         <code>false</code> otherwise.
	 */
	boolean isPropertyImportant(String propertyName) {
		int idx = propertyList.indexOf(propertyName);
		String prio;
		if (idx != -1) {
			return (prio = priorities.get(idx)) != null && prio.length() != 0;
		}
		return false;
	}

	/**
	 * Is the given property set in this declaration, with the given priority.
	 * 
	 * @param propertyName
	 *            the name of the property to test.
	 * @param important
	 *            true if priority is important.
	 * @return <code>true</code> if propertyName is set with the given priority,
	 *         <code>false</code> otherwise.
	 */
	boolean isPropertySet(String propertyName, boolean important) {
		int idx = propertyList.indexOf(propertyName);
		String prio;
		if (idx != -1) {
			boolean prioImportant = (prio = priorities.get(idx)) != null && prio.length() != 0;
			return prioImportant == important;
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		return propertyList.isEmpty();
	}

	/**
	 * The number of properties that have been explicitly set in this declaration block. The
	 * range of valid indices is 0 to length-1 inclusive.
	 */
	@Override
	public int getLength() {
		return propertyList.size();
	}

	/**
	 * Used to retrieve the properties that have been set in this declaration block. The order
	 * of the properties retrieved using this method does not have to be the order in which
	 * they were set. This method can be used to iterate over all properties in this
	 * declaration block.
	 * 
	 * @param index
	 *            the property index.
	 * @return the name of the property at this ordinal position, or the empty string if no
	 *         property exists at this position.
	 */
	@Override
	public String item(int index) {
		if (index < 0 || index > propertyList.size()) {
			return "";
		} else {
			return propertyList.get(index);
		}
	}

	@Override
	void clear() {
		propValue.clear();
		propertyList.clear();
		priorities.clear();
		shorthandSet.clear();
		StyleDeclarationErrorHandler errHandler = getStyleDeclarationErrorHandler();
		if (errHandler != null) {
			errHandler.reset();
		}
	}

	/**
	 * Retrieves the CSS rule that contains this declaration block.
	 * 
	 * @return the CSS rule that contains this declaration block or <code>null</code> if this
	 *         <code>CSSStyleDeclaration</code> is not attached to a <code>CSSRule</code>.
	 */
	@Override
	public BaseCSSDeclarationRule getParentRule() {
		return parentRule;
	}

	/**
	 * The node that owns this declaration.
	 * <p>
	 * For computed styles, the owner is always the element for which the style was computed.
	 * 
	 * @return the node that owns this declaration, or null if none.
	 */
	public Node getOwnerNode() {
		AbstractCSSStyleSheet sheet;
		if (parentRule != null && (sheet = parentRule.getParentStyleSheet()) != null) {
			return sheet.getOwnerNode();
		}
		return null;
	}

	/**
	 * Add all the properties in the given style declaration to this one.
	 * 
	 * @param style
	 *            the style declaration whose properties have to be added.
	 */
	public void addStyle(BaseCSSStyleDeclaration style) {
		ShorthandDatabase sdb = ShorthandDatabase.getInstance();
		HashSet<String> addedShorthands = new HashSet<String>(style.shorthandSet.size());
		// Process individual properties
		Iterator<String> it = style.propertyList.iterator();
		int i = -1;
		while (it.hasNext()) {
			i++;
			String propertyName = it.next();
			int pIndex = propertyList.indexOf(propertyName);
			if (pIndex == -1) {
				// Property has no value set currently.
				propertyList.add(propertyName);
				priorities.add(style.priorities.get(i));
			} else if (!"important".equals(priorities.get(pIndex))) {
				// Current value is not !important, added value may or may not be.
				priorities.set(pIndex, style.priorities.get(i));
			} else if (!"important".equals(style.priorities.get(i))) {
				// Current value is !important, added value is not.
				continue;
			}
			// Obtain value
			StyleValue value = style.getCSSValue(propertyName);
			// Deal with shorthands (we reach this only if priority allows)
			if (value.isSubproperty()) {
				String shorthand = sdb.getShorthand(propertyName);
				if (!addedShorthands.contains(shorthand)) {
					/*
					 * Check whether we got a shorthand of a shorthand (like border-width and border)
					 */
					String bigshorthand = sdb.getShorthand(shorthand);
					if (bigshorthand != null && style.shorthandSet.contains(bigshorthand)) {
						addedShorthands.add(bigshorthand);
						addShorthandName(bigshorthand);
						StyleValue shvalue = style.propValue.get(bigshorthand);
						BaseCSSStyleDeclaration.this.setPropertyCSSValue(bigshorthand, shvalue, null);
					}
					StyleValue shvalue = style.propValue.get(shorthand);
					if (shvalue != null) {
						addedShorthands.add(shorthand);
						addShorthandName(shorthand);
						BaseCSSStyleDeclaration.this.setPropertyCSSValue(shorthand, shvalue, null);
					}
				}
			}
			// Retrieve href info
			String href = null;
			if ("background-image".equals(propertyName) || "border-image-source".equals(propertyName)) {
				// Add parent stylesheet info
				if (style.getParentRule() != null) {
					href = style.getParentRule().getParentStyleSheet().getHref();
				} else {
					href = ((NodeStyleDeclaration) style).getOwnerNode().getOwnerDocument().getBaseURI();
				}
			}
			// Set value
			setPropertyCSSValue(propertyName, value, href);
		}
	}

	protected void setPropertyCSSValue(String propertyName, StyleValue value, String hrefcontext) {
		propValue.put(propertyName, value);
	}

	/**
	 * Splits this style declaration in two: one for important properties only, and the other
	 * with normal properties.
	 * 
	 * @param importantDecl
	 *            the style declaration for important properties.
	 * @param normalDecl
	 *            the style declaration for normal properties.
	 */
	@Override
	protected void prioritySplit(AbstractCSSStyleDeclaration importantDecl, AbstractCSSStyleDeclaration normalDecl) {
		int psz = propertyList.size();
		for (int i = 0; i < psz; i++) {
			String propertyName = propertyList.get(i);
			StyleValue value = propValue.get(propertyName);
			String priority = priorities.get(i);
			if ("important".equals(priority)) {
				importantDecl.addProperty(propertyName, value, priority);
			} else {
				normalDecl.addProperty(propertyName, value, priority);
			}
		}
		// Shorthands
		for (String sh : shorthandSet) {
			ShorthandValue value = (ShorthandValue) propValue.get(sh);
			if (value.isImportant()) {
				importantDecl.addProperty(sh, value, "important");
			} else {
				normalDecl.addProperty(sh, value, null);
			}
		}
	}

	/**
	 * Gets the style database which is used to compute the style.
	 * 
	 * @return <code>null</code> the style database, or <code>null</code> if no
	 *         style database is being used (like in the case of declared styles).
	 */
	public StyleDatabase getStyleDatabase() {
		if (parentRule != null) {
			AbstractCSSStyleSheet pSheet = parentRule.getParentStyleSheet();
			if (pSheet != null) {
				Node node = pSheet.getOwnerNode();
				if (node != null) {
					StyleDatabase sdb;
					if (node.getNodeType() != Node.DOCUMENT_NODE) {
						sdb = ((CSSDocument) node.getOwnerDocument()).getStyleDatabase();
					} else {
						sdb = ((CSSDocument) node).getStyleDatabase();
					}
					return sdb;
				}
			}
		}
		return null;
	}

	/**
	 * Computes the initial (default) value for the given property.
	 * 
	 * @param pdb
	 *            the PropertyDatabase object.
	 * @param propertyName
	 *            the name of the property.
	 * @return the initial value for the property, or null if none was found.
	 */
	StyleValue defaultPropertyValue(String propertyName, PropertyDatabase pdb) {
		StyleValue defval = pdb.getInitialValue(propertyName);
		if (defval == null) {
			if (propertyName.equals("color")) {
				// Initial value depends on user agent
				defval = getColorInitialValue();
			} else if (propertyName.equals("font-family")) {
				// Initial value for font-family depends on user agent
				defval = getFontFamilyInitialValue();
			} else if (propertyName.equals("text-align")) {
				String directionValue = getPropertyValue("direction");
				if (directionValue.equals("rtl")) {
					defval = new IdentifierValue("right");
				} else {
					defval = new IdentifierValue("left");
				}
			} else if (propertyName.endsWith("-color")) {
				// background-color does not reach here
				defval = getCurrentColor();
			} else if (propertyName.equals("quotes")) {
				defval = getValueFactory().parseProperty("\" \"");
			}
		}
		return defval;
	}

	private TypedValue getColorInitialValue() {
		TypedValue value;
		StyleDatabase sdb = getStyleDatabase();
		if (sdb == null) {
			// Initial value depends on user agent
			value = getSystemDefaultValue("color");
		} else {
			value = (TypedValue) sdb.getInitialColor();
			value = new SafeSystemDefaultValue(value);
		}
		return value;
	}

	private StyleValue getFontFamilyInitialValue() {
		StyleValue value;
		StyleDatabase sdb = getStyleDatabase();
		if (sdb == null) {
			// Initial value depends on user agent
			value = getSystemDefaultValue("font-family");
		} else {
			value = getValueFactory().parseProperty(sdb.getDefaultGenericFontFamily());
			if (value.getCssValueType() == CssType.TYPED) {
				value = new SafeSystemDefaultValue((TypedValue) value);
			} else {
				value = getSystemDefaultValue("font-family");
			}
		}
		return value;
	}

	private TypedValue getSystemDefaultValue(String propertyName) {
		TypedValue value;
		AbstractCSSStyleSheetFactory factory = getStyleSheetFactory();
		if (factory != null) {
			value = factory.getSystemDefaultValue(propertyName);
		} else {
			value = SystemDefaultValue.getInstance();
		}
		return value;
	}

	protected TypedValue getCurrentColor() {
		return new IdentifierValue("currentcolor");
	}

	public TypedValue getCSSColor() {
		StyleValue cssvalue = getCSSValue("color");
		TypedValue color;
		if (cssvalue == null || cssvalue.getCssValueType() != CssType.TYPED) {
			color = getColorInitialValue();
		} else {
			color = (TypedValue) cssvalue;
		}
		return color;
	}

	LinkedList<String> getShorthandSet() {
		return shorthandSet;
	}

	/**
	 * Set the subproperties of a shorthand, and returns its text representation.
	 * 
	 * @param propertyName
	 *            the shorthand property name.
	 * @param value
	 *            the shorthand property value.
	 * @param priority
	 *            the shorthand property priority.
	 * @return the SubpropertySetter for the shorthand, or null if propertyName is not a
	 *         shorthand.
	 * @throws DOMException
	 *             if a problem happens creating the value to assign to a subproperty, or the
	 *             property value is invalid.
	 */
	private SubpropertySetter setSubproperties(String propertyName, LexicalUnit value, boolean important)
			throws DOMException {
		ShorthandDatabase sdb = ShorthandDatabase.getInstance();
		if (sdb.isShorthand(propertyName)) {
			SubpropertySetter setter;
			// Check for var()
			if (isOrContainsType(value, LexicalType.VAR)) {
				setter = new PendingSubstitutionSetter(this, propertyName);
				setter.init(value, important);
				setter.assignSubproperties();
				return setter;
			}
			// Normal shorthands
			if ("font".equals(propertyName)) {
				if (getStyleDatabase() != null) {
					// Check for system font identifier
					if (value.getLexicalUnitType() == LexicalType.IDENT && value.getNextLexicalUnit() == null) {
						String decl = getStyleDatabase().getSystemFontDeclaration(value.getStringValue());
						if (decl != null) {
							return setSystemFont(decl, important);
						}
					}
				}
				setter = new FontShorthandSetter(this);
			} else if ("margin".equals(propertyName)) {
				setter = new MarginShorthandSetter(this);
			} else if ("padding".equals(propertyName)) {
				setter = new BoxShorthandSetter(this, "padding");
			} else if ("border".equals(propertyName)) {
				setter = new BorderShorthandSetter(this);
			} else if ("border-width".equals(propertyName)) {
				setter = new BorderWidthShorthandSetter(this);
			} else if ("border-style".equals(propertyName)) {
				setter = new BorderStyleShorthandSetter(this);
			} else if ("border-color".equals(propertyName)) {
				setter = new BorderColorShorthandSetter(this);
			} else if ("border-top".equals(propertyName)) {
				setter = new BorderSideShorthandSetter(this, propertyName, "top");
			} else if ("border-right".equals(propertyName)) {
				setter = new BorderSideShorthandSetter(this, propertyName, "right");
			} else if ("border-bottom".equals(propertyName)) {
				setter = new BorderSideShorthandSetter(this, propertyName, "bottom");
			} else if ("border-left".equals(propertyName)) {
				setter = new BorderSideShorthandSetter(this, propertyName, "left");
			} else if ("background".equals(propertyName)) {
				setter = new BackgroundShorthandSetter(this);
			} else if ("transition".equals(propertyName)) {
				setter = new TransitionShorthandSetter(this);
			} else if ("border-image".equals(propertyName)) {
				setter = new BorderImageShorthandSetter(this);
			} else if ("font-variant".equals(propertyName)) {
				setter = new FontVariantShorthandSetter(this);
			} else if ("border-radius".equals(propertyName)) {
				setter = new BorderRadiusShorthandSetter(this);
			} else if ("cue".equals(propertyName) || "pause".equals(propertyName) || "rest".equals(propertyName)) {
				setter = new SequenceShorthandSetter(this, propertyName);
			} else if ("list-style".equals(propertyName)) {
				setter = new ListStyleShorthandSetter(this);
			} else if ("animation".equals(propertyName)) {
				setter = new AnimationShorthandSetter(this);
			} else if ("flex".equals(propertyName)) {
				setter = new FlexShorthandSetter(this);
			} else if ("grid".equals(propertyName)) {
				setter = new GridShorthandSetter(this);
			} else if ("grid-template".equals(propertyName)) {
				setter = new GridTemplateShorthandSetter(this);
			} else if ("grid-area".equals(propertyName)) {
				setter = new GridAreaShorthandSetter(this);
			} else if ("grid-column".equals(propertyName) || "grid-row".equals(propertyName)) {
				setter = new GridPlacementShorthandSetter(this, propertyName);
			} else if ("columns".equals(propertyName)) {
				setter = new ColumnsShorthandSetter(this);
			} else if ("column-rule".equals(propertyName)) {
				setter = new ColumnRuleShorthandSetter(this);
			} else if ("place-content".equals(propertyName) || "place-items".equals(propertyName)
					|| "place-self".equals(propertyName)) {
				setter = new OrderedTwoIdentifierShorthandSetter(this, propertyName);
			} else if ("gap".equals(propertyName)) {
				setter = new OrderedTwoLPIShorthandSetter(this, propertyName);
			} else {
				setter = new ShorthandSetter(this, propertyName);
			}
			setter.init(value, important);
			if (!setter.assignSubproperties()) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Invalid property declaration: " + value.toString());
			}
			return setter;
		} else {
			return null;
		}
	}

	private static boolean isOrContainsType(LexicalUnit lunit, LexicalType unitType) {
		do {
			if (lunit.getLexicalUnitType() == unitType
					|| (lunit.getParameters() != null && isOrContainsType(lunit.getParameters(), unitType))
					|| (lunit.getSubValues() != null && isOrContainsType(lunit.getSubValues(), unitType))) {
				return true;
			}
			lunit = lunit.getNextLexicalUnit();
		} while (lunit != null);
		return false;
	}

	private SubpropertySetter setSystemFont(String fontDecl, boolean important) throws DOMException {
		Reader re = new StringReader(fontDecl);
		LexicalUnit lunit = null;
		try {
			lunit = createSACParser().parsePropertyValue(re);
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (IOException e) {
			// this won't happen
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return setSubproperties("font", lunit, important);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		TreeSet<String> propertyNames = new TreeSet<String>(propertyList);
		Iterator<String> it = propertyNames.iterator();
		while (it.hasNext()) {
			String property = it.next();
			result = prime * result + property.hashCode();
			result = prime * result + propValue.get(property).hashCode();
			String prio = priorities.get(propertyList.indexOf(property));
			result = prime * result + ((prio == null) ? 0 : prio.hashCode());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BaseCSSStyleDeclaration))
			return false;
		BaseCSSStyleDeclaration other = (BaseCSSStyleDeclaration) obj;
		// propertyList, propValue, etc. are never null
		if (propertyList.size() != other.propertyList.size()) {
			return false;
		}
		Iterator<String> it = propertyList.iterator();
		while (it.hasNext()) {
			String property = it.next();
			if (!other.propertyList.contains(property)) {
				return false;
			}
			StyleValue value = propValue.get(property);
			if (!value.equals(other.propValue.get(property))) {
				return false;
			}
			int idx = propertyList.indexOf(property);
			int idxo = other.propertyList.indexOf(property);
			String prio = priorities.get(idx);
			String prioo = other.priorities.get(idxo);
			if (prio == null) {
				if (prioo != null) {
					return false;
				}
			} else if (!prio.equals(prioo)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gives an object containing the differences between two declarations.
	 * <p>
	 * Properties that are set in this style declaration but not in <code>other</code> will be
	 * in the left side of the diff, while the ones that are set in the <code>other</code> but
	 * not in this one will be in the right side. Properties that are set to different values
	 * or that have different priorities will be in the <code>getDifferent()</code> part.
	 * 
	 * @param other
	 *            the style declaration to be compared to.
	 * @return the <code>Diff</code> object containing the differences.
	 */
	public Diff<String> diff(BaseCSSStyleDeclaration other) {
		PropertyDiff diff = new PropertyDiff();
		Iterator<String> it = propertyList.iterator();
		while (it.hasNext()) {
			String property = it.next();
			if (!other.propertyList.contains(property)) {
				diff.leftSide.add(property);
			} else {
				StyleValue value = getCSSValue(property);
				StyleValue otherValue = other.getCSSValue(property);
				if (valueEquals(value, otherValue)) {
					int idx = propertyList.indexOf(property);
					int idxo = other.propertyList.indexOf(property);
					String prio = priorities.get(idx);
					String prioo = other.priorities.get(idxo);
					if (prio == null) {
						if (prioo == null) {
							continue;
						}
					} else if (prio.equals(prioo)) {
						continue;
					}
				}
				diff.differentValues.add(property);
			}
		}
		it = other.propertyList.iterator();
		while (it.hasNext()) {
			String property = it.next();
			if (!propertyList.contains(property)) {
				diff.rightSide.add(property);
			}
		}
		return diff;
	}

	private boolean valueEquals(StyleValue value, StyleValue otherValue) {
		return value != null ? value.equals(otherValue) : otherValue == null;
	}

	private class PropertyDiff implements Diff<String> {

		LinkedList<String> leftSide = new LinkedList<String>();
		LinkedList<String> rightSide = new LinkedList<String>();
		LinkedList<String> differentValues = new LinkedList<String>();

		private PropertyDiff() {
			super();
		}

		@Override
		public boolean hasDifferences() {
			return !leftSide.isEmpty() || !rightSide.isEmpty() || !differentValues.isEmpty();
		}

		@Override
		public String[] getLeftSide() {
			if (leftSide.isEmpty()) {
				return null;
			}
			return leftSide.toArray(new String[0]);
		}

		@Override
		public String[] getRightSide() {
			if (rightSide.isEmpty()) {
				return null;
			}
			return rightSide.toArray(new String[0]);
		}

		@Override
		public String[] getDifferent() {
			if (differentValues.isEmpty()) {
				return null;
			}
			return differentValues.toArray(new String[0]);
		}

	}

	@Override
	public String toString() {
		return getCssText();
	}

	@Override
	public BaseCSSStyleDeclaration clone() {
		return new BaseCSSStyleDeclaration(this);
	}

	/**
	 * Produces a string from a lexical unit, without paying attention to whether the syntax
	 * or the CSS are valid.
	 * <p>
	 * Mostly useful for error reporting purposes.
	 * 
	 * @param value
	 *            the lexical unit.
	 * @return the string representation of the lexical unit.
	 */
	static String lexicalUnitToString(LexicalUnit value) {
		if (value == null) {
			return "";
		}
		return value.toString();
	}

	private class StyleDeclarationHandler extends PropertyCSSHandler {
		private StyleDeclarationHandler() {
			super();
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important) {
			try {
				super.property(name, value, important);
			} catch (DOMException e) {
				if (getStyleDeclarationErrorHandler() != null) {
					CSSPropertyValueException ex = new CSSPropertyValueException(e);
					ex.setValueText(value.toString());
					getStyleDeclarationErrorHandler().wrongValue(name, ex);
				}
			}
		}

		@Override
		public void warning(CSSParseException exception) throws CSSParseException {
			// This object only could have been passed as ErrorHandler if both parentRule
			// and StyleDeclarationErrorHandler are not null, but are checked just in case
			if (getStyleDeclarationErrorHandler() != null) {
				getStyleDeclarationErrorHandler().sacWarning(exception, propertyList.size() - 1);
			}
		}

		@Override
		public void error(CSSParseException exception) throws CSSParseException {
			// This object only could have been passed as ErrorHandler if both parentRule
			// and StyleDeclarationErrorHandler are not null, but are checked just in case
			if (getStyleDeclarationErrorHandler() != null) {
				getStyleDeclarationErrorHandler().sacError(exception, propertyList.size() - 1);
			}
		}

	}

	/**
	 * Test if the value is a color.
	 * 
	 * @param lunit the lexical unit to test.
	 * @return true if the value is a color.
	 */
	public static boolean testColor(LexicalUnit lunit) {
		switch (lunit.getLexicalUnitType()) {
		case RGBCOLOR:
		case HSLCOLOR:
		case LABCOLOR:
		case LCHCOLOR:
			return true;
		case IDENT:
			String sv = lunit.getStringValue();
			if (sv == null) {
				return false;
			}
			sv = sv.toLowerCase(Locale.ROOT);
			ColorIdentifiers colorids = ColorIdentifiers.getInstance();
			return colorids.isColorIdentifier(sv) || "transparent".equals(sv) || "currentcolor".equals(sv);
		case FUNCTION:
			String func = lunit.getFunctionName().toLowerCase(Locale.ROOT);
			if ("hwb".equals(func) || "color".equals(func)) {
				return true;
			}
		default:
			break;
		}
		return false;
	}

	@Override
	public String getAzimuth() {
		return getPropertyValue("azimuth");
	}

	@Override
	public void setAzimuth(String azimuth) throws DOMException {
		getPropertyCSSValue("azimuth").setCssText(azimuth);
	}

	@Override
	public String getBackground() {
		return getPropertyValue("background");
	}

	@Override
	public void setBackground(String background) throws DOMException {
		getPropertyCSSValue("background").setCssText(background);
	}

	@Override
	public String getBackgroundAttachment() {
		return getPropertyValue("background-attachment");
	}

	@Override
	public void setBackgroundAttachment(String backgroundAttachment) throws DOMException {
		getPropertyCSSValue("background-attachment").setCssText(backgroundAttachment);
	}

	@Override
	public String getBackgroundColor() {
		return getPropertyValue("background-color");
	}

	@Override
	public void setBackgroundColor(String backgroundColor) throws DOMException {
		getPropertyCSSValue("background-color").setCssText(backgroundColor);
	}

	@Override
	public String getBackgroundImage() {
		return getPropertyValue("background-image");
	}

	@Override
	public void setBackgroundImage(String backgroundImage) throws DOMException {
		getPropertyCSSValue("background-attachment").setCssText(backgroundImage);
	}

	@Override
	public String getBackgroundPosition() {
		return getPropertyValue("background-position");
	}

	@Override
	public void setBackgroundPosition(String backgroundPosition) throws DOMException {
		getPropertyCSSValue("background-position").setCssText(backgroundPosition);
	}

	@Override
	public String getBackgroundRepeat() {
		return getPropertyValue("background-repeat");
	}

	@Override
	public void setBackgroundRepeat(String backgroundRepeat) throws DOMException {
		getPropertyCSSValue("background-repeat").setCssText(backgroundRepeat);
	}

	@Override
	public String getBorder() {
		return getPropertyValue("border");
	}

	@Override
	public void setBorder(String border) throws DOMException {
		getPropertyCSSValue("border").setCssText(border);
	}

	@Override
	public String getBorderCollapse() {
		return getPropertyValue("border-collapse");
	}

	@Override
	public void setBorderCollapse(String borderCollapse) throws DOMException {
		getPropertyCSSValue("border-collapse").setCssText(borderCollapse);
	}

	@Override
	public String getBorderColor() {
		return getPropertyValue("border-color");
	}

	@Override
	public void setBorderColor(String borderColor) throws DOMException {
		getPropertyCSSValue("border-color").setCssText(borderColor);
	}

	@Override
	public String getBorderSpacing() {
		return getPropertyValue("border-spacing");
	}

	@Override
	public void setBorderSpacing(String borderSpacing) throws DOMException {
		getPropertyCSSValue("border-spacing").setCssText(borderSpacing);
	}

	@Override
	public String getBorderStyle() {
		return getPropertyValue("border-style");
	}

	@Override
	public void setBorderStyle(String borderStyle) throws DOMException {
		getPropertyCSSValue("border-style").setCssText(borderStyle);
	}

	@Override
	public String getBorderTop() {
		return getPropertyValue("border-top");
	}

	@Override
	public void setBorderTop(String borderTop) throws DOMException {
		getPropertyCSSValue("border-top").setCssText(borderTop);
	}

	@Override
	public String getBorderRight() {
		return getPropertyValue("border-right");
	}

	@Override
	public void setBorderRight(String borderRight) throws DOMException {
		getPropertyCSSValue("border-right").setCssText(borderRight);
	}

	@Override
	public String getBorderBottom() {
		return getPropertyValue("border-bottom");
	}

	@Override
	public void setBorderBottom(String borderBottom) throws DOMException {
		getPropertyCSSValue("border-bottom").setCssText(borderBottom);
	}

	@Override
	public String getBorderLeft() {
		return getPropertyValue("border-left");
	}

	@Override
	public void setBorderLeft(String borderLeft) throws DOMException {
		getPropertyCSSValue("border-left").setCssText(borderLeft);
	}

	@Override
	public String getBorderTopColor() {
		return getPropertyValue("border-top-color");
	}

	@Override
	public void setBorderTopColor(String borderTopColor) throws DOMException {
		getPropertyCSSValue("border-top-color").setCssText(borderTopColor);
	}

	@Override
	public String getBorderRightColor() {
		return getPropertyValue("border-right-color");
	}

	@Override
	public void setBorderRightColor(String borderRightColor) throws DOMException {
		getPropertyCSSValue("border-right-color").setCssText(borderRightColor);
	}

	@Override
	public String getBorderBottomColor() {
		return getPropertyValue("border-bottom-color");
	}

	@Override
	public void setBorderBottomColor(String borderBottomColor) throws DOMException {
		getPropertyCSSValue("border-bottom-color").setCssText(borderBottomColor);
	}

	@Override
	public String getBorderLeftColor() {
		return getPropertyValue("border-left-color");
	}

	@Override
	public void setBorderLeftColor(String borderLeftColor) throws DOMException {
		getPropertyCSSValue("border-left-color").setCssText(borderLeftColor);
	}

	@Override
	public String getBorderTopStyle() {
		return getPropertyValue("border-top-style");
	}

	@Override
	public void setBorderTopStyle(String borderTopStyle) throws DOMException {
		getPropertyCSSValue("border-top-style").setCssText(borderTopStyle);
	}

	@Override
	public String getBorderRightStyle() {
		return getPropertyValue("border-right-style");
	}

	@Override
	public void setBorderRightStyle(String borderRightStyle) throws DOMException {
		getPropertyCSSValue("border-right-style").setCssText(borderRightStyle);
	}

	@Override
	public String getBorderBottomStyle() {
		return getPropertyValue("border-bottom-style");
	}

	@Override
	public void setBorderBottomStyle(String borderBottomStyle) throws DOMException {
		getPropertyCSSValue("border-bottom-style").setCssText(borderBottomStyle);
	}

	@Override
	public String getBorderLeftStyle() {
		return getPropertyValue("border-left-style");
	}

	@Override
	public void setBorderLeftStyle(String borderLeftStyle) throws DOMException {
		getPropertyCSSValue("border-left-style").setCssText(borderLeftStyle);
	}

	@Override
	public String getBorderTopWidth() {
		return getPropertyValue("border-top-width");
	}

	@Override
	public void setBorderTopWidth(String borderTopWidth) throws DOMException {
		getPropertyCSSValue("border-top-width").setCssText(borderTopWidth);
	}

	@Override
	public String getBorderRightWidth() {
		return getPropertyValue("border-right-width");
	}

	@Override
	public void setBorderRightWidth(String borderRightWidth) throws DOMException {
		getPropertyCSSValue("border-right-width").setCssText(borderRightWidth);
	}

	@Override
	public String getBorderBottomWidth() {
		return getPropertyValue("border-bottom-width");
	}

	@Override
	public void setBorderBottomWidth(String borderBottomWidth) throws DOMException {
		getPropertyCSSValue("border-bottom-width").setCssText(borderBottomWidth);
	}

	@Override
	public String getBorderLeftWidth() {
		return getPropertyValue("border-left-width");
	}

	@Override
	public void setBorderLeftWidth(String borderLeftWidth) throws DOMException {
		getPropertyCSSValue("border-left-width").setCssText(borderLeftWidth);
	}

	@Override
	public String getBorderWidth() {
		return getPropertyValue("border-width");
	}

	@Override
	public void setBorderWidth(String borderWidth) throws DOMException {
		getPropertyCSSValue("border-width").setCssText(borderWidth);
	}

	@Override
	public String getBottom() {
		return getPropertyValue("bottom");
	}

	@Override
	public void setBottom(String bottom) throws DOMException {
		getPropertyCSSValue("bottom").setCssText(bottom);
	}

	@Override
	public String getCaptionSide() {
		return getPropertyValue("caption-side");
	}

	@Override
	public void setCaptionSide(String captionSide) throws DOMException {
		getPropertyCSSValue("caption-side").setCssText(captionSide);
	}

	@Override
	public String getClear() {
		return getPropertyValue("clear");
	}

	@Override
	public void setClear(String clear) throws DOMException {
		getPropertyCSSValue("clear").setCssText(clear);
	}

	@Override
	public String getClip() {
		return getPropertyValue("clip");
	}

	@Override
	public void setClip(String clip) throws DOMException {
		getPropertyCSSValue("clip").setCssText(clip);
	}

	@Override
	public String getColor() {
		return getPropertyValue("color");
	}

	@Override
	public void setColor(String color) throws DOMException {
		getPropertyCSSValue("color").setCssText(color);
	}

	@Override
	public String getContent() {
		return getPropertyValue("content");
	}

	@Override
	public void setContent(String content) throws DOMException {
		getPropertyCSSValue("content").setCssText(content);
	}

	@Override
	public String getCounterIncrement() {
		return getPropertyValue("counter-increment");
	}

	@Override
	public void setCounterIncrement(String counterIncrement) throws DOMException {
		getPropertyCSSValue("counter-increment").setCssText(counterIncrement);
	}

	@Override
	public String getCounterReset() {
		return getPropertyValue("counter-reset");
	}

	@Override
	public void setCounterReset(String counterReset) throws DOMException {
		getPropertyCSSValue("counter-reset").setCssText(counterReset);
	}

	@Override
	public String getCue() {
		return getPropertyValue("cue");
	}

	@Override
	public void setCue(String cue) throws DOMException {
		getPropertyCSSValue("cue").setCssText(cue);
	}

	@Override
	public String getCueAfter() {
		return getPropertyValue("cue-after");
	}

	@Override
	public void setCueAfter(String cueAfter) throws DOMException {
		getPropertyCSSValue("cue-after").setCssText(cueAfter);
	}

	@Override
	public String getCueBefore() {
		return getPropertyValue("cue-before");
	}

	@Override
	public void setCueBefore(String cueBefore) throws DOMException {
		getPropertyCSSValue("cue-before").setCssText(cueBefore);
	}

	@Override
	public String getCursor() {
		return getPropertyValue("cursor");
	}

	@Override
	public void setCursor(String cursor) throws DOMException {
		getPropertyCSSValue("cursor").setCssText(cursor);
	}

	@Override
	public String getDirection() {
		return getPropertyValue("direction");
	}

	@Override
	public void setDirection(String direction) throws DOMException {
		getPropertyCSSValue("direction").setCssText(direction);
	}

	@Override
	public String getDisplay() {
		return getPropertyValue("display");
	}

	@Override
	public void setDisplay(String display) throws DOMException {
		getPropertyCSSValue("display").setCssText(display);
	}

	@Override
	public String getElevation() {
		return getPropertyValue("elevation");
	}

	@Override
	public void setElevation(String elevation) throws DOMException {
		getPropertyCSSValue("elevation").setCssText(elevation);
	}

	@Override
	public String getEmptyCells() {
		return getPropertyValue("empty-cells");
	}

	@Override
	public void setEmptyCells(String emptyCells) throws DOMException {
		getPropertyCSSValue("empty-cells").setCssText(emptyCells);
	}

	@Override
	public String getCssFloat() {
		return getPropertyValue("css-float");
	}

	@Override
	public void setCssFloat(String cssFloat) throws DOMException {
		getPropertyCSSValue("css-float").setCssText(cssFloat);
	}

	@Override
	public String getFont() {
		return getPropertyValue("font");
	}

	@Override
	public void setFont(String font) throws DOMException {
		getPropertyCSSValue("font").setCssText(font);
	}

	@Override
	public String getFontFamily() {
		return getPropertyValue("font-family");
	}

	@Override
	public void setFontFamily(String fontFamily) throws DOMException {
		getPropertyCSSValue("font-family").setCssText(fontFamily);
	}

	@Override
	public String getFontSize() {
		return getPropertyValue("font-size");
	}

	@Override
	public void setFontSize(String fontSize) throws DOMException {
		getPropertyCSSValue("font-size").setCssText(fontSize);
	}

	@Override
	public String getFontSizeAdjust() {
		return getPropertyValue("font-size-adjust");
	}

	@Override
	public void setFontSizeAdjust(String fontSizeAdjust) throws DOMException {
		getPropertyCSSValue("font-size-adjust").setCssText(fontSizeAdjust);
	}

	@Override
	public String getFontStretch() {
		return getPropertyValue("font-stretch");
	}

	@Override
	public void setFontStretch(String fontStretch) throws DOMException {
		getPropertyCSSValue("font-stretch").setCssText(fontStretch);
	}

	@Override
	public String getFontStyle() {
		return getPropertyValue("font-style");
	}

	@Override
	public void setFontStyle(String fontStyle) throws DOMException {
		getPropertyCSSValue("font-style").setCssText(fontStyle);
	}

	@Override
	public String getFontVariant() {
		return getPropertyValue("font-variant");
	}

	@Override
	public void setFontVariant(String fontVariant) throws DOMException {
		getPropertyCSSValue("font-variant").setCssText(fontVariant);
	}

	@Override
	public String getFontWeight() {
		return getPropertyValue("font-weight");
	}

	@Override
	public void setFontWeight(String fontWeight) throws DOMException {
		getPropertyCSSValue("font-weight").setCssText(fontWeight);
	}

	@Override
	public String getHeight() {
		return getPropertyValue("height");
	}

	@Override
	public void setHeight(String height) throws DOMException {
		getPropertyCSSValue("height").setCssText(height);
	}

	@Override
	public String getLeft() {
		return getPropertyValue("left");
	}

	@Override
	public void setLeft(String left) throws DOMException {
		getPropertyCSSValue("left").setCssText(left);
	}

	@Override
	public String getLetterSpacing() {
		return getPropertyValue("letter-spacing");
	}

	@Override
	public void setLetterSpacing(String letterSpacing) throws DOMException {
		getPropertyCSSValue("letter-spacing").setCssText(letterSpacing);
	}

	@Override
	public String getLineHeight() {
		return getPropertyValue("line-height");
	}

	@Override
	public void setLineHeight(String lineHeight) throws DOMException {
		getPropertyCSSValue("line-height").setCssText(lineHeight);
	}

	@Override
	public String getListStyle() {
		return getPropertyValue("list-style");
	}

	@Override
	public void setListStyle(String listStyle) throws DOMException {
		getPropertyCSSValue("list-style").setCssText(listStyle);
	}

	@Override
	public String getListStyleImage() {
		return getPropertyValue("list-style-image");
	}

	@Override
	public void setListStyleImage(String listStyleImage) throws DOMException {
		getPropertyCSSValue("list-style-image").setCssText(listStyleImage);
	}

	@Override
	public String getListStylePosition() {
		return getPropertyValue("list-style-position");
	}

	@Override
	public void setListStylePosition(String listStylePosition) throws DOMException {
		getPropertyCSSValue("list-style-position").setCssText(listStylePosition);
	}

	@Override
	public String getListStyleType() {
		return getPropertyValue("list-style-type");
	}

	@Override
	public void setListStyleType(String listStyleType) throws DOMException {
		getPropertyCSSValue("list-style-type").setCssText(listStyleType);
	}

	@Override
	public String getMargin() {
		return getPropertyValue("margin");
	}

	@Override
	public void setMargin(String margin) throws DOMException {
		getPropertyCSSValue("margin").setCssText(margin);
	}

	@Override
	public String getMarginTop() {
		return getPropertyValue("margin-top");
	}

	@Override
	public void setMarginTop(String marginTop) throws DOMException {
		getPropertyCSSValue("margin-top").setCssText(marginTop);
	}

	@Override
	public String getMarginRight() {
		return getPropertyValue("margin-right");
	}

	@Override
	public void setMarginRight(String marginRight) throws DOMException {
		getPropertyCSSValue("margin-right").setCssText(marginRight);
	}

	@Override
	public String getMarginBottom() {
		return getPropertyValue("margin-bottom");
	}

	@Override
	public void setMarginBottom(String marginBottom) throws DOMException {
		getPropertyCSSValue("margin-bottom").setCssText(marginBottom);
	}

	@Override
	public String getMarginLeft() {
		return getPropertyValue("margin-left");
	}

	@Override
	public void setMarginLeft(String marginLeft) throws DOMException {
		getPropertyCSSValue("margin-left").setCssText(marginLeft);
	}

	@Override
	public String getMarkerOffset() {
		return getPropertyValue("marker-offset");
	}

	@Override
	public void setMarkerOffset(String markerOffset) throws DOMException {
		getPropertyCSSValue("marker-offset").setCssText(markerOffset);
	}

	@Override
	public String getMarks() {
		return getPropertyValue("marks");
	}

	@Override
	public void setMarks(String marks) throws DOMException {
		getPropertyCSSValue("marks").setCssText(marks);
	}

	@Override
	public String getMaxHeight() {
		return getPropertyValue("max-height");
	}

	@Override
	public void setMaxHeight(String maxHeight) throws DOMException {
		getPropertyCSSValue("max-height").setCssText(maxHeight);
	}

	@Override
	public String getMaxWidth() {
		return getPropertyValue("max-width");
	}

	@Override
	public void setMaxWidth(String maxWidth) throws DOMException {
		getPropertyCSSValue("max-width").setCssText(maxWidth);
	}

	@Override
	public String getMinHeight() {
		return getPropertyValue("min-height");
	}

	@Override
	public void setMinHeight(String minHeight) throws DOMException {
		getPropertyCSSValue("min-height").setCssText(minHeight);
	}

	@Override
	public String getMinWidth() {
		return getPropertyValue("min-width");
	}

	@Override
	public void setMinWidth(String minWidth) throws DOMException {
		getPropertyCSSValue("min-width").setCssText(minWidth);
	}

	@Override
	public String getOrphans() {
		return getPropertyValue("orphans");
	}

	@Override
	public void setOrphans(String orphans) throws DOMException {
		getPropertyCSSValue("orphans").setCssText(orphans);
	}

	@Override
	public String getOutline() {
		return getPropertyValue("outline");
	}

	@Override
	public void setOutline(String outline) throws DOMException {
		getPropertyCSSValue("outline").setCssText(outline);
	}

	@Override
	public String getOutlineColor() {
		return getPropertyValue("outline-color");
	}

	@Override
	public void setOutlineColor(String outlineColor) throws DOMException {
		getPropertyCSSValue("outline-color").setCssText(outlineColor);
	}

	@Override
	public String getOutlineStyle() {
		return getPropertyValue("outline-style");
	}

	@Override
	public void setOutlineStyle(String outlineStyle) throws DOMException {
		getPropertyCSSValue("outline-style").setCssText(outlineStyle);
	}

	@Override
	public String getOutlineWidth() {
		return getPropertyValue("outline-width");
	}

	@Override
	public void setOutlineWidth(String outlineWidth) throws DOMException {
		getPropertyCSSValue("outline-width").setCssText(outlineWidth);
	}

	@Override
	public String getOverflow() {
		return getPropertyValue("overflow");
	}

	@Override
	public void setOverflow(String overflow) throws DOMException {
		getPropertyCSSValue("overflow").setCssText(overflow);
	}

	@Override
	public String getPadding() {
		return getPropertyValue("padding");
	}

	@Override
	public void setPadding(String padding) throws DOMException {
		getPropertyCSSValue("padding").setCssText(padding);
	}

	@Override
	public String getPaddingTop() {
		return getPropertyValue("padding-top");
	}

	@Override
	public void setPaddingTop(String paddingTop) throws DOMException {
		getPropertyCSSValue("padding-top").setCssText(paddingTop);
	}

	@Override
	public String getPaddingRight() {
		return getPropertyValue("padding-right");
	}

	@Override
	public void setPaddingRight(String paddingRight) throws DOMException {
		getPropertyCSSValue("padding-right").setCssText(paddingRight);
	}

	@Override
	public String getPaddingBottom() {
		return getPropertyValue("padding-bottom");
	}

	@Override
	public void setPaddingBottom(String paddingBottom) throws DOMException {
		getPropertyCSSValue("padding-bottom").setCssText(paddingBottom);
	}

	@Override
	public String getPaddingLeft() {
		return getPropertyValue("padding-left");
	}

	@Override
	public void setPaddingLeft(String paddingLeft) throws DOMException {
		getPropertyCSSValue("padding-left").setCssText(paddingLeft);
	}

	@Override
	public String getPage() {
		return getPropertyValue("page");
	}

	@Override
	public void setPage(String page) throws DOMException {
		getPropertyCSSValue("page").setCssText(page);
	}

	@Override
	public String getPageBreakAfter() {
		return getPropertyValue("page-break-after");
	}

	@Override
	public void setPageBreakAfter(String pageBreakAfter) throws DOMException {
		getPropertyCSSValue("page-break-after").setCssText(pageBreakAfter);
	}

	@Override
	public String getPageBreakBefore() {
		return getPropertyValue("page-break-before");
	}

	@Override
	public void setPageBreakBefore(String pageBreakBefore) throws DOMException {
		getPropertyCSSValue("page-break-before").setCssText(pageBreakBefore);
	}

	@Override
	public String getPageBreakInside() {
		return getPropertyValue("page-break-inside");
	}

	@Override
	public void setPageBreakInside(String pageBreakInside) throws DOMException {
		getPropertyCSSValue("page-break-inside").setCssText(pageBreakInside);
	}

	@Override
	public String getPause() {
		return getPropertyValue("pause");
	}

	@Override
	public void setPause(String pause) throws DOMException {
		getPropertyCSSValue("pause").setCssText(pause);
	}

	@Override
	public String getPauseAfter() {
		return getPropertyValue("pause-after");
	}

	@Override
	public void setPauseAfter(String pauseAfter) throws DOMException {
		getPropertyCSSValue("pause-after").setCssText(pauseAfter);
	}

	@Override
	public String getPauseBefore() {
		return getPropertyValue("pause-before");
	}

	@Override
	public void setPauseBefore(String pauseBefore) throws DOMException {
		getPropertyCSSValue("pause-before").setCssText(pauseBefore);
	}

	@Override
	public String getPitch() {
		return getPropertyValue("pitch");
	}

	@Override
	public void setPitch(String pitch) throws DOMException {
		getPropertyCSSValue("pitch").setCssText(pitch);
	}

	@Override
	public String getPitchRange() {
		return getPropertyValue("pitch-range");
	}

	@Override
	public void setPitchRange(String pitchRange) throws DOMException {
		getPropertyCSSValue("pitch-range").setCssText(pitchRange);
	}

	@Override
	public String getPlayDuring() {
		return getPropertyValue("play-during");
	}

	@Override
	public void setPlayDuring(String playDuring) throws DOMException {
		getPropertyCSSValue("play-during").setCssText(playDuring);
	}

	@Override
	public String getPosition() {
		return getPropertyValue("position");
	}

	@Override
	public void setPosition(String position) throws DOMException {
		getPropertyCSSValue("position").setCssText(position);
	}

	@Override
	public String getQuotes() {
		return getPropertyValue("quotes");
	}

	@Override
	public void setQuotes(String quotes) throws DOMException {
		getPropertyCSSValue("quotes").setCssText(quotes);
	}

	@Override
	public String getRichness() {
		return getPropertyValue("richness");
	}

	@Override
	public void setRichness(String richness) throws DOMException {
		getPropertyCSSValue("richness").setCssText(richness);
	}

	@Override
	public String getRight() {
		return getPropertyValue("right");
	}

	@Override
	public void setRight(String right) throws DOMException {
		getPropertyCSSValue("right").setCssText(right);
	}

	@Override
	public String getSize() {
		return getPropertyValue("size");
	}

	@Override
	public void setSize(String size) throws DOMException {
		getPropertyCSSValue("size").setCssText(size);
	}

	@Override
	public String getSpeak() {
		return getPropertyValue("speak");
	}

	@Override
	public void setSpeak(String speak) throws DOMException {
		getPropertyCSSValue("speak").setCssText(speak);
	}

	@Override
	public String getSpeakHeader() {
		return getPropertyValue("speak-header");
	}

	@Override
	public void setSpeakHeader(String speakHeader) throws DOMException {
		getPropertyCSSValue("speak-header").setCssText(speakHeader);
	}

	@Override
	public String getSpeakNumeral() {
		return getPropertyValue("speak-numeral");
	}

	@Override
	public void setSpeakNumeral(String speakNumeral) throws DOMException {
		getPropertyCSSValue("speak-numeral").setCssText(speakNumeral);
	}

	@Override
	public String getSpeakPunctuation() {
		return getPropertyValue("speak-punctuation");
	}

	@Override
	public void setSpeakPunctuation(String speakPunctuation) throws DOMException {
		getPropertyCSSValue("speak-punctuation").setCssText(speakPunctuation);
	}

	@Override
	public String getSpeechRate() {
		return getPropertyValue("speech-rate");
	}

	@Override
	public void setSpeechRate(String speechRate) throws DOMException {
		getPropertyCSSValue("speech-rate").setCssText(speechRate);
	}

	@Override
	public String getStress() {
		return getPropertyValue("stress");
	}

	@Override
	public void setStress(String stress) throws DOMException {
		getPropertyCSSValue("stress").setCssText(stress);
	}

	@Override
	public String getTableLayout() {
		return getPropertyValue("table-layout");
	}

	@Override
	public void setTableLayout(String tableLayout) throws DOMException {
		getPropertyCSSValue("table-layout").setCssText(tableLayout);
	}

	@Override
	public String getTextAlign() {
		return getPropertyValue("text-align");
	}

	@Override
	public void setTextAlign(String textAlign) throws DOMException {
		getPropertyCSSValue("text-align").setCssText(textAlign);
	}

	@Override
	public String getTextDecoration() {
		return getPropertyValue("text-decoration");
	}

	@Override
	public void setTextDecoration(String textDecoration) throws DOMException {
		getPropertyCSSValue("text-decoration").setCssText(textDecoration);
	}

	@Override
	public String getTextIndent() {
		return getPropertyValue("text-indent");
	}

	@Override
	public void setTextIndent(String textIndent) throws DOMException {
		getPropertyCSSValue("text-indent").setCssText(textIndent);
	}

	@Override
	public String getTextShadow() {
		return getPropertyValue("text-shadow");
	}

	@Override
	public void setTextShadow(String textShadow) throws DOMException {
		getPropertyCSSValue("text-shadow").setCssText(textShadow);
	}

	@Override
	public String getTextTransform() {
		return getPropertyValue("text-transform");
	}

	@Override
	public void setTextTransform(String textTransform) throws DOMException {
		getPropertyCSSValue("text-transform").setCssText(textTransform);
	}

	@Override
	public String getTop() {
		return getPropertyValue("top");
	}

	@Override
	public void setTop(String top) throws DOMException {
		getPropertyCSSValue("top").setCssText(top);
	}

	@Override
	public String getUnicodeBidi() {
		return getPropertyValue("unicode-bidi");
	}

	@Override
	public void setUnicodeBidi(String unicodeBidi) throws DOMException {
		getPropertyCSSValue("unicode-bidi").setCssText(unicodeBidi);
	}

	@Override
	public String getVerticalAlign() {
		return getPropertyValue("vertical-align");
	}

	@Override
	public void setVerticalAlign(String verticalAlign) throws DOMException {
		getPropertyCSSValue("vertical-align").setCssText(verticalAlign);
	}

	@Override
	public String getVisibility() {
		return getPropertyValue("visibility");
	}

	@Override
	public void setVisibility(String visibility) throws DOMException {
		getPropertyCSSValue("visibility").setCssText(visibility);
	}

	@Override
	public String getVoiceFamily() {
		return getPropertyValue("voice-family");
	}

	@Override
	public void setVoiceFamily(String voiceFamily) throws DOMException {
		getPropertyCSSValue("voice-family").setCssText(voiceFamily);
	}

	@Override
	public String getVolume() {
		return getPropertyValue("volume");
	}

	@Override
	public void setVolume(String volume) throws DOMException {
		getPropertyCSSValue("volume").setCssText(volume);
	}

	@Override
	public String getWhiteSpace() {
		return getPropertyValue("white-space");
	}

	@Override
	public void setWhiteSpace(String whiteSpace) throws DOMException {
		getPropertyCSSValue("white-space").setCssText(whiteSpace);
	}

	@Override
	public String getWidows() {
		return null;
	}

	@Override
	public void setWidows(String widows) throws DOMException {
		getPropertyCSSValue("widows").setCssText(widows);
	}

	@Override
	public String getWidth() {
		return getPropertyValue("width");
	}

	@Override
	public void setWidth(String width) throws DOMException {
		getPropertyCSSValue("width").setCssText(width);
	}

	@Override
	public String getWordSpacing() {
		return getPropertyValue("word-spacing");
	}

	@Override
	public void setWordSpacing(String wordSpacing) throws DOMException {
		getPropertyCSSValue("word-spacing").setCssText(wordSpacing);
	}

	@Override
	public String getZIndex() {
		return getPropertyValue("z-index");
	}

	@Override
	public void setZIndex(String zIndex) throws DOMException {
		getPropertyCSSValue("z-index").setCssText(zIndex);
	}

}
