package io.sf.carte.doc.style.css.parser;

import java.util.Locale;

import io.sf.carte.doc.DOMNullCharacterException;
import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.UnitStringToId;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.uparser.TokenProducer;

abstract class ValueTokenHandler extends BufferTokenHandler implements LexicalProvider {

	private static final FunctionFactories functionFactories = new FunctionFactories();

	/**
	 * The first unit in the lexical chain.
	 */
	private LexicalUnitImpl lunit = null;

	/**
	 * The lexical unit currently being processed.
	 */
	LexicalUnitImpl currentlu = null;

	private final CommentStore commentStore = createCommentStore();

	private final ShorthandDatabase propertyDatabase;

	private int squareBracketDepth;

	boolean functionToken = false;

	private final boolean flagIEValues;

	ValueTokenHandler() {
		super();
		this.flagIEValues = hasParserFlag(Parser.Flag.IEVALUES);
		this.propertyDatabase = ShorthandDatabase.getInstance();
	}

	ValueTokenHandler(LexicalProvider parent) {
		super();
		this.currentlu = parent.getCurrentLexicalUnit();
		this.lunit = this.currentlu;
		this.flagIEValues = parent.hasParserFlag(Parser.Flag.IEVALUES);
		this.propertyDatabase = ShorthandDatabase.getInstance();
	}

	@Override
	protected void initializeBuffer() {
		this.buffer = new StringBuilder(128);
	}

	protected CommentStore createCommentStore() {
		return new DefaultCommentStore(this);
	}

	@Override
	public boolean hasParserFlag(Parser.Flag flag) {
		return false;
	}

	LexicalUnitImpl getLexicalUnit() {
		return parseError ? null : lunit;
	}

	@Override
	public LexicalUnitImpl getCurrentLexicalUnit() {
		return currentlu;
	}

	@Override
	public boolean isFunctionOrExpressionContext() {
		return functionToken;
	}

	@Override
	public void setCurrentLexicalUnit(LexicalUnitImpl currentlu) {
		this.currentlu = currentlu;
		if (currentlu != null && lunit == null) {
			lunit = currentlu;
			LexicalUnitImpl prevlu = currentlu;
			while ((prevlu = prevlu.previousLexicalUnit) != null) {
				lunit = prevlu;
			}
		}
	}

	/**
	 * Add an {@code EMPTY} lexical unit at the end of the current lexical chain.
	 */
	@Override
	public void addEmptyLexicalUnit() {
		EmptyUnitImpl empty = new EmptyUnitImpl();
		addPlainLexicalUnit(empty);
	}

	int getSquareBracketDepth() {
		return squareBracketDepth;
	}

	boolean allowSemicolonArgument() {
		return "switch".equalsIgnoreCase(currentlu.value);
	}

	@Override
	public void leftParenthesis(int index) {
		parendepth++;
		if (prevcp != 65) {
			if (buffer.length() == 0) {
				if (isFunctionOrExpressionContext() || isCustomProperty()) {
					// Sub-values
					addFunctionOrExpressionUnit(new SubExpressionUnitImpl());
					functionToken = true;
				} else {
					unexpectedCharError(index, TokenProducer.CHAR_LEFT_PAREN);
				}
			} else {
				handleError(index, ParseHelper.ERR_UNEXPECTED_TOKEN,
						"Unexpected token before '(': " + buffer);
				buffer.setLength(0);
			}
			prevcp = TokenProducer.CHAR_LEFT_PAREN;
		} else {
			prevcp = 32;
			newFunction(index);
		}
	}

	private void newFunction(int index) {
		LexicalUnitImpl lu;
		String raw;
		String name = unescapeStringValue(index);
		String lcName = name.toLowerCase(Locale.ROOT);

		LexicalUnitFactory factory = functionFactories.getFactory(lcName);

		if (factory == null) {
			if (name.isEmpty()) {
				handleError(index, ParseHelper.ERR_WRONG_VALUE, "Unexpected character '('.");
				buffer.setLength(0);
				resetEscapedTokenIndex();
				return;
			} else if (CSSParser.isNotForbiddenIdentStart(raw = buffer.toString())) {
				if (name.charAt(0) == '-' && name.length() > 3) {
					/*
					 * If CSS Functions & Mixins is implemented, should check for custom functions
					 * (and add a CUSTOM_FUNCTION type).
					 */
					// Prefixed function or calc() (for example -o-calc())
					lu = addFunctionOrExpressionUnit(new PrefixedFunctionUnitImpl());
				} else if (lcName.endsWith("-gradient")) {
					name = lcName;
					lu = addFunctionOrExpressionUnit(
							new ImageFunctionUnitImpl(LexicalType.GRADIENT));
				} else {
					lu = addFunctionOrExpressionUnit(new GenericFunctionUnitImpl());
				}
				lu.value = name;
				functionToken = true;
			} else {
				handleError(index, ParseHelper.ERR_WRONG_VALUE, "Unexpected: " + raw);
				buffer.setLength(0);
				resetEscapedTokenIndex();
				return;
			}
		} else {
			lu = factory.createUnit();
			if (isFunctionOrExpressionContext()) {
				currentlu.addFunctionParameter(lu);
				currentlu = lu;
			} else {
				if (currentlu != null) {
					currentlu.nextLexicalUnit = lu;
					lu.previousLexicalUnit = currentlu;
				}
				currentlu = lu;
				if (lunit == null) {
					lunit = lu;
				}
				commentStore.setPrecedingComments(lu);
				functionToken = true;
			}

			lu.value = factory.canonicalName(lcName);
			factory.handle(this, index);
		}

		buffer.setLength(0);
		resetEscapedTokenIndex();
	}

