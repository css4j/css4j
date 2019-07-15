/*

 Copyright (c) 2017-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.uparser;

import java.io.IOException;
import java.io.Reader;

/**
 * A simple parser that produces tokens from a String or Reader.
 * <p>
 * The tokens produced are:
 * <ul>
 * <li>Words. Contains letters, digits, format characters (Unicode Cf),
 * connector punctuations, certain symbols and any codepoint in
 * <code>allowInWords</code> or accepted by the supplied
 * <code>CharacterCheck</code> object.
 * <li>Quoted text (within single and double quotes).
 * <li>Grouping characters: {}[]().
 * <li>Separators.
 * <li>Escaped characters: anything after a backslash, unless it is within a
 * quoted text.
 * <li>Control characters.
 * <li>Other characters.
 * <li>Comments. (if a comment-supporting method is used)
 * </ul>
 * <p>
 * A moderate level of control of the parsing can be achieved with a
 * {@link TokenControl} object.
 * <p>
 */
public class TokenProducer {

	public static final byte ERR_UNEXPECTED_END_QUOTED = 1;
	public static final byte ERR_LASTCHAR_BACKSLASH = 2;
	public static final byte ERR_UNEXPECTED_END_COMMENTED = 3;

	/**
	 * !
	 */
	public static final int CHAR_EXCLAMATION = 33;

	/**
	 * #
	 */
	public static final int CHAR_NUMBER_SIGN = 35;

	/**
	 * $
	 */
	public static final int CHAR_DOLLAR = 36;

	/**
	 * %
	 */
	public static final int CHAR_PERCENT_SIGN = 37;

	/**
	 * (
	 */
	public static final int CHAR_LEFT_PAREN = 40;

	/**
	 * )
	 */
	public static final int CHAR_RIGHT_PAREN = 41;

	/**
	 * *
	 */
	public static final int CHAR_ASTERISK = 42;

	/**
	 * +
	 */
	public static final int CHAR_PLUS = 43;

	/**
	 * ,
	 */
	public static final int CHAR_COMMA = 44;

	/**
	 * -
	 */
	public static final int CHAR_HYPHEN_MINUS = 45;

	/**
	 * .
	 */
	public static final int CHAR_FULL_STOP = 46;

	/**
	 * /
	 */
	public static final int CHAR_SLASH = 47;

	/**
	 * :
	 */
	public static final int CHAR_COLON = 58;

	/**
	 * ;
	 */
	public static final int CHAR_SEMICOLON = 59;

	/**
	 * &lt;
	 */
	public static final int CHAR_LESS_THAN = 60;

	/**
	 * =
	 */
	public static final int CHAR_EQUALS = 61;

	/**
	 * &gt;
	 */
	public static final int CHAR_GREATER_THAN = 62;

	/**
	 * ?
	 */
	public static final int CHAR_QUESTION_MARK = 63;

	/**
	 * {@literal @}
	 */
	public static final int CHAR_COMMERCIAL_AT = 64;

	/**
	 * [
	 */
	public static final int CHAR_LEFT_SQ_BRACKET = 91;

	/**
	 * ]
	 */
	public static final int CHAR_RIGHT_SQ_BRACKET = 93;

	/**
	 * ^
	 */
	public static final int CHAR_CIRCUMFLEX_ACCENT = 94;

	/**
	 * {
	 */
	public static final int CHAR_LEFT_CURLY_BRACKET = 123;

	/**
	 * |
	 */
	public static final int CHAR_VERTICAL_LINE = 124;

	/**
	 * }
	 */
	public static final int CHAR_RIGHT_CURLY_BRACKET = 125;

	/**
	 * ~
	 */
	public static final int CHAR_TILDE = 126;

	private final TokenHandler handler;
	private final CharacterCheck charCheck;
	private boolean handleAllSeparators = true;
	private boolean acceptNewlineEndingQuote = false;
	private boolean acceptEofEndingQuoted = false;

	public TokenProducer(TokenHandler handler) {
		super();
		this.handler = handler;
		this.charCheck = new DisallowCharacterCheck();
	}

	/**
	 * Instantiate a <code>TokenProducer</code> object with the given handler and
	 * <code>CharacterCheck</code>.
	 * 
	 * @param handler
	 *            the token handler.
	 * @param charCheck
	 *            the character checker object.
	 */
	public TokenProducer(TokenHandler handler, CharacterCheck charCheck) {
		super();
		this.handler = handler;
		this.charCheck = charCheck;
	}

	public TokenProducer(TokenHandler handler, int[] allowInWords) {
		super();
		this.handler = handler;
		charCheck = new WhitelistCharacterCheck(allowInWords);
	}

	/**
	 * Set the handling of consecutive separators like whitespace or tabs.
	 * <p>
	 * Default is <code>true</code>.
	 * 
	 * @param handleAllSeparators
	 *            if set to <code>true</code>, all separator characters (including consecutive
	 *            ones) will trigger a {@link TokenHandler#separator(int, int)} method call.
	 *            Otherwise only single separations between the other types of tokens will be
	 *            taken into account.
	 */
	public void setHandleAllSeparators(boolean handleAllSeparators) {
		this.handleAllSeparators = handleAllSeparators;
	}

	/**
	 * If set, quoted strings ending with an unescaped newline (instead of the closing quote)
	 * are processed through the relevant <code>quoted</code> method, albeit an error is
	 * reported in any case. Otherwise, only the error is reported.
	 * <p>
	 * It is set to <code>false</code> by default.
	 * 
	 * @param accept
	 *            true to process quoted strings that ends with an unescaped newline, false
	 *            otherwise.
	 */
	public void setAcceptNewlineEndingQuote(boolean accept) {
		this.acceptNewlineEndingQuote = accept;
	}

	/**
	 * If set, quoted strings ending with an EOF (End Of File). are processed through the
	 * relevant <code>quoted</code> method, albeit an error is reported in any case.
	 * Otherwise, only the error is reported.
	 * <p>
	 * It is set to <code>false</code> by default.
	 * 
	 * @param accept
	 *            true to process quoted strings that ends with an EOF, <code>false</code> otherwise.
	 */
	public void setAcceptEofEndingQuoted(boolean accept) {
		this.acceptEofEndingQuoted = accept;
	}

