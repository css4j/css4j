/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.StringTokenizer;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionFactory;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Locator;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorFactory;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

import io.sf.carte.doc.DOMNullCharacterException;
import io.sf.carte.doc.agent.AgentUtil;
import io.sf.carte.doc.style.css.ExtendedCSSPrimitiveValue;
import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.nsac.AttributeCondition2;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.Condition2;
import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.nsac.Selector2;
import io.sf.carte.doc.style.css.om.SupportsConditionFactory;
import io.sf.carte.doc.style.css.parser.BooleanCondition.Type;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.AttributeConditionImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.CombinatorConditionImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.ConditionalSelectorImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.DescendantSelectorImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.ElementSelectorImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.LangConditionImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.PositionalConditionImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.SelectorArgumentConditionImpl;
import io.sf.carte.doc.style.css.parser.NSACSelectorFactory.SiblingSelectorImpl;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.uparser.TokenControl;
import io.sf.carte.uparser.TokenHandler;
import io.sf.carte.uparser.TokenProducer;

/**
 * CSS parser implementing the NSAC API.
 * <p>
 * Additionally to NSAC, it adds a few methods to parse media queries,
 * <code>{@literal @}supports</code> conditions and the bodies of other rules
 * (you probably want to use them through the Object Model instead of calling
 * them directly).
 */
public class CSSParser implements Parser2 {

	private DocumentHandler handler = null;
	private ErrorHandler errorHandler = null;
	private final EnumSet<Flag> parserFlags = EnumSet.noneOf(Flag.class);

	private int streamSizeLimit = 0x6000000;

	public CSSParser() {
		super();
	}

	@Override
	public void setLocale(Locale locale) throws CSSException {
		if (!Locale.ROOT.equals(locale)) {
			throw new CSSException("Locale not supported");
		}
	}

	@Override
	public void setDocumentHandler(DocumentHandler handler) {
		this.handler = handler;
	}

	@Override
	public void setSelectorFactory(SelectorFactory selectorFactory) {
		throw new IllegalArgumentException();
	}

	@Override
	public void setConditionFactory(ConditionFactory conditionFactory) {
		throw new IllegalArgumentException();
	}

	@Override
	public void setErrorHandler(ErrorHandler handler) {
		this.errorHandler = handler;
	}

	/**
	 * Set a parser flag.
	 * <p>
	 * Currently only <code>STARHACK</code> is supported, and it only applies to the parsing
	 * of full sheets.
	 * 
	 * @param flag
	 *            the flag.
	 */
	@Override
	public void setFlag(Flag flag) {
		parserFlags.add(flag);
	}

	/**
	 * Unset a parser flag.
	 * 
	 * @param flag
	 *            the flag.
	 */
	@Override
	public void unsetFlag(Flag flag) {
		parserFlags.remove(flag);
	}

	/**
	 * Set a new limit for the stream size that can be processed.
	 * <p>
	 * Calling this method does not affect the parsing that was already ongoing.
	 * </p>
	 * 
	 * @param streamSizeLimit the new limit to be enforced by new processing by this
	 *                        parser.
	 * @throws IllegalArgumentException if a limit below 64K was used.
	 */
	public void setStreamSizeLimit(int streamSizeLimit) {
		if (streamSizeLimit < 65536) {
			throw new IllegalArgumentException("Limit too low.");
		}
		this.streamSizeLimit = streamSizeLimit;
	}

