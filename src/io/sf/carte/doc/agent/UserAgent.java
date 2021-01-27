/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.agent;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.EntityResolver;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.style.css.CSSDocument;

/**
 * Very basic user agent abstraction.
 *
 * @author Carlos Amengual
 *
 */
public interface UserAgent {

	/**
	 * Reads and parses a markup document located at the given URL.
	 *
	 * @param url
	 *            the URL that points to the document.
	 * @return the CSSDocument.
	 * @throws IOException
	 *             if there is an I/O problem reading the URL.
	 * @throws DocumentException
	 *             if there is a problem parsing the document.
	 */
	CSSDocument readURL(URL url) throws IOException, DocumentException;

	/**
	 * Has the provided url been visited by this user agent.
	 *
	 * @param url
	 *            the URL to test.
	 * @return <code>true</code> if the URL was visited by this agent, <code>false</code> if not visited
	 *         or this agent does not support history.
	 */
	boolean isVisitedURL(URL url);

	/**
	 * Sets the entity resolver to be used when parsing documents.
	 *
	 * @param resolver
	 *            the entity resolver.
	 */
	void setEntityResolver(EntityResolver resolver);

	/**
	 * Get the control object for this user agent.
	 *
	 * @return the control object for this user agent, or null if this agent has
	 *         no control.
	 */
	AgentControl getAgentControl();

	/**
	 * User agent configuration and control.
	 *
	 */
	public interface AgentControl {
		/**
		 * Gets a control property.
		 *
		 * @param propertyName
		 *            the property name.
		 * @return the string representation of the property value, or null if
		 *         the property is not set.
		 */
		String getProperty(String propertyName);

		/**
		 * Sets a control property.
		 * <p>
		 * If the property is not known, the user agent may invoke the method
		 * {@link UserAgentErrorHandler#onUnknownProperty(String, String)}. If
		 * the property is known but the value is wrong, the user agent should
		 * call the method
		 * {@link UserAgentErrorHandler#onWrongPropertyValue(String, String)}.
		 *
		 * @param propertyName
		 *            the name of the property to set.
		 * @param value
		 *            the string representation of the property value.
		 */
		void setProperty(String propertyName, String value);

		/**
		 * Gets the global, useragent-wide default cookie config.
		 *
		 * @return the default user-agent cookie config.
		 */
		CookieConfig getCookieConfig();

		/**
		 * Gets the authentication credentials for the given URL.
		 *
		 * @param url
		 *            the url for which the credential is required.
		 * @param realm
		 *            the realm name, or null if there is no realm, in which
		 *            case any valid credential will be returned.
		 * @return the authentication credentials, or null if there is none.
		 */
		AuthenticationCredentials getAuthenticationCredentials(URL url, String realm);

		/**
		 * Gives the credentials for the given hostname and realm, creating a
		 * new one if there is none.
		 *
		 * @param host
		 *            the host.
		 * @param realm
		 *            the realm. Cannot be null.
		 * @return the credentials.
		 */
		AuthenticationCredentials authenticationCredentials(String host, String realm);

		/**
		 * Sets the connection timeout.
		 *
		 * @param timeout
		 *            the connection timeout, in milliseconds. A
		 *            <code>timeout</code> of 0 means no timeout (the default).
		 */
		void setConnectionTimeout(int timeout);
	}
}
