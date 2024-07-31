/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.MediaQuery;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.MediaQueryListListener;
import io.sf.carte.doc.style.css.nsac.CSSParseException;

/**
 * Old <code>MediaList</code> and <code>MediaQueryList</code> implementation,
 * supporting lists of plain media types (not media queries).
 * <p>
 * Used for media query interoperability tests.
 * </p>
 * 
 * @author Carlos Amengual
 * 
 */
public class MediaList implements MediaQueryList, Serializable {

	private static final long serialVersionUID = 1L;

	private static final MediaList allMediaSingleton;

	private final List<String> mediastringList; // list of individual media, empty if 'all'

	private final List<String> mediaList; // all of the individual media in this list

	private boolean allMedia;

	static {
		// prepare singleton
		allMediaSingleton = new UnmodifiableMediaList();
		allMediaSingleton.allMedia = true;
	}

	private MediaList() {
		super();
		mediastringList = new ArrayList<>(5);
		mediaList = new ArrayList<>(5);
	}

	private MediaList(List<String> listRef, List<String> mediaList) {
		super();
		mediastringList = listRef;
		this.mediaList = mediaList;
	}

	/**
	 * Create a media list for all media.
	 * 
	 * @return the media list.
	 */
	public static MediaList createMediaList() {
		MediaList mlist = new MediaList();
		mlist.allMedia = true;
		return mlist;
	}

	/**
	 * Create a media list for the given media.
	 * 
	 * @param media
	 *            the media specification.
	 * @return the media list.
	 */
	public static MediaList createMediaList(String media) {
		if (media == null) {
			throw new NullPointerException("Null media.");
		}
		MediaList mlist = new MediaList();
		mlist.setMediaText(media);
		return mlist;
	}

	public static MediaList createFromMediaList(org.w3c.dom.stylesheets.MediaList list) {
		MediaList mlist = new MediaList();
		if (list == null) {
			mlist.allMedia = true;
		} else {
			mlist.allMedia = false;
			int mll = list.getLength();
			for (int i = 0; i < mll; i++) {
				String newMedium = list.item(i);
				if ("all".equals(newMedium)) {
					mlist.allMedia = true;
					mlist.mediastringList.clear();
					return mlist;
				} else {
					mlist.addMedium(newMedium);
				}
			}
		}
		return mlist;
	}

	/**
	 * Create an unmodifiable media list for all media.
	 * 
	 * @return the unmodifiable media list.
	 */
	public static MediaList createUnmodifiable() {
		return allMediaSingleton;
	}

	/**
	 * Create an unmodifiable media list for the given media.
	 * 
	 * @param media
	 *            the comma-separated list of media. If <code>null</code>, the media list will
	 *            be for all media.
	 * @return the unmodifiable media list.
	 */
	public static MediaList createUnmodifiable(String media) {
		if (media == null) {
			return createUnmodifiable();
		}
		return new UnmodifiableMediaList(media);
	}

	/**
	 * Create a media list for the given SAC media list.
	 * 
	 * @param media
	 *            the media list. If <code>null</code>, the list will be for all
	 *            media.
	 * @return the media list.
	 */
	public static MediaList createMediaList(List<String> media) {
		MediaList newlist = new MediaList();
		newlist.allMedia = true;
		if (media != null) {
			int sz = media.size();
			for (int i = 0; i < sz; i++) {
				newlist.appendMedium(media.get(i));
			}
		}
		return newlist;
	}

	@Override
	public MediaQuery getMediaQuery(int index) {
		return null;
	}

	@Override
	public String getMedia() {
		return getMediaText();
	}

	@Override
	public String getMediaText() {
		if (allMedia) {
			return "all";
		}
		StringBuilder sb = new StringBuilder(mediastringList.size() * 8 + 2);
		Iterator<String> it = mediastringList.iterator();
		if (it.hasNext()) {
			sb.append(ParseHelper.escape(it.next()));
		}
		while (it.hasNext()) {
			sb.append(',').append(ParseHelper.escape(it.next()));
		}
		return sb.toString();
	}

	@Override
	public String getMinifiedMedia() {
		return getMediaText();
	}

	@Override
	public void setMediaText(String mediaText) throws DOMException {
		allMedia = false;
		mediastringList.clear();
		mediaList.clear();
		StringTokenizer st = new StringTokenizer(mediaText, ",");
		while (st.hasMoreElements()) {
			String medium = st.nextToken().trim().toLowerCase(Locale.ROOT);
			medium = ParseHelper.unescapeStringValue(medium);
			if ("all".equals(medium)) {
				allMedia = true;
				mediastringList.clear();
				return;
			} else {
				addMedium(medium);
			}
		}
	}

