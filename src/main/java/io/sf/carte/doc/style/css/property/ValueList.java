/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueList;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.CSSValueSyntax.Category;
import io.sf.carte.doc.style.css.CSSValueSyntax.Match;
import io.sf.carte.doc.style.css.CSSValueSyntax.Multiplier;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of {@code CSSValueList} based on {@link StyleValue}.
 */
abstract public class ValueList extends StyleValue implements CSSValueList<StyleValue> {

	private static final long serialVersionUID = 1L;

	protected final List<StyleValue> valueList;

	private ValueList() {
		super();
		valueList = new ArrayList<>();
	}

	private ValueList(ValueList copy) {
		super();
		valueList = new ArrayList<>(copy.valueList);
	}

	@Override
	public int getLength() {
		return valueList.size();
	}

	@Override
	public StyleValue item(int index) {
		if (index < 0 || index >= valueList.size()) {
			return null;
		}
		return valueList.get(index);
	}

	@Override
	public Iterator<StyleValue> iterator() {
		return valueList.iterator();
	}

	/**
	 * Adds a value to the end of this list.
	 * 
	 * @param value
	 *            the value to be added.
	 * @return true
	 */
	@Override
	public boolean add(StyleValue value) {
		if (value == null) {
			throw new NullPointerException("Null value added to ValueList");
		}
		return valueList.add(value);
	}

