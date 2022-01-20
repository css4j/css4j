/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.TestCSSStyleSheetFactory;

public class AbstractUserAgentTest {

	private static long creationDate = 1317896564653L;

	@Test
	public void testSetAcceptCookies() {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		CookieConfig agent = factory.getUserAgent().getAgentControl().getCookieConfig();
		agent.setAcceptAllCookies(true);
		assertTrue(agent.acceptsAllCookies());
		assertTrue(agent.acceptsSessionCookies());
		agent.setAcceptAllCookies(false);
		agent.setAcceptSessionCookies(true);
		assertFalse(agent.acceptsAllCookies());
		assertTrue(agent.acceptsSessionCookies());
		agent.setAcceptAllCookies(true);
		agent.setAcceptSessionCookies(false);
		assertFalse(agent.acceptsSessionCookies());
		assertFalse(agent.acceptsAllCookies());
	}

	@Test
	public void testParseCookie() {
		DOMCSSStyleSheetFactory factory = new DOMCSSStyleSheetFactory();
		AbstractUserAgent agent = factory.getUserAgent();
		agent.getAgentControl().getCookieConfig().setAcceptAllCookies(true);
		Cookie cookie = agent.parseCookie(
				"CKNAME=CKvalue; Domain=www.example.com; Path=/mypath; Expires=Fri, 05-Oct-2012 20:04:31 GMT; Secure; HttpOnly",
				"www.example.com", 80, creationDate);
		assertEquals("www.example.com", cookie.getDomain());
		assertEquals("CKNAME", cookie.getName());
		assertEquals("CKvalue", cookie.getValue());
		assertEquals("/mypath", cookie.getPath());
		assertEquals(1349467471000L, cookie.getExpiryTime());
		assertEquals(80, cookie.getPorts()[0]);
		assertTrue(cookie.isSecure());
		assertTrue(cookie.isHttpOnly());
		assertTrue(cookie.isPersistent());
		cookie = agent.parseCookie(
				"CKNAME=CKvalue; Domain=WWW.EXAMPLE.COM; Path=/mypath; Expires=Fri, 05-Oct-2012 20:04:31 GMT; Secure; HttpOnly",
				"www.example.com", 80, creationDate);
		assertEquals("www.example.com", cookie.getDomain());
	}

	@Test
	public void testReadURL() throws IOException, DocumentException {
		TestCSSStyleSheetFactory factory = new TestCSSStyleSheetFactory();
		AbstractUserAgent agent = factory.getUserAgent();
		agent.setOriginPolicy(MockOriginPolicy.getInstance());
		agent.getAgentControl().getCookieConfig().setAcceptAllCookies(true);
		agent.readURL(new URL(MockURLConnectionFactory.SAMPLE_URL));
		Set<Cookie> cookies = agent.getCookies(".example.com");
		assertNotNull(cookies);
		Cookie cookie = cookies.toArray(new Cookie[0])[0];
		assertEquals(".example.com", cookie.getDomain());
		assertEquals("countryCode", cookie.getName());
		assertEquals("EN", cookie.getValue());
		assertEquals("/", cookie.getPath());
		assertEquals(80, cookie.getPorts()[0]);
		assertFalse(cookie.isSecure());
		assertFalse(cookie.isHttpOnly());
		assertFalse(cookie.isPersistent());
	}

	@Test
	public void testMatch() throws MalformedURLException {
		URL url = new URL("http://www.example.com/mypath/myfile");
		Cookie cookie = new DefaultCookie();
		cookie.setDomain("www.example.com");
		cookie.addPort(80);
		cookie.setPath("/mypath");
		assertTrue(AbstractUserAgent.match(cookie, url, url.getHost().toLowerCase(Locale.ROOT), creationDate));
		cookie = new DefaultCookie();
		cookie.setDomain("www.example.com");
		cookie.addPort(80);
		cookie.setPath("/mypath");
		cookie.setExpiryTime(creationDate - 1L);
		assertFalse(AbstractUserAgent.match(cookie, url, url.getHost().toLowerCase(Locale.ROOT), creationDate));
		cookie = new DefaultCookie();
		cookie.setDomain("www.foo.com");
		cookie.addPort(80);
		cookie.setPath("/mypath");
		assertFalse(AbstractUserAgent.match(cookie, url, url.getHost().toLowerCase(Locale.ROOT), creationDate));
	}

	@Test
	public void testMatchPath() {
		assertTrue(AbstractUserAgent.matchPath("/mypath/myfile", "/mypath"));
		assertFalse(AbstractUserAgent.matchPath("/mypath/myfile", "/otherpath"));
	}

}
