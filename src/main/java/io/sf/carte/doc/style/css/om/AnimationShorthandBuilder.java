/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueList;
import io.sf.carte.util.BufferSimpleWriter;

/**
 * Build an 'animation' shorthand from individual properties.
 */
class AnimationShorthandBuilder extends ListOrderedShorthandBuilder {

	private final static List<String> RESET_PROPERTIES;

	static {
		String[] resetProperties = { "animation-timeline", "animation-range-start",
				"animation-range-end" };
		RESET_PROPERTIES = new ArrayList<>(3);
		Collections.addAll(RESET_PROPERTIES, resetProperties);
	}

	AnimationShorthandBuilder(BaseCSSStyleDeclaration parentStyle) {
		super("animation", parentStyle, "none", "animation-name");
	}

	@Override
	boolean isResetProperty(String property) {
		return RESET_PROPERTIES.contains(property);
	}

	@Override
	boolean appendValueText(BufferSimpleWriter wri, DeclarationFormattingContext context,
			String property, boolean appended) {
		if (!RESET_PROPERTIES.contains(property)) {
			return super.appendValueText(wri, context, property, appended);
		}
		return appended;
	}

	@Override
	void endShorthandSerialization(BufferSimpleWriter wri, DeclarationFormattingContext context,
			boolean importantShorthand) {
		for (String property : RESET_PROPERTIES) {
			StyleValue cssVal = getCSSValue(property);
			boolean important;
			if (isNotInitialValue(cssVal, property)
					&& importantShorthand == (important = isPropertyInImportantSet(property))) {
				// If the shorthand is a non-initial keyword
				StyleValue shorthandSample = getCSSValue("animation-name");
				if (shorthandSample.getCssValueType() == CSSValue.CssType.KEYWORD
						&& shorthandSample.getPrimitiveType() == cssVal.getPrimitiveType()) {
					continue;
				}
				wri.write(property);
				try {
					wri.write(':');
					context.writeMinifiedValue(wri, property, cssVal);
					if (important) {
						wri.write("!important;");
					} else {
						wri.write(';');
					}
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	boolean valueClash(int index, String property) {
		String chkProperty = property;
		if ("animation-timing-function".equals(property)) {
			chkProperty = "transition-timing-function";
		}
		StyleValue freePropertyValue = getCSSListItemValue(freeProperty, index);
		// Check for conflicting identifiers
		CssType freeType = freePropertyValue.getCssValueType();
		boolean retval = false;
		if (freeType == CssType.TYPED) {
			retval = isConflictingIdentifier(chkProperty, (CSSTypedValue) freePropertyValue);
		} else if (freeType == CssType.LIST) {
			retval = listHasConflictingIdentifiers(chkProperty, (ValueList) freePropertyValue);
		}
		// duration-delay
		if (!retval && property.equals("animation-duration")) {
			StyleValue delay = getCSSListItemValue("animation-delay", index);
			if (isNotInitialValue(delay, "animation-delay")) {
				retval = true;
			}
		}
		return retval;
	}

	@Override
	boolean validValueClash(String property) {
		String chkProperty = property;
		if ("animation-timing-function".equals(property)) {
			chkProperty = "transition-timing-function";
		}
		// Make sure that 'none' is in animation-fill-mode list in 'identifier.properties'

		boolean retval = super.validValueClash(chkProperty);
		if (!retval && property.equals("animation-duration")) {
			StyleValue delay = getCSSValue("animation-delay");
			if (isNotInitialValue(delay, "animation-delay")) {
				retval = true;
			}
		}
		return retval;
	}

}
