/*

 Copyright (c) 1998-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import io.sf.carte.doc.agent.AgentUtil;

/**
 * Implements EntityResolver2.
 * <p>
 * Has common W3C DTDs/entities built-in and loads others via the supplied
 * <code>SYSTEM</code> URL, provided that certain conditions are met:
 * </p>
 * <ul>
 * <li>URL protocol is <code>http</code>/<code>https</code>.</li>
 * <li>Either the mime type is valid for a DTD or entity, or the filename ends
 * with <code>.dtd</code>, <code>.ent</code> or <code>.mod</code>.</li>
 * <li>The whitelist is either disabled (no host added to it) or contains the
 * host from the URL.</li>
 * </ul>
 * <p>
 * If the whitelist was enabled (e.g. default constructor), any attempt to
 * download data from a remote URL not present in the whitelist is going to
 * produce an exception. You can use that to determine whether your documents
 * are referencing a DTD resource that is not bundled with this resolver.
 * </p>
 * <p>
 * If the constructor with a <code>false</code> argument was used, the whitelist
 * can still be enabled by adding a hostname via
 * {@link #addHostToWhiteList(String)}.
 * </p>
 * <p>
 * Although this resolver should protect you from most information leaks (see
 * <a href="https://owasp.org/www-community/attacks/Server_Side_Request_Forgery">SSRF
 * attacks</a>) and also from <code>jar:</code>
 * <a href="https://en.wikipedia.org/wiki/Zip_bomb">decompression bombs</a>, DoS
 * attacks based on entity expansion/recursion like the
 * <a href="https://en.wikipedia.org/wiki/Billion_laughs_attack">'billion laughs
 * attack'</a> may still be possible and should be prevented at the XML parser.
 * Be sure to use a properly configured, recent version of your parser.
 * </p>
 * 
 * @author Carlos Amengual
 * 
 */
public class DefaultEntityResolver implements EntityResolver2 {

	private final HashMap<String, String> systemIdToFilename = new HashMap<String, String>(64);

	private final HashMap<String, String> systemIdToPublicId = new HashMap<String, String>(13);

	private ClassLoader loader = null;

	private HashSet<String> whitelist = null;

	private static final String XHTML1_TRA_PUBLICID = "-//W3C//DTD XHTML 1.0 Transitional//EN";
	private static final String XHTML1_TRA_SYSTEMID = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd";
	private static final String SVG11_PUBLICID = "-//W3C//DTD SVG 1.1//EN";
	private static final String SVG11_SYSTEMID = "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd";

	/**
	 * Construct a resolver with the whitelist enabled.
	 */
	public DefaultEntityResolver() {
		this(true);
	}

