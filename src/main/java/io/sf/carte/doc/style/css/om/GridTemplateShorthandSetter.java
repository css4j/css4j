/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

class GridTemplateShorthandSetter extends BaseGridShorthandSetter {

	GridTemplateShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "grid-template");
	}

	@Override
	public short assignSubproperties() {
		// Keyword scan
		byte kwscan = scanForCssWideKeywords(currentValue);
		if (kwscan == 1) {
			return 0;
		} else if (kwscan == 2) {
			return 2;
		}

		String[] subparray = getShorthandDatabase().getShorthandSubproperties(getShorthandName());
		setPropertyToDefault(subparray[0]);
		setPropertyToDefault(subparray[1]);
		setPropertyToDefault(subparray[2]);

		// Determine if it is the syntax with grid-template-areas
		if (isTemplateAreasSyntax()) {
			if (templateSyntax(true)) {
				flush();
				return 0;
			} else {
				return 2;
			}
		}

		// Other syntaxes. We first test for 'none'
		if (isNoneDeclaration()) {
			appendValueItemString("none");
			flush();
			return 0;
		}

		// Syntax must be <‘grid-template-rows’> / <‘grid-template-columns’>
		if (templateSyntax(false)) {
			flush();
			return 0;
		}

		return 2;
	}

}
