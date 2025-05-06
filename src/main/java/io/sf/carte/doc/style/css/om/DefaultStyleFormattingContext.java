/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.List;

import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.SimpleWriter;

/**
 * Default implementation of a StyleFormattingContext.
 */
public class DefaultStyleFormattingContext extends DefaultDeclarationFormattingContext
		implements StyleFormattingContext {

	private static final long serialVersionUID = 1L;

	private static final String indentingUnit = "    ";

	private final StringBuilder indentString = new StringBuilder(48);

	private CSSRule parentContextRule = null;

	@Override
	public void deepenCurrentContext() {
		indentString.append(indentingUnit);
	}

	@Override
	public void endCurrentContext(CSSRule rule) {
		updateIndentString(rule.getParentRule());
	}

	@Override
	public void endPropertyDeclaration(SimpleWriter wri) throws IOException {
		wri.newLine();
	}

	@Override
	public void endInlinePropertyDeclaration(SimpleWriter wri) throws IOException {
		wri.write(' ');
	}

	@Override
	public void endRule(SimpleWriter wri, List<String> trailingComments) throws IOException {
		if (trailingComments != null) {
			int nc = trailingComments.size();
			for (int j = 0; j < nc; j++) {
				wri.write(" /*");
				wri.write(trailingComments.get(j));
				wri.write("*/");
			}
		}
		wri.newLine();
	}

	@Override
	public void endRuleList(SimpleWriter wri) throws IOException {
		writeFullIndent(wri);
	}

	@Override
	public void endStyleDeclaration(SimpleWriter wri) throws IOException {
		writeFullIndent(wri);
	}

	@Override
	public void setParentContext(CSSRule rule) {
		parentContextRule = rule;
	}

	@Override
	public void startRule(SimpleWriter wri, List<String> precedingComments) throws IOException {
		if (precedingComments != null) {
			int nc = precedingComments.size();
			for (int j = 0; j < nc; j++) {
				writeComment(wri, precedingComments.get(j));
			}
		}
		writeFullIndent(wri);
	}

	@Override
	public void startStyleDeclaration(SimpleWriter wri) throws IOException {
	}

	@Override
	public void updateContext(CSSRule rule) {
		updateIndentString(rule);
	}

	private void updateIndentString(CSSRule rule) {
		indentString.setLength(0);
		while (rule != parentContextRule) {
			deepenCurrentContext();
			rule = rule.getParentRule();
		}
	}

	@Override
	public void writeComment(SimpleWriter wri, String comment) throws IOException {
		writeFullIndent(wri);
		wri.write("/*");
		wri.write(comment);
		wri.write("*/");
		wri.newLine();
	}

	@Override
	public void writeFullIndent(SimpleWriter wri) throws IOException {
		wri.write(indentString);
	}

	@Override
	public void writeImportantPriority(SimpleWriter wri) throws IOException {
		wri.write(" ! important");
	}

	@Override
	public void writeLeftCurlyBracket(SimpleWriter wri) throws IOException {
		wri.write(' ');
		wri.write('{');
		wri.newLine();
	}

	@Override
	public void writeLevelIndent(SimpleWriter wri) throws IOException {
		wri.write(indentingUnit);
	}

	@Override
	public void writeRightCurlyBracket(SimpleWriter wri) throws IOException {
		wri.write('}');
	}

	@Override
	public void writeSemiColon(SimpleWriter wri) throws IOException {
		wri.write(';');
	}

	@Override
	public void writeURL(SimpleWriter wri, String href) throws IOException {
		char quote = '\'';
		if (parentContextRule != null) {
			AbstractCSSRule rule = (AbstractCSSRule) parentContextRule;
			AbstractCSSStyleSheetFactory sf = rule.getParentStyleSheet().getStyleSheetFactory();
			if (sf.hasFactoryFlag(CSSStyleSheetFactory.FLAG_STRING_DOUBLE_QUOTE)) {
				quote = '"';
			}
		}
		wri.write("url(");
		String quoted = ParseHelper.quote(href, quote);
		wri.write(quoted);
		wri.write(')');
	}

}
