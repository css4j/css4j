/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.property.AbstractCSSPrimitiveValue;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.IdentifierValue;
import io.sf.carte.doc.style.css.property.InheritValue;
import io.sf.carte.doc.style.css.property.PropertyDatabase;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.property.ValueItem;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Generic class that attempts to set the subproperties of shorthand properties.
 * 
 * @author Carlos Amengual
 * 
 */
class ShorthandSetter implements BaseCSSStyleDeclaration.SubpropertySetter {

	BaseCSSStyleDeclaration styleDeclaration;

	private final String shorthandName;

	private boolean priorityImportant = false;

	private PropertyDatabase pdb = PropertyDatabase.getInstance();

	final ValueFactory valueFactory;

	private HashMap<String, AbstractCSSValue> mypropValue = new HashMap<String, AbstractCSSValue>();

	private LinkedList<String> mypropertyList = new LinkedList<String>();

	private LinkedList<String> mypriorities = new LinkedList<String>();

	protected LexicalUnit currentValue = null;

	private StringBuilder valueBuffer = new StringBuilder(40);

	private StringBuilder miniValueBuffer = new StringBuilder(40);

	/**
	 * The values in the shorthand are attempted to set subproperty values in a certain order.
	 * The properties that failed to be set to the tested value are stored here.
	 */
	private ArrayList<String> unassignedProperties = new ArrayList<String>(6);

	ShorthandSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super();
		this.styleDeclaration = style;
		this.shorthandName = shorthandName;
		valueFactory = style.getValueFactory();
	}

	public String getShorthandName() {
		return shorthandName;
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

	public final PropertyDatabase getPropertyDatabase() {
		return pdb;
	}

	/**
	 * Check whether this shorthand contains any IE compatibility value
	 * (<code>SAC_COMPAT_IDENT</code> or <code>SAC_COMPAT_PRIO</code>).
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
			short type = lu.getLexicalUnitType();
			if (type == LexicalUnit2.SAC_COMPAT_IDENT || type == LexicalUnit2.SAC_COMPAT_PRIO) {
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
			if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
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
			} else if (lu.getLexicalUnitType() == LexicalUnit.SAC_FUNCTION) {
				String funcname = lu.getFunctionName();
				List<String> unass = getUnassignedProperties();
				Iterator<String> it = unass.iterator();
				while (it.hasNext()) {
					String property = it.next();
					if (property.endsWith("-color")) {
						if (!"var".equalsIgnoreCase(funcname)) {
							LexicalUnit param = lu.getParameters();
							while (param != null) {
								if (BaseCSSStyleDeclaration.testColor(param)) {
									setSubpropertyValue(property, createCSSValue(property, lu));
									it.remove();
									continue valueloop;
								}
								param = param.getNextLexicalUnit();
							}
						} else if (unass.size() == 1) {
							setSubpropertyValue(property, createCSSValue(property, lu));
							it.remove();
							break valueloop;
						}
					} else if (property.endsWith("-image")) {
						if (unass.size() == 1 && "var".equalsIgnoreCase(funcname)) {
							setSubpropertyValue(property, createCSSValue(property, lu));
							it.remove();
							break valueloop;
						}
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
		if (lunit.getLexicalUnitType() == LexicalUnit.SAC_INHERIT) {
			if (lunit.getNextLexicalUnit() != null) {
				reportMixedKeywords("inherit");
				return 2;
			}
			InheritValue inherit = InheritValue.getValue().asSubproperty();
			setSubpropertiesInherit(inherit);
			initValueString();
			appendValueItemString(inherit);
			return 1;
		} else if (lunit.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			String keyword = lunit.getStringValue();
			if (keyword.equalsIgnoreCase("initial") || keyword.equalsIgnoreCase("unset")) {
				if (lunit.getNextLexicalUnit() != null) {
					reportMixedKeywords(keyword);
					return 2;
				}
				AbstractCSSValue cssval = valueFactory.createCSSValueItem(lunit, true).getCSSValue();
				setSubpropertiesToKeyword(cssval);
				initValueString();
				appendValueItemString(cssval);
				return 1;
			}
		}
		return 0;
	}

	private void reportMixedKeywords(String keyword) {
		// report error: more than one single keyword was found
		StyleDeclarationErrorHandler errHandler = styleDeclaration.getStyleDeclarationErrorHandler();
		if (errHandler != null) {
			errHandler.shorthandError(getShorthandName(), "Found '" + keyword + "' keyword mixed with other values");
		}
	}

	protected void setSubpropertiesInherit(InheritValue inherit) {
		String[] subparray = getShorthandSubproperties();
		for (String subp : subparray) {
			if (!getPropertyDatabase().isShorthand(subp)) {
				styleDeclaration.setProperty(subp, inherit, getPriority());
			} else {
				String[] sh = getPropertyDatabase().getShorthandSubproperties(subp);
				for (String s : sh) {
					styleDeclaration.setProperty(s, inherit, getPriority());
				}
			}
		}
		return;
	}

	protected void setSubpropertiesToKeyword(AbstractCSSValue keyword) {
		String[] subparray = getShorthandSubproperties();
		for (String subp : subparray) {
			if (!getPropertyDatabase().isShorthand(subp)) {
				styleDeclaration.setProperty(subp, keyword, getPriority());
			} else {
				String[] sh = getPropertyDatabase().getShorthandSubproperties(subp);
				for (String s : sh) {
					styleDeclaration.setProperty(s, keyword, getPriority());
				}
			}
		}
		return;
	}

	protected String[] getShorthandSubproperties() {
		return getPropertyDatabase().getShorthandSubproperties(getShorthandName());
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
		Iterator<String> it = props.iterator();
		while (it.hasNext()) {
			String pname = it.next();
			if (!getPropertyDatabase().isShorthand(pname)) {
				setPropertyToDefault(pname);
			} else {
				String[] sh = getPropertyDatabase().getShorthandSubproperties(pname);
				for (int i = 0; i < sh.length; i++) {
					setPropertyToDefault(sh[i]);
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
			if (!getPropertyDatabase().isShorthand(pname)) {
				setPropertyToDefault(pname);
			} else {
				String[] sh = getPropertyDatabase().getShorthandSubproperties(pname);
				for (int i = 0; i < sh.length; i++) {
					setPropertyToDefault(sh[i]);
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
	AbstractCSSValue defaultPropertyValue(String propertyName) {
		AbstractCSSValue cssVal = styleDeclaration.defaultPropertyValue(propertyName, getPropertyDatabase());
		if (cssVal != null) {
			short type = cssVal.getCssValueType();
			if (type == CSSValue.CSS_PRIMITIVE_VALUE) {
				((AbstractCSSPrimitiveValue) cssVal).setSubproperty(true);
			} else if (type == CSSValue.CSS_VALUE_LIST) {
				((ValueList) cssVal).setSubproperty(true);
			}
		} else {
			IdentifierValue ident = new IdentifierValue("initial");
			ident.setSubproperty(true);
			cssVal = ident;
		}
		return cssVal;
	}

	void setPropertyToDefault(String pname) {
		AbstractCSSValue cssVal = defaultPropertyValue(pname);
		setProperty(pname, cssVal, getPriority());
	}

	void setDeclarationPropertyToDefault(String propertyName) {
		AbstractCSSValue cssVal = defaultPropertyValue(propertyName);
		styleDeclaration.setProperty(propertyName, cssVal, getPriority());
	}

	protected List<String> subpropertyList() {
		String[] subparray = getShorthandSubproperties();
		List<String> subp = new ArrayList<String>(subparray.length);
		subp.addAll(Arrays.asList(subparray.clone()));
		return subp;
	}

	@Override
	public boolean assignSubproperties() {
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return true;
		} else if (kwscan == 2) {
			return false;
		}
		LinkedList<LexicalUnit> unassignedValues = new LinkedList<LexicalUnit>();
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
		if (!unassignedValues.isEmpty()) {
			if (!scanUnassigned(unassignedValues)) {
				return false;
			}
		}
		// Reset subproperties not set by this shorthand
		resetSubproperties();
		flush();
		return true;
	}

	boolean isNotValidIdentifier(LexicalUnit lu) {
		return !lu.getStringValue().equalsIgnoreCase("none");
	}

	protected boolean assignSubproperty(String subproperty) {
		short lutype = currentValue.getLexicalUnitType();
		if (lutype == LexicalUnit.SAC_IDENT) {
			if (assignIdentifiers(subproperty)) {
				return true;
			}
		}
		if (subproperty.endsWith("-color")) {
			if (BaseCSSStyleDeclaration.testColor(currentValue)) {
				if (setCurrentValue(subproperty)) {
					return true;
				}
			}
		} else if (subproperty.endsWith("-width")) {
			if (ValueFactory.isSizeSACUnit(currentValue)) {
				if (setCurrentValue(subproperty)) {
					return true;
				}
			}
		} else if (subproperty.endsWith("-image") && isImage()) {
			if (setCurrentValue(subproperty)) {
				return true;
			}
		}
		return false;
	}

	boolean isImage() {
		short type = currentValue.getLexicalUnitType();
		return type == LexicalUnit.SAC_URI || (type == LexicalUnit.SAC_FUNCTION && isImageFunctionOrGradientName())
				|| type == LexicalUnit2.SAC_ELEMENT_REFERENCE;
	}

	private boolean isImageFunctionOrGradientName() {
		String lcfn = currentValue.getFunctionName().toLowerCase(Locale.US);
		return lcfn.endsWith("-gradient") || lcfn.equals("image") || lcfn.equals("image-set")
				|| lcfn.equals("cross-fade");
	}

	boolean setCurrentValue(String subproperty) {
		AbstractCSSValue cssValue = createCSSValue(subproperty, currentValue);
		if (cssValue != null) {
			setSubpropertyValue(subproperty, cssValue);
			nextCurrentValue();
			return true;
		}
		return false;
	}

	boolean assignIdentifiers(String subproperty) {
		if (testIdentifiers(subproperty)) {
			AbstractCSSValue cssValue = createCSSValue(subproperty, currentValue);
			setSubpropertyValue(subproperty, cssValue);
			nextCurrentValue();
			return true;
		}
		return false;
	}

	boolean testIdentifiers(String subproperty) {
		return getPropertyDatabase().isIdentifierValue(subproperty, currentValue.getStringValue());
	}

	protected void setSubpropertyValue(String subproperty, AbstractCSSValue cssValue) {
		setProperty(subproperty, cssValue, getPriority());
	}

	void setSubpropertyValueWListCheck(String property, AbstractCSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			ValueList list = (ValueList) value;
			if (list.getLength() == 1) {
				value = list.item(0);
			}
		}
		setSubpropertyValue(property, value);
	}

	void addSubpropertyValue(String subproperty, AbstractCSSValue cssValue, boolean commaList) {
		AbstractCSSValue cssval = getDeclaredCSSValue(subproperty);
		if (cssval == null) {
			setSubpropertyValue(subproperty, cssValue);
		} else {
			switch (cssval.getCssValueType()) {
			case CSSValue.CSS_VALUE_LIST:
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
			setProperty(subproperty, cssValue, getPriority());
		}
	}

	void setProperty(String subpropertyName, AbstractCSSValue cssValue, String priority) {
		mypropertyList.add(subpropertyName);
		mypriorities.add(priority);
		mypropValue.put(subpropertyName, cssValue);
	}

	boolean isPropertySet(String subpropertyName) {
		return mypropertyList.contains(subpropertyName);
	}

	AbstractCSSValue getDeclaredCSSValue(String propertyName) {
		return mypropValue.get(propertyName);
	}

	void flush() {
		int i = 0;
		Iterator<String> it = mypropertyList.iterator();
		while (it.hasNext()) {
			String myproperty = it.next();
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
	AbstractCSSValue createCSSValue() throws DOMException {
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

	protected AbstractCSSValue createCSSValue(String propertyName, LexicalUnit lunit) throws DOMException {
		return createCSSValue(propertyName, lunit, true);
	}

	AbstractCSSValue createCSSValue(String propertyName, LexicalUnit lunit, boolean subproperty) throws DOMException {
		AbstractCSSValue cssVal;
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
	void appendValueItemString(AbstractCSSValue cssValue) {
		if (cssValue != null) {
			String cssText = cssValue.getCssText();
			String miniCssText = cssValue.getMinifiedCssText(getShorthandName());
			StringBuilder buf = getValueItemBuffer();
			StringBuilder minibuf = getValueItemBufferMini();
			int len = buf.length();
			if (len != 0) {
				char c = buf.charAt(len -1);
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

	void reportDeclarationError(String propertyName, String message) {
		CSSDeclarationRule prule = styleDeclaration.getParentRule();
		if (prule != null) {
			prule.getStyleDeclarationErrorHandler().shorthandError(propertyName, message);
		}
	}

}