	/**
	 * Appends all of the elements in the given list to the end of this list.
	 * <p>
	 * The appended list must be of the same type as this list.
	 * 
	 * @param list the list to add.
	 * @return <code>true</code> if this list changed as a result of the call.
	 */
	public boolean addAll(ValueList list) {
		if (list == null) {
			throw new NullPointerException("Null list added to ValueList");
		}
		if (isCommaSeparated() != list.isCommaSeparated() || isBracketList() != list.isBracketList()) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Attempted to add lists of different types");
		}
		return valueList.addAll(list.valueList);
	}

	/**
	 * Removes the value at the specified index.
	 * 
	 * @param index
	 *            the index of the value to be removed.
	 * @return the list item that was removed.
	 */
	@Override
	public StyleValue remove(int index) {
		return valueList.remove(index);
	}

	/**
	 * Replaces the value at the specified index with the supplied value.
	 * 
	 * @param index
	 *            the index of the value to be replaced.
	 * @param value
	 *            the value to replace the item at <code>index</code>.
	 * @return the item previously at the specified position.
	 * @throws IndexOutOfBoundsException if the index is invalid.
	 * @throws NullPointerException if the value is <code>null</code>.
	 */
	@Override
	public StyleValue set(int index, StyleValue value) {
		if (value == null) {
			throw new NullPointerException("Null value set to ValueList");
		}
		return valueList.set(index, value);
	}

	/**
	 * Is this list empty ?
	 * 
	 * @return <code>true</code> if this list has no items, <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return valueList.isEmpty();
	}

	/**
	 * Removes all the items from this list.
	 */
	@Override
	public void clear() {
		valueList.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode() * prime;
		if (valueList != null) {
			for (CSSValue value : valueList) {
				result = prime * result + ((value == null) ? 0 : value.hashCode());
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ValueList other = (ValueList) obj;
		if (valueList.isEmpty()) { // valueList cannot be null
			return other.valueList.isEmpty();
		} else if (valueList.size() != other.valueList.size()) {
			return false;
		} else {
			int sz = valueList.size();
			for (int i = 0; i < sz; i++) {
				CSSValue item = valueList.get(i);
				CSSValue oitem = other.valueList.get(i);
				if (item == null) { // Should not be the case
					if (oitem != null) {
						return false;
					}
				} else if (!item.equals(oitem)) {
					return false;
				}
			}
		}
		return true;
	}

	public void setSubproperty(boolean subp) {
		if (valueList != null) {
			for (int i = 0; i < getLength(); i++) {
				CSSValue val = item(i);
				CssType cat = val.getCssValueType();
				if (cat == CssType.TYPED || cat == CssType.PROXY) {
					((PrimitiveValue) val).setSubproperty(true);
				} else if (cat == CssType.KEYWORD) {
					set(i, ((KeywordValue) val).asSubproperty());
				} else if (cat == CssType.LIST) {
					((ValueList) val).setSubproperty(true);
				}
			}
		}
	}

	@Override
	public boolean isSubproperty() {
		if (valueList != null) {
			for (StyleValue element : valueList) {
				if (!element.isSubproperty()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"This value has to be modified by accessing its elements.");
	}

	abstract public boolean isCommaSeparated();

	/**
	 * Is this a bracket list?
	 * 
	 * @return {@code true} if this is a bracket list.
	 */
	public boolean isBracketList() {
		return false;
	}

	@Override
	public Match matches(CSSValueSyntax syntax) {
		Match result = Match.FALSE;
		if (!isEmpty() && syntax != null) {
			// If the list has one value, match directly on it
			if (getLength() == 1) {
				return item(0).matches(syntax);
			}
			// Check for universal
			if (syntax.getCategory() == Category.universal) {
				return Match.TRUE;
			}
			// Match according to multipliers (including implicit)
			do {
				Multiplier mult = syntax.getMultiplier();
				if (mult == Multiplier.NUMBER) {
					if (isCommaSeparated() || syntax.getCategory() == Category.transformList) {
						Match match = valuesMatch(iterator(), syntax);
						if (match == Match.TRUE) {
							return Match.TRUE;
						} else if (result == Match.FALSE) {
							result = match;
						}
					}
				} else if ((mult == Multiplier.PLUS
						|| syntax.getCategory() == Category.transformList) && !isCommaSeparated()) {
					Match match = valuesMatch(iterator(), syntax);
					if (match == Match.TRUE) {
						return Match.TRUE;
					} else if (result == Match.FALSE) {
						result = match;
					}
				}
			} while ((syntax = syntax.getNext()) != null);
		}
		return result;
	}

	@Override
	Match matchesComponent(CSSValueSyntax syntax) {
		Multiplier mult = syntax.getMultiplier();
		if (mult == Multiplier.NUMBER) {
			if (isCommaSeparated() || getLength() == 1
					|| syntax.getCategory() == Category.transformList) {
				return valuesMatch(iterator(), syntax);
			}
		} else if ((mult == Multiplier.PLUS || syntax.getCategory() == Category.transformList)
				&& !isCommaSeparated()) {
			return valuesMatch(iterator(), syntax);
		}
		return Match.FALSE;
	}

	static Match valuesMatch(Iterator<StyleValue> it, CSSValueSyntax syntax) {
		Match result = Match.TRUE;
		while (it.hasNext()) {
			StyleValue value = it.next();
			if (value.getCssValueType() == CssType.LIST
					&& syntax.getCategory() != Category.transformList) {
				return Match.FALSE;
			}
			Match match = value.matchesComponent(syntax);
			if (match == Match.FALSE) {
				return Match.FALSE;
			} else if (match == Match.PENDING) {
				result = Match.PENDING;
			}
		}
		return result;
	}

	/**
	 * Creates and returns a copy of this object.
	 * <p>
	 * The list is cloned, but its contents are not.
	 * 
	 * @return a clone of this instance.
	 */
	@Override
	abstract public ValueList clone();

	public static ValueList createCSValueList() {
		return new CSValueList();
	}

	private static class CSValueList extends ValueList {

		private static final long serialVersionUID = 1L;

		private CSValueList() {
			super();
		}

		private CSValueList(ValueList copy) {
			super(copy);
		}

		@Override
		public String getCssText() {
			if (valueList.isEmpty()) {
				return "";
			}
			BufferSimpleWriter sw = new BufferSimpleWriter(valueList.size() * 24 + 16);
			try {
				writeCssText(sw);
			} catch (IOException e) {
			}
			return sw.toString();
		}

		@Override
		public String getMinifiedCssText(String propertyName) {
			if (valueList.isEmpty()) {
				return "";
			}

			StringBuilder buf = new StringBuilder(valueList.size() * 24 + 16);
			buf.append(item(0).getMinifiedCssText(propertyName));
			int sz = valueList.size();
			for (int i = 1; i < sz; i++) {
				buf.append(',').append(item(i).getMinifiedCssText(propertyName));
			}

			return buf.toString();
		}

		@Override
		public void writeCssText(SimpleWriter wri) throws IOException {
			if (!valueList.isEmpty()) {
				valueList.get(0).writeCssText(wri);
				int sz = valueList.size();
				for (int i = 1; i < sz; i++) {
					wri.write(',');
					wri.write(' ');
					valueList.get(i).writeCssText(wri);
				}
			}
		}

		@Override
		public boolean isCommaSeparated() {
			return true;
		}

		@Override
		public ValueList clone() {
			return new CSValueList(this);
		}

		@Override
		public ValueList wrap(String oldHrefContext, String parentSheetHref) {
			return new CSValueListWrapper(this, oldHrefContext, parentSheetHref);
		}

		public static class CSValueListWrapper extends CSValueList implements WrappedValue {

			private static final long serialVersionUID = 1L;

			private final String oldHrefContext, parentSheetHref;

			CSValueListWrapper(ValueList copy, String oldHrefContext, String parentSheetHref) {
				super(copy);
				this.parentSheetHref = parentSheetHref;
				this.oldHrefContext = oldHrefContext;
			}

			@Override
			public String getParentSheetHref() {
				return parentSheetHref;
			}

			@Override
			public StyleValue item(int index) {
				StyleValue val = super.item(index);
				if (val != null && val.getPrimitiveType() == CSSValue.Type.URI) {
					return new URIValueWrapper((URIValue) val, oldHrefContext, this.parentSheetHref);
				} else {
					return val;
				}
			}

			@Override
			public ValueList clone() {
				return new CSValueListWrapper(super.clone(), oldHrefContext, this.parentSheetHref);
			}

		}
	}

	public static ValueList createWSValueList() {
		return new WSValueList();
	}

	private static class WSValueList extends ValueList {

		private static final long serialVersionUID = 1L;

		private WSValueList() {
			super();
		}

		private WSValueList(ValueList copy) {
			super(copy);
		}

		@Override
		public String getCssText() {
			if (valueList.isEmpty()) {
				return "";
			}
			BufferSimpleWriter sw = new BufferSimpleWriter(valueList.size() * 24 + 16);
			try {
				writeCssText(sw);
			} catch (IOException e) {
			}
			return sw.toString();
		}

		@Override
		public String getMinifiedCssText(String propertyName) {
			if (valueList.isEmpty()) {
				return "";
			}

			StringBuilder buf = new StringBuilder(valueList.size() * 24 + 16);
			buf.append(item(0).getMinifiedCssText(propertyName));
			int sz = valueList.size();
			for (int i = 1; i < sz; i++) {
				buf.append(' ').append(item(i).getMinifiedCssText(propertyName));
			}

			return buf.toString();
		}

		@Override
		public void writeCssText(SimpleWriter wri) throws IOException {
			if (!valueList.isEmpty()) {
				valueList.get(0).writeCssText(wri);
				int sz = valueList.size();
				for (int i = 1; i < sz; i++) {
					wri.write(' ');
					valueList.get(i).writeCssText(wri);
				}
			}
		}

		@Override
		public boolean isCommaSeparated() {
			return false;
		}

		@Override
		public ValueList clone() {
			return new WSValueList(this);
		}

		@Override
		public ValueList wrap(String oldHrefContext, String parentSheetHref) {
			return new WSValueListWrapper(this, oldHrefContext, parentSheetHref);
		}

		public static class WSValueListWrapper extends WSValueList implements WrappedValue {

			private static final long serialVersionUID = 1L;

			private final String parentSheetHref;
			final String oldHrefContext;

			WSValueListWrapper(ValueList copy, String oldHrefContext, String parentSheetHref) {
				super(copy);
				this.parentSheetHref = parentSheetHref;
				this.oldHrefContext = oldHrefContext;
			}

			@Override
			public String getParentSheetHref() {
				return parentSheetHref;
			}

			@Override
			public StyleValue item(int index) {
				StyleValue val = super.item(index);
				if (val != null && val.getPrimitiveType() == CSSValue.Type.URI) {
					return new URIValueWrapper((URIValue) val, oldHrefContext, this.parentSheetHref);
				} else {
					return val;
				}
			}

			@Override
			public ValueList clone() {
				return new WSValueListWrapper(super.clone(), oldHrefContext, this.parentSheetHref);
			}

		}
	}

	public static ValueList createBracketValueList() {
		return new BracketValueList();
	}

	private static class BracketValueList extends ValueList {

		private static final long serialVersionUID = 1L;

		private BracketValueList() {
			super();
		}

		private BracketValueList(BracketValueList copy) {
			super(copy);
		}

		@Override
		public String getCssText() {
			if (valueList.isEmpty()) {
				return "[]";
			}
			BufferSimpleWriter sw = new BufferSimpleWriter(valueList.size() * 24 + 18);
			try {
				writeCssText(sw);
			} catch (IOException e) {
			}
			return sw.toString();
		}

		@Override
		public String getMinifiedCssText(String propertyName) {
			if (valueList.isEmpty()) {
				return "[]";
			}

			StringBuilder buf = new StringBuilder(valueList.size() * 24 + 16);
			buf.append('[').append(item(0).getMinifiedCssText(propertyName));
			int sz = valueList.size();
			for (int i = 1; i < sz; i++) {
				buf.append(' ').append(item(i).getMinifiedCssText(propertyName));
			}

			buf.append(']');
			return buf.toString();
		}

		@Override
		public void writeCssText(SimpleWriter wri) throws IOException {
			if (!valueList.isEmpty()) {
				wri.write('[');
				valueList.get(0).writeCssText(wri);
				int sz = valueList.size();
				for (int i = 1; i < sz; i++) {
					wri.write(' ');
					valueList.get(i).writeCssText(wri);
				}
				wri.write(']');
			} else {
				wri.write("[]");
			}
		}

		@Override
		public boolean isBracketList() {
			return true;
		}

		@Override
		public boolean isCommaSeparated() {
			return false;
		}

		@Override
		public ValueList clone() {
			return new BracketValueList(this);
		}

		@Override
		public ValueList wrap(String oldHrefContext, String parentSheetHref) {
			return new BracketValueListWrapper(this, oldHrefContext, parentSheetHref);
		}

		public static class BracketValueListWrapper extends WSValueList.WSValueListWrapper {

			private static final long serialVersionUID = 1L;

			BracketValueListWrapper(ValueList copy, String oldHrefContext, String parentSheetHref) {
				super(copy, oldHrefContext, parentSheetHref);
			}

			@Override
			public ValueList clone() {
				return new BracketValueListWrapper(super.clone(), oldHrefContext, getParentSheetHref());
			}

		}
	}

	abstract public ValueList wrap(String oldHrefContext, String parentSheetHref);

}
