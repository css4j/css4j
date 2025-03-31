/*

 Copyright (c) 2005-2025, Carlos Amengual.

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
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.sf.carte.doc.style.css.nsac.Parser;

/**
 * Abstract base class for User Agents, with cookie handling.
 * <p>
 * 
 * @author Carlos Amengual
 */
abstract public class AbstractUserAgent implements UserAgent, UserAgent.AgentControl, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private static final String HEADER_AUTHORIZATION = "Authorization";

	private static final String HEADER_AGENT = "User-agent";

	private static final String CSS4J_MAJOR_VERSION = "6";

	private static final String DEFAULT_USER_AGENT_STRING = userAgentString();

	private OriginPolicy originPolicy = null;

	private final EnumSet<Parser.Flag> parserFlags = EnumSet.noneOf(Parser.Flag.class);

	private UserAgentErrorHandler errorHandler = null;

	private final Map<String, Set<AuthenticationCredentials>> credentialMap = new HashMap<>();

	private String userAgentId = DEFAULT_USER_AGENT_STRING;

	private int timeout = 15000; // Connection timeout

	protected AbstractUserAgent(EnumSet<Parser.Flag> parserFlags) {
		super();
		this.parserFlags.addAll(parserFlags);
	}

	private static String defaultUserAgent() {
		return "Mozilla/5.0 CSS4J/" + CSS4J_MAJOR_VERSION;
	}

	private static String userAgentString() {
		String agentId;

		try {
			agentId = System.getProperty("http.agent");
		} catch (SecurityException e) {
			return defaultUserAgent();
		}

		if (agentId == null) {
			String osname;
			try {
				osname = System.getProperty("os.name");
			} catch (SecurityException e) {
				osname = null;
			}
			if (osname == null) {
				return defaultUserAgent();
			}
			StringBuilder buf = new StringBuilder(osname.length() + 30);
			buf.append("Mozilla/5.0 (").append(osname);
			try {
				String osarch = System.getProperty("os.arch");
				if (osarch != null) {
					buf.append("; ").append(osarch);
				}
			} catch (SecurityException e) {
			}
			buf.append(") CSS4J/").append(CSS4J_MAJOR_VERSION);
			agentId = buf.toString();
		}

		return agentId;
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
	 * @return null
	 */
	@Override
	@Deprecated(forRemoval = true)
	public CookieConfig getCookieConfig() {
		return null;
	}

	@Deprecated(forRemoval = true)
	public Set<Cookie> getCookies(String host) {
		return null;
	}

	/**
	 * Get the error handler.
	 * 
	 * @return the error handler
	 */
	public UserAgentErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Set the error handler.
	 * 
	 * @param handler the error handler to set
	 */
	public void setErrorHandler(UserAgentErrorHandler handler) {
		this.errorHandler = handler;
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

	@Override
	public boolean setProperty(String propertyName, String value) {
		if (propertyName.equals("parser.starhack")) {
			if ("true".equalsIgnoreCase(value)) {
				parserFlags.add(Parser.Flag.STARHACK);
			} else {
				parserFlags.remove(Parser.Flag.STARHACK);
			}
			return true;
		} else if (propertyName.equals("parser.ievalues")) {
			if ("true".equalsIgnoreCase(value)) {
				parserFlags.add(Parser.Flag.IEVALUES);
			} else {
				parserFlags.remove(Parser.Flag.IEVALUES);
			}
			return true;
		}

		return false;
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
		con.setAllowUserInteraction(false);
		if (con instanceof HttpURLConnection) {
			HttpURLConnection hcon = (HttpURLConnection) con;
			setCredentials(hcon, url, null, creationDate);
			if (userAgentId != null) {
				hcon.setRequestProperty(HEADER_AGENT, userAgentId);
			}
		}
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

	@Deprecated(forRemoval = true)
	protected void readCookies(HttpURLConnection hcon, long creationDate) {
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
