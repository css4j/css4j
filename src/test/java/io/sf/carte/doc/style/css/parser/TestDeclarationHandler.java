/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.ArrayList;
import java.util.LinkedList;

import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.ParserControl;

class TestDeclarationHandler extends EmptyCSSHandler implements CSSHandler {

	private ParserControl parserctl;

	ArrayList<String> propertyNames = new ArrayList<>();
	ArrayList<LexicalUnit> lexicalValues = new ArrayList<>();
	ArrayList<String> priorities = new ArrayList<>();
	ArrayList<Locator> ptyLocators = new ArrayList<>();
	LinkedList<String> comments = new LinkedList<>();

	short streamEndcount = 0;

	@Override
	public void comment(String text, boolean precededByLF) {
		comments.add(text);
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) {
		propertyNames.add(name);
		lexicalValues.add(value);
		if (important) {
			priorities.add("important");
		} else {
			priorities.add(null);
		}
		ptyLocators.add(getParserControl().createLocator());
	}

	@Override
	public void parseStart(ParserControl parserctl) {
		this.parserctl = parserctl;
		streamEndcount = 0;
	}

	@Override
	public void endOfStream() {
		streamEndcount++;
	}

	ParserControl getParserControl() {
		return parserctl;
	}

}
