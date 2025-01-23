/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import io.sf.carte.doc.style.css.CSSPropertyDefinition;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class PropertyDefinition implements CSSPropertyDefinition {

	private final String name;

	private final boolean inherits;

	private final CSSValueSyntax syntax;

	private final LexicalUnit initialValue;

	PropertyDefinition(String name, CSSValueSyntax syntax, boolean inherits, LexicalUnit initialValue) {
		super();
		this.name = name;
		this.syntax = syntax;
		this.inherits = inherits;
		this.initialValue = initialValue;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean inherits() {
		return inherits;
	}

	@Override
	public LexicalUnit getInitialValue() {
		return initialValue;
	}

	@Override
	public CSSValueSyntax getSyntax() {
		return syntax;
	}

}