	public void parse(String string) {
		StringParser sp = new StringParser(string);
		try {
			sp.parse();
		} catch (IOException e) {
		}
	}

	public void parse(String string, String commentOpen, String commentClose) {
		StringParser sp = new StringParser(string, commentOpen, commentClose);
		try {
			sp.parse();
		} catch (IOException e) {
		}
	}

	public void parse(Reader reader) throws IOException {
		ReaderParser sp = new ReaderParser(reader);
		sp.parse();
	}

	public void parse(Reader reader, int bufferCapacity) throws IOException {
		ReaderParser sp = new ReaderParser(reader, bufferCapacity);
		sp.parse();
	}

	/**
	 * Parse the given reader, with a single comment layout.
	 * 
	 * @param reader
	 *            the reader to parse.
	 * @param commentOpen
	 *            the token that opens a comment. It is not allowed to repeat the same
	 *            character at the beginning of the token. For example, <code>&lt;!--</code>
	 *            is a valid token but <code>&lt;&lt;--</code> would not.
	 * @param commentClose
	 *            the token that closes a comment.
	 * @throws IOException
	 *             if an I/O problem was found parsing the reader.
	 */
	public void parse(Reader reader, String commentOpen, String commentClose) throws IOException {
		ReaderParser sp = new ReaderParser(reader, commentOpen, commentClose);
		sp.parse();
	}

	/**
	 * Parse the given reader, with multiple comment layouts.
	 * 
	 * @param reader
	 *            the reader to parse.
	 * @param opening
	 *            the array of tokens that open a comment. It is not allowed to repeat the
	 *            same character at the beginning of a token. For example,
	 *            <code>&lt;!--</code> is a valid token but <code>&lt;&lt;--</code> would not.
	 * @param closing
	 *            the array of tokens that close the comment opened with the
	 *            <code>opening</code> at the same index.
	 * @throws IOException
	 *             if an I/O problem was found parsing the reader.
	 */
	public void parseMultiComment(Reader reader, String[] opening, String[] closing) throws IOException {
		ReaderMultiCommentParser sp = new ReaderMultiCommentParser(reader, opening, closing);
		sp.parse();
	}

	/**
	 * Basic access to the current sequence in this tokenizer.
	 * <p>
	 * It is intended to be used by objects implementing {@link CharacterCheck}.
	 */
	public interface SequenceParser {
		/**
		 * Give the contents of the word currently being formed.
		 * 
		 * @return the contents of the word currently being formed.
		 */
		CharSequence currentSequence();

		/**
		 * Was the previous codepoint added to the current word sequence as a
		 * CharacterCheck-accepted character ?
		 * <p>
		 * A CharacterCheck-accepted character is different to normal word characters, and is only
		 * added to a word if it is allowed by the CharacterCheck process.
		 * 
		 * @see CharacterCheck
		 * 
		 * @return <code>true</code> if the previous codepoint was added to the current word sequence as an
		 *         external, CharacterCheck-accepted character.
		 */
		boolean isPreviousCpWCharacter();

		/**
		 * Empty the current sequence.
		 */
		void resetCurrentSequence();
	}

	abstract class AbstractSequenceParser implements SequenceParser {
		/**
		 * The type of the previous character
		 */
		int prevtype = Character.SPACE_SEPARATOR;

		/**
		 * The index of the character currently in process, also used as an upper delimiter for a
		 * sequence.
		 */
		int rootIndex = 0;

		/**
		 * The lower bound for a character sequence, marks where the processing of the current
		 * sequence started.
		 */
		int previdx = 0;

		CommentManager commentManager;

		AbstractSequenceParser() {
			super();
		}

		/**
		 * Process a code point.
		 * 
		 * @param cp
		 *            the codepoint to process.
		 * @param nocomment
		 *            if true, do no comment character verification.
		 * @return 2 if a comment was processed, 1 if a non-comment-start codepoint was normally
		 *         processed, -1 if was expecting a comment-begin codepoint but no longer (but
		 *         still unprocessed), and 0 if a comment start character was found but not
		 *         processed. And -2 if a comment-begin codepoint was expected, but found a comment
		 *         start first character.
		 * 
		 * @throws IOException
		 */
		int processCodePoint(int cp, boolean nocomment) throws IOException {
			// Verify if matches comment delimiters
			if (!nocomment) {
				int result = commentManager.verifyComment(cp);
				if (result != 1) {
					return result;
				}
			}
			// Main codepoint processing
			int type = Character.getType(cp);
			switch (type) {
			case Character.SPACE_SEPARATOR:
			case Character.LINE_SEPARATOR:
			case Character.PARAGRAPH_SEPARATOR:
				if (handleAllSeparators || prevtype != Character.SPACE_SEPARATOR) {
					checkPreviousWord();
					handler.separator(rootIndex, cp);
					prevtype = Character.SPACE_SEPARATOR;
				}
				previdx = rootIndex + 1;
				break;
			case Character.LOWERCASE_LETTER:
			case Character.UPPERCASE_LETTER:
			case Character.DECIMAL_DIGIT_NUMBER:
			case Character.MODIFIER_LETTER:
			case Character.OTHER_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.LETTER_NUMBER:
			case Character.OTHER_NUMBER:
			case Character.CONNECTOR_PUNCTUATION:
			case Character.ENCLOSING_MARK:
			case Character.COMBINING_SPACING_MARK:
			case Character.NON_SPACING_MARK:
			case Character.FORMAT:
				prevtype = Character.LOWERCASE_LETTER;
				break;
			case Character.CONTROL:
				checkPreviousWord();
				prevtype = Character.CONTROL;
				handler.control(rootIndex, cp);
				previdx = rootIndex + 1;
				break;
			case Character.START_PUNCTUATION:
				if (charCheck.isAllowedCharacter(cp, this)) {
					prevtype = Character.UPPERCASE_LETTER;
					break;
				}
				checkPreviousWord();
				if (cp == 40 || cp == 91 || cp == 123) {
					// ([{
					handler.openGroup(rootIndex, cp);
					previdx = rootIndex + 1;
					prevtype = Character.OTHER_PUNCTUATION;
					break;
				}
				handleCharacter(cp);
				updatePrev(cp);
				break;
			case Character.END_PUNCTUATION:
				if (charCheck.isAllowedCharacter(cp, this)) {
					prevtype = Character.UPPERCASE_LETTER;
					break;
				}
				checkPreviousWord();
				if (cp == 41 || cp == 93 || cp == 125) {
					// )]}
					handler.closeGroup(rootIndex, cp);
					previdx = rootIndex + 1;
					prevtype = Character.OTHER_PUNCTUATION;
					break;
				}
				handleCharacter(cp);
				updatePrev(cp);
				break;
			case Character.OTHER_PUNCTUATION:
				if (charCheck.isAllowedCharacter(cp, this)) {
					prevtype = Character.UPPERCASE_LETTER;
					break;
				}
				checkPreviousWord();
				if (cp == 34 || cp == 39) {
					// " '
					quoted(cp);
				} else if (cp == 92) {
					// \ backslash
					int ncp = nextCodepoint();
					if (ncp != -1) {
						handler.escaped(rootIndex, ncp);
					} else {
						error(rootIndex - 1, ERR_LASTCHAR_BACKSLASH);
					}
				} else {
					handleCharacter(cp);
				}
				updatePrev(cp);
				break;
			case Character.MODIFIER_SYMBOL:
				// includes '^' (94), '`' (96)
			case Character.MATH_SYMBOL:
				// includes '+' (43), '|' (124), '~' (126), '<' (60)
			case Character.CURRENCY_SYMBOL:
				// includes '$'
			case Character.DASH_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
			case Character.OTHER_SYMBOL:
			case Character.SURROGATE:
				if (charCheck.isAllowedCharacter(cp, this)) {
					prevtype = Character.UPPERCASE_LETTER;
					break;
				}
				checkPreviousWord();
				handleCharacter(cp);
			default:
				updatePrev(cp);
			}
			return 1;
		}

