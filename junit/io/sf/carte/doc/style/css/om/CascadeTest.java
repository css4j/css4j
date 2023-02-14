/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.dom.CSSDOMImplementation;
import io.sf.carte.doc.dom.DOMBridge;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet.Cascade;

public class CascadeTest {

	private Cascade cascade;

	@BeforeEach
	public void setUp() {
		CSSDOMImplementation impl = new CSSDOMImplementation();
		BaseDocumentCSSStyleSheet sheet = DOMBridge.createDocumentStyleSheet(impl, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		CSSDocument ownerNode = impl.createDocument(null, "html", null);
		sheet.setOwnerDocument(ownerNode);
		cascade = sheet.new Cascade();
		StyleRule rule = new StyleRule();
		rule.setCssText("p.foo {font-size: 3em}");
		SelectorMatcher matcher = new DOMSelectorMatcher(sheet.getOwnerNode().getDocumentElement());
		cascade.add(rule.getSpecificity(0, matcher));
		rule = new StyleRule();
		rule.setCssText("#myid {font-size: 4em}");
		cascade.add(rule.getSpecificity(0, matcher));
		rule = new StyleRule();
		rule.setCssText("p {font-size: 1.2em}");
		cascade.add(rule.getSpecificity(0, matcher));
		rule = new StyleRule();
		rule.setCssText("div > p {font-size: 2.5em}");
		cascade.add(rule.getSpecificity(0, matcher));
		rule = new StyleRule();
		rule.setCssText("p.bar {font-size: 2em}");
		cascade.add(rule.getSpecificity(0, matcher));
	}

	@Test
	public void testIterator() {
		Iterator<StyleRule> it = cascade.iterator();
		assertTrue(it.hasNext());
		assertEquals("p", it.next().getSelectorText());
		assertTrue(it.hasNext());
		assertEquals("div>p", it.next().getSelectorText());
		assertTrue(it.hasNext());
		assertEquals("p.foo", it.next().getSelectorText());
		assertTrue(it.hasNext());
		assertEquals("p.bar", it.next().getSelectorText());
		assertTrue(it.hasNext());
		assertEquals("#myid", it.next().getSelectorText());
		assertFalse(it.hasNext());
	}

}
