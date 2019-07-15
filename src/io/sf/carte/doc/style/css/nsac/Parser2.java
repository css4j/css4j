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

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

/**
 * Updates SAC's {@link Parser} interface.
 */
public interface Parser2 extends Parser {

	/**
	 * NSAC parser flags: the {@link #STARHACK}, {@link #IEVALUES}, {@link #IEPRIO} and
	 * {@link #IEPRIOCHAR} flags are supported.
	 */
	public enum Flag {
		/**
		 * Handles asterisk-prefixed properties as normal, compliant CSS properties but reports a
		 * SAC warning.
		 */
		STARHACK,

		/**
		 * Accepts values with some IE hacks, producing {@link LexicalUnit2#SAC_COMPAT_IDENT}
		 * values.
		 */
		IEVALUES,

		/**
		 * Allows values ending with the '!ie' priority hack (and puts them into
		 * <code>SAC_COMPAT_IDENT</code> compatibility values).
		 */
		IEPRIO,

		/**
		 * Accepts values with an '!important!' priority, and sets it to
		 * {@link LexicalUnit2#SAC_COMPAT_PRIO} pseudo-values. Those values must be handled as of
		 * !important priority.
		 */
		IEPRIOCHAR
	}

	/**
	 * Set a parser flag.
	 * 
	 * @param flag
	 *            the flag.
	 */
	public void setFlag(Flag flag);

	/**
	 * Unset a parser flag.
	 * 
	 * @param flag
	 *            the flag.
	 */
	public void unsetFlag(Flag flag);

	/**
	 * Parse a CSS rule.
	 *
	 * @param source
	 *            the rule's source.
	 * @param nsmap
	 *            the namespace map.
	 * 
	 * @exception CSSException
	 *                Any CSS exception, possibly wrapping another exception.
	 * @exception java.io.IOException
	 *                An IO exception from the parser, possibly from a byte stream or
	 *                character stream supplied by the application.
	 */
	public void parseRule(InputSource source, NamespaceMap nsmap) throws CSSException, IOException;

	/**
	 * Interface giving access to namespace URI from the prefix.
	 */
	public interface NamespaceMap {
		/**
		 * Gets the namespace URI associated to the given prefix.
		 * 
		 * @param nsPrefix
		 *            the namespace prefix.
		 * @return the namespace URI string.
		 */
		String getNamespaceURI(String nsPrefix);
	}
}