	/**
	 * Construct a resolver with the whitelist enabled or disabled according to
	 * <code>enableWhitelist</code>.
	 * 
	 * @param enableWhitelist can be <code>false</code> to allow connecting to any
	 *                        host to retrieve DTDs or entities, or
	 *                        <code>true</code> to enable the (empty) whitelist so
	 *                        no network connections are to be allowed until a host
	 *                        is added to it.
	 */
	public DefaultEntityResolver(boolean enableWhitelist) {
		super();
		systemIdToFilename.put("https://www.w3.org/TR/html5/entities.dtd", "w3c/xhtml5.ent");
		systemIdToFilename.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "w3c/xhtml1-strict.dtd");
		systemIdToFilename.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
				"w3c/xhtml1-transitional.dtd");
		systemIdToFilename.put("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd", "w3c/xhtml11.dtd");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml11.dtd", "w3c/xhtml11.dtd");
		systemIdToFilename.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-lat1.ent", "w3c/xhtml-lat1.ent");
		systemIdToFilename.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-symbol.ent", "w3c/xhtml-symbol.ent");
		systemIdToFilename.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-special.ent", "w3c/xhtml-special.ent");
		// XHTML 1.1 modules
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inlstyle-1.mod", "w3c/xhtml-inlstyle-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml11-model-1.mod", "w3c/xhtml-11-model-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-datatypes-1.mod", "w3c/xhtml-datatypes-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-framework-1.mod", "w3c/xhtml-framework-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-text-1.mod", "w3c/xhtml-text-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-hypertext-1.mod", "w3c/xhtml-hypertext-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-list-1.mod", "w3c/xhtml-list-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-edit-1.mod", "w3c/xhtml-edit-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-bdo-1.mod", "w3c/xhtml-bdo-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-ruby-1.mod", "w3c/xhtml-ruby-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-pres-1.mod", "w3c/xhtml-pres-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-link-1.mod", "w3c/xhtml-link-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-meta-1.mod", "w3c/xhtml-meta-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-base-1.mod", "w3c/xhtml-base-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-script-1.mod", "w3c/xhtml-script-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-style-1.mod", "w3c/xhtml-style-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-image-1.mod", "w3c/xhtml-image-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-csismap-1.mod", "w3c/xhtml-csismap-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-ssismap-1.mod", "w3c/xhtml-ssismap-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-param-1.mod", "w3c/xhtml-param-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-object-1.mod", "w3c/xhtml-object-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-table-1.mod", "w3c/xhtml-table-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-form-1.mod", "w3c/xhtml-form-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-legacy-1.mod", "w3c/xhtml-legacy-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-struct-1.mod", "w3c/xhtml-struct-1.mod");
		// Other common DTDs
		systemIdToFilename.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd", "w3c/xhtml1-frameset.dtd");
		systemIdToFilename.put("http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd", "w3c/xhtml-basic11.dtd");
		systemIdToFilename.put("http://www.w3.org/TR/html4/strict.dtd", "w3c/html4-strict.dtd");
		systemIdToFilename.put("http://www.w3.org/TR/html4/loose.dtd", "w3c/html4-loose.dtd");
		systemIdToFilename.put("http://www.w3.org/TR/html4/frameset.dtd", "w3c/html4-frameset.dtd");
		systemIdToFilename.put("http://www.w3.org/Math/DTD/mathml2/mathml2.dtd", "w3c/mathml2.dtd");
		systemIdToFilename.put("http://www.w3.org/Math/DTD/mathml1/mathml.dtd", "w3c/mathml.dtd");
		systemIdToFilename.put("http://www.w3.org/2002/04/xhtml-math-svg/xhtml-math-svg.dtd", "w3c/xhtml-math-svg.dtd");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inlstruct-1.mod", "w3c/xhtml-inlstruct-1.dtd");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inlphras-1.mod", "w3c/xhtml-inlphras-1.dtd");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-blkstruct-1.mod", "w3c/xhtml-blkstruct-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-blkphras-1.mod", "w3c/xhtml-blkphras-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-applet-1.mod", "w3c/xhtml-applet-1.dtd");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-blkpres-1.mod", "w3c/xhtml-blkpres-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-basic-form-1.mod", "w3c/xhtml-basic-form-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-basic-table-1.mod", "w3c/xhtml-basic-table-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-frames-1.mod", "w3c/xhtml-frames-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-target-1.mod", "w3c/xhtml-target-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-iframe-1.mod", "w3c/xhtml-iframe-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-events-1.mod", "w3c/xhtml-events-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-nameident-1.mod", "w3c/xhtml-nameident-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-legacy-redecl-1.mod", "w3c/xhtml-legacy-redecl-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inlpres-1.mod", "w3c/xhtml-inlpres-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-arch-1.mod", "w3c/xhtml-arch-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-notations-1.mod", "w3c/xhtml-notations-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-qname-1.mod", "w3c/xhtml-qname-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-attribs-1.mod", "w3c/xhtml-attribs-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-charent-1.mod", "w3c/xhtml-charent-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-basic11-model-1.mod", "w3c/xhtml-basic11-model-1.mod");
		systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inputmode-1.mod", "w3c/xhtml-inputmode-1.mod");
		systemIdToFilename.put("http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd", "w3c/svg11.dtd");
		//
		systemIdToPublicId.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "-//W3C//DTD XHTML 1.0 Strict//EN");
		systemIdToPublicId.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
				"-//W3C//DTD XHTML 1.0 Transitional//EN");
		systemIdToPublicId.put("http://www.w3.org/MarkUp/DTD/xhtml11.dtd", "-//W3C//DTD XHTML 1.1//EN");
		systemIdToPublicId.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-lat1.ent",
				"-//W3C//ENTITIES Latin 1 for XHTML//EN");
		systemIdToPublicId.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-symbol.ent",
				"-//W3C//ENTITIES Symbols for XHTML//EN");
		systemIdToPublicId.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-special.ent",
				"-//W3C//ENTITIES Special for XHTML//EN");
		systemIdToPublicId.put("http://www.w3.org/TR/html4/strict.dtd", "-//W3C//DTD HTML 4.01//EN");
		systemIdToPublicId.put("http://www.w3.org/TR/html4/loose.dtd", "-//W3C//DTD HTML 4.01 Transitional//EN");
		systemIdToPublicId.put("http://www.w3.org/TR/html4/frameset.dtd", "-//W3C//DTD HTML 4.01 Frameset//EN");
		systemIdToPublicId.put("http://www.w3.org/Math/DTD/mathml2/mathml2.dtd", "-//W3C//DTD MathML 2.0//EN");
		systemIdToPublicId.put("http://www.w3.org/Math/DTD/mathml1/mathml.dtd", "math");
		systemIdToPublicId.put("http://www.w3.org/2002/04/xhtml-math-svg/xhtml-math-svg.dtd",
				"-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN");
		systemIdToPublicId.put("http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd", "-//W3C//DTD SVG 1.1//EN");
		if (enableWhitelist) {
			whitelist = new HashSet<String>(1);
		}
	}

	/**
	 * Add the given host to a whitelist for remote DTD fetching.
	 * <p>
	 * If the whitelist is enabled, only http or https URLs will be allowed.
	 * </p>
	 * 
	 * @param fqdn
	 *            the fully qualified domain name to add to the whitelist.
	 */
	public void addHostToWhiteList(String fqdn) {
		if (fqdn != null) {
			if (whitelist == null) {
				whitelist = new HashSet<String>(4);
			}
			whitelist.add(fqdn.toLowerCase(Locale.ROOT));
		}
	}

	@Override
	public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
		InputSource is;
		if ("html".equalsIgnoreCase(name)) {
			is = resolveEntity("[dtd]", XHTML1_TRA_PUBLICID, baseURI, XHTML1_TRA_SYSTEMID);
			is.setPublicId(null);
			is.setSystemId(null);
		} else if ("svg".equalsIgnoreCase(name)) {
			is = resolveEntity("[dtd]", SVG11_PUBLICID, baseURI, SVG11_SYSTEMID);
			is.setPublicId(null);
			is.setSystemId(null);
		} else {
			// This method can return null safely: there is no SystemId URL to connect to.
			is = null;
		}
		return is;
	}

	@Override
	public final InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
			throws SAXException, IOException {
		if (publicId == null) {
			publicId = systemIdToPublicId.get(systemId);
		} else if (systemId == null) {
			systemId = getSystemIdFromPublicId(publicId);
		}
		String fname = systemIdToFilename.get(systemId);
		InputSource isrc = null;
		if (fname != null) {
			Reader re = loadDTDfromClasspath(fname);
			if (re != null) {
				isrc = new InputSource(re);
				isrc.setPublicId(publicId);
				if (systemId != null) {
					isrc.setSystemId(systemId);
				}
			}
		} else if (systemId != null) {
			URL enturl;
			if (baseURI != null) {
				URL base = new URL(baseURI);
				enturl = new URL(base, systemId);
			} else {
				enturl = new URL(systemId);
			}
			if (isInvalidProtocol(enturl.getProtocol())) {
				throw new SAXException("Invalid url protocol: " + enturl.getProtocol());
			}
			if (isWhitelistEnabled() && !isWhitelistedHost(enturl.getHost())) {
				throw new SAXException(
						"Whitelist is enabled, and attempted to retrieve data from " + enturl.toExternalForm());
			}
			boolean invalidPath = isInvalidPath(enturl.getPath());
			String charset = "UTF-8";
			URLConnection con = openConnection(enturl);
			connect(con);
			String conType = con.getContentType();
			if (conType != null) {
				int sepidx = conType.indexOf(';');
				if (sepidx != -1 && sepidx < conType.length()) {
					conType = conType.substring(0, sepidx);
					charset = AgentUtil.findCharset(conType, sepidx + 1);
				}
			}
			if (invalidPath && !isValidContentType(conType)) {
				// Disconnect
				if (con instanceof HttpURLConnection) {
					((HttpURLConnection) con).disconnect();
				}
				throw new SAXException("Invalid url: " + enturl.toExternalForm());
			}
			isrc = new InputSource();
			isrc.setSystemId(enturl.toExternalForm());
			if (publicId != null) {
				isrc.setPublicId(publicId);
			}
			isrc.setEncoding(charset);
			InputStream is;
			try {
				is = con.getInputStream();
			} catch (FileNotFoundException e) {
				return null;
			}
			isrc.setCharacterStream(new InputStreamReader(is, charset));
		} else {
			isrc = getExternalSubset(name, baseURI);
		}
		return isrc;
	}

	private String getSystemIdFromPublicId(String publicId) {
		Iterator<Entry<String, String>> it = systemIdToPublicId.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			if (publicId.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	protected boolean isInvalidPath(String path) {
		int len = path.length();
		String ext;
		return len < 5 || (!(ext = path.substring(len - 4)).equalsIgnoreCase(".dtd") && !ext.equalsIgnoreCase(".ent")
				&& !ext.equalsIgnoreCase(".mod"));
	}

	/**
	 * Is the whitelist enabled ?
	 * 
	 * @return <code>true</code> if the whitelist is enabled.
	 */
	protected boolean isWhitelistEnabled() {
		return whitelist != null;
	}

	/**
	 * Is the given protocol not supported by this resolver ?
	 * 
	 * @param protocol
	 *            the protocol.
	 * @return <code>true</code> if this resolver considers the given protocol invalid.
	 */
	protected boolean isInvalidProtocol(String protocol) {
		return !protocol.equals("http") && !protocol.equals("https");
	}

	/**
	 * Is the given host whitelisted ?
	 * 
	 * @param host
	 *            the host to test.
	 * @return <code>true</code> if the given host is whitelisted.
	 */
	protected boolean isWhitelistedHost(String host) {
		return whitelist.contains(host.toLowerCase(Locale.ROOT));
	}

	/**
	 * Open a connection to the given URL.
	 * 
	 * @param url the URL to connect to.
	 * @return the connection.
	 * @throws IOException if an I/O error happened opening the connection.
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		return url.openConnection();
	}

	/**
	 * Connect the given <code>URLConnection</code>.
	 * 
	 * @param con
	 *            the <code>URLConnection</code>.
	 * @throws IOException
	 *             if a problem happened connecting.
	 */
	protected void connect(final URLConnection con) throws IOException {
		con.setConnectTimeout(60000);
		try {
			java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<Void>() {
				@Override
				public Void run() throws IOException {
					con.connect();
					return null;
				}
			});
		} catch (PrivilegedActionException e) {
			throw (IOException) e.getException();
		}
	}

	/**
	 * Is the given string a valid DTD/entity content-type ?
	 * 
	 * @param conType
	 *            the content-type.
	 * @return <code>true</code> if it is a valid DTD/entity content-type
	 */
	protected boolean isValidContentType(String conType) {
		return conType != null
				&& (conType.equals("application/xml-dtd") || conType.equals("text/xml-external-parsed-entity")
						|| conType.equals("application/xml-external-parsed-entity"));
	}

	@Override
	public final InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		return resolveEntity(null, publicId, null, systemId);
	}

	public InputSource resolveEntity(DocumentTypeDeclaration dtDecl) throws SAXException, IOException {
		return resolveEntity(dtDecl.getName(), dtDecl.getPublicId(), null, dtDecl.getSystemId());
	}

	public InputSource resolveEntity(DocumentType dtDecl) throws SAXException, IOException {
		return resolveEntity(dtDecl.getName(), dtDecl.getPublicId(), dtDecl.getBaseURI(), dtDecl.getSystemId());
	}

	public InputSource resolveEntity(String documentTypeDeclaration) throws SAXException, IOException {
		return resolveEntity(DocumentTypeDeclaration.parse(documentTypeDeclaration));
	}

	public void setClassLoader(ClassLoader loader) {
		this.loader = loader;
	}

	private Reader loadDTDfromClasspath(final String dtdFilename) {
		final String resPath;
		if (dtdFilename.charAt(0) != '/') {
			// relative
			String pkgPath = DefaultEntityResolver.class.getPackage().getName().replace('.', '/');
			StringBuilder buf = new StringBuilder(pkgPath.length() + dtdFilename.length() + 2);
			buf.append('/').append(pkgPath).append('/').append(dtdFilename);
			resPath = buf.toString();
		} else {
			// All filenames must be relative
			throw new AccessControlException("Attempt to read " + dtdFilename);
		}
		InputStream is = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				if (loader != null) {
					return loader.getResourceAsStream(resPath);
				} else {
					return this.getClass().getResourceAsStream(resPath);
				}
			}
		});
		Reader re = null;
		if (is != null) {
			try {
				re = new InputStreamReader(is, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// Should not happen, but...
				re = new InputStreamReader(is);
			}
		}
		return re;
	}
}