		private void updatePrev(int cp) {
			previdx = rootIndex + Character.charCount(cp);
			prevtype = Character.OTHER_PUNCTUATION;
		}

		private void checkPreviousWord() {
			if (isWordPreviousCodepoint()) {
				word();
			}
		}

		boolean isWordPreviousCodepoint() {
			if (prevtype == Character.LOWERCASE_LETTER) {
				return true;
			}
			if (isPreviousCpWCharacter()) {
				if (currentSequence().length() == 1) {
					handler.character(previdx, Character.codePointAt(currentSequence(), 0));
					resetCurrentSequence();
				} else {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean isPreviousCpWCharacter() {
			return prevtype == Character.UPPERCASE_LETTER;
		}

		protected void word() {
			CharSequence seq = currentSequence();
			if (seq.length() != 1 || !isPreviousCpWCharacter()) {
				handler.word(previdx, seq);
			} else {
				handler.character(previdx, seq.charAt(0));
			}
		}

		private void handleCharacter(int cp) {
			handler.character(rootIndex, cp);
		}

		/**
		 * Handle a quoted string.
		 * 
		 * @param quoteCp the quote character.
		 * @throws IOException
		 */
		protected void quoted(int quoteCp) throws IOException {
			CharSequence seq = quotedSequence(quoteCp);
			if (seq != null) {
				handler.quoted(rootIndex, seq, quoteCp);
			} // an unexpected control was found.
		}

		public void parse() throws IOException {
			this.commentManager.parse();
		}

		abstract class CommentManager implements TokenControl {

			int commentIndex = 0;
			int commentprevtype = 0;

			void parse() throws IOException {
				handler.tokenControl(this);
			}

			/**
			 * If set, quoted strings ending with an unescaped newline (instead of the closing quote)
			 * are processed through the relevant <code>quoted</code> method, albeit an error is
			 * reported in any case. Otherwise, only the error is reported.
			 * <p>
			 * It is set to <code>false</code> by default.
			 * 
			 * @param accept
			 *            true to process quoted strings that ends with an unescaped newline, false
			 *            otherwise.
			 */
			@Override
			public void setAcceptNewlineEndingQuote(boolean accept) {
				TokenProducer.this.setAcceptNewlineEndingQuote(accept);
			}

			/**
			 * If set, quoted strings ending with an EOF (End Of File). are processed through the
			 * relevant <code>quoted</code> method, albeit an error is reported in any case.
			 * Otherwise, only the error is reported.
			 * <p>
			 * It is set to <code>false</code> by default.
			 * 
			 * @param accept
			 *            true to process quoted strings that ends with an EOF, <code>false</code> otherwise.
			 */
			@Override
			public void setAcceptEofEndingQuoted(boolean accept) {
				TokenProducer.this.setAcceptEofEndingQuoted(accept);
			}

			/**
			 * Verifies a code point for comments.
			 * 
			 * @param cp the codepoint to verify.
			 * @return 2 if a comment was processed, 1 if a non-comment-start codepoint was
			 *         passed, -1 if was expecting a subsequent comment character but no
			 *         longer (but still unprocessed), and 0 if a comment start character
			 *         was found but not processed yet (not yet a full comment begin
			 *         string). And -2 if a comment-begin codepoint was expected, but found
			 *         a comment start first character.
			 * 
			 * @throws IOException
			 */
			int verifyComment(int cp) throws IOException {
				if (matchesExpectedOpening(cp)) {
					if (commentIndex == 0) {
						commentprevtype = prevtype;
					}
					commentIndex++;
					if (commentIndex == expectedOpeningLength()) {
						prevtype = commentprevtype;
						if (isWordPreviousCodepoint()) {
							wordPrecedingComment();
						}
						int openingLength = expectedOpeningLength();
						commentIndex = 0;
						rootIndex++;
						previdx = rootIndex;
						String comment = commentedSequence();
						handler.commented(previdx - openingLength, getLastCommentClass(), comment);
						resetCurrentSequence();
						rootIndex--;
						prevtype = -1;
						return 2;
					} else {
						return 0;
					}
				} else if (commentIndex != 0) {
					commentIndex = 0;
					commentprevtype = 0;
					return matchesExpectedOpening(cp) ? -2 : -1;
				}
				return 1;
			}

			void wordPrecedingComment() {
				word();
			}

			abstract boolean matchesExpectedOpening(int cp);

			abstract int expectedOpeningLength();

			abstract int closingLength();

			abstract int getLastCommentClass();

			abstract String commentedSequence() throws IOException;

		}

		abstract class NoCommentManager extends CommentManager {

			NoCommentManager() {
				super();
			}

			@Override
			public void enableAllComments() {
			}

			@Override
			public void disableAllComments() {
			}

			@Override
			public void enableComments(int type) {
			}

			@Override
			public void disableComments(int type) {
			}

			@Override
			boolean matchesExpectedOpening(int cp) {
				return false;
			}

			@Override
			int verifyComment(int cp) throws IOException {
				return 1;
			}

			@Override
			int expectedOpeningLength() {
				return 0;
			}

			@Override
			int closingLength() {
				return 0;
			}

			@Override
			int getLastCommentClass() {
				return 0;
			}

			@Override
			String commentedSequence() throws IOException {
				return "";
			}

		}

		abstract class SingleCommentManager extends CommentManager {
			int[] commentStart;
			int[] commentEnd;
			private boolean disabledComments = false;

			SingleCommentManager(String start, String end) {
				super();
				setCommentDelimiters(start, end);
			}

			@Override
			boolean matchesExpectedOpening(int cp) {
				return commentStart[commentIndex] == cp && !disabledComments;
			}

			@Override
			int expectedOpeningLength() {
				return commentStart.length;
			}

			@Override
			int getLastCommentClass() {
				return 0;
			}

			@Override
			int closingLength() {
				return commentEnd.length;
			}

			boolean closingEndsWithLF() {
				return closingEndsWith(13);
			}

			boolean closingEndsWith(int cp) {
				return closingCodepointAt(closingLength() - 1) == cp;
			}

			int closingCodepointAt(int idx) {
				return commentEnd[idx];
			}

			private void setCommentDelimiters(String start, String end) {
				int slen = start.length();
				int elen = end.length();
				int[] commentStart = new int[slen];
				int[] commentEnd = new int[elen];
				for (int i = 0; i < slen; i++) {
					commentStart[i] = start.codePointAt(i);
					if (commentStart[i] == 92) { // \ backslash
						throw new IllegalArgumentException("Not an allowed comment delimiter");
					}
				}
				for (int i = 0; i < elen; i++) {
					commentEnd[i] = end.codePointAt(i);
					if (commentEnd[i] == 92) { // \ backslash
						throw new IllegalArgumentException("Not an allowed comment delimiter");
					}
				}
				this.commentStart = commentStart;
				this.commentEnd = commentEnd;
			}

			@Override
			public void enableAllComments() {
				disabledComments = false;
			}

			@Override
			public void disableAllComments() {
				disabledComments = true;
			}

			@Override
			public void enableComments(int type) {
				if (type == 0) {
					disabledComments = false;
				}
			}

			@Override
			public void disableComments(int type) {
				if (type == 0) {
					disabledComments = true;
				}
			}
		}

		abstract class MultiCommentManager extends CommentManager {
			private int delimcount = 0;
			private int[][] commentStart;
			private int[][] commentEnd;
			private boolean[] disabledComments;
			private int commentClass = -1; // The initial value does not matter

			MultiCommentManager(String[] start, String[] end) {
				super();
				setCommentDelimiters(start, end);
			}

			@Override
			boolean matchesExpectedOpening(int cp) {
				if (isBeginningOfOpening()) {
					for (int i = 0; i < delimcount; i++) {
						if (commentStart[i][0] == cp && !disabledComments[i]) {
							commentClass = i;
							return true;
						}
					}
					return false;
				}
				if (commentStart[commentClass][commentIndex] == cp && !disabledComments[commentClass]) {
					return true;
				}
				// Check other possible matches (e.g. "//" vs "/*")
				topLoop: for (int i = commentClass + 1; i < delimcount; i++) {
					int len = commentStart[i].length;
					if (commentIndex < len && commentStart[i][commentIndex] == cp && !disabledComments[i]) {
						for (int j = commentIndex - 1; j != -1; j--) {
							if (commentStart[i][j] != cp) {
								continue topLoop;
							}
						}
						commentClass = i;
						return true;
					}
				}
				return false;
			}

			@Override
			int expectedOpeningLength() {
				return commentIndex == 0 ? -1 : commentStart[commentClass].length;
			}

			boolean isBeginningOfOpening() {
				return commentIndex == 0;
			}

			@Override
			int getLastCommentClass() {
				return commentClass;
			}

			int getMaxOpeningLength() {
				int slen = commentStart[0].length;
				for (int i = 1; i < delimcount; i++) {
					int len = commentStart[i].length;
					if (len > slen) {
						slen = len;
					}
				}
				return slen;
			}

			@Override
			int closingLength() {
				return commentEnd[commentClass].length;
			}

			boolean closingEndsWithLF() {
				return closingEndsWith(13);
			}

			boolean closingEndsWith(int cp) {
				return closingCodepointAt(closingLength() - 1) == cp;
			}

			int closingCodepointAt(int idx) {
				return commentEnd[commentClass][idx];
			}

			private void setCommentDelimiters(String[] start, String[] end) {
				delimcount = start.length;
				if (delimcount != end.length) {
					throw new IllegalArgumentException("Unmatched begin/end comment delimiters");
				}
				disabledComments = new boolean[delimcount];
				int[][] commentStart = new int[delimcount][];
				int[][] commentEnd = new int[delimcount][];
				for (int i = 0; i < delimcount; i++) {
					String s = start[i];
					int len = s.length();
					commentStart[i] = new int[len];
					for (int j = 0; j < len; j++) {
						int cp = s.codePointAt(j);
						commentStart[i][j] = cp;
						if (cp == 92) { // \ backslash
							throw new IllegalArgumentException("Not an allowed comment delimiter");
						}
					}
					s = end[i];
					len = s.length();
					commentEnd[i] = new int[len];
					for (int j = 0; j < len; j++) {
						int cp = s.codePointAt(j);
						commentEnd[i][j] = cp;
						if (cp == 92) { // \ backslash
							throw new IllegalArgumentException("Not an allowed comment delimiter");
						}
					}
				}
				this.commentStart = commentStart;
				this.commentEnd = commentEnd;
			}

			@Override
			public void enableAllComments() {
				for (int i = 0; i < disabledComments.length; i++) {
					disabledComments[i] = false;
				}
			}

			@Override
			public void disableAllComments() {
				for (int i = 0; i < disabledComments.length; i++) {
					disabledComments[i] = true;
				}
			}

			@Override
			public void enableComments(int type) {
				disabledComments[type] = false;
			}

			@Override
			public void disableComments(int type) {
				disabledComments[type] = true;
			}

		}

		/**
		 * Gives the next code point in the stream.
		 * 
		 * @return the next code point, or -1 if the end of the stream was reached.
		 * 
		 * @throws IOException
		 */
		abstract protected int nextCodepoint() throws IOException;

		/**
		 * Produces a quoted sequence that starts with the current code point, up to the
		 * given delimiter code point.
		 * 
		 * @param cp the delimiter code point.
		 * @return the quoted sequence, or <code>null</code> if an unexpected EOF was
		 *         found before the end of the sequence.
		 * 
		 * @throws IOException
		 */
		abstract protected CharSequence quotedSequence(int cp) throws IOException;

		abstract protected void error(int index, byte errCode);

	}

	private class StringParser extends AbstractSequenceParser {
		private String string;
		private int len;

		StringParser(String string) {
			super();
			this.commentManager = new StringNoCommentManager();
			this.string = string;
			len = string.length();
		}

		StringParser(String string, String start, String end) {
			super();
			this.commentManager = new StringSingleCommentManager(start, end);
			this.string = string;
			len = string.length();
		}

		@Override
		public CharSequence currentSequence() {
			return string.subSequence(previdx, rootIndex);
		}

		@Override
		public void resetCurrentSequence() {
			previdx = rootIndex;
		}

		@Override
		protected int nextCodepoint() throws IOException {
			if (rootIndex < len - 1) {
				rootIndex = string.offsetByCodePoints(rootIndex, 1);
				return string.codePointAt(rootIndex);
			} else {
				return -1;
			}
		}

		@Override
		protected CharSequence quotedSequence(int qcp) {
			boolean containsControls = false;
			boolean lastCpEscaped13 = false;
			int initial = rootIndex + 1, prevcp = -1;
			StringBuilder buffer = new StringBuilder(len - initial);
			for (int idx = initial; idx < len; idx++) {
				int cp = string.codePointAt(idx);
				if (cp == qcp) {
					// Quote character found
					if (prevcp != 92) {
						// not \ backslash
						if (containsControls) {
							handler.quotedWithControl(rootIndex, buffer, qcp);
							buffer = null;
						}
						rootIndex = idx;
						return buffer;
					}
					buffer.append(Character.toChars(qcp));
				} else if (Character.isISOControl(cp)) {
					if (cp == 10 || cp == 12 || cp == 13) {
						// LF/FF/CR character found
						if (prevcp != 92) {
							// not \ backslash
							if (cp != 10 || !lastCpEscaped13) {
								handler.error(idx, ERR_UNEXPECTED_END_QUOTED, buffer);
								handler.control(idx, cp);
								rootIndex = idx;
								if (!acceptNewlineEndingQuote) {
									return null;
								} else {
									return buffer;
								}
							}
						} else {
							// trim last backslash
							buffer.setLength(buffer.length() - 1);
							handler.quotedNewlineChar(idx, cp);
						}
						if (!lastCpEscaped13) {
							buffer.append('\n');
						}
						lastCpEscaped13 = cp == 13;
						prevcp = cp;
						continue;
					} else {
						buffer.append(Character.toChars(cp));
						containsControls = true;
					}
				} else {
					buffer.append(Character.toChars(cp));
					if (cp == 92 && prevcp == 92) {
						cp = 65;
					}
				}
				prevcp = cp;
				lastCpEscaped13 = false;
			}
			rootIndex = len;
			error(initial, ERR_UNEXPECTED_END_QUOTED);
			if (acceptEofEndingQuoted) {
				return buffer;
			}
			return null;
		}

		@Override
		protected void error(int index, byte errCode) {
			int beginIndex = index - 16;
			if (beginIndex < 0) {
				beginIndex = 0;
			}
			int endIndex = index + 16;
			if (endIndex > string.length()) {
				endIndex = string.length();
			}
			handler.error(index, errCode, string.substring(beginIndex, endIndex));
		}

		class StringNoCommentManager extends NoCommentManager {

			@Override
			void parse() throws IOException {
				super.parse();
				int cp;
				for (rootIndex = 0; rootIndex < len; rootIndex += Character.charCount(cp)) {
					cp = string.codePointAt(rootIndex);
					processCodePoint(cp, true);
				}
				int remlen = len - previdx;
				if (remlen > 0) {
					if (remlen != 1 || !isPreviousCpWCharacter()) {
						handler.word(previdx, currentSequence());
					} else {
						// Isolated CharacterCheck-characters are handled as plain characters
						handler.character(previdx, string.charAt(previdx));
					}
				}
				handler.endOfStream(len);
			}

		}

		class StringSingleCommentManager extends SingleCommentManager {

			StringSingleCommentManager(String start, String end) {
				super(start, end);
			}

			@Override
			void parse() throws IOException {
				super.parse();
				int unprocessedIdx = -1;
				int[] unprocessed = new int[commentStart.length];
				int cp;
				for (rootIndex = 0; rootIndex < len; rootIndex += Character.charCount(cp)) {
					cp = string.codePointAt(rootIndex);
					if (unprocessedIdx == -2) {
						unprocessedIdx = -1;
						if (cp == 10) {
							// Ignore 10 after 13
							continue;
						}
					}
					int ret = processCodePoint(cp, false);
					if (ret == 0) {
						unprocessedIdx++;
						unprocessed[unprocessedIdx] = cp;
					} else if (ret == 2) {
						if (commentEnd[commentEnd.length - 1] == 13) {
							// If last delimiter was LF, flag it
							unprocessedIdx = -2;
						} else {
							unprocessedIdx = -1;
						}
					} else if (ret == -1) {
						rootIndex = rootIndex - unprocessedIdx - 1;
						for (int j = 0; j <= unprocessedIdx; j++) {
							processCodePoint(unprocessed[j], true);
							rootIndex++;
						}
						processCodePoint(cp, true);
						unprocessedIdx = -1;
					} else if (ret == -2) {
						rootIndex = rootIndex - unprocessedIdx - 1;
						for (int j = 0; j <= unprocessedIdx; j++) {
							processCodePoint(unprocessed[j], true);
							rootIndex++;
						}
						unprocessedIdx = -1;
						if (processCodePoint(cp, false) != -2) {
							unprocessedIdx = 0;
							unprocessed[0] = cp;
						}
					}
				}
				int remlen = len - previdx;
				if (unprocessedIdx != -1) {
					rootIndex -= unprocessedIdx + 1;
					remlen -= unprocessedIdx + 1;
				}
				if (remlen > 0) {
					if (remlen != 1 || !isPreviousCpWCharacter()) {
						handler.word(previdx, currentSequence());
						previdx = rootIndex;
					} else {
						// Isolated CharacterCheck-characters are handled as plain characters
						handler.character(previdx, string.charAt(previdx));
						previdx++;
					}
				}
				if (unprocessedIdx != -1) {
					for (int i = 0; i <= unprocessedIdx; i++) {
						handler.character(previdx + i, unprocessed[i]);
					}
				}
				handler.endOfStream(len);
			}

			@Override
			protected String commentedSequence() throws IOException {
				boolean lastCp13 = false;
				int endIndex = 0;
				StringBuilder buffer = new StringBuilder(len - rootIndex);
				for (int idx = rootIndex; idx < len; idx++) {
					int cp = string.codePointAt(idx);
					if (cp == 10 || cp == 12 || cp == 13) {
						if (cp != 10 || !lastCp13) {
							handler.control(idx, cp);
						}
						lastCp13 = cp == 13;
					} else {
						lastCp13 = false;
					}
					if (commentEnd[endIndex] == cp) {
						endIndex++;
						if (endIndex == commentEnd.length) {
							rootIndex = idx + 1;
							return buffer.toString();
						} else {
							continue;
						}
					} else if (endIndex != 0) {
						for (int j = 0; j <= endIndex; j++) {
							buffer.append(commentEnd[j]);
						}
						endIndex = 0;
					}
					buffer.append(Character.toChars(cp));
				}
				rootIndex = len;
				if (!closingEndsWith(10)) {
					error(rootIndex, ERR_UNEXPECTED_END_COMMENTED);
				}
				return buffer.toString();
			}

			@Override
			void wordPrecedingComment() {
				int endIndex = rootIndex - commentIndex + 1;
				if (endIndex - previdx != 1 || !isPreviousCpWCharacter()) {
					handler.word(previdx, string.subSequence(previdx, endIndex));
				} else {
					handler.character(previdx, string.charAt(previdx));
				}
			}

		}

	}

	abstract private class AbstractReaderParser extends AbstractSequenceParser {
		Reader reader;
		StringBuilder buffer;

		AbstractReaderParser(Reader reader, int bufferCapacity) {
			super();
			this.reader = reader;
			buffer = new StringBuilder(bufferCapacity);
		}

		@Override
		public CharSequence currentSequence() {
			return buffer;
		}

		@Override
		public void resetCurrentSequence() {
			buffer.setLength(0);
		}

		/**
		 * Gives the next code point in the stream.
		 * 
		 * @return the next code point, or -1 if the end of the stream was reached.
		 */
		@Override
		protected int nextCodepoint() throws IOException {
			int ncp = reader.read();
			rootIndex += Character.charCount(ncp);
			return ncp;
		}

		@Override
		protected CharSequence quotedSequence(int qcp) throws IOException {
			boolean containsControls = false;
			boolean lastCpEscaped13 = false;
			int idx = rootIndex, prevcp = -1;
			int ncp;
			while ((ncp = nextCodepoint()) != -1) {
				if (ncp == qcp) {
					// Quote character found
					if (prevcp != 92) {
						// not \ backslash
						if (!containsControls) {
							return buffer;
						} else {
							handler.quotedWithControl(rootIndex, buffer, qcp);
							return null;
						}
					}
					idx++;
					buffer.append(Character.toChars(qcp));
				} else if (Character.isISOControl(ncp)) {
					if (ncp == 10 || ncp == 12 || ncp == 13) {
						// LF/FF/CR character found
						if (prevcp != 92) {
							// not \ backslash
							if (ncp != 10 || !lastCpEscaped13) {
								handler.error(rootIndex, ERR_UNEXPECTED_END_QUOTED, buffer);
								handler.control(rootIndex, ncp);
								String s;
								if (!acceptNewlineEndingQuote) {
									s = null;
								} else {
									s = buffer.toString();
								}
								buffer.setLength(0);
								return s;
							}
						} else {
							// trim last backslash
							buffer.setLength(buffer.length() - 1);
							handler.quotedNewlineChar(idx, ncp);
						}
						if (!lastCpEscaped13) {
							buffer.append('\n');
						}
						lastCpEscaped13 = ncp == 13;
						prevcp = ncp;
						continue;
					} else {
						buffer.append(Character.toChars(ncp));
						idx++;
						containsControls = true;
					}
				} else {
					buffer.append(Character.toChars(ncp));
					if (ncp == 92 && prevcp == 92) {
						ncp = 65;
					}
					idx++;
				}
				prevcp = ncp;
				lastCpEscaped13 = false;
			}
			error(rootIndex, ERR_UNEXPECTED_END_QUOTED);
			if (acceptEofEndingQuoted) {
				return buffer;
			}
			return null;
		}

		@Override
		protected void word() {
			super.word();
			buffer.setLength(0);
		}

		@Override
		protected void quoted(int quoteCp) throws IOException {
			super.quoted(quoteCp);
			buffer.setLength(0);
		}

		@Override
		protected void error(int index, byte errCode) {
			handler.error(index, errCode, buffer);
		}

	}

	private class ReaderParser extends AbstractReaderParser {

		ReaderParser(Reader reader) {
			this(reader, 256);
		}

		ReaderParser(Reader reader, int bufferCapacity) {
			super(reader, bufferCapacity);
			this.commentManager = new ReaderNoCommentManager();
		}

		ReaderParser(Reader reader, String start, String end) {
			this(reader, 256, start, end);
		}

		ReaderParser(Reader reader, int bufferCapacity, String start, String end) {
			super(reader, bufferCapacity);
			this.commentManager = new ReaderSingleCommentManager(start, end);
		}

		private class ReaderNoCommentManager extends NoCommentManager {
			@Override
			void parse() throws IOException {
				super.parse();
				rootIndex = 0;
				int cp = 0;
				while ((cp = reader.read()) != -1) {
					processCodePoint(cp, true);
					if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
						buffer.append(Character.toChars(cp));
					}
					rootIndex += Character.charCount(cp);
				}
				int buflen = currentSequence().length();
				if (buflen != 0) {
					if (buflen != 1 || !isPreviousCpWCharacter()) {
						handler.word(previdx, currentSequence());
					} else {
						// Isolated CharacterCheck-characters are handled as plain characters
						handler.character(previdx, currentSequence().charAt(0));
					}
				}
				handler.endOfStream(rootIndex);
			}

		}

		private class ReaderSingleCommentManager extends SingleCommentManager {

			ReaderSingleCommentManager(String start, String end) {
				super(start, end);
			}

			@Override
			void parse() throws IOException {
				super.parse();
				int unprocessedIdx = -1;
				int[] unprocessed = new int[commentStart.length];
				rootIndex = 0;
				int cp = 0;
				while ((cp = reader.read()) != -1) {
					if (unprocessedIdx == -2) {
						unprocessedIdx = -1;
						if (cp == 10) {
							// Ignore 10 after 13
							continue;
						}
					}
					int ret = processCodePoint(cp, false);
					if (ret == 0) {
						unprocessedIdx++;
						unprocessed[unprocessedIdx] = cp;
					} else if (ret == 2) {
						if (commentEnd[commentEnd.length - 1] == 13) {
							// If last delimiter was LF, flag it
							unprocessedIdx = -2;
						} else {
							unprocessedIdx = -1;
						}
					} else if (ret == -1) {
						for (int j = 0; j <= unprocessedIdx; j++) {
							int oldcp = unprocessed[j];
							processCodePoint(oldcp, true);
							if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
								buffer.append(Character.toChars(oldcp));
							}
						}
						processCodePoint(cp, true);
						unprocessedIdx = -1;
						if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
							buffer.append(Character.toChars(cp));
						}
					} else if (ret == -2) {
						for (int j = 0; j <= unprocessedIdx; j++) {
							int oldcp = unprocessed[j];
							processCodePoint(oldcp, true);
							if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
								buffer.append(Character.toChars(oldcp));
							}
						}
						unprocessedIdx = -1;
						if (processCodePoint(cp, false) != -2) {
							unprocessedIdx = 0;
							unprocessed[0] = cp;
						}
					} else if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
						buffer.append(Character.toChars(cp));
					}
					rootIndex += Character.charCount(cp);
				}
				int buflen = currentSequence().length();
				if (buflen != 0) {
					if (buflen != 1 || !isPreviousCpWCharacter()) {
						handler.word(previdx, currentSequence());
					} else {
						// Isolated CharacterCheck-characters are handled as plain characters
						handler.character(previdx, currentSequence().charAt(0));
					}
				}
				if (unprocessedIdx != -1) {
					previdx = rootIndex - unprocessedIdx - 1;
					for (int i = 0; i <= unprocessedIdx; i++) {
						handler.character(previdx + i, unprocessed[i]);
					}
				}
				handler.endOfStream(rootIndex);
			}

