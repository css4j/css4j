/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import io.sf.carte.doc.style.css.nsac.Parser;

/**
 * Abstract base class for User Agents, with cookie handling.
 * <p>
 * 
 * @author Carlos Amengual
 */
abstract public class AbstractUserAgent implements UserAgent, UserAgent.AgentControl, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private static final String HEADER_COOKIE = "Cookie";

	private static final String HEADER_SET_COOKIE = "Set-Cookie";

	private static final String HEADER_AUTHORIZATION = "Authorization";

	private static final String HEADER_AGENT = "User-agent";

	private OriginPolicy originPolicy = null;

	private final EnumSet<Parser.Flag> parserFlags = EnumSet.noneOf(Parser.Flag.class);

	private final UserAgentErrorHandler errorHandler = new LogUserAgentErrorHandler();

	// Hostname -> Cookies
	private final Map<String, Set<Cookie>> cookieMap = new HashMap<>();

	private final SimpleDateFormat cookieDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.ROOT);

	private final CookieConfig globalConfig = new GlobalCookieConfig();

	private final Map<String, Set<AuthenticationCredentials>> credentialMap = new HashMap<>();

	private String userAgentId = System.getProperty("http.agent", "Mozilla/4.0 (compatible; CSS4J)");

	private int timeout = 15000; // Connection timeout

	protected AbstractUserAgent(EnumSet<Parser.Flag> parserFlags) {
		super();
		this.parserFlags.addAll(parserFlags);
	}

	protected OriginPolicy getOriginPolicy() {
		return originPolicy;
	}

	public void setOriginPolicy(OriginPolicy originPolicy) {
		this.originPolicy = originPolicy;
	}

	@Override
	public UserAgent.AgentControl getAgentControl() {
		return this;
	}

	/**
	 * Gets the global, useragent-wide default cookie config.
	 * 
	 * @return the default user-agent cookie config.
	 */
	@Override
	public CookieConfig getCookieConfig() {
		return globalConfig;
	}

	public Set<Cookie> getCookies(String host) {
		return cookieMap.get(host);
	}

	protected EnumSet<Parser.Flag> getParserFlags() {
		return parserFlags;
	}

	@Override
	public String getProperty(String propertyName) {
		if (propertyName.equals("parser.starhack")) {
			return Boolean.toString(parserFlags.contains(Parser.Flag.STARHACK));
		} else if (propertyName.equals("parser.ievalues")) {
			return Boolean.toString(parserFlags.contains(Parser.Flag.IEVALUES));
		} else if (propertyName.equals("parser.ieprio")) {
			return Boolean.toString(parserFlags.contains(Parser.Flag.IEPRIO));
		} else if (propertyName.equals("parser.iepriochar")) {
			return Boolean.toString(parserFlags.contains(Parser.Flag.IEPRIOCHAR));
		}
		return null;
	}

	/**
	 * Sets a control property.
	 * 
	 * @param propertyName
	 *            the name of the property to set.
	 * @param value
	 *            the string representation of the property value.
	 */
	@Override
	public void setProperty(String propertyName, String value) {
		if (propertyName.equals("parser.starhack")) {
			if ("true".equalsIgnoreCase(value)) {
				parserFlags.add(Parser.Flag.STARHACK);
			} else {
				parserFlags.remove(Parser.Flag.STARHACK);
			}
		} else if (propertyName.equals("parser.ievalues")) {
			if ("true".equalsIgnoreCase(value)) {
				parserFlags.add(Parser.Flag.IEVALUES);
			} else {
				parserFlags.remove(Parser.Flag.IEVALUES);
			}
		}
	}

	/**
	 * Gets the authentication credentials for the given URL.
	 * 
	 * @param url
	 *            the url for which the credential is required.
	 * @param realm
	 *            the realm name, or null if there is no realm, in which case
	 *            any valid credential will be returned.
	 * @return the authentication credentials, or null if there is none.
	 */
	@Override
	public AuthenticationCredentials getAuthenticationCredentials(URL url, String realm) {
		Set<AuthenticationCredentials> credset = credentialMap.get(url.getHost());
		if (credset != null) {
			for (AuthenticationCredentials cred : credset) {
				String orealm = cred.getRealm();
				if (realm == null || realm.equals(orealm)) {
					return cred;
				}
			}
		}
		return null;
	}

	/**
	 * Gives the credentials for the given hostname and realm, creating a new
	 * one if there is none.
	 * 
	 * @param host
	 *            the host.
	 * @param realm
	 *            the realm. Cannot be null.
	 * @return the credentials.
	 */
	@Override
	public AuthenticationCredentials authenticationCredentials(String host, String realm) {
		if (realm == null) {
			throw new NullPointerException("Attempt to create a null realm.");
		}
		Set<AuthenticationCredentials> credset = credentialMap.get(host);
		if (credset == null) {
			credset = new HashSet<>();
		} else {
			for (AuthenticationCredentials cred : credset) {
				if (realm.equals(cred.getRealm())) {
					return cred;
				}
			}
		}
		AuthenticationCredentials creds = new MyAuthenticationCredentials(realm);
		credset.add(creds);
		return creds;
	}

	/**
	 * Sets the connection timeout.
	 * 
	 * @param timeout
	 *            the connection timeout, in milliseconds. A
	 *            <code>timeout</code> of 0 means no timeout (the default).
	 */
	@Override
	public void setConnectionTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public boolean isVisitedURL(URL url) {
		return false;
	}

	public void setUseragentId(String userAgentId) {
		this.userAgentId = userAgentId;
	}

	/**
	 * Open a URL connection according to the given document creation date.
	 * 
	 * @param url
	 *            the URL to connect to.
	 * @param creationDate
	 *            the creation date.
	 * @return the connection.
	 * @throws IOException
	 *             if an I/O exception occurs opening the connection.
	 */
	protected URLConnection openConnection(URL url, long creationDate) throws IOException {
		URLConnection con = createConnection(url);
		con.setConnectTimeout(timeout);
		if (con instanceof HttpURLConnection) {
			HttpURLConnection hcon = (HttpURLConnection) con;
			setCookies(hcon, url, creationDate);
			setCredentials(hcon, url, null, creationDate);
			if (userAgentId != null) {
				hcon.setRequestProperty(HEADER_AGENT, userAgentId);
			}
		}
		con.setAllowUserInteraction(false);
		return con;
	}

	/**
	 * Opens a connection to the given URL.
	 * 
	 * @param url
	 *            the URL to connect to.
	 * @return a URLConnection linking to the URL.
	 * @throws IOException
	 *             if an I/O exception occurs opening the connection.
	 */
	protected URLConnection createConnection(URL url) throws IOException {
		return url.openConnection();
	}

	private void setCredentials(HttpURLConnection hcon, URL url, String realm, long creationDate) {
		AuthenticationCredentials creds = getAuthenticationCredentials(url, realm);
		if (creds != null && creds.getAuthType() == AuthenticationCredentials.HTTP_BASIC_AUTH) {
			hcon.setRequestProperty(HEADER_AUTHORIZATION, basicToken(creds));
		}
	}

	private static String basicToken(AuthenticationCredentials creds) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("Basic ").append(creds.getLoginPrincipal().getName()).append(':');
		if (creds.getPassword() != null) {
			sb.append(creds.getPassword());
		}
		String s = Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
		int i = s.indexOf('=');
		if (i != -1) {
			return s.substring(0, i);
		} else {
			return s;
		}
	}

	private void setCookies(HttpURLConnection hcon, URL url, long creationDate) {
		Set<Cookie> matchingCookies = new HashSet<>(8);
		String host = url.getHost().toLowerCase(Locale.ROOT);
		Set<Cookie> cookies = cookieMap.get(host);
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (match(cookie, url, host, creationDate)) {
					matchingCookies.add(cookie);
				}
			}
			setCookies(hcon, matchingCookies);
		}
		if (originPolicy != null) {
			String domain = originPolicy.domainFromHostname(host);
			if (domain != host) {
				cookies = cookieMap.get(host);
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (match(cookie, url, host, creationDate)) {
							matchingCookies.add(cookie);
						}
					}
					setCookies(hcon, matchingCookies);
				}
			}
		}
	}

	protected void readCookies(HttpURLConnection hcon, long creationDate) {
		if ((globalConfig.acceptsAllCookies() || globalConfig.acceptsSessionCookies()) &&
				originPolicy != null) {
			String s = hcon.getHeaderField(HEADER_SET_COOKIE);
			if (s == null) {
				return;
			}
			URL url = hcon.getURL();
			String host = url.getHost();
			int port = url.getPort();
			if (port == -1) {
				port = url.getDefaultPort();
			}
			StringTokenizer st = new StringTokenizer(s, ",");
			while (st.hasMoreTokens()) {
				Cookie ck = parseCookie(st.nextToken().trim(), host, port, creationDate);
				if (ck != null) {
					if (originPolicy.isTopLevelSuffix(ck.getDomain())) {
						// Attempt to set a "supercookie"
						errorHandler.onSuperCookie(url, ck.getDomain());
						continue;
					}
					Set<Cookie> set = cookieMap.get(ck.getDomain());
					if (set == null) {
						set = new HashSet<>(8);
						cookieMap.put(ck.getDomain(), set);
					}
					set.add(ck);
				}
			}
		}
	}

	Cookie parseCookie(String ckhdr, String host, int port, long creationDate) {
		StringTokenizer st = new StringTokenizer(ckhdr, ";");
		String clave, valor;
		if (!st.hasMoreTokens()) {
			return null;
		}
		valor = st.nextToken().trim();
		int i = valor.indexOf('=');
		if (i == -1) {
			return null;
		}
		clave = valor.substring(0, i).trim();
		valor = valor.substring(i + 1).trim();
		Cookie ck = new DefaultCookie(port, clave, valor);
		while (st.hasMoreTokens()) {
			valor = st.nextToken().trim();
			i = valor.indexOf('=');
			if (i == -1) {
				if ("Secure".equalsIgnoreCase(valor)) {
					ck.setSecure();
				} else if ("HttpOnly".equalsIgnoreCase(valor)) {
					ck.setHttpOnly();
				}
			} else {
				clave = valor.substring(0, i).trim();
				valor = valor.substring(i + 1).trim();
				if ("path".equalsIgnoreCase(clave)) {
					ck.setPath(valor);
				} else if ("max-age".equalsIgnoreCase(clave)) {
					ck.setExpiryTime(Long.parseLong(valor) + creationDate);
				} else if ("Expires".equalsIgnoreCase(clave)) {
					try {
						ck.setExpiryTime(cookieDateFormat.parse(valor).getTime());
					} catch (ParseException e) {
					}
				} else if ("domain".equalsIgnoreCase(clave)) {
					ck.setDomain(valor.toLowerCase(Locale.ROOT));
				}
			}
		}
		if (!globalConfig.acceptsAllCookies() && ck.isPersistent()) {
			return null;
		}
		if (ck.getDomain() == null) {
			ck.setDomain(host);
		} else {
			if (!host.endsWith(ck.getDomain())) {
				// cookie injection
				return null;
			}
		}
		return ck;
	}

	static void setCookies(HttpURLConnection hcon, Set<Cookie> matchingCookies) {
		Iterator<Cookie> it = matchingCookies.iterator();
		StringBuilder sb = new StringBuilder(256);
		if (it.hasNext()) {
			formatCookie(sb, it.next());
		}
		while (it.hasNext()) {
			sb.append(',');
			formatCookie(sb, it.next());
		}
		hcon.setRequestProperty(HEADER_COOKIE, sb.toString());
	}

	private static void formatCookie(StringBuilder sb, Cookie ck) {
		sb.append(ck.getName()).append('=').append(ck.getValue());
		String s = ck.getPath();
		if (s != null) {
			sb.append(';').append("Path=").append(s);
		}
	}

	static boolean match(Cookie cookie, URL url, String host, long creationDate) {
		return host.endsWith(cookie.getDomain()) && matchPath(url.getPath(), cookie.getPath())
				&& (!cookie.isSecure() || "https".equals(url.getProtocol())) && matchPort(url, cookie.getPorts())
				&& (!cookie.isPersistent() || creationDate < cookie.getExpiryTime());
	}

	static boolean matchPath(String uPath, String cPath) {
		if (uPath == cPath) {
			return true;
		}
		return uPath != null && uPath.startsWith(cPath);
	}

	private static boolean matchPort(URL url, int[] ports) {
		int port = url.getPort();
		if (port == -1) {
			port = url.getDefaultPort();
		}
		// Fast track
		if (ports[0] == port) {
			return true;
		}
		int pl = ports.length;
		for (int i = 1; i < pl; i++) {
			if (ports[i] == port) {
				return true;
			}
		}
		return false;
	}

	static class GlobalCookieConfig implements CookieConfig {

		private boolean acceptAllCookies = false;
		private boolean acceptSessionCookies = false;

		@Override
		public boolean acceptsAllCookies() {
			return acceptAllCookies;
		}

		/**
		 * Accept all cookies when opening HTTP connections to retrieve
		 * resources.
		 * 
		 * @param acceptAllCookies
		 *            true to accept all cookies, or <code>false</code> to ignore cookies.
		 */
		@Override
		public void setAcceptAllCookies(boolean acceptAllCookies) {
			if (acceptAllCookies) {
				acceptSessionCookies = true;
			}
			this.acceptAllCookies = acceptAllCookies;
		}

		@Override
		public boolean acceptsSessionCookies() {
			return acceptSessionCookies;
		}

		/**
		 * Accept only session cookies when opening HTTP connections to retrieve
		 * resources.
		 * 
		 * @param acceptSessionCookies
		 *            true to accept session cookies, or <code>false</code> to ignore all
		 *            cookies.
		 */
		@Override
		public void setAcceptSessionCookies(boolean acceptSessionCookies) {
			if (!acceptSessionCookies) {
				acceptAllCookies = false;
			}
			this.acceptSessionCookies = acceptSessionCookies;
		}
	}

	static class MyAuthenticationCredentials implements AuthenticationCredentials {

		private Principal loginPrincipal = null;
		private String password = null;
		private final String realm;
		private byte authtype = HTTP_BASIC_AUTH;

		MyAuthenticationCredentials(String realm) {
			super();
			this.realm = realm;
		}

		@Override
		public Principal getLoginPrincipal() {
			return loginPrincipal;
		}

		@Override
		public void setLoginPrincipal(Principal loginPrincipal) {
			this.loginPrincipal = loginPrincipal;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public String getRealm() {
			return realm;
		}

		@Override
		public void setAuthType(byte authtype) {
			this.authtype = authtype;
		}

		@Override
		public byte getAuthType() {
			return authtype;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + authtype;
			result = prime * result + ((loginPrincipal == null) ? 0 : loginPrincipal.hashCode());
			result = prime * result + ((password == null) ? 0 : password.hashCode());
			result = prime * result + ((realm == null) ? 0 : realm.hashCode());
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
			if (getClass() != obj.getClass()) {
				return false;
			}
			MyAuthenticationCredentials other = (MyAuthenticationCredentials) obj;
			if (authtype != other.authtype) {
				return false;
			}
			if (loginPrincipal == null) {
				if (other.loginPrincipal != null) {
					return false;
				}
			} else if (!loginPrincipal.equals(other.loginPrincipal)) {
				return false;
			}
			if (password == null) {
				if (other.password != null) {
					return false;
				}
			} else if (!password.equals(other.password)) {
				return false;
			}
			if (realm == null) {
				return other.realm == null;
			} else {
				return realm.equals(other.realm);
			}
		}

		@Override
		public String toString() {
			return "[loginPrincipal=" + loginPrincipal + ", password=" + password + ", realm=" + realm + "]";
		}

	}

}
