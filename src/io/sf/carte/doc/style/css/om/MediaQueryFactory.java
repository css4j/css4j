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

import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMNullCharacterException;
import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.ExtendedCSSValue;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.MediaQueryListListener;
import io.sf.carte.doc.style.css.parser.ParseHelper;

/**
 * This factory has several static media-related methods.
 */
public class MediaQueryFactory {

	private static final HashSet<String> mediaFeatureSet;

	static {
		final String[] mediaFeatures = { "any-hover", "any-pointer", "aspect-ratio", "color", "color-gamut",
				"color-index", "grid", "height", "hover", "monochrome", "orientation", "overflow-block",
				"overflow-inline", "pointer", "resolution", "scan", "update", "width" };
		mediaFeatureSet = new HashSet<String>(mediaFeatures.length);
		Collections.addAll(mediaFeatureSet, mediaFeatures);
	}

	/**
	 * Creates a new media list for <code>mediaQueryString</code>.
	 * 
	 * @param mediaQueryString
	 *            the media query string.
	 * @return a new media list for <code>mediaQueryString</code>, or
	 *         <code>null</code> if the media query list could not be parsed for the
	 *         given canvas.
	 */
	public static MediaQueryList createMediaList(String mediaQueryString) {
		if (isPlainMediaList(mediaQueryString)) {
			return MediaList.createMediaList(mediaQueryString);
		}
		return createMediaQueryList(mediaQueryString);
	}

	/**
	 * Create an unmodifiable media query list for the given media.
	 * 
	 * @param media
	 *            the comma-separated list of media. If <code>null</code>, the
	 *            media list will be for all media.
	 * @return the unmodifiable media list.
	 */
	public static MediaQueryList createUnmodifiable(String media) {
		if (media == null) {
			return MediaList.createUnmodifiable();
		}
		if (isPlainMediaList(media)) {
			return MediaList.createUnmodifiable(media);
		}
		return ((MediaListAccess) createMediaQueryList(media)).unmodifiable();
	}

	/**
	 * Creates a new media query list for <code>mediaQueryString</code>.
	 * 
	 * @param mediaQueryString
	 *            the media query string.
	 * @return a new media query list for <code>mediaQueryString</code>.
	 */
	public static MediaQueryList createMediaQueryList(String mediaQueryString) {
		MyMediaQueryList qlist = new MyMediaQueryList();
		qlist.parse(mediaQueryString);
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
				qlist.parse(media.item(i));
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

	static class MyMediaQueryList implements MediaQueryList, MediaListAccess {

		LinkedList<MediaQuery> queryList = new LinkedList<MediaQuery>();

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
			if (!parse(mediaText)) {
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
			if (!parse(newMedium)) {
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

		/**
		 * Append the contents of the given SAC media list to this one.
		 * 
		 * @param sacMedia
		 *            the SAC media to add.
		 */
		@Override
		public void appendSACMediaList(SACMediaList sacMedia) {
			int sz = sacMedia.getLength();
			for (int i = 0; i < sz; i++) {
				parse(sacMedia.item(i));
			}
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
			return invalidQueryList;
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
			public void appendSACMediaList(SACMediaList sacMedia) {
				throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
						"Cannot modify target media: you must re-create the style sheet with a different media list.");
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

		boolean parse(String mediaQueryString) {
			invalidQueryList = false;
			MediaQueryParser.parse(mediaQueryString, new MyMediaQueryHandler());
			if (invalidQueryList && !queryList.isEmpty()) {
				invalidQueryList = false;
			}
			return !invalidQueryList;
		}

		public class MyMediaQueryHandler implements MediaQueryHandler {
			MediaQuery currentQuery;
			boolean invalidQuery = false;

			MyMediaQueryHandler() {
				super();
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
			public void featureValue(String featureName, ExtendedCSSValue value) {
				featureName = ParseHelper.unescapeStringValue(featureName);
				currentQuery.addFeature(featureName, (byte) 0, value, null);
			}

			@Override
			public void featureRange(String featureName, byte rangeType, ExtendedCSSValue minvalue,
					ExtendedCSSValue maxvalue) {
				featureName = ParseHelper.unescapeStringValue(featureName);
				currentQuery.addFeature(featureName, rangeType, minvalue, maxvalue);
			}

			@Override
			public void endQuery() {
				if (!invalidQuery) {
					queryList.add(currentQuery);
				}
				currentQuery = new MediaQuery();
				invalidQuery = false;
			}

			@Override
			public void invalidQuery(String message) {
				invalidQuery = true;
				invalidQueryList = true;
			}

		}

	}

}
