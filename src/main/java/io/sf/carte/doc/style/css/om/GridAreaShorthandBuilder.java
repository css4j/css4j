/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Set;

import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Build a grid placement shorthand from individual properties.
 */
class GridAreaShorthandBuilder extends GridPlacementShorthandBuilder {

	GridAreaShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("grid-area", parentStyle);
		String[] subp = getSubproperties();
		if (parentStyle.isPropertySet(subp[0])) {
			addAssignedProperty(subp[0], parentStyle.isPropertyImportant(subp[0]));
		}
		if (parentStyle.isPropertySet(subp[1])) {
			addAssignedProperty(subp[1], parentStyle.isPropertyImportant(subp[1]));
		}
		if (parentStyle.isPropertySet(subp[2])) {
			addAssignedProperty(subp[2], parentStyle.isPropertyImportant(subp[2]));
		}
		if (parentStyle.isPropertySet(subp[3])) {
			addAssignedProperty(subp[3], parentStyle.isPropertyImportant(subp[3]));
		}
	}

	/* @formatter:off
	 * 
	 * Shorthand for grid-row-start, grid-column-start, grid-row-end and grid-column-end
	 * 
	 * grid-area: <grid-line> [ / <grid-line> ]{0,3}
	 * 
	 * <grid-line> = auto | <custom-ident> | [ <integer> && <custom-ident>? ] |
	 *                                       [ span && [ <integer> || <custom-ident> ] ]
	 * 
	 * When grid-column-end is omitted, if grid-column-start is a <custom-ident>,
	 * grid-column-end is set to that <custom-ident>; otherwise, it is set to auto.
	 * 
	 * When grid-row-end is omitted, if grid-row-start is a <custom-ident>,
	 * grid-row-end is set to that <custom-ident>; otherwise, it is set to auto.
	 * 
	 * When grid-column-start is omitted, if grid-row-start is a <custom-ident>,
	 * all four longhands are set to that value. Otherwise, it is set to auto.
	 * 
	 *  @formatter:on
	 */
	@Override
	int appendShorthandSet(StringBuilder buf, Set<String> declaredSet, boolean important) {
		// Check for excluded values
		if (hasPropertiesToExclude(declaredSet)) {
			return 1;
		}

		BufferSimpleWriter wri = new BufferSimpleWriter(buf);
		DeclarationFormattingContext context = getParentStyle().getFormattingContext();

		if (declaredSet.contains("grid-row-start") && declaredSet.contains("grid-row-end")) {
			if (declaredSet.contains("grid-column-start")
				&& declaredSet.contains("grid-column-end")) {
				// grid-row-start/grid-column-start/grid-row-end/grid-column-end
				return super.appendShorthandSet(buf, declaredSet, important);
			}
			if (declaredSet.contains("grid-column-start")) {
				buf.append("grid-column-start:");
				BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context,
					getCSSValue("grid-column-start"), "grid-column-start");
				appendPriority(buf, important);
			}
			if (declaredSet.contains("grid-column-end")) {
				buf.append("grid-column-end:");
				BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context,
					getCSSValue("grid-column-end"), "grid-column-end");
				appendPriority(buf, important);
			}
			// grid-row
			return new GridPlacementShorthandBuilder("grid-row", getParentStyle())
				.appendShorthandSet(buf, declaredSet, important);
		} else if (declaredSet.contains("grid-column-start")
			&& declaredSet.contains("grid-column-end")) {
			if (declaredSet.contains("grid-row-start")) {
				buf.append("grid-row-start:");
				BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context,
					getCSSValue("grid-row-start"), "grid-row-start");
				appendPriority(buf, important);
			}
			if (declaredSet.contains("grid-row-end")) {
				buf.append("grid-row-end:");
				BaseCSSStyleDeclaration.appendMinifiedCssText(wri, context,
					getCSSValue("grid-row-end"), "grid-row-end");
				appendPriority(buf, important);
			}
			// grid-column
			return new GridPlacementShorthandBuilder("grid-column", getParentStyle())
				.appendShorthandSet(buf, declaredSet, important);
		}

		return 1;
	}

}
