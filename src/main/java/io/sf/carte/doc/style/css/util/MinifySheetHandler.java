/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.util;

import java.io.IOException;
import java.util.Locale;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.util.SimpleWriter;

class MinifySheetHandler implements CSSHandler {

	private boolean ignoreImports = false;

	private ValueFactory factory = new ValueFactory();

	private SimpleWriter writer;

	MinifySheetHandler(SimpleWriter wri) {
		super();
		this.writer = wri;
	}

	@Override
	public void parseStart(ParserControl parserctl) {
		// Starting StyleSheet processing
		ignoreImports = false;
	}

	@Override
	public void endOfStream() {
		// Ending StyleSheet processing
	}

	private void write(char c) {
		try {
			writer.write(c);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void write(CharSequence string) {
		try {
			writer.write(string);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void comment(String text, boolean precededByLF) {
		if (!text.isEmpty() && text.charAt(0) == '!') {
			write("/*");
			write(text);
			write("*/");
		}
	}

	@Override
	public void ignorableAtRule(String atRule) {
		// Ignorable @-rule
		write(atRule);
	}

	@Override
	public void namespaceDeclaration(String prefix, String uri) {
		write("@namespace ");
		if (prefix != null && !prefix.isEmpty()) {
			write(prefix);
			write(' ');
		}
		writeURL(uri);
		write(';');
	}

	private void writeURL(String uri) {
		String quoted = ParseHelper.quote(uri, '"');
		write(quoted);
	}

	@Override
	public void importStyle(String uri, String layer, BooleanCondition supportsCondition,
			MediaQueryList media, String defaultNamespaceURI) {
		// Ignore any '@import' rule that occurs inside a block or after any
		// non-ignored statement other than an @charset or an @import rule
		// (CSS 2.1 §4.1.5)
		if (ignoreImports) {
			return;
		}

		write("@import ");

		writeURL(uri);

		if (layer != null) {
			write(' ');
			write("layer");
			if (!layer.isEmpty()) {
				write('(');
				write(layer);
				write(')');
			}
		}

		if (supportsCondition != null) {
			write(' ');
			write("supports");
			write('(');
			StringBuilder buf = new StringBuilder();
			supportsCondition.appendMinifiedText(buf);
			write(buf);
			write(')');
		}

		if (media != null) {
			write(' ');
			write(media.getMinifiedMedia());
		}

		write(';');
	}

	@Override
	public void startSupports(BooleanCondition condition) {
		// Starting @supports block
		ignoreImports = true;
		write("@supports ");
		StringBuilder buf = new StringBuilder();
		condition.appendMinifiedText(buf);
		write(buf);
		write('{');
	}

	@Override
	public void endSupports(BooleanCondition condition) {
		endDeclarationsRule();
	}

	@Override
	public void startMedia(MediaQueryList media) {
		// Starting @media block
		ignoreImports = true;
		write("@media");

		if (media != null) {
			write(' ');
			write(media.getMinifiedMedia());
		}
		write('{');
	}

	@Override
	public void endMedia(MediaQueryList media) {
		endDeclarationsRule();
	}

	@Override
	public void startPage(PageSelectorList pageSelectorList) {
		ignoreImports = true;
		write("@page");
		if (pageSelectorList != null) {
			write(' ');
			write(pageSelectorList.toString());
		}
		write('{');
	}

	@Override
	public void endPage(PageSelectorList pageSelectorList) {
		endDeclarationsRule();
	}

	@Override
	public void startMargin(String name) {
		write('@');
		write(name);
		write('{');
	}

	@Override
	public void endMargin() {
		endDeclarationsRule();
	}

	@Override
	public void startFontFace() {
		ignoreImports = true;
		write("@font-face{");
	}

	@Override
	public void endFontFace() {
		endDeclarationsRule();
	}

	@Override
	public void startCounterStyle(String name) {
		ignoreImports = true;
		write("@counter-style ");
		write(name);
		write('{');
	}

	@Override
	public void endCounterStyle() {
		endDeclarationsRule();
	}

	@Override
	public void startKeyframes(String name) {
		ignoreImports = true;
		write("@keyframes ");
		write(name);
		write('{');
	}

	@Override
	public void endKeyframes() {
		write('}');
	}

	@Override
	public void startKeyframe(LexicalUnit keyframeSelector) {
		StringBuilder buf = new StringBuilder();
		write(miniKeyframeSelector(buf, keyframeSelector));
		write('{');
	}

	private static String miniKeyframeSelector(StringBuilder buffer, LexicalUnit selunit) {
		appendMiniSelector(buffer, selunit);
		LexicalUnit lu = selunit.getNextLexicalUnit();
		while (lu != null) {
			LexicalUnit nextlu = lu.getNextLexicalUnit();
			buffer.append(',');
			appendMiniSelector(buffer, nextlu);
			lu = nextlu.getNextLexicalUnit();
		}
		return buffer.toString();
	}

	private static void appendMiniSelector(StringBuilder buffer, LexicalUnit selunit) {
		LexicalType type = selunit.getLexicalUnitType();
		if (type == LexicalType.IDENT || type == LexicalType.STRING) {
			buffer.append(selunit.getStringValue());
		} else if (type == LexicalType.PERCENTAGE) {
			float floatValue = selunit.getFloatValue();
			if (floatValue == 0f) {
				buffer.append('0');
				return;
			}
			if (floatValue % 1 != 0) {
				buffer.append(String.format(Locale.ROOT, "%s", floatValue));
			} else {
				buffer.append(String.format(Locale.ROOT, "%.0f", floatValue));
			}
			buffer.append('%');
		} else if (type == LexicalType.INTEGER && selunit.getIntegerValue() == 0) {
			buffer.append('0');
		} else {
			buffer.append(selunit.getCssText());
		}
	}

	@Override
	public void endKeyframe() {
		endDeclarationsRule();
	}

	@Override
	public void startFontFeatures(String[] familyName) {
		ignoreImports = true;
		write("@font-feature-values ");
		write(familyName[0]);
		for (int i = 1; i < familyName.length; i++) {
			write(',');
			write(familyName[i]);
		}
		write('{');
	}

	@Override
	public void endFontFeatures() {
		write('}');
	}

	@Override
	public void startFeatureMap(String mapName) {
		write('@');
		write(mapName);
		write('{');
	}

	@Override
	public void endFeatureMap() {
		endDeclarationsRule();
	}

	@Override
	public void startProperty(String name) {
		ignoreImports = true;
		write("@property ");
		write(name);
		write('{');
	}

	@Override
	public void endProperty(boolean discard) {
		endDeclarationsRule();
	}

	@Override
	public void startSelector(SelectorList selectors) {
		ignoreImports = true;
		write(selectors.toString());
		write('{');
	}

	@Override
	public void endSelector(SelectorList selectors) {
		endDeclarationsRule();
	}

	private void endDeclarationsRule() {
		try {
			if (writer.getLastChar() == ';') {
				writer.unwrite();
			}
		} catch (UnsupportedOperationException e) {
		}
		write('}');
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) {
		write(name);
		write(':');

		serializeValue(value);

		if (important) {
			write("!important");
		}

		write(';');
	}

	private void serializeValue(LexicalUnit value) {
		String mini = LexicalValue.serializeMinifiedSequence(value);
		try {
			StyleValue omvalue = factory.createCSSValue(value);
			// If it is LEXICAL it will repeat the serializeMinifiedSequence()
			if (omvalue.getPrimitiveType() != Type.LEXICAL) {
				String ommini = omvalue.getMinifiedCssText();
				if (ommini.length() < mini.length()) {
					write(ommini);
					return;
				}
			}
		} catch (Exception e) {
		}
		write(mini);
	}

	@Override
	public void lexicalProperty(String name, LexicalUnit lunit, boolean important) {
		property(name, lunit, important);
	}

}
