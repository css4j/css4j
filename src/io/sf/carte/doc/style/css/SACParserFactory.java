/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;

/**
 * SAC Parser Factory used by static methods.
 * <p>
 * 
 * @author Carlos Amengual
 */
public class SACParserFactory {

	public static final String DEFAULT_PARSER = "io.sf.carte.doc.style.css.parser.CSSParser";

	static Logger log = LoggerFactory.getLogger(SACParserFactory.class.getName());

	public SACParserFactory() {
		super();
	}

	/**
	 * Create a SAC Parser specified by the system property
	 * <code>org.w3c.css.sac.parser</code>.
	 * <p>
	 * By default, uses this library's SAC Parser.
	 * 
	 * @return the SAC parser.
	 * @throws DOMException
	 *             INVALID_ACCESS_ERR if the Parser could not be instantiated.
	 */
	public static Parser createSACParser() throws DOMException {
		String parserClass = null;
		try {
			parserClass = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
				@Override
				public String run() {
					return System.getProperty("org.w3c.css.sac.parser");
				}
			});
		} catch (SecurityException e) {
			log.warn("Unable to read system property org.w3c.css.sac.parser", e);
		}
		if (parserClass != null) {
			if (log.isTraceEnabled()) {
				log.trace("Instantiating SAC parser " + parserClass);
			}
			try {
				return instantiateParser(parserClass);
			} catch (Exception e) {
				log.error("Could not instantiate system SAC parser", e);
				log.trace("Instantiating default SAC parser " + DEFAULT_PARSER);
			}
		}
		return instantiateParser(DEFAULT_PARSER);
	}

	/**
	 * Instantiate a SAC Parser from the <code>parserClass</code> class.
	 * <p>
	 * 
	 * @return the SAC parser.
	 * @throws DOMException
	 *             INVALID_ACCESS_ERR if the Parser could not be instantiated.
	 */
	private static Parser instantiateParser(final String parserClass) throws DOMException {
		Parser parser = null;
		String message = null;
		try {
			parser = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Parser>() {
				@Override
				public Parser run() {
					try {
						return (Parser) Class.forName(parserClass).getConstructor().newInstance();
					} catch (Exception e) {
						log.warn("Could not instantiate " + parserClass, e);
						return null;
					}
				}
			});
			if (parser == null) {
				message = "Could not instantiate " + parserClass;
			}
		} catch (SecurityException e) {
			message = "Unable to instantiate " + parserClass + " due to access restriction: " + e.getMessage();
			log.warn(message, e);
		}
		if (parser == null) {
			throw new DOMException(DOMException.INVALID_ACCESS_ERR, message);
		}
		return parser;
	}

}
