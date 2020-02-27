/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MockURLConnectionFactory {

	public static final String SAMPLE_URL = "http://www.example.com/xhtml/htmlsample.html";

	private static final Map<String, String> mockURLMap = new HashMap<String, String>();

	private final Map<String, Map<String, List<String>>> headerMap = new HashMap<String, Map<String, List<String>>>();

	private final HashMap<String, String> referrerMap = new HashMap<String, String>();

	/*
	 * After adding a file here and in classpath, remember to refresh the IDE if you
	 * are using one.
	 */
	static {
		mockURLMap.put(SAMPLE_URL, "htmlsample.html");
		mockURLMap.put("http://www.example.com/css/common.css", "common.css");
		mockURLMap.put("http://www.example.com/css/alter1.css", "alter1.css");
		mockURLMap.put("http://www.example.com/css/alter2.css", "alter2.css");
		mockURLMap.put("http://www.example.com/css/default.css", "default.css");
		mockURLMap.put("http://www.example.com/css/background.png", "background.png");
		mockURLMap.put("http://www.example.com/media/print.css", "print.css");
		mockURLMap.put("http://www.example.com/css/circular.css", "circular.css");
		mockURLMap.put("http://www.example.com/fonts/OpenSans-Regular.ttf", "contrib/OpenSans-Regular.ttf");
		mockURLMap.put("http://www.example.com/etc/fakepasswd", "fakepasswd");
	}

	public void setHeader(String ext, String headerName, String value) {
		Map<String, List<String>> extheaders = headerMap.get(ext);
		if (extheaders == null) {
			extheaders = new HashMap<String, List<String>>();
			headerMap.put(ext, extheaders);
		}
		List<String> hdrs = extheaders.get(headerName);
		if (hdrs == null) {
			hdrs = new ArrayList<String>(4);
			extheaders.put(headerName, hdrs);
		}
		int idx = hdrs.indexOf(headerName);
		if (idx < 0) {
			hdrs.add(value);
		} else {
			hdrs.set(idx, value);
		}
	}

	private static InputStream inputStreamFromClasspath(final String filename) {
		InputStream is = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				return getClass().getResourceAsStream(filename);
			}
		});
		return is;
	}

	private Map<String, List<String>> getHeadersForExtension(String ext) {
		return headerMap.get(ext);
	}

	public void assertReferrer(String url, String referrer) {
		referrerMap.put(url, referrer);
	}

	public void clearAssertions() {
		referrerMap.clear();
	}

	public URLConnection createConnection(URL url) {
		return new MockURLConnection(url);
	}

	class MockURLConnection extends HttpURLConnection {

		private final Map<String, List<String>> connheaders;

		private InputStream inputStream = null;

		MockURLConnection(URL url) {
			super(url);
			String ext = getExtension();
			connheaders = new HashMap<String, List<String>>();
			Map<String, List<String>> hdrmap = getHeadersForExtension(ext);
			if (hdrmap != null) {
				connheaders.putAll(hdrmap);
			}
			if ("html".equalsIgnoreCase(ext) || "xhtml".equalsIgnoreCase(ext) || "xml".equalsIgnoreCase(ext)) {
				String domain = url.getHost();
				if (domain.charAt(0) == 'w') {
					domain = domain.substring(domain.indexOf('.'));
				} else {
					domain = '.' + domain;
				}
				LinkedList<String> ckhdr = new LinkedList<String>();
				ckhdr.add("countryCode=EN; Domain=" + domain + "; Path=/");
				connheaders.put("Set-Cookie", ckhdr);
			}
		}

		@Override
		public void connect() throws IOException {
			if (!connected) {
				// Check for resource availability
				String url = getURL().toExternalForm();
				if (!mockURLMap.containsKey(url)) {
					throw new IOException("Unknown url: " + url);
				}
				// Check referrer assertion, if set
				String referrer = referrerMap.get(url);
				if (referrer != null && !referrer.equals(getRequestProperty("Referer"))) {
					throw new IOException(
							"Expected referrer: " + referrer + ", found " + getRequestProperty("Referer"));
				}
				connected = true;
			}
		}

		@Override
		public InputStream getInputStream() throws IOException {
			connect();
			if (inputStream == null) {
				String filename = mockURLMap.get(getURL().toExternalForm());
				inputStream = inputStreamFromClasspath(filename);
			}
			return inputStream;
		}

		private String getExtension() {
			String path = getURL().getPath();
			int pathlen = path.length();
			int dot = path.lastIndexOf('.', pathlen - 2);
			if (dot == -1) {
				return null;
			}
			return path.substring(dot + 1, pathlen);
		}

		@Override
		public String getHeaderField(String name) {
			List<String> hdrs = getHeaderFields().get(name);
			if (hdrs == null) {
				return null;
			}
			return hdrs.get(hdrs.size() - 1);
		}

		@Override
		public Map<String, List<String>> getHeaderFields() {
			return connheaders;
		}

		@Override
		public String getContentType() {
			String ext = getExtension();
			if ("css".equals(ext)) {
				return "text/css";
			} else if ("html".equals(ext)) {
				return "text/html";
			} else if ("xml".equals(ext)) {
				return "text/xml";
			} else if (ext != null) {
				List<String> ctype = getHeaderFields().get("content-type");
				if (ctype != null) {
					return ctype.get(0);
				}
			}
			return null;
		}

		@Override
		public void disconnect() {
		}

		@Override
		public boolean usingProxy() {
			return false;
		}

	}

}
