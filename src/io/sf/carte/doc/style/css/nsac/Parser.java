/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017-2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

import java.io.IOException;
import java.io.Reader;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.parser.MediaQueryFactory;
import io.sf.carte.doc.style.css.parser.MediaQueryHandler;

/**
 * A low-level CSS parser.
 * <p>
 * Based on SAC's {@code Parser} interface by Philippe Le Hegaret.
 * </p>
 * <p>
 * Compared to SAC, this interface replaces some of the methods that use an
 * {@code InputSource} with a {@code Reader}. The reason is that, in opinion of
 * the NSAC author, the {@code InputSource} (a concept coming from SAX) adds
 * bloat and in the worst case may widen the attack surface if the application
 * using the library is somehow abused. It is generally better that the main
 * application takes the responsibility of opening connections to files or
 * remote sources.
 * </p>
 * <p>
 * The reference implementation includes the replaced {@code InputSource}
 * methods, in case you want to use them.
 * </p>
 */
public interface Parser {

	/**
	 * NSAC parser flags: the {@link #STARHACK}, {@link #IEVALUES}, {@link #IEPRIO}
	 * and {@link #IEPRIOCHAR} flags are supported.
	 */
	enum Flag {
		/**
		 * Handles asterisk-prefixed properties as normal, compliant CSS properties but
		 * reports a SAC warning.
		 */
		STARHACK,

		/**
		 * Accepts values with some IE hacks, producing
		 * {@link LexicalUnit.LexicalType#COMPAT_IDENT COMPAT_IDENT} values.
		 */
		IEVALUES,

		/**
		 * Allows values ending with the '!ie' priority hack (and puts them into
		 * <code>COMPAT_IDENT</code> compatibility values).
		 */
		IEPRIO,

		/**
		 * Accepts values with an '!important!' priority, and sets it to
		 * {@link LexicalUnit.LexicalType#COMPAT_PRIO COMPAT_PRIO} pseudo-values. Those
		 * values must be handled as of !important priority.
		 */
		IEPRIOCHAR
	}

	/**
	 * Set a parser flag.
	 *
	 * @param flag the flag.
	 */
	void setFlag(Flag flag);

	/**
	 * Unset a parser flag.
	 *
	 * @param flag the flag.
	 */
	void unsetFlag(Flag flag);

	/**
	 * Allow an application to set a document event handler.
	 *
	 * <p>
	 * Applications may register a new or different handler in the middle of a
	 * parse, and the CSS parser must begin using the new handler immediately.
	 * </p>
	 *
	 * @param handler The document handler.
	 * @see CSSHandler
	 */
	void setDocumentHandler(CSSHandler handler);

	/**
	 * Allow an application to set an error event handler.
	 *
	 * <p>
	 * If the application does not register an error event handler, any error will
	 * result in a CSSParseException being thrown.
	 * </p>
	 *
	 * <p>
	 * Applications may register a new or different handler in the middle of a
	 * parse, and the CSS parser must begin using the new handler immediately.
	 * </p>
	 *
	 * @param handler The error handler.
	 * @see CSSErrorHandler
	 * @see CSSParseException
	 */
	void setErrorHandler(CSSErrorHandler handler);

	/**
	 * Parse a CSS style sheet.
	 *
	 * @param source the input source of the CSS sheet.
	 * 
	 * @throws CSSParseException     if an error was found and no error handler was
	 *                               set.
	 * @throws java.io.IOException   if a I/O error was found while retrieving the
	 *                               sheet.
	 * @throws IllegalStateException if the {@code CSSHandler} is not set.
	 * @see #setDocumentHandler
	 * @see #setErrorHandler
	 */
	void parseStyleSheet(InputSource source) throws CSSException, IOException, IllegalStateException;

	/**
	 * Parse a CSS style sheet.
	 *
	 * @param reader the character stream containing the CSS sheet.
	 * 
	 * @throws CSSParseException     if an error was found and no error handler was
	 *                               set.
	 * @throws java.io.IOException   if a I/O error was found while retrieving the
	 *                               sheet.
	 * @throws IllegalStateException if the {@code CSSHandler} is not set.
	 * @see #setDocumentHandler
	 * @see #setErrorHandler
	 */
	void parseStyleSheet(Reader reader) throws CSSParseException, IOException, IllegalStateException;

