/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.DOMNotSupportedException;
import io.sf.carte.doc.DOMNullCharacterException;
import io.sf.carte.doc.DOMSyntaxException;
import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSNumberValue;
import io.sf.carte.doc.style.css.MediaQuery;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.impl.MediaListAccess;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.parser.AbstractMediaQuery;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.NumberValue;

class MediaQueryListImpl implements MediaQueryList, MediaListAccess, java.io.Serializable {

	private static final long serialVersionUID = 2L;

	private final LinkedList<AbstractMediaQuery> queryList;

	private LinkedList<CSSParseException> queryErrorList = null;

	boolean invalidQueryList = false;

	private boolean hasProxy = false;

	MediaQueryListImpl() {
		super();
		queryList = new LinkedList<>();
	}

	/**
	 * Construct a media query list with a single query that has only a media type.
	 * 
	 * @param medium the media type.
	 */
	MediaQueryListImpl(String medium) {
		super();
		queryList = new LinkedList<>();
		if (medium != null && !"all".equalsIgnoreCase(medium)) {
			MediaQueryImpl query = createMediaQuery();
			query.setMediaType(medium);
			queryList.add(query);
		}
	}

	@SuppressWarnings("unchecked")
	MediaQueryListImpl(MediaQueryListImpl copyMe) {
		super();
		this.queryList = (LinkedList<AbstractMediaQuery>) copyMe.queryList.clone();
		this.invalidQueryList = copyMe.invalidQueryList;
		this.hasProxy = copyMe.hasProxy;
		if (copyMe.queryErrorList != null) {
			this.queryErrorList = copyMe.queryErrorList;
		}
	}

	protected MediaQueryImpl createMediaQuery() {
		return new MediaQueryImpl() {

			private static final long serialVersionUID = 1L;

			@Override
			protected CSSNumberValue createNumberValue(short unit, float valueInSpecifiedUnit,
					boolean calculated) {
				NumberValue value = NumberValue.createCSSNumberValue(unit, valueInSpecifiedUnit);
				value.setCalculatedNumber(calculated);
				return value;
			}

		};
	}

	@Override
	public String getMedia() {
		if (queryList.isEmpty()) {
			if (invalidQueryList) {
				return "not all";
			}
			return "all";
		}
		StringBuilder buf = new StringBuilder();
		Iterator<AbstractMediaQuery> it = queryList.iterator();
		buf.append(it.next().getMedia());
		while (it.hasNext()) {
			buf.append(',').append(it.next().getMedia());
		}
		return buf.toString();
	}

	@Override
	public String getMinifiedMedia() {
		if (queryList.isEmpty()) {
			if (invalidQueryList) {
				return "not all";
			}
			return "all";
		}
		StringBuilder buf = new StringBuilder();
		Iterator<AbstractMediaQuery> it = queryList.iterator();
		buf.append(it.next().getMinifiedMedia());
		while (it.hasNext()) {
			buf.append(',').append(it.next().getMinifiedMedia());
		}
		return buf.toString();
	}

	@Override
	public String getMediaText() {
		return getMedia();
	}

	@Override
	public void setMediaText(String mediaText) throws DOMException {
		queryList.clear();
		if (!parse(mediaText, null)) {
			throw new DOMSyntaxException("Invalid media query: " + mediaText);
		}
	}

	@Override
	public int getLength() {
		int sz = queryList.size();
		if (sz == 0 && invalidQueryList) {
			return 1; // length == 0 used to mean "all"
		}
		return sz;
	}

	@Override
	public String item(int index) {
		int sz = queryList.size();
		if (sz == 0 && invalidQueryList && index == 0) {
			return "not all";
		}
		if (index < 0 || index >= sz) {
			return null;
		}
		return queryList.get(index).getMedia();
	}

	/**
	 * Get the media query at {@code index}.
	 * 
	 * @param index the index.
	 * @return the media query at the {@code index}-th position in this list, or
	 *         {@code null} if that is not a valid index or the query list is
	 *         invalid.
	 */
	@Override
	public MediaQuery getMediaQuery(int index) {
		int sz = queryList.size();
		if ((index < 0 || index >= sz) || invalidQueryList) {
			return null;
		}
		return queryList.get(index);
	}

