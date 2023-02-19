/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.w3c.dom.DOMException;

/**
 * An implementation of the {@link DOMTokenList} interface.
 * <p>
 * It is internally a linked list, but behaves like an ordered set.
 */
public class DOMTokenListImpl implements DOMTokenList, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private final LinkedList<String> tokenset;

	public DOMTokenListImpl() {
		super();
		this.tokenset = new LinkedList<String>();
	}

	@Override
	public int getLength() {
		return tokenset.size();
	}

	@Override
	public String getValue() {
		int sz = tokenset.size();
		if (sz == 0) {
			return "";
		}
		if (sz == 1) {
			return tokenset.getFirst();
		}
		StringBuilder buf = new StringBuilder(32 + 12 * tokenset.size());
		Iterator<String> it = tokenset.iterator();
		buf.append(it.next());
		while (it.hasNext()) {
			buf.append(' ').append(it.next());
		}
		return buf.toString();
	}

	@Override
	public String getSortedValue() {
		int sz = tokenset.size();
		if (sz == 0) {
			return "";
		}
		if (sz == 1) {
			return tokenset.getFirst();
		}
		TreeSet<String> set = new TreeSet<String>(tokenset);
		StringBuilder buf = new StringBuilder(32 + 12 * tokenset.size());
		Iterator<String> it = set.iterator();
		buf.append(it.next());
		while (it.hasNext()) {
			buf.append(' ').append(it.next());
		}
		return buf.toString();
	}

	/**
	 * Sets the value of the collection as a String.
	 * 
	 * @param value the string value of the collection
	 * @throws DOMException <code>SYNTAX_ERR</code> if <code>value</code> is
	 *                      <code>null</code>.
	 */
	public void setValue(String value) throws DOMException {
		if (value == null) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Token cannot be null");
		}
		tokenset.clear();
		StringTokenizer st = new StringTokenizer(value);
		while (st.hasMoreTokens()) {
			tokenset.add(st.nextToken());
		}
	}

	@Override
	public String item(int index) {
		if (index < 0 || index >= tokenset.size()) {
			return null;
		}
		return tokenset.get(index);
	}

	@Override
	public boolean contains(String token) {
		return tokenset.contains(token);
	}

	@Override
	public Iterator<String> iterator() {
		return tokenset.iterator();
	}

	@Override
	public boolean containsAll(DOMTokenList otherlist) throws DOMException {
		if (otherlist == null) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Argument is null.");
		}
		Iterator<String> it = otherlist.iterator();
		while (it.hasNext()) {
			if (!contains(it.next())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean containsAll(Collection<String> tokenSet) {
		return tokenset.containsAll(tokenSet);
	}

	@Override
	public void add(String token) throws DOMException {
		if (token == null || token.length() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Token cannot be empty.");
		}
		if (token.indexOf(' ') != -1) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Token cannot contain spaces");
		}
		if (!tokenset.contains(token)) {
			tokenset.add(token);
		}
	}

	@Override
	public void remove(String token) throws DOMException {
		if (token == null || token.length() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Token cannot be empty");
		}
		if (token.indexOf(' ') != -1) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Token cannot contain spaces");
		}
		tokenset.remove(token);
	}

	@Override
	public boolean toggle(String token) throws DOMException {
		if (token == null || token.length() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Token cannot be empty");
		}
		if (token.indexOf(' ') != -1) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Token cannot contain spaces");
		}
		if (!tokenset.remove(token)) {
			return tokenset.add(token);
		}
		return false;
	}

	@Override
	public void replace(String oldToken, String newToken) throws DOMException {
		if (oldToken == null || newToken == null || oldToken.length() == 0 || newToken.length() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Tokens cannot be empty");
		}
		if (oldToken.indexOf(' ') != -1 || newToken.indexOf(' ') != -1) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Tokens cannot contain spaces");
		}
		int idx = tokenset.indexOf(oldToken);
		if (idx != -1) {
			tokenset.set(idx, newToken);
		}
	}

}
