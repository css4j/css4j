/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.DOMNullCharacterException;
import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.MediaQueryListListener;
import io.sf.carte.doc.style.css.parser.BooleanCondition;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.MediaQueryHandler;
import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * This factory has several static media-related methods.
 */
public class MediaQueryFactory {

	private static final HashSet<String> mediaFeatureSet;
	private static final HashSet<String> rangeFeatureSet;

	static {
		final String[] rangeFeatures = { "aspect-ratio", "color", "color-index", "height", "monochrome", "resolution",
				"width" };
		final String[] discreteFeatures = { "any-hover", "any-pointer", "color-gamut", "grid", "hover", "orientation",
				"overflow-block", "overflow-inline", "pointer", "scan", "update" };
		mediaFeatureSet = new HashSet<String>(rangeFeatures.length + discreteFeatures.length);
		rangeFeatureSet = new HashSet<String>(rangeFeatures.length);
		Collections.addAll(rangeFeatureSet, rangeFeatures);
		Collections.addAll(mediaFeatureSet, rangeFeatures);
		Collections.addAll(mediaFeatureSet, discreteFeatures);
	}

	/**
	 * Creates a new media list for <code>mediaQueryString</code>.
	 * 
	 * @param mediaQueryString
	 *            the media query string.
	 * @param owner
	 *            the node that would handle errors, if any.
	 * @return a new media list for <code>mediaQueryString</code>, or
	 *         <code>null</code> if the media query list could not be parsed for the
	 *         given canvas.
	 */
	public static MediaQueryList createMediaList(String mediaQueryString, Node owner) {
		if (isPlainMediaList(mediaQueryString)) {
			return MediaList.createMediaList(mediaQueryString);
		}
		return createMediaQueryList(mediaQueryString, owner);
	}

	/**
	 * Create an unmodifiable media query list for the given media.
	 * 
	 * @param media
	 *            the comma-separated list of media. If <code>null</code>, the
	 *            media list will be for all media.
	 * @param owner
	 *            the node that would handle errors, if any.
	 * @return the unmodifiable media list.
	 */
	public static MediaQueryList createUnmodifiable(String media, Node owner) {
		if (media == null) {
			return MediaList.createUnmodifiable();
		}
		if (isPlainMediaList(media)) {
			return MediaList.createUnmodifiable(media);
		}
		return ((MediaListAccess) createMediaQueryList(media, owner)).unmodifiable();
	}

	/**
	 * Creates a new media query list for <code>mediaQueryString</code>.
	 * 
	 * @param mediaQueryString
	 *            the media query string.
	 * @param owner
	 *            the node that would handle errors, if any.
	 * @return a new media query list for <code>mediaQueryString</code>.
	 */
	public static MediaQueryList createMediaQueryList(String mediaQueryString, Node owner) {
		MyMediaQueryList qlist = new MyMediaQueryList();
		qlist.parse(mediaQueryString, owner);
		return qlist;
	}

	/**
	 * Create a media query list for the given SAC media list.
	 * 
	 * @param media
	 *            the SAC media list.
	 * @return the media query list.
	 */
	public static MediaQueryList createMediaList(SACMediaList media) {
		int sz = media.getLength();
		boolean plainMedium = true;
		for (int i = 0; i < sz; i++) {
			if (!MediaList.isPlainMedium(media.item(i))) {
				plainMedium = false;
				break;
			}
		}
		if (plainMedium) {
			return MediaList.createMediaList(media);
		} else {
			MyMediaQueryList qlist = new MyMediaQueryList();
			for (int i = 0; i < sz; i++) {
				qlist.parse(media.item(i), null);
			}
			return qlist;
		}
	}

	static boolean isPlainMediaList(String newMedium) {
		StringTokenizer st = new StringTokenizer(newMedium, ",");
		while (st.hasMoreTokens()) {
			if (!MediaList.isPlainMedium(st.nextToken().trim())) {
				return false;
			}
		}
		return true;
	}

	public static boolean isMediaFeature(String string) {
		return mediaFeatureSet.contains(string);
	}

	static boolean isRangeFeature(String string) {
		return rangeFeatureSet.contains(string);
	}

	private static class MyMediaQueryList implements MediaQueryList, MediaListAccess {

		private LinkedList<MediaQuery> queryList = new LinkedList<MediaQuery>();

		private LinkedList<CSSParseException> queryErrorList = null;

		boolean invalidQueryList = false;

		public MyMediaQueryList() {
			super();
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
			Iterator<MediaQuery> it = queryList.iterator();
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
			Iterator<MediaQuery> it = queryList.iterator();
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
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad media query: " + mediaText);
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
		 * Gives an unmodifiable view of this media query list.
		 * 
		 * @return an unmodifiable view of this media query list.
		 */
		@Override
		public MediaQueryList unmodifiable() {
			return new UnmodifiableMediaQueryList();
		}

		@Override
		public void appendMedium(String newMedium) throws DOMException {
			if (!parse(newMedium, null)) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Bad media query: " + newMedium);
			}
		}

		@Override
		public void deleteMedium(String oldMedium) throws DOMException {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Delete operation not supported");
		}

