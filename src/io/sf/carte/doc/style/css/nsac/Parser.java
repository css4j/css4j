/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017,2018 Carlos Amengual.
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
		 * {@link LexicalUnit#SAC_COMPAT_IDENT} values.
		 */
		IEVALUES,

		/**
		 * Allows values ending with the '!ie' priority hack (and puts them into
		 * <code>SAC_COMPAT_IDENT</code> compatibility values).
		 */
		IEPRIO,

		/**
		 * Accepts values with an '!important!' priority, and sets it to
		 * {@link LexicalUnit#SAC_COMPAT_PRIO} pseudo-values. Those values must be
		 * handled as of !important priority.
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
	 * Allow an application to register a document event handler.
	 *
	 * <p>
	 * If the application does not register a document handler, all document events
	 * reported by the CSS parser will be silently ignored (this is the default
	 * behaviour implemented by HandlerBase).
	 * </p>
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
	 * Allow an application to register an error event handler.
	 *
	 * <p>
	 * If the application does not register an error event handler, all error events
	 * reported by the CSS parser will be silently ignored, except for fatalError,
	 * which will throw a CSSParseException (this is the default behaviour
	 * implemented by HandlerBase).
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
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             sheet.
	 * @see #setDocumentHandler
	 * @see #setErrorHandler
	 */
	void parseStyleSheet(InputSource source) throws CSSException, IOException;

	/**
	 * Parse a CSS style sheet.
	 *
	 * @param reader the character stream containing the CSS sheet.
	 * 
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             sheet.
	 * @see #setDocumentHandler
	 * @see #setErrorHandler
	 */
	void parseStyleSheet(Reader reader) throws CSSParseException, IOException;

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
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             sheet.
	 * @see #setDocumentHandler
	 * @see #setErrorHandler
	 */
	void parseStyleSheet(String uri) throws CSSParseException, IOException;

	/**
	 * Parse a comma separated list of selectors.
	 * 
	 * @param reader the character stream containing the selector list.
	 * 
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             list.
	 */
	SelectorList parseSelectors(Reader reader) throws CSSParseException, IOException;

	/**
	 * Parse a CSS style declaration (without '{' and '}').
	 *
	 * @param reader     the character stream containing the CSS style declaration.
	 * 
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             declaration.
	 */
	void parseStyleDeclaration(Reader reader) throws CSSParseException, IOException;

	/**
	 * Parse a CSS property value.
	 * 
	 * @param reader the character stream containing the CSS property value.
	 * 
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
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             rule.
	 */
	void parseRule(Reader reader) throws CSSParseException, IOException;

	/**
	 * Parse a CSS rule.
	 *
	 * @param reader the character stream containing the CSS rule.
	 * @param nsmap  the namespace map.
	 *
	 * @throws CSSParseException   if an error was found and no error handler was
	 *                             set.
	 * @throws java.io.IOException if a I/O error was found while retrieving the
	 *                             rule.
	 */
	void parseRule(Reader reader, NamespaceMap nsmap) throws CSSParseException, IOException;

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
	 * @throws CSSException
	 */
	MediaQueryList parseMediaQueryList(String media, Node owner) throws CSSException;

	/**
	 * Parse a media query string into the given handler.
	 * 
	 * @param media
	 *            the media query text.
	 * @param queryFactory
	 *            the query factory.
	 * @param mqhandler
	 *            the media query list handler.
	 * @throws CSSException <code>CSSException.SAC_NOT_SUPPORTED_ERR</code> if a
	 *                      hard-coded limit in nested expressions was reached.
	 */
	void parseMediaQueryList(String media, MediaQueryFactory queryFactory,
			MediaQueryHandler mqhandler) throws CSSException;

}