	@Override
	public void parseStyleSheet(InputSource source) throws CSSException, IOException {
		final int[] allowInWords = { 45, 95 }; // -_
		final String[] opening = {"/*", "<!--"};
		final String[] closing = {"*/", "-->"};
		Reader re = source.getCharacterStream();
		if (re == null) {
			InputStream is = source.getByteStream();
			if (is == null) {
				String uri = source.getURI();
				if (uri == null) {
					throw new IllegalArgumentException("Null character stream");
				}
				parseStyleSheet(uri);
				return;
			}
			String charset = source.getEncoding();
			if ( charset == null) {
				charset = "UTF-8";
			}
			re = new InputStreamReader(is, charset);
		}
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}
		SheetTokenHandler handler = new SheetTokenHandler(source, null);
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		tp.setAcceptEofEndingQuoted(true);
		this.handler.startDocument(source);
		tp.parseMultiComment(re, opening, closing);
	}

	@Override
	public void parseStyleSheet(String uri) throws CSSException, IOException {
		URL url = new URL(uri);
		URLConnection ucon = url.openConnection();
		ucon.connect();
		InputStream is = ucon.getInputStream();
		is = new BufferedInputStream(is);
		String contentEncoding = ucon.getContentEncoding();
		String conType = ucon.getContentType();
		Reader re = AgentUtil.inputStreamToReader(is, conType, contentEncoding, "utf-8");
		InputSource source = new InputSource(re);
		source.setURI(uri);
		SheetTokenHandler handler = new SheetTokenHandler(source, null);
		int[] allowInWords = { 45, 95 }; // -_
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		tp.setAcceptEofEndingQuoted(true);
		this.handler.startDocument(source);
		try {
			tp.parse(re, "/*", "*/"); // We do not look for CDO-CDC comments here
		} catch (IOException e) {
			try {
				re.close();
			} catch (IOException e1) {
				// Ignore e1
			}
			throw e;
		}
		re.close();
	}

	@Override
	public void parseStyleDeclaration(InputSource source) throws CSSException, IOException {
		final int[] allowInWords = { 45, 95 }; // -_
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}
		DeclarationTokenHandler handler = new DeclarationTokenHandler(source, ShorthandDatabase.getInstance());
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		tp.parse(getReaderFromSource(source), "/*", "*/");
	}

	private Reader getReaderFromSource(InputSource source) throws IOException {
		Reader re = source.getCharacterStream();
		if (re == null) {
			InputStream is = source.getByteStream();
			if (is != null) {
				String charset = source.getEncoding();
				if ( charset == null) {
					charset = "utf-8";
				}
				re = new InputStreamReader(is, charset);
			} else {
				String uri = source.getURI();
				if (uri != null) {
					URL url = new URL(uri);
					URLConnection con = url.openConnection();
					con.connect();
					is = con.getInputStream();
					is = new BufferedInputStream(is);
					String contentEncoding = con.getContentEncoding();
					String conType = con.getContentType();
					String charset = source.getEncoding();
					if (charset == null) {
						charset = "utf-8";
					}
					re = AgentUtil.inputStreamToReader(is, conType, contentEncoding, charset);
				}
			}
		}
		return re;
	}

	public void parseDeclarationRule(InputSource source) throws CSSException, IOException {
		final int[] allowInWords = { 45, 95 }; // -_
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}
		DeclarationTokenHandler handler = new DeclarationRuleTokenHandler(source, ShorthandDatabase.getInstance());
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		Reader re = getReaderFromSource(source);
		tp.parse(re, "/*", "*/");
	}

	@Override
	public void parseRule(InputSource source) throws CSSException, IOException {
		final int[] allowInWords = { 45, 95 }; // -_
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}
		RuleTokenHandler handler = new RuleTokenHandler(source, null);
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		Reader re = getReaderFromSource(source);
		if (re == null) {
			throw new IllegalArgumentException("Null character stream");
		}
		tp.parse(re, "/*", "*/");
	}

	@Override
	public void parseRule(InputSource source, NamespaceMap nsmap) throws CSSException, IOException {
		final int[] allowInWords = { 45, 95 }; // -_
		if (this.handler == null) {
			throw new IllegalStateException("No document handler was set.");
		}
		RuleTokenHandler handler = new RuleTokenHandler(source, nsmap);
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		Reader re = getReaderFromSource(source);
		if (re == null) {
			throw new IllegalArgumentException("Null character stream");
		}
		tp.parse(re, "/*", "*/");
	}

	public void parseKeyFramesBody(String blockList) throws CSSParseException {
		int[] allowInWords = { 45, 95 }; // -_
		KeyFrameBlockListTH handler = new KeyFrameBlockListTH(0, blockList.length());
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(blockList, "/*", "*/");
	}

	public void parseFontFeatureValuesBody(String blockList) {
		int[] allowInWords = { 45, 95 }; // -_
		FontFeatureValuesTH handler = new FontFeatureValuesTH(0, blockList.length());
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(blockList, "/*", "*/");
	}

	/**
	 * Parse the condition text of a <code>{@literal @}supports</code> rule.
	 * 
	 * @param conditionText
	 *            the condition text.
	 * @param rule
	 *            the rule that would process the error. if <code>null</code>, a problem while
	 *            parsing shall result in an exception. Note that
	 *            <code>SAC_NOT_SUPPORTED_ERR</code> exceptions are always thrown instead of
	 *            being processed by the rule.
	 * @return the <code>{@literal @}supports</code> condition, or <code>null</code> if a rule
	 *         was specified to handle the errors, and an error was produced.
	 * @throws CSSException
	 *             if there is a syntax problem and there is no error handler, or
	 *             <code>CSSException.SAC_NOT_SUPPORTED_ERR</code> if a hard-coded limit in
	 *             nested expressions was reached.
	 */
	public BooleanCondition parseSupportsCondition(String conditionText, ExtendedCSSRule rule)
			throws CSSException {
		int[] allowInWords = { 45, 46 }; // -.
		SupportsTokenHandler handler = new SupportsTokenHandler(rule);
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		try {
			tp.parse(conditionText, "/*", "*/");
		} catch (IndexOutOfBoundsException e) {
			CSSException ex = new CSSException(CSSException.SAC_NOT_SUPPORTED_ERR, "Nested conditions exceeded limit",
					e);
			ex.initCause(e);
			throw ex;
		}
		if (handler.errorCode == 0) {
			return handler.getCondition();
		} else {
			return null;
		}
	}

	/**
	 * Parse a media query.
	 * 
	 * @param media
	 *            the media query text.
	 * @param condFactory
	 *            the condition factory.
	 * @param mqhandler
	 *            the media query handler.
	 * @throws CSSException <code>CSSException.SAC_NOT_SUPPORTED_ERR</code> if a
	 *                      hard-coded limit in nested expressions was reached.
	 */
	public void parseMediaQuery(String media, MediaConditionFactory condFactory, MediaQueryHandler mqhandler)
			throws CSSException {
		int[] allowInWords = { 45, 46 }; // -.
		ConditionTokenHandler handler = new MediaQueryTokenHandler(condFactory, mqhandler);
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		mqhandler.startQuery();
		try {
			tp.parse(media, "/*", "*/");
		} catch (IndexOutOfBoundsException e) {
			CSSException ex = new CSSException(CSSException.SAC_NOT_SUPPORTED_ERR, "Nested queries exceed limit", e);
			ex.initCause(e);
			throw ex;
		}
	}

	abstract private static class DelegateHandler implements TokenHandler {

		public void preBooleanHandling(int index, Type type) {
		}

		@Override
		public void tokenControl(TokenControl control) {
			// Not called
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			// Not called
		}

		@Override
		public void quotedNewlineChar(int index, int codePoint) {
			// Not called
		}

		@Override
		public void control(int index, int codePoint) {
			// Not called
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			// Not called
		}

		@Override
		public void error(int index, byte errCode, CharSequence context) {
			// Not called
		}

	}

	private class ConditionTokenHandler extends CSSTokenHandler {

		final BooleanConditionFactory conditionFactory;

		/**
		 * The condition that we are currently working at in this handler.
		 */
		BooleanCondition currentCond = null;

		/**
		 * Index of nested operation's depth.
		 */
		private int opDepthIndex = 0;

		/**
		 * Number of unclosed left parentheses at each operation level (up to <code>opDepthIndex</code>).
		 */
		private final short[] opParenDepth = new short[32]; // Limited to 32 nested expressions

		private DelegateHandler predicateHandler;

		/**
		 * Are we reading a predicate instead of processing operation syntax ?
		 */
		boolean readingPredicate = false;

		ConditionTokenHandler(BooleanConditionFactory conditionFactory) {
			super(null);
			this.conditionFactory = conditionFactory;
			buffer = new StringBuilder(64);
		}

		DelegateHandler getPredicateHandler() {
			return predicateHandler;
		}

		void setPredicateHandler(DelegateHandler predicateHandler) {
			this.predicateHandler = predicateHandler;
		}

		@Override
		public void word(int index, CharSequence word) {
			if (!parseError) {
				if (!readingPredicate) {
					if (buffer.length() == 0) {
						processWord(index, word.toString());
					} else {
						unexpectedTokenError(index, word);
					}
				} else if (getCurrentParenDepth() > 1) {
					predicateHandler.word(index, word);
				} else {
					processWord(index, word.toString());
				}
				prevcp = 65;
			}
		}

		private void processWord(int index, String word) {
			String lctoken = word.toLowerCase(Locale.ROOT);
			if ("not".equals(lctoken)) {
				predicateHandler.preBooleanHandling(index, BooleanCondition.Type.NOT);
				BooleanCondition newCond = conditionFactory.createNotCondition();
				if (currentCond != null) {
					currentCond.addCondition(newCond);
				}
				setNestedCondition(newCond);
			} else if ("and".equals(lctoken)) {
				predicateHandler.preBooleanHandling(index, BooleanCondition.Type.AND);
				if (currentCond != null) {
					processOperation(index, BooleanCondition.Type.AND, word);
				} else {
					processImplicitAnd(index);
				}
			} else if ("or".equals(lctoken)) {
				if (currentCond != null) {
					predicateHandler.preBooleanHandling(index, BooleanCondition.Type.OR);
					try {
						processOperation(index, BooleanCondition.Type.OR, word);
					} catch (DOMException e) {
						unexpectedTokenError(index, word);
					}
				} else {
					unexpectedTokenError(index, word);
				}
			} else {
				readingPredicate = true;
				predicateHandler.word(index, word);
			}
		}

		void processOperation(int index, BooleanCondition.Type opType, String opname) {
			BooleanCondition operation = currentCond.getParentCondition();
			BooleanCondition.Type curType = currentCond.getType();
			if (curType == BooleanCondition.Type.PREDICATE) {
				if (operation == null) {
					BooleanCondition newCond = createOperation(opType);
					newCond.addCondition(currentCond);
					setNestedCondition(newCond);
				} else if (operation.getType() == opType) {
					currentCond = operation;
				} else {
					BooleanCondition newCond = createOperation(opType);
					if (opParenDepth[opDepthIndex] != 0) {
						BooleanCondition oldCond = operation.replaceLast(newCond);
						newCond.addCondition(oldCond);
					} else {
						newCond.addCondition(operation);
					}
					setNestedCondition(newCond);
				}
			} else if (curType == BooleanCondition.Type.NOT) {
				if (operation != null) {
					BooleanCondition newCond = createOperation(opType);
					BooleanCondition oldCond = operation.replaceLast(newCond);
					newCond.addCondition(oldCond);
					setNestedCondition(newCond);
				} else {
					unexpectedTokenError(index, opname);
				}
			} else if (curType != opType) {
				unexpectedTokenError(index, opname);
			}
		}

		BooleanCondition createOperation(BooleanCondition.Type opType) {
			if (opType == BooleanCondition.Type.AND) {
				return conditionFactory.createAndCondition();
			}
			return conditionFactory.createOrCondition();
		}

		private void setNestedCondition(BooleanCondition newCond) {
			currentCond = newCond;
			opDepthIndex++;
		}

		void processImplicitAnd(int index) {
			unexpectedTokenError(index, "and");
		}

		@Override
		public void openGroup(int index, int codepoint) {
			if (codepoint == 40) {
				opParenDepth[opDepthIndex]++;
			}
			predicateHandler.openGroup(index, codepoint);
			readingPredicate = true;
			prevcp = codepoint;
		}

		@Override
		public void closeGroup(int index, int codepoint) {
			if (codepoint == 41) {
				opParenDepth[opDepthIndex]--;
				if (opParenDepth[opDepthIndex] < 0) {
					unexpectedCharError(index, codepoint);
				} else if (buffer.length() != 0) {
					if (readingPredicate) {
						predicateHandler.closeGroup(index, codepoint);
					} else {
						unexpectedCharError(index, codepoint);
					}
				}
				if (opParenDepth[opDepthIndex] == 0 && currentCond != null) {
					if (opDepthIndex != 0) {
						opDepthIndex--;
						if (currentCond.getParentCondition() != null) {
							currentCond = currentCond.getParentCondition();
						}
					}
				}
				prevcp = codepoint;
			} else if (readingPredicate) {
				predicateHandler.closeGroup(index, codepoint);
				prevcp = codepoint;
			} else {
				unexpectedCharError(index, codepoint);
			}
		}

		short getCurrentParenDepth() {
			return opParenDepth[opDepthIndex];
		}

		@Override
		public void character(int index, int codepoint) {
			if (!parseError) {
				if (!readingPredicate) {
					if (codepoint == 44) { // ','
						predicateHandler.character(index, codepoint);
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else {
					predicateHandler.character(index, codepoint);
				}
			} else if (codepoint == 44) { // ',' may clear error
				predicateHandler.character(index, codepoint);
			}
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteCp) {
			if (!parseError) {
				if (readingPredicate) {
					predicateHandler.quoted(index, quoted, quoteCp);
					prevcp = 65;
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected: '" + quoted + '\'');
				}
			}
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			quoted(index, quoted, quoteCp);
		}

		@Override
		public void escaped(int index, int codepoint) {
			if (!parseError) {
				if (readingPredicate) {
					predicateHandler.escaped(index, codepoint);
				} else if (prevcp == TokenProducer.CHAR_LEFT_PAREN) {
					readingPredicate = true;
					predicateHandler.escaped(index, codepoint);
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected escaped character: \\u" + Integer.toHexString(codepoint));
				}
				prevcp = codepoint;
			}
		}

		@Override
		public void separator(int index, int codepoint) {
			if (!parseError) {
				if (readingPredicate) {
					predicateHandler.separator(index, codepoint);
				}
				prevcp = 32;
			}
		}

		@Override
		public void control(int index, int codepoint) {
			super.control(index, codepoint);
			if (escapedTokenIndex != -1 && CSSParser.bufferEndsWithEscapedCharOrWS(buffer)) {
				escapedTokenIndex = -1;
				buffer.append(' '); // break the escape
			}
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			separator(index, 12);
			prevcp = 12;
		}

		@Override
		public void endOfStream(int len) {
			if (opParenDepth[opDepthIndex] != 0) {
				handleError(len, ParseHelper.ERR_UNMATCHED_PARENTHESIS, "Unmatched parenthesis");
			} else if (!parseError) {
				predicateHandler.endOfStream(len);
			}
		}

		@Override
		protected void handleError(int index, byte errCode, String message) {
			throw createException(index, errCode, message);
		}

	}

	private class SupportsTokenHandler extends ConditionTokenHandler {

		/*
		 * Error-related fields.
		 */
		private byte errorCode = 0;
		private CSSParseException errorException = null;
		private final ExtendedCSSRule rule;

		SupportsTokenHandler(ExtendedCSSRule rule) {
			super(new SupportsConditionFactory());
			this.rule = rule;
			setPredicateHandler(new SupportsDelegateHandler());
		}

		BooleanCondition getCondition() {
			BooleanCondition condition = currentCond;
			if (condition != null) {
				while (condition.getParentCondition() != null) {
					condition = condition.getParentCondition();
				}
			}
			return condition;
		}

		@Override
		protected void handleError(int index, byte errCode, String message) {
			if (!parseError) {
				if (errorCode == 0) {
					errorCode = errCode;
					errorException = createException(index, errCode, message);
					if (rule != null) {
						rule.getParentStyleSheet().getErrorHandler().ruleParseError(rule, errorException);
					} else {
						throw errorException;
					}
				}
				parseError = true;
			}
		}

		private class SupportsDelegateHandler extends DelegateHandler {

			/**
			 * Are we reading a value instead of processing a property name ?
			 */
			private boolean readingValue = false;

			/**
			 * Number of unclosed left parentheses while reading a value.
			 */
			private short valueParendepth = 0;

			SupportsDelegateHandler() {
				super();
			}

			@Override
			public void word(int index, CharSequence word) {
				if (buffer.length() != 0) {
					if (!readingValue) {
						unexpectedTokenError(index, word);
						return;
					} else if (isPrevCpWhitespace()) {
						buffer.append(' ');
					}
				}
				buffer.append(word);
			}

			@Override
			public void openGroup(int index, int codepoint) {
				if (readingValue) {
					bufferAppend(codepoint);
				} else if (codepoint != 40) {
					unexpectedCharError(index, codepoint);
				} else if (buffer.length() != 0) {
					unexpectedCharError(index, codepoint);
				} else {
					prevcp = codepoint;
				}
			}

			@Override
			public void closeGroup(int index, int codepoint) {
				if (codepoint == 41) {
					if (readingValue) {
						if (valueParendepth == getCurrentParenDepth()) {
							String svalue = buffer.toString();
							ValueFactory factory = new ValueFactory();
							try {
								StyleValue value = factory.parseProperty(svalue);
								((DeclarationCondition) currentCond).setValue(value);
							} catch (DOMException e) {
								handleError(index, ParseHelper.ERR_WRONG_VALUE, "Bad @supports condition value.");
								errorException.initCause(e);
								((DeclarationCondition) currentCond).setValue(svalue);
							}
							buffer.setLength(0);
							readingValue = false;
							readingPredicate = false;
							escapedTokenIndex = -1;
						} else {
							bufferAppend(codepoint);
							return;
						}
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else if (readingValue) {
					bufferAppend(codepoint);
				} else {
					unexpectedCharError(index, codepoint);
				}
			}

			@Override
			public void character(int index, int codepoint) {
				// ! 33
				// : 58
				// ; 59
				if (!readingValue) {
					if (codepoint == 58) {
						BooleanCondition newCond = conditionFactory.createPredicate(buffer.toString());
						if (currentCond != null) {
							currentCond.addCondition(newCond);
						}
						currentCond = newCond;
						buffer.setLength(0);
						valueParendepth = getCurrentParenDepth();
						valueParendepth--;
						readingValue = true;
						escapedTokenIndex = -1;
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else {
					if (codepoint == 59) {
						unexpectedCharError(index, codepoint);
					} else {
						bufferAppend(codepoint);
					}
				}
				prevcp = codepoint;
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
				if (readingValue) {
					if (buffer.length() != 0) {
						buffer.append(' ');
					}
					char c = (char) quoteCp;
					buffer.append(c).append(quoted).append(c);
					prevcp = 65;
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected: '" + quoted + '\'');
				}
			}

			@Override
			public void escaped(int index, int codepoint) {
				if (ParseHelper.isHexCodePoint(codepoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				}
				bufferAppend(codepoint);
			}

			@Override
			public void separator(int index, int cp) {
				if (escapedTokenIndex != -1 && bufferEndsWithEscapedCharOrWS(buffer)) {
					buffer.append(' ');
					return;
				}
			}

			@Override
			public void endOfStream(int len) {
				if (readingPredicate) {
					handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of file");
				} else if (buffer.length() != 0) {
					handleError(len, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected token: " + buffer);
				} else if (currentCond == null) {
					handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "No condition found");
				}
			}

		}

	}

	private class MediaQueryTokenHandler extends ConditionTokenHandler {

		MediaQueryTokenHandler(MediaConditionFactory conditionFactory, MediaQueryHandler mqhandler) {
			super(conditionFactory);
			setPredicateHandler(new MediaQueryDelegateHandler(mqhandler));
		}

		@Override
		MediaQueryDelegateHandler getPredicateHandler() {
			return (MediaQueryDelegateHandler) super.getPredicateHandler();
		}

		@Override
		void processImplicitAnd(int index) {
			MediaQueryDelegateHandler mqhelper = getPredicateHandler();
			String medium = mqhelper.mediaType;
			if (medium == null) {
				if (buffer.length() != 0) {
					mqhelper.processMediaType(index);
				} else {
					unexpectedTokenError(index, "and");
					return;
				}
			}
			currentCond = ((MediaConditionFactory) conditionFactory).createMediaTypePredicate(medium);
			processOperation(index, BooleanCondition.Type.AND, "and");
		}

		@Override
		BooleanCondition createOperation(BooleanCondition.Type opType) {
			if (opType == BooleanCondition.Type.AND) {
				return conditionFactory.createAndCondition();
			}
			if (getPredicateHandler().mediaType == null) {
				return conditionFactory.createOrCondition();
			}
			throw new DOMException(DOMException.SYNTAX_ERR, "Unexpected 'OR'");
		}

		@Override
		protected void handleError(int index, byte errCode, String message) {
			if (!parseError) {
				MediaQueryDelegateHandler mqhelper = getPredicateHandler();
				CSSParseException ex = createException(index, errCode, message);
				mqhelper.handler.invalidQuery(ex);
				parseError = true;
			}
		}

		@Override
		void handleWarning(int index, byte errCode, String message) {
			if (!parseError) {
				MediaQueryDelegateHandler mqhelper = getPredicateHandler();
				CSSParseException ex = createException(index, errCode, message);
				mqhelper.handler.compatQuery(ex);
				super.handleWarning(index, errCode, message);
			}
		}

		class MediaQueryDelegateHandler extends DelegateHandler {

			private final MediaQueryHandler handler;
			private byte stage = 0;
			private boolean negativeQuery = false;
			private boolean spaceFound = false;
			private String mediaType = null;
			private String featureName = null;
			private String firstValue = null;
			private byte rangeType = 0; // Type of range expression, 0 if none
			private boolean functionToken = false;

			private static final int WORD_UNQUOTED = 0;

			private MediaQueryDelegateHandler(MediaQueryHandler handler) {
				super();
				this.handler = handler;
			}

			@Override
			public void word(int index, CharSequence word) {
				// @formatter:off
				//
				// Stages:
				// not medium and ( feature : value )
				//        0  | 1 |2|   3     |  4    |1
				// not medium and ( value1  <= feature < value2 )
				//        0  | 1 |2|   3     |5|  6     |7       |1
				// 127 = error
				//
				// @formatter:on
				if (stage == 127) {
					return;
				}
				if (functionToken) {
					if (buffer.length() != 0 && isPrevCpWhitespace()) {
						buffer.append(' ');
					}
					buffer.append(word);
				} else if (ParseHelper.equalsIgnoreCase(word, "not")) {
					if (stage != 0) {
						handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Found 'not' at the wrong parsing stage");
					} else {
						negativeQuery = true;
					}
				} else if (ParseHelper.equalsIgnoreCase(word, "only")) {
					if (stage != 0) {
						handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Found 'only' at the wrong parsing stage");
					} else {
						handler.onlyPrefix();
					}
				} else if (ParseHelper.equalsIgnoreCase(word, "or")) {
					handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Found 'or'");
				} else { // rest of cases are collected to buffer
					if (!appendWord(index, word, WORD_UNQUOTED)) {
						return;
					}
				}
				prevcp = 65; // A
			}

			private boolean appendWord(int index, CharSequence word, int quote) {
				if (buffer.length() != 0) {
					if (escapedTokenIndex == -1) {
						if (isPrevCpWhitespace()) {
							if (stage == 1) {
								handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Found white space between media");
								return false;
							}
							spaceFound = true;
							buffer.append(' ');
						}
					}
				}
				if (quote == WORD_UNQUOTED) {
					buffer.append(word);
				} else {
					char c = (char) quote;
					buffer.append(c).append(word).append(c);
				}
				if (!functionToken) {
					if (stage == 0) {
						stage = 1;
					} else if (stage == 5) { // after "value [<][=]"
						stage = 6;
					}
				}
				return true;
			}

			@Override
			public void preBooleanHandling(int index, Type type) {
				switch (type) {
				case AND:
					if (stage > 1) {
						handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Found 'and' at the wrong parsing stage");
						return;
					}
					if (buffer.length() != 0) {
						processMediaType(index);
					}
				case OR:
					stage = 2;
					break;
				default: // NOT
				}
			}

			/**
			 * Process a media type from buffer.
			 * <p>
			 * stage 0 or 1 is assumed, as well as a non-empty buffer.
			 * 
			 * @param index the index.
			 */
			private void processMediaType(int index) {
				if (mediaType == null && getCurrentParenDepth() == 0) {
					mediaType = rawBuffer();
					if (currentCond != null && isEmptyNotCondition()) {
						currentCond = null;
						negativeQuery = true;
						handler.negativeQuery();
					}
					handler.mediaType(mediaType);
				}
			}

			/**
			 * Checks whether the current condition is a stand-alone, empty <code>NOT</code>
			 * condition. Assumes currentCond != null
			 * 
			 * @return <code>true</code> if the current condition is a stand-alone, empty
			 *         <code>NOT</code> condition.
			 */
			private boolean isEmptyNotCondition() {
				return currentCond.getType() == Type.NOT
						&& currentCond.getParentCondition() == null && currentCond.getNestedCondition() == null;
			}

			@Override
			public void openGroup(int index, int codepoint) {
				if (codepoint == 40) { // '('
					if (!isPrevCpWhitespace()) {
						// Function token
						functionToken = true;
						buffer.append('(');
					} else if (functionToken) {
						buffer.append('(');
					} else if (buffer.length() != 0) {
						unexpectedCharError(index, codepoint);
					} else {
						if (stage == 2 || stage == 0) {
							stage = 3;
						}
					}
					parendepth++;
				} else {
					unexpectedCharError(index, codepoint);
				}
				prevcp = codepoint;
			}

			@Override
			public void closeGroup(int index, int codepoint) {
				if (codepoint == 41) { // ')'
					parendepth--;
					if (functionToken) {
						buffer.append(')');
						functionToken = false;
					} else {
						if (stage == 6) {
							processBuffer(index);
							// Need to determine whether we have "value <|>|= feature"
							// or "feature <|>|= value"
							if (firstValue != null && isKnownFeature(firstValue)) {
								String tempstr = firstValue;
								firstValue = featureName;
								featureName = tempstr;
							} else if (!isKnownFeature(featureName)) {
								if (isValidFeatureSyntax(firstValue)) {
									String tempstr = firstValue;
									firstValue = featureName;
									featureName = tempstr;
								} else if (!isValidFeatureSyntax(featureName)) {
									handleError(index, ParseHelper.ERR_RULE_SYNTAX, 
											"Wrong feature expression near " + featureName + " " + firstValue + ")");
									prevcp = codepoint;
									return;
								} else {
									reverseRangetype();
								}
							} else {
								reverseRangetype();
							}
							ExtendedCSSPrimitiveValue value1 = parseMediaFeature(firstValue);
							if (value1 == null) {
								handleError(index, ParseHelper.ERR_WRONG_VALUE, firstValue);
							} else {
								handlePredicate(featureName, rangeType, value1);
								if (value1.getPrimitiveType() == CSSPrimitiveValue.CSS_UNKNOWN) {
									handleWarning(index, ParseHelper.WARN_IDENT_COMPAT,
											"Probable hack in media feature.");
								}
							}
						} else if (buffer.length() != 0) {
							if (stage == 4) {
								ExtendedCSSPrimitiveValue value = parseMediaFeature(buffer.toString());
								if (value == null) {
									handleError(index, ParseHelper.ERR_WRONG_VALUE, buffer.toString());
								} else {
									handlePredicate(featureName, (byte) 0, value);
									if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_UNKNOWN) {
										handleWarning(index, ParseHelper.WARN_IDENT_COMPAT,
												"Probable hack in media feature.");
									}
								}
							} else if (stage == 7) {
								ExtendedCSSPrimitiveValue value1 = parseMediaFeature(firstValue);
								ExtendedCSSPrimitiveValue value2 = parseMediaFeature(buffer.toString());
								if (value1 == null) {
									handleError(index, ParseHelper.ERR_WRONG_VALUE, firstValue);
								} else if (value2 == null) {
									handleError(index, ParseHelper.ERR_WRONG_VALUE, buffer.toString());
								} else {
									handlePredicate(featureName, rangeType, value1, value2);
									if (value1.getPrimitiveType() == CSSPrimitiveValue.CSS_UNKNOWN
											|| value2.getPrimitiveType() == CSSPrimitiveValue.CSS_UNKNOWN) {
										handleWarning(index, ParseHelper.WARN_IDENT_COMPAT,
												"Probable hack in media feature.");
									}
								}
							} else if (stage == 3 && !spaceFound) {
								handlePredicate(buffer.toString(), (byte) 0, null);
							} else {
								handleError(index, ParseHelper.ERR_EXPR_SYNTAX, buffer.toString());
							}
							buffer.setLength(0);
							spaceFound = false;
							escapedTokenIndex = -1;
						} else {
							unexpectedCharError(index, codepoint);
						}
						if (stage == 5) {
							unexpectedCharError(index, codepoint);
						} else {
							rangeType = 0;
							stage = 1;
						}
						readingPredicate = false;
					}
				} else {
					unexpectedCharError(index, codepoint);
				}
				prevcp = codepoint;
			}

			private void handlePredicate(String featureName, byte rangeType, ExtendedCSSPrimitiveValue value) {
				MediaFeaturePredicate predicate = (MediaFeaturePredicate) conditionFactory.createPredicate(featureName);
				predicate.setRangeType(rangeType);
				predicate.setValue(value);
				if (currentCond == null) {
					currentCond = predicate;
				} else {
					currentCond.addCondition(predicate);
				}
				clearPredicate();
			}

			private void handlePredicate(String featureName, byte rangeType, ExtendedCSSPrimitiveValue value1,
					ExtendedCSSPrimitiveValue value2) {
				MediaFeaturePredicate predicate = (MediaFeaturePredicate) conditionFactory.createPredicate(featureName);
				predicate.setRangeType(rangeType);
				predicate.setValue(value1);
				predicate.setRangeSecondValue(value2);
				if (currentCond == null) {
					currentCond = predicate;
				} else {
					currentCond.addCondition(predicate);
				}
				clearPredicate();
			}

			/**
			 * Reverse the current range type.
			 * <p>
			 * Range type is a way to numerically characterize a range like 'a <= foo < b'
			 */
			private void reverseRangetype() {
				if ((rangeType & 2) == 2) {
					rangeType ^= 2;
					rangeType = (byte) (rangeType | 4);
				} else if ((rangeType & 4) == 4) {
					rangeType ^= 4;
					rangeType = (byte) (rangeType | 2);
				}
			}

			@Override
			public void character(int index, int codepoint) {
				// ! 33
				// : 58
				// ; 59
				if (functionToken) {
					if (isPrevCpWhitespace()) {
						buffer.append(' ');
					}
					bufferAppend(codepoint);
				} else {
					if (codepoint == 58) { // ':'
						if (buffer.length() != 0) {
							featureName = rawBuffer();
							stage = 4;
						} else {
							handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Empty feature name");
						}
					} else if (codepoint == 44) { // ,
						if (!parseError) {
							if (parendepth != 0) {
								handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Unmatched parenthesis");
								return;
							} else if (stage == 0) {
								handleError(index, ParseHelper.ERR_RULE_SYNTAX, "No media found");
							}
							processBuffer(index);
							endQuery(index);
						} else if (parendepth == 0) {
							handler.endQuery();
							clearQuery();
						}
						handler.startQuery();
					} else if (codepoint == 46) { // .
						if (stage == 4 || stage == 3 || stage == 7 || functionToken) {
							buffer.append('.');
						} else {
							unexpectedCharError(index, '.');
						}
					} else if (codepoint == 47) { // /
						if (stage == 4 || stage == 3 || stage == 6 || stage == 7 || functionToken) {
							buffer.append('/');
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 59) {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, ";");
					} else if (codepoint == 60) { // <
						// rangeType:
						// = 1, < 2, > 4,
						// <= 3, >= 5
						// a <= foo < b ; 19
						// a >= foo > b ; 37
						if (stage < 3 || (rangeType > 3 && ((rangeType & 16) != 0 || (rangeType & 4) != 0))) {
							unexpectedCharError(index, codepoint);
						} else {
							if (stage != 6 && stage != 7) {
								rangeType = (byte) (rangeType | 2);
								stage = 5;
							} else {
								processBuffer(index);
								rangeType = (byte) (rangeType | 16);
								stage = 7;
							}
						}
					} else if (codepoint == 61) { // =
						if (stage < 3 || (rangeType > 5 && (rangeType & 8) != 0)) {
							unexpectedCharError(index, codepoint);
						} else {
							if (stage != 6 && stage != 7) {
								rangeType = (byte) (rangeType | 1);
								stage = 5;
							} else {
								processBuffer(index);
								rangeType = (byte) (rangeType | 8);
								stage = 7;
							}
						}
					} else if (codepoint == 62 || (rangeType >= 4 && ((rangeType & 32) != 0 || (rangeType & 2) != 0))) { // >
						if (stage < 3) {
							unexpectedCharError(index, codepoint);
						} else {
							if (stage != 6 && stage != 7) {
								rangeType = (byte) (rangeType | 4);
								stage = 5;
							} else {
								processBuffer(index);
								rangeType = (byte) (rangeType | 32);
								stage = 7;
							}
						}
					} else {
						unexpectedCharError(index, codepoint);
					}
					if (stage == 5 && firstValue == null && buffer.length() != 0) {
						firstValue = rawBuffer();
					}
				}
			}

			private void processBuffer(int index) {
				if (buffer.length() != 0) {
					if (stage == 1) {
						processMediaType(index);
						if (mediaType == null) {
							unexpectedTokenError(index, buffer.toString());
							buffer.setLength(0);
						}
						readingPredicate = false;
					} else if (stage == 6) {
						featureName = rawBuffer();
					}
				}
			}

			private void endQuery(int index) {
				if (currentCond != null) {
					while (currentCond.getParentCondition() != null) {
						currentCond = currentCond.getParentCondition();
					}
					handler.condition(currentCond);
				} else if (negativeQuery && mediaType == null) {
					handleError(index, ParseHelper.ERR_EXPR_SYNTAX, "Negative query without media.");
				}
				handler.endQuery();
				clearQuery();
			}

			private void clearQuery() {
				currentCond = null;
				mediaType = null;
				stage = 0;
				prevcp = 32;
				negativeQuery = false;
				parseError = false;
				clearPredicate();
			}

			private void clearPredicate() {
				featureName = null;
				firstValue = null;
				rangeType = 0;
				spaceFound = false;
			}

			String rawBuffer() {
				spaceFound = false;
				return MediaQueryTokenHandler.this.rawBuffer();
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected: '" + quoted + '\'');
			}

			@Override
			public void escaped(int index, int codepoint) {
				if (ParseHelper.isHexCodePoint(codepoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				}
				bufferAppend(codepoint);
				if (stage == 5) {
					stage = 6;
				} else if (stage == 0) {
					stage = 1;
				}
			}

			@Override
			public void separator(int index, int cp) {
				if (escapedTokenIndex != -1 && bufferEndsWithEscapedCharOrWS(buffer)) {
					buffer.append(' ');
					return;
				}
			}

			@Override
			public void endOfStream(int len) {
				if (stage == 1) {
					processBuffer(len);
				}
				if (currentCond == null && mediaType == null) {
					if (buffer.length() != 0) {
						processMediaType(len);
						if (mediaType == null) {
							unexpectedTokenError(len, buffer.toString());
							buffer.setLength(0);
						}
						handler.endQuery();
						clearQuery();
					} else {
						handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "No valid query found");
					}
				} else if (readingPredicate || stage > 1) {
					handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of file");
					handler.endQuery();
				} else if (buffer.length() != 0) {
					handleError(len, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected token: " + buffer);
					handler.endQuery();
				} else if (currentCond != null && isEmptyNotCondition()) {
					handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "No valid query found");
					handler.endQuery();
				} else {
					endQuery(len);
				}
			}

		}

	}

	private ExtendedCSSPrimitiveValue parseMediaFeature(String stringValue) {
		ExtendedCSSPrimitiveValue value;
		try {
			value = new ValueFactory().parseMediaFeature(stringValue, this);
		} catch (RuntimeException e) {
			value = null;
		}
		return value;
	}

	/**
	 * Determine whether this looks like a media feature (rather than a value).
	 * 
	 * @param string the presumed feature name,
	 * @return <code>true</code> if the string looks like a media feature.
	 */
	private static boolean isKnownFeature(String string) {
		return string.startsWith("min-") || string.startsWith("max-") || MediaQueryDatabase.isMediaFeature(string)
				|| string.startsWith("device-");
	}

	private static boolean isValidFeatureSyntax(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (!Character.isLetter(c) && c != '-') {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getParserVersion() {
		return "http://www.w3.org/TR/REC-CSS2";
	}

	@Override
	public SelectorList parseSelectors(InputSource source) throws CSSException, IOException {
		int[] allowInWords = { 45, 95 }; // -_
		Reader re = getReaderFromSource(source);
		if (re == null) {
			throw new IllegalArgumentException("Null character stream");
		}
		SelectorTokenHandler handler = new SelectorTokenHandler(source, null);
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		tp.parse(re, "/*", "*/");
		return handler.getSelectorList();
	}

	public SelectorList parseSelectors(String seltext) throws CSSException {
		int[] allowInWords = { 45, 95 }; // -_
		SelectorTokenHandler handler = new SelectorTokenHandler();
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(seltext, "/*", "*/");
		return handler.getSelectorList();
	}

	private SelectorList parseSelectors(String seltext, NSACSelectorFactory factory) throws CSSException {
		int[] allowInWords = { 45, 95 }; // -_
		SelectorTokenHandler handler = new SelectorTokenHandler(factory);
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(seltext);
		return handler.getSelectorList();
	}

	private SelectorList parseSelectorArgument(String seltext, NSACSelectorFactory factory) throws CSSException {
		int[] allowInWords = { 45, 95 }; // -_
		SelectorTokenHandler handler = new SelectorArgumentTokenHandler(factory);
		TokenProducer tp = new TokenProducer(handler, allowInWords);
		tp.parse(seltext);
		return handler.getSelectorList();
	}

	@Override
	public LexicalUnit2 parsePropertyValue(InputSource source) throws CSSException, IOException {
		int[] allowInWords = { 45, 95 }; // -_
		Reader re = getReaderFromSource(source);
		if (re == null) {
			throw new IllegalArgumentException("Null character stream");
		}
		PropertyTokenHandler handler = new PropertyTokenHandler(source);
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		tp.parse(re, "/*", "*/");
		return handler.getLexicalUnit();
	}

	public LexicalUnit2 parsePropertyValue(String propertyName, Reader reader) throws CSSException, IOException {
		int[] allowInWords = { 45, 95 }; // -_
		PropertyTokenHandler handler = new PropertyTokenHandler(propertyName);
		TokenProducer tp = new TokenProducer(handler, allowInWords, streamSizeLimit);
		tp.parse(reader, "/*", "*/");
		return handler.getLexicalUnit();
	}

	@Override
	public boolean parsePriority(InputSource source) throws CSSException, IOException {
		Reader re = getReaderFromSource(source);
		if (re == null) {
			throw new IllegalArgumentException("Null character stream");
		}
		int cp = re.read();
		if (cp != -1) {
			short count = 0;
			StringBuilder buf = new StringBuilder(9);
			byte parsingWord = 0;
			if (isNotSeparator(cp)) {
				buf.appendCodePoint(cp);
				parsingWord = 1;
				count = 1;
			}
			while ((cp = re.read()) != -1 && parsingWord != 2) {
				if (isNotSeparator(cp)) {
					buf.appendCodePoint(cp);
					count++;
					if (count == 10) {
						return false;
					}
					parsingWord = 1;
				} else if (parsingWord == 1) {
					parsingWord = 2;
				}
			}
			if ("important".equals(buf.toString().toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

	static boolean bufferEndsWithEscapedCharOrWS(StringBuilder buffer) {
		int len = buffer.length();
		if (len != 0) {
			int bufCp = buffer.codePointAt(len - 1);
			if (ParseHelper.isHexCodePoint(bufCp) || bufCp == 32) {
				for (int i = 2; i <= 6; i++) {
					bufCp = buffer.codePointAt(len - i);
					if (ParseHelper.isHexCodePoint(bufCp)) {
						continue;
					} else if (bufCp == 92) { // \
						return true;
					} else {
						break;
					}
				}
			}
		}
		return false;
	}

	private static boolean isNotSeparator(int cp) {
		return cp != 32 && cp != 9 && cp != 10 && cp != 12 && cp != 13;
	}

	static SACMediaList parseMediaList(String media) {
		return new MySACMediaList(media.split(","));
	}

	private static class MySACMediaList implements SACMediaList {

		String[] list;

		MySACMediaList(String[] list) {
			super();
			this.list = list;
		}

		@Override
		public int getLength() {
			return list.length;
		}

		@Override
		public String item(int index) {
			if (index < 0 || index >= list.length) {
				return null;
			}
			return list[index].trim();
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			if (list.length != 0) {
				buf.append(list[0].trim());
			}
			for (int i = 1; i < list.length; i++) {
				buf.append(',').append(list[i].trim());
			}
			return buf.toString();
		}

	}

	private static class MySACAllMediaList implements SACMediaList {

		MySACAllMediaList() {
			super();
		}

		@Override
		public int getLength() {
			return 1;
		}

		@Override
		public String item(int index) {
			if (index != 0) {
				return null;
			}
			return "all";
		}

		@Override
		public String toString() {
			return "all";
		}

	}

	private static class SACMediaListWrapper {

		private final SACMediaList mediaList;
		private final SACMediaListWrapper parent;

		SACMediaListWrapper(SACMediaList mediaList, SACMediaListWrapper parent) {
			super();
			this.mediaList = mediaList;
			this.parent = parent;
		}

		public SACMediaList getMediaList() {
			return mediaList;
		}

		SACMediaListWrapper getParent() {
			return parent;
		}

		@Override
		public String toString() {
			return mediaList != null ? mediaList.toString() : "";
		}

	}

	abstract class NestedRuleTH extends CSSTokenHandler {

		private final String blockRuleName;
		private final boolean singleName;
		private final int offset;
		private byte stage = STAGE_WAIT_NAME;
		private int curlyBracketDepth = 0;
		private final DeclarationTokenHandler declarationHandler;

		private static final byte STAGE_WAIT_NAME = 1;
		private static final byte STAGE_WAIT_BLOCK_LIST = 2;
		private static final byte STAGE_WAIT_SELECTOR = 3;
		private static final byte STAGE_FOUND_SELECTOR = 4;
		private static final byte STAGE_DECLARATION_LIST = 5;
		private static final byte STAGE_END_BLOCK_LIST = 6;
		static final byte STAGE_SELECTOR_ERROR = 9;

		NestedRuleTH(int offset, int bufSize, String blockRuleName, boolean singleName) {
			super(null);
			this.offset = offset;
			this.blockRuleName = blockRuleName;
			this.singleName = singleName;
			declarationHandler = new NestedRuleDeclarationTokenHandler();
			buffer = new StringBuilder(bufSize);
		}

		void setStage(byte stage) {
			this.stage = stage;
		}

		@Override
		public void word(int index, CharSequence word) {
			if (stage == STAGE_DECLARATION_LIST) {
				declarationHandler.word(index, word);
			} else {
				if (stage == STAGE_WAIT_BLOCK_LIST || stage == STAGE_END_BLOCK_LIST) {
					unexpectedTokenError(index, word);
				} else if (!parseError) {
					buffer.append(word);
					if (stage == STAGE_WAIT_SELECTOR) {
						stage = STAGE_FOUND_SELECTOR;
					}
					prevcp = 65;
				}
			}
		}

		@Override
		public void separator(int index, int codePoint) {
			if (stage == STAGE_DECLARATION_LIST) {
				declarationHandler.separator(index, codePoint);
			} else {
				if (buffer.length() != 0) {
					if (stage == STAGE_WAIT_NAME && singleName && (escapedTokenIndex == -1 || isPrevCpWhitespace())) {
						stage = STAGE_WAIT_BLOCK_LIST;
					}
					if (!isPrevCpWhitespace()) {
						buffer.append(' ');
					}
				}
				prevcp = 32;
			}
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteCp) {
			if (stage == STAGE_DECLARATION_LIST) {
				declarationHandler.quoted(index, quoted, quoteCp);
			} else {
				if (stage == STAGE_WAIT_NAME) {
					if (singleName) {
						if (buffer.length() == 0) {
							buffer.append(quoted);
							stage = STAGE_WAIT_BLOCK_LIST;
							prevcp = 65;
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
									blockRuleName + " name must be a single identifier or string");
						}
					} else {
						buffer.append(quoted);
					}
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Expected " + blockRuleName + " selector, found '" + quoted + "'");
				}
			}
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			quoted(index, quoted, quoteCp);
		}

		@Override
		public void openGroup(int index, int codePoint) {
			if (stage == STAGE_DECLARATION_LIST) {
				declarationHandler.openGroup(index, codePoint);
				if (codePoint == TokenProducer.CHAR_LEFT_CURLY_BRACKET) {
					curlyBracketDepth++;
				}
			} else {
				if (codePoint == TokenProducer.CHAR_LEFT_CURLY_BRACKET) { // '{'
					curlyBracketDepth++;
					if (stage == STAGE_WAIT_NAME || stage == STAGE_WAIT_BLOCK_LIST) {
						if (buffer.length() != 0) {
							processName(index, unescapeBuffer(index).trim());
							if (!parseError) {
								prevcp = codePoint;
								stage = STAGE_WAIT_SELECTOR;
							}
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, blockRuleName + " must have a name.");
						}
					} else if (stage == STAGE_FOUND_SELECTOR) {
						processSelector(index);
						prevcp = codePoint;
						declarationHandler.curlyBracketDepth = 1;
						stage = STAGE_DECLARATION_LIST;
					}
					return;
				}
				handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
						"Unexpected char: " + new String(Character.toChars(codePoint)));
			}
		}

		void processName(int index, String name) {
			if ("initial".equalsIgnoreCase(name) || "inherit".equalsIgnoreCase(name) || "unset".equalsIgnoreCase(name)
					|| "none".equalsIgnoreCase(name) || "revert".equalsIgnoreCase(name)) {
				handleError(index, ParseHelper.ERR_INVALID_IDENTIFIER, "A CSS keyword is not a valid custom ident.");
			}
		}

		abstract void processSelector(int index);

		abstract void endBlock();

		abstract void endBlockList();

		@Override
		public void closeGroup(int index, int codePoint) {
			if (stage == STAGE_DECLARATION_LIST) {
				declarationHandler.closeGroup(index, codePoint);
				if (codePoint == TokenProducer.CHAR_RIGHT_CURLY_BRACKET) {
					curlyBracketDepth--;
					if (curlyBracketDepth == 1) {
						endBlock();
						stage = STAGE_WAIT_SELECTOR;
					}
				}
				prevcp = codePoint;
			} else {
				if (codePoint == TokenProducer.CHAR_RIGHT_CURLY_BRACKET) { // }
					curlyBracketDepth--;
					if (curlyBracketDepth == 0) {
						// Body of rule ends
						endBlockList();
						stage = STAGE_END_BLOCK_LIST;
						prevcp = codePoint;
					} else if (curlyBracketDepth == 1 && stage == STAGE_SELECTOR_ERROR) {
						stage = STAGE_WAIT_SELECTOR;
						parseError = false;
					}
					return;
				}
				handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
						"Unexpected char: " + new String(Character.toChars(codePoint)));
			}
		}

		@Override
		public void character(int index, int codePoint) {
			if (stage == STAGE_DECLARATION_LIST) {
				declarationHandler.character(index, codePoint);
			} else {
				char[] chars = Character.toChars(codePoint);
				if (stage == STAGE_WAIT_SELECTOR || stage == STAGE_FOUND_SELECTOR) {
					if (isValidSelectorCharacter(codePoint)) {
						buffer.append(chars);
						prevcp = codePoint;
						return;
					} else {
						stage = STAGE_SELECTOR_ERROR;
					}
				} else if (stage == STAGE_WAIT_NAME && (isValidNameCharacter(codePoint)
						|| (!singleName && codePoint == TokenProducer.CHAR_COMMA))) {
					buffer.append(chars);
					prevcp = codePoint;
					return;
				}
				handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected char: " + new String(chars));
			}
		}

		abstract boolean isValidSelectorCharacter(int codePoint);

		abstract boolean isValidNameCharacter(int codePoint);

		@Override
		public void escaped(int index, int codePoint) {
			if (stage == STAGE_DECLARATION_LIST) {
				declarationHandler.escaped(index, codePoint);
			} else {
				char[] chars = Character.toChars(codePoint);
				if (stage == STAGE_WAIT_BLOCK_LIST || stage == STAGE_END_BLOCK_LIST) {
					unexpectedTokenError(index, new String(chars));
				} else if (!parseError) {
					if (ParseHelper.isHexCodePoint(codePoint)) {
						setEscapedTokenStart(index);
						buffer.append('\\');
					}
					buffer.append(chars);
					if (stage == STAGE_WAIT_SELECTOR) {
						stage = STAGE_FOUND_SELECTOR;
					}
					prevcp = 65;
				}
			}
		}

		@Override
		public void endOfStream(int len) {
			if (curlyBracketDepth != 0) {
				if (stage == STAGE_DECLARATION_LIST) {
					declarationHandler.endOfStream(len);
					endBlock();
					endBlockList();
				} else if (stage == STAGE_END_BLOCK_LIST) {
					return;
				} else if (stage == STAGE_WAIT_SELECTOR || stage == STAGE_FOUND_SELECTOR) {
					endBlockList();
				}
				handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of " + blockRuleName + " rule.");
			} else if (stage != STAGE_END_BLOCK_LIST) {
				handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "Malformed " + blockRuleName + " rule.");
			}
		}

		@Override
		public void control(int index, int codepoint) {
			super.control(offset + index, codepoint);
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			if (!parseError && buffer.length() == 0 && curlyBracketDepth == 1 && parendepth == 0
					&& stage == STAGE_WAIT_SELECTOR) {
				super.commented(index, commentType, comment);
			} else {
				separator(index, 12);
				prevcp = 12;
			}
		}

		@Override
		CSSParseException createException(int index, byte errCode, String message) {
			return super.createException(offset + index, errCode, message);
		}

		private class NestedRuleDeclarationTokenHandler extends DeclarationTokenHandler {

			private NestedRuleDeclarationTokenHandler() {
				super(null, null);
			}

			@Override
			protected void handleRightCurlyBracket(int index) {
				resetHandler();
				NestedRuleTH.this.stage = STAGE_WAIT_SELECTOR;
				NestedRuleTH.this.prevcp = TokenProducer.CHAR_RIGHT_CURLY_BRACKET;
			}

			@Override
			public void control(int index, int codepoint) {
				NestedRuleTH.this.control(index, codepoint);
			}

			@Override
			CSSParseException createException(int index, byte errCode, String message) {
				return NestedRuleTH.this.createException(index, errCode, message);
			}

		}

	}

	private class KeyFrameBlockListTH extends NestedRuleTH {
		KeyFrameBlockListTH(int offset, int bufSize) {
			super(offset, bufSize, "@keyframes", true);
		}

		@Override
		void processSelector(int index) {
			String selector = rawBuffer();
			InputSource source = new InputSource(new StringReader(selector));
			LexicalUnit kfsel;
			try {
				kfsel = parsePropertyValue(source);
				((KeyframesHandler) handler).startKeyframe(kfsel);
			} catch (CSSException e) {
				handleError(index, ParseHelper.ERR_WRONG_VALUE, e.getMessage());
				setStage(STAGE_SELECTOR_ERROR);
				return;
			} catch (IOException e) {
				// Should not happen
			}
		}

		@Override
		void processName(int index, String name) {
			super.processName(index, name);
			if (!parseError) {
				((KeyframesHandler) handler).startKeyframes(name);
			}
		}

		@Override
		void endBlock() {
			((KeyframesHandler) handler).endKeyframe();
		}

		@Override
		void endBlockList() {
			((KeyframesHandler) handler).endKeyframes();
		}

		@Override
		boolean isValidSelectorCharacter(int codePoint) {
			return codePoint == TokenProducer.CHAR_PERCENT_SIGN || codePoint == TokenProducer.CHAR_COMMA
					|| codePoint == TokenProducer.CHAR_FULL_STOP;
		}

		@Override
		boolean isValidNameCharacter(int codePoint) {
			return false;
		}

	}

	private class FontFeatureValuesTH extends NestedRuleTH {
		FontFeatureValuesTH(int offset, int bufSize) {
			super(offset, bufSize, "@font-feature-values", false);
		}

		@Override
		void processSelector(int index) {
			String selector = unescapeBuffer(index);
			if (selector.length() > 1 && selector.charAt(0) == '@') {
				((FontFeatureValuesHandler) handler).startFeatureMap(selector.substring(1).trim());
			} else {
				handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Bad feature name: " + selector);
			}
		}

		@Override
		void processName(int index, String name) {
			String[] familyName = name.split("\\s*,\\s*");
			for (String fontName : familyName) {
				super.processName(index, fontName);
			}
			if (!parseError) {
				((FontFeatureValuesHandler) handler).startFontFeatures(familyName);
			}
		}

		@Override
		void endBlock() {
			((FontFeatureValuesHandler) handler).endFeatureMap();
		}

		@Override
		void endBlockList() {
			((FontFeatureValuesHandler) handler).endFontFeatures();
		}

		@Override
		boolean isValidSelectorCharacter(int codePoint) {
			return codePoint == TokenProducer.CHAR_COMMERCIAL_AT;
		}

		@Override
		boolean isValidNameCharacter(int codePoint) {
			return codePoint == TokenProducer.CHAR_HYPHEN_MINUS;
		}

	}

	private class RuleTokenHandler extends SheetTokenHandler {

		RuleTokenHandler(InputSource source, NamespaceMap nsMap) {
			super(source, nsMap);
		}

		@Override
		protected void endRuleBody() {
			if (getCurlyBracketDepth() == 0) {
				contextHandler = new RuleEndContentHandler();
			} else {
				super.endRuleBody();
			}
		}

		@Override
		protected void resetRuleState() {
			super.resetRuleState();
			contextHandler = new RuleEndContentHandler();
		}

		@Override
		void endDocument() {
		}

		private class RuleEndContentHandler extends CSSTokenHandler {

			RuleEndContentHandler() {
				super(null);
			}

			@Override
			public void word(int index, CharSequence word) {
				reportError(index);
			}

			@Override
			public void separator(int index, int codePoint) {
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
				reportError(index);
			}

			@Override
			public void quotedWithControl(int index, CharSequence quoted, int quote) {
				reportError(index);
			}

			@Override
			public void openGroup(int index, int codePoint) {
				reportError(index);
			}

			@Override
			public void closeGroup(int index, int codePoint) {
				reportError(index);
			}

			@Override
			public void character(int index, int codePoint) {
				reportError(index);
			}

			@Override
			public void escaped(int index, int codePoint) {
				reportError(index);
			}

			@Override
			public void control(int index, int codepoint) {
				RuleTokenHandler.this.control(index, codepoint);
			}

			@Override
			public void commented(int index, int commentType, String comment) {
			}

			@Override
			public void endOfStream(int len) {
			}

			@Override
			public void error(int index, byte errCode, CharSequence context) {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Found tokens after rule");
			}

			private void reportError(int index) {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Found tokens after rule");
			}

			@Override
			protected void handleError(int index, byte errCode, String message) {
				if (!parseError && errorHandler != null) {
					errorHandler.fatalError(createException(index, errCode, message));
				}
				parseError = true;
			}

			@Override
			CSSParseException createException(int index, byte errCode, String message) {
				return RuleTokenHandler.this.createException(index, errCode, message);
			}

		}
	}

	class SheetTokenHandler extends CSSTokenHandler {

		CSSTokenHandler contextHandler;
		private final DeclarationTokenHandler declarationHandler;
		private final SelectorTokenHandler selectorHandler;

		private String ruleFirstPart = null;
		private String ruleSecondPart = null;
		private String marginRule = null;

		private int curlyBracketDepth = 0;

		private byte ruleType = 0;

		private final byte MEDIA_RULE = 4;
		private final byte FONT_FACE_RULE = 5;
		private final byte PAGE_RULE = 6;
		private final byte MARGIN_RULE = 9;

		// @formatter:off
		//
		// Stage: 0 initial, 1 unknown rule
		// Non-nested: 32 charset (at beginning, ignored),
		//             34 namespace (expecting first token)
		//             35 namespace (expecting second token)
		//             36 namespace (expecting second token as url)
		//             37 namespace (received second token as url)
		//             38 import (expecting first token)
		//             39 import (expecting first token as url)
		//             40 import (expecting second token or final)
		// Stage 2: media, font-face, page
		// Other rules (stage 5): supports, document
		// font-feature-values, counter-style, keyframes, viewport
		// Stage 7: nested rule inside a stage-2 rule
		// Stage 8: nested page rule inside a media rule
		// Stage 9: nested media rule inside a media rule
		// Stage 10: nested font-face rule inside a media rule
		//
		// @formatter:on
		private byte stage = 0;

		// Next field is to check for @charset rules in bad place
		private boolean rulesFound = false;

		private SACMediaListWrapper medialist = null;

		SheetTokenHandler(InputSource source, NamespaceMap nsMap) {
			super(source);
			buffer = new StringBuilder(512);
			declarationHandler = new MyDeclarationTokenHandler(source);
			selectorHandler = new MySelectorTokenHandler(source, nsMap);
			contextHandler = selectorHandler;
		}

		int getCurlyBracketDepth() {
			return curlyBracketDepth;
		}

		@Override
		public void word(int index, CharSequence word) {
			if (contextHandler != null) {
				contextHandler.word(index, word);
			} else {
				if (prevcp == 64) { // Got an at-rule
					if (stage == 0) {
						if (ParseHelper.equalsIgnoreCase(word, "charset")) {
							if (!rulesFound) {
								stage = 32;
								buffer.setLength(0);
							} else {
								handleError(index - 1, ParseHelper.ERR_RULE_SYNTAX, "@charset must be the first rule");
							}
						} else if (ParseHelper.equalsIgnoreCase(word, "import")) {
							stage = 38;
							buffer.setLength(0);
						} else if (ParseHelper.equalsIgnoreCase(word, "namespace")) {
							stage = 34;
							buffer.setLength(0);
						} else if (ParseHelper.equalsIgnoreCase(word, "media")) {
							ruleType = MEDIA_RULE;
							stage = 2;
							buffer.setLength(0);
						} else if (ParseHelper.equalsIgnoreCase(word, "font-face")) {
							ruleType = FONT_FACE_RULE;
							stage = 2;
							buffer.setLength(0);
						} else if (ParseHelper.equalsIgnoreCase(word, "page")) {
							ruleType = PAGE_RULE;
							stage = 2;
							buffer.setLength(0);
						} else {
							buffer.append(word);
							stage = 5;
						}
					} else if (stage == 2) {
						String ruleName = word.toString().toLowerCase(Locale.ROOT);
						if (ruleType == PAGE_RULE) {
							if (isMarginRuleName(ruleName)) {
								ruleType = MARGIN_RULE;
								marginRule = ruleName;
								handler.startPage(ruleName, null);
								buffer.setLength(0);
							} else {
								handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
										"Expected margin rule, got " + word);
							}
						} else if (ruleType == MEDIA_RULE && "page".equals(ruleName)) {
							// Nested page rule inside @media
							ruleType = PAGE_RULE;
							stage = 8;
							buffer.setLength(0);
						} else if (ruleType == MEDIA_RULE && "font-face".equals(ruleName)) {
							// Nested font-face rule inside @media
							ruleType = FONT_FACE_RULE;
							stage = 10;
							buffer.setLength(0);
						} else if (ruleType == MEDIA_RULE && "media".equals(ruleName)) {
							// Nested media rule inside @media
							buffer.setLength(0);
						} else {
							// Nested rule
							stage = 7;
							buffer.append(word);
						}
					} else {
						buffer.append(word);
					}
				} else if (ruleType == PAGE_RULE && buffer.length() != 1 && ruleFirstPart != null) {
					// last cp not ':', not identifier, and page type already set
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Expecting pseudo-page, got " + word);
				} else {
					buffer.append(word);
				}
				prevcp = 65;
			}
		}

		private boolean isMarginRuleName(String ruleName) {
			StringTokenizer st = new StringTokenizer(ruleName, "-");
			while (st.hasMoreElements()) {
				String s = st.nextToken();
				if (!"top".equals(s) && !"left".equals(s) && !"center".equals(s) && !"right".equals(s)
						&& !"corner".equals(s) && !"bottom".equals(s) && !"middle".equals(s)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void separator(int index, int codepoint) {
			if (contextHandler != null) {
				contextHandler.separator(index, codepoint);
			} else {
				if (stage == 34) {
					if (ruleFirstPart == null) {
						if (buffer.length() != 0) {
							ruleFirstPart = buffer.toString();
							buffer.setLength(0);
							stage = 35;
						}
					} else {
						stage = 35;
					}
				} else if ((stage == 2 && ruleType == PAGE_RULE) || stage == 8) {
					processBufferPageRule(index);
					if (prevcp != 44) {
						prevcp = 32;
					}
					return;
				} else if (buffer.length() != 0 && (!isPrevCpWhitespace()
						|| (escapedTokenIndex != -1 && bufferEndsWithEscapedCharOrWS(buffer)))) {
					buffer.append(' ');
				}
				prevcp = 32;
			}
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteCp) {
			if (contextHandler != null) {
				contextHandler.quoted(index, quoted, quoteCp);
			} else if (stage == 5 || stage == 7) {
				char c = (char) quoteCp;
				buffer.append(c).append(quoted).append(c);
			} else {
				if (ruleFirstPart == null) {
					ruleFirstPart = quoted.toString();
					if (stage == 38) {
						stage = 40;
					}
				} else if (ruleSecondPart == null) {
					ruleSecondPart = quoted.toString();
				} else { // We fill the buffer so the error is found later
					buffer.append(quoted);
				}
				prevcp = 65;
			}
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			quoted(index, quoted, quoteCp);
		}

		@Override
		public void openGroup(int index, int codepoint) {
			if (contextHandler != null) {
				contextHandler.openGroup(index, codepoint);
			} else {
				if (codepoint == 123) { // '{'
					curlyBracketDepth++;
					if (stage == 2) {
						if (ruleType == MEDIA_RULE) {
							// Body of media rule starts
							if (parendepth == 0) {
								if (buffer.length() != 0) {
									medialist = newMediaList(buffer.toString());
									buffer.setLength(0);
								} else {
									medialist = newMediaListAll();
								}
								handler.startMedia(medialist.getMediaList());
								contextHandler = selectorHandler;
								selectorHandler.prevcp = 32;
							} else {
								handleError(index - buffer.length(), ParseHelper.ERR_UNEXPECTED_TOKEN,
										"Bad media query at @media rule: <" + buffer.toString() + ">");
								if (medialist != null) {
									medialist = medialist.getParent();
								}
								if (medialist == null) {
									ruleType = 0;
									stage = 0;
								}
								curlyBracketDepth--;
								parendepth = 0;
								contextHandler = new IgnoredDeclarationTokenHandler();
							}
						} else if (curlyBracketDepth == 1) {
							if (ruleType == FONT_FACE_RULE) {
								startFontFaceRule(index);
							} else if (ruleType == PAGE_RULE) {
								startPageRule(index);
							} else if (ruleType == MARGIN_RULE) {
								if (buffer.length() != 0) {
									handleError(index - buffer.length(), ParseHelper.ERR_UNEXPECTED_TOKEN,
											"Unexpected token in margin rule: " + buffer.toString());
								} else {
									declarationHandler.curlyBracketDepth = 1;
									contextHandler = declarationHandler;
								}
								curlyBracketDepth--;
							}
							buffer.setLength(0);
						}
					} else if (stage == 8 && curlyBracketDepth >= 2) {
						startPageRule(index);
					} else if (stage == 10 && curlyBracketDepth >= 2) {
						startFontFaceRule(index);
					} else {
						buffer.append('{');
					}
				} else if (codepoint == TokenProducer.CHAR_LEFT_PAREN) {
					parendepth++;
					if (stage == 35) {
						if (bufferEquals("url")) { // "uri("
							stage = 36;
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
									"Unexpected '(' after " + buffer.toString());
						}
					} else if (stage == 38) {
						if (bufferEquals("url")) { // "uri("
							stage = 39;
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
									"Unexpected '(' after " + buffer.toString());
						}
					} else if (stage == 34) {
						if (bufferEquals("url")) { // "uri("
							// Default namespace
							ruleFirstPart = "";
							stage = 36;
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
									"Unexpected '(' after " + buffer.toString());
						}
					} else {
						buffer.append('(');
					}
				} else {
					bufferAppend(codepoint);
				}
				prevcp = codepoint;
			}
		}

		private void startPageRule(int index) {
			processBufferPageRule(index);
			handler.startPage(ruleFirstPart, ruleSecondPart);
			declarationHandler.curlyBracketDepth = 1;
			contextHandler = declarationHandler;
			curlyBracketDepth--;
		}

		private void startFontFaceRule(int index) {
			if (buffer.length() != 0) {
				handleError(index - buffer.length(), ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected token in @font-face rule: " + buffer.toString());
			} else {
				handler.startFontFace();
				declarationHandler.curlyBracketDepth = 1;
				contextHandler = declarationHandler;
				curlyBracketDepth--;
			}
		}

		@Override
		public void closeGroup(int index, int codepoint) {
			if (contextHandler != null) {
				contextHandler.closeGroup(index, codepoint);
			} else {
				if (codepoint == 125) { // }
					curlyBracketDepth--;
					buffer.append('}');
					if (curlyBracketDepth == 0) {
						// Body of rule ends
						handler.ignorableAtRule(buffer.toString());
						buffer.setLength(0);
						stage = 0;
						endRuleBody();
					} else if (curlyBracketDepth == 1) {
						if (stage == 7) {
							handler.ignorableAtRule(buffer.toString());
							buffer.setLength(0);
							stage = 2;
							switchContextToStage2();
						}
					}
				} else if (codepoint == 41) {
					parendepth--;
					if (stage == 36) { // Ignore final ')' for URI
						processBuffer(index);
						if (ruleSecondPart != null) {
							stage = 37;
						} else {
							handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Empty URI in namespace rule");
						}
					} else if (stage == 39) {
						processBuffer(index);
						stage = 40;
					} else {
						buffer.append(')');
					}
				} else {
					bufferAppend(codepoint);
				}
				prevcp = codepoint;
			}
		}

		protected void endRuleBody() {
			contextHandler = selectorHandler;
			selectorHandler.prevcp = 32;
			rulesFound = true;
		}

		private void switchContextToStage2() {
			if (ruleType == MEDIA_RULE) {
				contextHandler = selectorHandler;
				selectorHandler.prevcp = 32;
			} else if (ruleType == FONT_FACE_RULE) {
				declarationHandler.curlyBracketDepth = 1;
				contextHandler = declarationHandler;
			} else if (ruleType == PAGE_RULE) {
				declarationHandler.curlyBracketDepth = 1;
				contextHandler = declarationHandler;
			} else if (ruleType == MARGIN_RULE) {
				declarationHandler.curlyBracketDepth = 1;
				contextHandler = declarationHandler;
			}
		}

		@Override
		public void character(int index, int codepoint) {
			if (contextHandler != null) {
				contextHandler.character(index, codepoint);
			} else {
				if (codepoint == 59) { // ;
					if (curlyBracketDepth == 0) {
						// End of rule
						if (parendepth == 0) {
							if (stage != 0) {
								endOfAtRule(index);
							} else {
								handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Empty @-rule.");
								resetRuleState();
							}
						} else {
							handleError(index, ParseHelper.ERR_UNMATCHED_PARENTHESIS, "Unmatched parentheses in rule.");
							resetRuleState();
						}
						contextHandler = selectorHandler;
					} else {
						buffer.append(';');
					}
				} else {
					if (ruleType == PAGE_RULE) {
						if (codepoint == 44) { // ,
							processBufferPageRule(index);
						} else if (codepoint == 58 && (prevcp == 44 || isPrevCpWhitespace())) {
							buffer.append(':');
							return; // preserve prevcp
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
									"Unexpected char " + new String(Character.toChars(codepoint)));
						}
					} else {
						bufferAppend(codepoint);
					}
				}
				prevcp = codepoint;
			}
		}

		private void endOfAtRule(int index) {
			processBuffer(index);
			if (buffer.length() != 0) {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Malformed @-rule, unexpected <" + buffer.toString() + ">");
			}
			if (stage == 40 || stage == 38) {
				if (ruleSecondPart != null) {
					medialist = newMediaList(ruleSecondPart);
				} else {
					medialist = null; // A dangling value may be there from a malformed rule
				}
				if (ruleFirstPart != null) {
					if (medialist == null) {
						medialist = newMediaListAll();
					}
					handler.importStyle(ruleFirstPart, medialist.getMediaList(), null);
				} else if (!parseError) {
					handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Malformed @-rule");
				}
			} else if (stage == 35 || stage == 37) {
				if (ruleSecondPart != null) {
					namespaceDeclaration(ruleFirstPart, ruleSecondPart);
				} else {
					handleError(index, ParseHelper.ERR_RULE_SYNTAX, "No URI in namespace rule");
				}
			} else if (stage == 34) {
				namespaceDeclaration("", ruleFirstPart);
			} else if (stage == 36) { // Bad namespace rule
				handleError(index, ParseHelper.ERR_RULE_SYNTAX, "Bad URI in namespace rule");
			}
			resetRuleState();
			rulesFound = true;
		}

		private SACMediaListWrapper newMediaListAll() {
			return new SACMediaListWrapper(new MySACAllMediaList(), medialist);
		}

		private SACMediaListWrapper newMediaList(String media) {
			return new SACMediaListWrapper(parseMediaList(media), medialist);
		}

		void namespaceDeclaration(String prefix, String uri) {
			handler.namespaceDeclaration(prefix, uri);
			selectorHandler.factory.registerNamespacePrefix(prefix, uri);
		}

		void resetRuleState() {
			parseError = false;
			if (medialist != null) {
				medialist = medialist.getParent();
				if (medialist == null) {
					stage = 0;
				}
			} else {
				stage = 0;
			}
			ruleFirstPart = null;
			ruleSecondPart = null;
			prevcp = 32;
			buffer.setLength(0);
		}

		private void processBufferPageRule(int index) {
			int buflen = buffer.length();
			if (buflen != 0) {
				if (buffer.charAt(0) == ':') {
					if (buflen == 1) {
						handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_CHAR,
								"Character ':' must always precede an identifier in page rule preludes");
					} else if (ruleSecondPart == null) {
						ruleSecondPart = buffer.toString();
					} else {
						ruleSecondPart = new StringBuilder(ruleSecondPart.length() + buflen).append(ruleSecondPart)
								.append(',').append(buffer).toString();
					}
				} else if (ruleFirstPart == null) {
					ruleFirstPart = buffer.toString();
				} else {
					handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN,
							"Unexpected identifier at page rule: " + buffer.toString());
				}
				buffer.setLength(0);
			}
		}

		private void processBuffer(int index) {
			if (buffer.length() != 0) {
				// Trim possible trailing space
				trimBufferTail();
				if (ruleFirstPart == null) {
					ruleFirstPart = buffer.toString();
					buffer.setLength(0);
				} else if (ruleSecondPart == null) {
					ruleSecondPart = buffer.toString();
					buffer.setLength(0);
				}
			}
		}

		private void trimBufferTail() {
			if (buffer.charAt(buffer.length() - 1) == ' ') {
				buffer.setLength(buffer.length() - 1);
			}
		}

		private boolean bufferEquals(String lcWord) {
			if (ParseHelper.equalsIgnoreCase(buffer, lcWord)) {
				buffer.setLength(0);
				return true;
			}
			return false;
		}

		@Override
		public void escaped(int index, int codepoint) {
			if (contextHandler != null) {
				contextHandler.escaped(index, codepoint);
			} else if (!parseError) {
				if (ParseHelper.isHexCodePoint(codepoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				}
				bufferAppend(codepoint);
			}
			prevcp = 65;
		}

		@Override
		public void endOfStream(int len) {
			if (contextHandler != null) {
				contextHandler.endOfStream(len);
				return;
			} else {
				if (ruleType == MARGIN_RULE) {
					handler.endPage(marginRule, null);
					ruleType = PAGE_RULE;
				}
				if (ruleType == PAGE_RULE) {
					processBufferPageRule(len);
					handler.endPage(ruleFirstPart, ruleSecondPart);
					if (stage == 8) {
						handler.endMedia(medialist.getMediaList());
					}
				} else if (ruleType == FONT_FACE_RULE) {
					handler.endFontFace();
					if (stage == 10) {
						handler.endMedia(medialist.getMediaList());
					}
				} else if (ruleType == MEDIA_RULE) {
					while (medialist != null) {
						handler.endMedia(medialist.getMediaList());
						medialist = medialist.getParent();
					}
				} else if (stage != 0 && !parseError) {
					if (curlyBracketDepth == 0) {
						endOfAtRule(len);
					} else if (buffer.length() != 0) {
						do {
							curlyBracketDepth--;
							buffer.append('}');
						} while (curlyBracketDepth > 0);
						// Body of rule ends
						handler.ignorableAtRule(buffer.toString());
						handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of stream");
					}
				}
			}
			endDocument();
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			if (stage == 5 || stage == 7) {
				// Unknown rule
				if (commentType == 0) {
					buffer.append("/*").append(comment).append("*/");
				}
			} else if (contextHandler != null) {
				contextHandler.commented(index, commentType, comment);
			} else {
				separator(index, 12);
			}
		}

		@Override
		protected void handleError(int index, byte errCode, String message) {
			if (contextHandler != null) {
				contextHandler.handleError(index, errCode, message);
			} else {
				super.handleError(index, errCode, message);
				buffer.setLength(0);
				this.stage = 127;
			}
		}

		private class MySelectorTokenHandler extends SelectorTokenHandler {

			MySelectorTokenHandler(InputSource source, NamespaceMap nsMap) {
				super(source, nsMap);
			}

			@Override
			protected void handleLeftCurlyBracket(int index) {
				declarationHandler.curlyBracketDepth = 1;
				contextHandler = declarationHandler;
				if (!parseError) {
					selectorHandler.processBuffer(index, TokenProducer.CHAR_LEFT_CURLY_BRACKET, true);
					if (!parseError) {
						if (addCurrentSelector(index)) {
							handler.startSelector(selist);
						} else {
							unexpectedCharError(index, 123);
						}
					}
				}
				MySelectorTokenHandler.this.stage = 0;
				if (parseError) {
					buffer.setLength(0);
					ignoreRule();
				}
			}

			private void ignoreRule() {
				selist.clear();
				currentsel = null;
				contextHandler = new IgnoredDeclarationTokenHandler();
			}

			@Override
			protected void handleRightCurlyBracket(int index) {
				if (SheetTokenHandler.this.stage == 2) {
					byte ruleType = SheetTokenHandler.this.ruleType;
					if (ruleType == MEDIA_RULE) {
						handler.endMedia(medialist.getMediaList());
						medialist = medialist.getParent();
						if (medialist == null) {
							resetSelectorHandler(true);
						} else {
							resetSelectorHandler(false);
						}
						return;
					} else if (SheetTokenHandler.this.curlyBracketDepth == 1) {
						resetSelectorHandler(true);
						return;
					}
				}
				handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected '}'");
				resetSelectorHandler(true);
				if (SheetTokenHandler.this.curlyBracketDepth < 0) {
					SheetTokenHandler.this.curlyBracketDepth = 0;
				}
			}

			private void resetSelectorHandler(boolean resetSheetStage) {
				resetHandler();
				contextHandler = selectorHandler;
				SheetTokenHandler.this.curlyBracketDepth--;
				if (resetSheetStage) {
					SheetTokenHandler.this.ruleType = 0;
					SheetTokenHandler.this.stage = 0;
				}
			}

			@Override
			boolean skipCharacterHandling() {
				return parseError && (prevcp != ';' || curlyBracketDepth != 0);
			}

			@Override
			protected void handleAtKeyword(int index) {
				// At-rule
				if (prevcp == ';') {
					parseError = false;
					stage = 0;
				} else if (stage > 0) {
					int len = buffer.length();
					if (len != 0) {
						String message;
						int cp = buffer.codePointAt(0);
						if (Character.isAlphabetic(cp)) {
							message = "Unknown token: " + buffer.toString();
						} else {
							message = "Unknown token starting with code point U+" + Integer.toHexString(cp);
						}
						handleError(index - len, ParseHelper.ERR_UNEXPECTED_TOKEN, message);
					} else {
						unexpectedCharError(index, 64);
					}
					return;
				}
				contextHandler = null;
				SheetTokenHandler.this.prevcp = 64;
				SheetTokenHandler.this.buffer.append('@');
			}

			@Override
			public void control(int index, int codepoint) {
				SheetTokenHandler.this.control(index, codepoint);
			}

			@Override
			public void endOfStream(int len) {
				if (buffer.length() != 0) {
					handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of stream");
				}
				endDocument();
			}

			@Override
			protected void handleError(int index, byte errCode, String message) {
				MySelectorTokenHandler.this.stage = 127;
				if (!parseError) {
					if (errorHandler != null) {
						if (prevcp == endcp) {
							errorHandler.error(createException(index, errCode, "Expected end of file"));
						} else {
							errorHandler.error(createException(index, errCode, message));
						}
					}
					parseError = true;
				}
				selist.clear();
				buffer.setLength(0);
			}

			@Override
			CSSParseException createException(int index, byte errCode, String message) {
				return SheetTokenHandler.this.createException(index, errCode, message);
			}

		}

		private class MyDeclarationTokenHandler extends DeclarationTokenHandler {

			MyDeclarationTokenHandler(InputSource source) {
				super(source, ShorthandDatabase.getInstance());
			}

			@Override
			protected void handleRightCurlyBracket(int index) {
				contextHandler = selectorHandler;
				selectorHandler.parseError = false;
				selectorHandler.prevcp = 32;
				selectorHandler.stage = 0;
				SheetTokenHandler.this.prevcp = 125;
				if (ruleType == FONT_FACE_RULE) {
					handler.endFontFace();
					if (SheetTokenHandler.this.stage == 10) {
						ruleType = MEDIA_RULE;
						buffer.setLength(0);
						SheetTokenHandler.this.stage = 2;
						contextHandler = selectorHandler;
						selectorHandler.prevcp = 32;
					} else {
						ruleType = 0;
						SheetTokenHandler.this.stage = 0;
					}
				} else if (ruleType == PAGE_RULE) {
					handler.endPage(ruleFirstPart, ruleSecondPart);
					ruleFirstPart = null;
					ruleSecondPart = null;
					if (SheetTokenHandler.this.stage == 8) {
						ruleType = MEDIA_RULE;
						buffer.setLength(0);
						SheetTokenHandler.this.stage = 2;
						contextHandler = selectorHandler;
						selectorHandler.prevcp = 32;
					} else {
						ruleType = 0;
						SheetTokenHandler.this.stage = 0;
					}
				} else if (ruleType == MARGIN_RULE) {
					handler.endPage(marginRule, null);
					ruleType = PAGE_RULE;
					marginRule = null;
					contextHandler = declarationHandler;
					declarationHandler.curlyBracketDepth = 2;
				} else {
					// Mark the end of rule (not of selector)
					handler.endSelector(SheetTokenHandler.this.selectorHandler.selist);
					SheetTokenHandler.this.selectorHandler.selist = new MySelectorListImpl();
				}
				rulesFound = true;
				resetHandler();
			}

			@Override
			protected void handleAtKeyword(int index) {
				if (propertyName == null && buffer.length() == 0 && getCurlyBracketDepth() == 1
						&& SheetTokenHandler.this.selectorHandler.getSelectorList().getLength() == 0) {
					contextHandler = null;
					SheetTokenHandler.this.buffer.append('@');
					SheetTokenHandler.this.prevcp = 64;
				} else {
					unexpectedCharError(index, 64);
				}
			}

			@Override
			public void control(int index, int codepoint) {
				SheetTokenHandler.this.control(index, codepoint);
			}

			@Override
			public void endOfStream(int len) {
				super.endOfStream(len);
				contextHandler = null;
				SheetTokenHandler.this.endOfStream(len);
			}

			@Override
			CSSParseException createException(int index, byte errCode, String message) {
				return SheetTokenHandler.this.createException(index, errCode, message);
			}

		}

		private class IgnoredDeclarationTokenHandler extends CSSTokenHandler {

			private int curlyBracketDepth = 1;

			IgnoredDeclarationTokenHandler() {
				super(null);
			}

			@Override
			public void word(int index, CharSequence word) {
			}

			@Override
			public void separator(int index, int codePoint) {
			}

			@Override
			public void quoted(int index, CharSequence quoted, int quoteCp) {
			}

			@Override
			public void quotedWithControl(int index, CharSequence quoted, int quote) {
			}

			@Override
			public void openGroup(int index, int codePoint) {
				if (codePoint == 123) { // '{'
					curlyBracketDepth++;
				}
			}

			@Override
			public void closeGroup(int index, int codePoint) {
				if (codePoint == 125) { // }
					curlyBracketDepth--;
					if (curlyBracketDepth == 0) {
						contextHandler = selectorHandler;
						selectorHandler.prevcp = 32;
						selectorHandler.parseError = false;
						selectorHandler.stage = 0;
						SheetTokenHandler.this.prevcp = 125;
					}
				}
			}

			@Override
			public void character(int index, int codePoint) {
			}

			@Override
			public void escaped(int index, int codePoint) {
			}

			@Override
			public void control(int index, int codepoint) {
				SheetTokenHandler.this.control(index, codepoint);
			}

			@Override
			public void endOfStream(int len) {
				contextHandler = null; // help gc
				SheetTokenHandler.this.endOfStream(len);
			}

		}

	}

	private class SelectorArgumentTokenHandler extends SelectorTokenHandler {

		SelectorArgumentTokenHandler(NSACSelectorFactory factory) {
			super(factory);
		}

		@Override
		protected void newDescendantSelector(int index, short type, int triggerCp) {
			if (currentsel == null) {
				currentsel = factory.createScopeSelector();
			} else if (!isValidCurrentSelector()) {
				unexpectedCharError(index, triggerCp);
				return;
			}
			newDescendantSelector(type);
		}

		@Override
		protected void newSiblingSelector(int index, short type, int triggerCp) {
			if (currentsel == null) {
				currentsel = factory.createScopeSelector();
			} else if (!isValidCurrentSelector()) {
				unexpectedCharError(index, triggerCp);
				return;
			}
			newSiblingSelector(type);
		}

	}

	class SelectorTokenHandler extends CSSTokenHandler {

		NSACSelectorFactory factory;
		private NamespaceMap nsMap;
		MySelectorListImpl selist = new MySelectorListImpl();
		Selector currentsel = null;

		// TODO: handle default namespace if set
		private String namespacePrefix = null;
		byte stage = 0;
		private boolean functionToken;

		private static final byte STAGE_COMBINATOR_OR_END = 2;
		private static final byte STAGE_ATTR_START = 4;
		private static final byte STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE = 7;
		private static final byte STAGE_ATTR_SYMBOL = 5;
		private static final byte STAGE_ATTR_POST_SYMBOL = 6;
		private static final byte STAGE_EXPECT_ID_OR_CLASSNAME = 8;
		private static final byte STAGE_EXPECT_PSEUDOELEM_NAME = 9;
		private static final byte STAGE_EXPECT_PSEUDOCLASS_NAME = 10;
		private static final byte STAGE_EXPECT_PSEUDOCLASS_ARGUMENT = 11;

		SelectorTokenHandler() {
			this(new NSACSelectorFactory());
		}

		SelectorTokenHandler(InputSource source, NamespaceMap nsMap) {
			super(source);
			factory = new NSACSelectorFactory();
			if (nsMap == null) {
				this.nsMap = factory;
			} else {
				this.nsMap = nsMap;
			}
			buffer = new StringBuilder(64);
		}

		SelectorTokenHandler(NSACSelectorFactory factory) {
			super(null);
			this.factory = factory;
			this.nsMap = factory;
			buffer = new StringBuilder(64);
		}

		SelectorList getSelectorList() {
			return selist;
		}

		@Override
		public void word(int index, CharSequence word) {
			if (buffer.length() != 0 && isPrevCpWhitespace()) {
				buffer.append(' ');
			}
			if (stage == STAGE_ATTR_START && prevcp != 65 && prevcp != TokenProducer.CHAR_VERTICAL_LINE) {
				unexpectedTokenError(index, word);
			} else {
				if (stage == 0) {
					stage = 1;
					buffer.append(word);
				} else if (stage == STAGE_COMBINATOR_OR_END) {
					buffer.append(word);
					newDescendantSelector(Selector.SAC_DESCENDANT_SELECTOR);
					stage = 1;
				} else if (stage == STAGE_ATTR_POST_SYMBOL && isPrevCpWhitespace()) {
					if (word.length() == 1) {
						char c = word.charAt(0);
						if (c == 'i' || c == 'I') {
							setAttributeConditionFlag(AttributeCondition2.Flag.CASE_I);
						} else if (c == 's' || c == 'S') {
							setAttributeConditionFlag(AttributeCondition2.Flag.CASE_S);
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Expected 'i', found: '" + c + '\'');
						}
						if (buffer.length() != 0) {
							handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
									"Expected 'i', found: '" + buffer.toString() + '\'');
							buffer.setLength(0);
						}
					} else {
						handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Expected 'i', found: '" + word + "'");
					}
				} else if (stage == 1) {
					if (prevcp != '*') {
						buffer.append(word);
					} else {
						unexpectedTokenError(index, word);
					}
				} else if (stage != STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE) {
					buffer.append(word);
				} else {
					unexpectedTokenError(index, word);
				}
			}
			prevcp = 65;
		}

		@Override
		public void separator(int index, int codepoint) {
			if (!parseError) {
				if (escapedTokenIndex != -1 && bufferEndsWithEscapedChar(buffer)) {
					buffer.append(' ');
					return;
				}
				if (prevcp == ':' || prevcp == '.' || prevcp == '#' || (prevcp == TokenProducer.CHAR_VERTICAL_LINE && getActiveSelector() == null)) {
					unexpectedCharError(index, codepoint);
					return;
				}
				if (stage == STAGE_ATTR_SYMBOL) {
					if (buffer.length() != 0) {
						setAttributeSelectorValue(index, unescapeBuffer(index));
						stage = STAGE_ATTR_POST_SYMBOL;
					}
				} else if (stage == 1 || stage == STAGE_EXPECT_ID_OR_CLASSNAME || stage == STAGE_EXPECT_PSEUDOELEM_NAME
						|| stage == STAGE_EXPECT_PSEUDOCLASS_NAME) {
					processBuffer(index, codepoint, false);
					if (prevcp == 65 || prevcp == 42 || prevcp == 41 || prevcp == 93) {
						// letter-or-digit, *, ), ]
						stage = STAGE_COMBINATOR_OR_END;
					}
				} else if (stage == STAGE_ATTR_START) {
					if (buffer.length() != 0) {
						stage = STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE;
					} else if (namespacePrefix != null) {
						unexpectedCharError(index, codepoint);
					}
					return;
				}
				if (prevcp != 44) {
					// If previous cp was a comma, we keep it
					prevcp = 32;
				}
			}
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteChar) {
			if (stage == STAGE_ATTR_SYMBOL && currentsel != null) { // Attribute selector
				setAttributeSelectorValue(index, quoted);
				stage = STAGE_ATTR_POST_SYMBOL;
			} else if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) { // Pseudo-class argument
				if (buffer.length() != 0 && isPrevCpWhitespace()) {
					buffer.append(' ');
				}
				char c = (char) quoteChar;
				buffer.append(c).append(quoted).append(c);
			} else {
				char c = (char) quoteChar;
				StringBuilder buf = new StringBuilder(quoted.length() + 2);
				buf.append(c).append(quoted).append(c);
				unexpectedTokenError(index, buf.toString());
			}
			prevcp = 65;
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteCp) {
			if (stage == STAGE_ATTR_SYMBOL && currentsel != null) { // Attribute selector
				setAttributeSelectorValue(index, quoted);
				stage = STAGE_ATTR_POST_SYMBOL;
			} else {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Quoted string contained unexpected control character: \"" + quoted + '"');
			}
			prevcp = 65;
		}

		private void setAttributeSelectorValue(int index, CharSequence value) {
			Condition cond = null;
			if (currentsel instanceof DescendantSelectorImpl) {
				Selector simple = ((DescendantSelectorImpl) currentsel).getSimpleSelector();
				if (!(simple instanceof ConditionalSelectorImpl)) {
					throw new IllegalStateException("Descendant selector has no conditional simple selector");
				}
				cond = ((ConditionalSelectorImpl) simple).condition;
			} else if (currentsel instanceof SiblingSelectorImpl) {
				Selector simple = ((SiblingSelectorImpl) currentsel).getSiblingSelector();
				if (!(simple instanceof ConditionalSelectorImpl)) {
					throw new IllegalStateException("Descendant selector has no conditional simple selector");
				}
				cond = ((ConditionalSelectorImpl) simple).condition;
			} else if (currentsel instanceof ConditionalSelectorImpl) {
				cond = ((ConditionalSelectorImpl) currentsel).condition;
			}
			if (cond instanceof CombinatorConditionImpl) {
				cond = ((CombinatorConditionImpl) cond).getSecondCondition();
			}
			if (cond instanceof AttributeConditionImpl) {
				AttributeConditionImpl attrcond = (AttributeConditionImpl) cond;
				if (attrcond != null) {
					if (attrcond.value == null) {
						attrcond.value = value.toString();
					} else {
						StringBuilder buf = new StringBuilder(attrcond.value.length() + value.length() + 1);
						buf.append(attrcond.value);
						if (isPrevCpWhitespace()) {
							buf.append(' ');
						}
						attrcond.value = buf.append(value).toString();
					}
					return;
				}
			}
			handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected token in selector: <" + value + ">");
		}

		private void setAttributeConditionFlag(AttributeCondition2.Flag flag) {
			Selector simple = getActiveSelector();
			if (simple == null || simple.getSelectorType() != Selector.SAC_CONDITIONAL_SELECTOR) {
				throw new IllegalStateException(
						"Processing attribute modifier of non-conditional selector");
			}
			Condition cond = ((ConditionalSelectorImpl) simple).getCondition();
			if (cond.getConditionType() == Condition.SAC_AND_CONDITION) {
				// If it wasn't the second condition, we would not be here
				cond = ((CombinatorCondition) cond).getSecondCondition();
			}
			// Unlikely to happen, but checking is nicer than class cast error
			if (!(cond instanceof AttributeConditionImpl)) {
				throw new IllegalStateException(
						"Processing attribute modifier of non-attribute conditional selector");
			}
			((AttributeConditionImpl) cond).setFlag(flag);
		}

		@Override
		public void openGroup(int index, int codepoint) {
			if (codepoint == 123) { // '{'
				handleLeftCurlyBracket(index);
			} else if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
				bufferAppend(codepoint);
				prevcp = codepoint;
				if (codepoint == 40) { // '('
					parendepth++;
				}
				return;
			} else {
				if (codepoint == 40) { // '('
					if (prevcp != 65 || buffer.length() == 0 || stage != STAGE_EXPECT_PSEUDOCLASS_NAME) {
						unexpectedCharError(index, codepoint);
					} else {
						newConditionalSelector(index, codepoint, Condition.SAC_PSEUDO_CLASS_CONDITION);
						if (!parseError) {
							stage = STAGE_EXPECT_PSEUDOCLASS_ARGUMENT;
							functionToken = true;
						}
					}
					parendepth++;
				} else if (codepoint == 91) { // '['
					if (prevcp != 65 && prevcp != 42 && prevcp != 44 && prevcp != 32 && prevcp != 93 && prevcp != 41
							&& prevcp != 43 && prevcp != 62 && prevcp != 125 && prevcp != 126 && prevcp != 124) {
						// Not letter-or-digit nor *,ws)]+}~|>
						unexpectedCharError(index, codepoint);
					} else {
						processBuffer(index, codepoint, false);
						stage = STAGE_ATTR_START;
						prevcp = 65;
						return;
					}
				}
			}
			prevcp = codepoint;
		}

		protected void handleLeftCurlyBracket(int index) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected '{'");
		}

		protected void handleRightCurlyBracket(int index) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected '}'");
		}

		@Override
		public void closeGroup(int index, int codepoint) {
			if (codepoint == 41) { // ')'
				parendepth--;
				if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
					if (parendepth == 0) {
						Selector sel = getActiveSelector();
						if (sel.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR) {
							Condition cond = ((ConditionalSelectorImpl) sel).condition;
							cond = getActiveCondition(cond);
							short condtype = cond.getConditionType();
							if (buffer.length() != 0) {
								if (condtype == Condition2.SAC_SELECTOR_ARGUMENT_CONDITION) {
									try {
										((SelectorArgumentConditionImpl) cond).arguments = parseSelectorArgument(
												rawBuffer(), factory);
									} catch (CSSParseException e) {
										byte errCode;
										if (e.getClass() == CSSNamespaceParseException.class) {
											errCode = ParseHelper.ERR_UNKNOWN_NAMESPACE;
										} else {
											errCode = ParseHelper.ERR_EXPR_SYNTAX;
										}
										CSSParseException ex = createException(index, errCode, e.getMessage());
										if (errorHandler != null) {
											errorHandler.error(ex);
											parseError = true;
											stage = 127;
										} else {
											throw ex;
										}
									}
								} else if (condtype == Condition.SAC_POSITIONAL_CONDITION) {
									if (((PositionalConditionImpl) cond).hasArgument()) {
										String arg = rawBuffer();
										if (!parsePositionalArgument((PositionalConditionImpl) cond, arg)) {
											handleError(index, ParseHelper.ERR_EXPR_SYNTAX,
													"Wrong subexpression: " + arg);
										}
									}
								} else if (condtype == Condition.SAC_LANG_CONDITION) {
									String s = unescapeBuffer(index);
									int len = s.length();
									if (s.charAt(len - 1) == ',') {
										handleError(index - 2, ParseHelper.ERR_UNEXPECTED_TOKEN,
												"Unexpected functional argument: " + s);
										return;
									}
									((LangConditionImpl) cond).lang = s;
								} else if (condtype == Condition.SAC_PSEUDO_CLASS_CONDITION) {
									String s = unescapeBuffer(index);
									if (!isValidIdentifier(s)) {
										handleError(index - s.length() - 1, ParseHelper.ERR_UNEXPECTED_TOKEN,
												"Unexpected functional argument: " + s);
										return;
									}
									((AttributeConditionImpl) cond).value = s;
								}
								buffer.setLength(0);
								stage = 1;
							} else {
								if (condtype == Condition.SAC_LANG_CONDITION
										|| condtype == Condition2.SAC_SELECTOR_ARGUMENT_CONDITION
										|| condtype == Condition.SAC_POSITIONAL_CONDITION) {
									unexpectedCharError(index, codepoint);
								} else {
									unexpectedCharError(index - 1, codepoint);
								}
							}
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else {
						bufferAppend(codepoint);
						prevcp = codepoint;
						return;
					}
				} else {
					unexpectedCharError(index, codepoint);
				}
				if (functionToken) {
					functionToken = false;
				}
			} else if (codepoint == 125) { // }
				handleRightCurlyBracket(index);
			} else if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
				bufferAppend(codepoint);
				prevcp = codepoint;
				return;
			} else if (codepoint == 93) { // ]
				if (stage == STAGE_ATTR_POST_SYMBOL) {
					if (buffer.length() != 0) {
						setAttributeSelectorValue(index, unescapeBuffer(index));
					}
					stage = 1;
				} else if (stage == STAGE_ATTR_START || stage == STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE) {
					if (buffer.length() != 0) {
						newConditionalSelector(index, codepoint, Condition.SAC_ATTRIBUTE_CONDITION);
						stage = 1;
					} else {
						// Error
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected ']', expected attribute name");
					}
				} else if (stage == STAGE_ATTR_SYMBOL) {
					if (buffer.length() != 0) {
						setAttributeSelectorValue(index, unescapeBuffer(index));
						stage = 1;
					} else {
						// Error
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected ']', expected attribute value");
					}
				} else {
					unexpectedCharError(index, codepoint);
				}
			}
			prevcp = codepoint;
		}

		private Selector getActiveSelector() {
			Selector sel;
			if (currentsel instanceof DescendantSelectorImpl) {
				sel = ((DescendantSelectorImpl) currentsel).getSimpleSelector();
			} else if (currentsel instanceof SiblingSelectorImpl) {
				sel = ((SiblingSelectorImpl) currentsel).getSiblingSelector();
			} else {
				sel = currentsel;
			}
			return sel;
		}

		private Condition getActiveCondition(Condition cond) {
			while (cond.getConditionType() == Condition.SAC_AND_CONDITION) {
				cond = ((CombinatorConditionImpl) cond).getSecondCondition();
			}
			return cond;
		}

		@Override
		String unescapeBuffer(int index) {
			String s;
			if (namespacePrefix == null) {
				s = unescapeStringValue(index);
			} else {
				handleError(index - namespacePrefix.length() - 1, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected token: " + namespacePrefix);
				namespacePrefix = null;
				s = "";
			}
			buffer.setLength(0);
			escapedTokenIndex = -1;
			return s;
		}

		private void processBuffer(int index, int triggerCp, boolean lastStage) {
			if (prevcp == 42) { // *
				if (currentsel == null || currentsel.getSelectorType() != Selector.SAC_ANY_NODE_SELECTOR) {
					setSimpleSelector(index, factory.getUniversalSelector(namespacePrefix));
				}
			} else if (stage == STAGE_COMBINATOR_OR_END) {
				if (!lastStage) {
					newDescendantSelector(Selector.SAC_DESCENDANT_SELECTOR);
					if (buffer.length() != 0) {
						// Type selectors are identifiers and could be escaped
						ElementSelectorImpl sel = newElementSelector(index);
						String raw = buffer.toString();
						String s = unescapeBuffer(index);
						if (isValidIdentifier(raw)) {
							sel.localName = s;
							stage = 1;
						} else {
							handleError(index - raw.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
									"Invalid identifier: " + raw);
							return;
						}
					}
				}
			} else if (buffer.length() != 0) {
				if (stage == 1) {
					ElementSelectorImpl sel = newElementSelector(index);
					String uri;
					if (namespacePrefix == null) {
						uri = getDefaultNamespaceURI();
					} else {
						uri = getNamespaceURI(index);
						if (parseError) {
							return;
						}
					}
					sel.namespaceUri = uri;
					String raw = buffer.toString();
					String s = unescapeBuffer(index);
					if (isValidIdentifier(raw)) {
						sel.localName = s;
					} else {
						handleError(index - raw.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
								"Invalid identifier: " + raw);
						return;
					}
				} else if (stage == STAGE_EXPECT_ID_OR_CLASSNAME) {
					String raw = buffer.toString();
					String s = unescapeBuffer(index);
					if (isValidIdentifier(raw)) {
						setAttributeSelectorValue(index, s);
						stage = 1;
					} else {
						handleError(index - raw.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
								"Invalid class name: " + raw);
						return;
					}
				} else if (stage == STAGE_EXPECT_PSEUDOCLASS_NAME) {
					newConditionalSelector(index, triggerCp, Condition.SAC_PSEUDO_CLASS_CONDITION);
					stage = 1;
				} else if (stage == STAGE_EXPECT_PSEUDOELEM_NAME) {
					newConditionalSelector(index, triggerCp, Condition2.SAC_PSEUDO_ELEMENT_CONDITION);
					stage = 1;
				} else if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
				} else if (stage == STAGE_ATTR_POST_SYMBOL) {
					setAttributeSelectorValue(index, unescapeBuffer(index));
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected: <" + buffer + ">");
					buffer.setLength(0);
				}
			} else if (namespacePrefix != null) {
				handleError(index - namespacePrefix.length() - 1, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected: " + namespacePrefix + "|");
				namespacePrefix = null;
			} else if (stage > 1 && stage != 11) {
				unexpectedCharError(index, triggerCp);
			}
		}

		@Override
		public void character(int index, int codepoint) {
			// @formatter:off
			//
			// stages (stage=1 means "found something", 2 means "ws after something"):
			//  tagname.classname[attr=value i]
			// 0|   1  |8|1      |4   |5|6    |1
			//  tagname tagname
			// 0|   1  |2
			// Stage  7: whitespace after stage 4
			// Stage  9: waiting for pseudo-element name
			// Stage 10: waiting for pseudo-class name
			// Stage 11: waiting for pseudo-class argument
			// Stage  2: waiting for possible descendant selector
			//
			// ! 33
			// # 35
			// % 37
			// + 43
			// , 44
			// . 46
			// : 58
			// ; 59
			// < 60
			// = 61
			// > 62
			// @ 64
			//
			// @formatter:on
			if (!skipCharacterHandling()) {
				if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
					// Special case: comma
					if (codepoint == 44) {
						if (prevcp == 44 || buffer.length() == 0) {
							unexpectedCharError(index, codepoint);
							return;
						}
					}
					if (isPrevCpWhitespace() && buffer.length() != 0) {
						buffer.append(' ');
					}
					bufferAppend(codepoint);
				} else if (stage == STAGE_ATTR_START || stage == STAGE_ATTR_EXPECT_SYMBOL_OR_CLOSE) {
					if (codepoint == TokenProducer.CHAR_VERTICAL_LINE) {
						if (stage == STAGE_ATTR_START) {
							if (namespacePrefix == null) {
								readNamespacePrefix(index, codepoint);
							} else {
								unexpectedCharError(index, codepoint);
							}
						} else if (prevcp != 65) {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 61) { // =
						// If we are in a '|=' case, we should have the attribute name in
						// namespacePrefix
						if (prevcp == TokenProducer.CHAR_VERTICAL_LINE && namespacePrefix != null
								&& buffer.length() == 0) {
							buffer.append(namespacePrefix);
							namespacePrefix = null;
						}
						// Process the buffer according to the previous character
						if (prevcp == TokenProducer.CHAR_VERTICAL_LINE) { // |
							newConditionalSelector(index, codepoint, Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 126) { // ~
							newConditionalSelector(index, codepoint, Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 36) { // $
							newConditionalSelector(index, codepoint, Condition2.SAC_ENDS_ATTRIBUTE_CONDITION);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 94) { // ^
							newConditionalSelector(index, codepoint, Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 42) { // *
							newConditionalSelector(index, codepoint, Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION);
							stage = STAGE_ATTR_SYMBOL;
						} else if (prevcp == 65) {
							newConditionalSelector(index, codepoint, Condition.SAC_ATTRIBUTE_CONDITION);
							stage = STAGE_ATTR_SYMBOL;
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (buffer.length() == 0) {
						if (codepoint != TokenProducer.CHAR_TILDE && codepoint != TokenProducer.CHAR_DOLLAR
								&& codepoint != TokenProducer.CHAR_CIRCUMFLEX_ACCENT
								&& codepoint != TokenProducer.CHAR_ASTERISK) {
							if (stage == STAGE_ATTR_START && ParseHelper.isValidXMLStartCharacter(codepoint)) {
								bufferAppend(codepoint);
								prevcp = 65;
								return;
							}
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint != TokenProducer.CHAR_TILDE && codepoint != TokenProducer.CHAR_DOLLAR
							&& codepoint != TokenProducer.CHAR_CIRCUMFLEX_ACCENT
							&& codepoint != TokenProducer.CHAR_ASTERISK) {
						if (stage == STAGE_ATTR_START && ParseHelper.isValidXMLCharacter(codepoint)) {
							bufferAppend(codepoint);
							prevcp = 65;
							return;
						}
						unexpectedCharError(index, codepoint);
					}
				} else if (codepoint == 42) { // *
					if (stage == 0) {
						stage = 1;
					} else if (stage == STAGE_COMBINATOR_OR_END) {
							newDescendantSelector(Selector.SAC_DESCENDANT_SELECTOR);
						((DescendantSelectorImpl) currentsel).simpleSelector = factory.getUniversalSelector(namespacePrefix);
					namespacePrefix = null;
						stage = 1;
					} else if (stage == 1 && namespacePrefix != null && prevcp == TokenProducer.CHAR_VERTICAL_LINE) {
						setSimpleSelector(index, factory.createUniversalSelector(getNamespaceURI(index)));
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else {
					if (prevcp == TokenProducer.CHAR_VERTICAL_LINE) {
						if (codepoint == TokenProducer.CHAR_VERTICAL_LINE) {
							handleColumnCombinator(index);
							prevcp = 32;
							return;
						}
						unexpectedCharError(index, codepoint);
						return;
					}
					if (codepoint == TokenProducer.CHAR_TILDE) { // ~
						if (stage == STAGE_COMBINATOR_OR_END) {
							stage = 1;
						} else {
							processBuffer(index, codepoint, false);
						}
						newSiblingSelector(index, Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR, codepoint);
					} else if (codepoint == 46) { // .
						if (stage != STAGE_EXPECT_ID_OR_CLASSNAME || buffer.length() != 0) {
							processBuffer(index, codepoint, false);
							newConditionalSelector(index, codepoint, Condition.SAC_CLASS_CONDITION);
							stage = STAGE_EXPECT_ID_OR_CLASSNAME;
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 35) { // #
						if (stage != STAGE_EXPECT_ID_OR_CLASSNAME || buffer.length() != 0) {
							processBuffer(index, codepoint, false);
							newConditionalSelector(index, codepoint, Condition.SAC_ID_CONDITION);
							stage = STAGE_EXPECT_ID_OR_CLASSNAME;
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 58) { // :
						if (prevcp == 58) {
							stage = STAGE_EXPECT_PSEUDOELEM_NAME;
						} else {
							processBuffer(index, codepoint, false);
							stage = STAGE_EXPECT_PSEUDOCLASS_NAME;
						}
					} else if (codepoint == 62) { // >
						if (stage == STAGE_COMBINATOR_OR_END) {
							stage = 1;
						}
						processBuffer(index, codepoint, false);
						if (stage < 2) {
							newDescendantSelector(index, Selector.SAC_CHILD_SELECTOR, codepoint);
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 43) { // +
						if (stage == STAGE_COMBINATOR_OR_END) {
							stage = 1;
						}
						processBuffer(index, codepoint, false);
						newSiblingSelector(index, Selector.SAC_DIRECT_ADJACENT_SELECTOR, codepoint);
					} else if (codepoint == TokenProducer.CHAR_VERTICAL_LINE) {
						// |
						if (stage == STAGE_EXPECT_ID_OR_CLASSNAME || stage == STAGE_EXPECT_PSEUDOCLASS_NAME
								|| stage == STAGE_EXPECT_PSEUDOELEM_NAME) {
							processBuffer(index, codepoint, false);
						} else if (stage == STAGE_COMBINATOR_OR_END) {
							stage = 1;
						} else if (stage == 1 && namespacePrefix == null) {
							readNamespacePrefix(index, codepoint);
						} else if (stage == 0 && namespacePrefix == null && buffer.length() == 0) {
							namespacePrefix = "";
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == 44) { // ,
						if (functionToken) {
							if (prevcp == 44) { // Consecutive commas
								unexpectedCharError(index, codepoint);
							} else {
								// Probably happening inside [] TODO better error checking
								buffer.append(',');
							}
						} else {
							processBuffer(index, codepoint, true);
							if (!parseError) {
								if (addCurrentSelector(index)) {
									stage = 0;
								} else {
									unexpectedCharError(index, codepoint);
								}
							}
						}
					} else if (codepoint == 64) { // @
						handleAtKeyword(index);
					} else if (codepoint == 45) { // -
						buffer.append('-');
					} else if (codepoint == 95) { // _
						buffer.append('_');
					} else {
						if (stage < 8) {
							if (stage == 0) {
								if (ParseHelper.isValidXMLStartCharacter(codepoint)) {
									bufferAppend(codepoint);
									stage = 1;
									prevcp = 65;
									return;
								}
							} else if (ParseHelper.isValidXMLCharacter(codepoint)) {
								bufferAppend(codepoint);
								prevcp = 65;
								return;
							}
						} else if (!isUnexpectedCharacter(codepoint)) {
							bufferAppend(codepoint);
							prevcp = 65;
							return;
						}
						unexpectedCharError(index, codepoint);
					}
				}
			}
			prevcp = codepoint;
		}

		boolean skipCharacterHandling() {
			return parseError;
		}

		private void handleColumnCombinator(int index) {
			if (stage == 1) {
				if (currentsel == null) {
					ElementSelectorImpl sel = newElementSelector(index);
					if (namespacePrefix != null) {
						sel.localName = namespacePrefix;
						namespacePrefix = null;
					} else if (buffer.length() != 0) {
						// Unclear whether this is reachable
						sel.localName = unescapeBuffer(index);
					} else {
						unexpectedCharError(index, TokenProducer.CHAR_VERTICAL_LINE);
						return;
					}
				}
				newDescendantSelector(index, Selector2.SAC_COLUMN_COMBINATOR_SELECTOR, TokenProducer.CHAR_VERTICAL_LINE);
			} else if (stage == 0 && buffer.length() == 0 && currentsel == null) {
				namespacePrefix = null;
				newDescendantSelector(index, Selector2.SAC_COLUMN_COMBINATOR_SELECTOR, TokenProducer.CHAR_VERTICAL_LINE);
			} else {
				unexpectedCharError(index, TokenProducer.CHAR_VERTICAL_LINE);
			}
		}

		/**
		 * Test whether the given code point represents a special CSS character that could be
		 * processed through this <code>character</code> method (thus excluding group delimiters
		 * like <code>(</code> and <code>)</code>, and code points previously tested.
		 * <p>
		 * Characters tested so far:
		 * 
		 * <pre>
		 *  * . # : > + ~ | , @ - _
		 * </pre>
		 * 
		 * @param cp
		 *            the code point to test.
		 * @return <code>true</code> if it is an untested character with special meaning, <code>false</code> otherwise.
		 */
		private boolean isUnexpectedCharacter(int cp) {
			return cp == 0x21 || cp == 0x24 || cp == 0x25 || cp == 0x26 || cp == 0x2f || (cp >= 0x3b && cp <= 0x3f)
					|| cp == 0x5e || cp == 0x60;
			// x21 !, x24 $, x25 %, x26 & x2f /, x3b ;, x3c <,
			// x3d =, x3e >, x3f ?, x5e ^, x60 `
		}

		private void readNamespacePrefix(int index, int codepoint) {
			if (prevcp == 65) {
				namespacePrefix = unescapeBuffer(index);
			} else if (prevcp == TokenProducer.CHAR_ASTERISK && buffer.length() == 0) {
				namespacePrefix = "*";
			} else {
				unexpectedCharError(index, codepoint);
			}
		}

		protected void handleAtKeyword(int index) {
			unexpectedCharError(index, 64);
		}

		private void newConditionalSelector(int index, int triggerCp, short condtype) {
			String name = rawBuffer();
			String lcname = name.toLowerCase(Locale.ROOT).intern();
			Condition condition;
			if (condtype == Condition.SAC_PSEUDO_CLASS_CONDITION) {
				// See if we can specify the pseudo class.
				if ("lang".equals(lcname)) {
					condition = factory.createCondition(Condition.SAC_LANG_CONDITION);
				} else if ("first-child".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createPositionalCondition();
				} else if ("nth-child".equals(lcname)) {
					condition = factory.createPositionalCondition(true);
				} else if ("last-child".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createPositionalCondition();
					((PositionalConditionImpl) condition).offset = 1;
					((PositionalConditionImpl) condition).forwardCondition = false;
				} else if ("nth-last-child".equals(lcname)) {
					condition = factory.createPositionalCondition(true);
					((PositionalConditionImpl) condition).offset = 1;
					((PositionalConditionImpl) condition).forwardCondition = false;
				} else if ("first-of-type".equals(lcname)) {
					condition = factory.createPositionalCondition();
					((PositionalConditionImpl) condition).oftype = true;
					((PositionalConditionImpl) condition).offset = 1;
				} else if ("last-of-type".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createPositionalCondition();
					((PositionalConditionImpl) condition).oftype = true;
					((PositionalConditionImpl) condition).offset = 1;
					((PositionalConditionImpl) condition).forwardCondition = false;
				} else if ("nth-of-type".equals(lcname)) {
					condition = factory.createPositionalCondition(true);
					((PositionalConditionImpl) condition).oftype = true;
				} else if ("nth-last-of-type".equals(lcname)) {
					condition = factory.createPositionalCondition(true);
					((PositionalConditionImpl) condition).oftype = true;
					((PositionalConditionImpl) condition).forwardCondition = false;
				} else if ("only-child".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createCondition(Condition.SAC_ONLY_CHILD_CONDITION);
				} else if ("only-of-type".equals(lcname)) {
					if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Positional pseudo-class cannot have argument");
						return;
					}
					condition = factory.createCondition(Condition.SAC_ONLY_TYPE_CONDITION);
				} else if ("not".equals(lcname) || "is".equals(lcname) || "has".equals(lcname)
						|| "where".equals(lcname)) {
					if (triggerCp != TokenProducer.CHAR_LEFT_PAREN) {
						StringBuilder buf = new StringBuilder(name.length() * 2 + 26);
						buf.append("Expected ':").append(name).append("(', found ':").append(name)
								.appendCodePoint(triggerCp).append('\'');
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, buf.toString());
						return;
					}
					condition = factory.createCondition(Condition2.SAC_SELECTOR_ARGUMENT_CONDITION);
					((SelectorArgumentConditionImpl) condition).setName(lcname);
				} else { // Other pseudo-classes
					if ("first-line".equals(lcname) || "first-letter".equals(lcname) || "before".equals(lcname)
							|| "after".equals(lcname)) {
						// Old-syntax pseudo-element
						condtype = Condition2.SAC_PSEUDO_ELEMENT_CONDITION;
					}
					condition = factory.createCondition(condtype);
				}
			} else {
				condition = factory.createCondition(condtype);
			}
			if (name.length() != 0 && condition instanceof AttributeConditionImpl) {
				switch (condition.getConditionType()) {
				case Condition.SAC_ATTRIBUTE_CONDITION:
				case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
				case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
				case Condition2.SAC_ENDS_ATTRIBUTE_CONDITION:
				case Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
				case Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION:
					if (namespacePrefix != null) {
						((AttributeConditionImpl) condition).namespaceURI = getNamespaceURI(index);
					}
				case Condition.SAC_PSEUDO_CLASS_CONDITION:
					if (isNotForbiddenIdentStart(name)) {
						((AttributeConditionImpl) condition).localName = safeUnescapeIdentifier(index, name).trim();
					} else {
						handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
								"Invalid pseudo-class: " + name);
						return;
					}
					break;
				case Condition2.SAC_PSEUDO_ELEMENT_CONDITION:
					if (!isValidIdentifier(name)) {
						handleError(index - name.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
								"Invalid pseudo-element: " + name);
						return;
					} else if (triggerCp == '(') {
						handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
								"Invalid pseudo-element declaration: " + name + '(');
						return;
					} else {
						((AttributeConditionImpl) condition).localName = safeUnescapeIdentifier(index, name);
					}
					break;
				default:
					((AttributeConditionImpl) condition).value = lcname;
				}
			}
			if (currentsel instanceof DescendantSelectorImpl) {
				Selector simple = ((DescendantSelectorImpl) currentsel).getSimpleSelector();
				if (simple != null && simple.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR) {
					CombinatorConditionImpl andcond = (CombinatorConditionImpl) factory
							.createCondition(Condition.SAC_AND_CONDITION);
					andcond.first = ((ConditionalSelectorImpl) simple).getCondition();
					andcond.second = condition;
					((DescendantSelectorImpl) currentsel).simpleSelector = factory
							.createConditionalSelector(((ConditionalSelectorImpl) simple).getSimpleSelector(), andcond);
				} else {
					((DescendantSelectorImpl) currentsel).simpleSelector = factory
							.createConditionalSelector(((DescendantSelectorImpl) currentsel).simpleSelector, condition);
				}
			} else if (currentsel instanceof SiblingSelectorImpl) {
				Selector simple = ((SiblingSelectorImpl) currentsel).getSiblingSelector();
				if (simple != null && simple.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR) {
					CombinatorConditionImpl andcond = (CombinatorConditionImpl) factory
							.createCondition(Condition.SAC_AND_CONDITION);
					andcond.first = ((ConditionalSelectorImpl) simple).getCondition();
					andcond.second = condition;
					((SiblingSelectorImpl) currentsel).siblingSelector = factory
							.createConditionalSelector(((ConditionalSelectorImpl) simple).getSimpleSelector(), andcond);
				} else {
					((SiblingSelectorImpl) currentsel).siblingSelector = factory
							.createConditionalSelector(((SiblingSelectorImpl) currentsel).siblingSelector, condition);
				}
			} else {
				if (currentsel != null && currentsel.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR) {
					CombinatorConditionImpl andcond = (CombinatorConditionImpl) factory
							.createCondition(Condition.SAC_AND_CONDITION);
					andcond.first = ((ConditionalSelectorImpl) currentsel).getCondition();
					andcond.second = condition;
					currentsel = factory.createConditionalSelector(
							((ConditionalSelectorImpl) currentsel).getSimpleSelector(), andcond);
				} else {
					currentsel = factory.createConditionalSelector((SimpleSelector) currentsel, condition);
				}
			}
		}

		private boolean parsePositionalArgument(PositionalConditionImpl cond, String expression) {
			AnBExpression expr = new MyAnBExpression();
			try {
				expr.parse(expression);
			} catch (IllegalArgumentException e) {
				return false;
			}
			cond.offset = expr.getOffset();
			cond.slope = expr.getStep();
			cond.ofList = expr.getSelectorList();
			cond.hasKeyword = expr.isKeyword();
			return true;
		}

		class MyAnBExpression extends AnBExpression {

			@Override
			protected SelectorList parseSelector(String selText) {
				CSSParser parser = new CSSParser();
				return parser.parseSelectors(selText, factory);
			}

		}

		private String getDefaultNamespaceURI() {
			String uri = nsMap.getNamespaceURI("");
			if (uri != null && factory != nsMap) {
				factory.registerNamespacePrefix("", uri);
			}
			return uri;
		}

		private String getNamespaceURI(int index) {
			String uri;
			if (namespacePrefix.length() != 0) {
				uri = nsMap.getNamespaceURI(namespacePrefix);
				if (uri != null) {
					if (factory != nsMap) {
						factory.registerNamespacePrefix(namespacePrefix, uri);
					}
				} else if (!namespacePrefix.equals("*")) {
					handleError(index - buffer.length() - namespacePrefix.length() - 1,
							ParseHelper.ERR_UNKNOWN_NAMESPACE, "Unknown namespace prefix: " + namespacePrefix);
				}
			} else {
				// |E (elements without a namespace)
				uri = "";
			}
			namespacePrefix = null;
			return uri;
		}

		private ElementSelectorImpl newElementSelector(int index) {
			ElementSelectorImpl elemsel = factory.createElementSelector();
			setSimpleSelector(index, elemsel);
			return elemsel;
		}

		private void setSimpleSelector(int index, SimpleSelector simple) {
			if (currentsel instanceof DescendantSelectorImpl) {
				((DescendantSelectorImpl) currentsel).simpleSelector = simple;
			} else if (currentsel instanceof SiblingSelectorImpl) {
				((SiblingSelectorImpl) currentsel).siblingSelector = simple;
			} else if (currentsel != null) {
				handleError(index - buffer.length(), ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected token after '" + currentsel.toString() + "': " + simple.toString());
			} else {
				currentsel = simple;
			}
		}

		protected void newDescendantSelector(int index, short type, int triggerCp) {
			if (currentsel != null && isValidCurrentSelector()) {
				newDescendantSelector(type);
			} else {
				unexpectedCharError(index, triggerCp);
			}
		}

		void newDescendantSelector(short type) {
			currentsel = factory.createDescendantSelector(type, currentsel);
			stage = 0;
		}

		protected void newSiblingSelector(int index, short type, int triggerCp) {
			if (currentsel != null && isValidCurrentSelector()) {
				newSiblingSelector(type);
			} else {
				unexpectedCharError(index, triggerCp);
			}
		}

		void newSiblingSelector(short type) {
			currentsel = factory.createSiblingSelector(type, currentsel);
			stage = 0;
		}

		@Override
		public void escaped(int index, int codepoint) {
			if (stage == STAGE_ATTR_START || stage == STAGE_ATTR_POST_SYMBOL || stage == STAGE_EXPECT_ID_OR_CLASSNAME
					|| stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT || stage == 0 || stage == STAGE_COMBINATOR_OR_END) {
				if (ParseHelper.isHexCodePoint(codepoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				} else if (stage == STAGE_EXPECT_PSEUDOCLASS_ARGUMENT) {
					Selector sel = getActiveSelector();
					if (sel.getSelectorType() == Selector.SAC_CONDITIONAL_SELECTOR) {
						Condition cond = ((ConditionalSelectorImpl) sel).condition;
						cond = getActiveCondition(cond);
						if (cond.getConditionType() == Condition2.SAC_SELECTOR_ARGUMENT_CONDITION) {
							buffer.append('\\');
						}
					}
				}
				bufferAppend(codepoint);
				if (stage == 0) {
					stage = 1;
				}
				prevcp = 65;
			} else if (stage == STAGE_ATTR_SYMBOL) {
				bufferAppend(codepoint);
				stage = STAGE_ATTR_POST_SYMBOL;
			} else {
				unexpectedCharError(index - 1, 92);
			}
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			if (stage == 0) {
				super.commented(index, commentType, comment);
			} else {
				separator(index, 12);
				if (prevcp != 44) {
					// If previous cp was a comma, we keep it
					prevcp = 12;
				}
			}
		}

		@Override
		public void endOfStream(int len) {
			processBuffer(len, 32, true);
			if (!parseError) {
				if (!addCurrentSelector(len)) {
					handleError(len, ParseHelper.ERR_UNEXPECTED_EOF, "Unexpected end of stream");
				}
			}
		}

		boolean addCurrentSelector(int index) {
			if (currentsel != null) {
				if (isValidCurrentSelector()) {
					this.selist.add(currentsel, index);
					currentsel = null;
					return true;
				} else {
					selist.clear();
					currentsel = null;
				}
			}
			return false;
		}

		boolean isValidCurrentSelector() {
			Selector last = null;
			switch (currentsel.getSelectorType()) {
			case Selector.SAC_CHILD_SELECTOR:
			case Selector.SAC_DESCENDANT_SELECTOR:
			case Selector2.SAC_COLUMN_COMBINATOR_SELECTOR:
				last = ((DescendantSelectorImpl) currentsel).getSimpleSelector();
				break;
			case Selector.SAC_DIRECT_ADJACENT_SELECTOR:
			case Selector2.SAC_SUBSEQUENT_SIBLING_SELECTOR:
				last = ((SiblingSelectorImpl) currentsel).getSiblingSelector();
				break;
			case Selector.SAC_CONDITIONAL_SELECTOR:
				Condition cond = ((ConditionalSelectorImpl) currentsel).getCondition();
				short condtype = cond.getConditionType();
				while (condtype == Condition.SAC_AND_CONDITION) {
					cond = ((CombinatorCondition) cond).getSecondCondition();
					if (cond == null) {
						return false;
					}
					condtype = cond.getConditionType();
				}
				switch (condtype) {
				case Condition.SAC_ATTRIBUTE_CONDITION:
				case Condition.SAC_PSEUDO_CLASS_CONDITION:
				case Condition2.SAC_PSEUDO_ELEMENT_CONDITION:
					if (((AttributeConditionImpl) cond).getLocalName() == null) {
						return false;
					}
					break;
				case Condition.SAC_CLASS_CONDITION:
				case Condition.SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION:
				case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
				case Condition2.SAC_BEGINS_ATTRIBUTE_CONDITION:
				case Condition2.SAC_ENDS_ATTRIBUTE_CONDITION:
				case Condition2.SAC_SUBSTRING_ATTRIBUTE_CONDITION:
					if (((AttributeConditionImpl) cond).getValue() == null) {
						return false;
					}
					break;
				case Condition.SAC_LANG_CONDITION:
					if (((LangConditionImpl) cond).getLang() == null) {
						return false;
					}
					break;
				case Condition2.SAC_SELECTOR_ARGUMENT_CONDITION:
					if (((SelectorArgumentConditionImpl) cond).getSelectors() == null) {
						return false;
					}
				}
			default:
				return true;
			}
			if (last == null) {
				return false;
			}
			return true;
		}

		@Override
		void resetHandler() {
			super.resetHandler();
			stage = 0;
			functionToken = false;
			escapedTokenIndex = -1;
			buffer.setLength(0);
			namespacePrefix = null;
			currentsel = null;
			// selist is cleared by 'error', but clearing could be needed here too
			// selist.clear();
		}

		@Override
		public void error(int index, byte errCode, CharSequence context) {
			if (errCode == TokenProducer.ERR_UNEXPECTED_END_QUOTED) {
				index -= context.length() + 1;
			}
			super.error(index, errCode, context);
			selist.clear();
		}

		@Override
		protected void handleError(int index, byte errCode, String message) {
			// We do not want to report secondary errors. Check for parse error state
			if (!parseError) {
				stage = 127;
				if (prevcp == endcp && endcp != -1) {
					throw createException(index, errCode,
							"Expected end of file, found " + new String(Character.toChars(prevcp)));
				} else {
					throw createException(index, errCode, message);
				}
			}
		}

	}

	private static boolean bufferEndsWithEscapedChar(StringBuilder buffer) {
		int len = buffer.length();
		if (len != 0) {
			int bufCp = buffer.codePointAt(len - 1);
			if (ParseHelper.isHexCodePoint(bufCp)) {
				for (int i = 2; i <= 6; i++) {
					bufCp = buffer.codePointAt(len - i);
					if (ParseHelper.isHexCodePoint(bufCp)) {
						continue;
					} else if (bufCp == 92) { // \
						return true;
					} else {
						break;
					}
				}
			}
		}
		return false;
	}

	class DeclarationRuleTokenHandler extends DeclarationTokenHandler {
		private String ruleFirstPart = null;

		DeclarationRuleTokenHandler(InputSource source, ShorthandDatabase propertyDatabase) {
			super(source, propertyDatabase, 0);
		}

		@Override
		void addWord(int index, CharSequence word) {
			if (prevcp == 64) { // Got an at-rule
				ruleFirstPart = word.toString().toLowerCase(Locale.ROOT);
				buffer.setLength(0);
			} else {
				super.addWord(index, word);
			}
		}

		@Override
		protected void handleAtKeyword(int index) {
			if (propertyName != null || buffer.length() != 0) {
				unexpectedCharError(index, 64);
			} else {
				// This should not be needed, but is done here to leave some 'trace'
				// in case this is not dealt with properly
				buffer.append('@');
			}
		}

		@Override
		protected void handleLeftCurlyBracket(int index) {
			if (!parseError) {
				String ruleSecondPart = null;
				if (buffer.length() != 0) {
					ruleSecondPart = unescapeBuffer(index);
				}
				prevcp = 32;
				((DeclarationRuleHandler) handler).startAtRule(ruleFirstPart, ruleSecondPart);
			}
		}

		@Override
		protected void handleRightCurlyBracket(int index) {
			if (!parseError) {
				((DeclarationRuleHandler) handler).endAtRule();
				resetHandler();
				ruleFirstPart = null;
			}
		}

		@Override
		public void character(int index, int codepoint) {
			if (getCurlyBracketDepth() != 0) {
				super.character(index, codepoint);
			} else {
				bufferAppend(codepoint);
				prevcp = codepoint;
			}
		}

		@Override
		protected void processBuffer(int index) {
			if (getCurlyBracketDepth() != 0) {
				super.processBuffer(index);
			}
		}

	}

	/**
	 * Small extension to SAC's {@link DocumentHandler} to deal with declaration rules.
	 */
	public interface DeclarationRuleHandler extends DocumentHandler {

		/**
		 * Marks the start of a declaration rule.
		 * 
		 * @param ruleName
		 *            the name of the rule.
		 * @param modifier
		 *            the modifier string (the contents of whatever is after the rule name and
		 *            before the style declaration), or <code>null</code> if no modifier was
		 *            found.
		 */
		void startAtRule(String ruleName, String modifier);

		/**
		 * Marks the end of a declaration rule.
		 */
		void endAtRule();

	}

	class PropertyTokenHandler extends DeclarationTokenHandler {
		PropertyTokenHandler(InputSource source) {
			super(source, null);
			this.propertyName = "";
		}

		PropertyTokenHandler(String propertyName) {
			super(null, ShorthandDatabase.getInstance());
			this.propertyName = propertyName;
		}

		@Override
		protected void handleAtKeyword(int index) {
			throw createException(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected '@'");
		}

		@Override
		protected void handleSemicolon(int index) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected ';'");
		}

		@Override
		public void endOfStream(int len) {
			if (parendepth != 0) {
				handleError(len, ParseHelper.ERR_UNMATCHED_PARENTHESIS, "Unmatched parenthesis");
			} else {
				processBuffer(len);
				if (getLexicalUnit() == null) {
					handleError(len, ParseHelper.ERR_EXPR_SYNTAX, "No value found");
				}
			}
		}

		@Override
		protected void handleError(int index, byte errCode, String message) {
			if (prevcp == endcp && endcp != -1) {
				throw createException(index, errCode,
						"Expected end of file, found " + new String(Character.toChars(prevcp)));
			} else {
				throw createException(index, errCode, message);
			}
		}
	}

	class DeclarationTokenHandler extends CSSTokenHandler {
		private LexicalUnitImpl lunit = null;
		private LexicalUnitImpl currentlu = null;
		String propertyName = null;
		private final ShorthandDatabase propertyDatabase;
		private boolean hexColor = false;
		private boolean unicodeRange = false;
		private boolean readPriority = false;
		private boolean priorityImportant = false;
		private int curlyBracketDepth;
		private int squareBracketDepth;
		private boolean functionToken = false;
		private final boolean flagIEValues;

		DeclarationTokenHandler(InputSource source, ShorthandDatabase propertyDatabase) {
			this(source, propertyDatabase, 1);
		}

		DeclarationTokenHandler(InputSource source, ShorthandDatabase propertyDatabase, int initialCurlyBracketDepth) {
			super(source);
			this.curlyBracketDepth = initialCurlyBracketDepth;
			flagIEValues = CSSParser.this.parserFlags.contains(Flag.IEVALUES);
			buffer = new StringBuilder(128);
			this.propertyDatabase = propertyDatabase;
		}

		LexicalUnit2 getLexicalUnit() {
			return parseError ? null : lunit;
		}

		int getCurlyBracketDepth() {
			return curlyBracketDepth;
		}

		@Override
		public void word(int index, CharSequence word) {
			if (!parseError) {
				if (readPriority) {
					if (ParseHelper.equalsIgnoreCase(word, "important")) {
						priorityImportant = true;
					} else {
						checkIEPrioHack(index, word.toString());
					}
				} else { // rest of cases are collected to buffer
					addWord(index, word);
				}
			}
			prevcp = 65; // A
		}

		void addWord(int index, CharSequence word) {
			if (prevcp == '\\') {
				buffer.append('\\');
			}
			buffer.append(word);
		}

		@Override
		public void openGroup(int index, int codepoint) {
			// If we reach here expecting hexColor or unicodeRange, we are in error
			if (hexColor || unicodeRange) {
				unexpectedCharError(index, codepoint);
			}
			if (codepoint == 40) { // '('
				parendepth++;
				if (!parseError) {
					if (prevcp != 65) {
						if (!functionToken) {
							// Not a function token
							unexpectedCharError(index, codepoint);
							prevcp = codepoint;
						} else if (buffer.length() == 0) {
							// Sub-values
							newLexicalUnit(LexicalUnit.SAC_SUB_EXPRESSION);
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
									"Unexpected token: " + buffer.toString());
							buffer.setLength(0);
						}
					} else {
						newFunction(index);
					}
				}
			} else if (codepoint == 123) { // '{'
				curlyBracketDepth++;
				handleLeftCurlyBracket(index);
			} else if (codepoint == 91) { // '['
				squareBracketDepth++;
				if (!parseError) {
					if (propertyName != null) {
						processBuffer(index);
						newLexicalUnit(LexicalUnit2.SAC_LEFT_BRACKET);
					} else {
						unexpectedCharError(index, codepoint);
					}
				}
			}
			prevcp = codepoint;
		}

		private void newFunction(int index) {
			LexicalUnitImpl lu;
			String name = unescapeBuffer(index);
			if ("url".equalsIgnoreCase(name)) {
				lu = newLexicalUnit(LexicalUnit.SAC_URI);
			} else if ("rgb".equalsIgnoreCase(name) || "rgba".equalsIgnoreCase(name)) {
				lu = newLexicalUnit(LexicalUnit.SAC_RGBCOLOR);
			} else if ("attr".equalsIgnoreCase(name)) {
				lu = newLexicalUnit(LexicalUnit.SAC_ATTR);
			} else if ("element".equalsIgnoreCase(name)) {
				lu = newLexicalUnit(LexicalUnit2.SAC_ELEMENT_REFERENCE);
				functionToken = true;
				return;
			} else if ("rect".equalsIgnoreCase(name)) {
				lu = newLexicalUnit(LexicalUnit.SAC_RECT_FUNCTION);
			} else if ("counter".equalsIgnoreCase(name)) {
				lu = newLexicalUnit(LexicalUnit.SAC_COUNTER_FUNCTION);
			} else if ("counters".equalsIgnoreCase(name)) {
				lu = newLexicalUnit(LexicalUnit.SAC_COUNTERS_FUNCTION);
			} else {
				lu = newLexicalUnit(LexicalUnit.SAC_FUNCTION);
			}
			lu.value = name;
			functionToken = true;
		}

		protected void handleLeftCurlyBracket(int index) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected '{'");
		}

		private LexicalUnitImpl newLexicalUnit(short unitType) {
			LexicalUnitImpl lu;
			if (functionToken) {
				if (currentlu.getLexicalUnitType() == LexicalUnit.SAC_URI) {
					// Special case
					lu = currentlu;
				} else {
					lu = new LexicalUnitImpl(unitType);
					currentlu.addFunctionParameter(lu);
					if (ParseHelper.isFunctionUnitType(unitType) || unitType == LexicalUnit.SAC_SUB_EXPRESSION) {
						currentlu = lu;
					}
				}
			} else {
				lu = new LexicalUnitImpl(unitType);
				if (currentlu != null) {
					currentlu.nextLexicalUnit = lu;
					lu.previousLexicalUnit = currentlu;
				}
				currentlu = lu;
				if (lunit == null) {
					lunit = lu;
				}
			}
			return lu;
		}

		@Override
		public void closeGroup(int index, int codepoint) {
			if (codepoint == 41) { // ')'
				processBuffer(index);
				parendepth--;
				if (functionToken) {
					checkFunction(index);
					if (currentlu.ownerLexicalUnit != null) {
						currentlu = currentlu.ownerLexicalUnit;
					} else {
						functionToken = false;
					}
				}
			} else if (codepoint == 125) { // }
				if (parendepth == 0 && squareBracketDepth == 0) {
					if (curlyBracketDepth == 1) {
						endOfPropertyDeclaration(index);
						handleRightCurlyBracket(index);
					} else if (curlyBracketDepth == 2) {
						buffer.setLength(0);
					} else {
						this.parseError = true;
					}
				}
				curlyBracketDepth--;
			} else if (codepoint == 93) { // ]
				squareBracketDepth--;
				if (!parseError) {
					if (propertyName != null) {
						processBuffer(index);
						newLexicalUnit(LexicalUnit2.SAC_RIGHT_BRACKET);
					} else {
						unexpectedCharError(index, codepoint);
					}
				}
			}
			prevcp = codepoint;
		}

		private void checkFunction(int index) {
			String name;
			short type = currentlu.getLexicalUnitType();
			// We allow empty functions only for URI and FUNCTION
			if (currentlu.parameters == null) {
				if (type != LexicalUnit.SAC_FUNCTION && type != LexicalUnit.SAC_URI
						&& type != LexicalUnit2.SAC_ELEMENT_REFERENCE) {
					unexpectedCharError(index, ')');
				}
				return;
			} else if (!lastParamIsOperand()) {
				unexpectedCharError(index, ')');
				return;
			}
			if ((type == LexicalUnit.SAC_RGBCOLOR && !isValidRGBColor()) || (type == LexicalUnit.SAC_FUNCTION
					&& ("hsl".equalsIgnoreCase((name = currentlu.getFunctionName())) || "hsla".equalsIgnoreCase(name))
					&& !isValidHSLColor())) {
				String s;
				try {
					s = "Wrong color: " + currentlu.toString();
				} catch (Exception e) {
					s = "Wrong color.";
				}
				handleError(index, ParseHelper.ERR_WRONG_VALUE, s);
			}
		}

		private boolean lastParamIsOperand() {
			short type = findLastValue(currentlu.parameters).getLexicalUnitType();
			return type != LexicalUnit.SAC_OPERATOR_COMMA && !typeIsAlgebraicOperator(type);
		}

		private boolean lastParamIsAlgebraicOperator() {
			short type = findLastValue(currentlu.parameters).getLexicalUnitType();
			return typeIsAlgebraicOperator(type);
		}

		private boolean typeIsAlgebraicOperator(short type) {
			return type == LexicalUnit.SAC_OPERATOR_PLUS || type == LexicalUnit.SAC_OPERATOR_MINUS
					|| type == LexicalUnit.SAC_OPERATOR_MULTIPLY || type == LexicalUnit.SAC_OPERATOR_SLASH;
		}

		private boolean isValidRGBColor() {
			LexicalUnitImpl lu = currentlu.parameters;
			short valCount = 0;
			short lastType = -1;
			boolean hasCommas = false;
			boolean hasNoCommas = false;
			do {
				short type = lu.getLexicalUnitType();
				if (type == LexicalUnit.SAC_OPERATOR_COMMA) {
					if (lastType == LexicalUnit.SAC_OPERATOR_COMMA || lastType == -1 || hasNoCommas) {
						return false;
					}
					hasCommas = true;
				} else if (type == LexicalUnit.SAC_INTEGER || type == LexicalUnit.SAC_PERCENTAGE) {
					if (hasCommas) {
						if (lastType != LexicalUnit.SAC_OPERATOR_COMMA) {
							return false;
						}
						if (valCount == 3) {
							int value = lu.getIntegerValue();
							if (value < 0 || value > 1) {
								return false;
							}
						}
					} else if (lastType != type) {
						if (lastType != -1) {
							if (valCount == 3) {
								if (lastType != LexicalUnit.SAC_OPERATOR_SLASH) {
									return false;
								}
								if (type == LexicalUnit.SAC_INTEGER) {
									int value = lu.getIntegerValue();
									if (value < 0 || value > 1) {
										return false;
									}
								} else { // %
									float value = lu.getFloatValue();
									if (value < 0 || value > 100f) {
										return false;
									}
								}
							} else if (lastType == LexicalUnit.SAC_PERCENTAGE) {
								// We tolerate zeroes mixed with percentages
								if (lu.getIntegerValue() != 0) {
									return false;
								}
							} else if (lastType == LexicalUnit.SAC_INTEGER) {
								if (lu.getPreviousLexicalUnit().getIntegerValue() != 0) {
									return false;
								}
							}
						}
					} else {
						hasNoCommas = true;
					}
					valCount++;
				} else if (type == LexicalUnit.SAC_OPERATOR_SLASH) {
					if (valCount != 3 || (lastType != LexicalUnit.SAC_INTEGER && lastType != LexicalUnit.SAC_PERCENTAGE)
							|| hasCommas) {
						return false;
					}
				} else if (type == LexicalUnit.SAC_REAL) {
					if ((lastType != LexicalUnit.SAC_OPERATOR_SLASH && lastType != LexicalUnit.SAC_OPERATOR_COMMA)
							|| valCount != 3) {
						return false;
					}
					valCount = 4;
				} else {
					return false;
				}
				lastType = type;
				lu = lu.nextLexicalUnit;
			} while (lu != null);
			return valCount == 3 || valCount == 4;
		}

		private boolean isValidHSLColor() {
			LexicalUnitImpl lu = currentlu.parameters;
			short pcntCount = 0;
			short lastType = -1;
			boolean hasCommas = false;
			boolean hasNoCommas = false;
			do {
				short type = lu.getLexicalUnitType();
				if (type == LexicalUnit.SAC_PERCENTAGE) {
					if (lastType == -1) {
						return false;
					}
					if (hasCommas) {
						if (lastType != LexicalUnit.SAC_OPERATOR_COMMA) {
							return false;
						}
					} else if (lastType == LexicalUnit.SAC_INTEGER || isAngleType(lastType)) {
						hasNoCommas = true;
					}
					pcntCount++;
				} else if (type == LexicalUnit.SAC_OPERATOR_COMMA) {
					if (lastType == LexicalUnit.SAC_OPERATOR_COMMA || lastType == -1 || hasNoCommas) {
						return false;
					}
					hasCommas = true;
				} else if (type == LexicalUnit.SAC_INTEGER) {
					if (lastType != -1) {
						int value;
						if ((lastType != LexicalUnit.SAC_OPERATOR_SLASH && lastType != LexicalUnit.SAC_OPERATOR_COMMA)
								|| pcntCount < 2 || (value = lu.getIntegerValue()) < 0 || value > 1) {
							return false;
						}
					}
				} else if (isAngleType(type)) {
					if (lastType != -1) {
						return false;
					}
				} else if (type == LexicalUnit.SAC_OPERATOR_SLASH) {
					if (pcntCount != 2 || lastType != LexicalUnit.SAC_PERCENTAGE || hasCommas) {
						return false;
					}
				} else if (type == LexicalUnit.SAC_REAL) {
					if ((lastType != LexicalUnit.SAC_OPERATOR_SLASH && lastType != LexicalUnit.SAC_OPERATOR_COMMA)
							|| pcntCount != 2) {
						return false;
					}
					pcntCount = 3;
				} else {
					return false;
				}
				lastType = type;
				lu = lu.nextLexicalUnit;
			} while (lu != null);
			return pcntCount >= 2;
		}

		private boolean isAngleType(short type) {
			return type == LexicalUnit.SAC_DEGREE || type == LexicalUnit.SAC_RADIAN || type == LexicalUnit.SAC_GRADIAN
					|| type == LexicalUnit2.SAC_TURN;
		}

		protected void handleRightCurlyBracket(int index) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected '}'");
		}

		@Override
		public void character(int index, int codepoint) {
			// ! 33
			// # 35
			// % 37
			// + 43
			// , 44
			// . 46
			// / 47
			// : 58
			// ; 59
			// < 60
			// = 61
			// > 62
			// @ 64
			if (functionToken && currentlu.getLexicalUnitType() == LexicalUnit.SAC_URI) {
				bufferAppend(codepoint);
			} else if (codepoint == 59) {
				handleSemicolon(index);
			} else if (!parseError) {
				if (propertyName == null) {
					if (codepoint == 45) { // -
						// TokenProducer is supposed to send only isolated '-'
						buffer.append('-');
						codepoint = 65;
					} else if (codepoint == 95) { // _
						// TokenProducer is supposed to send only isolated '_'
						buffer.append('_');
						codepoint = 65;
					} else if (codepoint == 58) { // :
						// Here we should have the property name in buffer
						if (buffer.length() != 0) {
							setPropertyName(index);
						} else {
							handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected ':'");
						}
					} else if (codepoint == 64) {
						handleAtKeyword(index);
					} else {
						badPropertyName(index, codepoint);
						return;
					}
				} else if (readPriority) {
					String compatText;
					if (codepoint == 33 && priorityImportant && parserFlags.contains(Flag.IEPRIOCHAR)
							&& (compatText = setFullIdentCompat()) != null) { // !
						warnIdentCompat(index, compatText);
						lunit.setUnitType(LexicalUnit2.SAC_COMPAT_PRIO);
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else if (codepoint == 44) { // ,
					if (!functionToken || currentlu.parameters == null || !addToIdentCompat()) {
						processBuffer(index);
					}
					newLexicalUnit(LexicalUnit.SAC_OPERATOR_COMMA);
				} else if (codepoint == 33) { // !
					if (!functionToken) {
						processBuffer(index);
						readPriority = true;
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else if (!hexColor) {
					if (codepoint == 45) { // -
						if (!unicodeRange) {
							if (functionToken) {
								processBuffer(index);
								if (currentlu.parameters == null || !lastParamIsAlgebraicOperator()) {
									newLexicalUnit(LexicalUnit.SAC_OPERATOR_MINUS);
								} else {
									unexpectedCharError(index, codepoint);
								}
								prevcp = codepoint;
								return;
							} else if (isCustomProperty()) {
								processBuffer(index);
								newCustomPropertyOperator(index, codepoint, LexicalUnit.SAC_OPERATOR_MINUS);
								prevcp = codepoint;
								return;
							}
						}
						buffer.append('-');
						codepoint = 65;
					} else if (!unicodeRange) {
						if (codepoint == 95) { // _
							buffer.append('_');
							codepoint = 65;
						} else if (codepoint == 46) { // .
							handleFullStop(index);
						} else if (codepoint == 37) { // %
							if (prevcp == 65 && Character.isDigit(buffer.charAt(buffer.length() - 1))) {
								buffer.append('%');
							} else {
								processBuffer(index);
								newLexicalUnit(LexicalUnit.SAC_OPERATOR_MOD);
							}
						} else if (codepoint == 35) { // #
							if (buffer.length() != 0) {
								if (functionToken
										&& currentlu.getLexicalUnitType() != LexicalUnit2.SAC_ELEMENT_REFERENCE) {
									buffer.append('#');
								} else {
									unexpectedCharError(index, codepoint);
								}
							} else if (currentlu == null
									|| currentlu.getLexicalUnitType() != LexicalUnit2.SAC_ELEMENT_REFERENCE) {
								hexColor = true;
							} else if (currentlu.value == null) {
								buffer.append('#');
							} else {
								unexpectedCharError(index, codepoint);
							}
						} else if (codepoint == 58) { // :
							// Progid hack ?
							handleColon(index);
						} else if (codepoint == 43) { // +
							// Are we in a unicode range ?
							char c;
							if (buffer.length() == 1 && ((c = buffer.charAt(0)) == 'U' || c == 'u')) {
								buffer.setLength(0);
								unicodeRange = true;
							} else if (!isPrevCpWhitespace() && (buffer.length() == 0
									|| (c = buffer.charAt(buffer.length() - 1)) != 'E' && c != 'e')) {
								if (functionToken) {
									processBuffer(index);
									if (currentlu.parameters == null || !lastParamIsAlgebraicOperator()) {
										newLexicalUnit(LexicalUnit.SAC_OPERATOR_PLUS);
									} else {
										unexpectedCharError(index, codepoint);
									}
								} else if (isCustomProperty()) {
									processBuffer(index);
									newCustomPropertyOperator(index, codepoint, LexicalUnit.SAC_OPERATOR_PLUS);
								} else {
									unexpectedCharError(index, codepoint);
								}
							} else {
								buffer.append('+');
								codepoint = 65;
							}
						} else if (codepoint == 47) { // '/'
							processBuffer(index);
							if (!functionToken || (currentlu.parameters != null && lastParamIsOperand())) {
								newLexicalUnit(LexicalUnit.SAC_OPERATOR_SLASH);
							} else {
								unexpectedCharError(index, codepoint);
							}
						} else if (functionToken) {
							if (codepoint == 42) { // '*'
								processBuffer(index);
								if (currentlu.parameters != null && lastParamIsOperand()) {
									newLexicalUnit(LexicalUnit.SAC_OPERATOR_MULTIPLY);
								} else {
									unexpectedCharError(index, codepoint);
								}
							} else if (codepoint == 61 && handleEqualsSignInsideFunction(index)) {
								prevcp = 65;
								return;
							} else {
								unexpectedCharError(index, codepoint);
							}
						} else if (isCustomProperty()) {
							if (codepoint == TokenProducer.CHAR_ASTERISK) { // '*'
								processBuffer(index);
								newCustomPropertyOperator(index, codepoint, LexicalUnit.SAC_OPERATOR_MULTIPLY);
							} else {
								unexpectedCharError(index, codepoint);
							}
						} else if (codepoint != TokenProducer.CHAR_COMMERCIAL_AT
								&& codepoint != TokenProducer.CHAR_QUESTION_MARK
								&& codepoint != TokenProducer.CHAR_ASTERISK) {
							bufferAppend(codepoint);
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (codepoint == TokenProducer.CHAR_QUESTION_MARK && buffer.length() < 6) {
						bufferAppend(codepoint);
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else {
					unexpectedCharError(index, codepoint);
				}
			}
			prevcp = codepoint;
		}

		private void setPropertyName(int index) {
			String raw = buffer.toString();
			if (escapedTokenIndex == -1) {
				if (isNotForbiddenIdentStart(raw)
						|| (raw.charAt(0) == '*' && CSSParser.this.parserFlags.contains(Flag.STARHACK))) {
					propertyName = raw;
					buffer.setLength(0);
					return;
				}
			} else if (isNotForbiddenIdentStart(raw)) {
				propertyName = unescapeBuffer(index);
				if (!parseError && !isValidIdentifier(propertyName)) {
					handleWarning(index - buffer.length(), ParseHelper.WARN_PROPERTY_NAME,
							"Suspicious property name: " + raw);
				}
				return;
			}
			handleError(index - buffer.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
					"Invalid property name: '" + raw + '\'');
		}

		private void newCustomPropertyOperator(int index, int codepoint, short operatorUnitType) {
			if (currentlu == null) {
				newLexicalUnit(operatorUnitType);
				return;
			} else {
				// This method is not being called if we are in calc()
				assert(currentlu.parameters == null);
				//
				short type;
				if (!typeIsAlgebraicOperator(type = currentlu.getLexicalUnitType())
						&& type != LexicalUnit.SAC_OPERATOR_COMMA) {
					newLexicalUnit(operatorUnitType);
					return;
				}
			}
			unexpectedCharError(index, codepoint);
		}

		private boolean handleEqualsSignInsideFunction(int index) {
			/*
			 * IE Hacks: progid / expression hack: check whether this is 'filter' property, or
			 * we are in 'expression' hack.
			 * Note: propertyName has already been checked as not-null here.
			 */
			if (flagIEValues && (this.propertyName.length() == 0 || this.propertyName.endsWith("filter")
					|| "expression".equalsIgnoreCase(currentlu.getFunctionName()))) {
				if (prevcp == 65 || isPrevCpWhitespace() || prevcp == TokenProducer.CHAR_RIGHT_SQ_BRACKET) {
					// Could be a MS gradient or expression
					LexicalUnitImpl lu;
					int buflen = buffer.length();
					if (buflen != 0) {
						if (escapedTokenIndex == -1) {
							buffer.append('=');
							String s = buffer.toString();
							newLexicalUnit(LexicalUnit2.SAC_COMPAT_IDENT).value = s;
							buffer.setLength(0);
							hexColor = false;
							warnIdentCompat(index - buflen, s);
							return true;
						}
					} else if ((lu = currentlu.parameters) != null) {
						// We are in functional context, find last argument
						lu = findLastValue(lu);
						// Add '=' to the last parameter if ident, or to buffer if not empty
						short lutype = lu.getLexicalUnitType();
						if (lutype == LexicalUnit.SAC_IDENT) {
							lu.setUnitType(LexicalUnit2.SAC_COMPAT_IDENT);
							String s = lu.getStringValue();
							lu.value += '=';
							warnIdentCompat(index - s.length(), s);
							return true;
						} else if (lutype == LexicalUnit2.SAC_COMPAT_IDENT) {
							lu.value += '=';
							return true;
						} else if (lutype == LexicalUnit2.SAC_RIGHT_BRACKET) {
							newLexicalUnit(LexicalUnit2.SAC_COMPAT_IDENT).value = "=";
							warnIdentCompat(index, "=");
							return true;
						}
					}
				}
			}
			return false;
		}

		private LexicalUnitImpl findLastValue(LexicalUnitImpl lu) {
			LexicalUnitImpl nextlu;
			while ((nextlu = lu.nextLexicalUnit) != null) {
				lu = nextlu;
			}
			return lu;
		}

		private boolean isCustomProperty() {
			return propertyName.startsWith("--");
		}

		/**
		 * If the latest processed value was a <code>SAC_COMPAT_IDENT</code>, add the contents of
		 * the current buffer -if any- to it.
		 * 
		 * @return <code>true</code> if the latest processed value was a <code>SAC_COMPAT_IDENT</code> and the
		 *         buffer was either empty or contained no escaped content.
		 */
		private boolean addToIdentCompat() {
			if (escapedTokenIndex == -1) {
				// We are in functional context, find last argument
				LexicalUnitImpl lu = findLastValue(currentlu.parameters);
				// Add buffer to the last parameter if ident
				short lutype = lu.getLexicalUnitType();
				if (lutype == LexicalUnit2.SAC_COMPAT_IDENT) {
					if (hexColor) {
						lu.value += '#';
						hexColor = false;
					}
					if (buffer.length() != 0) {
						lu.value += buffer;
						buffer.setLength(0);
					}
					prevcp = 65;
					return true;
				}
			}
			return false;
		}

		private boolean checkLastIdentCompat() {
			LexicalUnitImpl lu = currentlu.parameters;
			if (lu != null) {
				lu = findLastValue(lu);
				// Add buffer to the last parameter if compat ident
				if (lu.getLexicalUnitType() == LexicalUnit2.SAC_COMPAT_IDENT) {
					if (hexColor) {
						lu.value += '#';
						hexColor = false;
					}
					lu.value += buffer;
					buffer.setLength(0);
					return true;
				}
			}
			return false;
		}

		private void handleFullStop(int index) {
			if (prevcp == 65) {
				buffer.append('.');
			} else if (buffer.length() == 0) {
				LexicalUnitImpl lastValue;
				if (prevcp == 45 && functionToken && escapedTokenIndex == -1
						&& this.currentlu.parameters != null && (lastValue = findLastValue(currentlu.parameters))
								.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_MINUS) {
					LexicalUnitImpl prev = lastValue.previousLexicalUnit;
					if (prev != null) {
						prev.nextLexicalUnit = null;
					} else {
						currentlu.parameters = null;
					}
					buffer.append('-');
				}
				buffer.append('0').append('.');
			} else {
				handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected '.'");
			}
		}

		private void handleColon(int index) {
			int buflen = buffer.length();
			if (buflen != 0) {
				if (buflen != 6 || !flagIEValues || !ParseHelper.equalsIgnoreCase(buffer, "progid")) {
					handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected ':'");
				} else {
					buffer.append(':');
					handleWarning(index, ParseHelper.WARN_PROGID_HACK, "Progid hack applied");
				}
			}
		}

		protected void handleAtKeyword(int index) {
			unexpectedCharError(index, 64);
		}

		protected void handleSemicolon(int index) {
			if (curlyBracketDepth == 1 && parendepth == 0 && squareBracketDepth == 0)
				endOfPropertyDeclaration(index);
			else
				handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected ';'");
		}

		protected void badPropertyName(int index, int codepoint) {
			if (buffer.length() == 0 && codepoint == TokenProducer.CHAR_ASTERISK
					&& parserFlags.contains(Flag.STARHACK)) {
				// IE asterisk hack
				if (errorHandler != null) {
					errorHandler.warning(createException(index, ParseHelper.ERR_UNEXPECTED_CHAR,
							"Unexpected character: * (IE hack)"));
				}
				buffer.append('*');
			} else {
				unexpectedCharError(index, codepoint);
			}
		}

		protected void endOfPropertyDeclaration(int index) {
			if (propertyName != null) {
				processBuffer(index);
				if (!parseError) {
					if (lunit != null) {
						handler.property(propertyName, lunit, priorityImportant);
					} else {
						handleError(index, ParseHelper.ERR_EXPR_SYNTAX,
								"Found property name (" + propertyName + ") but no value");
					}
				}
				propertyName = null;
			} else if (buffer.length() != 0) {
				unexpectedTokenError(index, buffer);
			}
			// Reset other state fields
			resetHandler();
		}

		@Override
		void resetHandler() {
			super.resetHandler();
			lunit = null;
			currentlu = null;
			priorityImportant = false;
			readPriority = false;
			functionToken = false;
			hexColor = false;
			unicodeRange = false;
			buffer.setLength(0);
		}

		protected void processBuffer(int index) {
			if (parseError) {
				buffer.setLength(0);
				return;
			}
			int buflen = buffer.length();
			if (buflen != 0) {
				if (this.propertyName == null) {
					// Set the property name
					setPropertyName(index);
				} else if (readPriority) {
					String prio = rawBuffer();
					if ("important".equalsIgnoreCase(prio)) {
						priorityImportant = true;
					} else {
						checkIEPrioHack(index - buflen, prio);
					}
				} else if (unicodeRange) {
					parseUnicodeRange(index, buflen);
				} else if (functionToken) {
					if (currentlu.getLexicalUnitType() == LexicalUnit.SAC_URI) {
						// uri
						currentlu.value = rawBuffer();
					} else if (currentlu.getLexicalUnitType() == LexicalUnit2.SAC_ELEMENT_REFERENCE) {
						String s = unescapeStringValue(index);
						if (s.length() > 1 && s.charAt(0) == '#') {
							currentlu.value = s.substring(1);
						} else {
							handleError(index - buflen, ParseHelper.ERR_WRONG_VALUE, "Wrong element reference: " + s);
							functionToken = false;
						}
						buffer.setLength(0);
					} else if (escapedTokenIndex != -1 || !checkLastIdentCompat()) {
						if (!hexColor) {
							parseNonHexcolorValue(index);
						} else {
							if (!parseHexColor(buflen)) {
								handleError(index - buflen, ParseHelper.ERR_WRONG_VALUE, "Wrong color value #" + buffer);
							}
							buffer.setLength(0);
							hexColor = false;
						}
					}
				} else if (hexColor) {
					if (!parseHexColor(buflen)) {
						handleError(index - buflen, ParseHelper.ERR_WRONG_VALUE, "Wrong color value #" + buffer);
					}
					buffer.setLength(0);
					hexColor = false;
				} else {
					parseNonHexcolorValue(index);
				}
			} else if (hexColor) {
				handleError(index, ParseHelper.ERR_WRONG_VALUE, "Empty hex color value");
			} else if (unicodeRange) {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Bad unicode range");
			}
		}

		/**
		 * Parse a value that is not an hex color.
		 * 
		 * @param index the parsing index.
		 */
		private void parseNonHexcolorValue(int index) {
			// Unescape and check for unit
			String raw = buffer.toString();
			int buflen = raw.length();
			String cssText;
			String str;
			if (escapedTokenIndex != -1) {
				int escsz = index - escapedTokenIndex;
				int nonescLen = buflen - escsz;
				if (nonescLen <= 0) {
					try {
						str = unescapeIdentifier(index, raw);
					} catch (DOMNullCharacterException e) {
						// NULL characters are valid, but if we find them with IEVALUES set...
						if (flagIEValues) {
							setIdentCompat(index - buflen, raw);
							escapedTokenIndex = -1;
							buffer.setLength(0);
							return;
						} else {
							str = safeUnescapeIdentifier(index, raw);
						}
					}
					cssText = ParseHelper.escapeCssChars(ParseHelper.escapeBackslash(raw)).toString();
				} else {
					CharSequence rawPart = buffer.subSequence(0, nonescLen);
					cssText = buffer.substring(nonescLen);
					try {
						str = unescapeIdentifier(index, cssText);
						str = rawPart + str;
					} catch (DOMNullCharacterException e) {
						if (flagIEValues) {
							setIdentCompat(index - buflen, raw);
							escapedTokenIndex = -1;
							buffer.setLength(0);
							return;
						} else {
							str = safeUnescapeIdentifier(index, cssText);
							str = rawPart + str;
						}
					}
					rawPart = ParseHelper.escapeAllBackslash(rawPart);
					cssText = ParseHelper.escapeCssCharsAndFirstChar(rawPart) + cssText;
				}
				escapedTokenIndex = -1;
			} else {
				str = buffer.toString();
				cssText = ParseHelper.escapeCssCharsAndFirstChar(raw).toString();
			}
			createIdentifierOrNumberOrKeyword(index, raw, str, cssText);
		}

		private void createIdentifierOrNumberOrKeyword(int index, String raw, String ident, String cssText) {
			buffer.setLength(0);
			int len = ident.length();
			int i = len - 1;
			for (; i >= 0; i--) {
				int cp = ident.codePointAt(i);
				if (!Character.isLetter(cp) && cp != 37) { // Not letter nor %
					if (cp < 48 || cp > 57 || !parseNumber(index, ident, i + 1)) {
						// Either not ending in [0-9] range or not parsable as a number
						if (!newIdentifier(raw, ident, cssText)) {
							// Check for a single '+'
							if (raw.length() == 1 && raw.charAt(0) == '+') {
								newOperator(index, '+', LexicalUnit.SAC_OPERATOR_PLUS);
								return;
							} else {
								checkForIEValue(index, raw);
							}
						}
					}
					break;
				}
			}
			if (i == -1) {
				if (ident.equalsIgnoreCase("inherit")) {
					newLexicalUnit(LexicalUnit.SAC_INHERIT);
				} else if (ident.equalsIgnoreCase("initial")) {
					newLexicalUnit(LexicalUnit.SAC_IDENT).value = "initial";
				} else if (ident.equalsIgnoreCase("unset")) {
					newLexicalUnit(LexicalUnit.SAC_IDENT).value = "unset";
				} else {
					if (!newIdentifier(raw, ident, cssText)) {
						handleError(index - raw.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
								"Invalid identifier: " + raw);
					}
				}
			}
		}

		private void newOperator(int index, int codePoint, short operatorUnitType) {
			short type;
			if (this.currentlu == null) {
				if (isCustomProperty()) {
					newLexicalUnit(operatorUnitType);
					return;
				}
			} else if (currentlu.parameters != null) {
				if (lastParamIsOperand()) {
					newLexicalUnit(operatorUnitType);
					return;
				}
			} else if (isCustomProperty() && !typeIsAlgebraicOperator(type = currentlu.getLexicalUnitType())
					&& type != LexicalUnit.SAC_OPERATOR_COMMA) {
				newLexicalUnit(operatorUnitType);
				return;
			}
			unexpectedCharError(index, codePoint);
		}

		private void parseUnicodeRange(int index, int buflen) {
			LexicalUnitImpl lu1;
			LexicalUnitImpl lu2 = null;
			String s = rawBuffer();
			int idx = s.indexOf('-');
			if (idx == -1) {
				byte check = rangeLengthCheck(s);
				if (check == 1) {
					lu1 = new LexicalUnitImpl(LexicalUnit.SAC_INTEGER);
					lu1.intValue = Integer.parseInt(s, 16);
				} else if (check == 2) {
					lu1 = new LexicalUnitImpl(LexicalUnit2.SAC_UNICODE_WILDCARD);
					lu1.value = s;
				} else {
					handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN, "Invalid unicode range: " + s);
					return;
				}
			} else if (idx > 0 && idx < s.length() - 1) {
				String range1 = s.substring(0, idx);
				String range2 = s.substring(idx + 1);
				byte check = rangeLengthCheck(range1);
				if (check == 1) {
					lu1 = new LexicalUnitImpl(LexicalUnit.SAC_INTEGER);
					lu1.intValue = Integer.parseInt(range1, 16);
				} else {
					handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN, "Invalid unicode range: " + s);
					return;
				}
				check = rangeLengthCheck(range2);
				if (check == 1) {
					lu2 = new LexicalUnitImpl(LexicalUnit.SAC_INTEGER);
					lu2.intValue = Integer.parseInt(range2, 16);
				} else {
					handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN, "Invalid unicode range: " + s);
					return;
				}
			} else {
				handleError(index - buflen, ParseHelper.ERR_UNEXPECTED_TOKEN, "Invalid unicode range: " + s);
				return;
			}
			LexicalUnitImpl range = newLexicalUnit(LexicalUnit.SAC_UNICODERANGE);
			range.addFunctionParameter(lu1);
			if (lu2 != null) {
				range.addFunctionParameter(lu2);
			}
			unicodeRange = false;
		}

		private byte rangeLengthCheck(String range) {
			byte wildcardCount = 0;
			int len = range.length();
			if (len < 7) {
				for (int i = 0; i < len; i++) {
					if (range.charAt(i) == '?') {
						wildcardCount++;
					} else if (wildcardCount != 0) {
						return 0;
					}
				}
				if (wildcardCount == 0) {
					return (byte) 1;
				}
				if (wildcardCount != 6) {
					return (byte) 2;
				}
			}
			return 0;
		}

		private boolean parseNumber(int index, String s, int i) {
			String strnum;
			String unit = null;
			short unitType;
			if (i != s.length()) {
				unit = s.substring(i);
				unit = unit.trim().toLowerCase(Locale.ROOT).intern();
				unitType = ParseHelper.unitFromString(unit);
				strnum = s.substring(0, i);
			} else { // No unit
				if (s.lastIndexOf('.', i) == -1) {
					unitType = LexicalUnit.SAC_INTEGER;
				} else {
					unitType = LexicalUnit.SAC_REAL;
				}
				strnum = s;
			}
			if (unitType == LexicalUnit.SAC_INTEGER) {
				try {
					int intval = Integer.parseInt(strnum);
					LexicalUnitImpl lu = newLexicalUnit(unitType);
					lu.intValue = intval;
				} catch (NumberFormatException e) {
					return false;
				}
			} else {
				try {
					LexicalUnitImpl lu;
					float flval = Float.parseFloat(strnum);
					if (unitType == LexicalUnit.SAC_REAL && flval == 0f) {
						lu = newLexicalUnit(LexicalUnit.SAC_INTEGER);
						lu.intValue = (int) flval;
					} else {
						lu = newLexicalUnit(unitType);
						lu.floatValue = flval;
						if (unit != null) {
							lu.dimensionUnitText = unit;
						}
					}
				} catch (NumberFormatException e) {
					return false;
				}
			}
			return true;
		}

		private boolean parseHexColor(int buflen) {
			try {
				if (buflen == 3) {
					newLexicalUnit(LexicalUnit.SAC_RGBCOLOR);
					currentlu.value = "rgb";
					boolean prevft = functionToken;
					functionToken = true;
					parseHexComponent(0, 1, true);
					parseHexComponent(1, 2, true);
					parseHexComponent(2, 3, true);
					recoverOwnerUnit();
					functionToken = prevft;
				} else if (buflen == 6) {
					newLexicalUnit(LexicalUnit.SAC_RGBCOLOR);
					currentlu.value = "rgb";
					boolean prevft = functionToken;
					functionToken = true;
					parseHexComponent(0, 2, false);
					parseHexComponent(2, 4, false);
					parseHexComponent(4, 6, false);
					recoverOwnerUnit();
					functionToken = prevft;
				} else if (buflen == 8) {
					newLexicalUnit(LexicalUnit.SAC_RGBCOLOR);
					currentlu.value = "rgb";
					boolean prevft = functionToken;
					functionToken = true;
					parseHexComponent(0, 2, false);
					parseHexComponent(2, 4, false);
					parseHexComponent(4, 6, false);
					int comp = hexComponent(6, 8, false);
					newLexicalUnit(LexicalUnit.SAC_OPERATOR_SLASH);
					newLexicalUnit(LexicalUnit.SAC_REAL).floatValue = comp / 255f;
					recoverOwnerUnit();
					functionToken = prevft;
				} else if (buflen == 4) {
					newLexicalUnit(LexicalUnit.SAC_RGBCOLOR);
					currentlu.value = "rgb";
					boolean prevft = functionToken;
					functionToken = true;
					parseHexComponent(0, 1, true);
					parseHexComponent(1, 2, true);
					parseHexComponent(2, 3, true);
					int comp = hexComponent(3, 4, true);
					newLexicalUnit(LexicalUnit.SAC_OPERATOR_SLASH);
					newLexicalUnit(LexicalUnit.SAC_REAL).floatValue = comp / 255f;
					recoverOwnerUnit();
					functionToken = prevft;
				} else {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}

		private void parseHexComponent(int start, int end, boolean doubleDigit) {
			int comp = hexComponent(start, end, doubleDigit);
			newLexicalUnit(LexicalUnit.SAC_INTEGER).intValue = comp;
		}

		private int hexComponent(int start, int end, boolean doubleDigit) {
			String s;
			if (doubleDigit) {
				CharSequence seq = buffer.subSequence(start, end);
				s = new StringBuilder(2).append(seq).append(seq).toString();
			} else {
				s = buffer.substring(start, end);
			}
			return Integer.parseInt(s, 16);
		}

		private void recoverOwnerUnit() {
			currentlu.identCssText = "#" + buffer;
			if (currentlu.ownerLexicalUnit != null) {
				currentlu = currentlu.ownerLexicalUnit;
			}
		}

		private boolean newIdentifier(String raw, String ident, String cssText) {
			if (isNotForbiddenIdentStart(raw)) {
				if (propertyDatabase != null) {
					String lcident = ident.toLowerCase(Locale.ROOT);
					if (lcident != ident) {
						if (propertyDatabase.isShorthand(propertyName)) {
							// Only if no Custom Ident was previously found.
							if (!isPreviousValueCustomIdent()) {
								String[] longhands = propertyDatabase.getLonghandProperties(propertyName);
								for (int i = 0; i < longhands.length; i++) {
									if (isIdentifierValueOf(longhands[i], lcident)) {
										ident = lcident;
									}
								}
							}
						} else if (isIdentifierValueOf(propertyName, lcident)) {
							ident = lcident;
						}
					}
				}
				LexicalUnitImpl lu = newLexicalUnit(LexicalUnit.SAC_IDENT);
				lu.value = ident;
				lu.identCssText = cssText;
				return true;
			}
			return false;
		}

		private boolean isIdentifierValueOf(String propertyName, String lcident) {
			return propertyDatabase.isIdentifierValue(propertyName, lcident) || "none".equals(lcident);
		}

		private boolean isPreviousValueCustomIdent() {
			String s;
			return currentlu != null && currentlu.getLexicalUnitType() == LexicalUnit.SAC_IDENT
					&& (s = currentlu.getStringValue()) != s.toLowerCase(Locale.ROOT);
		}

		@Override
		public void quoted(int index, CharSequence quoted, int quoteChar) {
			if (!hexColor && !unicodeRange && !readPriority && propertyName != null) {
				processBuffer(index);
				if (!parseError) {
					String s = quoted.toString();
					LexicalUnitImpl lu = newLexicalUnit(LexicalUnit.SAC_STRING_VALUE);
					lu.value = safeUnescapeIdentifier(index, s);
					char c = (char) quoteChar;
					StringBuilder buf = new StringBuilder(s.length() + 2);
					buf.append(c).append(s).append(c);
					lu.identCssText = buf.toString();
					prevcp = 65;
				}
			} else {
				char c = (char) quoteChar;
				StringBuilder buf = new StringBuilder(quoted.length() + 2);
				buf.append(c).append(quoted).append(c);
				unexpectedTokenError(index, buf.toString());
			}
		}

		@Override
		public void quotedWithControl(int index, CharSequence quoted, int quoteChar) {
			if (!hexColor && !unicodeRange && !readPriority && propertyName != null) {
				processBuffer(index);
				if (!parseError) {
					String s = quoted.toString();
					LexicalUnitImpl lu = newLexicalUnit(LexicalUnit.SAC_STRING_VALUE);
					lu.value = safeUnescapeIdentifier(index, s);
					char c = (char) quoteChar;
					lu.identCssText = c + ParseHelper.escapeControl(s) + c;
					prevcp = 65;
				}
			} else {
				char c = (char) quoteChar;
				StringBuilder buf = new StringBuilder(quoted.length() + 2);
				buf.append(c).append(quoted).append(c);
				unexpectedTokenError(index, buf.toString());
			}
		}

		@Override
		public void escaped(int index, int codepoint) {
			if (!parseError) {
				if (unicodeRange || isEscapedContentError(index, codepoint)) {
					unexpectedCharError(index, codepoint);
				}
			}
		}

		private boolean isEscapedContext(int prevcp) {
			return prevcp == 65 || isPrevCpWhitespace() || prevcp == TokenProducer.CHAR_COLON
					|| prevcp == TokenProducer.CHAR_COMMA || prevcp == TokenProducer.CHAR_SEMICOLON
					|| prevcp == TokenProducer.CHAR_LEFT_CURLY_BRACKET;
		}

		private boolean isEscapedContentError(int index, int codepoint) {
			if (isEscapedContext(prevcp) && !hexColor) {
				if (ParseHelper.isHexCodePoint(codepoint)) {
					setEscapedTokenStart(index);
					buffer.append('\\');
				}
				prevcp = 65;
				bufferAppend(codepoint);
			} else if (flagIEValues && isIEHackSuffix(codepoint)
					&& (lunit != null || buffer.length() != 0)) {
				buffer.append('\\');
				bufferAppend(codepoint);
				String compatText = setFullIdentCompat();
				escapedTokenIndex = -1;
				if (compatText != null) {
					warnIdentCompat(index, compatText);
					prevcp = codepoint;
				} else {
					return true;
				}
			} else {
				return true;
			}
			return false;
		}

		@Override
		public void separator(int index, int codepoint) {
			if (!parseError) {
				if (escapedTokenIndex != -1 && bufferEndsWithEscapedCharOrWS(buffer)) {
					buffer.append(' ');
					return;
				}
				processBuffer(index);
			}
			prevcp = 32;
		}

		@Override
		protected void highControl(int index, int codepoint) {
			if (!parseError) {
				if (!hexColor && !unicodeRange && !readPriority && propertyName != null) {
					bufferAppend(codepoint);
				} else {
					handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected control: " + codepoint);
				}
			}
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			if (!parseError && buffer.length() == 0 && propertyName == null && curlyBracketDepth == 1 && parendepth == 0
					&& squareBracketDepth == 0) {
				super.commented(index, commentType, comment);
			} else {
				processBuffer(index);
			}
			prevcp = 12;
		}

		@Override
		public void endOfStream(int len) {
			if (parendepth != 0) {
				handleError(len, ParseHelper.ERR_UNMATCHED_PARENTHESIS, "Unmatched parenthesis");
			} else if (propertyName != null) {
				processBuffer(len);
				endOfPropertyDeclaration(len);
			} else if (buffer.length() != 0) {
				handleError(len, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected token: " + buffer);
			}
		}

		@Override
		public void error(int index, byte errCode, CharSequence context) {
			super.error(index, errCode, context);
			lunit = null;
		}

		private void checkIEPrioHack(int index, String prio) {
			String compatText;
			buffer.append('!').append(prio);
			if (parserFlags.contains(Flag.IEPRIO) && "ie".equals(prio) && (compatText = setFullIdentCompat()) != null) {
				warnIdentCompat(index, compatText);
			} else {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Invalid priority: " + prio);
			}
		}

		private void checkForIEValue(int index, String raw) {
			int rawlen = raw.length();
			if (!flagIEValues || rawlen <= 2 || raw.charAt(rawlen - 2) != '\\'
					|| !isIEHackSuffix(raw.codePointAt(rawlen - 1)) || !setIdentCompat(index - rawlen, raw)) {
				handleError(index - rawlen, ParseHelper.ERR_INVALID_IDENTIFIER, "Invalid identifier: " + raw);
			}
		}

		private boolean isIEHackSuffix(int codepoint) {
			return codepoint == '9' || codepoint == '0';
		}

		/**
		 * Attempts to set a compat identifier as the current working value.
		 * 
		 * @param index
		 *            the index at which the value was found.
		 * @param lastvalue
		 *            the contents of the buffer.
		 * @return <code>true</code> if the compat ident unit was set, <code>false</code> if an error was encountered in the
		 *         process and the unit was not set. An error must be flagged in that case.
		 */
		private boolean setIdentCompat(int index, String lastvalue) {
			if (currentlu != null) {
				String prev;
				try {
					prev = currentlu.toString();
				} catch (RuntimeException e) {
					lunit.reset();
					return false;
				}
				currentlu.reset();
				currentlu.value = prev + ' ' + lastvalue;
				currentlu.setUnitType(LexicalUnit2.SAC_COMPAT_IDENT);
			} else {
				newLexicalUnit(LexicalUnit2.SAC_COMPAT_IDENT).value = lastvalue;
			}
			warnIdentCompat(index, lastvalue);
			return true;
		}

		/**
		 * Attempts to set a compat identifier as the root working value.
		 * 
		 * @return the compat ident string, or null if an error was encountered when setting it.
		 *         An error must be flagged in that case.
		 */
		private String setFullIdentCompat() {
			String newval;
			if (!hexColor) {
				newval = rawBuffer();
			} else {
				hexColor = false;
				newval = '#' + rawBuffer();
			}
			if (lunit != null) {
				try {
					newval = lunit.toString() + newval;
				} catch (RuntimeException e) {
					return null;
				} finally {
					lunit.reset();
				}
				lunit.value = newval;
				lunit.setUnitType(LexicalUnit2.SAC_COMPAT_IDENT);
			} else {
				newLexicalUnit(LexicalUnit2.SAC_COMPAT_IDENT).value = newval;
			}
			return newval;
		}

		private void warnIdentCompat(int index, String ident) {
			handleWarning(index, ParseHelper.WARN_IDENT_COMPAT, "Found compat ident: " + ident);
		}

	}

	abstract class CSSTokenHandler implements TokenHandler {
		int line = 1;
		int prevlinelength = -1;
		private boolean foundCp13andNotYet10or12 = false;

		int prevcp = 32;
		int endcp = -1;
		short parendepth = 0;
		StringBuilder buffer;
		int escapedTokenIndex = -1;

		boolean parseError = false;

		private final InputSource source;

		CSSTokenHandler(InputSource source) {
			super();
			this.source = source;
		}

		@Override
		public void tokenControl(TokenControl control) {
		}

		@Override
		public void control(int index, int codepoint) {
			/*
			 * Replace any U+000D CARRIAGE RETURN (CR) code points, U+000C FORM FEED (FF) code points,
			 * or pairs of U+000D CARRIAGE RETURN (CR) followed by U+000A LINE FEED (LF), by a single
			 * U+000A LINE FEED (LF) code point.
			 */
			if (codepoint == 10) { // LF \n
				separator(index, codepoint);
				if (!foundCp13andNotYet10or12) {
					line++;
					prevlinelength = index;
				} else {
					foundCp13andNotYet10or12 = false;
					prevlinelength++;
				}
				prevcp = 10;
			} else if (codepoint == 12) { // FF
				separator(index, codepoint);
				if (!foundCp13andNotYet10or12) {
					line++;
				} else {
					foundCp13andNotYet10or12 = false;
				}
				prevlinelength = index;
				prevcp = 10;
			} else if (codepoint == 13) { // CR
				line++;
				prevlinelength = index;
				foundCp13andNotYet10or12 = true;
			} else if (codepoint == 9) { // TAB
				separator(index, codepoint);
			} else if (codepoint < 0x80) {
				unexpectedCharError(index, codepoint);
			} else {
				highControl(index, codepoint);
			}
		}

		protected void highControl(int index, int codepoint) {
			if (!parseError) {
				handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR, "Unexpected control: " + codepoint);
			}
		}

		/**
		 * Return true if previous codepoint is whitespace (codepoints 32 and 10).
		 * 
		 * @return true if previous codepoint is whitespace.
		 */
		boolean isPrevCpWhitespace() {
			return prevcp == 32 || prevcp == 10;
		}

		@Override
		public void quotedNewlineChar(int index, int codepoint) {
			if (codepoint == 10) { // LF \n
				if (prevcp != 13) {
					line++;
					prevlinelength = index;
				}
			} else if (codepoint == 12) { // FF
				line++;
				prevlinelength = index;
			} else if (codepoint == 13) { // CR
				line++;
				prevlinelength = index;
				prevcp = codepoint;
			}
		}

		@Override
		public void commented(int index, int commentType, String comment) {
			if (commentType == 0) {
				handler.comment(comment);
			}
			prevcp = 12;
		}

		void setEscapedTokenStart(int index) {
			if (escapedTokenIndex == -1) {
				escapedTokenIndex = index - 1;
			}
		}

		void bufferAppend(int codepoint) {
			buffer.appendCodePoint(codepoint);
		}

		String rawBuffer() {
			escapedTokenIndex = -1;
			String s = buffer.toString();
			buffer.setLength(0);
			return s;
		}

		String unescapeBuffer(int index) {
			String s = unescapeStringValue(index);
			buffer.setLength(0);
			escapedTokenIndex = -1;
			return s;
		}

		String unescapeStringValue(int index) {
			String s;
			if (escapedTokenIndex != -1) {
				int escsz = index - escapedTokenIndex;
				int rawlen = buffer.length() - escsz;
				if (rawlen <= 0) {
					s = safeUnescapeIdentifier(index, buffer.toString());
				} else {
					CharSequence rawseq = buffer.subSequence(0, rawlen);
					s = rawseq + safeUnescapeIdentifier(index, buffer.substring(rawlen));
				}
			} else {
				s = buffer.toString();
			}
			return s;
		}

		String safeUnescapeIdentifier(int index, String inputString) {
			return ParseHelper.unescapeStringValue(inputString, true, true);
		}

		String unescapeIdentifier(int index, String inputString) throws DOMNullCharacterException {
			return ParseHelper.unescapeStringValue(inputString, true, false);
		}

		/**
		 * Verify that the given identifier does not start in a way which is forbidden
		 * by the specification.
		 * <p>
		 * If the processing reached this, the rest of the identifier should be fine.
		 * </p>
		 * 
		 * @param s the identifier to test.
		 * @return true if it starts as a valid identifier.
		 */
		boolean isNotForbiddenIdentStart(String s) {
			char c = s.charAt(0);
			if (c != '-') {
				return !Character.isDigit(c) && c != '+';
			}
			return (s.length() > 1 && !Character.isDigit(c = s.charAt(1))) || c == '\\';
		}

		boolean isValidIdentifier(String s) {
			char c = s.charAt(0);
			if (c != '-') {
				return !Character.isDigit(c);
			}
			return (s.length() > 1 && !Character.isDigit(c = s.charAt(1))) || c == '\\';
		}

		void resetHandler() {
			prevcp = 32;
			parendepth = 0;
			parseError = false;
		}

		@Override
		public void error(int index, byte errCode, CharSequence context) {
			handleError(index, errCode, "Syntax error near " + context);
		}

		protected void handleError(int index, byte errCode, String message) {
			if (!parseError) {
				if (errorHandler != null) {
					if (prevcp == endcp) {
						errorHandler.fatalError(createException(index, errCode, "Expected end of file"));
					} else {
						errorHandler.error(createException(index, errCode, message));
					}
				} else {
					throw createException(index, errCode, message);
				}
				parseError = true;
			}
		}

		void unexpectedCharError(int index, int codepoint) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_CHAR,
					"Unexpected '" + new String(Character.toChars(codepoint)) + "'");
		}

		void unexpectedTokenError(int index, CharSequence token) {
			handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN, "Unexpected: " + token);
		}

		void handleWarning(int index, byte errCode, String message) {
			if (!parseError) {
				if (errorHandler != null) {
					errorHandler.warning(createException(index, errCode, message));
				}
			}
		}

		protected String getSourceURI() {
			if (source != null) {
				return source.getURI();
			}
			return null;
		}

		CSSParseException createException(int index, byte errCode, String message) {
			Locator locator = new LocatorImpl(line, index - prevlinelength, getSourceURI());
			if (errCode == ParseHelper.ERR_UNKNOWN_NAMESPACE) {
				return new CSSNamespaceParseException(message, locator);
			}
			return new CSSParseException(message, locator);
		}

		void endDocument() {
			if (handler != null) {
				handler.endDocument(source);
			}
		}

		class MySelectorListImpl extends SelectorListImpl {
			private static final long serialVersionUID = 1L;

			public boolean add(Selector sel, int index) {
				if (add(sel)) {
					return true;
				}
				if (errorHandler != null) {
					int selsz;
					try {
						selsz = sel.toString().length();
					} catch (RuntimeException e) {
						selsz = 1;
					}
					String message = "Duplicate selector in list";
					try {
						message += ": " + sel.toString();
					} catch (RuntimeException e) {
					}
					errorHandler.warning(createException(index - selsz, ParseHelper.WARN_DUPLICATE_SELECTOR, message));
				}
				return false;
			}

		}

	}

	static class LocatorImpl implements Locator {

		int line;
		int column;
		String uri;

		LocatorImpl(int line, int column, String uri) {
			super();
			this.line = line;
			this.column = column;
			this.uri = uri;
		}

		@Override
		public String getURI() {
			return uri;
		}

		@Override
		public int getLineNumber() {
			return line;
		}

		@Override
		public int getColumnNumber() {
			return column;
		}

	}

}
