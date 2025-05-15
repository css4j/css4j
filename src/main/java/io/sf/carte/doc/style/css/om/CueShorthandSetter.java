/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

class CueShorthandSetter extends SequenceShorthandSetter {

	public CueShorthandSetter(BaseCSSStyleDeclaration style) {
		super(style, "cue");
	}

	@Override
	public short assignSubproperties() {
		if (isAttrTainted()) {
			return 2;
		}
		return super.assignSubproperties();
	}

}
