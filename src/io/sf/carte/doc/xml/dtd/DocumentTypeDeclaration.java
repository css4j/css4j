/*

 Copyright (c) 1998-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.xml.dtd;

import org.xml.sax.SAXException;

import io.sf.jclf.text.TokenParser;

/**
 * Document Type Declaration.
 * 
 * @author Carlos Amengual
 */
public class DocumentTypeDeclaration {

	private String name;

	private String publicId;

	private String systemId;

	/**
	 * XHTML 1.1 transitional DTD.
	 */
	public static final String XHTML1_TRA_DTDECL = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";

	/**
	 * XHTML 1.1 transitional public ID.
	 */
	public static final String XHTML1_TRA_PUBLICID = "-//W3C//DTD XHTML 1.0 Transitional//EN";

	public DocumentTypeDeclaration(String publicId) {
		this(null, publicId, null);
	}

	public DocumentTypeDeclaration(String name, String publicId, String systemId) {
		super();
		this.name = name;
		this.publicId = publicId;
		this.systemId = systemId;
	}

	public String getPublicId() {
		return publicId;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Parses an XML document type declaration.
	 * 
	 * @param documentTypeDeclaration
	 *            the document type declaration.
	 * @return the corresponding DocumentTypeDeclaration object.
	 * @throws SAXException
	 *             if the document type declaration could not be parsed.
	 */
	public static DocumentTypeDeclaration parse(String documentTypeDeclaration) throws SAXException {
		int i = documentTypeDeclaration.indexOf('<');
		int j = documentTypeDeclaration.lastIndexOf('>');
		if (j < i || j <= 0) {
			throw new SAXException("Not a Document Type Declaration: " + documentTypeDeclaration);
		}
		TokenParser tp = new TokenParser(documentTypeDeclaration.substring(i + 1, j), " ");
		if (!tp.hasNext() || !tp.nextToken().equals("!DOCTYPE") || !tp.hasNext()) {
			throw new SAXException("Not a Document Type Declaration: " + documentTypeDeclaration);
		}
		String name = tp.nextToken();
		if (!tp.hasNext()) {
			throw new SAXException("Unparsable Document Type Declaration: " + documentTypeDeclaration);
		}
		String idtype = tp.nextToken();
		if (!tp.hasNext()) {
			throw new SAXException("Unparsable Document Type Declaration: " + documentTypeDeclaration);
		}
		String id = tp.nextToken();
		String publicId = null;
		String systemId = null;
		if (idtype.equals("PUBLIC")) {
			publicId = id;
			if (tp.hasNext()) {
				systemId = tp.nextToken();
			}
		} else if (idtype.equals("SYSTEM")) {
			systemId = id;
		}
		return new DocumentTypeDeclaration(name, publicId, systemId);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(128);
		buf.append("<!DOCTYPE ").append(name);
		if (publicId != null) {
			buf.append(" PUBLIC \"").append(publicId).append('"');
		}
		if (systemId != null) {
			if (publicId == null) {
				buf.append(" SYSTEM");
			}
			buf.append(" \"").append(systemId).append('"');
		}
		buf.append('>');
		return buf.toString();
	}

}