	/**
	 * Parse a CSS sheet from a URI.
	 * <p>
	 * The sheet is parsed as a rule list, that is, XML's {@code CDO}-{@code CDC}
	 * comments are not expected.
	 * </p>
	 * <p>
	 * Usage of this method may have security implications. Please make sure that
	 * the URI being passed is safe to use.
	 * </p>
	 *
	 * @param uri The URI locating the sheet.
	 * @throws CSSParseException     if an error was found and no error handler was
	 *                               set.
	 * @throws java.io.IOException   if a I/O error was found while retrieving the
	 *                               sheet.
	 * @throws IllegalStateException if the {@code CSSHandler} is not set.
	 * @see #setDocumentHandler
	 * @see #setErrorHandler
	 */
	void parseStyleSheet(String uri) throws CSSParseException, IOException, IllegalStateException;

	/**
	 * Parse a comma separated list of selectors.
	 * 
	 * @param reader the character stream containing the selector list.
	 * 
	 * @return the selector list.
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             list.
	 */
	SelectorList parseSelectors(Reader reader) throws CSSParseException, IOException;

	/**
	 * Parse a CSS style declaration (without '{' and '}').
	 *
	 * @param reader the character stream containing the CSS style declaration.
	 * 
	 * @throws CSSParseException     if an error was found and no error handler was
	 *                               set.
	 * @throws java.io.IOException   if a I/O error was found while retrieving the
	 *                               declaration.
	 * @throws IllegalStateException if the {@code CSSHandler} is not set.
	 */
	void parseStyleDeclaration(Reader reader) throws CSSParseException, IOException, IllegalStateException;

	/**
	 * Parse a CSS property value.
	 * 
	 * @param reader the character stream containing the CSS property value.
	 * 
	 * @return the lexical unit containing the value, possibly chained to subsequent
	 *         lexical units.
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             value.
	 */
	LexicalUnit parsePropertyValue(Reader reader) throws CSSParseException, IOException;

	/**
	 * Parse a CSS priority value (e.g. "!important").
	 * 
	 * @param reader the character stream containing the CSS priority.
	 * 
	 * @return {@code true} if the priority is important.
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             priority.
	 */
	boolean parsePriority(Reader reader) throws CSSParseException, IOException;

	/**
	 * Parse a namespaceless CSS rule.
	 * 
	 * @param reader the character stream containing the CSS rule.
	 *
	 * @throws CSSParseException     if an error was found and no error handler was
	 *                               set.
	 * @throws java.io.IOException   if a I/O error was found while retrieving the
	 *                               rule.
	 * @throws IllegalStateException if the {@code CSSHandler} is not set.
	 */
	void parseRule(Reader reader) throws CSSParseException, IOException, IllegalStateException;

	/**
	 * Parse a CSS rule.
	 *
	 * @param reader the character stream containing the CSS rule.
	 * @param nsmap  the namespace map.
	 *
	 * @throws CSSParseException     if an error was found and no error handler was
	 *                               set.
	 * @throws java.io.IOException   if a I/O error was found while retrieving the
	 *                               rule.
	 * @throws IllegalStateException if the {@code CSSHandler} is not set.
	 */
	void parseRule(Reader reader, NamespaceMap nsmap) throws CSSParseException, IOException, IllegalStateException;

	/**
	 * Interface giving access to namespace URI from the prefix.
	 */
	public interface NamespaceMap {
		/**
		 * Gets the namespace URI associated to the given prefix.
		 *
		 * @param nsPrefix the namespace prefix.
		 * @return the namespace URI string.
		 */
		String getNamespaceURI(String nsPrefix);
	}

	/**
	 * Parse an individual list of media queries.
	 * 
	 * @param media the string representation of the list of media queries.
	 * @param owner the node that owns the responsibility to handle the errors in
	 *              the query list.
	 * 
	 * @return the media query list.
	 * @throws CSSException
	 */
	MediaQueryList parseMediaQueryList(String media, Node owner) throws CSSException;

	/**
	 * Parse a media query string into the given handler.
	 * 
	 * @param media        the media query text.
	 * @param queryFactory the query factory.
	 * @param mqhandler    the media query list handler.
	 * @throws CSSException <code>CSSException.NOT_SUPPORTED_ERR</code> if a
	 *                      hard-coded limit in nested expressions was reached.
	 */
	void parseMediaQueryList(String media, MediaQueryFactory queryFactory, MediaQueryHandler mqhandler)
			throws CSSException;

}
