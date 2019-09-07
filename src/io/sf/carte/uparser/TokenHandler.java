/*

 Copyright (c) 2017-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.uparser;

/**
 * To be implemented by listeners that handle the different events generated by
 * the <code>TokenProducer</code>.
 */
public interface TokenHandler {

	/**
	 * At the beginning of parsing, this method is called, passing the {@link TokenControl}
	 * object that can be used to fine-control the parsing.
	 *
	 * @param control
	 *            the <code>TokenControl</code> object in charge of parsing.
	 */
	void tokenControl(TokenControl control);

	/**
	 * A word was found by the parser (includes connector punctuation).
	 *
	 * @param index
	 *            the index at which the word was found.
	 * @param word
	 *            the word.
	 */
	void word(int index, CharSequence word);

	/**
	 * A separator (Zs, Zl and Zp unicode categories) was found.
	 *
	 * @param index
	 *            the index at which the separator was found.
	 * @param codePoint
	 *            the codepoint of the found separator.
	 */
	void separator(int index, int codePoint);

	/**
	 * A quoted string was found by the parser.
	 *
	 * @param index
	 *            the index at which the quoted string was found.
	 * @param quoted
	 *            the quoted sequence of characters, without the quotes.
	 * @param quote
	 *            the quote character.
	 */
	void quoted(int index, CharSequence quoted, int quote);

	/**
	 * A quoted string was found by the parser, and contains control characters.
	 *
	 * @param index
	 *            the index at which the quoted string was found.
	 * @param quoted
	 *            the quoted sequence of characters, without the quotes.
	 * @param quoteCp
	 *            the quote character codepoint.
	 */
	void quotedWithControl(int index, CharSequence quoted, int quoteCp);

	/**
	 * An unescaped FF/LF/CR control was found while assembling a quoted string.
	 *
	 * @param index
	 *            the index at which the control was found.
	 * @param codePoint
	 *            the FF/LF/CR codepoint.
	 */
	void quotedNewlineChar(int index, int codePoint);

	/**
	 * Called when one of these codepoints is found: (, [, {
	 *
	 * @param index
	 *            the index at which the codepoint was found.
	 * @param codePoint
	 *            the found codepoint.
	 */
	void openGroup(int index, int codePoint);

	/**
	 * Called when one of these codepoints is found: ), ], }
	 *
	 * @param index
	 *            the index at which the codepoint was found.
	 * @param codePoint
	 *            the found codepoint.
	 */
	void closeGroup(int index, int codePoint);

	/**
	 * Other characters including punctuation (excluding connector punctuation) and symbols
	 * (Sc, Sm and Sk unicode categories) was found, that was not one of the non-alphanumeric
	 * characters allowed in words.
	 * <p>
	 * Symbols in So category are considered part of words and won't be handled by this
	 * method.
	 *
	 * @param index
	 *            the index at which the punctuation was found.
	 * @param codePoint
	 *            the codepoint of the found punctuation.
	 */
	void character(int index, int codePoint);

	/**
	 * A codepoint preceded with a backslash was found outside of quoted text.
	 *
	 * @param index
	 *            the index at which the escaped codepoint was found.
	 * @param codePoint
	 *            the escaped codepoint.
	 */
	void escaped(int index, int codePoint);

	/**
	 * A control character codepoint was found.
	 *
	 * @param index
	 *            the index at which the control codepoint was found.
	 * @param codePoint
	 *            the control codepoint.
	 */
	void control(int index, int codePoint);

	/**
	 * A commented string was found by the parser.
	 *
	 * @param index
	 *            the index at which the commented string was found.
	 * @param commentType
	 *            the type of comment.
	 * @param comment
	 *            the commented string.
	 */
	void commented(int index, int commentType, String comment);

	/**
	 * The stream that was being parsed reached its end.
	 *
	 * @param len
	 *            the length of the processed stream.
	 */
	void endOfStream(int len);

	/**
	 * An error was found while parsing.
	 * <p>
	 * Something was found that broke the assumptions made by the parser, like an escape
	 * character at the end of the stream or an unmatched quote.
	 *
	 * @param index
	 *            the index at which the error was found.
	 * @param errCode
	 *            the error code.
	 * @param context
	 *            a context sequence. If a string was parsed, it will contain up to 16
	 *            characters before and after the error.
	 */
	void error(int index, byte errCode, CharSequence context);
}
