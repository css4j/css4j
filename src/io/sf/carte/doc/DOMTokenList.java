/*
 * This software includes material derived from DOM (https://www.w3.org/TR/dom/).
 * Copyright © 2015 W3C® (MIT, ERCIM, Keio, Beihang).
 * https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
 * 
 * Copyright © 2017, Carlos Amengual.
 * 
 * SPDX-License-Identifier: W3C-20150513
 */

package io.sf.carte.doc;

import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.DOMException;

/**
 * Based on DOM interface <a href="https://www.w3.org/TR/dom/#interface-domtokenlist">DOMTokenList</a>
 * which is actually a set.
 */
public interface DOMTokenList extends Iterable<String> {

	/**
	 * Gets the number of tokens stored in this collection.
	 * 
	 * @return the number of tokens stored in this collection.
	 */
	public int getLength();

	/**
	 * Gets the value of the collection as a String.
	 * 
	 * @return the value of the collection as a String.
	 */
	public String getValue();

	/**
	 * Gets the value of the collection as a String, with the tokens alphabetically ordered.
	 * 
	 * @return the value of the collection as an alphabetically ordered string.
	 */
	public String getSortedValue();

	/**
	 * Retrieves a token from the collection by its index.
	 * 
	 * @param index
	 *            the index.
	 * @return the token in the given place of this collection, or <code>null</code>
	 *         if the index is negative, greater than or equal to the length of this
	 *         collection.
	 */
	public String item(int index);

	/**
	 * Obtain an iterator over the members of this set.
	 * 
	 * @return an iterator over the members of this set.
	 */
	@Override
	public Iterator<String> iterator();

	/**
	 * Does this collection contains this token?
	 * 
	 * @param token
	 *            the token.
	 * @return <code>true</code> if the list contains the given token, <code>false</code> otherwise.
	 */
	public boolean contains(String token);

	/**
	 * Does this collection contain all the tokens in the supplied
	 * <code>DOMTokenList</code>?
	 * 
	 * @param otherlist another <code>DOMTokenList</code>.
	 * @return <code>true</code> if this list contains all the tokens in the given
	 *         <code>otherlist</code>, <code>false</code> otherwise.
	 * @throws DOMException
	 *             SYNTAX_ERR if the argument is <code>null</code> or empty.
	 */
	public boolean containsAll(DOMTokenList otherlist);

	/**
	 * Does this collection contain all the tokens in the given collection?
	 * 
	 * @param tokenSet
	 *            the collection of tokens.
	 * @return <code>true</code> if the list contains all the tokens in the given set, false
	 *         otherwise.
	 * @throws DOMException
	 *             SYNTAX_ERR if the argument is <code>null</code> or empty.
	 */
	public boolean containsAll(Collection<String> tokenSet);

	/**
	 * Adds the given token to this collection.
	 * <p>
	 * If the set already contains the token, then do nothing.
	 * 
	 * @param token
	 *            the token to add.
	 * @throws DOMException
	 *             SYNTAX_ERR if the token is <code>null</code> or empty.
	 *             INVALID_CHARACTER_ERR if the token contains spaces.
	 */
	public void add(String token) throws DOMException;

	/**
	 * Removes the given token from this collection.
	 * 
	 * @param token
	 *            the token to remove.
	 * @throws DOMException
	 *             SYNTAX_ERR if the token is <code>null</code> or empty.
	 *             INVALID_CHARACTER_ERR if the token contains spaces.
	 */
	public void remove(String token) throws DOMException;

	/**
	 * If the given token exists in this collection, remove it; otherwise add
	 * it.
	 * 
	 * @param token
	 *            the token to toggle.
	 * @return <code>true</code> if the token is added, <code>false</code> if it is removed.
	 * @throws DOMException
	 *             SYNTAX_ERR if the token is <code>null</code> or empty.
	 *             INVALID_CHARACTER_ERR if the token contains spaces.
	 */
	public boolean toggle(String token) throws DOMException;

	/**
	 * Replaces an existing token with a new token.
	 * <p>
	 * If the set does not contain the token, do nothing.
	 * 
	 * @param oldToken
	 *            the token that has to be replaced.
	 * @param newToken
	 *            the new token.
	 * @throws DOMException
	 *             SYNTAX_ERR if any of the tokens is <code>null</code> or empty.
	 *             INVALID_CHARACTER_ERR if any of the tokens contains spaces.
	 */
	public void replace(String oldToken, String newToken) throws DOMException;

}