	void parseMediaText(String mediaText) {
		allMedia = false;
		StringTokenizer st = new StringTokenizer(mediaText, ",");
		while (st.hasMoreElements()) {
			String newMedium = st.nextToken().trim().toLowerCase(Locale.ROOT);
			if ("all".equals(newMedium)) {
				allMedia = true;
				mediastringList.clear();
				return;
			} else {
				addMedium(newMedium);
			}
		}
	}

	@Override
	public int getLength() {
		if (allMedia) {
			return 0;
		}
		return mediastringList.size();
	}

	@Override
	public String item(int index) {
		try {
			return mediastringList.get(index);
		} catch (IndexOutOfBoundsException e) {
			if (allMedia && index == 0) {
				return "all";
			}
			return null;
		}
	}

	@Override
	public void deleteMedium(String oldMedium) throws DOMException {
		if (!mediastringList.remove(oldMedium)) {
			throw new DOMException(DOMException.NOT_FOUND_ERR, oldMedium + " not in media list.");
		}
		mediaList.remove(oldMedium);
	}

	@Override
	public void appendMedium(String newMedium) throws DOMException {
		if (newMedium == null) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "Null medium");
		}
		String lcnm = newMedium.toLowerCase(Locale.ROOT);
		if ("all".equals(newMedium)) {
			allMedia = true;
			mediastringList.clear();
		} else {
			addMedium(lcnm);
		}
	}

	private void addMedium(String newMedium) {
		if ("all".equals(newMedium)) {
			allMedia = true;
			mediastringList.clear();
		} else {
			if (newMedium == null) {
				throw new NullPointerException("New medium cannot be null");
			}
			newMedium = newMedium.intern();
			mediastringList.add(newMedium);
			mediaList.add(newMedium);
			allMedia = false;
		}
	}

	/**
	 * Is this an all-media list?
	 * 
	 * @return <code>true</code> if this list matches all media, <code>false</code> otherwise.
	 */
	@Override
	public boolean isAllMedia() {
		return allMedia;
	}

	@Override
	public boolean isNotAllMedia() {
		// This list only contains valid media or 'all'.
		return false;
	}

	/**
	 * Did this media query list produce errors when being parsed ?
	 * 
	 * @return <code>true</code> if this list come from a media string that produced errors when
	 *         parsed, <code>false</code> otherwise.
	 */
	@Override
	public boolean hasErrors() {
		return false;
	}

	@Override
	public List<CSSParseException> getExceptions() {
		return null;
	}

	/**
	 * Does the given media list contain any media present in this list?
	 * 
	 * @param otherMedia
	 *            the other media list to test.
	 * @return <code>true</code> if the other list contains any media which belongs to this
	 *         list, <code>false</code> otherwise.
	 */
	@Override
	public boolean matches(MediaQueryList otherMedia) {
		if (isAllMedia()) {
			return true;
		}
		if (otherMedia == null) {
			return !isNotAllMedia(); // null list handled as "all"
		}
		int sz = otherMedia.getLength();
		for (int i = 0; i < sz; i++) {
			String iitem = otherMedia.item(i).toLowerCase(Locale.ROOT);
			if (mediastringList.contains(iitem) || "all".equals(iitem)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Does this list match the given medium-canvas combination?
	 * <p>
	 * Unless <code>medium</code> is a media query string, matches are according
	 * to HTML4 spec, section 6.13.
	 * </p>
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
		if (allMedia) {
			return true;
		}
		if (medium == null) {
			return false;
		}
		if (medium.indexOf(' ') != -1) {
			return mediastringList.contains(medium);
		} else {
			// HTML4 spec, section 6.13
			return mediaList.contains(medium);
		}
	}

	/**
	 * Gives an unmodifiable view of this media list.
	 * 
	 * @return an unmodifiable view of this media list.
	 */
	public MediaList unmodifiable() {
		if (allMedia) {
			return allMediaSingleton;
		} else {
			return new UnmodifiableMediaList(this);
		}
	}

	@Override
	public int hashCode() {
		int result = 1;
		if (mediastringList != null) {
			for (String media : mediastringList) {
				result += media.hashCode();
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MediaList)) {
			return false;
		}
		MediaList other = (MediaList) obj;
		if (mediastringList == null) {
			if (other.mediastringList != null) {
				return false;
			}
		} else if (mediastringList.size() != other.mediastringList.size()
				|| !mediastringList.containsAll(other.mediastringList)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getMediaText();
	}

	@Override
	public void addListener(MediaQueryListListener listener) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "You should use CSSCanvas for this");
	}

	@Override
	public void removeListener(MediaQueryListListener listener) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "You should use CSSCanvas for this");
	}

	private static class UnmodifiableMediaList extends MediaList {

		private static final long serialVersionUID = 2L;

		private UnmodifiableMediaList() {
			super();
		}

		private UnmodifiableMediaList(String media) {
			super();
			parseMediaText(media);
		}

		private UnmodifiableMediaList(MediaList list) {
			super(list.mediastringList, list.mediaList);
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

}