	@Override
	public void setMediaQuery(int index, AbstractMediaQuery query) {
		queryList.set(index, query);
	}

	/**
	 * Gives an unmodifiable view of this media query list.
	 * 
	 * @return an unmodifiable view of this media query list.
	 */
	@Override
	public MediaQueryList unmodifiable() {
		return hasProxy ? clone() : new UnmodifiableMediaQueryList();
	}

	@Override
	public void appendMedium(String newMedium) throws DOMException {
		if (!parse(newMedium, null)) {
			throw new DOMSyntaxException("Invalid media query: " + newMedium);
		}
	}

	@Override
	public void deleteMedium(String oldMedium) throws DOMException {
		throw new DOMNotSupportedException("Delete operation not supported");
	}

	/**
	 * Does this list match the given medium-canvas combination?
	 * 
	 * @param medium the lowercase name of the medium to test for, <code>null</code>
	 *               if all media.
	 * @param canvas the canvas where the document is to be rendered, or null if no
	 *               canvas.
	 * @return <code>true</code> if this list matches the supplied media name and
	 *         canvas, false otherwise.
	 */
	@Override
	public boolean matches(String medium, CSSCanvas canvas) {
		if (isAllMedia()) {
			return true;
		}

		for (AbstractMediaQuery query : queryList) {
			if (query.matches(medium, canvas)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Does the given media list contain any media present in this list?
	 * <p>
	 * If query list A matches B, then if a medium matches B it will also match A.
	 * The opposite may not be true.
	 * 
	 * @param otherMedia the other media list to test.
	 * @return <code>true</code> if the other media contains any media which applies
	 *         to this list, <code>false</code> otherwise.
	 */
	@Override
	public boolean matches(MediaQueryList otherMedia) {
		if (otherMedia == null) {
			return !isNotAllMedia(); // null list handled as "all"
		}
		if (otherMedia.isNotAllMedia()) {
			return false;
		}
		if (isAllMedia()) {
			return true;
		}
		if (otherMedia.isAllMedia()) {
			// If we are here, this is not "all".
			return false;
		}
		MediaQueryListImpl otherqlist;
		if (otherMedia instanceof MediaQueryListImpl) {
			otherqlist = (MediaQueryListImpl) otherMedia;
		} else if (otherMedia.getClass() == UnmodifiableMediaQueryList.class) {
			otherqlist = ((UnmodifiableMediaQueryList) otherMedia).getEnclosingInstance();
		} else {
			// Old implementation
			return oldMatch(otherMedia);
		}
		// Prepare a set of other media
		HashSet<AbstractMediaQuery> otherList = new HashSet<>(otherqlist.queryList.size());
		otherList.addAll(otherqlist.queryList);
		for (AbstractMediaQuery query : queryList) {
			Iterator<AbstractMediaQuery> otherIt = otherList.iterator();
			while (otherIt.hasNext()) {
				AbstractMediaQuery othermq = otherIt.next();
				if (query.matches(othermq)) {
					otherIt.remove();
				}
			}
			if (otherList.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private boolean oldMatch(MediaQueryList otherMedia) {
		int sz = getLength();
		HashSet<String> mediastringList = new HashSet<>(sz);
		for (int i = 0; i < sz; i++) {
			String item = item(i).toLowerCase(Locale.ROOT);
			mediastringList.add(item);
		}
		int osz = otherMedia.getLength();
		for (int i = 0; i < osz; i++) {
			String item = otherMedia.item(i).toLowerCase(Locale.ROOT);
			if (mediastringList.contains(item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is this an all-media list?
	 * 
	 * @return <code>true</code> if this list matches all media, <code>false</code>
	 *         otherwise.
	 */
	@Override
	public boolean isAllMedia() {
		return queryList.isEmpty() && !invalidQueryList;
	}

	@Override
	public boolean isNotAllMedia() {
		return invalidQueryList || (queryList.size() == 1 && queryList.get(0).isNotAllMedia());
	}

	@Override
	public boolean hasProxy() {
		return hasProxy;
	}

	/**
	 * Did this media query list produce errors when being parsed ?
	 * 
	 * @return <code>true</code> if this list come from a media string that produced
	 *         errors when parsed, <code>false</code> otherwise.
	 */
	@Override
	public boolean hasErrors() {
		return invalidQueryList || queryErrorList != null;
	}

	/**
	 * Get the exceptions found while parsing the query, if any.
	 * 
	 * @return the exceptions found while parsing the query, or <code>null</code> if
	 *         no errors were found while parsing the media query.
	 */
	@Override
	public LinkedList<CSSParseException> getExceptions() {
		return queryErrorList;
	}

	@Override
	public int hashCode() {
		int result = 1;
		if (queryList != null) {
			for (MediaQuery mq : queryList) {
				result += mq.hashCode();
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		MediaQueryListImpl other;
		if (obj.getClass() == UnmodifiableMediaQueryList.class) {
			other = ((UnmodifiableMediaQueryList) obj).getEnclosingInstance();
		} else if (obj instanceof MediaQueryListImpl) {
			other = (MediaQueryListImpl) obj;
		} else {
			return false;
		}
		if (queryList == null) {
			if (other.queryList != null)
				return false;
		} else if (queryList.size() != other.queryList.size() || !queryList.containsAll(other.queryList)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getMedia();
	}

	@Override
	public MediaQueryList clone() {
		return new MediaQueryListImpl(this);
	}

	private class UnmodifiableMediaQueryList implements MediaQueryList, MediaListAccess {

		UnmodifiableMediaQueryList() {
			super();
		}

		@Override
		public MediaQuery getMediaQuery(int index) {
			return MediaQueryListImpl.this.getMediaQuery(index);
		}

		@Override
		public String getMedia() {
			return MediaQueryListImpl.this.getMedia();
		}

		@Override
		public String getMinifiedMedia() {
			return MediaQueryListImpl.this.getMinifiedMedia();
		}

		@Override
		public String getMediaText() {
			return getMedia();
		}

		@Override
		public int getLength() {
			return MediaQueryListImpl.this.getLength();
		}

		@Override
		public String item(int index) {
			return MediaQueryListImpl.this.item(index);
		}

		@Override
		public boolean isAllMedia() {
			return MediaQueryListImpl.this.isAllMedia();
		}

		@Override
		public boolean isNotAllMedia() {
			return MediaQueryListImpl.this.isNotAllMedia();
		}

		@Override
		public boolean matches(MediaQueryList otherMedia) {
			return MediaQueryListImpl.this.matches(otherMedia);
		}

		@Override
		public boolean matches(String medium, CSSCanvas canvas) {
			return MediaQueryListImpl.this.matches(medium, canvas);
		}

		@Override
		public MediaQueryList unmodifiable() {
			return this;
		}

		@Override
		public MediaQueryList clone() {
			return this;
		}

		@Override
		public boolean hasProxy() {
			return false;
		}

		@Override
		public boolean hasErrors() {
			return MediaQueryListImpl.this.hasErrors();
		}

		@Override
		public LinkedList<CSSParseException> getExceptions() {
			return MediaQueryListImpl.this.getExceptions();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void addListener(io.sf.carte.doc.style.css.MediaQueryListListener listener) {
			MediaQueryListImpl.this.addListener(listener);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void removeListener(io.sf.carte.doc.style.css.MediaQueryListListener listener) {
			MediaQueryListImpl.this.removeListener(listener);
		}

		@Override
		public int hashCode() {
			return MediaQueryListImpl.this.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return MediaQueryListImpl.this.equals(obj);
		}

		@Override
		public String toString() {
			return getMedia();
		}

		@Override
		public void setMediaQuery(int index, AbstractMediaQuery query) {
			throw createNoModificationAllowedException();
		}

		@Override
		public void setMediaText(String mediaText) throws DOMException {
			throw createNoModificationAllowedException();
		}

		@Override
		public void appendMedium(String newMedium) throws DOMException {
			throw createNoModificationAllowedException();
		}

		@Override
		public void deleteMedium(String oldMedium) throws DOMException {
			throw createNoModificationAllowedException();
		}

		private DOMException createNoModificationAllowedException() {
			return new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					"Cannot modify target media: you must re-create the style sheet with a different media list.");
		}

		private MediaQueryListImpl getEnclosingInstance() {
			return MediaQueryListImpl.this;
		}

	}

	/**
	 * Parses the given media query string.
	 * <p>
	 * Does not reset the media query list, but adds to it.
	 * 
	 * @param mediaQueryString the media query string.
	 * @param owner            the owner node that would process errors.
	 * @return <code>true</code> if the query list is not invalid. Note that if this
	 *         query list already contains a valid query, it will never return
	 *         <code>false</code>.
	 */
	boolean parse(String mediaQueryString, Node owner) {
		CSSValueMediaQueryFactory mqf = getMediaQueryFactory();
		Parser parser = mqf.createParser();
		MyMediaQueryHandler qhandler = new MyMediaQueryHandler(owner);
		invalidQueryList = false;
		parser.parseMediaQueryList(mediaQueryString, mqf, qhandler);
		return !invalidQueryList;
	}

	protected CSSValueMediaQueryFactory getMediaQueryFactory() {
		return new CSSValueMediaQueryFactory();
	}

	class MyMediaQueryHandler implements io.sf.carte.doc.style.css.MediaQueryHandler {
		private MediaQueryImpl currentQuery;
		private boolean invalidQuery = false;
		private boolean compatQuery = false;
		private boolean allMedia = false;
		private final Node ownerNode;

		MyMediaQueryHandler(Node ownerNode) {
			super();
			this.ownerNode = ownerNode;
		}

		@Override
		public void startQuery() {
			currentQuery = createMediaQuery();
		}

		@Override
		public void mediaType(String mediaType) {
			String unescaped;
			try {
				unescaped = ParseHelper.unescapeStringValue(mediaType, false, false);
			} catch (DOMNullCharacterException e) {
				unescaped = mediaType;
			}
			currentQuery.setMediaType(unescaped);
		}

		@Override
		public void negativeQuery() {
			currentQuery.setNegative(true);
		}

		@Override
		public void onlyPrefix() {
			currentQuery.setOnlyPrefix(true);
		}

		@Override
		public void condition(BooleanCondition condition) {
			currentQuery.setFeaturePredicate(condition);
		}

		@Override
		public void endQuery() {
			if (!invalidQuery) {
				if (!currentQuery.isNotAllMedia() || !containsNotAll()) {
					queryList.add(currentQuery);
				}
				if (invalidQueryList && !compatQuery) {
					invalidQueryList = false;
				}
				if (currentQuery.isAllMedia()) {
					allMedia = true;
				}
			}
			currentQuery = null;
			invalidQuery = false;
			compatQuery = false;
		}

		private boolean containsNotAll() {
			for (AbstractMediaQuery query : queryList) {
				if (query.isNotAllMedia()) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void endQueryList() {
			if (allMedia) {
				queryList.clear();
			}
		}

		@Override
		public MediaQueryList getMediaQueryList() {
			return MediaQueryListImpl.this;
		}

		@Override
		public void setContainsProxy() {
			hasProxy = true;
		}

		@Override
		public boolean reportsErrors() {
			return ownerNode != null;
		}

		@Override
		public void invalidQuery(CSSParseException queryError) {
			invalidQuery = true;
			invalidQueryList = true;
			if (queryErrorList == null) {
				queryErrorList = new LinkedList<>();
			}
			queryErrorList.add(queryError);
			if (ownerNode != null) {
				CSSMediaException e = new CSSMediaException(queryError);
				((CSSDocument) ownerNode.getOwnerDocument()).getErrorHandler().mediaQueryError(ownerNode, e);
			}
		}

		@Override
		public void compatQuery(CSSParseException exception) {
			invalidQueryList = true;
			compatQuery = true;
			if (queryErrorList == null) {
				queryErrorList = new LinkedList<>();
			}
			queryErrorList.add(exception);
			if (ownerNode != null) {
				CSSMediaException e = new CSSMediaException(exception);
				((CSSDocument) ownerNode.getOwnerDocument()).getErrorHandler().mediaQueryWarning(ownerNode, e);
			}
		}

	}

}