	@Override
	public void leftSquareBracket(int index) {
		squareBracketDepth++;
		processBuffer(index, TokenProducer.CHAR_LEFT_SQ_BRACKET);
		commentStore.setTrailingComments();
		newLexicalUnit(LexicalType.LEFT_BRACKET);
		prevcp = 32;
	}

	@Override
	public void rightParenthesis(int index) {
		processBuffer(index, TokenProducer.CHAR_RIGHT_PAREN);
		decrParenDepth(index);
		if (isFunctionOrExpressionContext() && !isInError()) {
			checkFunction(index);
			endFunctionArgument(index);
		}
		commentStore.reset();
		prevcp = TokenProducer.CHAR_RIGHT_PAREN;
	}

	@Override
	public void endFunctionArgument(int index) {
		commentStore.setTrailingComments();
		if (currentlu.ownerLexicalUnit != null) {
			currentlu = currentlu.ownerLexicalUnit;
		} else {
			functionToken = false;
		}
	}

	/**
	 * Create a non-function (nor expression) lexical unit, add it as the current
	 * value.
	 * 
	 * @param unitType the unit type. Cannot be a function or expression.
	 * @return the lexical unit that should be processed as the current unit,
	 *         generally the newly created value.
	 */
	private LexicalUnitImpl newLexicalUnit(LexicalType unitType) {
		LexicalUnitImpl lu = new LexicalUnitImpl(unitType);
		return addPlainLexicalUnit(lu);
	}

	/**
	 * Add a non-function (nor expression) lexical unit as the current value.
	 * 
	 * @param lu the lexical unit to add.
	 * @return the lexical unit that should be processed as the current unit.
	 */
	@Override
	public LexicalUnitImpl addPlainLexicalUnit(LexicalUnitImpl lu) {
		commentStore.setPrecedingComments(lu);
		if (isFunctionOrExpressionContext()) {
			LexicalUnitImpl param = currentlu.parameters;
			if (param != null) {
				commentStore.setLastParameterTrailingComments(param);
				// Set preceding comments, just in case there was e.g. a comma
				commentStore.setPrecedingComments(lu);
			}
			currentlu.addFunctionParameter(lu);
		} else {
			if (currentlu != null) {
				currentlu.nextLexicalUnit = lu;
				lu.previousLexicalUnit = currentlu;
				commentStore.setTrailingComments(currentlu);
			}
			currentlu = lu;
			if (lunit == null) {
				lunit = lu;
			}
		}
		return lu;
	}

	private LexicalUnitImpl addFunctionOrExpressionUnit(LexicalUnitImpl lu) {
		commentStore.setPrecedingComments(lu);
		if (isFunctionOrExpressionContext()) {
			LexicalUnitImpl param = currentlu.parameters;
			if (param != null) {
				commentStore.setLastParameterTrailingComments(param);
				// Set preceding comments, just in case there was e.g. a comma
				commentStore.setPrecedingComments(lu);
			}
			currentlu.addFunctionParameter(lu);
		} else {
			if (currentlu != null) {
				currentlu.nextLexicalUnit = lu;
				lu.previousLexicalUnit = currentlu;
				commentStore.setTrailingComments(currentlu);
				// Set preceding comments, just in case there was e.g. a comma
				commentStore.setPrecedingComments(lu);
			}
			if (lunit == null) {
				lunit = lu;
			}
		}
		currentlu = lu;
		return lu;
	}

	private void checkFunction(int index) {
		LexicalType type = currentlu.getLexicalUnitType();
		// We allow empty functions only for URI, ELEMENT_REFERENCE and FUNCTION
		if (currentlu.parameters == null) {
			switch (type) {
			case URI:
				if (!currentlu.isParameter() && parendepth != 0) {
					unexpectedCharError(index, ')');
				}
				break;
			case FUNCTION:
				break;
			case ELEMENT_REFERENCE:
				if (currentlu.value != null) {
					break;
				}
			default:
				unexpectedCharError(index, ')');
			}
			return;
		}

		LexicalUnitFactory factory = functionFactories.getFactory(currentlu.getFunctionName());

		if (factory == null || factory.validate(this, index, currentlu)) {
			return;
		}

		// Report a generic error
		String s;
		try {
			s = "Wrong value: " + currentlu.toString();
		} catch (Exception e) {
			s = "Wrong value.";
		}
		handleError(index, ParseHelper.ERR_WRONG_VALUE, s);
	}

