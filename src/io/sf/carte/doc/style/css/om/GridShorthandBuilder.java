/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.CSSPrimitiveValue2;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Build a grid shorthand from individual properties.
 */
class GridShorthandBuilder extends ShorthandBuilder {

	GridShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("grid", parentStyle);
	}

	@Override
	protected int getMinimumSetSize() {
		return 3;
	}

	@Override
	boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return false;
		}
		BaseCSSStyleDeclaration style = getParentStyle();
		if (!style.isPropertySet("grid-template-rows") || !style.isPropertySet("grid-template-columns")
				 || !style.isPropertySet("grid-template-areas")) {
			return false;
		}
		// Now we have, at least 'grid-template'. Check for full 'grid'.
		if (style.isPropertySet("grid-auto-rows") && style.isPropertySet("grid-auto-columns")
				 && style.isPropertySet("grid-auto-flow")) {
			return new FullGridShorthandBuilder(style).appendShorthandSet(buf, declaredSet, important);
		}
		return new GridTemplateShorthandBuilder(style).appendShorthandSet(buf, declaredSet, important);
	}

	/*
	 * This override is optimized for the case where non system-default values cannot be found
	 */
	@Override
	protected boolean isNotInitialValue(AbstractCSSValue cssVal, String propertyName) {
		return cssVal != null && !isInitialIdentifier(cssVal)
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	private void appendValueText(StringBuilder buf, AbstractCSSValue cssVal) {
		buf.append(cssVal.getMinifiedCssText(getShorthandName()));
	}

	private boolean isIdentifier(AbstractCSSValue cssVal) {
		return cssVal.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE &&
				((CSSPrimitiveValue) cssVal).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
	}

	private boolean isIdentifier(AbstractCSSValue cssVal, String ident) {
		return isIdentifier(cssVal) && ident.equalsIgnoreCase(((CSSPrimitiveValue) cssVal).getStringValue());
	}

	private class GridTemplateShorthandBuilder extends GridShorthandBuilder {

		GridTemplateShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
			super(parentStyle);
		}

		@Override
		boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
			// Append property name
			buf.append(getShorthandName()).append(':');
			// Check for CSS-wide keywords
			byte check = checkValuesForInherit(declaredSet);
			if (check == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				return true;
			} else if (check == 2) {
				return false;
			}
			check = checkValuesForKeyword("unset", declaredSet);
			if (check == 1) {
				// All values are unset
				buf.append("unset");
				appendPriority(buf, important);
				return true;
			} else if (check == 2) {
				return false;
			}
			// Make sure that it is not a layered property
			String[] subp = getLonghandProperties();
			for (String property : subp) {
				AbstractCSSValue cssVal = getCSSValue(property);
				if ((cssVal.getCssValueType() == CSSValue.CSS_VALUE_LIST &&
						((ValueList) cssVal).isCommaSeparated())) {
					return false;
				}
			}
			GridTemplateValues values = new GridTemplateValues(declaredSet);
			/*
			 * check for grid-template: none
			 */
			if (values.isNoneValue()) {
				buf.append("none");
				appendPriority(buf, important);
				return true;
			}
			//
			if (!values.defaultGridTAreas && values.lacksRepeatInGridTRows) {
				if (values.gridAreasSyntax(buf, declaredSet)) {
					appendPriority(buf, important);
					return true;
				}
			}
			//
			values.gridRowsColumnsSyntax(buf, declaredSet, important);
			return true;
		}

		@Override
		String getShorthandName() {
			return "grid-template";
		}

		@Override
		protected int getTotalSetSize() {
			return 3;
		}

	}

	private class FullGridShorthandBuilder extends GridShorthandBuilder {

		FullGridShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
			super(parentStyle);
		}

		@Override
		boolean appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
			// Append property name
			buf.append(getShorthandName()).append(':');
			// Check for CSS-wide keywords
			byte check = checkValuesForInherit(declaredSet);
			if (check == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				return true;
			} else if (check == 2) {
				return false;
			}
			check = checkValuesForKeyword("unset", declaredSet);
			if (check == 1) {
				// All values are unset
				buf.append("unset");
				appendPriority(buf, important);
				return true;
			} else if (check == 2) {
				return false;
			}
			// Make sure that it is not a layered property
			String[] subp = getLonghandProperties();
			for (String property : subp) {
				AbstractCSSValue cssVal = getCSSValue(property);
				if ((cssVal.getCssValueType() == CSSValue.CSS_VALUE_LIST &&
						((ValueList) cssVal).isCommaSeparated())) {
					return false;
				}
			}
			GridValues values = new GridValues(declaredSet);
			/*
			 * check for grid-template: none
			 */
			if (values.isNoneValue()) {
				buf.append("none");
				appendPriority(buf, important);
				return true;
			}
			//
			if (values.defaultGridTAreas) {
				if (!values.rowAFlow && values.defaultGridARows && values.defaultGridTColumns) {
					if (values.gridColumnAutoFlowSyntax(buf, declaredSet)) {
						appendPriority(buf, important);
						return true;
					}
				} else if (values.rowAFlow && values.defaultGridAColumns && values.defaultGridTRows
						&& (!values.defaultGridARows || values.isAutoflowDense())) {
					if (values.gridRowAutoFlowSyntax(buf, declaredSet)) {
						appendPriority(buf, important);
						return true;
					}
				}
			} else if (!values.defaultGridTAreas && values.lacksRepeatInGridTRows) {
				if (values.gridAreasSyntax(buf, declaredSet)) {
					appendPriority(buf, important);
					return true;
				}
			}
			//
			values.gridRowsColumnsSyntax(buf, declaredSet, important);
			return true;
		}

	}

	private class GridTemplateValues {

		final AbstractCSSValue cssGridTAreas;
		final AbstractCSSValue cssGridTRows;
		final AbstractCSSValue cssGridTColumns;
		final boolean defaultGridTAreas;
		final boolean defaultGridTRows;
		final boolean defaultGridTColumns;
		boolean lacksRepeatInGridTRows;

		GridTemplateValues(Set<String> declaredSet) {
			super();
			/*
			 * Obtain values and check for defaults
			 */
			cssGridTAreas = getCSSValue("grid-template-areas");
			cssGridTRows = getCSSValue("grid-template-rows");
			cssGridTColumns = getCSSValue("grid-template-columns");
			defaultGridTAreas = isIdentifier(cssGridTAreas, "none") || !declaredSet.contains("grid-template-areas");
			defaultGridTRows = isIdentifier(cssGridTRows, "none") || !declaredSet.contains("grid-template-rows");
			defaultGridTColumns = isIdentifier(cssGridTColumns, "none") || !declaredSet.contains("grid-template-columns");
			lacksRepeatInGridTRows = lacksRepeatFunction(cssGridTRows);
		}

		private boolean lacksRepeatFunction(AbstractCSSValue value) {
			if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				ValueList list = (ValueList) value;
				Iterator<AbstractCSSValue> it = list.iterator();
				while (it.hasNext()) {
					if (!lacksRepeatFunction(it.next())) {
						return false;
					}
				}
			} else if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				CSSPrimitiveValue primi = (CSSPrimitiveValue) value;
				return primi.getPrimitiveType() != CSSPrimitiveValue2.CSS_FUNCTION ||
						"repeat".equalsIgnoreCase(primi.getStringValue());
			}
			return true;
		}

		boolean gridAreasSyntax(StringBuilder buf, Set<String> declaredSet) {
			/* @formatter:off
			 * 
			 * grid-template full syntax:
			 * 
			 * [ <line-names>? <string> <track-size>? <line-names>? ]+ [ / <explicit-track-list> ]?
			 * 
			 * <explicit-track-list> = [ <line-names>? <track-size> ]+ <line-names>?
			 * 
			 * -Set grid-template-areas to the strings listed.
			 * -Set grid-template-rows to the <track-size>s following each string (filling in auto
			 *  for any missing sizes), and splicing in the named lines defined before/after each size.
			 * -Set grid-template-columns to the track listing specified after the slash (or none,
			 *  if not specified).
			 * 
			 *  @formatter:on
			 */
			if (isIdentifier(cssGridTRows, "auto") || !declaredSet.contains("grid-template-rows")) {
				AbstractCSSValue areavalue = getGridTemplateAreaItem(0);
				appendValueText(buf, areavalue);
				int idx = 1;
				while ((areavalue = getGridTemplateAreaItem(idx)) != null) {
					buf.append(' ');
					appendValueText(buf, areavalue);
					idx++;
				}
			} else if (cssGridTRows.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				if (!((ValueList) cssGridTRows).isBracketList()) {
					int rowlistIdx = 0;
					ValueList rowsslist = (ValueList) cssGridTRows;
					Iterator<AbstractCSSValue> it = rowsslist.iterator();
					boolean bracketLast = false;
					while (it.hasNext()) {
						AbstractCSSValue value = it.next();
						ValueList bracketlist;
						if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST &&
								(bracketlist = (ValueList) value).isBracketList()) {
							int sz = bracketlist.getLength();
							if (sz < 2) {
								appendValueText(buf, value);
							} else if (sz == 2){
								buf.append('[');
								appendValueText(buf, bracketlist.item(0));
								buf.append("] [");
								appendValueText(buf, bracketlist.item(1));
								buf.append(']');
							} else {
								return false;
							}
							AbstractCSSValue areavalue = getGridTemplateAreaItem(rowlistIdx);
							if (areavalue != null) {
								buf.append(' ');
								appendValueText(buf, areavalue);
								buf.append(' ');
								rowlistIdx++;
								bracketLast = true;
							} else if (it.hasNext()) {
								return false;
							}
						} else {
							AbstractCSSValue areavalue = getGridTemplateAreaItem(rowlistIdx);
							if (areavalue != null && !bracketLast) {
								appendValueText(buf, areavalue);
								buf.append(' ');
								rowlistIdx++;
							}
							// track-size
							if (!isIdentifier(value) || !"auto".equalsIgnoreCase(value.getCssText())) {
								appendValueText(buf, value);
								buf.append(' ');
							}
							bracketLast = false;
						}
					}
				} else {
					// Bracket list [line-name] 
					appendValueText(buf, cssGridTRows);
					AbstractCSSValue areavalue;
					int idx = 0;
					while ((areavalue = getGridTemplateAreaItem(idx)) != null) {
						buf.append(' ');
						appendValueText(buf, areavalue);
						buf.append(' ');
						idx++;
					}
				}
			} else {
				// track-size
				AbstractCSSValue areavalue = getGridTemplateAreaItem(0);
				appendValueText(buf, areavalue);
				buf.append(' ');
				appendValueText(buf, cssGridTRows);
				int idx = 1;
				while ((areavalue = getGridTemplateAreaItem(idx)) != null) {
					buf.append(' ');
					appendValueText(buf, areavalue);
					buf.append(' ');
					idx++;
				}
			}
			if (!defaultGridTColumns) {
				buf.append('/');
				appendValueText(buf, cssGridTColumns);
			} else {
				int lm1 = buf.length() - 1;
				if (buf.charAt(lm1) == ' ') {
					buf.setLength(lm1);
				}
			}
			return true;
		}

		void gridRowsColumnsSyntax(StringBuilder buf, Set<String> declaredSet, boolean important) {
			appendValueText(buf, cssGridTRows);
			if (!defaultGridTColumns || isIdentifier(cssGridTRows, "auto")) {
				buf.append('/');
				appendValueText(buf, cssGridTColumns);
			}
			appendPriority(buf, important);
			if (!defaultGridTAreas) {
				buf.append("grid-template-areas:");
				appendValueText(buf, cssGridTAreas);
				appendPriority(buf, important);
			}
		}

		boolean isNoneValue() {
			return defaultGridTAreas && defaultGridTRows && defaultGridTColumns;
		}

		AbstractCSSValue getGridTemplateAreaItem(int index) {
			if (cssGridTAreas.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				return ((ValueList) cssGridTAreas).item(index);
			}
			if (!defaultGridTAreas && index == 0) {
				return cssGridTAreas;
			} else {
				return null;
			}
		}
	}

	private class GridValues extends GridTemplateValues {

		final AbstractCSSValue cssGridARows;
		final AbstractCSSValue cssGridAColumns;
		final AbstractCSSValue cssGridAFlow;
		final boolean defaultGridARows;
		final boolean defaultGridAColumns;
		final boolean defaultGridAFlow;
		final boolean rowAFlow;

		GridValues(Set<String> declaredSet) {
			super(declaredSet);
			/*
			 * Obtain values and check for defaults
			 */
			cssGridARows = getCSSValue("grid-auto-rows");
			cssGridAColumns = getCSSValue("grid-auto-columns");
			cssGridAFlow = getCSSValue("grid-auto-flow");
			defaultGridARows = isIdentifier(cssGridARows, "auto") || !declaredSet.contains("grid-auto-rows");
			defaultGridAColumns = isIdentifier(cssGridAColumns, "auto") || !declaredSet.contains("grid-auto-columns");
			defaultGridAFlow = isIdentifier(cssGridAFlow, "row") || !declaredSet.contains("grid-auto-flow");
			rowAFlow = isRowAutoflow();
		}

		@Override
		void gridRowsColumnsSyntax(StringBuilder buf, Set<String> declaredSet, boolean important) {
			super.gridRowsColumnsSyntax(buf, declaredSet, important);
			if (!defaultGridARows) {
				buf.append("grid-auto-rows:");
				appendValueText(buf, cssGridARows);
				appendPriority(buf, important);
			}
			if (!defaultGridAColumns) {
				buf.append("grid-auto-columns:");
				appendValueText(buf, cssGridAColumns);
				appendPriority(buf, important);
			}
			if (!defaultGridAFlow) {
				buf.append("grid-auto-flow:");
				appendValueText(buf, cssGridAFlow);
				appendPriority(buf, important);
			}
		}

		/*
		 * grid: [ auto-flow && dense? ] <'grid-auto-rows'>? / <'grid-template-columns'>
		 */
		private boolean gridRowAutoFlowSyntax(StringBuilder buf, Set<String> declaredSet) {
			if (!isAutoflowDense()) {
				buf.append("auto-flow ");
			} else {
				buf.append("auto-flow dense ");
			}
			appendValueText(buf, cssGridARows);
			if (!defaultGridTColumns) {
				buf.append('/');
				appendValueText(buf, cssGridTColumns);
			}
			return true;
		}

		/*
		 * grid: <'grid-template-rows'> / [ auto-flow && dense? ] <'grid-auto-columns'>?
		 */
		private boolean gridColumnAutoFlowSyntax(StringBuilder buf, Set<String> declaredSet) {
			appendValueText(buf, cssGridTRows);
			if (!defaultGridAColumns) {
				buf.append('/');
				if (!isAutoflowDense()) {
					buf.append("auto-flow ");
				} else {
					buf.append("auto-flow dense ");
				}
				appendValueText(buf, cssGridAColumns);
			}
			return true;
		}

		@Override
		boolean isNoneValue() {
			return defaultGridTAreas && defaultGridTRows && defaultGridTColumns && defaultGridARows
					 && defaultGridAColumns && defaultGridAFlow;
		}

		private boolean isRowAutoflow() {
			if (cssGridAFlow.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				ValueList list = (ValueList) cssGridAFlow;
				if (list.getLength() == 2) {
					return "row".equalsIgnoreCase(list.item(0).getCssText()) ||
							"row".equalsIgnoreCase(list.item(1).getCssText());
				}
			} else {
				return "row".equalsIgnoreCase(cssGridAFlow.getCssText());
			}
			return false;
		}

		private boolean isAutoflowDense() {
			if (cssGridAFlow.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
				ValueList list = (ValueList) cssGridAFlow;
				if (list.getLength() == 2) {
					return "dense".equalsIgnoreCase(list.item(1).getCssText()) ||
							"dense".equalsIgnoreCase(list.item(0).getCssText());
				}
			}
			return false;
		}

	}
}
