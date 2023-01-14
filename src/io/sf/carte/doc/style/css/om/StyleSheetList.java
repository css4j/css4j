/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.w3c.dom.DOMStringList;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.StyleSheet;

import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.CSSStyleSheetList;
import io.sf.carte.util.Visitor;

/**
 * Abstract base implementation class for style sheet lists.
 */
abstract public class StyleSheetList implements CSSStyleSheetList<AbstractCSSRule>, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private final List<AbstractCSSStyleSheet> list;

	private final MyDOMStringList styleSheetSets = new MyDOMStringList();

	private String preferredStyleSheetSet = "";

	private boolean needsUpdate;

	protected StyleSheetList(int initialCapacity) {
		super();
		list = new ArrayList<AbstractCSSStyleSheet>(initialCapacity);
		needsUpdate = true;
	}

	@Override
	public int getLength() {
		if (needsUpdate) {
			update();
		}
		return list.size();
	}

	@Override
	public AbstractCSSStyleSheet item(int index) {
		if (needsUpdate) {
			update();
		}
		if (index >= list.size() || index < 0) {
			return null;
		}
		return list.get(index);
	}

	public DOMStringList getStyleSheetSets() {
		return styleSheetSets;
	}

	@Override
	public void acceptStyleRuleVisitor(Visitor<CSSStyleRule> visitor) {
		if (needsUpdate) {
			update();
		}
		for (AbstractCSSStyleSheet sheet : list) {
			sheet.acceptStyleRuleVisitor(visitor);
		}
	}

	@Override
	public void acceptDeclarationRuleVisitor(Visitor<CSSDeclarationRule> visitor) {
		if (needsUpdate) {
			update();
		}
		for (AbstractCSSStyleSheet sheet : list) {
			sheet.acceptDeclarationRuleVisitor(visitor);
		}
	}

	@Override
	public void acceptDescriptorRuleVisitor(Visitor<CSSDeclarationRule> visitor) {
		if (needsUpdate) {
			update();
		}
		for (AbstractCSSStyleSheet sheet : list) {
			sheet.acceptDescriptorRuleVisitor(visitor);
		}
	}

	/**
	 * Add the <code>sheet</code> style sheet to this list.
	 * 
	 * @param sheet
	 *            the style sheet.
	 */
	public void add(AbstractCSSStyleSheet sheet) {
		if (sheet != null) {
			list.add(sheet);
			String title = sheet.getTitle();
			if (title != null && title.length() > 0) { // Persistent sheets are
														// excluded
				styleSheetSets.add(title);
				// Per HTML4 spec § 14.3.2:
				// "If two or more LINK elements specify a preferred
				//  style sheet, the first one takes precedence."
				if (!sheet.getDisabled() && preferredStyleSheetSet.length() == 0) {
					preferredStyleSheetSet = title;
				}
			}
		}
	}

	/**
	 * Gets the preferred style sheet set as obtained from the sheets in the
	 * list.
	 * 
	 * @return the preferred style sheet set, or the empty string if none is
	 *         preferred.
	 */
	public String getPreferredStyleSheetSet() {
		if (needsUpdate) {
			update();
		}
		return preferredStyleSheetSet;
	}

	/**
	 * Remove the sheet with the given <code>title</code> from this list.
	 * 
	 * @param title the title of the sheet to remove.
	 */
	public void remove(String title) {
		Iterator<AbstractCSSStyleSheet> it = list.iterator();
		while (it.hasNext()) {
			CSSStyleSheet sheet = it.next();
			if (title.equals(sheet.getTitle())) {
				list.remove(sheet);
			}
		}
	}

	/**
	 * Remove the given sheet from this list.
	 * 
	 * @param sheet the sheet to be removed.
	 * @return <code>true</code> if this list contained <code>sheet</code>.
	 */
	public boolean remove(StyleSheet sheet) {
		return list.remove(sheet);
	}

	protected Iterator<AbstractCSSStyleSheet> iterator() {
		return list.iterator();
	}

	protected void clear() {
		list.clear();
	}

	protected boolean needsUpdate() {
		return needsUpdate;
	}

	protected void setNeedsUpdate(boolean needsUpdate) {
		this.needsUpdate = needsUpdate;
	}

	protected void update() {
		styleSheetSets.clear();
		clear();
	}

	abstract protected boolean hasErrorsOrWarnings();

	class MyDOMStringList implements DOMStringList {

		private final TreeSet<String> titleSet;

		MyDOMStringList() {
			super();
			titleSet = new TreeSet<String>();
		}

		/**
		 * Retrieve a <code>String</code> by ordinal index.
		 * 
		 * @param index the index in this list.
		 * @return the string at <code>index</code>, or <code>null</code> if
		 *         <code>index</code> is less than zero, or greater or equal to the list
		 *         length.
		 */
		@Override
		public String item(int index) {
			if (needsUpdate) {
				update();
			}
			if (index >= titleSet.size() || index < 0) {
				return null;
			}
			Iterator<String> it = titleSet.iterator();
			String title = null;
			int i = 0;
			while (it.hasNext()) {
				String s = it.next();
				if (i == index) {
					title = s;
					break;
				}
				i++;
			}
			return title;
		}

		@Override
		public int getLength() {
			if (needsUpdate) {
				update();
			}
			return titleSet.size();
		}

		@Override
		public boolean contains(String str) {
			return titleSet.contains(str);
		}

		private void add(String title) {
			titleSet.add(title);
		}

		private void clear() {
			titleSet.clear();
		}

	}

}
