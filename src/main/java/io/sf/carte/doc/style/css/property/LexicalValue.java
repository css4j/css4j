/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

/**
 * A value that contains a {@code var} or {@code attr()} value as part of its
 * declaration.
 */
public class LexicalValue extends AbstractLexicalValue {

	private static final long serialVersionUID = 1L;

	public LexicalValue() {
		super(Type.LEXICAL);
	}

	protected LexicalValue(LexicalValue copied) {
		super(copied);
	}

	@Override
	public LexicalValue clone() {
		return new LexicalValue(this);
	}

}
