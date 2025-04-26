/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.uparser.TokenProducer;

class URLTokenHandler extends CallbackTokenHandler {

	private String url = null;

	private boolean allowModifiers;

	private LexicalUnitImpl urlUnit = null;

	private LexicalUnitImpl modifier = null;

	private boolean legacySyntax = false;

	URLTokenHandler(CSSContentHandler caller) {
		super(caller);
		parendepth = 1;
		this.allowModifiers = false;
	}

	URLTokenHandler(LexicalProvider caller) {
		super(caller);
		parendepth = 1;
		// The current unit must be a url()
		urlUnit = caller.getCurrentLexicalUnit();
		this.allowModifiers = urlUnit != null;
	}

	@Override
	protected void initializeBuffer() {
		buffer = new StringBuilder(256);
	}

	@Override
	public void word(int index, CharSequence word) {
		if (url == null || allowModifiers) {
			super.word(index, word);
		} else {
			unexpectedTokenError(index, word);
		}
	}

	@Override
	public void leftCurlyBracket(int index) {
		if (url == null || allowModifiers) {
			buffer.append('{');
		} else {
			unexpectedLeftCurlyBracketError(index);
		}
	}

	@Override
	public void rightCurlyBracket(int index) {
		appendIfValid(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
	}

	private void appendIfValid(int index, int codePoint) {
		if (url == null || allowModifiers) {
			bufferAppend(codePoint);
		} else {
			unexpectedCharError(index, codePoint);
		}
	}

	@Override
	public void leftSquareBracket(int index) {
		appendIfValid(index, TokenProducer.CHAR_LEFT_SQ_BRACKET);
	}

	@Override
	public void rightSquareBracket(int index) {
		appendIfValid(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
	}

	@Override
	void processBuffer(int index, int triggerCp) {
		if (buffer.length() > 0) {
			if (url == null) {
				legacySyntax = true;
				allowModifiers = false;
				url = rawBuffer();
				if (urlUnit != null) {
					urlUnit.value = url;
				}
			} else if (allowModifiers) {
				String mod = unescapeBuffer(index);
				LexicalUnitImpl lu = new LexicalUnitImpl(LexicalType.IDENT);
				lu.value = mod;
				addModifier(lu);
			} else {
				unexpectedTokenError(index, buffer);
			}
		}
	}

	private void addModifier(LexicalUnitImpl lu) {
		if (modifier == null) {
			modifier = lu;
			urlUnit.parameters = lu;
		} else {
			modifier.nextLexicalUnit = lu;
			lu.previousLexicalUnit = modifier;
			modifier = lu;
			lu.ownerLexicalUnit = urlUnit;
		}
	}

	@Override
	public void separator(int index, int codepoint) {
		if (isEscapedIdent() && CSSParser.bufferEndsWithEscapedCharOrWS(buffer)) {
			buffer.append(' ');
		} else if (url == null) {
			processBuffer(index, codepoint);
		} else if (legacySyntax) {
			unexpectedCharError(index, codepoint);
		}
	}

	@Override
	public void quoted(int index, CharSequence quoted, int quote) {
		if (url == null) {
			String escaped = quoted.toString();
			url = CSSParser.safeUnescapeIdentifier(index, escaped);
			if (urlUnit != null) {
				urlUnit.value = url;
				StringBuilder buf = new StringBuilder(escaped.length() + 2);
				char c = (char) quote;
				buf.append(c);
				buf.append(escaped);
				buf.append(c);
				urlUnit.identCssText = buf.toString();
			}
		} else {
			unexpectedCharError(index, quote);
		}
	}

	@Override
	public void character(int index, int codePoint) {
		appendIfValid(index, codePoint);
	}

	@Override
	public void escaped(int index, int codePoint) {
		if (url != null && !allowModifiers) {
			unexpectedCharError(index, codePoint);
		} else {
			if (isEscapedCodepoint(codePoint)) {
				setEscapedTokenStart(index);
				buffer.append('\\');
			}
			bufferAppend(codePoint);
		}
	}

	@Override
	public void leftParenthesis(int index) {
		parendepth++;
		if (url != null && buffer.length() > 0 && allowModifiers) {
			String mod = unescapeBuffer(index);
			LexicalUnitImpl lu = new GenericFunctionUnitImpl();
			lu.value = mod;
			addModifier(lu);
			yieldHandling(
					new ValueTokenHandler(false) {

						@Override
						void decrParenDepth(int index) {
							parendepth--;
							if (parendepth < 0 && !isInError()) {
								URLTokenHandler.this.parendepth--;
								yieldHandling(URLTokenHandler.this);
							}
						}

						@Override
						public void handleErrorRecovery() {
							URLTokenHandler.this.handleErrorRecovery();
						}

						@Override
						public CSSErrorHandler getErrorHandler() {
							return URLTokenHandler.this.getErrorHandler();
						}

						@Override
						public HandlerManager getManager() {
							return URLTokenHandler.this.getManager();
						}

					});
		} else {
			unexpectedCharError(index, '(');
		}
	}

	@Override
	public void rightParenthesis(int index) {
		parendepth--;
		if (parendepth == 0) {
			processBuffer(index, ')');
			// Decrease caller parentheses depth, which must be 1 or higher,
			// otherwise this handler would have not been instantiated.
			// So we call decrParenDepth() which does not check the depth.
			caller.decrParenDepth();
			endFunctionArgument(index);
		}
		// Cannot reach this
	}

	void endFunctionArgument(int index) {
		getControlHandler().yieldHandling(caller);
		setURL(url, urlUnit);
	}

	@Override
	public void endOfStream(int len) {
		processBuffer(len, 0);
		if (!isInError()) {
			getRootHandler().unexpectedEOFError(len);
		} else {
			getRootHandler().setParseError();
		}
		getRootHandler().endOfStream(len);
	}

	@Override
	protected void resetHandler() {
		super.resetHandler();
		url = null;
		urlUnit = null;
		modifier = null;
	}

	protected void setURL(String url, LexicalUnitImpl urlUnit) {
	}

}
