/*

 Copyright (c) 2005-2025, Carlos Amengual.

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
 * A sorted set implementation of the {@link DOMTokenList} interface.
 * <p>
 * The implementation is optimized for an use case where most instances of this
 * class will only host a single token. Hence, it can hold a reference to a
 * single String or to a sorted set, depending on the number of items in this
 * list/set.
 */
public class DOMTokenSetImpl implements DOMTokenList, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private boolean multiple = false;

	// This reference can host a String or a TreeSet
	Object value = "";

	public DOMTokenSetImpl() {
		super();
	}

	@Override
	public String getValue() {
		if (multiple) {
			@SuppressWarnings("unchecked")
			LinkedList<String> set = (LinkedList<String>) this.value;
			StringBuilder buf = new StringBuilder(32 + 8 * set.size());
			Iterator<String> it = set.iterator();
			buf.append(it.next());
			while (it.hasNext()) {
				buf.append(' ').append(it.next());
			}
			return buf.toString();
		}
		return (String) value;
	}

	@Override
	public String getSortedValue() {
		if (multiple) {
			@SuppressWarnings("unchecked")
			TreeSet<String> set = new TreeSet<>((LinkedList<String>) this.value);
			StringBuilder buf = new StringBuilder(32 + 8 * set.size());
			Iterator<String> it = set.iterator();
			buf.append(it.next());
			while (it.hasNext()) {
				buf.append(' ').append(it.next());
			}
			return buf.toString();
		}
		return (String) value;
	}

	/**
	 * Sets the value of the collection as a String.
	 * 
	 * @param value the string value of the collection
	 * @throws DOMException <code>SYNTAX_ERR</code> if <code>value</code> is
	 *                      <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public void setValue(String value) throws DOMException {
		if (!checkMultipleToken(value)) {
			multiple = false;
			this.value = value.trim();
		} else {
			LinkedList<String> set;
			if (multiple) {
				set = (LinkedList<String>) this.value;
				set.clear();
			} else {
				multiple = true;
				set = new LinkedList<>();
				this.value = set;
			}
			StringTokenizer st = new StringTokenizer(value);
			while (st.hasMoreTokens()) {
				set.add(st.nextToken());
			}
		}
	}

	/**
	 * Utility method intended for cross-package internal use by the library.
	 * 
	 * @param value the value to check.
	 * @return <code>true</code> if the value contains more than one token.
	 */
	public static boolean checkMultipleToken(String value) {
		boolean foundChar = false, foundWS = false;
		if (value == null) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Null value");
		}
		int len = value.length();
		for (int i = 0; i < len; i++) {
			char c = value.charAt(i);
			if (foundChar && (c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f')) {
				foundWS = true;
			} else {
				if (foundWS) {
					return true;
				}
				foundChar = true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getLength() {
		if (multiple) {
			return ((LinkedList<String>) this.value).size();
		}
		return ((String) value).length() == 0 ? 0 : 1;
	}

	@Override
	public String item(int index) {
		if (multiple) {
			@SuppressWarnings("unchecked")
			LinkedList<String> set = (LinkedList<String>) this.value;
			if (index < 0 || index >= set.size()) {
				return null;
			}
			return set.get(index);
		}
		if (index != 0 || ((String) this.value).length() == 0) {
			return null;
		}
		return (String) this.value;
	}

	@Override
	public Iterator<String> iterator() {
		if (multiple) {
			@SuppressWarnings("unchecked")
			LinkedList<String> linkedList = (LinkedList<String>) this.value;
			return linkedList.iterator();
		}
		return ((String) value).length() == 0 ? new EmptyIterator<>() : new SingleItemIterator((String) value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(String token) {
		if (multiple) {
			return ((LinkedList<String>) this.value).contains(token);
		}
		return value.equals(token);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(DOMTokenList otherlist) {
		if (otherlist == null) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Argument is null.");
		}
		int otherlen = otherlist.getLength();
		if (otherlen == 0) {
			return true;
		}
		if (multiple) {
			if (otherlen == 1) {
				return ((LinkedList<String>) this.value).contains(otherlist.item(0));
			}
			for (String element : otherlist) {
				if (!contains(element)) {
					return false;
				}
			}
			return true;
		}
		return otherlen == 1 && value.equals(otherlist.item(0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(Collection<String> tokenSet) {
		if (multiple) {
			return ((LinkedList<String>) this.value).containsAll(tokenSet);
		}
		int sz = tokenSet.size();
		return sz == 0 || (sz == 1 && value.equals(tokenSet.iterator().next()));
	}

	@Override
	public void add(String token) throws DOMException {
		argumentCheckVoidSpaces(token);
		addUnchecked(token);
	}

	@SuppressWarnings("unchecked")
	protected void addUnchecked(String token) throws DOMException {
		String strValue;
		if (multiple) {
			LinkedList<String> set = (LinkedList<String>) this.value;
			if (!set.contains(token)) {
				set.add(token);
			}
		} else if ((strValue = (String) this.value).length() != 0) {
			if (!strValue.equals(token)) {
				LinkedList<String> set = new LinkedList<>();
				set.add(strValue);
				set.add(token);
				this.value = set;
				multiple = true;
			}
		} else {
			this.value = token;
		}
	}

	@Override
	public void remove(String token) throws DOMException {
		argumentCheckVoidSpaces(token);
		removeUnchecked(token);
	}

	protected void removeUnchecked(String token) throws DOMException {
		if (multiple) {
			@SuppressWarnings("unchecked")
			LinkedList<String> set = (LinkedList<String>) this.value;
			set.remove(token);
			if (set.size() == 1) {
				this.value = set.getFirst();
				multiple = false;
			}
		} else if (value.equals(token)) {
			this.value = "";
		}
	}

	@Override
	public boolean toggle(String token) throws DOMException {
		argumentCheckVoidSpaces(token);
		return toggleUnchecked(token);
	}

	@SuppressWarnings("unchecked")
	protected boolean toggleUnchecked(String token) throws DOMException {
		LinkedList<String> set;
		if (multiple) {
			set = (LinkedList<String>) this.value;
			if (!set.remove(token)) {
				set.add(token);
				return true;
			}
			if (set.size() == 1) {
				this.value = set.getFirst();
				multiple = false;
				set.clear();
			}
		} else if (this.value.equals(token)) {
			this.value = "";
		} else if (((String) this.value).length() == 0) {
			this.value = token;
			return true;
		} else {
			set = new LinkedList<>();
			set.add((String) this.value);
			set.add(token);
			this.value = set;
			multiple = true;
			return true;
		}
		return false;
	}

	@Override
	public void replace(String oldToken, String newToken) throws DOMException {
		argumentCheckVoidSpaces(oldToken);
		argumentCheckVoidSpaces(newToken);
		replaceUnchecked(oldToken, newToken);
	}

	protected void replaceUnchecked(String oldToken, String newToken) throws DOMException {
		if (multiple) {
			@SuppressWarnings("unchecked")
			LinkedList<String> set = (LinkedList<String>) this.value;
			int idx = set.indexOf(oldToken);
			if (idx != -1) {
				if (!set.contains(newToken)) {
					set.set(idx, newToken);
				} else {
					set.remove(idx);
				}
			}
		} else if (this.value.equals(oldToken)) {
			this.value = newToken;
		}
	}

	/**
	 * Empty this set.
	 * <p>
	 * After calling this method, the length of this set is zero.
	 */
	@SuppressWarnings("unchecked")
	public void clear() {
		if (multiple) {
			((LinkedList<String>) this.value).clear(); // Help GC
			multiple = false;
		}
		this.value = "";
	}

	protected void argumentCheckVoidSpaces(String token) throws DOMException {
		if (token == null || token.length() == 0) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Token cannot be empty");
		}
		if (token.indexOf(' ') != -1) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Token cannot contain spaces");
		}
	}

	@Override
	public String toString() {
		return getValue();
	}
}
