/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.LinkedList;

import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;

class TestDeclarationHandler extends EmptyCSSHandler implements CSSHandler {

	LinkedList<String> propertyNames = new LinkedList<String>();
	LinkedList<LexicalUnit> lexicalValues = new LinkedList<LexicalUnit>();
	LinkedList<String> priorities = new LinkedList<String>();
	LinkedList<String> comments = new LinkedList<String>();

	boolean streamEnded = false;

	@Override
	public void comment(String text) {
		comments.add(text);
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important, int index) {
		propertyNames.add(name);
		lexicalValues.add(value);
		if (important) {
			priorities.add("important");
		} else {
			priorities.add(null);
		}
	}

	@Override
	public void endOfStream() {
		streamEnded = true;
	}

}