	private boolean isVarOrLastParamIsOperand() {
		if (currentlu.getLexicalUnitType() == LexicalType.VAR) {
			return true;
		}
		LexicalType type = CSSParser.findLastValue(currentlu.parameters).getLexicalUnitType();
		return type != LexicalType.OPERATOR_COMMA && !CSSParser.typeIsAlgebraicOperator(type);
	}

	private boolean lastParamIsAlgebraicOperator() {
		LexicalType type = CSSParser.findLastValue(currentlu.parameters).getLexicalUnitType();
		return CSSParser.typeIsAlgebraicOperator(type);
	}

	private boolean lastParamIsMultOrSlashOperator() {
		LexicalType type = CSSParser.findLastValue(currentlu.parameters).getLexicalUnitType();
		return type == LexicalType.OPERATOR_MULTIPLY || type == LexicalType.OPERATOR_SLASH;
	}

	@Override
	public void rightCurlyBracket(int index) {
		// End of declaration
		if (parendepth != 0 || squareBracketDepth != 0) {
			setParseError();
			parendepth = 0;
			squareBracketDepth = 0;
		} else {
			processBuffer(index, TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
		}
		commentStore.setTrailingComments();
		endOfPropertyDeclaration(index);
		getManager().rightCurlyBracket(index);
		prevcp = TokenProducer.CHAR_RIGHT_CURLY_BRACKET;
	}

	@Override
	public void rightSquareBracket(int index) {
		squareBracketDepth--;
		processBuffer(index, TokenProducer.CHAR_RIGHT_SQ_BRACKET);
		commentStore.setTrailingComments();
		newLexicalUnit(LexicalType.RIGHT_BRACKET);
		prevcp = TokenProducer.CHAR_RIGHT_SQ_BRACKET;
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
		if (codepoint == TokenProducer.CHAR_SEMICOLON) {
			handleSemicolon(index);
		} else if (!isInError()) {
			if (codepoint == 44) { // ,
				if (!functionToken || currentlu.parameters == null || !addToIdentCompat()) {
					processBuffer(index, codepoint);
					// Spare a isInError() call
				}
				newOperator(LexicalType.OPERATOR_COMMA);
			} else if (codepoint == TokenProducer.CHAR_EXCLAMATION) { // !
				if (!functionToken) {
					processBuffer(index, codepoint);
					if (!isInError()) {
						setPriorityHandler(index);
					}
				} else {
					unexpectedCharError(index, codepoint);
				}
			} else if (codepoint == 45) { // -
				if (prevcp != 65) {
					processBuffer(index, codepoint);
				}
				buffer.append('-');
				codepoint = 65;
			} else if (codepoint == 95) { // _
				buffer.append('_');
				codepoint = 65;
			} else if (codepoint == 46) { // .
				handleFullStop(index);
			} else if (codepoint == 37) { // %
				if (prevcp == 65 && CSSParser.isDigit(buffer.charAt(buffer.length() - 1))) {
					buffer.append('%');
				} else {
					processBuffer(index, codepoint);
					newOperator(LexicalType.OPERATOR_MOD);
				}
			} else if (codepoint == 35) { // #
				if (buffer.length() == 0) {
					// Handle hex color
					yieldHandling(new HexColorTH(this));
					prevcp = 65;
					return;
				} else {
					unexpectedCharError(index, codepoint);
				}
			} else if (codepoint == 58) { // :
				// Nested pseudo-class/element or Progid hack ?
				handleColon(index);
			} else if (codepoint == 43) { // +
				// Are we in a unicode range ?
				char c;
				if (buffer.length() == 1 && ((c = buffer.charAt(0)) == 'U' || c == 'u')) {
					assert prevcp == 65;
					buffer.setLength(0);
					handleUnicodeRange();
					prevcp = 32;
					return;
				} else if (buffer.length() == 0
						|| (c = buffer.charAt(buffer.length() - 1)) != 'E' && c != 'e') {
					// No scientific notation
					if (functionToken) {
						processBuffer(index, codepoint);
						boolean prevCpWS = isPrevCpWhitespace();
						if (((prevCpWS && currentlu.getLexicalUnitType() == LexicalType.CALC)
								|| flagIEValues) && currentlu.parameters != null
								&& !lastParamIsAlgebraicOperator()) {
							// We are either in calc() plus operator context
							// or in IE compatibility
							newOperator(LexicalType.OPERATOR_PLUS);
						} else if (prevCpWS || currentlu.parameters == null
								|| lastParamIsMultOrSlashOperator()) {
							// We are in sign context
							buffer.append('+');
							codepoint = 65;
						} else {
							unexpectedCharError(index, codepoint);
						}
					} else if (isPrevCpWhitespace()) {
						buffer.append('+');
						codepoint = 65;
					} else if (isCustomProperty()) {
						processBuffer(index, codepoint);
						newCustomPropertyOperator(index, codepoint, LexicalType.OPERATOR_PLUS);
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else {
					buffer.append('+');
					codepoint = 65;
				}
			} else if (codepoint == 47) { // '/'
				processBuffer(index, codepoint);
				if (!functionToken || (currentlu.parameters != null && (isVarOrLastParamIsOperand()
						|| currentlu.getLexicalUnitType() == LexicalType.ATTR))) {
					newOperator(LexicalType.OPERATOR_SLASH);
				} else {
					unexpectedCharError(index, codepoint);
				}
			} else if (functionToken) {
				if (codepoint == TokenProducer.CHAR_ASTERISK) { // '*'
					processBuffer(index, codepoint);
					if (currentlu.parameters != null && isVarOrLastParamIsOperand()) {
						newOperator(LexicalType.OPERATOR_MULTIPLY);
					} else {
						unexpectedCharError(index, codepoint);
					}
				} else if (codepoint == 61 && handleEqualsSignInsideFunction(index)) {
					codepoint = 65;
				} else {
					unexpectedCharError(index, codepoint);
				}
			} else if (isCustomProperty()) {
				if (codepoint == TokenProducer.CHAR_ASTERISK) { // '*'
					processBuffer(index, codepoint);
					newCustomPropertyOperator(index, codepoint, LexicalType.OPERATOR_MULTIPLY);
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
		}
		prevcp = codepoint;
	}

	private void handleSemicolon(int index) {
		if (squareBracketDepth == 0 && parendepth >= 0) {
			if (!isInError()) { // Could be in unexpected ; error
				processBuffer(index, TokenProducer.CHAR_SEMICOLON);
				commentStore.setTrailingComments();
			}
			if (parendepth > 0) {
				if (!isInError()) {
					if (isFunctionOrExpressionContext() && allowSemicolonArgument()) {
						newOperator(LexicalType.OPERATOR_SEMICOLON);
					} else {
						// Force error recovery
						unexpectedCharError(index, ';');
					}
				}
			} else if (!isInError()) {
				endOfValue(index);
			} else {
				// Resume handling
				resetHandler();
				resetParseError();
				getManager().restoreInitialHandler();
			}
		} else {
			unexpectedCharError(index, ';');
		}
	}

	protected void setPriorityHandler(int index) {
		unexpectedCharError(index, TokenProducer.CHAR_EXCLAMATION);
	}

	protected void endOfValue(int index) {
		endOfPropertyDeclaration(index);
	}

	protected void endOfPropertyDeclaration(int index) {
	}

	private void newCustomPropertyOperator(int index, int codepoint, LexicalType operator) {
		if (currentlu == null) {
			newOperator(operator);
			return;
		} else {
			// This method is not being called if we are in calc()
			assert currentlu.parameters == null;

			LexicalType type;
			if (!CSSParser.typeIsAlgebraicOperator(type = currentlu.getLexicalUnitType())
					&& type != LexicalType.OPERATOR_COMMA) {
				newOperator(operator);
				return;
			}
		}
		unexpectedCharError(index, codepoint);
	}

	private boolean handleEqualsSignInsideFunction(int index) {
		/*
		 * IE Hacks: progid / expression hack: check whether this is 'filter' property,
		 * or we are in 'expression' hack. Note: propertyName has already been checked
		 * as not-null here.
		 */
		if (flagIEValues && (getPropertyName().isEmpty() || getPropertyName().endsWith("filter")
				|| "expression".equalsIgnoreCase(currentlu.getFunctionName()))) {
			if (prevcp == 65 || isPrevCpWhitespace()
					|| prevcp == TokenProducer.CHAR_RIGHT_SQ_BRACKET) {
				// Could be a MS gradient or expression
				LexicalUnitImpl lu;
				int buflen = buffer.length();
				if (buflen != 0) {
					if (!isEscapedIdent()) {
						buffer.append('=');
						String s = buffer.toString();
						newLexicalUnit(LexicalType.COMPAT_IDENT).value = s;
						buffer.setLength(0);
						warnIdentCompat(index - buflen, s);
						return true;
					}
				} else if ((lu = currentlu.parameters) != null) {
					// We are in functional context, find last argument
					lu = CSSParser.findLastValue(lu);
					// Add '=' to the last parameter if ident, or to buffer if not empty
					LexicalType lutype = lu.getLexicalUnitType();
					if (lutype == LexicalType.IDENT) {
						lu.setUnitType(LexicalType.COMPAT_IDENT);
						String s = lu.getStringValue();
						lu.value += '=';
						warnIdentCompat(index - s.length(), s);
						return true;
					} else if (lutype == LexicalType.COMPAT_IDENT) {
						lu.value += '=';
						return true;
					} else if (lutype == LexicalType.RIGHT_BRACKET) {
						newLexicalUnit(LexicalType.COMPAT_IDENT).value = "=";
						warnIdentCompat(index, "=");
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * If the latest processed value was a <code>COMPAT_IDENT</code>, add the
	 * contents of the current buffer -if any- to it.
	 * 
	 * @return <code>true</code> if the latest processed value was a
	 *         <code>COMPAT_IDENT</code> and the buffer was either empty or
	 *         contained no escaped content.
	 */
	private boolean addToIdentCompat() {
		if (!isEscapedIdent()) {
			// We are in functional context, find last argument
			LexicalUnitImpl lu = CSSParser.findLastValue(currentlu.parameters);
			// Add buffer to the last parameter if ident
			LexicalType lutype = lu.getLexicalUnitType();
			if (lutype == LexicalType.COMPAT_IDENT) {
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
			lu = CSSParser.findLastValue(lu);
			// Add buffer to the last parameter if compat ident
			if (lu.getLexicalUnitType() == LexicalType.COMPAT_IDENT) {
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
			if (prevcp == 45 && isFunctionOrExpressionContext() && !isEscapedIdent()
					&& this.currentlu.parameters != null
					&& (lastValue = CSSParser.findLastValue(currentlu.parameters))
							.getLexicalUnitType() == LexicalType.OPERATOR_MINUS) {
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
		if (flagIEValues && buflen == 6 && ParseHelper.equalsIgnoreCase(buffer, "progid")) {
			buffer.append(':');
			handleWarning(index, ParseHelper.WARN_PROGID_HACK, "Progid hack applied");
		} else if (functionToken) {
			unexpectedCharError(index, TokenProducer.CHAR_COLON);
		} else {
			handlePseudo(index);
		}
	}

	protected void handlePseudo(int index) {
		unexpectedCharError(index, TokenProducer.CHAR_COLON);
	}

	/**
	 * Get a handler for nested selectors.
	 * 
	 * @param index the index.
	 * @return the handler, or {@code null} if cannot handle.
	 */
	protected BufferTokenHandler nestedSelectorHandler(int index) {
		return null;
	}

	private void handleUnicodeRange() {
		yieldHandling(new UnicodeRangeTH(this));
	}

	@Override
	protected void processBuffer(int index, int triggerCp) {
		// XXX next block can probably be removed
		// This can be reached in error due to unexpected ';' or EOF
		// In those cases, buffer should be empty.
		if (parseError) {
			buffer.setLength(0);
			return;
		}
		int buflen = buffer.length();
		if (buflen != 0
				&& (!isFunctionOrExpressionContext() || isEscapedIdent() || !checkLastIdentCompat())) {
			parseNonHexcolorValue(index);
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

		if (isEscapedIdent()) {
			// We are in escaped context
			int escsz = index - getEscapedTokenIndex();
			int nonescLen = buflen - escsz;
			if (nonescLen <= 0) {
				try {
					str = unescapeIdentifier(index, raw);
					cssText = ParseHelper.safeEscape(str, true, true);
				} catch (DOMNullCharacterException e) {
					// NULL characters are valid, but if we find them with IEVALUES set...
					if (flagIEValues) {
						setIdentCompat(index - buflen, raw);
						resetEscapedTokenIndex();
						buffer.setLength(0);
						return;
					} else {
						str = CSSParser.safeUnescapeIdentifier(raw);
						cssText = safeNullEscape(raw);
					}
				}
			} else {
				CharSequence rawPart = buffer.subSequence(0, nonescLen);
				cssText = buffer.substring(nonescLen);
				try {
					str = unescapeIdentifier(index, cssText);
					cssText = ParseHelper.safeEscape(str, true, true);
				} catch (DOMNullCharacterException e) {
					if (flagIEValues) {
						setIdentCompat(index - buflen, raw);
						resetEscapedTokenIndex();
						buffer.setLength(0);
						return;
					} else {
						str = CSSParser.safeUnescapeIdentifier(cssText);
						cssText = safeNullEscape(cssText);
					}
				}
				str = rawPart + str;
				rawPart = ParseHelper.escapeAllBackslash(rawPart);
				cssText = ParseHelper.escapeCssCharsAndFirstChar(rawPart) + cssText;
			}
			resetEscapedTokenIndex();
			if (!createIdentifierOrKeyword(index, raw, str, cssText)) {
				checkForIEValue(index, raw);
			}
		} else {
			str = buffer.toString();
			cssText = ParseHelper.escapeCssCharsAndFirstChar(raw).toString();
			createIdentifierOrNumberOrKeyword(index, raw, str, cssText);
		}
		buffer.setLength(0);
	}

	private static String unescapeIdentifier(int index, String inputString)
			throws DOMNullCharacterException {
		return ParseHelper.unescapeStringValue(inputString, true, false);
	}

	private void createIdentifierOrNumberOrKeyword(int index, String raw, String ident,
			String cssText) {
		// Unless the first character is whitespace, try parsing a numeric value
		int cp = ident.codePointAt(0);
		if (cp != 32) {
			int len = ident.length();
			int i = len - 1;
			for (; i >= 0; i--) {
				cp = ident.codePointAt(i);
				if (!Character.isLetter(cp) && cp != 37) { // Not letter nor %
					// Either not ending in [0-9] range or not parsable as a number
					if ((cp < 48 || cp > 57 || !parseNumber(index, ident, i + 1))
							&& !newIdentifier(raw, ident, cssText)) {
						// Check for a single '+' or '-'
						if (raw.length() == 1) {
							char c = raw.charAt(0);
							if (c == '+') {
								newOperator(index, '+', LexicalType.OPERATOR_PLUS);
								return;
							} else if (c == '-') {
								newOperator(index, '-', LexicalType.OPERATOR_MINUS);
								return;
							}
						} else {
							checkForIEValue(index, raw);
						}
					}
					break;
				}
			}
			if (i != -1) {
				// We are done
				return;
			}
		}

		if (!createIdentifierOrKeyword(index, raw, ident, cssText)) {
			handleError(index - raw.length(), ParseHelper.ERR_INVALID_IDENTIFIER,
					"Invalid identifier: " + raw);
		}
	}

	private boolean parseNumber(int index, String s, int i) {
		String unit = null;
		LexicalUnitImpl lu;
		if (i != s.length()) {
			// Parse number
			String strnum = s.substring(0, i);
			float flval;
			try {
				flval = Float.parseFloat(strnum);
			} catch (NumberFormatException e) {
				return false;
			}

			// Unit
			unit = s.substring(i);
			unit = unit.trim().toLowerCase(Locale.ROOT);
			short cssUnit = UnitStringToId.unitFromString(unit);
			final LexicalType unitType;
			if (cssUnit == CSSUnit.CSS_PERCENTAGE) {
				unitType = LexicalType.PERCENTAGE;
			} else {
				unitType = LexicalType.DIMENSION;
			}

			// Create a new dimension/percentage lexical unit
			lu = newLexicalUnit(unitType);
			lu.floatValue = flval;
			lu.dimensionUnitText = unit;
			lu.setCssUnit(cssUnit);
		} else { // No unit
			if (s.lastIndexOf('.', i) == -1) {
				int intval;
				try {
					intval = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					// Maybe it is exponent syntax ("1E2")
					float flval;
					try {
						flval = Float.parseFloat(s);
					} catch (NumberFormatException e1) {
						return false;
					}
					lu = newNumberUnit(LexicalType.REAL);
					lu.floatValue = flval;
					return true;
				}
				lu = newNumberUnit(LexicalType.INTEGER);
				lu.intValue = intval;
			} else {
				float flval;
				try {
					flval = Float.parseFloat(s);
				} catch (NumberFormatException e) {
					return false;
				}
				if (flval == 0f) {
					lu = newNumberUnit(LexicalType.INTEGER);
					lu.intValue = (int) flval;
				} else {
					lu = newNumberUnit(LexicalType.REAL);
					lu.floatValue = flval;
				}
			}
		}
		return true;
	}

	private void newOperator(int index, int codePoint, LexicalType operator) {
		LexicalType type;
		if (this.currentlu == null) {
			if (isCustomProperty()) {
				newOperator(operator);
				return;
			}
		} else if (currentlu.parameters != null) {
			if (isVarOrLastParamIsOperand()) {
				newOperator(operator);
				return;
			}
		} else if (isCustomProperty()
				&& !CSSParser.typeIsAlgebraicOperator(type = currentlu.getLexicalUnitType())
				&& type != LexicalType.OPERATOR_COMMA) {
			newOperator(operator);
			return;
		}
		unexpectedCharError(index, codePoint);
	}

	private LexicalUnitImpl newOperator(LexicalType operator) {
		LexicalUnitImpl lu = new OperatorUnitImpl(operator);
		return addPlainLexicalUnit(lu);
	}

	protected boolean isCustomProperty() {
		return false;
	}

	private boolean createIdentifierOrKeyword(int index, String raw, String ident, String cssText) {
		if (ident.equalsIgnoreCase("inherit")) {
			newLexicalUnit(LexicalType.INHERIT);
		} else if (ident.equalsIgnoreCase("initial")) {
			newLexicalUnit(LexicalType.INITIAL);
		} else if (ident.equalsIgnoreCase("unset")) {
			newLexicalUnit(LexicalType.UNSET);
		} else if (ident.equalsIgnoreCase("revert")) {
			newLexicalUnit(LexicalType.REVERT);
		} else {
			return newIdentifier(raw, ident, cssText);
		}
		return true;
	}

	private boolean newIdentifier(String raw, String ident, String cssText) {
		if (CSSParser.isNotForbiddenIdentStart(raw)) {
			if (propertyDatabase != null) {
				String lcident = ident.toLowerCase(Locale.ROOT);
				if (lcident != ident) {
					if (propertyDatabase.isShorthand(getPropertyName())) {
						// Only if no Custom Ident was previously found.
						if (!isPreviousValueCustomIdent()) {
							String[] longhands = propertyDatabase
									.getLonghandProperties(getPropertyName());
							for (String longhand : longhands) {
								if (isIdentifierValueOf(longhand, lcident)) {
									ident = lcident;
								}
							}
						}
					} else if (isIdentifierValueOf(getPropertyName(), lcident)) {
						ident = lcident;
					}
				}
			}
			LexicalUnitImpl lu = newLexicalUnit(LexicalType.IDENT);
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
		return currentlu != null && currentlu.getLexicalUnitType() == LexicalType.IDENT
				&& (s = currentlu.getStringValue()) != s.toLowerCase(Locale.ROOT);
	}

	private String safeNullEscape(String raw) {
		CharSequence seq = ParseHelper.escapeCssChars(ParseHelper.escapeBackslash(raw));
		// Add a whitespace to \0 if there isn't
		String cssText;
		int seqlen = seq.length();
		if (seq.charAt(seqlen - 1) == '0') {
			StringBuilder sb = new StringBuilder(seqlen + 1);
			sb.append(seq).append(' ');
			cssText = sb.toString();
		} else {
			cssText = seq.toString();
		}
		return cssText;
	}

	private void checkForIEValue(int index, String raw) {
		int rawlen = raw.length();
		if (!flagIEValues || rawlen <= 2 || raw.charAt(rawlen - 2) != '\\'
				|| !isIEHackSuffix(raw.codePointAt(rawlen - 1))
				|| !setIdentCompat(index - rawlen, raw)) {
			handleError(index - rawlen, ParseHelper.ERR_INVALID_IDENTIFIER,
					"Invalid identifier: " + raw);
		}
	}

	private boolean isIEHackSuffix(int codepoint) {
		return codepoint == '9' || codepoint == '0';
	}

	/**
	 * Attempts to set a compat identifier as the current working value.
	 * 
	 * @param index     the index at which the value was found.
	 * @param lastvalue the contents of the buffer.
	 * @return <code>true</code> if the compat ident unit was set,
	 *         <code>false</code> if an error was encountered in the process and the
	 *         unit was not set. An error must be flagged in that case.
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
			StringList preceding = currentlu.getPrecedingComments();
			currentlu.reset();
			LexicalUnitImpl lu = new LexicalUnitImpl(LexicalType.COMPAT_IDENT);
			lu.value = prev + ' ' + lastvalue;
			if (currentlu == lunit) {
				lunit = lu;
			} else {
				currentlu.replaceBy(lu);
			}
			currentlu = lu;
			lu.addPrecedingComments(preceding);
		} else {
			newLexicalUnit(LexicalType.COMPAT_IDENT).value = lastvalue;
		}
		warnIdentCompat(index, lastvalue);
		return true;
	}

	/**
	 * Attempts to set a compat identifier as the root working value.
	 * 
	 * @param rawBuffer the raw buffer.
	 * @return the compat ident string, or null if an error was encountered when
	 *         setting it. An error must be flagged in that case.
	 */
	String setFullIdentCompat(String rawBuffer) {
		String newval = rawBuffer;
		if (lunit != null) {
			StringList preceding = lunit.getPrecedingComments();
			try {
				newval = lunit.toString() + newval;
			} catch (RuntimeException e) {
				return null;
			} finally {
				lunit.reset();
			}
			lunit = new LexicalUnitImpl(LexicalType.COMPAT_IDENT);
			lunit.value = newval;
			lunit.addPrecedingComments(preceding);
		} else {
			newLexicalUnit(LexicalType.COMPAT_IDENT).value = newval;
		}
		return newval;
	}

	void warnIdentCompat(int index, String ident) {
		handleWarning(index, ParseHelper.WARN_IDENT_COMPAT, "Found compat ident: " + ident);
	}

	private LexicalUnitImpl newNumberUnit(LexicalType sacType) {
		LexicalUnitImpl lu = newLexicalUnit(sacType);
		lu.setCssUnit(CSSUnit.CSS_NUMBER);
		return lu;
	}

	@Override
	public void quoted(int index, CharSequence quoted, int quoteChar) {
		processBuffer(index, quoteChar);
		if (!isInError()) {
			String s = quoted.toString();
			LexicalUnitImpl lu = newLexicalUnit(LexicalType.STRING);
			if (lu.value != null) {
				handleError(index, ParseHelper.ERR_WRONG_VALUE,
						"Unexpected string: " + quoteChar + quoted + quoteChar);
			}
			lu.value = CSSParser.safeUnescapeIdentifier(s);
			char c = (char) quoteChar;
			StringBuilder buf = new StringBuilder(s.length() + 2);
			buf.append(c).append(s).append(c);
			lu.identCssText = buf.toString();
			prevcp = 65;
		}
	}

	@Override
	public void quotedWithControl(int index, CharSequence quoted, int quoteChar) {
		processBuffer(index, quoteChar);
		if (!isInError()) {
			String s = quoted.toString();
			LexicalUnitImpl lu = newLexicalUnit(LexicalType.STRING);
			if (lu.value != null) {
				handleError(index, ParseHelper.ERR_WRONG_VALUE,
						"Unexpected string: " + quoteChar + quoted + quoteChar);
			}
			lu.value = CSSParser.safeUnescapeIdentifier(s);
			char c = (char) quoteChar;
			StringBuilder buf = new StringBuilder(s.length() + 2);
			buf.append(c).append(ParseHelper.escapeControl(s)).append(c);
			lu.identCssText = buf.toString();
			prevcp = 65;
		}
	}

	@Override
	public void escaped(int index, int codepoint) {
		if (isEscapedContentError(index, codepoint)) {
			unexpectedCharError(index, codepoint);
		}
	}

	private boolean isEscapedContentError(int index, int codepoint) {
		if (isEscapedContext(prevcp) && !isLastValueHexColor()) {
			// We add a backslash if is an hex, \ (0x5c), + (0x2b) , - (0x2d)
			// or whitespace (0x20) to avoid confusions with numbers and
			// operators
			if (isEscapedCodepoint(codepoint)) {
				setEscapedTokenStart(index);
				buffer.append('\\');
			}
			prevcp = 65;
			bufferAppend(codepoint);
		} else if (flagIEValues && isIEHackSuffix(codepoint) // \9 \0
				&& (lunit != null || buffer.length() != 0)) {
			buffer.append('\\');
			bufferAppend(codepoint);
			String compatText = setFullIdentCompat(rawBuffer());
			resetEscapedTokenIndex();
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

	// This check exists to avoid escaped content where it doesn't belong
	private boolean isEscapedContext(int prevcp) {
		return prevcp == 65 || isPrevCpWhitespace() || prevcp == TokenProducer.CHAR_COLON
				|| prevcp == TokenProducer.CHAR_COMMA || prevcp == TokenProducer.CHAR_SEMICOLON
				|| prevcp == TokenProducer.CHAR_LEFT_CURLY_BRACKET;
	}

	private boolean isLastValueHexColor() {
		return currentlu != null && currentlu.getLexicalUnitType() == LexicalType.RGBCOLOR
				&& currentlu.identCssText != null;
	}

	@Override
	public void separator(int index, int codepoint) {
		if (getEscapedTokenIndex() != -1 && CSSParser.bufferEndsWithEscapedChar(buffer)) {
			buffer.append(' ');
			return;
		}
		processBuffer(index, codepoint);
		setWhitespacePrevCp();
	}

	/*
	 * Comment management.
	 */

	@Override
	public void commented(int index, int commentType, String comment) {
		if (buffer.length() != 0) {
			processBuffer(index, 12);
			if (commentType == 0) {
				commentStore.addTrailingComment(comment);
				commentStore.setTrailingComments();
			}
		} else if (commentType == 0) {
			if (!isPrevCpWhitespace() && (prevcp != 12 || commentStore.haveTrailingComments())) {
				commentStore.addTrailingComment(comment);
			} else {
				commentStore.addPrecedingComment(comment);
				commentStore.resetTrailingComments();
			}
		}
		prevcp = 12;
	}

	CommentStore getCommentStore() {
		return commentStore;
	}

	@Override
	public StringList getPrecedingCommentsAndClear() {
		return commentStore.getPrecedingCommentsAndClear();
	}

	@Override
	public StringList getTrailingCommentsAndClear() {
		return commentStore.getTrailingCommentsAndClear();
	}

	/*
	 * End of comment management.
	 */

	@Override
	public void endOfStream(int len) {
		if (parendepth != 0) {
			handleError(len, ParseHelper.ERR_UNMATCHED_PARENTHESIS, "Unmatched parenthesis");
		} else {
			if (!isInError()) { // Could be in unexpected EOF error
				processBuffer(len, 0);
				commentStore.setTrailingComments();
			}
			// The next call checks for error at the manager level
			endOfPropertyDeclaration(len);
		}

		getManager().endOfStream(len);
	}

	@Override
	public void error(int index, byte errCode, CharSequence context) {
		super.error(index, errCode, context);
		resetHandler();
	}

	String getPropertyName() {
		return "";
	}

	@Override
	public void resetHandler() {
		super.resetHandler();
		lunit = null;
		currentlu = null;
		commentStore.reset();
		functionToken = false;
		buffer.setLength(0);
	}

}
