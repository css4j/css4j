/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.ArrayList;
import java.util.Collections;

import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.LexicalValue;

class PendingSubstitutionSetter extends BaseShorthandSetter {

	private boolean priorityImportant = false;

	private LexicalUnit lexicalValue = null;

	PendingSubstitutionSetter(BaseCSSStyleDeclaration style, String shorthandName) {
		super(style, shorthandName);
	}

	@Override
	public void init(LexicalUnit shorthandValue, boolean important) {
		this.lexicalValue = shorthandValue;
		this.priorityImportant = important;
	}

	@Override
	public short assignSubproperties() {
		PendingValue pending = new PendingValue(getShorthandName(), lexicalValue);
		String[] longhands = getLonghands();
		for (String longhand : longhands) {
			if (priorityImportant || !styleDeclaration.isPropertyImportant(longhand)) {
				styleDeclaration.setProperty(longhand, pending, priorityImportant);
			}
		}
		return 0;
	}

	@Override
	public String getCssText() {
		return lexicalValue.toString();
	}

	String[] getLonghands() {
		String[] longhands = getShorthandDatabase().getLonghandProperties(getShorthandName());
		if ("border".equals(getShorthandName())) {
			ArrayList<String> lhs = new ArrayList<>(longhands.length + 5);
			Collections.addAll(lhs, longhands);
			lhs.add("border-image-source");
			lhs.add("border-image-slice");
			lhs.add("border-image-width");
			lhs.add("border-image-outset");
			lhs.add("border-image-repeat");
			longhands = lhs.toArray(new String[0]);
		} else if ("font".equals(getShorthandName())) {
			ArrayList<String> lhs = new ArrayList<>(longhands.length + 6);
			Collections.addAll(lhs, longhands);
			lhs.add("font-variant-caps");
			lhs.add("font-variant-ligatures");
			lhs.add("font-variant-position");
			lhs.add("font-variant-numeric");
			lhs.add("font-variant-alternates");
			lhs.add("font-variant-east-asian");
			longhands = lhs.toArray(new String[0]);
		}
		return longhands;
	}

	@Override
	public ShorthandValue createCSSShorthandValue(LexicalUnit value) {
		return ShorthandValue.createCSSShorthandValue(getShorthandDatabase(), getShorthandName(), value,
				priorityImportant, false);
	}

	@Override
	public String getMinifiedCssText() {
		return LexicalValue.serializeMinifiedSequence(lexicalValue);
	}

}
