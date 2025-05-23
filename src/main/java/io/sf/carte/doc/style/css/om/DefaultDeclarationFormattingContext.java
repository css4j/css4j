/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import io.sf.carte.doc.style.css.DeclarationFormattingContext;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.SimpleWriter;

/**
 * Default implementation of a DeclarationFormattingContext.
 */
public class DefaultDeclarationFormattingContext
		implements DeclarationFormattingContext, java.io.Serializable {

	private static final long serialVersionUID = 2L;

	@Override
	public void endPropertyDeclaration(SimpleWriter wri) throws IOException {
	}

	@Override
	public void startPropertyDeclaration(SimpleWriter wri) throws IOException {
		writeFullIndent(wri);
	}

	@Override
	public void writeColon(SimpleWriter wri) throws IOException {
		wri.write(':');
		wri.write(' ');
	}

	@Override
	public void writeComma(SimpleWriter wri) throws IOException {
		wri.write(',');
		wri.write(' ');
	}

	@Override
	public void writeFullIndent(SimpleWriter wri) throws IOException {
	}

	@Override
	public void writeSemiColon(SimpleWriter wri) throws IOException {
		wri.write(';');
		wri.write(' ');
	}

	@Override
	public void writeURL(SimpleWriter wri, String href) throws IOException {
		char quote = '\'';
		wri.write("url(");
		String quoted = ParseHelper.quote(href, quote);
		wri.write(quoted);
		wri.write(')');
	}

}
