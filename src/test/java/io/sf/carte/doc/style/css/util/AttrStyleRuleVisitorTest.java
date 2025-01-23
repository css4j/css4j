/*

 Copyright (c) 2020-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.dom.DOMDocument;
import io.sf.carte.doc.dom.DOMElement;
import io.sf.carte.doc.dom.TestDOMImplementation;
import io.sf.carte.doc.dom.XMLDocumentBuilder;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.nsac.AttributeCondition;
import io.sf.carte.doc.style.css.nsac.Condition.ConditionType;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.parser.AttributeConditionVisitor;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;
import io.sf.carte.util.Visitor;

public class AttrStyleRuleVisitorTest {

	@Test
	public void testVisit() throws SAXException {
		DOMDocument doc = parseDocument(
				"<html><head><style>#foo,.myclass{display:block}#bar,.other{margin:1px}</style></head><div></div></html>",
				"example.html");

		PrependConditionVisitor attrVisitor = new PrependConditionVisitor();
		Visitor<CSSStyleRule> ruleVisitor = new AttrStyleRuleVisitor(attrVisitor);
		for (DOMElement style : doc.getElementsByTagNameNS("*", "style")) {
			AbstractCSSStyleSheet sheet = (AbstractCSSStyleSheet) ((LinkStyle<?>) style).getSheet();
			if (sheet != null) {
				sheet.acceptStyleRuleVisitor(ruleVisitor);
				style.normalize(); // Write the result to the inner text node
			}
		}

		assertEquals(
				"<html><head><style>#pre-foo,.pre-myclass {display: block; }#pre-bar,.pre-other {margin: 1px; }</style></head><div></div></html>",
				doc.toString().replaceAll("\n", ""));
	}

	class PrependConditionVisitor extends AttributeConditionVisitor {

		@Override
		public void visit(AttributeCondition condition) {
			ConditionType type = condition.getConditionType();
			if (type == ConditionType.ID || type == ConditionType.CLASS) {
				String currentName = condition.getValue();
				String newName = "pre-" + currentName;
				setConditionValue(condition, newName);
			}
		}

	}

	private DOMDocument parseDocument(String text, String filename) throws SAXException {
		TestDOMImplementation domImpl = new TestDOMImplementation(false);
		XMLDocumentBuilder builder = new XMLDocumentBuilder(domImpl);
		builder.setIgnoreElementContentWhitespace(true);
		builder.setHTMLProcessing(true);
		builder.setEntityResolver(new DefaultEntityResolver());
		InputSource is = new InputSource(new StringReader(text));
		DOMDocument document;
		try {
			document = (DOMDocument) builder.parse(is);
			URL base = new URL("http://www.example.com/");
			document.setDocumentURI(new URL(base, filename).toExternalForm());
		} catch (IOException e) {
			document = null;
		}
		return document;
	}

}