			@Override
			protected String commentedSequence() throws IOException {
				boolean lastCp13 = false;
				int endIndex = 0;
				int ncp;
				while ((ncp = nextCodepoint()) != -1) {
					if (ncp == 10 || ncp == 12 || ncp == 13) {
						if (ncp != 10 || !lastCp13) {
							handler.control(rootIndex - 1, ncp);
						}
						lastCp13 = ncp == 13;
					} else {
						lastCp13 = false;
					}
					if (commentEnd[endIndex] == ncp) {
						endIndex++;
						if (endIndex == commentEnd.length) {
							return buffer.toString();
						} else {
							continue;
						}
					} else if (endIndex != 0) {
						// Verify whether current char is still commentEnd[i]
						endIndex = commentEndIndex(endIndex, ncp);
						if (endIndex == 0) {
							buffer.append(Character.toChars(ncp));
						}
						continue;
					}
					buffer.append(Character.toChars(ncp));
				}
				if (!closingEndsWith(10)) {
					error(rootIndex, ERR_UNEXPECTED_END_COMMENTED);
				}
				return buffer.toString();
			}

			private int commentEndIndex(int endIndex, int ncp) {
				// endIndex is guaranteed to be at least 1 here
				int endIndexm1 = endIndex - 1;
				if (commentEnd[endIndexm1] != ncp || !repeatedEndCp(endIndexm1)) {
					for (int j = 0; j < endIndex; j++) {
						buffer.append(Character.toChars(commentEnd[j]));
					}
					endIndex = 0;
				} else {
					buffer.append(Character.toChars(commentEnd[0]));
				}
				return endIndex;
			}

