/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.nsac.CSSException;

public class SheetRemoteTest {

	@Test
	public void testParseStyleSheetRemote() throws CSSException, IOException {
		if (TestConfig.REMOTE_TESTS) {
			CSSParser parser = new CSSParser();
			TestCSSHandler handler = new TestCSSHandler();
			parser.setDocumentHandler(handler);
			TestErrorHandler errorHandler = new TestErrorHandler();
			parser.setErrorHandler(errorHandler);
			parser.parseStyleSheet("https://css4j.github.io/usage-e.css");
			assertEquals(0, handler.comments.size());
			assertTrue(handler.selectors.size() != 0);
			assertTrue(handler.propertyNames.size() != 0);
			assertFalse(errorHandler.hasError());
		}

	}

}
