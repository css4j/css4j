/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.CSSValueSyntax;

class SyntaxComponent implements CSSValueSyntax {

	private Category cat;

	private String name;

	private Multiplier multiplier = Multiplier.NONE;

	private SyntaxComponent next = null;

	SyntaxComponent(String name, Category cat) {
		super();
		this.name = name;
		this.cat = cat;
	}

	SyntaxComponent() {
		super();
	}

	@Override
	public Category getCategory() {
		return cat;
	}

	void setCategory(Category cat) {
		this.cat = cat;
	}

	@Override
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	@Override
	public Multiplier getMultiplier() {
		return multiplier;
	}

	void setMultiplier(Multiplier multiplier) {
		this.multiplier = multiplier;
	}

	@Override
	public SyntaxComponent getNext() {
		return next;
	}

	void setNext(SyntaxComponent next) {
		this.next = next;
	}

	@Override
	public CSSValueSyntax shallowClone() {
		if (next == null) {
			return this;
		}
		SyntaxComponent comp = new SyntaxComponent(name, cat);
		comp.setMultiplier(getMultiplier());
		return comp;
	}

	@Override
	public String toString() {
		if (cat == Category.IDENT && next == null && multiplier == Multiplier.NONE) {
			return ParseHelper.escape(name, true, true);
		}
		//
		StringBuilder buf = new StringBuilder(32);
		appendToBuffer(this, buf);
		return buf.toString();
	}

	private static void appendToBuffer(SyntaxComponent syntax, StringBuilder buf) {
		if (syntax.cat == Category.universal) {
			buf.append('*');
		} else if (syntax.cat == Category.IDENT) {
			buf.append(ParseHelper.escape(syntax.name, true, true));
		} else {
			buf.append('<').append(syntax.name).append('>');
		}
		switch (syntax.getMultiplier()) {
		case NUMBER:
			buf.append('#');
			break;
		case PLUS:
			buf.append('+');
		default:
			break;
		}
		//
		if (syntax.next != null) {
			buf.append(" | ");
			appendToBuffer(syntax.next, buf);
		}
	}

}
