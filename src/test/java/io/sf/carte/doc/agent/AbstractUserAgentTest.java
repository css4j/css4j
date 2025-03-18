/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;
import static org.junit.Assert.assertSame;

import java.net.URI;

import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class AbstractUserAgentTest {

	@Test
	public void testReadURL() throws Exception {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractUserAgent agent = factory.getUserAgent();
		TestUserAgentErrorHandler errorHandler = new TestUserAgentErrorHandler();
		agent.setErrorHandler(errorHandler);
		assertSame(errorHandler, agent.getErrorHandler());

		agent.setOriginPolicy(MockOriginPolicy.getInstance());

		agent.readURL(new URI(MockURLConnectionFactory.SAMPLE_URL).toURL());
	}

}