		/**
		 * Does this list match the given medium-canvas combination?
		 * 
		 * @param medium
		 *            the lowercase name of the medium to test for.
		 * @param canvas
		 *            the canvas where the document is to be rendered, or null if no
		 *            canvas.
		 * @return <code>true</code> if this list matches the supplied media name and canvas, false
		 *         otherwise.
		 */
		@Override
		public boolean matches(String medium, CSSCanvas canvas) {
			Iterator<MediaQuery> it = queryList.iterator();
			while (it.hasNext()) {
				if (it.next().matches(medium, canvas)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Does the given SAC media list contain any media present in this list?
		 * 
		 * @param sacMedia
		 *            the SAC media list to test.
		 * @return <code>true</code> if the SAC media contains any media which applies to this
		 *         list, <code>false</code> otherwise.
		 */
		@Override
		public boolean match(SACMediaList sacMedia) {
			if (isAllMedia()) {
				return true;
			}
			if (sacMedia == null) {
				return !isNotAllMedia(); // null list handled as "all"
			}
			MyMediaQueryList otherqlist = (MyMediaQueryList) MediaQueryFactory.createMediaList(sacMedia);
			if (otherqlist.isAllMedia()) {
				return true;
			}
			int sz = otherqlist.queryList.size();
			for (int i = 0; i < sz; i++) {
				if (queryList.contains(otherqlist.queryList.get(i))) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void addListener(MediaQueryListListener listener) throws DOMException {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "You should use CSSCanvas for this");
		}

		@Override
		public void removeListener(MediaQueryListListener listener) throws DOMException {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "You should use CSSCanvas for this");
		}

		/**
		 * Is this an all-media list?
		 * 
		 * @return <code>true</code> if this list matches all media, <code>false</code> otherwise.
		 */
		@Override
		public boolean isAllMedia() {
			return queryList.isEmpty() && !invalidQueryList;
		}

		@Override
		public boolean isNotAllMedia() {
			return queryList.isEmpty();
		}

		/**
		 * Did this media query list produce errors when being parsed ?
		 * 
		 * @return <code>true</code> if this list come from a media string that produced errors when
		 *         parsed, <code>false</code> otherwise.
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
			if (!(obj instanceof MyMediaQueryList))
				return false;
			MyMediaQueryList other = (MyMediaQueryList) obj;
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

		private class UnmodifiableMediaQueryList implements MediaQueryList, MediaListAccess {

			UnmodifiableMediaQueryList() {
				super();
			}

			@Override
			public String getMedia() {
				return MyMediaQueryList.this.getMedia();
			}

			@Override
			public String getMinifiedMedia() {
				return MyMediaQueryList.this.getMinifiedMedia();
			}

			@Override
			public String getMediaText() {
				return getMedia();
			}

			@Override
			public int getLength() {
				return MyMediaQueryList.this.getLength();
			}

			@Override
			public String item(int index) {
				return MyMediaQueryList.this.item(index);
			}

			@Override
			public boolean isAllMedia() {
				return MyMediaQueryList.this.isAllMedia();
			}

			@Override
			public boolean isNotAllMedia() {
				return MyMediaQueryList.this.isNotAllMedia();
			}

			@Override
			public boolean match(SACMediaList sacMedia) {
				return MyMediaQueryList.this.match(sacMedia);
			}

			@Override
			public boolean matches(String medium, CSSCanvas canvas) {
				return MyMediaQueryList.this.matches(medium, canvas);
			}

			@Override
			public MediaQueryList unmodifiable() {
				return this;
			}

			@Override
			public boolean hasErrors() {
				return MyMediaQueryList.this.hasErrors();
			}

			@Override
			public LinkedList<CSSParseException> getExceptions() {
				return MyMediaQueryList.this.getExceptions();
			}

			@Override
			public void addListener(MediaQueryListListener listener) {
				MyMediaQueryList.this.addListener(listener);
			}

			@Override
			public void removeListener(MediaQueryListListener listener) {
				MyMediaQueryList.this.removeListener(listener);
			}

			@Override
			public void setMediaText(String mediaText) throws DOMException {
				throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
						"Cannot modify target media: you must re-create the style sheet with a different media list.");
			}

			@Override
			public void appendMedium(String newMedium) throws DOMException {
				throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
						"Cannot modify target media: you must re-create the style sheet with a different media list.");
			}

			@Override
			public void deleteMedium(String oldMedium) throws DOMException {
				throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
						"Cannot modify target media: you must re-create the style sheet with a different media list.");
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
			invalidQueryList = false;
			CSSParser parser = new CSSParser();
			MediaQueryHandler qhandler = new MyMediaQueryHandler(owner);
			parser.parseMediaQuery(mediaQueryString, new MediaConditionFactoryImpl(), qhandler);
			if (invalidQueryList && !queryList.isEmpty()) {
				invalidQueryList = false;
			}
			return !invalidQueryList;
		}

		private class MyMediaQueryHandler implements io.sf.carte.doc.style.css.parser.MediaQueryHandler {
			private MediaQuery currentQuery;
			private boolean invalidQuery = false;
			private final Node ownerNode;

			MyMediaQueryHandler(Node ownerNode) {
				super();
				this.ownerNode = ownerNode;
			}

			@Override
			public void startQuery() {
				currentQuery = new MediaQuery();
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
					queryList.add(currentQuery);
				}
				currentQuery = null;
				invalidQuery = false;
			}

			@Override
			public void invalidQuery(CSSParseException queryError) {
				invalidQuery = true;
				invalidQueryList = true;
				if (queryErrorList == null) {
					queryErrorList = new LinkedList<CSSParseException>();
				}
				queryErrorList.add(queryError);
				if (ownerNode != null) {
					CSSMediaException e = new CSSMediaException(queryError);
					((CSSDocument) ownerNode.getOwnerDocument()).getErrorHandler().mediaQueryError(ownerNode, e);
				}
			}

		}

	}

}