			private boolean repeatedEndCp(int endIndexm1) {
				for (int i = endIndexm1; i > 0; i--) {
					if (commentEnd[i] != commentEnd[i - 1]) {
						return false;
					}
				}
				return true;
			}

		}
	}

	private class ReaderMultiCommentParser extends AbstractReaderParser {

		ReaderMultiCommentParser(Reader reader, String[] start, String[] end) {
			this(reader, start, end, 256);
		}

		ReaderMultiCommentParser(Reader reader, String[] start, String[] end, int bufferCapacity) {
			super(reader, bufferCapacity);
			this.commentManager = new ReaderMultiCommentManager(start, end);
		}

		private class ReaderMultiCommentManager extends MultiCommentManager {

			ReaderMultiCommentManager(String[] start, String[] end) {
				super(start, end);
			}

			@Override
			void parse() throws IOException {
				super.parse();
				int unprocessedIdx = -1;
				int[] unprocessed = new int[getMaxOpeningLength()];
				rootIndex = 0;
				int cp = 0;
				while ((cp = reader.read()) != -1) {
					if (unprocessedIdx == -2) {
						unprocessedIdx = -1;
						if (cp == 10) {
							// Ignore 10 after 13
							continue;
						}
					}
					int ret = processCodePoint(cp, false);
					if (ret == 0) {
						unprocessedIdx++;
						unprocessed[unprocessedIdx] = cp;
					} else if (ret == 2) {
						if (closingEndsWithLF()) {
							// If last delimiter was LF, flag it
							unprocessedIdx = -2;
						} else {
							unprocessedIdx = -1;
						}
					} else if (ret == -1) {
						for (int j = 0; j <= unprocessedIdx; j++) {
							int oldcp = unprocessed[j];
							processCodePoint(oldcp, true);
							if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
								buffer.append(Character.toChars(oldcp));
							}
						}
						processCodePoint(cp, true);
						unprocessedIdx = -1;
						if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
							buffer.append(Character.toChars(cp));
						}
					} else if (ret == -2) {
						for (int j = 0; j <= unprocessedIdx; j++) {
							int oldcp = unprocessed[j];
							processCodePoint(oldcp, true);
							if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
								buffer.append(Character.toChars(oldcp));
							}
						}
						unprocessedIdx = -1;
						if (processCodePoint(cp, false) != -2) {
							unprocessedIdx = 0;
							unprocessed[0] = cp;
						}
					} else if (prevtype == Character.LOWERCASE_LETTER || prevtype == Character.UPPERCASE_LETTER) {
						buffer.append(Character.toChars(cp));
					}
					rootIndex += Character.charCount(cp);
				}
				int buflen = currentSequence().length();
				if (buflen != 0) {
					if (buflen != 1 || !isPreviousCpWCharacter()) {
						handler.word(previdx, currentSequence());
					} else {
						// Isolated CharacterCheck-characters are handled as plain characters
						handler.character(previdx, currentSequence().charAt(0));
					}
				}
				if (unprocessedIdx != -1) {
					previdx = rootIndex - unprocessedIdx - 1;
					for (int i = 0; i <= unprocessedIdx; i++) {
						handler.character(previdx + i, unprocessed[i]);
					}
				}
				handler.endOfStream(rootIndex);
			}

			@Override
			protected String commentedSequence() throws IOException {
				boolean lastCp13 = false;
				int endIndex = 0;
				int ncp;
				while ((ncp = nextCodepoint()) != -1) {
					if (ncp == 10 || ncp == 12 || ncp == 13) {
						if (ncp != 10 || !lastCp13) {
							handler.control(rootIndex - 1, ncp);
						}
						lastCp13 = ncp == 13;
					} else {
						lastCp13 = false;
					}
					if (closingCodepointAt(endIndex) == ncp) {
						endIndex++;
						if (endIndex == closingLength()) {
							return buffer.toString();
						} else {
							continue;
						}
					} else if (endIndex != 0) {
						// Verify whether current char is still closingCodepointAt(i - 1)
						endIndex = commentEndIndex(endIndex, ncp);
						if (endIndex == 0) {
							buffer.append(Character.toChars(ncp));
						}
						continue;
					}
					buffer.append(Character.toChars(ncp));
				}
				if (!closingEndsWith(10)) {
					error(rootIndex, ERR_UNEXPECTED_END_COMMENTED);
				}
				return buffer.toString();
			}

			private int commentEndIndex(int endIndex, int ncp) {
				// endIndex is guaranteed to be at least 1 here
				int endIndexm1 = endIndex - 1;
				if (closingCodepointAt(endIndexm1) != ncp || !repeatedEndCp(endIndexm1)) {
					for (int j = 0; j < endIndex; j++) {
						buffer.append(Character.toChars(closingCodepointAt(j)));
					}
					endIndex = 0;
				} else {
					buffer.append(Character.toChars(closingCodepointAt(0)));
				}
				return endIndex;
			}

			private boolean repeatedEndCp(int endIndexm1) {
				for (int i = endIndexm1; i > 0; i--) {
					if (closingCodepointAt(i) != closingCodepointAt(i - 1)) {
						return false;
					}
				}
				return true;
			}
		}
	}

	/**
	 * Check whether a character codepoint would be a valid addition to a word.
	 */
	public interface CharacterCheck {

		/**
		 * Is the given character codepoint a valid addition to the word being formed by the
		 * <code>SequenceParser</code>.
		 * 
		 * @param codePoint
		 *            the character codepoint to test.
		 * @param parser
		 *            the object representing the current state of the parser.
		 * @return <code>true</code> if it can be safely added to the word being currently formed, false
		 *         otherwise.
		 */
		boolean isAllowedCharacter(int codePoint, SequenceParser parser);
	}

	class DisallowCharacterCheck implements CharacterCheck {

		@Override
		public boolean isAllowedCharacter(int codePoint, SequenceParser parser) {
			return false;
		}

	}

	class WhitelistCharacterCheck implements CharacterCheck {

		private final int[] allowInWords;

		WhitelistCharacterCheck(int[] allowInWords) {
			super();
			this.allowInWords = allowInWords;
		}

		@Override
		public boolean isAllowedCharacter(int codePoint, SequenceParser parser) {
			for (int i = 0; i < allowInWords.length; i++) {
				if (codePoint == allowInWords[i]) {
					return true;
				}
			}
			return false;
		}

	}

}
