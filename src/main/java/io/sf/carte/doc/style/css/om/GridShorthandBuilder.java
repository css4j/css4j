/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.Iterator;
import java.util.Set;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.BufferSimpleWriter;

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
	int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return 1;
		}

		BaseCSSStyleDeclaration style = getParentStyle();
		if (!style.isPropertySet("grid-template-rows") || !style.isPropertySet("grid-template-columns")
				|| !style.isPropertySet("grid-template-areas")) {
			return 1;
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
	protected boolean isNotInitialValue(StyleValue cssVal, String propertyName) {
		return cssVal != null && !isEffectiveInitialKeyword(cssVal)
				&& !valueEquals(getInitialPropertyValue(propertyName), cssVal);
	}

	private void appendValueText(StringBuilder buf, StyleValue cssVal) {
		buf.append(cssVal.getMinifiedCssText(getShorthandName()));
	}

	private boolean isIdentifier(StyleValue cssVal) {
		return cssVal.getPrimitiveType() == CSSValue.Type.IDENT;
	}

	private boolean isIdentifier(StyleValue cssVal, String ident) {
		return isIdentifier(cssVal) && ident.equalsIgnoreCase(((CSSTypedValue) cssVal).getStringValue());
	}

	private class GridTemplateShorthandBuilder extends GridShorthandBuilder {

		GridTemplateShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
			super(parentStyle);
		}

		@Override
		int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
			if (!declaredSet.contains("grid-template-areas")
					&& !declaredSet.contains("grid-template-rows")
					&& !declaredSet.contains("grid-template-columns")) {
				// No grid-template properties in set, only leftover
				appendLeftoverProperties(buf, declaredSet, important);
				return 0;
			}

			// Append property name
			buf.append(getShorthandName()).append(':');

			// Check for CSS-wide keywords
			byte check = checkValuesForInherit(declaredSet);
			if (check == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				appendLeftoverProperties(buf, declaredSet, important);
				return 0;
			} else if (check == 2) {
				return 1;
			}

			// Revert
			check = checkValuesForType(CSSValue.Type.REVERT, declaredSet);
			if (check == 1) {
				// All values are revert
				buf.append("revert");
				appendPriority(buf, important);
				appendLeftoverProperties(buf, declaredSet, important);
				return 0;
			} else if (check == 2) {
				return 1;
			}

			// pending value check
			if (checkValuesForType(CSSValue.Type.INTERNAL, declaredSet) != 0) {
				return 1;
			}

			// Make sure that it is not a layered property
			String[] subp = getLonghandProperties();
			for (String property : subp) {
				StyleValue cssVal = getCSSValue(property);
				if (cssVal.getCssValueType() == CssType.LIST
						&& ((ValueList) cssVal).isCommaSeparated()) {
					return 1;
				}
			}

			GridTemplateValues values = new GridTemplateValues(declaredSet);

			/*
			 * check for grid-template: none
			 */
			if (values.isNoneValue()) {
				buf.append("none");
				appendPriority(buf, important);
				appendLeftoverProperties(buf, declaredSet, important);
				return 0;
			}

			if (!values.defaultGridTAreas && values.lacksRepeatInGridTRows
					&& values.gridAreasSyntax(buf, declaredSet)) {
				appendPriority(buf, important);
				appendLeftoverProperties(buf, declaredSet, important);
				return 0;
			}

			values.gridRowsColumnsSyntax(buf, declaredSet, important);
			appendLeftoverProperties(buf, declaredSet, important);
			return 0;
		}

		private void appendLeftoverProperties(StringBuilder buf, Set<String> declaredSet,
				boolean important) {
			if (declaredSet.contains("grid-auto-rows")) {
				appendIndividualProperty(buf, "grid-auto-rows", important);
			}
			if (declaredSet.contains("grid-auto-columns")) {
				appendIndividualProperty(buf, "grid-auto-columns", important);
			}
			if (declaredSet.contains("grid-auto-flow")) {
				appendIndividualProperty(buf, "grid-auto-flow", important);
			}
		}

		private void appendIndividualProperty(StringBuilder buf, String property,
				boolean important) {
			BufferSimpleWriter wri = new BufferSimpleWriter(buf);
			DeclarationFormattingContext context = getParentStyle().getFormattingContext();

			StyleValue value = getCSSValue(property);
			buf.append(property).append(':');
			BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context, value, property);
			appendPriority(buf, important);
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
		int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
			// Append property name
			buf.append(getShorthandName()).append(':');

			// Check for CSS-wide keywords
			byte check = checkValuesForInherit(declaredSet);
			if (check == 1) {
				// All values are inherit
				buf.append("inherit");
				appendPriority(buf, important);
				return 0;
			} else if (check == 2) {
				return 1;
			}

			// Revert
			check = checkValuesForType(CSSValue.Type.REVERT, declaredSet);
			if (check == 1) {
				// All values are revert
				buf.append("revert");
				appendPriority(buf, important);
				return 0;
			} else if (check == 2) {
				return 1;
			}

			// pending value check
			if (checkValuesForType(CSSValue.Type.INTERNAL, declaredSet) != 0) {
				return 1;
			}

			// Make sure that it is not a layered property
			String[] subp = getLonghandProperties();
			for (String property : subp) {
				StyleValue cssVal = getCSSValue(property);
				if (cssVal.getCssValueType() == CssType.LIST &&
						((ValueList) cssVal).isCommaSeparated()) {
					return 1;
				}
			}

			GridValues values = new GridValues(declaredSet);

			/*
			 * check for grid-template: none
			 */
			if (values.isNoneValue()) {
				buf.append("none");
				appendPriority(buf, important);
				return 0;
			}

			if (values.defaultGridTAreas) {
				if (!values.rowAFlow && values.defaultGridARows && values.defaultGridTColumns) {
					if (values.gridColumnAutoFlowSyntax(buf, declaredSet)) {
						appendPriority(buf, important);
						return 0;
					}
				} else if (values.rowAFlow && values.defaultGridAColumns && values.defaultGridTRows
						&& (!values.defaultGridARows || values.isAutoflowDense())) {
					if (values.gridRowAutoFlowSyntax(buf, declaredSet)) {
						appendPriority(buf, important);
						return 0;
					}
				}
			} else if (!values.defaultGridTAreas && values.lacksRepeatInGridTRows
					&& values.gridAreasSyntax(buf, declaredSet)) {
				appendPriority(buf, important);
				return 0;
			}

			values.gridRowsColumnsSyntax(buf, declaredSet, important);

			return 0;
		}

	}

	private class GridTemplateValues {

		final StyleValue cssGridTAreas;
		final StyleValue cssGridTRows;
		final StyleValue cssGridTColumns;

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

			defaultGridTAreas = isIdentifier(cssGridTAreas, "none")
					|| !declaredSet.contains("grid-template-areas")
					|| isEffectiveInitialKeyword(cssGridTAreas);
			defaultGridTRows = isIdentifier(cssGridTRows, "none")
					|| !declaredSet.contains("grid-template-rows")
					|| isEffectiveInitialKeyword(cssGridTRows);
			defaultGridTColumns = isIdentifier(cssGridTColumns, "none")
					|| !declaredSet.contains("grid-template-columns")
					|| isEffectiveInitialKeyword(cssGridTColumns);

			lacksRepeatInGridTRows = lacksRepeatFunction(cssGridTRows);
		}

		private boolean lacksRepeatFunction(StyleValue value) {
			if (value.getCssValueType() == CssType.LIST) {
				ValueList list = (ValueList) value;
				for (StyleValue item : list) {
					if (!lacksRepeatFunction(item)) {
						return false;
					}
				}
			} else if (value.getCssValueType() == CssType.TYPED) {
				CSSTypedValue primi = (CSSTypedValue) value;
				return primi.getPrimitiveType() != CSSValue.Type.FUNCTION ||
						!"repeat".equalsIgnoreCase(primi.getStringValue());
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
				StyleValue areavalue = getGridTemplateAreaItem(0);
				appendValueText(buf, areavalue);
				int idx = 1;
				while ((areavalue = getGridTemplateAreaItem(idx)) != null) {
					buf.append(' ');
					appendValueText(buf, areavalue);
					idx++;
				}
			} else if (cssGridTRows.getCssValueType() == CssType.LIST) {
				if (!((ValueList) cssGridTRows).isBracketList()) {
					int rowlistIdx = 0;
					ValueList rowsslist = (ValueList) cssGridTRows;
					Iterator<StyleValue> it = rowsslist.iterator();
					boolean bracketLast = false;
					while (it.hasNext()) {
						StyleValue value = it.next();
						ValueList bracketlist;
						if (value.getCssValueType() == CssType.LIST &&
								(bracketlist = (ValueList) value).isBracketList()) {
							int sz = bracketlist.getLength();
							if (sz < 2) {
								appendValueText(buf, value);
							} else if (sz == 2) {
								buf.append('[');
								appendValueText(buf, bracketlist.item(0));
								buf.append("] [");
								appendValueText(buf, bracketlist.item(1));
								buf.append(']');
							} else {
								return false;
							}
							StyleValue areavalue = getGridTemplateAreaItem(rowlistIdx);
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
							StyleValue areavalue = getGridTemplateAreaItem(rowlistIdx);
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
					StyleValue areavalue;
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
				StyleValue areavalue = getGridTemplateAreaItem(0);
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

			int lm1 = buf.length() - 1;
			if (buf.charAt(lm1) == ' ') {
				// trim trailing space
				buf.setLength(lm1);
			}

			if (!defaultGridTColumns) {
				buf.append('/');
				appendValueText(buf, cssGridTColumns);
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

		StyleValue getGridTemplateAreaItem(int index) {
			if (cssGridTAreas.getCssValueType() == CssType.LIST) {
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

		final StyleValue cssGridARows;
		final StyleValue cssGridAColumns;
		final StyleValue cssGridAFlow;
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
			defaultGridARows = isIdentifier(cssGridARows, "auto") || !declaredSet.contains("grid-auto-rows")
					|| isEffectiveInitialKeyword(cssGridARows);
			defaultGridAColumns = isIdentifier(cssGridAColumns, "auto") || !declaredSet.contains("grid-auto-columns")
					|| isEffectiveInitialKeyword(cssGridAColumns);
			defaultGridAFlow = isIdentifier(cssGridAFlow, "row") || !declaredSet.contains("grid-auto-flow")
					|| isEffectiveInitialKeyword(cssGridAFlow);
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
			if (cssGridAFlow.getCssValueType() == CssType.LIST) {
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
			if (cssGridAFlow.getCssValueType() == CssType.LIST) {
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
