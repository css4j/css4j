/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Tag;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import io.sf.carte.doc.style.css.nsac.CSSParseException;

/**
 * Use <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a> to
 * perform a fuzz test.
 * <p>
 * To perform actual fuzzing, the environment variable {@code JAZZER_FUZZ}
 * should be set to a non-empty value, and the inputs directory should be
 * created.
 * </p>
 * <p>
 * See also <a href=
 * "https://github.com/CodeIntelligenceTesting/cifuzz"><code>cifuzz</code></a>.
 * </p>
 * <p>
 * Important note: in the JUnit Runner configuration(s) of your IDE (or build
 * system) you should exclude the {@code Fuzz} tag if you do not want to run
 * this test there.
 * </p>
 */
@Tag("Fuzz")
class CSSParserFuzzTest {

	@FuzzTest
	void fuzzTest(FuzzedDataProvider data)
		throws CSSParseException, IllegalStateException, IOException {
		CSSParser parser = new CSSParser();
		TestCSSHandler handler = new TestCSSHandler();
		parser.setDocumentHandler(handler);
		TestErrorHandler errHandler = new TestErrorHandler();
		parser.setErrorHandler(errHandler);

		String input = data.consumeRemainingAsString();
		parser.parseStyleSheet(new StringReader(input));

		handler.checkRuleEndings();
	}

}
