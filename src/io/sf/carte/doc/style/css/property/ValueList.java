/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.doc.style.css.ExtendedCSSValueList;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSValueList.
 * 
 * @author Carlos Amengual
 *
 */
abstract public class ValueList extends StyleValue implements ExtendedCSSValueList<StyleValue> {

	protected final List<StyleValue> valueList;

	private ValueList() {
		super(CSSValue.CSS_VALUE_LIST);
		valueList = new ArrayList<StyleValue>();
	}

	private ValueList(ValueList copy) {
		super(CSSValue.CSS_VALUE_LIST);
		valueList = new ArrayList<StyleValue>(copy.valueList);
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
		if( value == null) {
			throw new NullPointerException("Null value added to CSSValueList");
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
			throw new NullPointerException("Null list added to CSSValueList");
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
		if( value == null) {
			throw new NullPointerException("Null value set to CSSValueList");
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
			Iterator<StyleValue> it = valueList.iterator();
			while (it.hasNext()) {
				ExtendedCSSValue value = it.next();
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
			if (!other.valueList.isEmpty()) {
				return false;
			}
		} else if (valueList.size() != other.valueList.size()) {
			return false;
		} else {
			int sz = valueList.size();
			for (int i = 0; i < sz; i++) {
				ExtendedCSSValue item = valueList.get(i);
				ExtendedCSSValue oitem = other.valueList.get(i);
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
				ExtendedCSSValue val = item(i);
				if (val.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					((PrimitiveValue) val).setSubproperty(true);
				} else if (val.getCssValueType() == CSSValue.CSS_INHERIT) {
					set(i, ((InheritValue) val).asSubproperty());
				} else if (val.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
					((ValueList) val).setSubproperty(true);
				}
			}
		}
	}

	@Override
	public boolean isSubproperty() {
		if (valueList != null) {
			Iterator<StyleValue> it = valueList.iterator();
			while (it.hasNext()) {
				if (!it.next().isSubproperty()) {
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

	public boolean isBracketList() {
		return false;
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
				if (val != null && val.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
						&& ((CSSPrimitiveValue) val).getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
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
				if (val != null && val.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
						&& ((CSSPrimitiveValue) val).getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
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
			buf.append('[').append(item(0).getCssText());
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
