/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.css.CSSRule;

import io.sf.carte.util.SimpleWriter;

/**
 * A formatting context for inline styles.
 */
public class InlineStyleFormattingContext extends DefaultStyleFormattingContext {

	@Override
	public void endCurrentContext(CSSRule rule) {
	}

	@Override
	public void endPropertyDeclaration(SimpleWriter wri) throws IOException {
	}

	@Override
	public void endInlinePropertyDeclaration(SimpleWriter wri) throws IOException {
	}

	@Override
	public void endRule(SimpleWriter wri, List<String> trailingComments) throws IOException {
	}

	@Override
	public void updateContext(CSSRule rule) {
	}

	@Override
	public void writeComment(SimpleWriter wri, String comment) throws IOException {
		wri.write("/*");
		wri.write(comment);
		wri.write("*/");
	}

	@Override
	public void writeFullIndent(SimpleWriter wri) throws IOException {
	}

	@Override
	public void writeLeftCurlyBracket(SimpleWriter wri) throws IOException {
		wri.write(' ');
		wri.write('{');
	}

	@Override
	public void writeLevelIndent(SimpleWriter wri) throws IOException {
	}

	@Override
	public void writeSemiColon(SimpleWriter wri) throws IOException {
		wri.write(';');
		wri.write(' ');
	}

}
