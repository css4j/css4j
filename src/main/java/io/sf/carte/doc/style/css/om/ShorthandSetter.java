/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.InitialValue;
import io.sf.carte.doc.style.css.property.KeywordValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.RevertValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.UnsetValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueItem;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Generic class that attempts to set the subproperties of shorthand properties.
 *
 * @author Carlos Amengual
 *
 */
class ShorthandSetter extends BaseShorthandSetter {

	private static CSSValueSyntax imageSyntax = new SyntaxParser().parseSyntax("<image>");

	private boolean priorityImportant = false;

	final ValueFactory valueFactory;

	private final HashMap<String, StyleValue> mypropValue = new HashMap<>();

	private final List<String> mypropertyList = new ArrayList<>();

	private final List<Boolean> mypriorities = new ArrayList<>();

	protected LexicalUnit currentValue = null;

	private final StringBuilder valueBuffer = new StringBuilder(40);

	private final StringBuilder miniValueBuffer = new StringBuilder(40);

	private transient boolean attrTainted;

	/**
	 * The values in the shorthand are attempted to set subproperty values in a certain order.
	 * The properties that failed to be set to the tested value are stored here.
	 */
	private final ArrayList<String> unassignedProperties = new ArrayList<>(6);

	ShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super(style, shorthandName);
		valueFactory = style.getValueFactory();
	}

	public boolean isPriorityImportant() {
		return priorityImportant;
	}

	String getPriority() {
		return priorityImportant ? "important" : null;
	}

	void setPriority(boolean important) {
		this.priorityImportant = important;
	}

	/**
	 * @return {@code true} if the shorthand is attr()-tainted.
	 */
	protected boolean isAttrTainted() {
		return attrTainted;
	}

	/**
	 * @param attrTainted {@code true} if the shorthand is attr()-tainted.
	 */
	@Override
	public void setAttrTainted(boolean attrTainted) {
		this.attrTainted = attrTainted;
	}

	/**
	 * Check whether this shorthand contains any IE compatibility value
	 * (<code>COMPAT_IDENT</code> or <code>COMPAT_PRIO</code>).
	 * <p>
	 * If any of those values is present, that means that the factory is configured
	 * for compatibility (no need to check for that).
	 * 
	 * @return <code>true</code> if the current value contains an IE compatibility
	 *         value.
	 */
	boolean hasCompatValue() {
		LexicalUnit lu = currentValue;
		while (lu != null) {
			LexicalType type = lu.getLexicalUnitType();
			if (type == LexicalType.COMPAT_IDENT || type == LexicalType.COMPAT_PRIO) {
				return true;
			}
			lu = lu.getNextLexicalUnit();
		}
		return false;
	}

	void addUnassignedProperty(String propertyName) {
		unassignedProperties.add(propertyName);
	}

	List<String> getUnassignedProperties() {
		return unassignedProperties;
	}

	boolean scanUnassigned(List<LexicalUnit> unassignedValues) {
		valueloop: for (LexicalUnit lu : unassignedValues) {
			if (lu.getLexicalUnitType() == LexicalType.IDENT) {
				if (!isNotValidIdentifier(lu)) {
					continue;
				}
				String sv = lu.getStringValue();
				Iterator<String> it = getUnassignedProperties().iterator();
				while (it.hasNext()) {
					String property = it.next();
					if (property.endsWith("-color") && sv.endsWith("-color")) {
						setSubpropertyValue(property, createCSSValue(property, lu));
						it.remove();
						continue valueloop;
					}
				}
			} else if (lu.getLexicalUnitType() == LexicalType.FUNCTION) {
				List<String> unass = getUnassignedProperties();
				Iterator<String> it = unass.iterator();
				while (it.hasNext()) {
					String property = it.next();
					if (property.endsWith("-color")) {
						LexicalUnit param = lu.getParameters();
						while (param != null) {
							if (testColor(param)) {
								setSubpropertyValue(property, createCSSValue(property, lu));
								it.remove();
								continue valueloop;
							}
							param = param.getNextLexicalUnit();
						}
					}
				}
			} else if (lu.getLexicalUnitType() == LexicalType.VAR) {
				List<String> unass = getUnassignedProperties();
				Iterator<String> it = unass.iterator();
				while (it.hasNext()) {
					String property = it.next();
					if (property.endsWith("-color")) {
						if (unass.size() == 1) {
							setSubpropertyValue(property, createCSSValue(property, lu));
							it.remove();
							break valueloop;
						}
					} else if (property.endsWith("-image") && unass.size() == 1) {
						setSubpropertyValue(property, createCSSValue(property, lu));
						it.remove();
						break valueloop;
					}
				}
			}
			// Unable to assign this value.
			StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
			if (errHandler != null) {
				errHandler.unassignedShorthandValue(getShorthandName(), lu.toString());
			}
			return false;
		}
		return true;
	}

	/**
	 * Look for CSS-wide keywords.
	 * 
	 * @param lunit
	 *            the lexical unit to be scanned.
	 * @returns 0 if no CSS-wide keyword was found, 1 if all values are keywords, or 2 if a
	 *          keyword was found mixed with other values.
	 */
	byte scanForCssWideKeywords(LexicalUnit lunit) {
		KeywordValue kwval = createKeywordValueSubproperty(lunit);
		if (kwval != null) {
			if (lunit.getNextLexicalUnit() != null) {
				reportMixedKeywords(kwval.getCssText());
				return 2;
			}
			setSubpropertiesToKeyword(kwval);
			initValueString();
			appendValueItemString(kwval);
			return 1;
		}
		return 0;
	}

	KeywordValue createKeywordValueSubproperty(LexicalUnit lunit) {
		KeywordValue kwval;
		switch (lunit.getLexicalUnitType()) {
		case INHERIT:
			kwval = InheritValue.getValue().asSubproperty();
			break;
		case INITIAL:
			kwval = InitialValue.getValue().asSubproperty();
			break;
		case UNSET:
			kwval = UnsetValue.getValue().asSubproperty();
			break;
		case REVERT:
			kwval = RevertValue.getValue().asSubproperty();
			break;
		default:
			kwval = null;
		}
		return kwval;
	}

	private void reportMixedKeywords(String keyword) {
		// report error: more than one single keyword was found
		StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
		if (errHandler != null) {
			errHandler.shorthandError(getShorthandName(), "Found '" + keyword + "' keyword mixed with other values");
		}
	}

	protected void setSubpropertiesToKeyword(StyleValue keyword) {
		String[] subparray = getShorthandSubproperties();
		for (String subp : subparray) {
			if (!getShorthandDatabase().isShorthand(subp)) {
				styleDeclaration.setProperty(subp, keyword, isPriorityImportant());
			} else {
				String[] sh = getShorthandDatabase().getShorthandSubproperties(subp);
				for (String s : sh) {
					styleDeclaration.setProperty(s, keyword, isPriorityImportant());
				}
			}
		}
	}

	void setListSubpropertyValue(String pname, ValueList list) {
		if (list.getLength() == 1) {
			StyleValue val = list.item(0);
			CssType cat;
			if (val.isPrimitiveValue()) {
				((PrimitiveValue) val).setSubproperty(true);
			} else if ((cat = val.getCssValueType()) == CssType.KEYWORD) {
				val = ((KeywordValue) val).asSubproperty();
			} else if (cat == CssType.LIST) {
				((ValueList) val).setSubproperty(true);
			}
			setSubpropertyValue(pname, val);
		} else {
			list.setSubproperty(true);
			setSubpropertyValue(pname, list);
		}
	}

	protected String[] getShorthandSubproperties() {
		return getShorthandDatabase().getShorthandSubproperties(getShorthandName());
	}

	/**
	 * Test if the value is a color.
	 * 
	 * @param lunit the lexical unit to test.
	 * @return true if the value is a color.
	 */
	static boolean testColor(LexicalUnit lunit) {
		CSSValueSyntax syn = SyntaxParser.createSimpleSyntax("color");
		return lunit.shallowClone().matches(syn) == Match.TRUE;
	}

	protected void nextCurrentValue() {
		if (currentValue != null) {
			currentValue = currentValue.getNextLexicalUnit();
			// Add the value string. We do this here in case the shorthand
			// setter decides to stop setting values, to avoid having -in
			// the text representation- values that were not used as subproperties.
			appendValueItemString();
		}
	}

	@Override
	public void init(LexicalUnit shorthandValue, boolean important) {
		this.currentValue = shorthandValue;
		setPriority(important);
		// Reset the variables
		unassignedProperties.clear();
		initValueString();
		appendValueItemString();
	}

	/**
	 * Reset subproperties not explicitly set by this shorthand.
	 */
	protected void resetSubproperties() {
		List<String> props = getUnassignedProperties();
		for (String pname : props) {
			if (!getShorthandDatabase().isShorthand(pname)) {
				setPropertyToDefault(pname);
			} else {
				String[] longhands = getShorthandDatabase().getShorthandSubproperties(pname);
				for (String longhand : longhands) {
					setPropertyToDefault(longhand);
				}
			}
		}
	}

	/**
	 * Set to default all subproperties.
	 */
	protected void setSubpropertiesToDefault() {
		String[] subp = getShorthandSubproperties();
		for (String pname : subp) {
			if (!getShorthandDatabase().isShorthand(pname)) {
				setPropertyToDefault(pname);
			} else {
				String[] longhands = getShorthandDatabase().getShorthandSubproperties(pname);
				for (String longhand : longhands) {
					setPropertyToDefault(longhand);
				}
			}
		}
	}

	/**
	 * Computes the initial (default) value for the given property.
	 * 
	 * @param propertyName
	 *            the name of the property.
	 * @return the initial value for the property, or null if none was found.
	 */
	StyleValue defaultPropertyValue(String propertyName) {
		StyleValue cssVal = styleDeclaration.defaultPropertyValue(propertyName);
		if (cssVal != null) {
			CssType type = cssVal.getCssValueType();
			if (type == CssType.TYPED) {
				((PrimitiveValue) cssVal).setSubproperty(true);
			} else if (type == CssType.LIST) {
				((ValueList) cssVal).setSubproperty(true);
			}
		} else {
			cssVal = InitialValue.getValue().asSubproperty();
		}
		return cssVal;
	}

	void setPropertyToDefault(String pname) {
		StyleValue cssVal = defaultPropertyValue(pname);
		setProperty(pname, cssVal, isPriorityImportant());
	}

	void setDeclarationPropertyToDefault(String propertyName) {
		StyleValue cssVal = defaultPropertyValue(propertyName);
		styleDeclaration.setProperty(propertyName, cssVal, isPriorityImportant());
	}

	protected List<String> subpropertyList() {
		String[] subparray = getShorthandSubproperties();
		List<String> subp = new ArrayList<>(subparray.length);
		Collections.addAll(subp, subparray);
		return subp;
	}

	@Override
	public boolean assignSubproperties() {
		boolean result = draftSubproperties();
		if (result) {
			flush();
		}
		return result;
	}

	boolean draftSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return true;
		} else if (kwscan == 2) {
			return false;
		}
		LinkedList<LexicalUnit> unassignedValues = new LinkedList<>();
		List<String> subp = subpropertyList();
		while (currentValue != null) {
			boolean assigned = false;
			for (int i = 0; i < subp.size(); i++) {
				String pname = subp.get(i);
				// Try to match this property name with the current value
				if (assignSubproperty(pname)) {
					subp.remove(i);
					assigned = true;
					break;
				}
			}
			if (!assigned) {
				unassignedValues.add(currentValue);
				nextCurrentValue();
			}
		}
		if (!subp.isEmpty()) {
			// Add remaining unassigned properties
			Iterator<String> it = subp.iterator();
			while (it.hasNext()) {
				addUnassignedProperty(it.next());
			}
		}
		if (!unassignedValues.isEmpty() && !scanUnassigned(unassignedValues)) {
			return false;
		}
		// Reset subproperties not set by this shorthand
		resetSubproperties();
		return true;
	}

	boolean isNotValidIdentifier(LexicalUnit lu) {
		return !lu.getStringValue().equalsIgnoreCase("none");
	}

	protected boolean assignSubproperty(String subproperty) {
		LexicalType lutype = currentValue.getLexicalUnitType();
		if (lutype == LexicalType.IDENT && assignIdentifiers(subproperty)) {
			return true;
		}
		if (subproperty.endsWith("-color")) {
			if (testColor(currentValue)) {
				return setCurrentValue(subproperty);
			}
		} else if (subproperty.endsWith("-width")) {
			if (ValueFactory.isLengthSACUnit(currentValue)) {
				return setCurrentValue(subproperty);
			}
		} else if (subproperty.endsWith("-image") && isImage()) {
			return setCurrentValue(subproperty);
		}
		return false;
	}

	boolean isImage() {
		LexicalType type = currentValue.getLexicalUnitType();
		return type == LexicalType.URI || type == LexicalType.SRC
			|| (type == LexicalType.FUNCTION && isImageFunctionOrGradientName())
			|| type == LexicalType.ELEMENT_REFERENCE
			|| currentValue.shallowClone().matches(imageSyntax) == Match.TRUE;
	}

	private boolean isImageFunctionOrGradientName() {
		String lcfn = currentValue.getFunctionName().toLowerCase(Locale.ROOT);
		return lcfn.endsWith("-gradient") || lcfn.equals("image") || lcfn.equals("image-set")
				|| lcfn.equals("cross-fade");
	}

	boolean setCurrentValue(String subproperty) {
		StyleValue cssValue = createCSSValue(subproperty, currentValue);
		if (cssValue != null) {
			setSubpropertyValue(subproperty, cssValue);
			nextCurrentValue();
			return true;
		}
		return false;
	}

	boolean assignIdentifiers(String subproperty) {
		if (testIdentifiers(subproperty)) {
			StyleValue cssValue = createCSSValue(subproperty, currentValue);
			setSubpropertyValue(subproperty, cssValue);
			nextCurrentValue();
			return true;
		}
		return false;
	}

	boolean testIdentifiers(String subproperty) {
		return getShorthandDatabase().isIdentifierValue(subproperty, currentValue.getStringValue());
	}

	protected void setSubpropertyValue(String subproperty, StyleValue cssValue) {
		setProperty(subproperty, cssValue, isPriorityImportant());
	}

	void setSubpropertyValueWListCheck(String property, StyleValue value) {
		if (value.getCssValueType() == CssType.LIST) {
			ValueList list = (ValueList) value;
			if (list.getLength() == 1) {
				value = list.item(0);
			}
		}
		setSubpropertyValue(property, value);
	}

	void addSubpropertyValue(String subproperty, StyleValue cssValue, boolean commaList) {
		StyleValue cssval = getDeclaredCSSValue(subproperty);
		if (cssval == null) {
			setSubpropertyValue(subproperty, cssValue);
		} else {
			switch (cssval.getCssValueType()) {
			case LIST:
				((ValueList) cssval).add(cssValue);
				cssValue = cssval;
				break;
			default:
				ValueList list;
				if (commaList) {
					list = ValueList.createCSValueList();
				} else {
					list = ValueList.createWSValueList();
				}
				list.add(cssval);
				list.add(cssValue);
				cssValue = list;
			}
			setProperty(subproperty, cssValue, isPriorityImportant());
		}
	}

	void setProperty(String subpropertyName, StyleValue cssValue, boolean important) {
		mypropertyList.add(subpropertyName);
		mypriorities.add(important);
		mypropValue.put(subpropertyName, cssValue);
	}

	boolean isPropertySet(String subpropertyName) {
		return mypropertyList.contains(subpropertyName);
	}

	StyleValue getDeclaredCSSValue(String propertyName) {
		return mypropValue.get(propertyName);
	}

	void flush() {
		int i = 0;
		for (String myproperty : mypropertyList) {
			styleDeclaration.setProperty(myproperty, mypropValue.get(myproperty), mypriorities.get(i));
			i++;
		}
		mypropertyList.clear();
		mypriorities.clear();
		mypropValue.clear();
	}

	/**
	 * Creates a CSSValue item from the current lexical unit <code>currentValue</code>.
	 * <p>
	 * It sets <code>currentValue</code> to the next lexical unit.
	 * 
	 * @return the created value.
	 * @throws DOMException
	 *             if a problem was found setting the lexical value to a CSS value.
	 */
	StyleValue createCSSValue() throws DOMException {
		ValueItem item = valueFactory.createCSSValueItem(currentValue, true);
		if (item.hasWarnings()) {
			StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
			if (errHandler != null) {
				item.handleSyntaxWarnings(errHandler);
			}
		}
		currentValue = item.getNextLexicalUnit();
		return item.getCSSValue();
	}

	protected StyleValue createCSSValue(String propertyName, LexicalUnit lunit) throws DOMException {
		return createCSSValue(propertyName, lunit, true);
	}

	StyleValue createCSSValue(String propertyName, LexicalUnit lunit, boolean subproperty) throws DOMException {
		StyleValue cssVal;
		ValueItem item = valueFactory.createCSSValueItem(lunit, subproperty);
		cssVal = item.getCSSValue();
		if (item.hasWarnings()) {
			StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
			if (errHandler != null) {
				item.handleSyntaxWarnings(errHandler);
			}
		}
		return cssVal;
	}

	/**
	 * Initializes the buffers that will hold the string representation of the shorthand.
	 */
	void initValueString() {
		valueBuffer.setLength(0);
		miniValueBuffer.setLength(0);
	}

	/**
	 * Adds the given property value to the text representation of the subproperty, in order
	 * to be used in setting the css text for the shorthand.
	 * 
	 * @param cssValue
	 *            the value
	 */
	void appendValueItemString(StyleValue cssValue) {
		if (cssValue != null) {
			String cssText = cssValue.getCssText();
			String miniCssText = cssValue.getMinifiedCssText(getShorthandName());
			StringBuilder buf = getValueItemBuffer();
			StringBuilder minibuf = getValueItemBufferMini();
			int len = buf.length();
			if (len != 0) {
				char c = buf.charAt(len - 1);
				if (!isDelimiterChar(c, miniCssText)) {
					minibuf.append(' ');
				}
				buf.append(' ');
			}
			buf.append(cssText);
			minibuf.append(miniCssText);
		}
	}

	protected boolean isDelimiterChar(char c, String cssText) {
		return c == ',' || c == '/';
	}

	/**
	 * Adds the given string to the text representation of the subproperty, in order
	 * to be used in setting the css text for the shorthand.
	 * 
	 * @param text
	 *            the text
	 */
	void appendValueItemString(String text) {
		getValueItemBuffer().append(text);
		getValueItemBufferMini().append(text);
	}

	/**
	 * Adds the text representation of the current subproperty value, to be used in setting
	 * the css text for the shorthand.
	 */
	protected void appendValueItemString() {
		if (currentValue != null) {
			valueFactory.appendValueString(getValueItemBuffer(), currentValue);
			valueFactory.appendMinifiedValueString(getValueItemBufferMini(), currentValue);
		}
	}

	StringBuilder getValueItemBuffer() {
		return valueBuffer;
	}

	StringBuilder getValueItemBufferMini() {
		return miniValueBuffer;
	}

	void appendToValueBuffer(StringBuilder buf, StringBuilder minibuf) {
		valueBuffer.append(buf);
		miniValueBuffer.append(minibuf);
	}

	@Override
	public String getCssText() {
		return valueBuffer.toString();
	}

	@Override
	public String getMinifiedCssText() {
		return miniValueBuffer.toString();
	}

	@Override
	public ShorthandValue createCSSShorthandValue(LexicalUnit value) {
		return ShorthandValue.createCSSShorthandValue(getShorthandDatabase(), getShorthandName(), value,
				isPriorityImportant(), attrTainted);
	}

	void reportDeclarationError(String propertyName, String message) {
		CSSDeclarationRule prule = styleDeclaration.getParentRule();
		if (prule != null) {
			prule.getStyleDeclarationErrorHandler().shorthandError(propertyName, message);
		}
	}

}
